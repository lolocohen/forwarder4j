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

import java.net.*;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.*;

/**
 * This is the main class for aunching Fowarder4j.
 * @author Laurent Cohen
 */
public class Forwarder implements Runnable {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Forwarder.class);
  /**
   * Prefix for all configuration properties.
   */
  private final static String PREFIX = "forwarder4j.";
  /**
   * A simple pattern to validate the CLI args.
   */
  private static final Pattern CLI_ARG_PATTERN = Pattern.compile("[0-9]+=.*");
  /**
   * The incoming local port.
   */
  private final int inPort;
  /**
   * The destination remote host and port.
   */
  private final HostPort outDest;

  public static void main(String[] args) {
    try {
      final Map<Integer, String> ports = new TreeMap<>();
      
      if ((args != null) && (args.length > 0)) {
        for (final String arg: args) {
          if (CLI_ARG_PATTERN.matcher(arg).matches()) {
            final int idx = arg.indexOf('=');
            createForwarder(arg.substring(0, idx), arg.substring(idx + 1), ports);
          } else {
            System.out.printf("Argument '%s' does not conform to the pattern '<local_port>=<host>:<port>'\n", arg);
          }
        }
      }

      final Config config = Config.getConfiguration();
      final String servicePrefix = PREFIX + "service.";
      final Config defs = config.filter((name, value) -> (name != null) && name.startsWith(servicePrefix));
      final Set<String> names = defs.stringPropertyNames();
      if ((names != null) && !names.isEmpty()) {
        for (String name: names) {
          final String s = name.substring(servicePrefix.length());
          createForwarder(s, defs.getProperty(name), ports);
        }
      }

      if (ports.isEmpty()) {
        System.out.println("No entry defined, exiting.");
        System.exit(0);
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
   */
  private static void createForwarder(final String portStr, final String target, final Map<Integer, String> allPorts) {
    final int port;
    try {
      port = Integer.valueOf(portStr);
    } catch(NumberFormatException e) {
      log.error(String.format("%s. '%s' is not a valid port number, ignoring it", e, portStr));
      return;
    }
    if (!allPorts.containsKey(port)) {
      allPorts.put(port, target);
      HostPort hp = HostPort.fromString(target);
      Forwarder server = new Forwarder(port, hp);
      System.out.printf("Forwarding local port %d to %s%n", port, hp);
      new Thread(server, "Server-" + port).start();
    } else {
      System.out.printf("Port %d is already mapped to %s, cannot map it again to %s\n", port, allPorts.get(port), target);
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
      final ServerSocket server = new ServerSocket(inPort);
      server.setReceiveBufferSize(Utils.SOCKET_BUFFER_SIZE);
      while (true) {
        final Socket socket = server.accept();
        try {
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
          log.error(e.getMessage(), e);
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
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
}
