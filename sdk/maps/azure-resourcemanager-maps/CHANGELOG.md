# Release History

## 1.2.0-beta.1 (2026-03-23)

- Azure Resource Manager Azure Maps client library for Java. This package contains Microsoft Azure SDK for Azure Maps Management SDK. Resource Provider. Package api-version 2025-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationDetail` was removed

#### `models.MetricSpecification` was removed

#### `models.Dimension` was removed

#### `models.MapsOperations` was removed

#### `models.MapsAccounts` was removed

#### `models.ServiceSpecification` was removed

#### `models.CreatorList` was removed

#### `models.LinkedResource` was modified

* `validate()` was removed

#### `models.MapsAccountUpdateParameters` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `validate()` was removed

#### `models.CorsRule` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `validate()` was removed

#### `models.CustomerManagedKeyEncryption` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `validate()` was removed
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.CorsRules` was modified

* `validate()` was removed

#### `models.MapsKeySpecification` was modified

* `validate()` was removed

#### `models.AccountSasParameters` was modified

* `validate()` was removed

#### `models.Maps` was modified

* `list(com.azure.core.util.Context)` was removed
* `list()` was removed

#### `models.Kind` was modified

* `GEN1` was removed

#### `models.CreatorUpdateParameters` was modified

* `validate()` was removed

#### `models.CustomerManagedKeyEncryptionKeyIdentity` was modified

* `validate()` was removed
* `withDelegatedIdentityClientId(java.util.UUID)` was removed
* `java.util.UUID delegatedIdentityClientId()` -> `java.lang.String delegatedIdentityClientId()`

#### `models.Name` was modified

* `S0` was removed
* `S1` was removed

#### `models.Encryption` was modified

* `validate()` was removed

### Features Added

* `models.PrivateEndpoint` was added

* `models.OperationStatusResult` was added

* `models.Operation` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.OperationResults` was added

* `models.PrivateLinkResources` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.ActionType` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.Origin` was added

* `models.PublicNetworkAccess` was added

* `models.LocationsItem` was added

* `models.PrivateEndpointConnections` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.OperationStatus` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpointConnection` was added

#### `models.MapsAccountUpdateParameters` was modified

* `publicNetworkAccess()` was added
* `withLocations(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `locations()` was added
* `privateEndpointConnections()` was added

#### `AzureMapsManager` was modified

* `operationResults()` was added
* `operationStatus()` was added
* `privateLinkResources()` was added
* `privateEndpointConnections()` was added

#### `models.MapsAccount$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withLocations(java.util.List)` was added

#### `models.CreatorUpdateParameters` was modified

* `consumedStorageUnitSizeInBytes()` was added
* `withTotalStorageUnitSizeInBytes(java.lang.Integer)` was added
* `withConsumedStorageUnitSizeInBytes(java.lang.Integer)` was added
* `totalStorageUnitSizeInBytes()` was added

#### `models.CustomerManagedKeyEncryptionKeyIdentity` was modified

* `withFederatedClientId(java.lang.String)` was added
* `federatedClientId()` was added
* `withDelegatedIdentityClientId(java.lang.String)` was added

#### `models.Creator$Update` was modified

* `withConsumedStorageUnitSizeInBytes(java.lang.Integer)` was added
* `withTotalStorageUnitSizeInBytes(java.lang.Integer)` was added

## 1.1.0 (2024-12-11)

- Azure Resource Manager AzureMaps client library for Java. This package contains Microsoft Azure SDK for AzureMaps Management SDK. Azure Maps. Package tag package-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0 (2023-08-22)

- Azure Resource Manager AzureMaps client library for Java. This package contains Microsoft Azure SDK for AzureMaps Management SDK. Azure Maps. Package tag package-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.LinkedResource` was added

* `models.ManagedServiceIdentity` was added

* `models.CorsRules` was added

* `models.SigningKey` was added

* `models.UserAssignedIdentity` was added

* `models.AccountSasParameters` was added

* `models.MapsAccountSasToken` was added

* `models.InfrastructureEncryption` was added

* `models.CustomerManagedKeyEncryptionKeyIdentity` was added

* `models.IdentityType` was added

* `models.CorsRule` was added

* `models.Encryption` was added

* `models.CustomerManagedKeyEncryption` was added

* `models.ManagedServiceIdentityType` was added

#### `models.Accounts` was modified

* `listSas(java.lang.String,java.lang.String,models.AccountSasParameters)` was added
* `listSasWithResponse(java.lang.String,java.lang.String,models.AccountSasParameters,com.azure.core.util.Context)` was added

#### `models.MapsAccount$Update` was modified

* `withEncryption(models.Encryption)` was added
* `withCors(models.CorsRules)` was added
* `withLinkedResources(java.util.List)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MapsAccountUpdateParameters` was modified

* `withEncryption(models.Encryption)` was added
* `identity()` was added
* `withCors(models.CorsRules)` was added
* `cors()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `encryption()` was added
* `withLinkedResources(java.util.List)` was added
* `linkedResources()` was added

#### `models.MetricSpecification` was modified

* `sourceMdmAccount()` was added
* `withLockAggregationType(java.lang.String)` was added
* `withSourceMdmAccount(java.lang.String)` was added
* `internalMetricName()` was added
* `withSupportedAggregationTypes(java.lang.String)` was added
* `withInternalMetricName(java.lang.String)` was added
* `supportedAggregationTypes()` was added
* `sourceMdmNamespace()` was added
* `withSourceMdmNamespace(java.lang.String)` was added
* `lockAggregationType()` was added

#### `models.MapsAccount` was modified

* `identity()` was added
* `listSasWithResponse(models.AccountSasParameters,com.azure.core.util.Context)` was added
* `listSas(models.AccountSasParameters)` was added

#### `models.Creator` was modified

* `systemData()` was added

#### `models.MapsAccount$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

## 1.0.0-beta.2 (2023-01-18)

- Azure Resource Manager AzureMaps client library for Java. This package contains Microsoft Azure SDK for AzureMaps Management SDK. Azure Maps. Package tag package-2021-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.MapsAccountProperties` was removed

* `models.CreatorProperties` was removed

#### `models.Creator$DefinitionStages` was modified

* `withProperties(models.CreatorProperties)` was removed in stage 3

#### `models.Accounts` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Creator$Definition` was modified

* `withProperties(models.CreatorProperties)` was removed

#### `models.MapsAccount` was modified

* `models.MapsAccountProperties properties()` -> `fluent.models.MapsAccountProperties properties()`

#### `models.Creator$Update` was modified

* `withStorageUnits(java.lang.Integer)` was removed

#### `models.Creator` was modified

* `models.CreatorProperties properties()` -> `fluent.models.CreatorProperties properties()`

#### `models.MapsAccount$Definition` was modified

* `withProperties(models.MapsAccountProperties)` was removed

### Features Added

#### `models.Accounts` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Creator$Definition` was modified

* `withProperties(fluent.models.CreatorProperties)` was added

#### `AzureMapsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.Maps` was modified

* `list(com.azure.core.util.Context)` was added
* `list()` was added

#### `AzureMapsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Dimension` was modified

* `internalName()` was added
* `withSourceMdmNamespace(java.lang.String)` was added
* `sourceMdmNamespace()` was added
* `withInternalMetricName(java.lang.String)` was added
* `withInternalName(java.lang.String)` was added
* `withToBeExportedToShoebox(java.lang.Boolean)` was added
* `toBeExportedToShoebox()` was added
* `internalMetricName()` was added

#### `models.MapsAccount` was modified

* `resourceGroupName()` was added

#### `models.Creator$Update` was modified

* `withStorageUnits(int)` was added

#### `models.Creator` was modified

* `resourceGroupName()` was added

#### `models.MapsAccount$Definition` was modified

* `withProperties(fluent.models.MapsAccountProperties)` was added

## 1.0.0-beta.1 (2021-05-17)

- Azure Resource Manager AzureMaps client library for Java. This package contains Microsoft Azure SDK for AzureMaps Management SDK. Azure Maps. Package tag package-2021-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

