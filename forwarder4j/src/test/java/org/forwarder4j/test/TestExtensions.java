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

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Laurent Cohen
 */
public class TestExtensions implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger("TEST");

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    printClassInfo(String.format("start of class %s", context.getRequiredTestClass().getName()));
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    printClassInfo(String.format("end of class %s", context.getRequiredTestClass().getName()));
    System.out.printf("***** end of class %s *****\n", context.getRequiredTestClass().getName());
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    log.info(String.format(">>>>> start of method %s()", context.getRequiredTestMethod().getName()));
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    log.info(String.format("<<<<< end of method %s()", context.getRequiredTestMethod().getName()));
  }


  private static void printClassInfo(final String info) {
    final int len = info.length() + 12;
    final StringBuilder sb = new StringBuilder(len);
    for (int i=0; i<len; i++) sb.append('*');
    final String bar = sb.toString();
    final String message = String.format("\n%s\n***** %s *****\n%s", bar, info, bar);
    //System.out.printf("\n%s\n***** %s *****\n", info);
    log.info(message);
  }
}
