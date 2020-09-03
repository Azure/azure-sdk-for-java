#!/bin/bash

cd "$(dirname "$0")" || exit


for i in {1..3}; do
  echo "Trying ${i}"
  ./install_java_mvn_git.sh && break || sleep 3;
done
