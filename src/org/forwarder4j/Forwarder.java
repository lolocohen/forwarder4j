/*
 * Fowarder4j.
 * Copyright (C) 2015 Fowarder4j Team.
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
import java.util.concurrent.*;

import org.forwarder4j.Config.Filter;
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
   * The incoming local port.
   */
  private final int inPort;
  /**
   * The destination remote host and port.
   */
  private final HostPort outDest;
  /**
   * Executes write and close requests asynchronously and sequentially.
   */
  private final ExecutorService executor;

  public static void main(String[] args) {
    try {
      Config config = Config.getConfiguration();
      final String servicePrefix = PREFIX + "service.";
      Config defs = config.filter(new Filter() {
        @Override
        public boolean accepts(String name, String value) {
          return (name != null) && name.startsWith(servicePrefix);
        }
      });
      Set<String> names = defs.stringPropertyNames();
      if ((names == null) || names.isEmpty()) {
        System.out.println("No port forwarding definition found in the conifguration, exiting.");
        System.exit(0);
      }
      Set<Integer> ports = new TreeSet<>();
      for (String name: names) {
        String s = name.substring(servicePrefix.length());
        try {
          int n = Integer.valueOf(s);
          ports.add(n);
        } catch(NumberFormatException e) {
          log.error(String.format("property '%s' does not hold a valid port number, ignoring it", name));
        }
      }
      for (Integer n: ports) {
        String name = PREFIX + "service." + n;
        HostPort hp = HostPort.fromString(defs.getString(name));
        Forwarder server = new Forwarder(n, hp);
        System.out.printf("Forwarding local port %d to %s%n", n, hp);
        new Thread(server, "Server-" + n).start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  
  
  /**
   * Intiialize this forwarder with the specified incomin port and outbound destination.
   * @param inPort the incoming local port.
   * @param outDest the destination remote host and port.
   */
  private Forwarder(final int inPort, final HostPort outDest) {
    this.inPort = inPort;
    this.outDest = outDest;
    executor = Executors.newFixedThreadPool(1);
  }

  @Override
  public void run() {
    try {
      log.debug(String.format("Forwarding local port %d to %s", inPort, outDest));
      ServerSocket server = new ServerSocket(inPort);
      server.setReceiveBufferSize(Utils.SOCKET_BUFFER_SIZE);
      while (true) {
        final Socket socket = server.accept();
        executor.execute(new Runnable() {
          @Override
          public void run() {
            try {
              socket.setReceiveBufferSize(Utils.SOCKET_BUFFER_SIZE);
              socket.setSendBufferSize(Utils.SOCKET_BUFFER_SIZE);
              log.debug("accepted socket {}", socket);
              Connection in = new Connection(socket);
              Connection out = new Connection(outDest.host, outDest.port);
              in.addConnectionListener(new Listener(out));
              out.addConnectionListener(new Listener(in));
              new Thread(out, "outgoing-receiver-" + outDest).start();
              new Thread(in, "incoming-receiver-" + inPort).start();
            } catch (Exception e) {
              log.error(e.getMessage(), e);
            }
          }
        });
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
      executor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            final int len = event.getData().length;
            log.debug("writing {} bytes to {}", len, otherConnection);
            otherConnection.send(event.getData());
          } catch(Exception e) {
            log.debug(e.getMessage(), e);
          }
        }
      });
    }

    @Override
    public void throwableRaised(final ConnectionEvent event) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          log.debug("received throwable from {} : ", event.getConnection(), event.getThrowable());
          try {
            otherConnection.close();
          } catch(Exception e) {
            log.debug(e.getMessage(), e);
          }
        }
      });
    }
  }
}
