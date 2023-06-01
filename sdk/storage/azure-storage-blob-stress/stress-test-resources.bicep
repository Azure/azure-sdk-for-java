param baseName string
param endpointSuffix string = 'core.windows.net'
param location string = resourceGroup().location
param storageApiVersion string = '2019-06-01'

var primaryAccountName = 'prim${baseName}'

resource primaryAccount 'Microsoft.Storage/storageAccounts@2019-06-01' = {
  name: primaryAccountName
  location: location
  sku: {
    name: 'Standard_RAGRS'
    tier: 'Standard'
  }
  kind: 'StorageV2'
  properties: { }
}

output STORAGE_CONNECTION_STRING string = '"DefaultEndpointsProtocol=https;AccountName=${primaryAccountName};AccountKey=${listKeys(primaryAccount.id, storageApiVersion).keys[0].value};EndpointSuffix=${endpointSuffix}"'
