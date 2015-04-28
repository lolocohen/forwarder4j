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

/**
 * Representation of a host + port information.
 * @author Laurent Cohen
 */
class HostPort {
  public final String host;
  public final int port;
  /**
   * Whether the host is an ipv6 address.
   */
  public boolean ipv6Address = false;

  public HostPort(String host, int port) {
    String s = host.trim();
    if (s.startsWith("[")) {
      s = s.replace("[", "").replace("]", "");
      ipv6Address = true;
    }
    this.host = s;
    this.port = port;
  }

  public static HostPort fromString(String source) {
    String src = source.trim();
    boolean ipv6Address = false;
    // IPv6 address
    if (src.startsWith("[")) {
      src = src.replace("[", "").replace("]", "");
      ipv6Address = true;
    }
    int idx = src.lastIndexOf(':');
    HostPort hp = new HostPort(src.substring(0, idx), Integer.valueOf(src.substring(idx + 1)));
    hp.ipv6Address = ipv6Address;
    return hp;
  }

  @Override
  public String toString() {
    if (ipv6Address) return "[" + host + "]:" + port;
    return host + ":" + port;
  }
}