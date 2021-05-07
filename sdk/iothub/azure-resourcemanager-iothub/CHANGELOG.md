# Release History

## 1.0.0-beta.2 (2021-05-07)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-preview-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.CertificateBodyDescription` was removed

#### `models.IotHubResources` was modified

* `createEventHubConsumerGroup(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `createEventHubConsumerGroupWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.CertificateDescription$Update` was modified

* `withIfMatch(java.lang.String)` was removed
* `withCertificate(java.lang.String)` was removed

#### `models.CertificateDescription$Definition` was modified

* `withCertificate(java.lang.String)` was removed

### New Feature

* `models.EncryptionPropertiesDescription` was added

* `models.ArmIdentity` was added

* `models.ResourceIdentityType` was added

* `models.KeyVaultKeyProperties` was added

* `models.EventHubConsumerGroupName` was added

* `models.EventHubConsumerGroupInfo$DefinitionStages` was added

* `models.EventHubConsumerGroupBodyDescription` was added

* `models.EventHubConsumerGroupInfo$Definition` was added

* `models.NetworkRuleIpAction` was added

* `models.NetworkRuleSetProperties` was added

* `models.DefaultAction` was added

* `models.IotHubPropertiesDeviceStreams` was added

* `models.ArmUserIdentity` was added

* `models.ManagedIdentity` was added

* `models.NetworkRuleSetIpRule` was added

#### `models.EventHubConsumerGroupInfo` was modified

* `refresh(com.azure.core.util.Context)` was added
* `refresh()` was added

#### `models.IotHubResources` was modified

* `getEventHubConsumerGroupByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteEventHubConsumerGroupById(java.lang.String)` was added
* `defineEventHubConsumerGroup(java.lang.String)` was added
* `getEventHubConsumerGroupById(java.lang.String)` was added
* `deleteEventHubConsumerGroupByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.IotHubProperties` was modified

* `withEncryption(models.EncryptionPropertiesDescription)` was added
* `withNetworkRuleSets(models.NetworkRuleSetProperties)` was added
* `deviceStreams()` was added
* `encryption()` was added
* `networkRuleSets()` was added
* `withDeviceStreams(models.IotHubPropertiesDeviceStreams)` was added

#### `models.RoutingServiceBusQueueEndpointProperties` was modified

* `withIdentity(models.ManagedIdentity)` was added
* `identity()` was added

#### `models.RoutingStorageContainerProperties` was modified

* `identity()` was added
* `withIdentity(models.ManagedIdentity)` was added

#### `models.ExportDevicesRequest` was modified

* `identity()` was added
* `withIdentity(models.ManagedIdentity)` was added

#### `models.ImportDevicesRequest` was modified

* `withIdentity(models.ManagedIdentity)` was added
* `identity()` was added

#### `models.StorageEndpointProperties` was modified

* `withIdentity(models.ManagedIdentity)` was added
* `identity()` was added

#### `models.EndpointHealthData` was modified

* `lastKnownError()` was added
* `lastSuccessfulSendAttemptTime()` was added
* `lastKnownErrorTime()` was added
* `lastSendAttemptTime()` was added

#### `models.CertificateDescription$Update` was modified

* `ifMatch(java.lang.String)` was added
* `withProperties(models.CertificateProperties)` was added

#### `models.CertificateDescription$Definition` was modified

* `withProperties(models.CertificateProperties)` was added

#### `models.RoutingEventHubProperties` was modified

* `identity()` was added
* `withIdentity(models.ManagedIdentity)` was added

#### `models.RoutingServiceBusTopicEndpointProperties` was modified

* `identity()` was added
* `withIdentity(models.ManagedIdentity)` was added

#### `models.IotHubDescription` was modified

* `identity()` was added

#### `models.IotHubDescription$Definition` was modified

* `withIdentity(models.ArmIdentity)` was added

## 1.0.0-beta.1 (2021-03-02)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-2020-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
