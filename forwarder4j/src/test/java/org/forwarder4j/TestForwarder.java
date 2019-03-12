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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.forwarder4j.test.ClientConnection;
import org.forwarder4j.test.Server;
import org.forwarder4j.test.ServerConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Laurent Cohen
 */
public class TestForwarder extends BaseTest {
  private static final int REMOTE_PORT = 10_000;
  private static Server server;

  @BeforeAll
  public static void setup() throws Exception {
    server = new Server(REMOTE_PORT);
  }

  @AfterAll
  public static void teardown() throws Exception {
    server.close();
  }

  @Test()
  public void testSimpleForwarding() throws Exception {
    final int forwardingPort = 11_000;
    new Thread(server).start();
    try (final Forwarder forwarder = new Forwarder(forwardingPort, HostPort.from("localhost:" + REMOTE_PORT))) {
      new Thread(forwarder).start();
      assertConditionTimeout(2000L, 50L, () -> forwarder.isBound());
      try (final ClientConnection connection = new ClientConnection(forwardingPort)) {
        final String msg = "hello forwarder4j!";
        final String response = connection.send(msg).receive();
        assertEquals(String.format(ServerConnection.RESPONSE_FORMAT, REMOTE_PORT, msg), response);
      }
    }
  }

  @Test()
  public void testForwarderMain() throws Exception {
    final int[] ports = { 11_000, 11_001 };
    final String target = "localhost:" + REMOTE_PORT;
    final String[] args = new String[ports.length];
    for (int i=0; i<ports.length; i++) args[i] = ports[i] + "=" + target;
    Forwarder.main(args);;
    Map<Integer, Forwarder> map = Forwarder.getAdmin().getForwarderMap();
    assertNotNull(map);
    assertEquals(ports.length, map.size());
    for (final Forwarder forwarder: map.values()) assertConditionTimeout(2000L, 50L, () -> forwarder.isBound());
    for (final int port: ports) {
      try (final ClientConnection connection = new ClientConnection(port)) {
        final String msg = "hello forwarder4j!";
        final String response = connection.send(msg).receive();
        assertEquals(String.format(ServerConnection.RESPONSE_FORMAT, REMOTE_PORT, msg), response);
      }
    }
    for (final Forwarder forwarder: map.values()) {
      forwarder.close();
      assertConditionTimeout(2000L, 50L, () -> forwarder.isClosed());
    }
  }
}
