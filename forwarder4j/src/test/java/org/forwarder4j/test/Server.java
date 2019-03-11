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

package org.forwarder4j.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Laurent Cohen
 */
public class Server implements Runnable, AutoCloseable {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Server.class);
  private final ServerSocket server;
  private final AtomicBoolean closed = new AtomicBoolean();
  private final List<ServerConnection> connections = new CopyOnWriteArrayList<>();

  public Server(final int port) throws IOException {
    server = new ServerSocket(port);
  }

  @Override
  public void run() {
    try {
      while (!isClosed()) {
        final Socket socket = server.accept();
        final ServerConnection connection = new ServerConnection(socket);
        connections.add(connection);
        new Thread(connection).start();
      }
    } catch (final Exception e) {
      if (!isClosed()) log.error(e.getMessage(), e);
    }
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      try {
        server.close();
        for (final ServerConnection connection: connections) {
          connection.close();
        }
      } catch (final Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  public boolean isClosed() {
    return closed.get();
  }
}
