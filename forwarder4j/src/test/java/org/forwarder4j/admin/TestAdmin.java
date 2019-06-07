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

package org.forwarder4j.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.forwarder4j.BaseTest;
import org.forwarder4j.Forwarder;
import org.forwarder4j.test.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Laurent Cohen
 */
public class TestAdmin extends BaseTest {
  private static Server server;

  @BeforeAll
  public static void setup() throws Exception {
    server = new Server(REMOTE_PORT);
    new Thread(server).start();
    Forwarder.main(new String[0]);
  }

  @AfterAll
  public static void teardown() throws Exception {
    server.close();
    //Admin.executeCommand("localhost", 8192, "stop").trim();;
  }

  @Test()
  public void testAddAndRemoveOutput() throws Exception {
    String output = Admin.executeCommand("localhost", 8192, "+11000=localhost:10000").trim();
    assertEquals("forwarding port 11000 to localhost:10000", output);
    output = Admin.executeCommand("localhost", 8192, "-11000").trim();
    assertEquals("port definition for '11000' was removed", output);
  }

  @Test()
  public void testAddListRemoveListOutput() throws Exception {
    String output = Admin.executeCommand("localhost", 8192, "+11000=localhost:10000").trim();
    assertEquals("forwarding port 11000 to localhost:10000", output);
    output = Admin.executeCommand("localhost", 8192, "list").trim();
    assertEquals("List of entries:\n- 11000=localhost:10000", output);
    output = Admin.executeCommand("localhost", 8192, "-11000").trim();
    assertEquals("port definition for '11000' was removed", output);
    output = Admin.executeCommand("localhost", 8192, "list").trim();
    assertEquals("No entry defined", output);
  }
}
