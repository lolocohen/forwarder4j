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

import static org.junit.jupiter.api.Assertions.fail;

import org.forwarder4j.test.TestExtensions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 
 * @author Laurent Cohen
 */
@ExtendWith(TestExtensions.class)
public class BaseTest {
  protected static final int REMOTE_PORT = 10_000;

  @BeforeEach
  @ExtendWith(TestExtensions.class)
  public void setupInstance() throws Exception {
  }

  @AfterEach
  @ExtendWith(TestExtensions.class)
  public void teardownInstance() throws Exception {
  }

  /**
   * Wait until the specified condition is fulfilled, or the timeout expires, whichever happens first.
   * @param condition the condition to check.
   * @param millis the milliseconds part of the timeout. A value of zero means an infinite timeout.
   * @param sleepInterval how long to wait between evaluations of the condition, in millis.
   * @return true if the condition is {@code null} or was fulfilled before the timeout expired, {@code false} otherwise.
   * @throws IllegalArgumentException if the millis are negative.
   */
  public static boolean assertConditionTimeout(final long millis, final long sleepInterval, final Condition condition) throws IllegalArgumentException {
    return assertConditionTimeout(String.format("exceeded timeout of %,d ms", (millis > 0L) ? millis : Long.MAX_VALUE), millis, sleepInterval, condition);
  }

  /**
   * Wait until the specified condition is fulfilled, or the timeout expires, whichever happens first.
   * @param condition the condition to check.
   * @param millis the milliseconds part of the timeout. A value of zero means an infinite timeout.
   * @param sleepInterval how long to wait between evaluations of the condition, in millis.
   * @param message the message to print in case of failure.
   * @return true if the condition is {@code null} or was fulfilled before the timeout expired, {@code false} otherwise.
   * @throws IllegalArgumentException if the millis are negative.
   */
  public static boolean assertConditionTimeout(final String message, final long millis, final long sleepInterval, final Condition condition) throws IllegalArgumentException {
    if (sleepInterval < 0L) throw new IllegalArgumentException("sleepInterval must be >= 0");
    if (condition == null) return true;
    if (millis < 0L) throw new IllegalArgumentException("millis cannot be negative");
    final long timeout = (millis > 0L) ? millis : Long.MAX_VALUE;
    boolean fulfilled = false;
    final long start = System.nanoTime();
    long elapsed = 0L;
    while (!(fulfilled = condition.evaluate()) && ((elapsed = (System.nanoTime() - start) / 1_000_000L) < timeout)) {
      try {
        Thread.sleep(sleepInterval);
      } catch (@SuppressWarnings("unused") final InterruptedException e) {
      }
    }
    if (elapsed > timeout) fail(message);
    return fulfilled;
  }

  /**
   * This interface represents a condition to evaluate to either {@code true} or {@code false}.
   */
  @FunctionalInterface
  public static interface Condition {
    /**
     * Evaluate this condition.
     * @return {@code true} if the condition is fulfilled, {@code false} otherwise.
     */
    boolean evaluate();
  }

  /**
   * This interface handles exceptions raised by its {@code evaluate()} method and returns {@code false} when it happens.
   */
  @FunctionalInterface
  public static interface ThrowingCondition extends Condition {
    @Override
    default boolean evaluate() {
      try {
        return evaluateWithException();
      } catch (@SuppressWarnings("unused") final Exception e) {
        return false;
      }
    }

    /**
     * Evaluate an arbitrary condition.
     * @return true if the condition was met, false otherwise.
     * @throws Exception if any error occurs during the evaluation.
     */
    public boolean evaluateWithException() throws Exception;
  }

  @SafeVarargs
  public static <E> boolean isOneOf(final E elt, final E...array) {
    if ((array == null) || (array.length == 0)) return false;
    for (final E tmp: array) {
      if (elt == tmp) return true;
      if ((elt != null) && (tmp != null) && elt.equals(tmp)) return true;
    }
    return false;
  }
}
