#!/bin/sh

echo Install Python
sudo apt-get install -y --upgrade python3-pip python3-setuptools 2>&1
pip3 install --upgrade wheel 2>&1
pip3 install --upgrade PyYAML requests 2>&1

# install tsp-client
echo Install tsp-client
cd eng/common/tsp-client
npm ci 2>&1
cd ../../..

cat << EOF > $2
{"envs": {"PATH": "$JAVA_HOME_11_X64/bin:$PATH", "JAVA_HOME": "$JAVA_HOME_11_X64"}}
EOF
