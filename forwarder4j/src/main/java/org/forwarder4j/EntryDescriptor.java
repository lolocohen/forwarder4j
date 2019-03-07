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

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class describes a port forwarding definition.
 * @author Laurent Cohen
 */
public class EntryDescriptor {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(EntryDescriptor.class);
  /**
   * A simple pattern to validate the CLI args.
   */
  private static final Pattern CLI_ARG_PATTERN = Pattern.compile("[0-9]+=.*");
  /**
   * The port to forward through.
   */
  private final int port;
  /**
   * The target of the forwarded traffic.
   */
  private final HostPort target;

  /**
   * Initialize with the specified local port and target host:port.
   * @param port the local port to bind to.
   * @param target the target end point to forward to/from.
   */
  private EntryDescriptor(final int port, final HostPort target) {
    this.port = port;
    this.target = target;
  }

  /**
   * Factory method to create an {@code EntryDescriptor} from a string. 
   * @param desc a string in the form {@code local_port=host:port}.
   * @return a new {@code EntryDescriptor} instance.
   */
  public static EntryDescriptor from(final String desc) {
    if (!CLI_ARG_PATTERN.matcher(desc).matches()) {
      throw new IllegalArgumentException("");
    }
    final int idx = desc.indexOf('=');
    return from(desc.substring(0, idx), desc.substring(idx + 1));
  }

  /**
   * Factory method to create an {@code EntryDescriptor} from 2 strings representing the local port and host:port target. 
   * @param portStr a string representing a port number.
   * @param target a string in the form {@code host:port}.
   * @return a new {@code EntryDescriptor} instance.
   */
  public static EntryDescriptor from(final String portStr, final String target) {
    int port = -1;
    try {
      port = Integer.valueOf(portStr.trim());
    } catch(NumberFormatException e) {
      final String message = String.format("%s. '%s' is not a valid port number, ignoring it", e, portStr);
      log.error(message);
      throw new IllegalArgumentException(message);
    }
    final HostPort hp = HostPort.from(target);
    return new EntryDescriptor(port, hp);
  }

  /**
   * @return the port to forward through.
   */
  public int getPort() {
    return port;
  }

  /**
   * @return the target of the forwarded traffic.
   */
  public HostPort getTarget() {
    return target;
  }
}
