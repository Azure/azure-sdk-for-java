@description('The base resource name.')
param baseName string = '${resourceGroup().name}'

@description('This is the object id of the service principal used to run tests.')
param testApplicationOid string

@description('Location of the resource.')
param location string = resourceGroup().location

var webPubSubName = '${baseName}-e2e'
var webPubSubSocketIOName = '${baseName}-socketio-e2e'

// Find role id by heading to the Web Pub Sub resource, selecting Access Control (IAM), Roles, choose the Role,
// then click on View under Details and check out the JSON.
var webPubSubOwnerRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '12cf5a90-567b-43ae-8102-96cf46c7d9b4')
var webPubSubOperatorRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', 'c7393b34-138c-406f-901b-d8cf2b17e6ae')

resource webPubSubSocketIO 'Microsoft.SignalRService/webPubSub@2024-10-01-preview' = {
  name: webPubSubSocketIOName
  location: location
  kind: 'SocketIO'
  sku: {
    name: 'Standard_S1'
    tier: 'Standard'
    capacity: 1
  }
  identity: {
    type: 'None'
  }
  properties: {
    tls: {
      clientCertEnabled: false
    }
    publicNetworkAccess: 'Enabled'
    disableLocalAuth: false
    disableAadAuth: false
  }
}

resource webPubSub 'Microsoft.SignalRService/webPubSub@2021-10-01' = {
  name: webPubSubName
  location: location
  sku: {
    name: 'Standard_S1'
    tier: 'Standard'
    capacity: 1
  }
  identity: {
    type: 'None'
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

resource webPubSubSocketIOOwnerRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid('owner', webPubSubSocketIO.id, testApplicationOid)
  scope: webPubSubSocketIO
  properties: {
    roleDefinitionId: webPubSubOwnerRoleId
    principalId: testApplicationOid
    principalType: 'ServicePrincipal'
  }
}

resource webPubSubSocketIOOperatorRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid('operator', webPubSubSocketIO.id, testApplicationOid)
  scope: webPubSubSocketIO
  properties: {
    roleDefinitionId: webPubSubOperatorRoleId
    principalId: testApplicationOid
    principalType: 'ServicePrincipal'
  }
}

output AZURE_SUBSCRIPTION_ID string = subscription().subscriptionId
output AZURE_RESOURCE_GROUP_NAME string = resourceGroup().name
output WEB_PUB_SUB_CONNECTION_STRING string = webPubSub.listKeys().primaryConnectionString
output WEB_PUB_SUB_ENDPOINT string = 'https://${webPubSub.properties.hostName}'
output WEB_PUB_SUB_SOCKETIO_ENDPOINT string = 'https://${webPubSubSocketIO.properties.hostName}'
