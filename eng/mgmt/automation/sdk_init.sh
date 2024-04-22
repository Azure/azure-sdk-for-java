#!/bin/sh

apt-get install python3.8 2>&1
apt-get install -y --upgrade python3-pip python3-setuptools 2>&1

# install tsp-client globally (local install may interfere with tooling)
npm install -g @azure-tools/typespec-client-generator-cli

cat << EOF > $1
{}
EOF
