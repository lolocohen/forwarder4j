/*
 * Forward4j.
 * Copyright (C) 2015 Forward4j Team.
 * http://
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

package org.forward4j;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.*;

/**
 * 
 * @author Laurent Cohen
 */
class Connection implements Runnable, AutoCloseable {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Connection.class);
  private SocketWrapper socketWrapper;
  private List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();
  private final byte[] buffer = new byte[Utils.TEMP_BUFFER_SIZE];
  private long totalRead = 0L;
  private long totalWritten = 0L;

  public Connection(final Socket socket) throws Exception {
    socketWrapper = new SocketWrapper(socket);
  }

  public Connection(final String host, final int port) throws Exception {
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

  public void send(final byte[] data) throws Exception {
    log.debug("writing {} bytes to {}", data.length, this);
    socketWrapper.write(data, 0, data.length);
    totalWritten += data.length;
  }

  public void addConnectionListener(ConnectionListener listener) {
    if (listener != null) listeners.add(listener);
  }

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
