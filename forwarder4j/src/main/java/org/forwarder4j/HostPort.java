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

/**
 * Representation of a host + port information.
 * @author Laurent Cohen
 */
public class HostPort {
  /**
   * The host name or IP address.
   */
  private final String host;
  /**
   * The prot number.
   */
  private final int port;
  /**
   * Whether the host is an ipv6 address.
   */
  private boolean ipv6Address;

  /**
   * Initialize with the specified host and port.
   * @param host the host or ip address.
   * @param port the port number.
   */
  private HostPort(String host, int port, final boolean ipv6Address) {
    this.ipv6Address = ipv6Address;
    this.host = host;
    this.port = port;
  }

  /**
   * Factory method which converts a string into a {@link HostPort} instance.
   * @param source a string in the form {@code <host>:<port>}.
   * @return a {@link HostPort} instance.
   */
  public static HostPort from(final String source) {
    String src = source.trim();
    boolean ipv6 = false;
    if (src.startsWith("[")) {
      src = src.replace("[", "").replace("]", "");
      ipv6 = true;
    }
    final int idx = src.lastIndexOf(':');
    return new HostPort(src.substring(0, idx), Integer.valueOf(src.substring(idx + 1)), ipv6);
  }

  @Override
  public String toString() {
    if (ipv6Address) return "[" + host + "]:" + port;
    return host + ":" + port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }
}
