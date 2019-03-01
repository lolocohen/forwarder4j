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

import java.io.*;
import java.net.*;

import org.slf4j.*;

/**
 * Common abstract superclass for all socket clients. This class is provided as a convenience and provides
 * as set of common methods to all classes implementing the {@link org.jppf.comm.socket.SocketWrapper SocketWrapper} interface.
 * @author Laurent Cohen
 */
class SocketWrapper implements AutoCloseable {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(SocketWrapper.class);
  /**
   * The underlying socket wrapped by this SocketClient.
   */
  protected Socket socket = null;
  /**
   * A reference to the underlying socket's output stream.
   */
  protected DataOutputStream dos = null;
  /**
   * A buffered stream built on top of to the underlying socket's input stream.
   */
  protected DataInputStream dis = null;
  /**
   * The host the socket connects to.
   */
  protected String host = null;
  /**
   * The port number on the host the socket connects to.
   */
  protected int port = -1;
  /**
   * Flag indicating the opened state of the underlying socket.
   */
  protected boolean opened = false;

  /**
   * Initialize this socket client and connect it to the specified host on the specified port.
   * @param host the remote host this socket client connects to.
   * @param port the remote port on the host this socket client connects to.
   * @param serializer the object serializer used by this socket client.
   * @throws ConnectException if the connection fails.
   * @throws IOException if there is an issue with the socket streams.
   */
  public SocketWrapper(final String host, final int port) throws ConnectException, IOException {
    this.host = host;
    this.port = port;
    open();
  }

  /**
   * Initialize this socket client with an already opened and connected socket.
   * @param socket the underlying socket this socket client wraps around.
   * @throws IOException if the socket connection fails.
   */
  public SocketWrapper(final Socket socket) throws IOException {
    this.host = socket.getInetAddress().getHostName();
    this.port = socket.getPort();
    this.socket = socket;
    initStreams();
    opened = true;
  }

  /**
   * Send an array of bytes over a TCP socket connection.
   * @param data the data to send.
   * @param offset the position where to start reading data from the input array.
   * @param len the length of data to write.
   * @throws IOException if the underlying output stream throws an exception.
   */
  public void write(final byte[] data, final int offset, final int len) throws IOException {
    checkOpened();
    dos.write(data, offset, len);
    flush();
  }

  /**
   * Flush the data currently in the send buffer.
   * @throws IOException if an I/O error occurs.
   */
  public void flush() throws IOException {
    dos.flush();
  }

  /**
   * Read <code>len</code> bytes from a TCP connection into a byte array, starting
   * at position <code>offset</code> in that array.
   * This method blocks until at least one byte of data is received.
   * @param data an array of bytes into which the data is stored.
   * @param offset the position where to start storing data read from the socket.
   * @param len the length of data to read.
   * @return the number of bytes actually read or -1 if the end of stream was reached.
   * @throws IOException if the underlying input stream throws an exception.
   */
  public int readAll(final byte[] data, final int offset, final int len) throws IOException {
    checkOpened();
    int count = 0;
    while (count < len) {
      int n = dis.read(data, count + offset, len - count);
      if (n < 0) break;
      else count += n;
    }
    return count;
  }

  /**
   * Read <code>len</code> bytes from a TCP connection into a byte array, starting
   * at position <code>offset</code> in that array.
   * @param data an array of bytes into which the data is stored.
   * @param offset the position where to start storing data read from the socket.
   * @param len the length of data to read.
   * @return the number of bytes actually read or -1 if the end of stream was reached.
   * @throws IOException if the underlying input stream throws an exception.
   */
  public int read(final byte[] data, final int offset, final int len) throws IOException {
    checkOpened();
    return dis.read(data, offset, len);
  }

  /**
   * Open the underlying socket connection.
   * @throws ConnectException if the socket fails to connect.
   * @throws IOException if the underlying input and output streams raise an error.
   * @see org.jppf.comm.socket.SocketWrapper#open()
   */
  private void open() throws ConnectException, IOException {
    if (!opened) {
      if ((host == null) || "".equals(host.trim())) throw new ConnectException("You must specify the host name");
      else if (port <= 0) throw new ConnectException("You must specify the port number");
      socket = new Socket();
      InetSocketAddress addr = new InetSocketAddress(host, port);
      socket.setReceiveBufferSize(Utils.SOCKET_BUFFER_SIZE);
      socket.setSendBufferSize(Utils.SOCKET_BUFFER_SIZE);
      socket.setTcpNoDelay(Utils.SOCKET_TCP_NODELAY);
      socket.setKeepAlive(Utils.SOCKET_KEEPALIVE);
      socket.connect(addr);
      initStreams();
      opened = true;
      if (log.isDebugEnabled()) log.debug("getReceiveBufferSize() = " + socket.getReceiveBufferSize());
    }
  }

  /**
   * Initialize all the stream used for receiving and sending objects through the
   * underlying socket connection.
   * @throws IOException if an error occurs during the streams initialization.
   */
  private void initStreams() throws IOException {
    OutputStream os = socket.getOutputStream();
    InputStream is = socket.getInputStream();
    dos = new DataOutputStream(new BufferedOutputStream(os));
    dis = new DataInputStream(new BufferedInputStream(is));
  }

  /**
   * Close the underlying socket connection.
   * @throws ConnectException if the socket connection is not opened.
   * @throws IOException if the underlying input and output streams raise an error.
   */
  @Override
  public void close() throws IOException {
    opened = false;
    if (socket != null) {
      Utils.closeSilent(dis);
      Utils.closeSilent(dos);
      socket.close();
    }
  }

  /**
   * Determine whether this socket client is opened or not.
   * @return true if this client is opened, false otherwise.
   */
  public boolean isOpened() {
    return opened;
  }

  /**
   * Check whether the underlying socket is opened or not.
   * @throws ConnectException if the connection is not opened.
   */
  private void checkOpened() throws ConnectException {
    if (!opened) throw new ConnectException("Client connection not opened");
  }

  /**
   * Get the remote host the underlying socket connects to.
   * @return the host name or ip address as a string.
   */
  public String getHost() {
    return host;
  }

  /**
   * Get the remote port the underlying socket connects to.
   * @return the port number on the remote host.
   */
  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    sb.append("socket=").append(socket);
    sb.append(']');
    return sb.toString();
  }
}
