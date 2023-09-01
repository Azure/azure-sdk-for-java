# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

