@echo off
:: Java 8 or later is required
call java -cp config;lib/* -Djava.util.logging.config.file=config/logging-admin-client.properties org.forwarder4j.admin.Admin %*
