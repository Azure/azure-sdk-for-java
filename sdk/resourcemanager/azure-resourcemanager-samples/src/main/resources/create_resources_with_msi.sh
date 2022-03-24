#!/bin/bash

sudo apt update
sudo apt install -y python3
sudo apt install -y python3-pip

sudo pip3 install azure-cli

az login --msi
az storage account create -n $1 -g $2 -l $3 --sku Premium_LRS
