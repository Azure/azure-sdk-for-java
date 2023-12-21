#!/bin/sh

apt-get install python3.8
apt-get install -y --upgrade python3-pip python3-setuptools

curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" # This loads nvm
nvm install v18.15.0
nvm alias default node

cat << EOF > $1
{}
EOF
