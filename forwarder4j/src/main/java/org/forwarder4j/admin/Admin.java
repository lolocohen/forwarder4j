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

package org.forwarder4j.admin;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.forwarder4j.Config;
import org.forwarder4j.EntryDescriptor;
import org.forwarder4j.Forwarder;
import org.forwarder4j.SocketWrapper;
import org.forwarder4j.cli.CLIParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the server and client side of the administration service.
 * @author Laurent Cohen
 */
public class Admin implements Runnable {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(Admin.class);
  /**
   * Determines whether the debug level is enabled in the log configuration, without the cost of a method call.
   */
  private static final boolean debugEnabled = log.isDebugEnabled();
  /**
   * Regex to split a list of commands into individual commands.
   */
  private static final Pattern COMMAND_SPLIT_PATTERN = Pattern.compile(",|;|\\|");
  /**
   * The default admin port.
   */
  private static final int DEFAULT_PORT = 8192;
  /**
   * Description of the "help" CLI param.
   */
  private static final String HELP_PARAM_DESC = "Print these instructiosn and exit. Any other option is ignored";
  /**
   * Description of the "host" CLI param.
   */
  private static final String HOST_PARAM_DESC = "the host on which the admin is running (defaults to 'localhost')";
  /**
   * Description of the "port" CLI param.
   */
  private static final String PORT_PARAM_DESC = "the administrative port number (defaults to '" + DEFAULT_PORT + "')";
  /**
   * Description of the "commands" CLI param.
   */
  private static final String COMMANDS_PARAM_DESC = 
    "the list of commands to perform, separated with comma (','),_\n_semicolon (';'), or pipe ('|')\n" +
    "  Available commands:\n" +
    "    +<local_port>=<host>:<port> : adds/sets forwarding of host:port through local_port\n" +
    "    -<local_port>               : removes any port forwarding via local_port\n" +
    "    list                        : lists all current port forwarding definitions\n" +
    "    stop                        : terminates Forwarder4j. Any command after this is ignored";
  /**
   * Description of the "commands" CLI param.
   */
  private static final String DESCRIPTION = "to run the tool: [./f4j-admin.sh | f4j-admin.bat] options\navailable options:";
  /**
   * Mapping of existing {@link Forwarder forwarders} to their local port.
   */
  private final Map<Integer, Forwarder> forwarderMap = new HashMap<>();
  /**
   * The definitions of the available command line options.
   */
  private static final CLIParams CLI = new CLIParams()
    .addSwitch("-h", HELP_PARAM_DESC, "-?", "--help")
    .add("-H", HOST_PARAM_DESC, "--admin-host")
    .add("-p", PORT_PARAM_DESC, "--admin-port")
    .add("-c", COMMANDS_PARAM_DESC, "--admin-commands")
    .setTitle("Forwarder4j administration tool usage")
    .setDescription(DESCRIPTION);

  //----- admin client methods -----//

  public static void main(final String...args) {
    try {
      final CLIParams params = CLI.parseArguments(args);
      if (params.has("-h")) params.printUsage();
      else {
        final String host = params.getString("-H", "localhost");
        final int port = params.getInt("-p", DEFAULT_PORT);
        final String command = params.getString("-c", null);
        final String response = executeCommand(host, port, command);
        System.out.println(response);
      }
    } catch (final Exception e) {
      e.printStackTrace(System.out);
    }
  }

  /**
   * Execute the specified command(s) on the specified remote admin daemon.
   * @param host the host on which the admin daemon is running.
   * @param port the admin port.
   * @param command the command(s) to send.
   * @return the response from the admin daemon.
   * @throws Exception if any error occurs.
   */
  public static String executeCommand(final String host, final int port, final String command) throws Exception {
    try (final SocketWrapper connection = new SocketWrapper(host, port)) {
      connection.writeString(command);
      return connection.readString();
    }
  }

  //----- admin server methods -----//

  @Override
  public void run() {
    try {
      final Config cfg = Config.getConfiguration();
      final int port = cfg.getInt("forwarder4j.admin.port", DEFAULT_PORT);
      try (final ServerSocket server = new ServerSocket(port)) {
        final String msg = "admin service running on port " + port;
        System.out.println(msg);
        log.info(msg);
        while (true) {
          final Socket socket = server.accept();
          execute(socket);
        }
      }
    } catch (final Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  private void setEntry(final int localPort, final Forwarder forwarder) {
    synchronized(forwarderMap) {
      forwarderMap.put(localPort, forwarder);
    }
  }

  /**
   * Execute a command string from an incoming socket connection.
   * @param socket the socket connection through which the command string is sent.
   */
  private void execute(final Socket socket) {
    try (final SocketWrapper connection = new SocketWrapper(socket)) {
      final String str = connection.readString();
      if (debugEnabled) log.debug("received commands '{}'", str);
      final String[] cmds = COMMAND_SPLIT_PATTERN.split(str);
      for (int i=0; i<cmds.length; i++) cmds[i] = cmds[i].trim();
      final StringBuilder response = new StringBuilder();
      boolean stopped = false;
      for (final String cmd: cmds) {
        try {
          String ret = "";
          if (debugEnabled) log.debug("processing command '{}'", cmd);
          if (cmd.startsWith("list")) ret = executeList();
          else if (cmd.startsWith("stop") || cmd.startsWith("clear")) {
            try {
              ret = executeStop();
            } catch (final Exception e) {
              final String msg = "error stopping the application";
              log.error(msg, e);
              ret = msg + ": " + e;
            }
            stopped = cmd.startsWith("stop");
            response.append(ret).append('\n');
            break;
          }
          else if (cmd.startsWith("+")) ret = executeSet(cmd);
          else if (cmd.startsWith("-")) ret = executeRemove(cmd);
          else ret = "Command not understood, ignoring it: " + cmd;
          response.append(ret).append('\n');
        } catch (final Exception e) {
          response.append(e.getMessage()).append('\n');
        }
      }
      connection.writeString(response.toString());
      if (stopped) {
        log.info("stop requested, exiting Forwarder4j");
        System.exit(0);
      }
    } catch(final Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Execute the admin command "list".
   * @return a string that lists all entries, one by line.
   * @throws Exception if any error occurs.
   */
  private String executeList() throws Exception {
    if (debugEnabled) log.debug("processing list command");
    final Map<Integer, Forwarder> map;
    synchronized(forwarderMap) {
      map = new TreeMap<>(forwarderMap);
    }
    final StringBuilder sb = new StringBuilder(map.isEmpty() ? "No entry defined" : "List of entries:");
    map.forEach((key, value) -> sb.append("\n- ").append(value));
    if (debugEnabled) log.debug("sending response:\n{}", sb);
    return sb.toString();
  }

  /**
   * Add a new entry or change an existing one.
   * @param command a string describing the entry to add or change.
   * @return a meesage desribing the result of the operation.
   * @throws Exception if any error occurs.
   */
  private String executeSet(final String command) throws Exception {
    if (debugEnabled) log.debug("processing add/set command '{}'", command);
    final EntryDescriptor desc = EntryDescriptor.from(command.substring(1));
    final int port = desc.getPort();
    synchronized(forwarderMap) {
      if (forwarderMap.containsKey(port)) {
        forwarderMap.remove(port);
        executeRemove(desc.getPort());
      }
      final Forwarder forwarder = createForwarder(desc, null);
      if (forwarder != null) {
        while (!forwarder.isBound() && !forwarder.isClosed()) Thread.sleep(50L);
      }
      return String.format("forwarding port %d to %s", desc.getPort(), desc.getTarget());
    }
  }

  /**
   * Remove an existing entry.
   * @param command a string describing the entry to add or change.
   * @return a meesage desribing the result of the operation.
   * @throws Exception if any error occurs.
   */
  private String executeRemove(final String command) throws Exception {
    if (debugEnabled) log.debug("processing remove command '{}'", command);
    final String portStr = command.substring(1);
    int port = -1;
    try {
      port = Integer.valueOf(portStr);
    } catch (@SuppressWarnings("unused") final NumberFormatException e) {
      return "'" + command.substring(1) + "' is not a valid port number";
    }
    return executeRemove(port);
  }

  /**
   * Remove an existing entry.
   * @param command a string describing the entry to add or change.
   * @return a meesage desribing the result of the operation.
   * @throws Exception if any error occurs.
   */
  private String executeRemove(final int port) throws Exception {
    synchronized(forwarderMap) {
      if (!forwarderMap.containsKey(port)) return "port '" + port + "' was not defined and couldn't be removed";
      final Forwarder forwarder = forwarderMap.remove(port);
      forwarder.close();
    }
    return "port definition for '" + port + "' was removed";
  }

  /**
   * Terminate the application.
   * @return a meesage desribing the result of the operation.
   * @throws Exception if any error occurs.
   */
  private String executeStop() throws Exception {
    if (debugEnabled) log.debug("stopping the application");
    synchronized(forwarderMap) {
      for (final Map.Entry<Integer, Forwarder> entry: forwarderMap.entrySet()) {
        final Forwarder forwarder = entry.getValue();
        try {
          forwarder.close();
        } catch (final Exception e) {
          log.error("error stopiing Forwarder[{}]", forwarder, e);
        }
      }
    }
    return "the application is now ready to terminate";
  }

  /**
   * Create a forwarder for the specified local port and target host/port.
   * @param port the local port to forward through.
   * @param target the target host and port to forward to.
   * @param allPorts the set of already defined local port entries.
   * @return the created forwarder, or {@code null} if it could not be created.
   */
  public Forwarder createForwarder(final EntryDescriptor desc, final Map<Integer, String> allPorts) {
    if ((allPorts == null) || !allPorts.containsKey(desc.getPort())) {
      if (allPorts != null) allPorts.put(desc.getPort(), desc.getTarget().toString());
      Forwarder server = new Forwarder(desc.getPort(), desc.getTarget());
      System.out.printf("Forwarding local port %d to %s%n", desc.getPort(), desc.getTarget());
      setEntry(desc.getPort(), server);
      new Thread(server, "Server-" + desc.getPort()).start();
      return server;
    } else {
      System.out.printf("Port %d is already mapped to %s, cannot map it again to %s\n",
        desc.getPort(), allPorts.get(desc.getPort()), desc.getTarget());
      return null;
    }
  }

  public Map<Integer, Forwarder> getForwarderMap() {
    return forwarderMap;
  }
}
