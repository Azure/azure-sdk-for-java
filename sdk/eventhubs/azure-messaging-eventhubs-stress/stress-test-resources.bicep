@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resources. By default, this is the same as the resource group.')
param location string = resourceGroup().location

var subBaseName = substring(baseName, 0, min(length(baseName), 10))
var eventHubsNamespaceName = '${subBaseName}-eventhubs'
var eventHubName = 'test-event-hub'
var eventHubAuthRulesName = '${subBaseName}-eventhub-rules'
var secondEventHubName = 'test-event-hub2'
var secondEventHubAuthRulesName = '${subBaseName}-eventhub-rules2'

// storage account must be between 3 and 24 characters in length and use numbers and lower-case letters only 
var storageAccountName = replace('${subBaseName}account', '-', '')
var storageContainerName = 'test-blob-container'

resource eventHubsNamespace 'Microsoft.EventHub/namespaces@2021-11-01' = {
  name: eventHubsNamespaceName
  location: location
  sku: {
    name: 'Standard'
    tier: 'Standard'
  }
  properties: {}
}

resource eventHub 'Microsoft.EventHub/namespaces/eventhubs@2021-11-01' = {
  parent: eventHubsNamespace
  name: eventHubName
  properties: {
    messageRetentionInDays: 1
    partitionCount: 32
  }
}

resource eventHubAuthRules 'Microsoft.EventHub/namespaces/eventhubs/authorizationRules@2021-11-01' = {
  parent: eventHub
  name: eventHubAuthRulesName
  properties:  {
    rights: [
      'Manage'
      'Send'
      'Listen'
    ]
  }
}

resource secondEventHub 'Microsoft.EventHub/namespaces/eventhubs@2021-11-01' = {
  parent: eventHubsNamespace
  name: secondEventHubName
  properties: {
    messageRetentionInDays: 1
    partitionCount: 32
  }
}

resource secondEventHubAuthRules 'Microsoft.EventHub/namespaces/eventhubs/authorizationRules@2021-11-01' = {
  parent: secondEventHub
  name: secondEventHubAuthRulesName
  properties:  {
    rights: [
      'Manage'
      'Send'
      'Listen'
    ]
  }
}


resource storageAccount 'Microsoft.Storage/storageAccounts@2021-09-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
  }
}

resource storageContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2021-09-01' = {
  name: '${storageAccountName}/default/${storageContainerName}'
  dependsOn: [
    storageAccount
  ]
}

// Construct eventhubs connection string
var eventHubsVersion = eventHubsNamespace.apiVersion
var eventHubsConnectionString = listkeys(eventHubAuthRulesName, eventHubsVersion).primaryConnectionString
var secondEventHubsConnectionString = listkeys(secondEventHubAuthRulesName, eventHubsVersion).primaryConnectionString

// Construct storage account connection string
var endpointSuffix = environment().suffixes.storage
var storageAccountId = storageAccount.id
var storageAccountVersion = storageAccount.apiVersion
var storageConnectionString = 'DefaultEndpointsProtocol=https;AccountName=${storageAccountName};EndpointSuffix=${endpointSuffix};AccountKey=${listKeys(storageAccountId, storageAccountVersion).keys[0].value}'

output RESOURCE_GROUP string = resourceGroup().name
output EVENT_HUB_NAMESPACE string = eventHubsNamespaceName
output EVENT_HUB_HOSTNAME string = '${eventHubsNamespaceName}.servicebus.windows.net'
output EVENT_HUB_NAME string = eventHubName
output EVENT_HUBS_CONNECTION_STRING string = '"${eventHubsConnectionString}"'
output STORAGE_CONTAINER_NAME string = storageContainerName
output STORAGE_CONNECTION_STRING string = '"${storageConnectionString}"'
output SECOND_EVENT_HUB_NAME string = secondEventHubName
output SECOND_EVENT_HUBS_CONNECTION_STRING string = '"${secondEventHubsConnectionString}"'
