param baseName string
param endpointSuffix string = environment().suffixes.storage
param location string = resourceGroup().location

var uniqueSuffix = uniqueString(resourceGroup().id)
var primaryAccountName = '${take(baseName, 11)}${uniqueSuffix}'
var pageBlobStorageAccountName = '${take(baseName, 7)}${uniqueSuffix}page'

resource primaryAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: primaryAccountName
  location: location
  sku: {
    name: 'Premium_LRS'
  }
  kind: 'BlockBlobStorage'
  properties: {}
}

resource pageBlobStorageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: pageBlobStorageAccountName
  location: location
  sku: {
    name: 'Premium_LRS'
  }
  kind: 'StorageV2'
  properties: {}
}

output STORAGE_ENDPOINT_STRING string = '"https://${primaryAccountName}.blob.${endpointSuffix}"'
output PAGE_BLOB_STORAGE_ENDPOINT_STRING string = '"https://${pageBlobStorageAccountName}.blob.${endpointSuffix}"'
