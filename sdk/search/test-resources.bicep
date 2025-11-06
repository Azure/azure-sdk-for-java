targetScope = 'resourceGroup'

@minLength(4)
@maxLength(20)
@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resource. By default, this is the same as the resource group.')
param location string = resourceGroup().location

@description('The tenant ID to which the application and resources belong.')
param tenantId string = subscription().tenantId

@description('The client OID to grant access to test resources.')
param testApplicationOid string = deployer().objectId

@description('The Search service endpoint suffix.')
param searchEndpointSuffix string = 'search.windows.net'

@description('The Search service SKU to create.')
@allowed([
  'basic'
  'standard'
])
param searchSku string = 'basic'

// Variables
var guidSeed = guid(baseName)
var unique = uniqueString(guidSeed)
var searchServiceName = 'azs-java-${unique}'
var storageAccountName = 'search${unique}'

// Static resource group name for TME tenant
var staticResourceGroupName = 'static-test-resources'

// Can this deployment access known static resources
var canUseStatic = tenantId == '70a036f6-8e4d-4615-bad6-149c02e7720d' && subscription().id == '4d042dc6-fe17-4698-a23f-ec6a8d1e98f4'

// Deployed OpenAI resource for non-TME tenants
resource openai 'Microsoft.CognitiveServices/accounts@2025-06-01' = if (!canUseStatic) {
  name: toLower(baseName)
  location: location
  kind: 'OpenAI'
  sku: {
    name: 'S0'
  }
  properties: {
    customSubDomainName: toLower(baseName)
    publicNetworkAccess: 'Enabled'
    disableLocalAuth: true
  }
  // Deployment of the gpt-4.1-nano model
  resource openaiDeployment 'deployments' = {
    name: 'search-knowledge-agent-model'
    sku: {
      name: 'Standard'
      capacity: 250000
    }
    properties: {
      model: {
        format: 'OpenAI'
        name: 'gpt-4.1-nano'
      }
    }
  }
}

// Static OpenAI resource for TME tenant
resource openaiStatic 'Microsoft.CognitiveServices/accounts@2025-06-01' existing = if (canUseStatic) {
  name: 'azsdk-openai-shared-test'
  scope: resourceGroup(staticResourceGroupName)
  // Static model deployment
  resource openaiStaticDeployment 'deployments' existing = {
    name: 'search-knowledge-agent-model'
  }
}

// Managed identity to place on the search service
resource searchServiceIdentity 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' = if (!canUseStatic) {
  name: '${baseName}-search-service-identity'
  location: location
}

resource staticSearchServiceIdentity 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' existing = if (canUseStatic) {
  name: 'azsdk-search-service-identity'
  scope: resourceGroup(staticResourceGroupName)
}

// Azure AI Search service
resource search 'Microsoft.Search/searchServices@2025-05-01' = {
  name: searchServiceName
  location: location
  sku: {
    name: searchSku
  }
  properties: {
    disableLocalAuth: true
    replicaCount: 1
    partitionCount: 1
    hostingMode: 'default'
    publicNetworkAccess: 'enabled'
    networkRuleSet: {
      ipRules: []
    }
    authOptions: null
    endpoint: 'https://${searchServiceName}.${searchEndpointSuffix}'
    semanticSearch: 'free'
  }
  identity: {
    type: 'UserAssigned'
    userAssignedIdentities: {
      '${canUseStatic ? searchServiceIdentity.id : staticSearchServiceIdentity.id}': {}
    }
  }
}

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
    allowBlobPublicAccess: false
    supportsHttpsTrafficOnly: true
    minimumTlsVersion: 'TLS1_2'
  }

  resource blobServices 'blobServices' = {
    name: 'default'
    resource blobContainer 'containers' = {
      name: 'searchcontainer'
      properties: {
        publicAccess: 'None'
      }
    }
  }
}

// Role assignments:
// Identity           | Resource         | Role
// -------------------------------------------------------------------------------
// search service      | storage account | Storage Blob Data Reader
// test application    | storage account | Storage Blob Data Contributor
// test application    | search service  | Search Index Data Contributor
// test application    | search service  | Search Service Contributor
// search service      | openai account  | Cognitive Services OpenAI Contributor

// Storage Blob Data Reader role definition
resource storageBlobDataReaderRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  // This is the Storage Blob Data Reader role
  // See https://learn.microsoft.com/azure/role-based-access-control/built-in-roles#storage-blob-data-reader
  name: '2a2b9908-6ea1-4ae2-8e65-a410df84e7d1'
}

// Storage Blob Data Contributor role definition
resource storageBlobDataContributorRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  // This is the Storage Blob Data Contributor role
  // See https://learn.microsoft.com/azure/role-based-access-control/built-in-roles#storage-blob-data-contributor
  name: 'ba92f5b4-2d11-453d-a403-e96b0029c9fe'
}

// Search Index Data Contributor role definition
resource searchIndexDataContributorRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  // This is the Search Index Data Contributor role
  // See https://learn.microsoft.com/azure/role-based-access-control/built-in-roles#search-index-data-contributor
  name: '8ebe5a00-799e-43f5-93ac-243d3dce84a7'
}

// Search Service Contributor role definition
resource searchServiceContributorRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  // This is the Search Service Contributor role
  // See https://learn.microsoft.com/azure/role-based-access-control/built-in-roles#search-service-contributor
  name: '7ca78c08-252a-4471-8644-bb5ff32d4ba0'
}

// Assign Storage Blob Data Reader role for Azure Search service identity on the storage account
resource search_Storage_RoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(storageBlobDataReaderRoleDefinition.id, search.id, storageAccount.id)
  scope: storageAccount
  properties: {
    principalId: tenantId != '70a036f6-8e4d-4615-bad6-149c02e7720d'
      ? searchServiceIdentity.properties.principalId
      : staticSearchServiceIdentity.properties.principalId
    roleDefinitionId: storageBlobDataReaderRoleDefinition.id
  }
}

// Assign Storage Blob Data Reader role for Azure Search service identity on the storage account
resource testApp_Storage_RoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(storageBlobDataContributorRoleDefinition.id, testApplicationOid, storageAccount.id)
  scope: storageAccount
  properties: {
    principalId: testApplicationOid
    roleDefinitionId: storageBlobDataContributorRoleDefinition.id
  }
}

// Assign Search Index Data Reader role to testApplicationOid
resource testApp_search_indexDataReaderRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(searchIndexDataContributorRoleDefinition.id, testApplicationOid, search.id)
  scope: search
  properties: {
    principalId: testApplicationOid
    roleDefinitionId: searchIndexDataContributorRoleDefinition.id
  }
}

// Assign Search Service Contributor role to testApplicationOid
resource testApp_search_contributorRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(searchServiceContributorRoleDefinition.id, testApplicationOid, search.id)
  scope: search
  properties: {
    principalId: testApplicationOid
    roleDefinitionId: searchServiceContributorRoleDefinition.id
  }
}

// Cognitive Services OpenAI Contributor role definition
resource openaiContributorRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  // Cognitive Services OpenAI Contributor role
  // See https://learn.microsoft.com/azure/role-based-access-control/built-in-roles#cognitive-services-openai-contributor
  name: 'a001fd3d-188f-4b5d-821b-7da978bf7442'
}

// Assign Cognitive Services OpenAI Contributor role to the search resource's identity if we created the OpenAI resource
resource search_openAi_roleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = if (!canUseStatic) {
  name: guid(openaiContributorRoleDefinition.id, searchServiceIdentity.id, openai.id)
  scope: openai
  properties: {
    principalId: searchServiceIdentity.properties.principalId
    roleDefinitionId: openaiContributorRoleDefinition.id
  }
}

// Outputs
output SEARCH_SERVICE_ENDPOINT string = search.properties.endpoint
output SEARCH_STORAGE_ACCOUNT_NAME string = storageAccount.name
output SEARCH_STORAGE_CONTAINER_NAME string = storageAccount::blobServices::blobContainer.name
output SEARCH_OPENAI_ENDPOINT string = canUseStatic ? openaiStatic.properties.endpoint : openai.properties.endpoint
output SEARCH_OPENAI_DEPLOYMENT_NAME string = canUseStatic
  ? openaiStatic::openaiStaticDeployment.name
  : openai::openaiDeployment.name
output SEARCH_OPENAI_MODEL_NAME string = canUseStatic
  ? openaiStatic::openaiStaticDeployment.properties.model.name
  : openai::openaiDeployment.properties.model.name
output SEARCH_USER_ASSIGNED_IDENTITY string = canUseStatic
  ? '/subscriptions/${subscription().subscriptionId}/resourceGroups/${staticResourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/${staticSearchServiceIdentity.name}'
  : '/subscriptions/${subscription().subscriptionId}/resourceGroups/${resourceGroup().name}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/${searchServiceIdentity.name}'
