param baseName string
param endpointSuffix string = 'core.windows.net'
param location string = resourceGroup().location
param storageApiVersion string = '2022-09-01'

var primaryAccountName = '${baseName}'

resource primaryAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: primaryAccountName
  location: location
  sku: {
    name: 'Premium_LRS'
  }
  kind: 'FileStorage'
  properties: {}
}

output STORAGE_CONNECTION_STRING string = '"DefaultEndpointsProtocol=https;AccountName=${primaryAccountName};AccountKey=${listKeys(primaryAccount.id, storageApiVersion).keys[0].value};EndpointSuffix=${endpointSuffix}"'
output STORAGE_ENDPOINT_STRING string = '"https://${primaryAccountName}.file.core.windows.net"'
