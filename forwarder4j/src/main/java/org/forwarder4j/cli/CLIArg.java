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

package org.forwarder4j.cli;

/**
 * Instances of this class represent named command-line arguments.
 * @author Laurent Cohen
 */
class CLIArg {
  /**
   * This argument's name.
   */
  private final String name;
  /**
   * If {@code true} then this argument has a value explicitly specified on the command line,
   * when {@code false} it is a boolean switch set to {@code true} when present or {@code false} when unspecified.
   */
  private final boolean switchArg;
  /**
   * A description of this argument.
   */
  private final String usage;
  /**
   * Other names for this argument.
   */
  private final String[] aliases;

  /**
   * Initialize this argument with the specified name and explicit value flag.
   * @param name this argument's name.
   * @param switchArg if {@code true} then this argument has a value explicitly specified on the command line,
   * when {@code false} it is a boolean switch set to {@code true} when present or {@code false} when unspecified.
   * @param usage a string describing this argument's usage.
   * @param aliases other names for this argument.
   */
  public CLIArg(final String name, final boolean switchArg, final String usage, final String...aliases) {
    this.name = name;
    this.switchArg = switchArg;
    this.usage = usage == null ? "" : usage;
    this.aliases = (aliases == null) ? new String[0] : aliases;
  }

  /**
   * Get this argument's name.
   * @return the name of this argument.
   */
  public String getName() {
    return name;
  }

  /**
   * Whether this argument is boolean switch.
   * @return {@code false} when this argument has a value explicitly specified on the command line,
   * {@code true} when it is a boolean switch set to {@code true} when present or {@code false} when unspecified.
   */
  public boolean isSwitch() {
    return switchArg;
  }

  /**
   * Get a description of this argument.
   * @return a string describing this argument's usage.
   */
  public String getUsage() {
    return usage;
  }

  /**
   * Get the other names this argument is known by.
   * @return an array of strings.
   */
  public String[] getAliases() {
    return aliases;
  }
}
