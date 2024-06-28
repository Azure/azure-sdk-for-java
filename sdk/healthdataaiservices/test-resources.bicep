@minLength(10)
param testApplicationOid string

@minLength(6)
@maxLength(50)
@description('The base resource name.')
param baseName string

param location string = resourceGroup().location

@description('The location of the resource. By default, this is the same as the resource group.')
param deidLocation string = 'canadacentral'

param deploymentTime string = utcNow('u')

var realtimeDataUserRoleId = 'bb6577c4-ea0a-40b2-8962-ea18cb8ecd4e'
var batchDataOwnerRoleId = '8a90fa6b-6997-4a07-8a95-30633a7c97b9'
var storageBlobDataContributor = 'ba92f5b4-2d11-453d-a403-e96b0029c9fe'

var blobStorageName = take(toLower(replace('blob-${baseName}', '-', '')), 24)
var blobContainerName = 'container-${baseName}'
var deidServiceName = 'deid-${baseName}'

resource storageAccount 'Microsoft.Storage/storageAccounts@2022-05-01' = {
  name: blobStorageName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    minimumTlsVersion: 'TLS1_2'
  }
}

resource blobService 'Microsoft.Storage/storageAccounts/blobServices@2022-05-01' = {
  parent: storageAccount
  name: 'default'
}

resource container 'Microsoft.Storage/storageAccounts/blobServices/containers@2022-05-01' = {
  parent: blobService
  name: blobContainerName
}

resource storageRoleAssignment 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(resourceGroup().id, storageAccount.id, testApplicationOid, storageBlobDataContributor)
  properties: {
    roleDefinitionId: resourceId('Microsoft.Authorization/roleDefinitions', storageBlobDataContributor)
    principalId: testApplicationOid
  }
  scope: storageAccount
}

resource testDeidService 'microsoft.healthdataaiservices/deidservices@2023-06-01-preview' = {
  name: deidServiceName
  location: deidLocation
}

resource realtimeRole 'Microsoft.Authorization/roleAssignments@2020-10-01-preview' = {
  name: guid(resourceGroup().id, testDeidService.id, testApplicationOid, realtimeDataUserRoleId)
  scope: testDeidService
  properties: {
    roleDefinitionId: resourceId('Microsoft.Authorization/roleDefinitions', realtimeDataUserRoleId)
    principalId: testApplicationOid
  }
}

resource batchRole 'Microsoft.Authorization/roleAssignments@2020-10-01-preview' = {
  name: guid(resourceGroup().id, testDeidService.id, testApplicationOid, batchDataOwnerRoleId)
  scope: testDeidService
  properties: {
    roleDefinitionId: resourceId('Microsoft.Authorization/roleDefinitions', batchDataOwnerRoleId)
    principalId: testApplicationOid
  }
}

output DEID_SERVICE_ENDPOINT string = testDeidService.properties.serviceUrl
output STORAGE_CONTAINER_NAME string = container.name
output STORAGE_ACCOUNT_NAME string = storageAccount.name
