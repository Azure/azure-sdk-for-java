#!/bin/bash
# setup-benchmark-vm.sh — Run once after VM creation to install dependencies
# See §6.3 of the test plan.

set -euo pipefail

echo "=== Setting up benchmark VM ==="

# JDK + networking tools
sudo apt-get update && sudo apt-get install -y openjdk-21-jdk git net-tools iproute2 sysstat procps tmux

# Maven 3.9+ (Ubuntu 22.04 apt provides 3.6.3 which is too old for SDK plugins)
wget -q https://dlcdn.apache.org/maven/maven-3/3.9.12/binaries/apache-maven-3.9.12-bin.tar.gz -O /tmp/maven.tar.gz
sudo tar -xzf /tmp/maven.tar.gz -C /opt/
sudo ln -sf /opt/apache-maven-3.9.12/bin/mvn /usr/local/bin/mvn
export PATH=/opt/apache-maven-3.9.12/bin:$PATH

# Async-profiler
wget -qO /tmp/async-profiler.tar.gz \
  https://github.com/async-profiler/async-profiler/releases/download/v3.0/async-profiler-3.0-linux-x64.tar.gz
sudo tar -xzf /tmp/async-profiler.tar.gz -C /opt/
echo 'export PATH=$PATH:/opt/async-profiler-3.0-linux-x64/bin' >> ~/.bashrc

# Clone SDK
git clone https://github.com/Azure/azure-sdk-for-java.git ~/azure-sdk-for-java
cd ~/azure-sdk-for-java

# Build benchmark module (must run from sdk/cosmos)
cd sdk/cosmos
mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos -am clean install
mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-test clean install
mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-encryption clean install
mvn -e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-benchmark clean package -P package-assembly

echo "=== Setup complete ==="
echo "Next: Set APPLICATIONINSIGHTS_CONNECTION_STRING and copy tenants.json from benchmark-config/ to the VM"
