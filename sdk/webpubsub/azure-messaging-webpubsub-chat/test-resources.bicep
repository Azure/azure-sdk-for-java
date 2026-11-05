@description('The base resource name.')
param baseName string = resourceGroup().name

@description('This is the object id of the service principal used to run tests.')
param testApplicationOid string

@description('Location of the resource.')
param location string = resourceGroup().location

var webPubSubName = '${baseName}-chat-e2e'
var chatStorageAccountName = toLower('wpsChat${uniqueString(resourceGroup().id)}')
var webPubSubOwnerRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '12cf5a90-567b-43ae-8102-96cf46c7d9b4')
var webPubSubOperatorRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', 'c7393b34-138c-406f-901b-d8cf2b17e6ae')
var blobDataContributorRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', 'ba92f5b4-2d11-453d-a403-e96b0029c9fe')
var tableDataContributorRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '0a9a7e1f-b9d0-4cc4-a60d-0319b160aaa3')
var queueDataContributorRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '974c5e8b-45b9-4653-ba55-5f855dd0fb88')

resource webPubSub 'Microsoft.SignalRService/webPubSub@2025-12-01-preview' = {
  name: webPubSubName
  location: location
  kind: 'WebPubSub'
  sku: {
    name: 'Standard_S1'
    tier: 'Standard'
    capacity: 1
  }
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    tls: {
      clientCertEnabled: false
    }
    networkACLs: {
      defaultAction: 'Deny'
      publicNetwork: {
        allow: [ 'ServerConnection', 'ClientConnection', 'RESTAPI', 'Trace' ]
      }
      privateEndpoints: []
    }
    publicNetworkAccess: 'Enabled'
    disableLocalAuth: false
    disableAadAuth: false
  }
}

resource chatStorageAccount 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: chatStorageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    allowBlobPublicAccess: false
    allowSharedKeyAccess: false
    minimumTlsVersion: 'TLS1_2'
    supportsHttpsTrafficOnly: true
  }
}

resource chatBlobDataContributorRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(webPubSub.id, chatStorageAccount.id, blobDataContributorRoleId)
  scope: chatStorageAccount
  properties: {
    roleDefinitionId: blobDataContributorRoleId
    principalId: webPubSub.identity.principalId
    principalType: 'ServicePrincipal'
  }
}

resource chatTableDataContributorRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(webPubSub.id, chatStorageAccount.id, tableDataContributorRoleId)
  scope: chatStorageAccount
  properties: {
    roleDefinitionId: tableDataContributorRoleId
    principalId: webPubSub.identity.principalId
    principalType: 'ServicePrincipal'
  }
}

resource chatQueueDataContributorRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(webPubSub.id, chatStorageAccount.id, queueDataContributorRoleId)
  scope: chatStorageAccount
  properties: {
    roleDefinitionId: queueDataContributorRoleId
    principalId: webPubSub.identity.principalId
    principalType: 'ServicePrincipal'
  }
}

resource chatPersistentStorage 'Microsoft.SignalRService/webPubSub/persistentStorages@2025-12-01-preview' = {
  name: 'chatstorage'
  parent: webPubSub
  properties: {
    storageAccount: {
      id: chatStorageAccount.id
    }
  }
  dependsOn: [
    chatBlobDataContributorRoleAssignment
    chatTableDataContributorRoleAssignment
    chatQueueDataContributorRoleAssignment
  ]
}

resource chatHub 'Microsoft.SignalRService/webPubSub/hubs@2025-12-01-preview' = {
  name: 'chat'
  parent: webPubSub
  properties: {
    chat: {
      mode: 'Enabled'
      persistentStorage: {
        id: chatPersistentStorage.id
      }
    }
  }
}

resource webPubSubOwnerRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid('owner', webPubSub.id, testApplicationOid)
  scope: webPubSub
  properties: {
    roleDefinitionId: webPubSubOwnerRoleId
    principalId: testApplicationOid
    principalType: 'ServicePrincipal'
  }
}

resource webPubSubOperatorRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid('operator', webPubSub.id, testApplicationOid)
  scope: webPubSub
  properties: {
    roleDefinitionId: webPubSubOperatorRoleId
    principalId: testApplicationOid
    principalType: 'ServicePrincipal'
  }
}

output WPS_CHAT_CONNECTION_STRING string = webPubSub.listKeys().primaryConnectionString
output WPS_CHAT_ENDPOINT string = 'https://${webPubSub.properties.hostName}'