@description('The tenant id to which the application and resources belong.')
param tenantId string = '72f988bf-86f1-41af-91ab-2d7cd011db47'

@description('The client id of the service principal used to run tests.')
param testApplicationId string

@description('The client OID to grant access to test resources.')
param testApplicationOid string

@description('The application client secret used to run tests.')
param testApplicationSecret string

@minLength(6)
@maxLength(50)
@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resource. By default, this is the same as the resource group.')
param location string = 'westus2'

@description('A new GUID used to identify the role assignment')
param roleNameGuid string = newGuid()

var contributorRoleId = '/subscriptions/${subscription().subscriptionId}/providers/Microsoft.Authorization/roleDefinitions/b24988ac-6180-42a0-ab88-20f7382dd24c'

resource contributorRoleId_name 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid('contributorRoleId${resourceGroup().name}')
  properties: {
    roleDefinitionId: contributorRoleId
    principalId: testApplicationOid
  }
}

var adtOwnerRoleDefinitionId = '/subscriptions/${subscription().subscriptionId}/providers/Microsoft.Authorization/roleDefinitions/bcd981a7-7f74-457b-83e1-cceb9e632ffe'

resource digitaltwin 'Microsoft.DigitalTwins/digitalTwinsInstances@2020-12-01' = {
    name: baseName
    location: location
    properties: {}
}

resource digitaltwinRoleAssignment 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
	name: roleNameGuid
    properties: {
        roleDefinitionId: adtOwnerRoleDefinitionId
        principalId: testApplicationOid
    }
    scope: digitaltwin
}

resource eventHubNamespace 'Microsoft.EventHub/namespaces@2018-01-01-preview' = {
    name: baseName
    location: location
    sku: {
        name: 'Standard'
        tier: 'Standard'
        capacity: 1
    }
    properties: {
        zoneRedundant: false
        isAutoInflateEnabled: false
        maximumThroughputUnits: 0
        kafkaEnabled: false
    }
}

resource eventHubNamespaceEventHub 'Microsoft.EventHub/namespaces/eventhubs@2017-04-01' = {
    name: '${eventHubNamespace.name}/${baseName}'
    properties: {
        messageRetentionInDays: 7
        partitionCount: 4
        status: 'Active'
    }
}

resource eventHubNamespaceAuthRules 'Microsoft.EventHub/namespaces/AuthorizationRules@2017-04-01' = {
    name: '${eventHubNamespace.name}/RootManageSharedAccessKey'
    properties: {
        rights: [
            'Listen'
            'Manage'
            'Send'
        ]
    }
}

resource eventHubNamespaceEventHubAuthRules 'Microsoft.EventHub/namespaces/eventhubs/authorizationRules@2017-04-01' = {
    name: '${eventHubNamespaceEventHub.name}/owner'
    properties: {
        rights: [
            'Listen'
            'Manage'
            'Send'
        ]
    }
}

resource digitaltwinEndpoints 'Microsoft.DigitalTwins/digitalTwinsInstances/endpoints@2020-12-01' = {
    name: '${digitaltwin.name}/someEventHubEndpoint'
    properties: {
        endpointType: 'EventHub'
        authenticationType: 'KeyBased'
        connectionStringPrimaryKey: listKeys(eventHubNamespaceEventHubAuthRules.id, '2017-04-01').primaryConnectionString
        connectionStringSecondaryKey: listKeys(eventHubNamespaceEventHubAuthRules.id, '2017-04-01').secondaryConnectionString
    }
}

output DIGITALTWINS_URL string = 'https://${digitaltwin.properties.hostName}'
output AZURE_TENANT_ID string = tenantId
output AZURE_CLIENT_ID string = testApplicationId
output AZURE_CLIENT_SECRET string = testApplicationSecret
output AZURE_SUBSCRIPTION_ID string = subscription().subscriptionId
output AZURE_RESOURCE_GROUP_NAME string = resourceGroup().name
