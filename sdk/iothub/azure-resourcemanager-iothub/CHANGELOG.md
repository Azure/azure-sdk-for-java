# Release History

## 1.1.0-beta.1 (Unreleased)


## 1.0.0 (2021-05-14)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.CertificateBodyDescription` was removed

#### `models.IotHubResources` was modified

* `createEventHubConsumerGroupWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `createEventHubConsumerGroup(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.CertificateDescription$Update` was modified

* `withCertificate(java.lang.String)` was removed

#### `models.CertificateDescription$Definition` was modified

* `withCertificate(java.lang.String)` was removed

### New Feature

* `models.ArmIdentity` was added

* `models.ResourceIdentityType` was added

* `models.EventHubConsumerGroupName` was added

* `models.EventHubConsumerGroupInfo$DefinitionStages` was added

* `models.EventHubConsumerGroupBodyDescription` was added

* `models.EventHubConsumerGroupInfo$Definition` was added

* `models.NetworkRuleIpAction` was added

* `models.NetworkRuleSetProperties` was added

* `models.DefaultAction` was added

* `models.ArmUserIdentity` was added

* `models.ManagedIdentity` was added

* `models.NetworkRuleSetIpRule` was added

#### `models.EventHubConsumerGroupInfo` was modified

* `refresh()` was added
* `refresh(com.azure.core.util.Context)` was added

#### `models.IotHubResources` was modified

* `defineEventHubConsumerGroup(java.lang.String)` was added
* `getEventHubConsumerGroupByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteEventHubConsumerGroupById(java.lang.String)` was added
* `deleteEventHubConsumerGroupByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getEventHubConsumerGroupById(java.lang.String)` was added

#### `models.IotHubProperties` was modified

* `networkRuleSets()` was added
* `withNetworkRuleSets(models.NetworkRuleSetProperties)` was added

#### `models.RoutingServiceBusQueueEndpointProperties` was modified

* `withIdentity(models.ManagedIdentity)` was added
* `identity()` was added

#### `models.RoutingStorageContainerProperties` was modified

* `withIdentity(models.ManagedIdentity)` was added
* `identity()` was added

#### `models.ExportDevicesRequest` was modified

* `configurationsBlobName()` was added
* `includeConfigurations()` was added
* `withIncludeConfigurations(java.lang.Boolean)` was added
* `withIdentity(models.ManagedIdentity)` was added
* `identity()` was added
* `withConfigurationsBlobName(java.lang.String)` was added

#### `models.ImportDevicesRequest` was modified

* `includeConfigurations()` was added
* `withIdentity(models.ManagedIdentity)` was added
* `withConfigurationsBlobName(java.lang.String)` was added
* `identity()` was added
* `withIncludeConfigurations(java.lang.Boolean)` was added
* `configurationsBlobName()` was added

#### `models.StorageEndpointProperties` was modified

* `identity()` was added
* `withIdentity(models.ManagedIdentity)` was added

#### `models.EndpointHealthData` was modified

* `lastSuccessfulSendAttemptTime()` was added
* `lastKnownErrorTime()` was added
* `lastKnownError()` was added
* `lastSendAttemptTime()` was added

#### `models.CertificateDescription$Update` was modified

* `withProperties(models.CertificateProperties)` was added

#### `models.CertificateDescription$Definition` was modified

* `withProperties(models.CertificateProperties)` was added

#### `models.RoutingEventHubProperties` was modified

* `withIdentity(models.ManagedIdentity)` was added
* `identity()` was added

#### `models.RoutingServiceBusTopicEndpointProperties` was modified

* `identity()` was added
* `withIdentity(models.ManagedIdentity)` was added

#### `models.IotHubDescription` was modified

* `identity()` was added

#### `models.IotHubDescription$Definition` was modified

* `withIdentity(models.ArmIdentity)` was added

## 1.0.0-beta.2 (2021-05-13)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-2020-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2021-03-02)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-2020-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
