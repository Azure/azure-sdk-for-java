# Release History

## 1.0.0-beta.4 (Unreleased)


## 1.0.0-beta.3 (2021-08-03)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. A Storage Cache provides scalable caching service for NAS clients, serving data from either NFSv3 or Blob at-rest storage (referred to as "Storage Targets"). These operations allow you to manage Caches. Package tag package-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SystemData` was removed

* `models.CreatedByType` was removed

#### `models.StorageTarget` was modified

* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.StorageTarget$Update` was modified

* `withProvisioningState(models.ProvisioningStateType)` was removed
* `withTargetType(models.StorageTargetType)` was removed

#### `models.StorageTargets` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Cache` was modified

* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.StorageTargetResource` was modified

* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.StorageTarget$Definition` was modified

* `withProvisioningState(models.ProvisioningStateType)` was removed

#### `models.Cache$Definition` was modified

* `withUpgradeStatus(models.CacheUpgradeStatus)` was removed
* `withProvisioningState(models.ProvisioningStateType)` was removed

#### `models.Cache$Update` was modified

* `withSubnet(java.lang.String)` was removed
* `withCacheSizeGB(java.lang.Integer)` was removed
* `withProvisioningState(models.ProvisioningStateType)` was removed
* `withSku(models.CacheSku)` was removed
* `withUpgradeStatus(models.CacheUpgradeStatus)` was removed

### Features Added

* `models.UserAssignedIdentitiesValue` was added

* `models.StorageTargetOperations` was added

#### `StorageCacheManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.CacheIdentity` was modified

* `userAssignedIdentities()` was added
* `withUserAssignedIdentities(java.util.Map)` was added

#### `models.CacheEncryptionSettings` was modified

* `rotationToLatestKeyVersionEnabled()` was added
* `withRotationToLatestKeyVersionEnabled(java.lang.Boolean)` was added

#### `models.StorageTargets` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `StorageCacheManager` was modified

* `storageTargetOperations()` was added

## 1.0.0-beta.2 (2021-03-08)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. A Storage Cache provides scalable caching service for NAS clients, serving data from either NFSv3 or Blob at-rest storage (referred to as "Storage Targets"). These operations allow you to manage Caches. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ClfsTargetProperties` was removed

* `models.Nfs3TargetProperties` was removed

* `models.StorageTargetProperties` was removed

* `models.UnknownTargetProperties` was removed

#### `models.UnknownTarget` was modified

* `withUnknownMap(java.util.Map)` was removed
* `unknownMap()` was removed

### New Feature

* `models.Condition` was added

* `models.BlobNfsTarget` was added

#### `models.StorageTarget` was modified

* `blobNfs()` was added
* `targetType()` was added
* `dnsRefresh(com.azure.core.util.Context)` was added
* `dnsRefresh()` was added

#### `models.CacheHealth` was modified

* `conditions()` was added

#### `models.StorageTarget$Update` was modified

* `withTargetType(models.StorageTargetType)` was added
* `withBlobNfs(models.BlobNfsTarget)` was added

#### `models.CacheNetworkSettings` was modified

* `dnsSearchDomain()` was added
* `withDnsSearchDomain(java.lang.String)` was added
* `dnsServers()` was added
* `ntpServer()` was added
* `withDnsServers(java.util.List)` was added
* `withNtpServer(java.lang.String)` was added

#### `models.UnknownTarget` was modified

* `withAttributes(java.util.Map)` was added
* `attributes()` was added

#### `models.StorageTargets` was modified

* `dnsRefresh(java.lang.String,java.lang.String,java.lang.String)` was added
* `dnsRefresh(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.StorageTarget$Definition` was modified

* `withTargetType(models.StorageTargetType)` was added
* `withBlobNfs(models.BlobNfsTarget)` was added

## 1.0.0-beta.1 (2021-02-22)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. A Storage Cache provides scalable caching service for NAS clients, serving data from either NFSv3 or Blob at-rest storage (referred to as "Storage Targets"). These operations allow you to manage Caches. Package tag package-2020-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
