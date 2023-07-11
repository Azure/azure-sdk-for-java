param baseName string = resourceGroup().name
param location string = resourceGroup().location

resource eventHubNamespace 'Microsoft.EventHub/namespaces@2015-08-01' = {
  name: 'eh-${baseName}'
  location: location
  sku: {
    capacity: 40
    name: 'Standard'
    tier: 'Standard'
  }

  resource eventHub 'eventhubs' = {
    name: 'eh-${baseName}-hub'
    properties: {
      partitionCount: 32
    }
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2019-06-01' = {
  name: '${baseName}blob'
  location: location
  kind: 'BlockBlobStorage'
  sku: {
    name: 'Premium_LRS'
  }
}

var eventHubsAuthRuleResourceId = resourceId('Microsoft.EventHub/namespaces/authorizationRules', eventHubNamespace.name, 'RootManageSharedAccessKey')

var name = storageAccount.name
var key = storageAccount.listKeys().keys[0].value
var connectionString = 'DefaultEndpointsProtocol=https;AccountName=${name};AccountKey=${key}'

output EVENTHUBS_CONNECTION_STRING string = listkeys(eventHubsAuthRuleResourceId, '2015-08-01').primaryConnectionString
output EVENTHUB_NAME string = eventHubNamespace::eventHub.name
output AZURE_STORAGE_ACCOUNT_NAME string = name
output AZURE_STORAGE_ACCOUNT_KEY string = key
output AZURE_STORAGE_CONNECTION_STRING string = connectionString
output STANDARD_STORAGE_CONNECTION_STRING string = connectionString
output STORAGE_CONNECTION_STRING string = connectionString







