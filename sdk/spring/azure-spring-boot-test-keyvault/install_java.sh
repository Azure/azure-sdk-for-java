#!/bin/bash

sudo apt-get update
sudo apt remove --purge openjdk-8-jre-headless openjdk-8-jre openjdk-8-jdk-headless openjdk-8-jdk
sudo apt autoremove
sudo apt install --reinstall openjdk-8-jre-headless openjdk-8-jre openjdk-8-jdk-headless openjdk-8-jdk
