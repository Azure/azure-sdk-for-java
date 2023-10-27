@description('The tenant id to which the application and resources belong.')
param tenantId string = '72f988bf-86f1-41af-91ab-2d7cd011db47'

@description('The client id of the service principal used to run tests.')
param testApplicationId string

@description('This is the object id of the service principal used to run tests.')
param testApplicationOid string

@description('The application client secret used to run tests.')
param testApplicationSecret string

var kubernetesServiceClusterAdminRoleId = '/subscriptions/${subscription().subscriptionId}/providers/Microsoft.Authorization/roleDefinitions/0ab0b1a8-8aac-4efd-b8c2-3ee1fb270be8'

resource kubernetesServiceClusterAdminRoleId_name 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid('kubernetsServiceClusterAdminRoleId${resourceGroup().name}')
  properties: {
    roleDefinitionId: kubernetsServiceClusterAdminRoleId
    principalId: testApplicationOid
  }
}

output AZURE_TENANT_ID string = tenantId
output AZURE_CLIENT_ID string = testApplicationId
output AZURE_CLIENT_SECRET string = testApplicationSecret
output AZURE_SUBSCRIPTION_ID string = subscription().subscriptionId
output AZURE_RESOURCE_GROUP_NAME string = resourceGroup().name
