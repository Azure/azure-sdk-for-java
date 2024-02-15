@description('The base resource name.')
param baseName string = resourceGroup().name

@description('The location of the resources. By default, this is the same as the resource group.')
param location string = resourceGroup().location

var serviceBusAuthRulesName = 'stress-test-auth-rule'
var serviceBusQueueName = 'test-queue'
var serviceBusSessionQueueName = 'test-session-queue'
var serviceBusTopicName = 'test-topic'
var serviceBusSubscriptionName = 'test-subscription'
var serviceBusSessionSubscriptionName = 'test-session-subscription'
var serviceBusMessageLockDuration = 'PT05S'

resource serviceBusNamespace 'Microsoft.ServiceBus/namespaces@2021-11-01' = {
  name: baseName
  location: location
  sku: {
    capacity: 2
    name: 'Premium'
    tier: 'Premium'
  }
  properties: {}
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

resource serviceBusQueue 'Microsoft.ServiceBus/namespaces/queues@2021-11-01' = {
  parent: serviceBusNamespace
  name: serviceBusQueueName
  properties: {
    maxSizeInMegabytes: 5120
    lockDuration: serviceBusMessageLockDuration
  }
}

resource serviceBusSessionQueue 'Microsoft.ServiceBus/namespaces/queues@2021-11-01' = {
  parent: serviceBusNamespace
  name: serviceBusSessionQueueName
  properties: {
    requiresSession: true
  }
}

resource serviceBusTopic 'Microsoft.ServiceBus/namespaces/topics@2021-11-01' = {
  parent: serviceBusNamespace
  name: serviceBusTopicName
}

resource serviceBusSubscription 'Microsoft.ServiceBus/namespaces/topics/subscriptions@2021-11-01' = {
  parent: serviceBusTopic
  name: serviceBusSubscriptionName
  properties: {
    lockDuration: serviceBusMessageLockDuration
  }
}

resource serviceBusSessionSubscription 'Microsoft.ServiceBus/namespaces/topics/subscriptions@2021-11-01' = {
  parent: serviceBusTopic
  name: serviceBusSessionSubscriptionName
  properties: {
    requiresSession: true
  }
}

// Construct servicebus connection string
var serviceBusVersion = serviceBusNamespace.apiVersion
var serviceBusConnectionString = listkeys(serviceBusAuthRulesName, serviceBusVersion).primaryConnectionString

output SERVICEBUS_CONNECTION_STRING string ='"${serviceBusConnectionString}"'
output SERVICEBUS_QUEUE_NAME string = serviceBusQueueName
output SERVICEBUS_SESSION_QUEUE_NAME string = serviceBusSessionQueueName
output SERVICEBUS_TOPIC_NAME string = serviceBusTopicName
output SERVICEBUS_SUBSCRIPTION_NAME string = serviceBusSubscriptionName
output SERVICEBUS_SESSION_SUBSCRIPTION_NAME string = serviceBusSessionSubscriptionName
