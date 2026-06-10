#!/bin/sh
# Import the Foundry vNext egress-proxy CA cert into Java's truststore so that
# outbound HTTPS calls (Azure OpenAI, Foundry storage API, MSI, etc.) succeed
# from inside an Azure Agent Server. The cert is mounted by the platform at
# a well-known path when running under the vNext "ADC" hosting environment;
# absent locally.
ADC_CERT="/etc/ssl/certs/adc-egress-proxy-ca.crt"
if [ -f "$ADC_CERT" ]; then
  echo "Importing ADC egress proxy CA cert into Java truststore..."
  cat "$ADC_CERT" >> /etc/ssl/certs/ca-certificates.crt 2>/dev/null || true
  JAVA_CACERTS="$JAVA_HOME/lib/security/cacerts"
  if [ -f "$JAVA_CACERTS" ]; then
    keytool -importcert -noprompt -trustcacerts \
      -alias adc-egress-proxy \
      -file "$ADC_CERT" \
      -keystore "$JAVA_CACERTS" \
      -storepass changeit 2>/dev/null || true
    echo "ADC CA cert imported into Java truststore."
  else
    echo "WARNING: Java cacerts not found at $JAVA_CACERTS"
  fi
else
  echo "No ADC egress proxy CA cert found (not running in vNext)."
fi

exec java \
  ${APPLICATIONINSIGHTS_CONNECTION_STRING:+-javaagent:/app/applicationinsights-agent.jar} \
  -jar /app/app.jar
