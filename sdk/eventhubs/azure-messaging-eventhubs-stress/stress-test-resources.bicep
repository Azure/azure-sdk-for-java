@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resources. By default, this is the same as the resource group.')
param location string = resourceGroup().location

var eventHubsAuthRulesName = 'stress-test-auth-rule'
var storageContainerName = 'test-blob-container'

resource eventHubsNamespace 'Microsoft.EventHub/namespaces@2021-11-01' = {
  name: baseName
  location: location
  sku: {
    name: 'Premium'
    tier: 'Premium'
    capacity: 2
  }
  properties: {}
}

resource eventHub 'Microsoft.EventHub/namespaces/eventhubs@2021-11-01' = {
  parent: eventHubsNamespace
  name: 'test-event-hub'
  properties: {
    messageRetentionInDays: 1
    partitionCount: 32
  }
}

resource forwardEventHub 'Microsoft.EventHub/namespaces/eventhubs@2021-11-01' = {
  parent: eventHubsNamespace
  name: 'forward-event-hub'
  properties: {
    messageRetentionInDays: 1
    partitionCount: 32
  }
}

resource eventHubsAuthRules 'Microsoft.EventHub/namespaces/authorizationRules@2021-11-01' = {
  parent: eventHubsNamespace
  name: eventHubsAuthRulesName
  properties: {
    rights: [
      'Manage'
      'Send'
      'Listen'
    ]
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2021-09-01' = {
  name: baseName
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
  name: '${baseName}/default/${storageContainerName}'
  dependsOn: [
    storageAccount
  ]
}

// Construct eventhubs connection string
var eventHubsVersion = eventHubsNamespace.apiVersion
var eventHubsConnectionString = listkeys(eventHubsAuthRulesName, eventHubsVersion).primaryConnectionString

// Construct storage account connection string
var endpointSuffix = environment().suffixes.storage
var storageAccountId = storageAccount.id
var storageAccountVersion = storageAccount.apiVersion
var storageConnectionString = 'DefaultEndpointsProtocol=https;AccountName=${baseName};EndpointSuffix=${endpointSuffix};AccountKey=${listKeys(storageAccountId, storageAccountVersion).keys[0].value}'
var forwardPartitionCount = forwardEventHub.properties.partitionCount
output EVENTHUBS_CONNECTION_STRING string = '"${eventHubsConnectionString}"'
output EVENTHUBS_EVENT_HUB_NAME string = eventHub.name

output FORWARD_EVENTHUBS_CONNECTION_STRING string = '"${eventHubsConnectionString}"'
output FORWARD_EVENT_HUB_NAME string = forwardEventHub.name
output FORWARD_PARTITIONS_COUNT string = '"${forwardPartitionCount}"'

output STORAGE_CONTAINER_NAME string = storageContainerName
output STORAGE_CONNECTION_STRING string = '"${storageConnectionString}"'
