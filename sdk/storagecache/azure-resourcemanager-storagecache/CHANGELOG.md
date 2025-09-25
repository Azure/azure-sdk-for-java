# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2025-09-25)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. Azure Managed Lustre provides a fully managed Lustre® file system, integrated with Blob storage, for use on demand. These operations create and manage Azure Managed Lustre file systems. Package tag package-2025-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AutoImportJobUpdatePropertiesAdminStatus` was added

* `models.AutoExportJob$Update` was added

* `models.AutoExportJobUpdate` was added

* `models.AutoImportJobPropertiesProvisioningState` was added

* `models.AutoImportJob$Definition` was added

* `models.AutoImportJobPropertiesAdminStatus` was added

* `models.AutoImportJobState` was added

* `models.AutoExportStatusType` was added

* `models.AutoExportJob$UpdateStages` was added

* `models.ImportJobAdminStatus` was added

* `models.AutoExportJob$Definition` was added

* `models.AutoImportJob$UpdateStages` was added

* `models.AutoExportJob` was added

* `models.AutoImportJob$DefinitionStages` was added

* `models.AutoExportJobAdminStatus` was added

* `models.AutoImportJobs` was added

* `models.AutoImportJobUpdate` was added

* `models.AutoExportJobProvisioningStateType` was added

* `models.AutoExportJob$DefinitionStages` was added

* `models.AutoImportJobPropertiesStatusBlobSyncEvents` was added

* `models.AutoExportJobs` was added

* `models.AutoImportJob$Update` was added

* `models.AutoImportJobsListResult` was added

* `models.AutoExportJobsListResult` was added

* `models.AutoImportJob` was added

#### `models.ImportJob$Update` was modified

* `withAdminStatus(models.ImportJobAdminStatus)` was added

#### `models.ImportJobUpdate` was modified

* `adminStatus()` was added
* `withAdminStatus(models.ImportJobAdminStatus)` was added

#### `models.ImportJob` was modified

* `importedDirectories()` was added
* `importedSymlinks()` was added
* `importedFiles()` was added
* `preexistingDirectories()` was added
* `preexistingFiles()` was added
* `adminStatus()` was added
* `preexistingSymlinks()` was added

#### `models.ImportJob$Definition` was modified

* `withAdminStatus(models.ImportJobAdminStatus)` was added

#### `StorageCacheManager` was modified

* `autoImportJobs()` was added
* `autoExportJobs()` was added

## 1.0.0 (2024-12-23)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. Azure Managed Lustre provides a fully managed Lustre® file system, integrated with Blob storage, for use on demand. These operations create and manage Azure Managed Lustre file systems. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release Azure Resource Manager StorageCache client library for Java.

## 1.0.0-beta.12 (2024-12-04)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. Azure Managed Lustre provides a fully managed Lustre® file system, integrated with Blob storage, for use on demand. These operations create and manage Azure Managed Lustre file systems. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.StorageTargetResource` was modified

* `type()` was added
* `name()` was added
* `id()` was added

## 1.0.0-beta.11 (2024-05-20)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. Azure Managed Lustre provides a fully managed Lustre® file system, integrated with Blob storage, for use on demand. These operations create and manage Azure Managed Lustre file systems. Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ErrorResponse` was removed

#### `models.AscOperation` was modified

* `models.ErrorResponse error()` -> `models.AscOperationErrorResponse error()`

### Features Added

* `models.ConflictResolutionMode` was added

* `models.ImportJobs` was added

* `models.ImportJob$Update` was added

* `models.ImportJobUpdate` was added

* `models.ImportJob$UpdateStages` was added

* `models.ImportJob` was added

* `models.ImportJobProvisioningStateType` was added

* `models.ImportJobsListResult` was added

* `models.ImportJob$Definition` was added

* `models.AscOperationErrorResponse` was added

* `models.ImportJob$DefinitionStages` was added

* `models.ImportStatusType` was added

#### `models.AmlFilesystemHsmSettings` was modified

* `withImportPrefixesInitial(java.util.List)` was added
* `importPrefixesInitial()` was added

#### `StorageCacheManager` was modified

* `importJobs()` was added

## 1.0.0-beta.10 (2024-02-27)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. Azure Managed Lustre provides a fully managed Lustre® file system, integrated with Blob storage, for use on demand. These operations create and manage Azure Managed Lustre file systems. Package tag package-preview-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AmlFilesystemSquashMode` was added

* `models.AmlFilesystemRootSquashSettings` was added

#### `models.AmlFilesystem$Definition` was modified

* `withRootSquashSettings(models.AmlFilesystemRootSquashSettings)` was added

#### `models.AmlFilesystemUpdate` was modified

* `rootSquashSettings()` was added
* `withRootSquashSettings(models.AmlFilesystemRootSquashSettings)` was added

#### `models.AmlFilesystem` was modified

* `rootSquashSettings()` was added

#### `models.AmlFilesystem$Update` was modified

* `withRootSquashSettings(models.AmlFilesystemRootSquashSettings)` was added

## 1.0.0-beta.9 (2023-06-20)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. Azure Managed Lustre provides a fully managed Lustre® file system, integrated with Blob storage, for use on demand. These operations create and manage Azure Managed Lustre file systems. Package tag package-2023-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Cache` was modified

* `scalingFactor()` was removed

#### `models.Cache$Definition` was modified

* `withScalingFactor(java.lang.Double)` was removed

#### `models.AmlFilesystem` was modified

* `mgsAddress()` was removed
* `lustreVersion()` was removed
* `mountCommand()` was removed

#### `models.Cache$Update` was modified

* `withScalingFactor(java.lang.Double)` was removed

### Features Added

* `models.AmlFilesystemContainerStorageInterface` was added

* `models.AmlFilesystemClientInfo` was added

#### `models.AmlFilesystem` was modified

* `clientInfo()` was added

## 1.0.0-beta.8 (2023-03-13)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. A Storage Cache provides scalable caching service for NAS clients, serving data from either NFSv3 or Blob at-rest storage (referred to as "Storage Targets"). These operations allow you to manage Caches. Package tag package-preview-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AmlFilesystemIdentity` was added

* `models.AmlFilesystemsListResult` was added

* `models.FilesystemSubnetStatusType` was added

* `models.AmlFilesystemIdentityType` was added

* `models.AmlFilesystemEncryptionSettings` was added

* `models.AmlFilesystemArchiveInfo` was added

* `models.AmlFilesystem$Definition` was added

* `models.AmlFilesystem$DefinitionStages` was added

* `models.AmlFilesystemArchiveStatus` was added

* `models.AmlFilesystemProvisioningStateType` was added

* `models.RequiredAmlFilesystemSubnetsSize` was added

* `models.ResourceProviders` was added

* `models.AmlFilesystemUpdatePropertiesMaintenanceWindow` was added

* `models.AmlFilesystems` was added

* `models.MaintenanceDayOfWeekType` was added

* `models.AmlFilesystemUpdate` was added

* `models.AmlFilesystem$UpdateStages` was added

* `models.ArchiveStatusType` was added

* `models.AmlFilesystemCheckSubnetErrorFilesystemSubnet` was added

* `models.AmlFilesystemArchive` was added

* `models.AmlFilesystemHealth` was added

* `models.RequiredAmlFilesystemSubnetsSizeInfo` was added

* `models.AmlFilesystemHealthStateType` was added

* `models.AmlFilesystemCheckSubnetError` was added

* `models.AmlFilesystemCheckSubnetErrorException` was added

* `models.SkuName` was added

* `models.AmlFilesystem` was added

* `models.AmlFilesystemSubnetInfo` was added

* `models.AmlFilesystemHsmSettings` was added

* `models.AmlFilesystemPropertiesHsm` was added

* `models.AmlFilesystem$Update` was added

* `models.AmlFilesystemPropertiesMaintenanceWindow` was added

* `models.UserAssignedIdentitiesValueAutoGenerated` was added

#### `models.Cache` was modified

* `scalingFactor()` was added

#### `models.Cache$Definition` was modified

* `withScalingFactor(java.lang.Double)` was added

#### `StorageCacheManager` was modified

* `resourceProviders()` was added
* `amlFilesystems()` was added

#### `models.Cache$Update` was modified

* `withScalingFactor(java.lang.Double)` was added

## 1.0.0-beta.7 (2023-02-22)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. A Storage Cache provides scalable caching service for NAS clients, serving data from either NFSv3 or Blob at-rest storage (referred to as "Storage Targets"). These operations allow you to manage Caches. Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Caches` was modified

* `startPrimingJob(java.lang.String,java.lang.String,models.PrimingJob)` was removed
* `spaceAllocation(java.lang.String,java.lang.String,java.util.List)` was removed
* `stopPrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter)` was removed
* `pausePrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter)` was removed
* `resumePrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter)` was removed

#### `models.StorageTargets` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.Cache` was modified

* `startPrimingJob(models.PrimingJob)` was removed
* `stopPrimingJob(models.PrimingJobIdParameter)` was removed
* `resumePrimingJob(models.PrimingJobIdParameter)` was removed
* `pausePrimingJob(models.PrimingJobIdParameter)` was removed

### Features Added

#### `models.Nfs3Target` was modified

* `withWriteBackTimer(java.lang.Integer)` was added
* `withVerificationTimer(java.lang.Integer)` was added
* `verificationTimer()` was added
* `writeBackTimer()` was added

#### `models.StorageTargets` was modified

* `restoreDefaults(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `restoreDefaults(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.BlobNfsTarget` was modified

* `writeBackTimer()` was added
* `withWriteBackTimer(java.lang.Integer)` was added
* `verificationTimer()` was added
* `withVerificationTimer(java.lang.Integer)` was added

## 1.0.0-beta.6 (2022-07-08)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. A Storage Cache provides scalable caching service for NAS clients, serving data from either NFSv3 or Blob at-rest storage (referred to as "Storage Targets"). These operations allow you to manage Caches. Package tag package-2022-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.StorageTargetSpaceAllocation` was added

* `models.PrimingJobIdParameter` was added

* `models.CacheUpgradeSettings` was added

* `models.PrimingJobState` was added

* `models.LogSpecification` was added

* `models.PrimingJob` was added

#### `models.StorageTarget` was modified

* `allocationPercentage()` was added
* `resourceGroupName()` was added

#### `StorageCacheManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Caches` was modified

* `startPrimingJob(java.lang.String,java.lang.String,models.PrimingJob,com.azure.core.util.Context)` was added
* `resumePrimingJob(java.lang.String,java.lang.String)` was added
* `pausePrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter)` was added
* `resumePrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter)` was added
* `spaceAllocation(java.lang.String,java.lang.String,java.util.List)` was added
* `pausePrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter,com.azure.core.util.Context)` was added
* `stopPrimingJob(java.lang.String,java.lang.String)` was added
* `stopPrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter,com.azure.core.util.Context)` was added
* `spaceAllocation(java.lang.String,java.lang.String,java.util.List,com.azure.core.util.Context)` was added
* `resumePrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter,com.azure.core.util.Context)` was added
* `pausePrimingJob(java.lang.String,java.lang.String)` was added
* `startPrimingJob(java.lang.String,java.lang.String)` was added
* `startPrimingJob(java.lang.String,java.lang.String,models.PrimingJob)` was added
* `stopPrimingJob(java.lang.String,java.lang.String,models.PrimingJobIdParameter)` was added
* `spaceAllocation(java.lang.String,java.lang.String)` was added

#### `models.ApiOperationPropertiesServiceSpecification` was modified

* `withLogSpecifications(java.util.List)` was added
* `logSpecifications()` was added

#### `models.Cache` was modified

* `spaceAllocation()` was added
* `startPrimingJob(models.PrimingJob)` was added
* `upgradeSettings()` was added
* `resumePrimingJob()` was added
* `resourceGroupName()` was added
* `primingJobs()` was added
* `pausePrimingJob(models.PrimingJobIdParameter)` was added
* `pausePrimingJob()` was added
* `stopPrimingJob(models.PrimingJobIdParameter)` was added
* `stopPrimingJob()` was added
* `stopPrimingJob(models.PrimingJobIdParameter,com.azure.core.util.Context)` was added
* `pausePrimingJob(models.PrimingJobIdParameter,com.azure.core.util.Context)` was added
* `startPrimingJob(models.PrimingJob,com.azure.core.util.Context)` was added
* `startPrimingJob()` was added
* `resumePrimingJob(models.PrimingJobIdParameter)` was added
* `resumePrimingJob(models.PrimingJobIdParameter,com.azure.core.util.Context)` was added

#### `models.Cache$Definition` was modified

* `withUpgradeSettings(models.CacheUpgradeSettings)` was added

#### `StorageCacheManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Cache$Update` was modified

* `withUpgradeSettings(models.CacheUpgradeSettings)` was added

## 1.0.0-beta.5 (2022-03-22)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. A Storage Cache provides scalable caching service for NAS clients, serving data from either NFSv3 or Blob at-rest storage (referred to as "Storage Targets"). These operations allow you to manage Caches. Package tag package-2022-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ResourceUsage` was added

* `models.ResourceUsagesListResult` was added

* `models.ResourceUsageName` was added

* `models.AscUsages` was added

#### `models.StorageTargetOperations` was modified

* `invalidate(java.lang.String,java.lang.String,java.lang.String)` was added
* `invalidate(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Cache` was modified

* `zones()` was added

#### `models.Cache$Definition` was modified

* `withZones(java.util.List)` was added

#### `StorageCacheManager` was modified

* `ascUsages()` was added

## 1.0.0-beta.4 (2021-10-08)

- Azure Resource Manager StorageCache client library for Java. This package contains Microsoft Azure SDK for StorageCache Management SDK. A Storage Cache provides scalable caching service for NAS clients, serving data from either NFSv3 or Blob at-rest storage (referred to as "Storage Targets"). These operations allow you to manage Caches. Package tag package-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OperationalStateType` was added

#### `models.StorageTarget` was modified

* `state()` was added

#### `models.StorageTarget$Update` was modified

* `withState(models.OperationalStateType)` was added

#### `models.StorageTarget$Definition` was modified

* `withState(models.OperationalStateType)` was added

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
