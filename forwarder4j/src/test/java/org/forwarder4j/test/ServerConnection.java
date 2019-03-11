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
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Laurent Cohen
 */
public class ServerConnection extends BaseConnection<ServerConnection> {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ServerConnection.class);
  public static final String RESPONSE_FORMAT = "response from %d: %s";

  public ServerConnection(Socket socket) throws IOException {
    super(socket);
  }

  @Override
  public void run() {
    try {
      while (socketWrapper.isOpened()) {
        final String message = receive();
        send(String.format(RESPONSE_FORMAT, socketWrapper.getSocket().getLocalPort(), message));
      }
    } catch (final Exception e) {
      if (socketWrapper.isOpened()) log.error(e.getMessage(), e);
    }
  }
}
