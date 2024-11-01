# Setting up an Azure Cosmos DB Instance

## Prerequisites

- Azure subscription with permissions to create:
  - Resource Groups, Cosmos DB
- Bash shell (tested on Visual Studio Codespaces, Cloud Shell, Mac, Ubuntu, Windows with WSL2)
  - Will not work with WSL1
- Azure CLI ([download](https://learn.microsoft.com/cli/azure/install-azure-cli?view=azure-cli-latest))

## Create Azure Cosmos DB Instance, Database and Container

Login to Azure and select subscription.

```bash

az login

# show your Azure accounts
az account list -o table

# select the Azure subscription if necessary
az account set -s {subscription name or Id}

```

Create a new Azure Resource Group for this quickstart, then add to it a Cosmos DB Account, Database and Container using the Azure CLI.

> The `az cosmosdb sql` extension is currently in preview and is subject to change

```bash

# replace with a unique name
# do not use punctuation or uppercase (a-z, 0-9)
export Cosmos_Name={your Cosmos DB name}

## if true, change name to avoid DNS failure on create
az cosmosdb check-name-exists -n ${Cosmos_Name}

# set environment variables
export Cosmos_Location="centralus"
export Cosmos_Database="kafkaconnect"
export Cosmos_Container="kafka"

# Resource Group Name
export Cosmos_RG=${Cosmos_Name}-rg-cosmos

# create a new resource group
az group create -n $Cosmos_RG -l $Cosmos_Location

# create the Cosmos DB server
# this command takes several minutes to run
az cosmosdb create -g $Cosmos_RG -n $Cosmos_Name

# create the database
# 400 is the minimum --throughput (RUs)
az cosmosdb sql database create -a $Cosmos_Name -n $Cosmos_Database -g $Cosmos_RG --throughput 400

# create the container
# /id is the partition key (case sensitive)
az cosmosdb sql container create -p /id -g $Cosmos_RG -a $Cosmos_Name -d $Cosmos_Database -n $Cosmos_Container

# OPTIONAL: Enable Time to Live (TTL) on the container
export Cosmos_Container_TTL=1000
az cosmosdb sql container update -g $Cosmos_RG -a $Cosmos_Name -d $Cosmos_Database -n $Cosmos_Container --ttl=$Cosmos_Container_TTL

```

With the Azure Cosmos DB instance setup, you will need to get the Cosmos DB endpoint URI and primary connection key. These values will be used to setup the Cosmos DB Source and Sink connectors.

```bash

# Keep note of both of the following values as they will be used later

# get Cosmos DB endpoint URI
echo https://${Cosmos_Name}.documents.azure.com:443/

# get Cosmos DB primary connection key
az cosmosdb keys list -n $Cosmos_Name -g $Cosmos_RG --query primaryMasterKey -o tsv

```

### Cleanup

Remove the Cosmos DB instance and the associated resource group

```bash

# delete Cosmos DB instance
az cosmosdb delete -g $Cosmos_RG -n $Cosmos_Name

# delete Cosmos DB resource group
az group delete --no-wait -y -n $Cosmos_RG

```
