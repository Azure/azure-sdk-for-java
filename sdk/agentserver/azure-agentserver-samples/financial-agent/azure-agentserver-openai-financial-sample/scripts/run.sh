#!/bin/bash

# Note: the ADC egress proxy CA certificate is installed into the JVM truststore
# at startup from Java code (see TrustStoreInstaller.installAdcEgressProxyCertificate).

export CA_LOG_REQUESTS=true

# Prompt, response, and tool content is sensitive and disabled by default.
# Set OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT=true explicitly when
# the deployment is permitted to export this content for trajectory evaluation.
java -javaagent:/app/applicationinsights-agent.jar -jar app.jar
