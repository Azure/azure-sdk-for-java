# Release History

## 1.3.0-beta.1 (2024-07-17)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BackupInstanceResource` was modified

* `stopProtection(com.azure.core.util.Context)` was removed
* `suspendBackups(com.azure.core.util.Context)` was removed

#### `models.BackupInstances` was modified

* `suspendBackups(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `stopProtection(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.IdentityType` was added

* `models.EncryptionSettings` was added

* `models.InfrastructureEncryptionState` was added

* `models.BcdrSecurityLevel` was added

* `models.BackupInstancesExtensionRoutings` was added

* `models.StopProtectionRequest` was added

* `models.CmkKeyVaultProperties` was added

* `models.CmkKekIdentity` was added

* `models.EncryptionState` was added

* `models.SuspendBackupRequest` was added

#### `models.AdhocBasedTriggerContext` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AdhocBasedTaggingCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBackupRecoveryTimeBasedRestoreRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withResourceGuardOperationRequests(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.ResourceMoveDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CrossRegionRestoreJobRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceLifeCycle` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DppWorkerRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TaggingCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobExtendedInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SoftDeleteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DppTrackedResource` was modified

* `id()` was added
* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.BackupInstanceResource` was modified

* `suspendBackups(models.SuspendBackupRequest,com.azure.core.util.Context)` was added
* `stopProtection(models.StopProtectionRequest,com.azure.core.util.Context)` was added

#### `models.AdhocBackupTriggerOption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SupportedFeature` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PolicyParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataStoreParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.RestorableTimeRange` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBackupParams` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.ClientDiscoveryDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DppResource` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added

#### `models.StorageSetting` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DppResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ProtectionStatusDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackupInstanceResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RangeBasedItemLevelRestoreCriteria` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.SecretStoreResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupRestoreRequest` was modified

* `resourceGuardOperationRequests()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added

#### `models.BackupSchedule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `DataProtectionManager` was modified

* `backupInstancesExtensionRoutings()` was added

#### `models.PatchBackupVaultInput` was modified

* `resourceGuardOperationRequests()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withResourceGuardOperationRequests(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeletedBackupInstance` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `currentProtectionState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `protectionErrorDetails()` was added
* `provisioningState()` was added
* `protectionStatus()` was added

#### `models.CrossSubscriptionRestoreSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AbsoluteDeleteOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.SecuritySettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withEncryptionSettings(models.EncryptionSettings)` was added
* `encryptionSettings()` was added

#### `models.DefaultResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.AzureBackupRestoreWithRehydrationRequest` was modified

* `objectType()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RestoreJobRecoveryPointDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BaseResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.AzureBackupFindRestorableTimeRangesRequestResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NamespacedNameResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerContext` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BasePolicyRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.AzureBackupJobResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KubernetesClusterBackupDatasourceParameters` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UnlockDeleteRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceGuardOperationDetail` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserFacingError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatasourceSet` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RestoreFilesTargetInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.RestoreTargetInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeleteOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackupCriteria` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeletedBackupInstanceResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserFacingWarningDetail` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeletionInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackupInstance` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `resourceGuardOperationRequests()` was added

#### `models.ValidateCrossRegionRestoreRequestObject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClientDiscoveryForProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomCopyOption` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TargetDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ItemLevelRestoreCriteria` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ItemPathBasedRestoreCriteria` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RestoreTargetInfoBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.AzureOperationalStoreParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.BaseBackupPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.InnerError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureRetentionRule` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MonitoringSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FeatureValidationRequestBase` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FeatureSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AuthCredentials` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupVault` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `bcdrSecurityLevel()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `resourceGuardOperationRequests()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added

#### `models.Day` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceGuardOperation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClientDiscoveryResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SyncBackupInstanceRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationJobExtendedInfo` was modified

* `objectType()` was added

#### `models.CopyOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackupDatasourceParameters` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleBasedBackupCriteria` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CrossRegionRestoreSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CrossRegionRestoreJobsRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupFindRestorableTimeRangesResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMonitorAlertSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IdentityDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FeatureValidationResponse` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClientDiscoveryForLogSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DppTrackedResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FeatureValidationRequest` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesStorageClassRestoreCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.DppBaseResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidateRestoreRequestObject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerBackupRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Datasource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BlobBackupDatasourceParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.RetentionTag` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CrossRegionRestoreDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidateForBackupRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TargetCopySetting` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationExtendedInfo` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobSubTask` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KubernetesClusterVaultTierRestoreCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added
* `resourceModifierReference()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withResourceModifierReference(models.NamespacedNameResource)` was added

#### `models.PatchResourceRequestInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesPVRestoreCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecretStoreBasedAuthCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.AzureBackupRehydrationRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduleBasedTriggerContext` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BaseBackupPolicyResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackupInstances` was modified

* `suspendBackups(java.lang.String,java.lang.String,java.lang.String,models.SuspendBackupRequest,com.azure.core.util.Context)` was added
* `stopProtection(java.lang.String,java.lang.String,java.lang.String,models.StopProtectionRequest,com.azure.core.util.Context)` was added

#### `models.RecoveryPointDataStoreDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CopyOnExpiryOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImmutabilitySettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataStoreInfoBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AdHocBackupRuleOptions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesClusterRestoreCriteria` was modified

* `objectType()` was added
* `resourceModifierReference()` was added
* `withResourceModifierReference(models.NamespacedNameResource)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FetchSecondaryRPsRequestParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceGuardProxyBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceGuardProxyBaseResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CrossRegionRestoreRequestObject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceGuardResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupRecoveryPoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ItemLevelRestoreTargetInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.AzureBackupFindRestorableTimeRangesRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceGuard` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClientDiscoveryForServiceSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupDiscreteRecoveryPoint` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBackupRecoveryPointBasedRestoreRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withResourceGuardOperationRequests(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.BackupVaultResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBackupRecoveryPointResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImmediateCopyOption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.DppProxyResource` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added

#### `models.FeatureValidationResponseBase` was modified

* `objectType()` was added

#### `models.DppBaseTrackedResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `name()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupJob` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PatchResourceGuardInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DppIdentityDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackupParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

## 1.2.0 (2023-12-18)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CrossRegionRestoreJobRequest` was added

* `models.RecoveryPointCompletionState` was added

* `models.FetchSecondaryRecoveryPoints` was added

* `models.DefaultResourceProperties` was added

* `models.FetchCrossRegionRestoreJobsOperations` was added

* `models.FetchCrossRegionRestoreJobs` was added

* `models.UserFacingWarningDetail` was added

* `models.ValidateCrossRegionRestoreRequestObject` was added

* `models.CrossRegionRestoreJobsRequest` was added

* `models.ResourcePropertiesObjectType` was added

* `models.CrossRegionRestoreDetails` was added

* `models.KubernetesClusterVaultTierRestoreCriteria` was added

* `models.FetchSecondaryRPsRequestParameters` was added

* `models.CrossRegionRestoreRequestObject` was added

#### `models.JobExtendedInfo` was modified

* `warningDetails()` was added

#### `DataProtectionManager` was modified

* `fetchCrossRegionRestoreJobsOperations()` was added
* `fetchSecondaryRecoveryPoints()` was added
* `fetchCrossRegionRestoreJobs()` was added

#### `models.BackupVault` was modified

* `withReplicatedRegions(java.util.List)` was added
* `replicatedRegions()` was added

#### `models.BackupInstances` was modified

* `triggerCrossRegionRestore(java.lang.String,java.lang.String,models.CrossRegionRestoreRequestObject)` was added
* `validateCrossRegionRestore(java.lang.String,java.lang.String,models.ValidateCrossRegionRestoreRequestObject,com.azure.core.util.Context)` was added
* `triggerCrossRegionRestore(java.lang.String,java.lang.String,models.CrossRegionRestoreRequestObject,com.azure.core.util.Context)` was added
* `validateCrossRegionRestore(java.lang.String,java.lang.String,models.ValidateCrossRegionRestoreRequestObject)` was added

#### `models.AzureBackupDiscreteRecoveryPoint` was modified

* `withRecoveryPointState(models.RecoveryPointCompletionState)` was added
* `recoveryPointState()` was added

## 1.1.0 (2023-07-21)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2023-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CrossRegionRestoreState` was added

* `models.BaseResourceProperties` was added

* `models.NamespacedNameResource` was added

* `models.UserAssignedIdentity` was added

* `models.CrossRegionRestoreSettings` was added

* `models.IdentityDetails` was added

* `models.SecureScoreLevel` was added

#### `models.AzureBackupRecoveryTimeBasedRestoreRequest` was modified

* `withIdentityDetails(models.IdentityDetails)` was added

#### `models.AzureBackupRestoreRequest` was modified

* `withIdentityDetails(models.IdentityDetails)` was added
* `identityDetails()` was added

#### `models.DeletedBackupInstance` was modified

* `withIdentityDetails(models.IdentityDetails)` was added

#### `models.AzureBackupRestoreWithRehydrationRequest` was modified

* `withIdentityDetails(models.IdentityDetails)` was added

#### `models.KubernetesClusterBackupDatasourceParameters` was modified

* `backupHookReferences()` was added
* `withBackupHookReferences(java.util.List)` was added

#### `models.DatasourceSet` was modified

* `withResourceProperties(models.BaseResourceProperties)` was added
* `resourceProperties()` was added

#### `models.BackupInstance` was modified

* `withIdentityDetails(models.IdentityDetails)` was added
* `identityDetails()` was added

#### `models.FeatureSettings` was modified

* `crossRegionRestoreSettings()` was added
* `withCrossRegionRestoreSettings(models.CrossRegionRestoreSettings)` was added

#### `models.BackupVault` was modified

* `secureScore()` was added

#### `models.Datasource` was modified

* `withResourceProperties(models.BaseResourceProperties)` was added
* `resourceProperties()` was added

#### `models.KubernetesClusterRestoreCriteria` was modified

* `withRestoreHookReferences(java.util.List)` was added
* `restoreHookReferences()` was added

#### `models.AzureBackupRecoveryPointBasedRestoreRequest` was modified

* `withIdentityDetails(models.IdentityDetails)` was added

#### `models.AzureBackupJob` was modified

* `rehydrationPriority()` was added

#### `models.DppIdentityDetails` was modified

* `userAssignedIdentities()` was added
* `withUserAssignedIdentities(java.util.Map)` was added

## 1.0.0 (2023-06-28)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ResourceGuardProxyBaseResource$DefinitionStages` was added

* `models.DppResourceGuardProxies` was added

* `models.UnlockDeleteRequest` was added

* `models.ResourceGuardOperationDetail` was added

* `models.UnlockDeleteResponse` was added

* `models.ResourceGuardProxyBaseResource$Update` was added

* `models.ResourceGuardProxyBaseResource$Definition` was added

* `models.ResourceGuardProxyBase` was added

* `models.ResourceGuardProxyBaseResourceList` was added

* `models.ResourceGuardProxyBaseResource$UpdateStages` was added

* `models.ResourceGuardProxyBaseResource` was added

#### `DataProtectionManager` was modified

* `dppResourceGuardProxies()` was added

## 1.0.0-beta.4 (2023-06-20)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ResourceGuardProxyBaseResource$DefinitionStages` was added

* `models.DppResourceGuardProxies` was added

* `models.UnlockDeleteRequest` was added

* `models.ResourceGuardOperationDetail` was added

* `models.UnlockDeleteResponse` was added

* `models.ResourceGuardProxyBaseResource$Update` was added

* `models.ResourceGuardProxyBaseResource$Definition` was added

* `models.ResourceGuardProxyBase` was added

* `models.ResourceGuardProxyBaseResourceList` was added

* `models.ResourceGuardProxyBaseResource$UpdateStages` was added

* `models.ResourceGuardProxyBaseResource` was added

#### `DataProtectionManager` was modified

* `dppResourceGuardProxies()` was added

## 1.0.0-beta.3 (2023-02-14)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ResourceGuardProxyBaseResource$DefinitionStages` was removed

* `models.DppResourceGuardProxies` was removed

* `models.UnlockDeleteRequest` was removed

* `models.ResourceGuardOperationDetail` was removed

* `models.ResourceGuardProvisioningState` was removed

* `models.BackupInstancesExtensionRoutings` was removed

* `models.UnlockDeleteResponse` was removed

* `models.ResourceGuardProxyBaseResource$Update` was removed

* `models.ResourceGuardProxyBaseResource$Definition` was removed

* `models.ResourceGuardProxyBase` was removed

* `models.ResourceGuardProxyBaseResourceList` was removed

* `models.ResourceGuardProxyBaseResource$UpdateStages` was removed

* `models.ResourceGuardProxyBaseResource` was removed

#### `models.ResourceGuardResource$Update` was modified

* `withProperties(models.PatchBackupVaultInput)` was removed
* `withIdentity(models.DppIdentityDetails)` was removed

#### `models.DppTrackedResource` was modified

* `etag()` was removed
* `systemData()` was removed

#### `DataProtectionManager` was modified

* `dppResourceGuardProxies()` was removed
* `backupInstancesExtensionRoutings()` was removed

#### `models.BackupVaults` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.ResourceGuardResource$Definition` was modified

* `withIdentity(models.DppIdentityDetails)` was removed

#### `models.ResourceGuard` was modified

* `models.ResourceGuardProvisioningState provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.ResourceGuardResource` was modified

* `identity()` was removed

### Features Added

* `models.CrossSubscriptionRestoreSettings` was added

* `models.FeatureSettings` was added

* `models.CrossSubscriptionRestoreState` was added

* `models.DppBaseTrackedResource` was added

* `models.PatchResourceGuardInput` was added

#### `models.DppTrackedResource` was modified

* `withTags(java.util.Map)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.PatchBackupVaultInput` was modified

* `featureSettings()` was added
* `withFeatureSettings(models.FeatureSettings)` was added

#### `models.TargetDetails` was modified

* `targetResourceArmId()` was added
* `withTargetResourceArmId(java.lang.String)` was added

#### `models.BackupVaults` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BackupVault` was modified

* `withFeatureSettings(models.FeatureSettings)` was added
* `featureSettings()` was added

#### `models.AzureBackupDiscreteRecoveryPoint` was modified

* `expiryTime()` was added

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

