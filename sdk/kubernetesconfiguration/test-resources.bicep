@description('The tenant id to which the application and resources belong.')
param tenantId string = '72f988bf-86f1-41af-91ab-2d7cd011db47'

@description('The client id of the service principal used to run tests.')
param testApplicationId string

@description('This is the object id of the service principal used to run tests.')
param testApplicationOid string

@description('The application client secret used to run tests.')
param testApplicationSecret string

var ownerRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '8e3af657-a8ff-443c-a75c-2fe8c4bcb635')

resource ownerRoleId_name 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid('ownerRoleId${resourceGroup().name}')
  scope: subscription()
  properties: {
    roleDefinitionId: ownerRoleId
    principalId: testApplicationOid
  }
}

output AZURE_TENANT_ID string = tenantId
output AZURE_CLIENT_ID string = testApplicationId
output AZURE_CLIENT_SECRET string = testApplicationSecret
output AZURE_SUBSCRIPTION_ID string = subscription().subscriptionId
output AZURE_RESOURCE_GROUP_NAME string = resourceGroup().name
