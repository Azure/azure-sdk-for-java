# Release History

## 1.1.0-beta.1 (2026-01-31)

### Breaking Changes

#### `models.PrivateEndpointConnection` was modified

* `validate()` was removed

#### `models.DeidUpdate` was modified

* `validate()` was removed

#### `models.DeidServiceProperties` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `HealthDataAIServicesManager` was modified

* `fluent.HealthDataAIServicesClient serviceClient()` -> `fluent.HealthDataAIServicesManagementClient serviceClient()`

#### `models.DeidPropertiesUpdate` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentityUpdate` was modified

* `validate()` was removed

#### `models.PrivateEndpointConnectionProperties` was modified

* `validate()` was removed

#### `models.PrivateLinkResourceProperties` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `validate()` was removed

### Features Added

* `models.DeidServiceSku` was added

* `models.SkuTier` was added

* `models.DeidServiceSkuName` was added

#### `models.DeidService$Definition` was modified

* `withSku(models.DeidServiceSku)` was added

#### `models.DeidUpdate` was modified

* `sku()` was added
* `withSku(models.DeidServiceSku)` was added

#### `models.DeidService$Update` was modified

* `withSku(models.DeidServiceSku)` was added

#### `models.DeidService` was modified

* `sku()` was added


## 1.0.0 (2024-11-21)

- Azure Resource Manager Health Data AI Services client library for Java. This package contains Microsoft Azure SDK for Health Data AI Services Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- The first stable release for the azure-resourcemanager-healthdataaiservices Java SDK.

## 1.0.0-beta.1 (2024-08-14)

- Azure Resource Manager Health Data AI Services client library for Java. This package contains Microsoft Azure SDK for Health Data AI Services Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-healthdataaiservices Java SDK.
