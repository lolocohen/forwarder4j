# Forwarder4j
Forwarder4j is a multihoming port forwarder written in Java.

It allows you to forward network traffic from a local port to a remote server defined by its host name or IP address and a remote port.

### Running the tool

To run it, use of of the run scripts:
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
forwarder4j.service.1081 = [FFF1:0002:FFF3:0004:FFF5:0006:FFF7:0008]:80
```

#### Licensing

Forwarder4j is licensed under the terms of the [Apache License, v2.0](http://www.apache.org/licenses/LICENSE-2.0.html)