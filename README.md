# Forwarder4j
Forwarder4j is a multihoming TCP port forwarder written in Java.

It allows you to forward network traffic from a local port to a remote server defined by its host name or IP address and a remote port.

## Usage

### Downloading and installing

You can dowbnload the latest version from the [Github releases](https://github.com/lolocohen/forwarder4j/releases)

To install, unzip `forwarder4j-dist-xxx.zip` anywhere on your file system.


### Running the tool

To run it, use one of of the run scripts:
- **run.bat** on Windows systems
- **./run.sh** on Linux/Unix systems

### Configuration
To configure the port forwarding definitions:
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

## Building

- clone the repository:
  - `git clone git@github.com:lolocohen/forwarder4j.git`
  - or `git clone https://github.com/lolocohen/forwarder4j.git`
- build with `mvn clean install`


## Licensing

Forwarder4j is licensed under the terms of the [Apache License, v2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
