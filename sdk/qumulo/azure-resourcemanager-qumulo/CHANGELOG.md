# Release History

## 2.0.0 (2024-07-23)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package tag package-2024-06-19. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.StorageSku` was removed

#### `models.ProvisioningState` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.ProvisioningState[] values()` -> `java.util.Collection values()`

#### `models.FileSystemResource` was modified

* `models.StorageSku storageSku()` -> `java.lang.String storageSku()`
* `initialCapacity()` was removed

#### `models.MarketplaceSubscriptionStatus` was modified

* `toString()` was removed
* `models.MarketplaceSubscriptionStatus[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.FileSystemResourceUpdateProperties` was modified

* `withPrivateIPs(java.util.List)` was removed
* `withClusterLoginUrl(java.lang.String)` was removed
* `clusterLoginUrl()` was removed
* `privateIPs()` was removed

#### `models.FileSystemResource$Definition` was modified

* `withInitialCapacity(int)` was removed
* `withStorageSku(models.StorageSku)` was removed

### Features Added

#### `models.FileSystemResourceUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedServiceIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileSystemResourceListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileSystemResourceUpdateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MarketplaceDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withTermUnit(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `termUnit()` was added

#### `models.FileSystemResource$Definition` was modified

* `withStorageSku(java.lang.String)` was added

## 1.0.0 (2023-05-25)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.2 (2023-05-22)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-04-18)

- Azure Resource Manager Qumulo client library for Java. This package contains Microsoft Azure SDK for Qumulo Management SDK.  Package tag package-2022-10-12-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
