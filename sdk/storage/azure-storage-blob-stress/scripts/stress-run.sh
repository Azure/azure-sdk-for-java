#!/bin/sh
set -ex;
set -exa;
keytool -import -alias test -file /mnt/outputs/dev-cert.pfx -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -keypass changeit -storepass changeit;
