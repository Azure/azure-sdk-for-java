#!/bin/sh
set -ex;
set -exa;
attempts=0;
while [ ! -s /mnt/outputs/dev-cert.crt ]; do
  attempts=$((attempts + 1));
  if [ "$attempts" -gt 60 ]; then
    echo "Timed out waiting for fault injector certificate" >&2;
    exit 1;
  fi;
  sleep 1;
done;
keytool -delete -alias HttpFaultInject -keystore "${JAVA_HOME}/lib/security/cacerts" -storepass changeit >/dev/null 2>&1 || true;
keytool -importcert -trustcacerts -alias HttpFaultInject -file /mnt/outputs/dev-cert.crt -keystore "${JAVA_HOME}/lib/security/cacerts" -noprompt -storepass changeit;