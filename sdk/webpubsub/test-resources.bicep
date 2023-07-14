@description('The base resource name.')
param baseName string = '${resourceGroup().name}'

@description('The tenant id to which the application and resources belong.')
param tenantId string = '72f988bf-86f1-41af-91ab-2d7cd011db47'

@description('The client id of the service principal used to run tests.')
param testApplicationId string

@description('This is the object id of the service principal used to run tests.')
param testApplicationOid string

@secure()
@description('The application client secret used to run tests.')
param testApplicationSecret string

@description('Location of the resource.')
param location string = resourceGroup().location

var webPubSubName = '${baseName}-e2e'

// Find role id by heading to the Web Pub Sub resource, selecting Access Control (IAM), Roles, choose the Role,
// then click on View under Details and check out the JSON.
var webPubSubContributorRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '12cf5a90-567b-43ae-8102-96cf46c7d9b4')

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

resource webPubSubContributor 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid('contributor', webPubSubName)
  scope: webPubSub
  properties: {
    roleDefinitionId: webPubSubContributorRoleId
    principalId: testApplicationOid
  }
}

output AZURE_TENANT_ID string = tenantId
output AZURE_CLIENT_ID string = testApplicationId
output AZURE_CLIENT_SECRET string = testApplicationSecret
output AZURE_SUBSCRIPTION_ID string = subscription().subscriptionId
output AZURE_RESOURCE_GROUP_NAME string = resourceGroup().name
output WEB_PUB_SUB_CONNECTION_STRING string = webPubSub.listKeys().primaryConnectionString
output WEB_PUB_SUB_ENDPOINT string = 'https://${webPubSub.properties.hostName}'
