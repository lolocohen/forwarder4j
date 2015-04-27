/*
 * Forward4j.
 * Copyright (C) 2015 Forward4j Team.
 * http://
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
import java.util.Set;
import java.util.concurrent.*;

import org.forwarder4j.Config.Filter;
import org.slf4j.*;

/**
 * 
 * @author Laurent Cohen
 */
public class Forwarder implements Runnable {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Forwarder.class);
  private final static String PREFIX = "forward4j.";
  private final int inPort;
  private final HostPort outDest;
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
      for (String name: names) {
        String s = name.substring(servicePrefix.length());
        int n = Integer.valueOf(s);
        HostPort hp = HostPort.fromString(defs.getString(name));
        Forwarder server = new Forwarder(n, hp);
        System.out.printf("Listening to incoming connections on port %d tunnelled to %s%n", n, hp);
        new Thread(server, "Server-" + n).start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Forwarder(final int inPort, final HostPort outDest) {
    this.inPort = inPort;
    this.outDest = outDest;
    executor = Executors.newFixedThreadPool(1);
  }

  @Override
  public void run() {
    try {
      log.debug(String.format("Listening to incoming connections on port %d tunnelled to %s", inPort, outDest));
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

  private class Listener implements ConnectionListener {
    private final Connection otherConnection;

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
