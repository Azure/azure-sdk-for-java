# Release History

## 1.2.0 (2026-06-19)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package api-version 2026-04-16. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.FileSystemResourceListResult` was removed

#### `models.FileSystemResourceUpdate` was modified

* `validate()` was removed

#### `models.FileSystemResource` was modified

* `availabilityZone()` was removed
* `storageSku()` was removed
* `privateIPs()` was removed
* `provisioningState()` was removed
* `adminPassword()` was removed
* `userDetails()` was removed
* `marketplaceDetails()` was removed
* `clusterLoginUrl()` was removed
* `delegatedSubnetId()` was removed

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `validate()` was removed
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed
* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

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

#### `models.FileSystemResource$Definition` was modified

* `withAvailabilityZone(java.lang.String)` was removed
* `withAdminPassword(java.lang.String)` was removed
* `withStorageSku(java.lang.String)` was removed
* `withUserDetails(models.UserDetails)` was removed
* `withMarketplaceDetails(models.MarketplaceDetails)` was removed
* `withDelegatedSubnetId(java.lang.String)` was removed
* `withClusterLoginUrl(java.lang.String)` was removed
* `withPrivateIPs(java.util.List)` was removed

### Features Added

* `models.FileSystemResourceProperties` was added

#### `models.FileSystemResource` was modified

* `properties()` was added

#### `models.FileSystemResourceUpdateProperties` was modified

* `performanceTier()` was added
* `withPerformanceTier(java.lang.String)` was added

#### `models.FileSystemResource$Definition` was modified

* `withProperties(models.FileSystemResourceProperties)` was added

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
