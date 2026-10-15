# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0 (2026-06-22)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package api-version 2026-04-16. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.FileSystemResourceListResult` was removed

#### `models.FileSystemResourceUpdate` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`
* `validate()` was removed
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `validate()` was removed

#### `models.FileSystemResourceUpdateProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.UserDetails` was modified

* `validate()` was removed

#### `models.MarketplaceDetails` was modified

* `validate()` was removed

#### `QumuloManager` was modified

* `fluent.QumuloStorage serviceClient()` -> `fluent.QumuloManagementClient serviceClient()`

### Features Added

#### `models.FileSystemResource` was modified

* `performanceTier()` was added

#### `models.FileSystemResourceUpdateProperties` was modified

* `withPerformanceTier(java.lang.String)` was added
* `performanceTier()` was added

#### `models.FileSystemResource$Definition` was modified

* `withPerformanceTier(java.lang.String)` was added

## 1.1.0 (2024-09-10)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package tag package-2024-06-19. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.StorageSku` was removed

#### `models.ProvisioningState` was modified

* `valueOf(java.lang.String)` was removed
* `models.ProvisioningState[] values()` -> `java.util.Collection values()`
* `toString()` was removed

#### `models.FileSystemResource` was modified

* `models.StorageSku storageSku()` -> `java.lang.String storageSku()`
* `initialCapacity()` was removed

#### `models.MarketplaceSubscriptionStatus` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.MarketplaceSubscriptionStatus[] values()` -> `java.util.Collection values()`

#### `models.FileSystemResourceUpdateProperties` was modified

* `clusterLoginUrl()` was removed
* `withClusterLoginUrl(java.lang.String)` was removed
* `privateIPs()` was removed
* `withPrivateIPs(java.util.List)` was removed

#### `models.FileSystemResource$Definition` was modified

* `withStorageSku(models.StorageSku)` was removed
* `withInitialCapacity(int)` was removed

### Features Added

#### `models.FileSystemResourceUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedServiceIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileSystemResourceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileSystemResourceUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MarketplaceDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withTermUnit(java.lang.String)` was added
* `termUnit()` was added

#### `models.FileSystemResource$Definition` was modified

* `withStorageSku(java.lang.String)` was added

## 1.0.0 (2023-05-25)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.2 (2023-05-22)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-04-18)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package tag package-2022-10-12-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
