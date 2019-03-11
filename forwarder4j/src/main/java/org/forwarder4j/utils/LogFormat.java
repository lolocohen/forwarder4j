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

package org.forwarder4j.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * A formatter similar to {@code java.util.logging.SimpleFormatter}, with additional available parameters, including the thread id and thread name.
 * <p>The format is declared in the logging properties file with the "{@code org.forwarder4j.utils.LogFormat.format}" property.
 */
public class LogFormat extends Formatter {
  private static final String DEFAULT_FORMAT = "%1$tF %1$tT.%1$tL [%5$-7s][%7$20.20s][%3$s.%4$s()] %8$s%n";
  private static final String format = retrieveFormat();

  private static String retrieveFormat() {
    String format = LogManager.getLogManager().getProperty(LogFormat.class.getName() + ".format");
    if (format == null) {
      format = DEFAULT_FORMAT;
    } else {
      try {
        String.format(format, new Date(), "", "", "", "", 0, "", "");
      } catch (@SuppressWarnings("unused") final Exception e) {
        System.out.println("logging format '" + format + "' is invalid, using default: '" + DEFAULT_FORMAT + "'");
        format = DEFAULT_FORMAT;
      }
    }
    return format;
  }

  /**
   * Format the specified {@link LogRecord} using a {@link java.util.Formatter format string} where the parameters are in the following order:
   * <ol>
   * <li>date (a {@link Date})</li>
   * <li>loggerName (a {@link String})</li>
   * <li>className (a {@link String})</li>
   * <li>methodName (a {@link String})</li>
   * <li>logging level (a {@link String})</li>
   * <li>threadId (an {@code int})</li>
   * <li>threadName (a {@link String})</li>
   * <li>message (a {@link String})</li>
   * </ol>
   * <p>For example, the format : {@code %1$tF %1$tT.%1$tL [%5$-7s][%6$08d][%3$s.%4$s()] %7$s%n}<br>
   * results in: {@code 2019-03-05 06:05:04.321 [INFO   ][00000057][somepackage.SomeClass.someMethod()] some message}
   * @param record the log record to format.
   * @return a formatted string representtion of the log record.
   */
  @Override
  public String format(final LogRecord record) {
    final Date date = new Date(record.getMillis());
    final String loggerName = record.getLoggerName();
    String className = record.getSourceClassName();
    className = (className == null) ? "<unknown class>" : className;
    String methodName = record.getSourceMethodName();
    methodName = (methodName == null) ? "<unknown method>" : methodName;
    final String level = record.getLevel().toString();
    final int threadId = record.getThreadID();
    final ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(threadId);
    final String threadName = threadInfo.getThreadName();
    final String message = record.getMessage();
    String formatted = String.format(format, date, loggerName, className, methodName, level, threadId, threadName, message);
    final Throwable t = record.getThrown();
    if (t != null) formatted += Utils.getStackTrace(t);
    return formatted;
  }
}
