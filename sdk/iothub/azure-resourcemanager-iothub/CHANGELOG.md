# Release History

## 1.4.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.4.0-beta.2 (2026-05-08)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package api-version 2025-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.IotHubQuotaMetricInfoListResult` was removed

#### `models.EndpointHealthDataListResult` was removed

#### `models.JobResponseListResult` was removed

#### `models.IotHubDescriptionListResult` was removed

#### `models.EventHubConsumerGroupsListResult` was removed

#### `models.IotHubSkuDescriptionListResult` was removed

#### `models.OperationListResult` was removed

#### `models.SharedAccessSignatureAuthorizationRuleListResult` was removed

#### `models.EncryptionPropertiesDescription` was modified

* `validate()` was removed

#### `models.FeedbackProperties` was modified

* `validate()` was removed

#### `models.MatchedRoute` was modified

* `MatchedRoute()` was changed to private access
* `validate()` was removed
* `withProperties(models.RouteProperties)` was removed

#### `models.GroupIdInformationProperties` was modified

* `GroupIdInformationProperties()` was changed to private access
* `withRequiredMembers(java.util.List)` was removed
* `validate()` was removed
* `withGroupId(java.lang.String)` was removed
* `withRequiredZoneNames(java.util.List)` was removed

#### `models.ArmIdentity` was modified

* `validate()` was removed

#### `models.DeviceRegistry` was modified

* `validate()` was removed

#### `models.OperationInputs` was modified

* `validate()` was removed

#### `models.TestRouteInput` was modified

* `validate()` was removed

#### `models.RoutingMessage` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.IotHubCapacity` was modified

* `IotHubCapacity()` was changed to private access
* `validate()` was removed

#### `models.RouteCompilationError` was modified

* `RouteCompilationError()` was changed to private access
* `withLocation(models.RouteErrorRange)` was removed
* `withSeverity(models.RouteErrorSeverity)` was removed
* `validate()` was removed
* `withMessage(java.lang.String)` was removed

#### `models.KeyVaultKeyProperties` was modified

* `validate()` was removed

#### `models.UserSubscriptionQuota` was modified

* `UserSubscriptionQuota()` was changed to private access
* `withLimit(java.lang.Integer)` was removed
* `withId(java.lang.String)` was removed
* `validate()` was removed
* `withType(java.lang.String)` was removed
* `withUnit(java.lang.String)` was removed
* `withCurrentValue(java.lang.Integer)` was removed
* `withName(models.Name)` was removed

#### `models.EventHubConsumerGroupName` was modified

* `validate()` was removed

#### `models.IpFilterRule` was modified

* `validate()` was removed

#### `models.EventHubConsumerGroupBodyDescription` was modified

* `validate()` was removed

#### `models.IotHubLocationDescription` was modified

* `IotHubLocationDescription()` was changed to private access
* `validate()` was removed
* `withRole(models.IotHubReplicaRoleType)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.IotHubResources` was modified

* `models.IotHubDescription deleteById(java.lang.String)` -> `void deleteById(java.lang.String)`
* `models.IotHubDescription delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.IotHubDescription deleteByResourceGroup(java.lang.String,java.lang.String)` -> `void deleteByResourceGroup(java.lang.String,java.lang.String)`
* `models.IotHubDescription deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.RoutingProperties` was modified

* `validate()` was removed

#### `models.IotHubProperties` was modified

* `validate()` was removed

#### `models.Name` was modified

* `Name()` was changed to private access
* `withValue(java.lang.String)` was removed
* `validate()` was removed
* `withLocalizedValue(java.lang.String)` was removed

#### `models.FallbackRouteProperties` was modified

* `validate()` was removed

#### `models.CertificatePropertiesWithNonce` was modified

* `CertificatePropertiesWithNonce()` was changed to private access
* `withPolicyResourceId(java.lang.String)` was removed
* `validate()` was removed

#### `models.RoutingServiceBusQueueEndpointProperties` was modified

* `validate()` was removed

#### `models.RoutingStorageContainerProperties` was modified

* `validate()` was removed

#### `models.RoutingTwinProperties` was modified

* `validate()` was removed

#### `models.EnrichmentProperties` was modified

* `validate()` was removed

#### `models.RouteProperties` was modified

* `validate()` was removed

#### `models.FailoverInput` was modified

* `validate()` was removed

#### `models.ExportDevicesRequest` was modified

* `validate()` was removed

#### `models.TagsResource` was modified

* `validate()` was removed

#### `models.NetworkRuleSetProperties` was modified

* `validate()` was removed

#### `models.CertificateProperties` was modified

* `validate()` was removed

#### `models.RouteErrorRange` was modified

* `RouteErrorRange()` was changed to private access
* `withStart(models.RouteErrorPosition)` was removed
* `validate()` was removed
* `withEnd(models.RouteErrorPosition)` was removed

#### `models.RoutingCosmosDBSqlApiProperties` was modified

* `validate()` was removed

#### `models.PrivateEndpointConnections` was modified

* `models.PrivateEndpointConnection delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.PrivateEndpointConnection delete(java.lang.String,java.lang.String,java.lang.String)` -> `void delete(java.lang.String,java.lang.String,java.lang.String)`

#### `models.EventHubProperties` was modified

* `validate()` was removed

#### `models.ImportDevicesRequest` was modified

* `validate()` was removed

#### `models.StorageEndpointProperties` was modified

* `validate()` was removed

#### `models.RootCertificateProperties` was modified

* `validate()` was removed

#### `models.RouteErrorPosition` was modified

* `RouteErrorPosition()` was changed to private access
* `validate()` was removed
* `withColumn(java.lang.Integer)` was removed
* `withLine(java.lang.Integer)` was removed

#### `models.TestAllRoutesInput` was modified

* `validate()` was removed

#### `models.RoutingEndpoints` was modified

* `validate()` was removed

#### `models.IotHubPropertiesDeviceStreams` was modified

* `validate()` was removed

#### `models.TestRouteResultDetails` was modified

* `TestRouteResultDetails()` was changed to private access
* `validate()` was removed
* `withCompilationErrors(java.util.List)` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.ErrorDetails` was modified

* `ErrorDetails()` was changed to private access
* `validate()` was removed

#### `models.CloudToDeviceProperties` was modified

* `validate()` was removed

#### `models.MessagingEndpointProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.ArmUserIdentity` was modified

* `validate()` was removed

#### `models.CertificateVerificationDescription` was modified

* `validate()` was removed

#### `models.ManagedIdentity` was modified

* `validate()` was removed

#### `models.IotHubSkuInfo` was modified

* `validate()` was removed

#### `models.RoutingEventHubProperties` was modified

* `validate()` was removed

#### `models.RoutingTwin` was modified

* `validate()` was removed

#### `models.NetworkRuleSetIpRule` was modified

* `validate()` was removed

#### `models.RoutingServiceBusTopicEndpointProperties` was modified

* `validate()` was removed

#### `models.PrivateEndpointConnectionProperties` was modified

* `validate()` was removed

### Features Added

#### `models.EventHubConsumerGroupInfo` was modified

* `systemData()` was added

#### `models.CertificateDescription` was modified

* `systemData()` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added

## 1.4.0-beta.1 (2025-11-17)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-preview-2025-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.EncryptionPropertiesDescription` was added

* `models.DeviceRegistry` was added

* `models.KeyVaultKeyProperties` was added

* `models.IpVersion` was added

* `models.RootCertificateProperties` was added

* `models.IotHubPropertiesDeviceStreams` was added

#### `models.IotHubProperties` was modified

* `rootCertificate()` was added
* `deviceStreams()` was added
* `withDeviceStreams(models.IotHubPropertiesDeviceStreams)` was added
* `withRootCertificate(models.RootCertificateProperties)` was added
* `encryption()` was added
* `withDeviceRegistry(models.DeviceRegistry)` was added
* `withEncryption(models.EncryptionPropertiesDescription)` was added
* `withIpVersion(models.IpVersion)` was added
* `deviceRegistry()` was added
* `ipVersion()` was added

#### `models.CertificatePropertiesWithNonce` was modified

* `policyResourceId()` was added
* `withPolicyResourceId(java.lang.String)` was added

#### `models.CertificateProperties` was modified

* `withPolicyResourceId(java.lang.String)` was added
* `policyResourceId()` was added

## 1.3.0 (2024-12-11)

- Azure Resource Manager IotHub client library for Java. This package contains Microsoft Azure SDK for IotHub Management SDK. Use this API to manage the IoT hubs in your Azure subscription. Package tag package-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.ErrorDetails` was modified

* `getCode()` was added
* `getAdditionalInfo()` was added
* `getDetails()` was added
* `getMessage()` was added
* `getTarget()` was added

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
