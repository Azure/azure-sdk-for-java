# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0 (2023-09-20)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.RoutingCosmosDBSqlApiProperties` was added

#### `models.CertificateDescription` was modified

* `resourceGroupName()` was added

#### `IotHubManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.IotHubProperties` was modified

* `enableDataResidency()` was added
* `withEnableDataResidency(java.lang.Boolean)` was added

#### `IotHubManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.RoutingEndpoints` was modified

* `cosmosDBSqlContainers()` was added
* `withCosmosDBSqlContainers(java.util.List)` was added

#### `models.IotHubDescription` was modified

* `resourceGroupName()` was added
* `systemData()` was added

## 1.2.0-beta.4 (2023-09-18)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-preview-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.RoutingCosmosDBSqlApiProperties` was modified

* `withCollectionName(java.lang.String)` was removed
* `collectionName()` was removed

#### `models.RoutingEndpoints` was modified

* `withCosmosDBSqlCollections(java.util.List)` was removed
* `cosmosDBSqlCollections()` was removed

#### `models.ErrorDetails` was modified

* `getHttpStatusCode()` was removed

### Features Added

#### `models.RoutingCosmosDBSqlApiProperties` was modified

* `containerName()` was added
* `withContainerName(java.lang.String)` was added

#### `models.RoutingEndpoints` was modified

* `withCosmosDBSqlContainers(java.util.List)` was added
* `cosmosDBSqlContainers()` was added

#### `models.ErrorDetails` was modified

* `httpStatusCode()` was added

## 1.2.0-beta.3 (2023-04-18)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-preview-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.IpVersion` was added

#### `models.IotHubProperties` was modified

* `ipVersion()` was added
* `withIpVersion(models.IpVersion)` was added

## 1.2.0-beta.2 (2022-08-23)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-preview-2022-04-30. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.EncryptionPropertiesDescription` was added

* `models.KeyVaultKeyProperties` was added

* `models.RoutingCosmosDBSqlApiProperties` was added

* `models.RootCertificateProperties` was added

* `models.IotHubPropertiesDeviceStreams` was added

#### `models.CertificateDescription` was modified

* `resourceGroupName()` was added

#### `IotHubManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.IotHubProperties` was modified

* `withRootCertificate(models.RootCertificateProperties)` was added
* `withDeviceStreams(models.IotHubPropertiesDeviceStreams)` was added
* `rootCertificate()` was added
* `deviceStreams()` was added
* `encryption()` was added
* `withEncryption(models.EncryptionPropertiesDescription)` was added

#### `IotHubManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.RoutingEndpoints` was modified

* `withCosmosDBSqlCollections(java.util.List)` was added
* `cosmosDBSqlCollections()` was added

#### `models.IotHubDescription` was modified

* `resourceGroupName()` was added

## 1.2.0-beta.1 (2022-01-24)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-2021-07-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.IotHubProperties` was modified

* `enableDataResidency()` was added
* `withEnableDataResidency(java.lang.Boolean)` was added

#### `models.IotHubDescription` was modified

* `systemData()` was added

## 1.1.0 (2021-08-30)

- Add support for new service API version 2021-07-01
    - Add flag for disabling local authentication to IotHubProperties
    - Add flags to disable device and/or module symmetric key based authentication to IotHubProperties
    - Add flag for restricting outbound network access to IotHubProperties
    - Add settable list of allowed fully qualified domain names for egress from IoT Hub to IotHubProperties
- Upgraded `azure-core` dependency from `1.13.0` to `1.19.0` 
  - [azure-core changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/CHANGELOG.md#1190-2021-08-06)
- Upgraded `azure-core-management` dependency from `1.2.2` to `1.4.0` 
  - [azure-core changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-management/CHANGELOG.md#140-2021-08-06)
  
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
