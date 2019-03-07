/*
 * Fowarder4j.
 * Copyright (C) 2015-2019 Fowarder4j Team.
 * https://github.com/lolocohen/forwarder4j
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.forwarder4j;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.forwarder4j.admin.Admin;
import org.forwarder4j.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this class represent port fowarding definitions.
 * @author Laurent Cohen
 */
public class Forwarder implements Runnable {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Forwarder.class);
  /**
   * Determines whether the debug level is enabled in the log configuration, without the cost of a method call.
   */
  private static final boolean debugEnabled = log.isDebugEnabled();
  /**
   * Prefix for all configuration properties.
   */
  private final static String PREFIX = "forwarder4j.";
  /**
   * The administration service.
   */
  private static final Admin admin = new Admin();
  /**
   * The incoming local port.
   */
  private final int inPort;
  /**
   * The destination remote host and port.
   */
  private final HostPort outDest;
  /**
   * Whether this forwarder is closed.
   */
  private AtomicBoolean closed = new AtomicBoolean(false);
  /**
   * Whether this forwarder is bound to its local port.
   */
  private AtomicBoolean bound = new AtomicBoolean(false);
  /**
   * A server socket bound to {@link #inPort}.
   */
  private ServerSocket server;

  public static void main(final String...args) {
    try {
      new Thread(admin, "Admin").start();

      final Map<Integer, String> ports = new TreeMap<>();
      
      if ((args != null) && (args.length > 0)) {
        for (final String arg: args) {
          try {
            final EntryDescriptor desc = EntryDescriptor.from(arg);
            createForwarder(desc, ports);
          } catch (final IllegalArgumentException e) {
            System.out.println(e.getMessage());
          }
        }
      }

      final Config config = Config.getConfiguration();
      final String servicePrefix = PREFIX + "service.";
      final Config defs = config.filter((name, value) -> (name != null) && name.startsWith(servicePrefix));
      final Set<String> names = defs.stringPropertyNames();
      if ((names != null) && !names.isEmpty()) {
        for (String name: names) {
          try {
            final String s = name.substring(servicePrefix.length());
            final EntryDescriptor desc = EntryDescriptor.from(s, defs.getProperty(name));
            createForwarder(desc, ports);
          } catch (final IllegalArgumentException e) {
            System.out.println(e.getMessage());
          }
        }
      }

      if (ports.isEmpty()) {
        System.out.println("No entry defined");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create a forwarder for the specified local port and target host/port.
   * @param port the local port to forward through.
   * @param target the target host and port to forward to.
   * @param allPorts the set of already defined local port entries.
   * @return the created forwarder, or {@code null} if it could not be created.
   */
  public static Forwarder createForwarder(final EntryDescriptor desc, final Map<Integer, String> allPorts) {
    if ((allPorts == null) || !allPorts.containsKey(desc.getPort())) {
      if (allPorts != null) allPorts.put(desc.getPort(), desc.getTarget().toString());
      Forwarder server = new Forwarder(desc.getPort(), desc.getTarget());
      System.out.printf("Forwarding local port %d to %s%n", desc.getPort(), desc.getTarget());
      admin.setEntry(desc.getPort(), server);
      new Thread(server, "Server-" + desc.getPort()).start();
      return server;
    } else {
      System.out.printf("Port %d is already mapped to %s, cannot map it again to %s\n", desc.getPort(), allPorts.get(desc.getPort()), desc.getTarget());
      return null;
    }
  }

  /**
   * Initialize this forwarder with the specified incoming port and outbound destination.
   * @param inPort the incoming local port.
   * @param outDest the destination remote host and port.
   */
  private Forwarder(final int inPort, final HostPort outDest) {
    this.inPort = inPort;
    this.outDest = outDest;
  }

  @Override
  public void run() {
    try {
      log.debug(String.format("Forwarding local port %d to %s", inPort, outDest));
      final int max = 5;
      int attempts = 0;
      while (!bound.get() && (attempts < max)) {
        try {
          server = new ServerSocket(inPort);
          bound.set(true);
          if (debugEnabled) log.debug("bound to port {} on attempt {}/{}", inPort, attempts + 1, max);
        } catch (final BindException e) {
          attempts++;
          if (attempts >= max) {
            if (debugEnabled) log.debug("failed to bind to port {} after {} attempts", inPort, max);
            throw e;
          }
          if (debugEnabled) log.debug("could not bind to port {} on attempt {}/{}", inPort, attempts, max);
          Thread.sleep(1000L);
        }
      }
      server.setReceiveBufferSize(Utils.SOCKET_BUFFER_SIZE);
      while (!closed.get()) {
        Socket socket = null;
        try {
          socket = server.accept();
          socket.setReceiveBufferSize(Utils.SOCKET_BUFFER_SIZE);
          socket.setSendBufferSize(Utils.SOCKET_BUFFER_SIZE);
          log.debug("accepted socket {}", socket);
          final Connection in = new Connection(socket);
          final Connection out = new Connection(outDest.host, outDest.port);
          in.addConnectionListener(new Listener(out));
          out.addConnectionListener(new Listener(in));
          out.run();
          in.run();
        } catch (Exception e) {
          if (!closed.get()) log.error(e.getMessage(), e);
          else log.info("Forwarder [{}] was closed", this);
        }
      }
    } catch (final Exception e) {
      closed.set(true);
      log.error(e.getMessage(), e);
    }
  }

  public void close() throws IOException {
    if (closed.compareAndSet(false, true)) {
      if (debugEnabled) log.debug("closing Forwarder[{}]", this);
      bound.set(false);
      server.close();
    }
  }

  /**
   * Listens to connection events from a connection and forwards the data read to another connection.
   */
  private class Listener implements ConnectionListener {
    /**
     * The connection to forward data to.
     */
    private final Connection otherConnection;

    /**
     * Intiialize this listener with the specified connection.
     * @param otherConnection he connection to forward data to.
     */
    public Listener(final Connection otherConnection) {
      this.otherConnection = otherConnection;
    }

    @Override
    public void incomingData(final ConnectionEvent event) {
      try {
        final int len = event.getData().length;
        log.debug("writing {} bytes to {}", len, otherConnection);
        otherConnection.offer(event.getData());
      } catch(Exception e) {
        log.debug(e.getMessage(), e);
      }
    }

    @Override
    public void throwableRaised(final ConnectionEvent event) {
      log.debug("received throwable from {} : ", event.getConnection(), event.getThrowable());
      try {
        otherConnection.close();
      } catch(Exception e) {
        log.debug(e.getMessage(), e);
      }
    }
  }

  @Override
  public String toString() {
    return Integer.toString(inPort) + "=" + outDest;
  }

  public boolean isBound() {
    return bound.get();
  }

  public boolean isClosed() {
    return closed.get();
  }
}
