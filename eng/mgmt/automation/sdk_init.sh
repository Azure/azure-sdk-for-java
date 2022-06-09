#!/bin/sh

sudo apt-get install -y --upgrade python3-pip python3-setuptools
pip3 install --upgrade wheel
pip3 install --upgrade PyYAML requests

cat << EOF > $1
{}
EOF