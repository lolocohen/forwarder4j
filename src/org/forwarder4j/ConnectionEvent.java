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

import java.util.EventObject;

/**
 * Instances of this class represent events sent by a {@link Connection}.
 * @author Laurent Cohen
 */
class ConnectionEvent extends EventObject {
  /**
   * The data that was read from the connection, if any.
   */
  private final byte[] data;
  /**
   * A throwable that was read during an I/O operation on the connection, if any.
   */
  private final Throwable throwable;

  /**
   * Initialize this event with the specified source connection, data and throwable.
   * @param connection the source of this event.
   * @param data the data that was read from the connection, if any.
   * @param throwable the throwable that was read during an I/O operation on the connection, if any.
   */
  public ConnectionEvent(final Connection connection, final byte[] data, final Throwable throwable) {
    super(connection);
    this.data = data;
    this.throwable = throwable;
  }

  /**
   * Get the source connection.
   * @return an instance of {@link Connection}.
   */
  public Connection getConnection() {
    return (Connection) getSource();
  }

  /**
   * Get the data that was read from the connection, if any.
   * @return the data as a byte array, or {@code null} if none was received for this event.
   */
  public byte[] getData() {
    return data;
  }

  /**
   * Get the throwable that was read during an I/O operation on the connection, if any.
   * @return a {@link Throwable}, or {@code null} if no throwable was raised for this event.
   */
  public Throwable getThrowable() {
    return throwable;
  }
}
