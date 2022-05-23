# Release History

## 1.0.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.5 (2022-05-16)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2022-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.TieringPolicy` was removed

* `models.TieringMode` was removed

#### `models.SubProtectionPolicy` was modified

* `tieringPolicy()` was removed
* `withTieringPolicy(java.util.Map)` was removed

#### `models.AzureIaaSvmProtectionPolicy` was modified

* `tieringPolicy()` was removed
* `withTieringPolicy(java.util.Map)` was removed

#### `models.AzureVmWorkloadProtectedItemExtendedInfo` was modified

* `withNewestRecoveryPointInArchive(java.time.OffsetDateTime)` was removed
* `newestRecoveryPointInArchive()` was removed
* `withOldestRecoveryPointInVault(java.time.OffsetDateTime)` was removed
* `oldestRecoveryPointInVault()` was removed
* `oldestRecoveryPointInArchive()` was removed
* `withOldestRecoveryPointInArchive(java.time.OffsetDateTime)` was removed

#### `models.AzureIaaSvmProtectedItemExtendedInfo` was modified

* `withOldestRecoveryPointInVault(java.time.OffsetDateTime)` was removed
* `oldestRecoveryPointInVault()` was removed
* `withNewestRecoveryPointInArchive(java.time.OffsetDateTime)` was removed
* `newestRecoveryPointInArchive()` was removed
* `withOldestRecoveryPointInArchive(java.time.OffsetDateTime)` was removed
* `oldestRecoveryPointInArchive()` was removed

#### `models.ResourceGuardProxyOperations` was modified

* `put(java.lang.String,java.lang.String,java.lang.String)` was removed
* `putWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ResourceGuardProxyBaseResource$DefinitionStages` was added

* `models.ResourceGuardProxyBaseResource$Definition` was added

* `models.ResourceGuardProxyBaseResource$UpdateStages` was added

* `models.ResourceGuardProxyBaseResource$Update` was added

#### `models.ProtectedItemResource` was modified

* `resourceGroupName()` was added

#### `models.PrivateEndpointConnectionResource` was modified

* `resourceGroupName()` was added

#### `models.ResourceGuardProxyBaseResource` was modified

* `refresh(com.azure.core.util.Context)` was added
* `regionName()` was added
* `unlockDelete(models.UnlockDeleteRequest)` was added
* `refresh()` was added
* `update()` was added
* `resourceGroupName()` was added
* `region()` was added
* `unlockDeleteWithResponse(models.UnlockDeleteRequest,com.azure.core.util.Context)` was added

#### `models.ProtectionIntentResource` was modified

* `resourceGroupName()` was added

#### `models.ProtectionContainerResource` was modified

* `resourceGroupName()` was added

#### `models.ProtectionPolicyResource` was modified

* `resourceGroupName()` was added

#### `models.ResourceGuardProxyOperations` was modified

* `deleteById(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.4 (2022-04-29)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2021-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.TieringPolicy` was added

* `models.TieringMode` was added

#### `models.SubProtectionPolicy` was modified

* `withTieringPolicy(java.util.Map)` was added
* `tieringPolicy()` was added

#### `RecoveryServicesBackupManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.AzureIaaSvmProtectionPolicy` was modified

* `tieringPolicy()` was added
* `withTieringPolicy(java.util.Map)` was added

#### `RecoveryServicesBackupManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.AzureVmWorkloadProtectedItemExtendedInfo` was modified

* `newestRecoveryPointInArchive()` was added
* `oldestRecoveryPointInArchive()` was added
* `withOldestRecoveryPointInArchive(java.time.OffsetDateTime)` was added
* `oldestRecoveryPointInVault()` was added
* `withNewestRecoveryPointInArchive(java.time.OffsetDateTime)` was added
* `withOldestRecoveryPointInVault(java.time.OffsetDateTime)` was added

#### `models.AzureIaaSvmProtectedItemExtendedInfo` was modified

* `oldestRecoveryPointInArchive()` was added
* `withOldestRecoveryPointInVault(java.time.OffsetDateTime)` was added
* `oldestRecoveryPointInVault()` was added
* `newestRecoveryPointInArchive()` was added
* `withOldestRecoveryPointInArchive(java.time.OffsetDateTime)` was added
* `withNewestRecoveryPointInArchive(java.time.OffsetDateTime)` was added

## 1.0.0-beta.3 (2022-02-15)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2021-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OperationStatusValidateOperationExtendedInfo` was added

* `models.ValidateOperationStatuses` was added

* `models.AcquireStorageAccountLock` was added

* `models.ProtectionIntentItemType` was added

* `models.DedupState` was added

* `models.AzureWorkloadContainerAutoProtectionIntent` was added

* `models.XcoolState` was added

* `models.HourlySchedule` was added

* `models.ValidateOperationResults` was added

* `models.WeeklySchedule` was added

* `models.SimpleSchedulePolicyV2` was added

* `models.RecoveryPointTierInformationV2` was added

* `models.DailySchedule` was added

* `models.ValidateOperations` was added

* `models.IaasvmPolicyType` was added

* `models.AzureIaaSvmJobV2` was added

#### `models.BackupResourceVaultConfig` was modified

* `isSoftDeleteFeatureStateEditable()` was added
* `withIsSoftDeleteFeatureStateEditable(java.lang.Boolean)` was added

#### `models.ProtectionContainer` was modified

* `protectableObjectType()` was added
* `withProtectableObjectType(java.lang.String)` was added

#### `models.AzureIaaSvmJob` was modified

* `containerName()` was added
* `withIsUserTriggered(java.lang.Boolean)` was added
* `isUserTriggered()` was added
* `withContainerName(java.lang.String)` was added

#### `models.ExtendedProperties` was modified

* `linuxVmApplicationName()` was added
* `withLinuxVmApplicationName(java.lang.String)` was added

#### `models.AzureIaaSvmProtectionPolicy` was modified

* `policyType()` was added
* `withPolicyType(models.IaasvmPolicyType)` was added

#### `models.AzureStorageJob` was modified

* `isUserTriggered()` was added
* `withIsUserTriggered(java.lang.Boolean)` was added

#### `models.ProtectedItem` was modified

* `isArchiveEnabled()` was added
* `withIsArchiveEnabled(java.lang.Boolean)` was added
* `policyName()` was added
* `withPolicyName(java.lang.String)` was added

#### `models.IaaSvmProtectableItem` was modified

* `resourceGroup()` was added
* `withVirtualMachineVersion(java.lang.String)` was added
* `withResourceGroup(java.lang.String)` was added
* `virtualMachineVersion()` was added

#### `RecoveryServicesBackupManager` was modified

* `validateOperationResults()` was added
* `validateOperations()` was added
* `validateOperationStatuses()` was added

#### `models.AzureVmWorkloadProtectedItemExtendedInfo` was modified

* `recoveryModel()` was added
* `withRecoveryModel(java.lang.String)` was added

#### `models.SimpleSchedulePolicy` was modified

* `hourlySchedule()` was added
* `withHourlySchedule(models.HourlySchedule)` was added

#### `models.AzureStorageContainer` was modified

* `acquireStorageAccountLock()` was added
* `withAcquireStorageAccountLock(models.AcquireStorageAccountLock)` was added

#### `models.BackupResourceConfig` was modified

* `withDedupState(models.DedupState)` was added
* `xcoolState()` was added
* `dedupState()` was added
* `withXcoolState(models.XcoolState)` was added

## 1.0.0-beta.2 (2021-11-29)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2021-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2021-05-24)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2021-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
