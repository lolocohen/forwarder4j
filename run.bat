@echo off
rem Java 7 or later is required
rem set JAVA_HOME=C:\java\jdk1.7.0
rem set PATH=%JAVA_HOME%\bin;%PATH%
call java -cp config;Forwarder4j.jar;lib/* org.forwarder4j.Forwarder
