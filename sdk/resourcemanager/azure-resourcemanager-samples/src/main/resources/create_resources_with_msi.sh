#!/bin/bash

/usr/bin/yes | sudo apt-get update
/usr/bin/yes | sudo apt install python3
/usr/bin/yes | sudo apt install python3-pip
sudo pip3 install azure-cli
az login --msi
az storage account create -n $1 -g $2 -l $3 --sku Premium_LRS
