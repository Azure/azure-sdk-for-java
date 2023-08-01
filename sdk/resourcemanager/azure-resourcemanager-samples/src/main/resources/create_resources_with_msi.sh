#!/bin/bash

curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

az login --identity
az storage account create -n $1 -g $2 -l $3 --sku Premium_LRS
