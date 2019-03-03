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

import java.util.EventListener;

/**
 * Interface for receiving notifications of {@link ConnectionEvent}s.
 * @author Laurent Cohen
 */
interface ConnectionListener extends EventListener {
  /**
   * Called when data was received from the {@link Connection}.
   * @param event encapsulates the connection and received data.
   */
  void incomingData(final ConnectionEvent event);
  /**
   * Called when data an exception was raised during an I/O operation on a {@link Connection}.
   * @param event encapsulates the connection and exception.
   */
  void throwableRaised(final ConnectionEvent event);
}
