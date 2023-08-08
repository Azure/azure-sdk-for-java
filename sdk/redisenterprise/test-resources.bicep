@description('The tenant id to which the application and resources belong.')
param tenantId string = '72f988bf-86f1-41af-91ab-2d7cd011db47'

@description('The client id of the service principal used to run tests.')
param testApplicationId string

@description('This is the object id of the service principal used to run tests.')
param testApplicationOid string

@description('The application client secret used to run tests.')
param testApplicationSecret string

var contributorRoleId = '/subscriptions/${subscription().subscriptionId}/providers/Microsoft.Authorization/roleDefinitions/b24988ac-6180-42a0-ab88-20f7382dd24c'

resource contributorRoleId_name 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid('contributorRoleId${resourceGroup().name}')
  properties: {
    roleDefinitionId: contributorRoleId
    principalId: testApplicationOid
  }
}

output AZURE_TENANT_ID string = tenantId
output AZURE_CLIENT_ID string = testApplicationId
output AZURE_CLIENT_SECRET string = testApplicationSecret
output AZURE_SUBSCRIPTION_ID string = subscription().subscriptionId
output AZURE_RESOURCE_GROUP_NAME string = resourceGroup().name
