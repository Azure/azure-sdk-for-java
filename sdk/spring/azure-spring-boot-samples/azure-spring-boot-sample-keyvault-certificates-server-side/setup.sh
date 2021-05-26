#!/usr/bin/env bash

# ==== set Azure Subscription ====
az account set --subscription $1

# ==== Create Resource Group ====
az group create --name $2 --location $3

# ==== Create Key Vault, self-signed certificate ====
az keyvault create --name $4           \
                   --resource-group $2 \
                   --location $3
export KEY_VAULT_URI=$(az keyvault show --name $4 --resource-group $2 | jq -r '.properties.vaultUri')

az keyvault certificate create --vault-name $4 \
    -n self-signed \
    -p "$(az keyvault certificate get-default-policy)"

# ==== Create Service Principal ====
if [ $5 ];then
  export SERVICE_PRINCIPAL_SECRET=$(az ad sp create-for-rbac --name $5 | jq -r '.password')
  export SERVICE_PRINCIPAL_ID=$(az ad sp list --display-name $5 | jq -r '.[0].appId')
  export SERVICE_PRINCIPAL_TETANT=$(az ad sp list --display-name $5 | jq -r '.[0].appOwnerTenantId')

  az keyvault set-policy --name $4 --certificate-permission get list \
     --key-permission get list \
     --secret-permission get list \
     --spn ${SERVICE_PRINCIPAL_ID} \
     --resource-group $2
fi
