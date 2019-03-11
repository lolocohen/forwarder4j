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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.forwarder4j.test.ClientConnection;
import org.forwarder4j.test.Server;
import org.forwarder4j.test.ServerConnection;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Laurent Cohen
 */
public class TestForwarder {

  @Test()
  public void testSimpleForwarding() throws Exception {
    final int remotePort = 10_000, forwardingPort = 11_000;
    try(final Server server = new Server(remotePort)) {
      new Thread(server).start();
      try (final Forwarder forwarder = new Forwarder(forwardingPort, HostPort.from("localhost:" + remotePort))) {
        new Thread(forwarder).start();
        Thread.sleep(50L);
        try (final ClientConnection connection = new ClientConnection(forwardingPort)) {
          final String msg = "hello forwarder4j!";
          final String response = connection.send(msg).receive();
          assertEquals(String.format(ServerConnection.RESPONSE_FORMAT, remotePort, msg), response);
        }
      }
    }
  }
}
