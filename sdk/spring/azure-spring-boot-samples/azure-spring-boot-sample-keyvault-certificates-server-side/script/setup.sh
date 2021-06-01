#!/usr/bin/env bash

source script/export_environment_variables.sh

# ==== set Azure Subscription ====
az account set --subscription ${SUBSCRIPTION_ID}

# ==== Create Resource Group ====
az group create --name ${RESOURCE_GROUP_NAME} --location ${REGION_NAME}

# ==== Create Key Vault and certificate ====
az keyvault create --name ${KEY_VAULT_NAME}           \
                   --resource-group ${RESOURCE_GROUP_NAME} \
                   --location ${REGION_NAME}
KEY_VAULT_URI=$(az keyvault show --name ${KEY_VAULT_NAME} --resource-group ${RESOURCE_GROUP_NAME} | jq -r '.properties.vaultUri')
sed -i 's#export KEY_VAULT_URI=#&'"$KEY_VAULT_URI"'#' script/export_environment_variables_of_created_resource.sh

az keyvault certificate create --vault-name ${KEY_VAULT_NAME} \
    -n ${CERTIFICATE_NAME} \
    -p "$(az keyvault certificate get-default-policy)"

# ==== Create Service Principal ====
if [ ${SERVICE_PRINCIPAL_NAME} ];then
  SERVICE_PRINCIPAL_SECRET=$(az ad sp create-for-rbac --name ${SERVICE_PRINCIPAL_NAME} | jq -r '.password')
  sed -i 's#export SERVICE_PRINCIPAL_SECRET=#&'"$SERVICE_PRINCIPAL_SECRET"'#' script/export_environment_variables_of_created_resource.sh

  SERVICE_PRINCIPAL_ID=$(az ad sp list --display-name ${SERVICE_PRINCIPAL_NAME} | jq -r '.[0].appId')
  sed -i 's#export SERVICE_PRINCIPAL_ID=#&'"$SERVICE_PRINCIPAL_ID"'#' script/export_environment_variables_of_created_resource.sh

  SERVICE_PRINCIPAL_TETANT=$(az ad sp list --display-name ${SERVICE_PRINCIPAL_NAME} | jq -r '.[0].appOwnerTenantId')
  sed -i 's#export SERVICE_PRINCIPAL_TETANT=#&'"$SERVICE_PRINCIPAL_TETANT"'#' script/export_environment_variables_of_created_resource.sh

  az keyvault set-policy --name ${KEY_VAULT_NAME} --certificate-permission get list \
     --key-permission get list \
     --secret-permission get list \
     --spn ${SERVICE_PRINCIPAL_ID} \
     --resource-group ${RESOURCE_GROUP_NAME}
fi

source script/export_environment_variables_of_created_resource.sh
