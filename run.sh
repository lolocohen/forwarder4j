#! /bin/sh
# Java 7 or later is required
#export JAVA_HOME=/opt/java/jdk1.7.0
#export PATH=$JAVA_HOME/bin:$PATH
java -cp config:Forwarder4j.jar:lib/* org.forwarder4j.Forwarder
