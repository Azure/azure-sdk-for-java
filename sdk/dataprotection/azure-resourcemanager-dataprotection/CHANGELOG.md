# Release History

## 1.0.0-beta.2 (2023-01-18)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-preview-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.BackupInstancesValidateForRestoreResponse` was removed

* `models.BackupInstancesValidateForBackupHeaders` was removed

* `models.ExportJobsTriggerResponse` was removed

* `models.ExportJobsTriggerHeaders` was removed

* `models.BackupInstancesValidateForRestoreHeaders` was removed

* `models.BackupInstancesValidateForBackupResponse` was removed

* `models.BackupInstancesTriggerRestoreResponse` was removed

* `models.BackupInstancesAdhocBackupHeaders` was removed

* `models.BackupInstancesTriggerRehydrateResponse` was removed

* `models.BackupInstancesAdhocBackupResponse` was removed

* `models.BackupInstancesTriggerRehydrateHeaders` was removed

* `models.BackupInstancesDeleteResponse` was removed

* `models.BackupInstancesDeleteHeaders` was removed

* `models.BackupInstancesTriggerRestoreHeaders` was removed

#### `models.ResourceGuards` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BackupVaults` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ResourceGuard` was modified

* `models.ProvisioningState provisioningState()` -> `models.ResourceGuardProvisioningState provisioningState()`

### Features Added

* `models.SyncType` was added

* `models.SoftDeleteSettings` was added

* `models.ExistingResourcePolicy` was added

* `models.AlertsState` was added

* `models.ResourceGuardProxyBaseResource$DefinitionStages` was added

* `models.PatchBackupVaultInput` was added

* `models.DppResourceGuardProxies` was added

* `models.DeletedBackupInstance` was added

* `models.SecuritySettings` was added

* `models.OperationStatusBackupVaultContexts` was added

* `models.DeletedBackupInstances` was added

* `models.KubernetesClusterBackupDatasourceParameters` was added

* `models.UnlockDeleteRequest` was added

* `models.ResourceGuardOperationDetail` was added

* `models.DeletedBackupInstanceResourceList` was added

* `models.DeletionInfo` was added

* `models.ResourceGuardProvisioningState` was added

* `models.BackupInstancesExtensionRoutings` was added

* `models.ItemPathBasedRestoreCriteria` was added

* `models.MonitoringSettings` was added

* `models.ValidationType` was added

* `models.SyncBackupInstanceRequest` was added

* `models.UnlockDeleteResponse` was added

* `models.BackupDatasourceParameters` was added

* `models.PersistentVolumeRestoreMode` was added

* `models.ResourceGuardProxyBaseResource$Update` was added

* `models.AzureMonitorAlertSettings` was added

* `models.KubernetesStorageClassRestoreCriteria` was added

* `models.ResourceGuardProxyBaseResource$Definition` was added

* `models.SoftDeleteState` was added

* `models.BlobBackupDatasourceParameters` was added

* `models.DeletedBackupInstanceResource` was added

* `models.KubernetesPVRestoreCriteria` was added

* `models.OperationStatusResourceGroupContexts` was added

* `models.ImmutabilitySettings` was added

* `models.KubernetesClusterRestoreCriteria` was added

* `models.ResourceGuardProxyBase` was added

* `models.ResourceGuardProxyBaseResourceList` was added

* `models.ResourceGuardProxyBaseResource$UpdateStages` was added

* `models.ResourceGuardProxyBaseResource` was added

* `models.ImmutabilityState` was added

* `models.DppProxyResource` was added

#### `models.AzureBackupRecoveryTimeBasedRestoreRequest` was modified

* `withSourceResourceId(java.lang.String)` was added

#### `models.ResourceGuards` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ResourceGuardResource$Update` was modified

* `withProperties(models.PatchBackupVaultInput)` was added

#### `models.BackupVaultResource$Update` was modified

* `withProperties(models.PatchBackupVaultInput)` was added

#### `models.BackupInstanceResource` was modified

* `resumeProtection(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `resumeBackups()` was added
* `suspendBackups()` was added
* `stopProtection()` was added
* `resumeProtection()` was added
* `resumeBackups(com.azure.core.util.Context)` was added
* `syncBackupInstance(models.SyncBackupInstanceRequest)` was added
* `stopProtection(com.azure.core.util.Context)` was added
* `suspendBackups(com.azure.core.util.Context)` was added
* `syncBackupInstance(models.SyncBackupInstanceRequest,com.azure.core.util.Context)` was added
* `tags()` was added

#### `models.PolicyParameters` was modified

* `withBackupDatasourceParametersList(java.util.List)` was added
* `backupDatasourceParametersList()` was added

#### `models.SecretStoreResource` was modified

* `value()` was added
* `withValue(java.lang.String)` was added

#### `models.AzureBackupRestoreRequest` was modified

* `withSourceResourceId(java.lang.String)` was added
* `sourceResourceId()` was added

#### `DataProtectionManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `deletedBackupInstances()` was added
* `operationStatusResourceGroupContexts()` was added
* `dppResourceGuardProxies()` was added
* `backupInstancesExtensionRoutings()` was added
* `operationStatusBackupVaultContexts()` was added

#### `models.BackupVaultResource` was modified

* `resourceGroupName()` was added

#### `models.AzureBackupRestoreWithRehydrationRequest` was modified

* `withSourceResourceId(java.lang.String)` was added

#### `models.BackupInstanceResource$Definition` was modified

* `withTags(java.util.Map)` was added

#### `models.BackupInstance` was modified

* `validationType()` was added
* `withValidationType(models.ValidationType)` was added

#### `models.BackupVaults` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BackupVault` was modified

* `withSecuritySettings(models.SecuritySettings)` was added
* `isVaultProtectedByResourceGuard()` was added
* `monitoringSettings()` was added
* `securitySettings()` was added
* `withMonitoringSettings(models.MonitoringSettings)` was added

#### `models.BaseBackupPolicyResource` was modified

* `resourceGroupName()` was added

#### `models.PatchResourceRequestInput` was modified

* `withProperties(models.PatchBackupVaultInput)` was added
* `properties()` was added

#### `DataProtectionManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.BackupInstances` was modified

* `resumeProtection(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `suspendBackups(java.lang.String,java.lang.String,java.lang.String)` was added
* `stopProtection(java.lang.String,java.lang.String,java.lang.String)` was added
* `resumeProtection(java.lang.String,java.lang.String,java.lang.String)` was added
* `resumeBackups(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `suspendBackups(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getBackupInstanceOperationResult(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `stopProtection(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `syncBackupInstance(java.lang.String,java.lang.String,java.lang.String,models.SyncBackupInstanceRequest,com.azure.core.util.Context)` was added
* `resumeBackups(java.lang.String,java.lang.String,java.lang.String)` was added
* `syncBackupInstance(java.lang.String,java.lang.String,java.lang.String,models.SyncBackupInstanceRequest)` was added
* `getBackupInstanceOperationResultWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ResourceGuard` was modified

* `withVaultCriticalOperationExclusionList(java.util.List)` was added

#### `models.AzureBackupRecoveryPointBasedRestoreRequest` was modified

* `withSourceResourceId(java.lang.String)` was added

#### `models.BackupInstanceResource$Update` was modified

* `withTags(java.util.Map)` was added

#### `models.ResourceGuardResource` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-11-10)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2021-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

