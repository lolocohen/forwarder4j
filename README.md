[![Build Status](https://travis-ci.org/lolocohen/forwarder4j.svg?branch=master)](https://travis-ci.org/lolocohen/forwarder4j)

# Forwarder4j
Forwarder4j is a multihoming TCP port forwarder written in Java.

It allows you to forward network traffic from a local port to a remote server defined by its host name or IP address and a remote port.

## Usage

### Downloading and installing

You can download the latest version from the [Github releases](https://github.com/lolocohen/forwarder4j/releases)

To install, unzip `forwarder4j-dist-xxx.zip` anywhere on your file system.


### Running the tool

To run it, use one of of the run scripts:
- **./f4j.sh** on Linux/Unix systems
- **f4j.bat** on Windows systems

### Configuration

You can define port forwarding entries both at the command line and in a properties file. Entries defined in the command line always take precedence over those in the properties file.

In case of duplicate definitions for a given port, the first valid one is used and the others are discarded. 

#### Port forwarding definitions in the command line:

```
./f4j.sh <local_port1>=<remote_host1>:<remote_port1> ... <local_portN>=<remote_hostN>:<remote_portN>
```

Example: 

```ini
./f4j.sh 2001=www.space_odissey.com:2001 2002=97.42.10.24:80 2003=[FFF1:0002:FFF3:0004:FFF5:0006:FFF7:0008]:80
```

#### Definitions in a configuration file:

- open the configuration file **config/forwarder4j.properties**
- add any number of service definitions in the form:<br>
`forwarder4j.service.<local_port> = <remote_host>:<remote_port>`

Examples:
```INI
forwarder4j.service.1081 = www.mysite.com:80
forwarder4j.service.1082 = 1.2.3.4:80
# also with IPv6 addresses
forwarder4j.service.1083 = [FFF1:0002:FFF3:0004:FFF5:0006:FFF7:0008]:80
```

#### Configuration file location

By default, the configuration file is searched as `config/forwarder4j.properties`. Another location can be specified with the `forwarder4j.config` system property. For example:

```ini
java ... -Dforwarder4j.config=path/to/myConfig.properties org.forwarder4j.Forwarder 8089=www.myhost.com:80
```


### Administration

Forwarder4j can be administered remotely with a command-line tool:
- **./f4j-admin.sh** on Linux/Unix
- **f4j-admin.bat** on Windows

To display usage instructions and the list of options, just type `./f4j-admin.sh -h`. This results in the following being printed to the console:

~~~ini
Forwarder4j administration tool usage
to run the tool: [./f4j-admin.sh | f4j-admin.bat] options
available options:
-h, -?, --help               : Print these instructiosn and exit. Any other option is ignored
-H, --admin-host <value>     : the host on which the admin is running (defaults to 'localhost')
-p, --admin-port <value>     : the administrative port number (defaults to '8192')
-c, --admin-commands <value> : the list of commands to perform, separated with comma (','),
                             : semicolon (';'), or pipe ('|')
  Available commands:
    +<local_port>=<host>:<port> : adds/sets forwarding of host:port through local_port
    -<local_port>               : removes any port forwarding via local_port
    list                        : lists all current port forwarding definitions
    stop                        : terminates Forwarder4j. Any command after this is ignored
~~~

#### Configuration

By default, the administration service listens to port 8192. A different port can be configured in the `config/forwarder4j.properties` file:

~~~ini
# administration port
forwarder4j.admin.port = 12345
~~~



#### Example:

start Fowarder4j:

~~~ini
> f4j.bat
> admin service running on port 8192
> No entry defined
~~~

administrate:

~~~ini
> f4j-admin.bat -c +2000=localhost:8880;+2001=localhost:8880;list;-2001;list
> forwarding port 2000 to localhost:8880
> forwarding port 2001 to localhost:8880
> List of entries:
> - 2000=localhost:8880
> - 2001=localhost:8880
> port definition for '2001' was removed
> List of entries:
> - 2000=localhost:8880

~~~



## Building

- clone the repository:
  - `git clone git@github.com:lolocohen/forwarder4j.git`
  - or `git clone https://github.com/lolocohen/forwarder4j.git`
- build with `mvn clean install`


## Licensing

Forwarder4j is licensed under the terms of the [Apache License, v2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
