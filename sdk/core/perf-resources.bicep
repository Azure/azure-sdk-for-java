param baseName string = resourceGroup().name
param location string = resourceGroup().location

resource storageAccount 'Microsoft.Storage/storageAccounts@2019-06-01' = {
  name: '${baseName}blob'
  location: location
  kind: 'BlockBlobStorage'
  sku: {
    name: 'Premium_LRS'
  }
}

resource storageAccountBlobServices 'Microsoft.Storage/storageAccounts/blobServices@2021-09-01' = {
  name: 'default'
  parent: storageAccount
}

resource testContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2021-09-01' = {
  name: 'testcontainer'
  parent: storageAccountBlobServices
}

param now string = utcNow()
param expiryDate string = dateTimeAdd(now, 'P14D')
param accountSasProperties object = {
  signedServices: 'b'
  signedPermission: 'rw'
  signedExpiry: expiryDate
  signedResourceTypes: 'sco'
}

var sasToken = storageAccount.listAccountSas('2021-09-01', accountSasProperties).accountSasToken

var containerUrl = '${storageAccount.properties.primaryEndpoints.blob}${testContainer.name}?${sasToken}'

output AZURE_STORAGE_CONTAINER_SAS_URL string = containerUrl
