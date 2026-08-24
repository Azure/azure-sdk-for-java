#!/bin/bash

# Note: the ADC egress proxy CA certificate is installed into the JVM truststore
# at startup from Java code (see TrustStoreInstaller.installAdcEgressProxyCertificate).

export CA_LOG_REQUESTS=true

java -Dlog4j.provider=org.apache.logging.log4j.simple.internal.SimpleProvider -javaagent:/app/applicationinsights-agent.jar -jar app.jar
