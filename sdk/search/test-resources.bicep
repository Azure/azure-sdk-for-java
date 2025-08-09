@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The Search service endpoint suffix.')
param searchEndpointSuffix string = 'search.windows.net'

@description('The Storage account endpoint suffix.')
param storageEndpointSuffix string = 'core.windows.net'

@description('The location of the resource. By default, this is the same as the resource group.')
param location string = resourceGroup().location

@description('The principal to assign the role to. This is application object id.')
param testApplicationOid string

@description('The Search service SKU to create.')
@allowed([
  'free'
  'basic'
  'standard'
])
param searchSku string = 'basic'

// Variables
var searchServiceName = 'azs-java-${baseName}'
var storageAccountName = 'search${baseName}'
var searchIndexDataContributorRoleId = '8ebe5a00-799e-43f5-93ac-243d3dce84a7'
var searchServiceContributorRoleId = '7ca78c08-252a-4471-8644-bb5ff32d4ba0'

// Role assignment for Search Index Data Contributor
resource searchIndexDataContributorRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(resourceGroup().id, testApplicationOid, searchIndexDataContributorRoleId)
  scope: resourceGroup()
  properties: {
    roleDefinitionId: resourceId('Microsoft.Authorization/roleDefinitions', searchIndexDataContributorRoleId)
    principalId: testApplicationOid
  }
}

// Role assignment for Search Service Contributor
resource searchServiceContributorRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(resourceGroup().id, testApplicationOid, searchServiceContributorRoleId)
  scope: resourceGroup()
  properties: {
    roleDefinitionId: resourceId('Microsoft.Authorization/roleDefinitions', searchServiceContributorRoleId)
    principalId: testApplicationOid
  }
}

// Azure Search Service
resource searchService 'Microsoft.Search/searchServices@2025-05-01' = {
  name: searchServiceName
  location: location
  sku: {
    name: searchSku
  }
  properties: {
    replicaCount: 1
    partitionCount: 1
    hostingMode: 'default'
    publicNetworkAccess: 'enabled'
    networkRuleSet: {
      ipRules: []
    }
    disableLocalAuth: true
    authOptions: null
    endpoint: 'https://${searchServiceName}.${searchEndpointSuffix}'
  }
}

// Storage Account
resource storageAccount 'Microsoft.Storage/storageAccounts@2025-01-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
    allowSharedKeyAccess: false
    supportsHttpsTrafficOnly: true
    minimumTlsVersion: 'TLS1_2'
  }
}

resource blobServices 'Microsoft.Storage/storageAccounts/blobServices@2025-01-01' = {
  parent: storageAccount
  name: 'default'
}

// Blob Container
resource blobContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2025-01-01' = {
  parent: blobServices
  name: 'searchcontainer'
}

// Outputs
output SEARCH_SERVICE_ENDPOINT string = searchService.properties.endpoint
