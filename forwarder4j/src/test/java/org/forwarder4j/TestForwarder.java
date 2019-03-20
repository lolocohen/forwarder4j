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

import java.util.Arrays;
import java.util.Map;

import org.forwarder4j.test.ClientConnection;
import org.forwarder4j.test.Server;
import org.forwarder4j.test.ServerConnection;
import org.forwarder4j.test.TestExtensions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Laurent Cohen
 */
public class TestForwarder extends BaseTest {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(TestForwarder.class);
  private static final int REMOTE_PORT = 10_000;
  private static Server server;

  @BeforeAll
  public static void setup() throws Exception {
    server = new Server(REMOTE_PORT);
    new Thread(server).start();
  }

  @AfterAll
  public static void teardown() throws Exception {
    server.close();
  }

  @BeforeEach
  @ExtendWith(TestExtensions.class)
  public void setupInstance() throws Exception {
  }

  @AfterEach
  @ExtendWith(TestExtensions.class)
  public void teardownInstance() throws Exception {
  }

  @Test()
  public void testSimpleForwarding() throws Exception {
    final int forwardingPort = 11_000;
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
  public void testSimpleForwarderMain() throws Exception {
    final Integer[] ports = { 11000, 11001 };
    final String target = "localhost:" + REMOTE_PORT;
    final String[] args = new String[ports.length];
    for (int i=0; i<ports.length; i++) args[i] = ports[i] + "=" + target;
    log.info("calling Forwarder.main({})", Arrays.toString(args)); 
    Forwarder.main(args);
    Map<Integer, Forwarder> map = Forwarder.getAdmin().getForwarderMap();
    assertNotNull(map);
    assertEquals(ports.length, map.size());
    for (final Forwarder forwarder: map.values()) {
      assertTrue(isOneOf(forwarder.getInPort(), ports));
      assertConditionTimeout(2000L, 50L, () -> forwarder.isBound());
    }
    for (final int port: ports) {
      log.info("testing connection to port {}", port); 
      try (final ClientConnection connection = new ClientConnection(port)) {
        final String msg = "hello forwarder4j!";
        log.info("getting response from port {}", port); 
        final String response = connection.send(msg).receive();
        log.info("response = {}", response); 
        assertEquals(String.format(ServerConnection.RESPONSE_FORMAT, REMOTE_PORT, msg), response);
      }
    }
    for (final Forwarder forwarder: map.values()) {
      log.info("closing {}", forwarder); 
      forwarder.close();
      assertConditionTimeout(2000L, 50L, () -> forwarder.isClosed());
    }
  }
}
