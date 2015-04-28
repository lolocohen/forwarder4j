/*
 * Fowarder4j.
 * Copyright (C) 2015 Fowarder4j Team.
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

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.*;

/**
 * Instances of this class represent a network connection with the ability to notify registered listners of incoming data and I/O errors.
 * @author Laurent Cohen
 */
class Connection implements Runnable, AutoCloseable {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Connection.class);
  /**
   * A wrapper for the underlying socket connection.
   */
  private SocketWrapper socketWrapper;
  /**
   * The listeners registered witrht his connection.
   */
  private List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();
  /**
   * Temporary buffer used to read data from the socket connection.
   */
  private final byte[] buffer = new byte[Utils.TEMP_BUFFER_SIZE];
  /**
   * COunt of all bytes read.
   */
  private long totalRead = 0L;
  /**
   * Count of all written bytes.
   */
  private long totalWritten = 0L;

  /**
   * Initialize from the specified established socket conneciton.
   * @param socket the underlying socket for this connection.
   * @throws IOException if any I/O error occurs.
   */
  public Connection(final Socket socket) throws IOException {
    socketWrapper = new SocketWrapper(socket);
  }

  /**
   * Initialize this connection with the specified host and port.
   * @param host the host to connect to.
   * @param port the port to connect to on the host.
   * @throws IOException if any I/O error occurs while establishing the connection.
   */
  public Connection(final String host, final int port) throws IOException {
    socketWrapper = new SocketWrapper(host, port);
    log.debug("opened connection to {}", this);
  }

  @Override
  public void run() {
    try {
      while (true) {
        int n = socketWrapper.read(buffer, 0, buffer.length);
        if (n < 0) throw new EOFException("EOF on " + socketWrapper);
        else if (n > 0) {
          log.debug("read {} bytes from {}", n, this);
          totalRead += n;
          byte[] tmp = new byte[n];
          System.arraycopy(buffer, 0, tmp, 0, n);
          ConnectionEvent event = new ConnectionEvent(this, tmp, null);
          for (ConnectionListener listener: listeners) listener.incomingData(event);
        }
        else Thread.sleep(10L);
      }
    } catch (Exception e) {
      ConnectionEvent event = new ConnectionEvent(this, null, e);
      for (ConnectionListener listener: listeners) listener.throwableRaised(event);
    }
  }

  /**
   * Send the specified data through this onnection.
   * @param data the data to send.
   * @throws IOException if any I/O error occurs.
   */
  public void send(final byte[] data) throws IOException {
    log.debug("writing {} bytes to {}", data.length, this);
    socketWrapper.write(data, 0, data.length);
    totalWritten += data.length;
  }

  /**
   * Add a listener to this connection.
   * @param listener the listener to add. If {@code null}, then this method has no effect.
   */
  public void addConnectionListener(ConnectionListener listener) {
    if (listener != null) listeners.add(listener);
  }

  /**
   * Remove a listener from this connection.
   * @param listener the listener to remove. If {@code null}, then this method has no effect.
   */
  public void removeConnectionListener(ConnectionListener listener) {
    if (listener != null) listeners.remove(listener);
  }

  @Override
  public void close() {
    try {
      if (socketWrapper != null) socketWrapper.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    sb.append(socketWrapper.getHost()).append(':').append(socketWrapper.getPort());
    sb.append(", totalRead=").append(totalRead);
    sb.append(", totalWritten=").append(totalWritten);
    sb.append(']');
    return sb.toString();
  }
}
