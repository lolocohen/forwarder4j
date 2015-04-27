/*
 * Forward4j.
 * Copyright (C) 2015 Forward4j Team.
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

import org.slf4j.*;


/**
 * Utiloty methods and constants.
 * @author Laurent Cohen
 */
class Utils {
  /**
   * Size of send and receive buffer for socket connections. Defaults to 32768.
   */
  static int SOCKET_BUFFER_SIZE = Config.getConfiguration().getInt("forward4j.socket.buffer.size", 32*1024);
  /**
   * Disable Nagle's algorithm to improve performance. Defaults to true.
   */
  static boolean SOCKET_TCP_NODELAY = Config.getConfiguration().getBoolean("forward4j.socket.tcp_nodelay", true);
  /**
   * Enable / disable keepalive. Defaults to false.
   */
  static boolean SOCKET_KEEPALIVE = Config.getConfiguration().getBoolean("forward4j.socket.keepalive", false);
  /**
   * Size of temporary buffers (including direct buffers) used in I/O transfers. Defaults to 32768.
   */
  static int TEMP_BUFFER_SIZE = Config.getConfiguration().getInt("forward4j.temp.buffer.size", 32*1024);

  /**
   * Attempt to close the specified closeable without logging an eventual error.
   * @param closeable the closeable to close.
   * @throws Exception if any error occurs while closing the closeable.
   */
  public static void close(final AutoCloseable closeable) throws Exception {
    closeable.close();
  }

  /**
   * Attempt to silently close (no exception logging) the specified closeable.
   * @param closeable the closeable to close.
   */
  public static void closeSilent(final AutoCloseable closeable) {
    close(closeable, null);
  }

  /**
   * Attempt to close the specified closeable and log any eventual error.
   * @param closeable the closeable to close.
   * @param log the logger to use; if null no logging occurs.
   */
  public static void close(final AutoCloseable closeable, final Logger log) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        if (log != null) {
          String s = "unable to close stream/reader/writer: " + getMessage(e);
          if (log.isDebugEnabled()) log.debug(s, e);
          else log.warn(s);
        }
      }
    }
  }

  /**
   * Get the message of the specified <code>Throwable</code> along with its class name.
   * @param t the <code>Throwable</code> object from which to get the message.
   * @return a formatted message from the <code>Throwable</code>.
   */
  public static String getMessage(final Throwable t) {
    if (t == null) return "null";
    return t.getClass().getName() + ": " + t.getMessage();
  }
}
