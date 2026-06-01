# Release History

## 1.0.0-beta.4 (2026-06-01)

- Azure Resource Manager Azure Quantum client library for Java. This package contains Microsoft Azure SDK for Azure Quantum Management SDK. Microsoft.Quantum Resource Provider Management API. Package api-version 2025-12-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OfferingsListResult` was removed

#### `models.QuantumWorkspaceIdentity` was removed

#### `models.WorkspaceListResult` was removed

#### `models.TagsObject` was removed

#### `models.ResourceIdentityType` was removed

#### `models.OperationsList` was removed

#### `models.CheckNameAvailabilityParameters` was modified

* `validate()` was removed

#### `models.ProviderPropertiesAad` was modified

* `ProviderPropertiesAad()` was changed to private access
* `validate()` was removed

#### `models.ApiKeys` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `validate()` was removed

#### `models.PricingDetail` was modified

* `PricingDetail()` was changed to private access
* `validate()` was removed
* `withId(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed

#### `models.TargetDescription` was modified

* `TargetDescription()` was changed to private access
* `withAcceptedDataFormats(java.util.List)` was removed
* `validate()` was removed
* `withDescription(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withAcceptedContentEncodings(java.util.List)` was removed

#### `models.PricingDimension` was modified

* `PricingDimension()` was changed to private access
* `withId(java.lang.String)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed

#### `models.ProviderProperties` was modified

* `ProviderProperties()` was changed to private access
* `withQuotaDimensions(java.util.List)` was removed
* `validate()` was removed
* `withManagedApplication(models.ProviderPropertiesManagedApplication)` was removed
* `withSkus(java.util.List)` was removed
* `withAad(models.ProviderPropertiesAad)` was removed
* `withPricingDimensions(java.util.List)` was removed
* `withTargets(java.util.List)` was removed

#### `models.ApiKey` was modified

* `ApiKey()` was changed to private access
* `withCreatedAt(java.time.OffsetDateTime)` was removed
* `validate()` was removed

#### `models.QuantumWorkspace$Definition` was modified

* `withIdentity(models.QuantumWorkspaceIdentity)` was removed

#### `models.ProviderPropertiesManagedApplication` was modified

* `ProviderPropertiesManagedApplication()` was changed to private access
* `validate()` was removed

#### `models.Offerings` was modified

* `list(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Provider` was modified

* `validate()` was removed

#### `models.WorkspaceResourceProperties` was modified

* `validate()` was removed

#### `models.SkuDescription` was modified

* `SkuDescription()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed
* `withTargets(java.util.List)` was removed
* `withId(java.lang.String)` was removed
* `withAutoAdd(java.lang.Boolean)` was removed
* `withQuotaDimensions(java.util.List)` was removed
* `withRestrictedAccessUri(java.lang.String)` was removed
* `withVersion(java.lang.String)` was removed
* `withPricingDetails(java.util.List)` was removed

#### `models.QuantumWorkspace` was modified

* `models.QuantumWorkspaceIdentity identity()` -> `models.ManagedServiceIdentity identity()`

#### `models.QuotaDimension` was modified

* `QuotaDimension()` was changed to private access
* `validate()` was removed
* `withPeriod(java.lang.String)` was removed
* `withQuota(java.lang.Float)` was removed
* `withScope(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withUnit(java.lang.String)` was removed
* `withUnitPlural(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.CheckNameAvailabilityResult` was modified

* `java.lang.String reason()` -> `models.CheckNameAvailabilityReason reason()`

### Features Added

* `models.Origin` was added

* `models.ActionType` was added

* `models.QuantumSuiteOfferProperties` was added

* `models.ManagedOnBehalfOfConfiguration` was added

* `models.SuiteOffers` was added

* `models.ManagedServiceIdentity` was added

* `models.MoboBrokerResource` was added

* `models.QuotaAllocations` was added

* `models.QuantumWorkspaceTagsUpdate` was added

* `models.WorkspaceKind` was added

* `models.UserAssignedIdentity` was added

* `models.ManagedServiceIdentityType` was added

* `models.CheckNameAvailabilityReason` was added

* `models.QuantumSuiteOffer` was added

#### `models.ProvisioningStatus` was modified

* `CANCELED` was added

#### `models.TargetDescription` was modified

* `metadata()` was added
* `numQubits()` was added
* `targetProfile()` was added

#### `AzureQuantumManager` was modified

* `suiteOffers()` was added

#### `models.QuantumWorkspace$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Offerings` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Provider` was modified

* `withQuotas(models.QuotaAllocations)` was added
* `quotas()` was added

#### `models.WorkspaceResourceProperties` was modified

* `workspaceKind()` was added
* `managedOnBehalfOfConfiguration()` was added
* `withWorkspaceKind(models.WorkspaceKind)` was added
* `managedStorageAccount()` was added

#### `models.Operation` was modified

* `origin()` was added
* `actionType()` was added

## 1.0.0-beta.3 (2024-12-03)

- Azure Resource Manager AzureQuantum client library for Java. This package contains Microsoft Azure SDK for AzureQuantum Management SDK.  Package tag package-2023-11-13-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.QuantumWorkspace$Definition` was modified

* `withStorageAccount(java.lang.String)` was removed
* `withProviders(java.util.List)` was removed
* `withApiKeyEnabled(java.lang.Boolean)` was removed

#### `models.QuantumWorkspace` was modified

* `apiKeyEnabled()` was removed
* `storageAccount()` was removed
* `provisioningState()` was removed
* `providers()` was removed
* `usable()` was removed
* `endpointUri()` was removed

### Features Added

* `models.WorkspaceResourceProperties` was added

#### `models.QuantumWorkspace$Definition` was modified

* `withProperties(models.WorkspaceResourceProperties)` was added

#### `models.QuantumWorkspace` was modified

* `properties()` was added

## 1.0.0-beta.2 (2024-03-15)

- Azure Resource Manager AzureQuantum client library for Java. This package contains Microsoft Azure SDK for AzureQuantum Management SDK.  Package tag package-2023-11-13-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ApiKey` was added

* `models.ApiKeys` was added

* `models.ListKeysResult` was added

* `models.KeyType` was added

#### `models.QuantumWorkspace$Definition` was modified

* `withApiKeyEnabled(java.lang.Boolean)` was added

#### `models.WorkspaceOperations` was modified

* `regenerateKeysWithResponse(java.lang.String,java.lang.String,models.ApiKeys,com.azure.core.util.Context)` was added
* `listKeys(java.lang.String,java.lang.String)` was added
* `listKeysWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `regenerateKeys(java.lang.String,java.lang.String,models.ApiKeys)` was added

#### `models.QuantumWorkspace` was modified

* `apiKeyEnabled()` was added

## 1.0.0-beta.1 (2023-07-21)

- Azure Resource Manager AzureQuantum client library for Java. This package contains Microsoft Azure SDK for AzureQuantum Management SDK.  Package tag package-2022-01-10-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
