#!/bin/sh

sudo apt-get install -y --upgrade python3-pip python3-setuptools
pip3 install --upgrade wheel
pip3 install --upgrade PyYAML requests

cat << EOF > $2
{"envs": {"PATH": "$JAVA_HOME_11_X64/bin:$PATH", "JAVA_HOME": "$JAVA_HOME_11_X64"}}
EOF