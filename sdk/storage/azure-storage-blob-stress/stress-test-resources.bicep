param baseName string
param endpointSuffix string = 'core.windows.net'
param location string = resourceGroup().location
param storageApiVersion string = '2022-09-01'

var primaryAccountName = '${baseName}'
var pageBlobStorageAccountName = '${baseName}pageblob'

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

output STORAGE_CONNECTION_STRING string = '"DefaultEndpointsProtocol=https;AccountName=${primaryAccountName};AccountKey=${listKeys(primaryAccount.id, storageApiVersion).keys[0].value};EndpointSuffix=${endpointSuffix}"'
output PAGE_BLOB_STORAGE_CONNECTION_STRING string = '"DefaultEndpointsProtocol=https;AccountName=${pageBlobStorageAccountName};AccountKey=${listKeys(pageBlobStorageAccount.id, storageApiVersion).keys[0].value};EndpointSuffix=${endpointSuffix}"'
output STORAGE_ENDPOINT_STRING string = '"https://${primaryAccountName}.blob.core.windows.net"'
output PAGE_BLOB_STORAGE_ENDPOINT_STRING string = '"https://${pageBlobStorageAccountName}.blob.core.windows.net"'
