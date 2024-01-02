#!/bin/sh

sudo apt-get install -y --upgrade python3-pip python3-setuptools
pip3 install --upgrade wheel
pip3 install --upgrade PyYAML requests

curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" # This loads nvm
nvm install v18.15.0
nvm alias default node

cat << EOF > $2
{"envs": {"PATH": "$JAVA_HOME_11_X64/bin:$PATH", "JAVA_HOME": "$JAVA_HOME_11_X64"}}
EOF
