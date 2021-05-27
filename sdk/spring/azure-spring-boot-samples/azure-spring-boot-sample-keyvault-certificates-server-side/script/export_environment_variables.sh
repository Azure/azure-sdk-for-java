#!/usr/bin/env bash

# Set your Azure Subscription id where all required resources will be created.
export SUBSCRIPTION_ID=799c12ba-353c-44a1-883d-84808ebb2216

# Set the name for your Azure resource group to be created.
export RESOURCE_GROUP_NAME=yirg8

# Set the region for all resources to be created.
export REGION_NAME=westus

# Set the name for your Azure Key Vault to be created.
export KEY_VAULT_NAME=yikvcli8

# Set the name for your Service Principal to be created. It should be NULL if using managed identity. ====
export SERVICE_PRINCIPAL_NAME=yispcli8
