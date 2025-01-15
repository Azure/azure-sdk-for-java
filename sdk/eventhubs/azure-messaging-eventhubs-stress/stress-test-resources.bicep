@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resources. By default, this is the same as the resource group.')
param location string = resourceGroup().location

@description('The name of the storage account.')
param storageAccountName string = 'stress${uniqueString(resourceGroup().id)}'

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

var eventHubsHttpsEndpoint = replace(eventHubsNamespace.properties.serviceBusEndpoint, 'https://', '')
var eventHubsPortIndex = lastIndexOf(eventHubsHttpsEndpoint, ':')
output EVENT_HUBS_FULLY_QUALIFIED_NAMESPACE string = substring(eventHubsHttpsEndpoint, 0, eventHubsPortIndex)
output EVENT_HUBS_EVENT_HUB_NAME string = eventHub.name

output FORWARD_EVENT_HUB_NAME string = forwardEventHub.name
output FORWARD_PARTITIONS_COUNT int = forwardEventHub.properties.partitionCount

output STORAGE_BLOB_ENDPOINT_URI string = storageAccount.properties.primaryEndpoints.blob
output STORAGE_CONTAINER_NAME string = storageContainerName
