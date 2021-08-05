#!/bin/bash
set -eux

cd "$(dirname "$0")"


sudo apt-get update
sudo apt-get install -y default-jdk maven git
