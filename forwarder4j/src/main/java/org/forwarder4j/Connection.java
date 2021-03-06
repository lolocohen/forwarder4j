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

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.forwarder4j.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this class represent a network connection with the ability to notify registered listners of incoming data and I/O errors.
 * @author Laurent Cohen
 */
class Connection implements Runnable, AutoCloseable {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(Connection.class);
  /**
   * Determines whether the debug level is enabled in the log configuration, without the cost of a method call.
   */
  private static final boolean debugEnabled = log.isDebugEnabled();
  /**
   * Determines whether the trace level is enabled in the log configuration, without the cost of a method call.
   */
  private static final boolean traceEnabled = log.isTraceEnabled();
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
   * Count of all bytes read.
   */
  private long totalRead;
  /**
   * Count of all written bytes.
   */
  private long totalWritten;
  /**
   * Reads from the underlying socket in a separate thread.
   */
  final Receiver receiver = new Receiver();
  /**
   * Writes to the underlying socket in a separate thread.
   */
  private final Sender sender = new Sender();
  /**
   * Whether this connection is closed.
   */
  private final AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * Initialize from the specified established socket conneciton.
   * @param socket the underlying socket for this connection.
   * @throws IOException if any I/O error occurs.
   */
  public Connection(final Socket socket) throws IOException {
    socketWrapper = new SocketWrapper(socket);
    if (debugEnabled) log.debug("created {}", this);
  }

  /**
   * Initialize this connection with the specified host and port.
   * @param host the host to connect to.
   * @param port the port to connect to on the host.
   * @throws IOException if any I/O error occurs while establishing the connection.
   */
  public Connection(final String host, final int port) throws IOException {
    socketWrapper = new SocketWrapper(host, port);
    if (debugEnabled) log.debug("opened {}", this);
  }

  @Override
  public void run() {
    try {
      final String hostPort = socketWrapper.getHost() + ":" + socketWrapper.getPort();
      if (debugEnabled) log.debug("starting sender and receiver for {}", hostPort);
      new Thread(sender, hostPort + "-sender").start();
      new Thread(receiver, hostPort + "-receiver").start();
    } catch (final Exception e) {
      ConnectionEvent event = new ConnectionEvent(this, null, e);
      for (ConnectionListener listener: listeners) listener.throwableRaised(event);
    }
  }

  /**
   * Send the specified data through this onnection.
   * @param data the data to send.
   * @throws IOException if any I/O error occurs.
   */
  public void offer(final byte[] data) throws IOException {
    if (debugEnabled) log.debug("offering {} bytes to {}", data.length, this);
    sender.toSendQueue.offer(data);
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
    if (closed.compareAndSet(false, true)) {
      if (debugEnabled) log.debug("closing {}", this);
      try {
        if (socketWrapper != null) socketWrapper.close();
      } catch (IOException e) {
        log.error("error closing {}", this, e);
      }
    }
  }

  @Override
  public String toString() {
    return new StringBuilder(getClass().getSimpleName()).append('[')
      .append(socketWrapper.getHost()).append(':').append(socketWrapper.getPort())
      .append(", totalRead=").append(totalRead)
      .append(", totalWritten=").append(totalWritten)
      .append(']').toString();
  }

  /**
   * Reads from the underlying socket in a separate thread.
   */
  private class Receiver implements Runnable {
    @Override
    public void run() {
      try {
        if (debugEnabled) log.debug("starting receiver for {}", Connection.this);
        while (!closed.get() && socketWrapper.isOpened()) {
          final int n = socketWrapper.read(buffer, 0, buffer.length);
          if (n < 0) throw new EOFException("EOF on " + socketWrapper);
          else if (n > 0) {
            if (traceEnabled) log.trace("read {} bytes from {}", n, Connection.this);
            totalRead += n;
            final byte[] tmp = new byte[n];
            System.arraycopy(buffer, 0, tmp, 0, n);
            final ConnectionEvent event = new ConnectionEvent(Connection.this, tmp, null);
            for (final ConnectionListener listener: listeners) listener.incomingData(event);
          }
        }
      } catch (final Exception e) {
        if (debugEnabled) log.debug("exception in receiver of {} : {}", Connection.this, e.toString());
        sender.toSendQueue.offer(new byte[0]);
        final ConnectionEvent event = new ConnectionEvent(Connection.this, null, e);
        for (final ConnectionListener listener: listeners) listener.throwableRaised(event);
      }
      if (debugEnabled) log.debug("ending receiver for {}", Connection.this);
    }
  }

  /**
   * Writes to the underlying socket in a separate thread.
   */
  private class Sender implements Runnable {
    /**
     * The queue of buffers to send.
     */
    private final BlockingQueue<byte[]> toSendQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
      try {
        if (debugEnabled) log.debug("starting sender for {}", Connection.this);
        while (!closed.get() && socketWrapper.isOpened()) {
          final byte[] data = toSendQueue.take();
          if (data.length == 0) break;
          if (socketWrapper.isOpened()) {
            if (traceEnabled) log.trace("writing {} bytes to {}", data.length, Connection.this);
            socketWrapper.write(data, 0, data.length);
            totalWritten += data.length;
            if (traceEnabled) log.trace("sent {} bytes to {}", data.length, Connection.this);
          }
        }
      } catch (final Exception e) {
        if (debugEnabled) log.debug("exception in sender of {} : {}", Connection.this, e.toString());
        final ConnectionEvent event = new ConnectionEvent(Connection.this, null, e);
        for (final ConnectionListener listener: listeners) listener.throwableRaised(event);
      }
      if (debugEnabled) log.debug("ending sender for {}", Connection.this);
    }
  }
}
