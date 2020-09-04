#!/bin/bash

sudo apt-get update
sudo apt remove --purge openjdk-8-jre-headless
sudo apt autoremove
sudo apt install --reinstall openjdk-8-jre-headless
sudo apt-get install -y openjdk-8-jdk
# sudo apt-get install -y openjdk-8-jdk maven git
