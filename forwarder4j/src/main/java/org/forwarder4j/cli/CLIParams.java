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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.forwarder4j.Config;

/**
 * This class describes a set  of command-line arguments.
 * @author Laurent Cohen
 */
public class CLIParams extends Config {
  /**
   * Explicit serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  /**
   * The argument definitions.
   */
  private final Map<String, CLIArg> argDefs = new LinkedHashMap<>();
  /**
   * The argument definitions.
   */
  private final Map<String, CLIArg> defsWithAliases = new LinkedHashMap<>();
  /**
   * The title diaplayed in the {@link #printUsage()} method.
   */
  private String title;
  /**
   * General description of the command line.
   */
  private String description;

  /**
   * Add a new argument with an explicit value.
   * @param name the name of the argument.
   * @param usage a string describing the argument's usage.
   * @param aliases other names for the same argument.
   * @return return this object, for method call chaining.
   */
  @SuppressWarnings("unchecked")
  public CLIParams add(final String name, final String usage, final String...aliases) {
    return add(name, false, usage, aliases);
  }

  /**
   * Add a new argument as a boollean switch.
   * @param name the name of the argument.
   * @param usage a string describing the argument's usage.
   * @param aliases other names for the same argument.
   * @return return this object, for method call chaining.
   */
  @SuppressWarnings("unchecked")
  public CLIParams addSwitch(final String name, final String usage, final String...aliases) {
    return add(name, true, usage, aliases);
  }

  /**
   * Add a new argument.
   * @param name the name of the argument.
   * @param isSwitch whether the argument is a boolean switch.
   * @param usage a string describing the argument's usage.
   * @param aliases other names for the same argument.
   * @return return this object, for method call chaining.
   */
  private CLIParams add(final String name, final boolean isSwitch, final String usage, final String...aliases) {
    final CLIArg cliArg = new CLIArg(name, isSwitch, usage, aliases);
    argDefs.put(name, cliArg);
    defsWithAliases.put(name, cliArg);
    if (aliases != null) {
      for (final String alias: aliases) defsWithAliases.put(alias, cliArg);
    }
    return this;
  }

  /**
   * Determine whether the set of cli arguments has the specified option.
   * @param name the name of the option to find out.
   * @return {@code true} if the option is among the arguments, {@code false} otherwise.
   */
  public boolean has(final String name) {
    if (containsKey(name)) return true;
    final CLIArg arg = defsWithAliases.get(name);
    if (arg == null) return false;
    for (final String alias: arg.getAliases()) {
      if (containsKey(alias)) return true;
    }
    return false;
  }

  /**
   * Print usage of the arguments.
   * @return this object, for method call chaining.
   */
  public CLIParams printUsage() {
    if (title != null) System.out.println(title);
    if (description != null) System.out.println(description);
    int maxLen = 0;
    final Map<String, String> textMap = new HashMap<>();
    for (final CLIArg arg: argDefs.values()) {
      final StringBuilder sb = new StringBuilder(arg.getName());
      final String[] aliases = arg.getAliases();
      for (int i=0; i<aliases.length; i++) sb.append(", ").append(aliases[i]);
      if (!arg.isSwitch()) sb.append(" <value>");
      final String s = sb.toString();
      textMap.put(arg.getName(), s);
      if (s.length() > maxLen) maxLen = s.length();
    }
    final String format1 = "%-" + maxLen + "s : %s\n";
    final String format2 = "%-" + maxLen + "s : %s\n";
    for (final Map.Entry<String, CLIArg> entry: argDefs.entrySet()) {
      final CLIArg arg = entry.getValue();
      String usage = arg.getUsage();
      if (usage == null) usage = "";
      final String[] lines = usage.split("_\\n_");
      if ((lines == null) || (lines.length == 0)) {
        System.out.printf(format1, textMap.get(arg.getName()), usage);
      } else {
        System.out.printf(format1, textMap.get(arg.getName()), lines[0]);
        for (int i=1; i<lines.length; i++) System.out.printf(format2, "", lines[i]);
      }
    }
    return this;
  }

  /**
   * Parse the specified command line arguments.
   * @param clArgs the arguments to parse.
   * @throws Exception if any error occurs.
   * @return this object, for method call chaining.
   */
  public CLIParams parseArguments(final String...clArgs)  throws Exception {
    int pos = 0;
    try {
      while (pos < clArgs.length) {
        final String name = clArgs[pos++];
        CLIArg arg = argDefs.get(name);
        if (arg == null) {
          for (final CLIArg tmp: argDefs.values()) {
            for (final String alias: tmp.getAliases()) {
              if (name.equals(alias)) {
                arg = tmp;
                break;
              }
            }
            if (arg != null) break;
          }
          if (arg == null) throw new IllegalArgumentException("Unknown argument: " + name);
        }
        if (arg.isSwitch()) setBoolean(name, true);
        else setString(name, clArgs[pos++]);
      }
    } catch (final Exception e) {
      printError(null, e, clArgs);
      throw e;
    }
    return this;
  }

  /**
   * Print an error to the console.
   * @param message an optional message ot dispaly.
   * @param t an optional throwable.
   * @param clArgs the list of arguments being parsed.
   */
  void printError(final String message, final Throwable t, final String...clArgs) {
    System.out.println("Error found parsing the arguments " + Arrays.asList(clArgs));
    if (message != null) System.out.println(message);
    if (t != null) t.printStackTrace(System.out);
    printUsage();
  }

  /**
   * Get the title diaplayed in the {@link #printUsage()} method.
   * @return the title as a string.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title diaplayed in the {@link #printUsage()} method.
   * @param title the title as a string.
   * @return this object, for method call chaining.
   */
  @SuppressWarnings("unchecked")
  public CLIParams setTitle(final String title) {
    this.title = title;
    return this;
  }

  /**
   * Get the title diaplayed in the {@link #printUsage()} method.
   * @return the title as a string.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the command line description displayed in the {@link #printUsage()} method.
   * @param description the title as a string.
   * @return this object, for method call chaining.
   */
  @SuppressWarnings("unchecked")
  public CLIParams setDescription(final String description) {
    this.description = description;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final Map.Entry<String, CLIArg> entry: argDefs.entrySet()) {
      final String key = entry.getKey();
      final String value = getString(key);
      if (value != null) {
        sb.append("  ").append(key);
        if (!entry.getValue().isSwitch()) sb.append(" ").append(value);
        sb.append('\n');
      }
    }
    return sb.toString();
  }
}
