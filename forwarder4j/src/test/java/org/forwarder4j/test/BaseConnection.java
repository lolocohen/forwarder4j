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

import org.forwarder4j.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Laurent Cohen
 */
public abstract class BaseConnection<T extends BaseConnection<T>> implements AutoCloseable, Runnable {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ClientConnection.class);
  /**
   * Determines whether the debug level is enabled in the log configuration.
   */
  private static final boolean debugEnabled = log.isDebugEnabled();
  final SocketWrapper socketWrapper;

  public BaseConnection(final String host, final int port) throws IOException {
    socketWrapper = new SocketWrapper(host, port);
  }

  public BaseConnection(final Socket socket) throws IOException {
    socketWrapper = new SocketWrapper(socket);
  }

  @Override
  public void close() throws IOException {
    if (debugEnabled) log.debug("closing {}", this);
    socketWrapper.close();
  }

  @SuppressWarnings("unchecked")
  public T send(final String message) throws IOException {
    if (debugEnabled) log.debug("{} sending '{}'", this, message);
    socketWrapper.writeString(message);
    return (T) this;
  }

  public String receive() throws IOException {
    final String message = socketWrapper.readString();
    if (debugEnabled) log.debug("{} received '{}'", this, message);
    return message;
  }

  @Override
  public String toString() {
    return new StringBuilder(getClass().getSimpleName()).append('[')
      .append(socketWrapper.getHost()).append(':').append(socketWrapper.getPort())
      .append(']').toString();
  }
}
