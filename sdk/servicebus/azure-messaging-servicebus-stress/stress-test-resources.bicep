@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resources. By default, this is the same as the resource group.')
param location string = resourceGroup().location

var subBaseName = '${substring(baseName, 0, min(length(baseName), 12))}-${substring(guid(baseName), 0, 4)}'
var serviceBusNamespaceName = '${subBaseName}-sbns'
var serviceBusAuthRulesName = '${subBaseName}-sbrl'
var serviceBusQueueName = 'test-queue'
var serviceBusTopicName = 'test-topic'
var serviceBusSubscriptionName = 'test-subscription'

resource serviceBusNamespace 'Microsoft.ServiceBus/namespaces@2021-11-01' = {
  name: serviceBusNamespaceName
  location: location
  sku: {
    name: 'Standard'
    tier: 'Standard'
  }
  properties: {}
}

resource serviceBusQueue 'Microsoft.ServiceBus/namespaces/queues@2021-11-01' = {
  parent: serviceBusNamespace
  name: serviceBusQueueName
}

resource serviceBusAuthRules 'Microsoft.ServiceBus/namespaces/AuthorizationRules@2021-11-01'= {
  parent: serviceBusNamespace
  name: serviceBusAuthRulesName
  properties: {
    rights: [
      'Manage'
      'Send'
      'Listen'
    ]
  }
}

resource serviceBusTopic 'Microsoft.ServiceBus/namespaces/topics@2021-11-01' = {
  parent: serviceBusNamespace
  name: serviceBusTopicName
}

resource serviceBusSubscription 'Microsoft.ServiceBus/namespaces/topics/subscriptions@2021-11-01' = {
  parent: serviceBusTopic
  name: serviceBusSubscriptionName
}

// Construct servicebus connection string
var serviceBusVersion = serviceBusNamespace.apiVersion
var serviceBusConnectionString = listkeys(serviceBusAuthRulesName, serviceBusVersion).primaryConnectionString

output SERVICEBUS_CONNECTION_STRING string ='"${serviceBusConnectionString}"'
output SERVICEBUS_QUEUE_NAME string = serviceBusQueueName
output SERVICEBUS_TOPIC_NAME string = serviceBusTopicName 
output SERVICEBUS_SUBSCRIPTION_NAME string = serviceBusSubscriptionName 
