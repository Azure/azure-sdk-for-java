# Release History

## 1.5.0 (2025-09-24)

- Azure Resource Manager Data Protection client library for Java. This package contains Microsoft Azure SDK for Data Protection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package api-version 2025-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ClientDiscoveryValueForSingleApi` was removed

#### `models.AksVolumeTypes` was removed

#### `models.DppWorkerRequest` was removed

#### `models.DppTrackedResource` was removed

#### `models.ClientDiscoveryDisplay` was removed

#### `models.BackupInstanceResourceList` was removed

#### `models.AzureBackupFindRestorableTimeRangesRequestResource` was removed

#### `models.AzureBackupJobResourceList` was removed

#### `models.DeletedBackupInstanceResourceList` was removed

#### `models.ClientDiscoveryForProperties` was removed

#### `models.ClientDiscoveryResponse` was removed

#### `models.ClientDiscoveryForLogSpecification` was removed

#### `models.DppBaseResourceList` was removed

#### `models.BaseBackupPolicyResourceList` was removed

#### `models.ResourceGuardProxyBaseResourceList` was removed

#### `models.ResourceGuardResourceList` was removed

#### `models.ClientDiscoveryForServiceSpecification` was removed

#### `models.BackupVaultResourceList` was removed

#### `models.AzureBackupRecoveryPointResourceList` was removed

#### `models.DppProxyResource` was removed

#### `models.DppBaseTrackedResource` was removed

#### `models.AdhocBasedTriggerContext` was modified

* `validate()` was removed

#### `models.AdhocBasedTaggingCriteria` was modified

* `validate()` was removed

#### `models.AzureBackupRecoveryTimeBasedRestoreRequest` was modified

* `validate()` was removed

#### `models.ResourceMoveDetails` was modified

* `withTargetResourcePath(java.lang.String)` was removed
* `withOperationId(java.lang.String)` was removed
* `withSourceResourcePath(java.lang.String)` was removed
* `withStartTimeUtc(java.lang.String)` was removed
* `validate()` was removed
* `withCompletionTimeUtc(java.lang.String)` was removed

#### `models.CrossRegionRestoreJobRequest` was modified

* `validate()` was removed

#### `models.SourceLifeCycle` was modified

* `validate()` was removed

#### `models.AdlsBlobBackupDatasourceParameters` was modified

* `validate()` was removed

#### `models.TaggingCriteria` was modified

* `validate()` was removed

#### `models.JobExtendedInfo` was modified

* `withAdditionalDetails(java.util.Map)` was removed
* `validate()` was removed

#### `models.SoftDeleteSettings` was modified

* `validate()` was removed

#### `models.BackupVaultOperationResultsGetHeaders` was modified

* `validate()` was removed
* `withLocation(java.lang.String)` was removed
* `withRetryAfter(java.lang.Integer)` was removed
* `withAzureAsyncOperation(java.lang.String)` was removed

#### `models.BackupInstanceResource` was modified

* `models.OperationJobExtendedInfo validateForModifyBackup(models.ValidateForModifyBackupRequest,com.azure.core.util.Context)` -> `void validateForModifyBackup(models.ValidateForModifyBackupRequest,com.azure.core.util.Context)`
* `models.OperationJobExtendedInfo validateForModifyBackup(models.ValidateForModifyBackupRequest)` -> `void validateForModifyBackup(models.ValidateForModifyBackupRequest)`

#### `models.AdhocBackupTriggerOption` was modified

* `validate()` was removed

#### `models.SupportedFeature` was modified

* `withExposureControlledFeatures(java.util.List)` was removed
* `withFeatureName(java.lang.String)` was removed
* `withSupportStatus(models.FeatureSupportStatus)` was removed
* `validate()` was removed

#### `models.PolicyParameters` was modified

* `validate()` was removed

#### `models.DataStoreParameters` was modified

* `validate()` was removed

#### `models.RestorableTimeRange` was modified

* `validate()` was removed
* `withObjectType(java.lang.String)` was removed
* `withStartTime(java.lang.String)` was removed
* `withEndTime(java.lang.String)` was removed

#### `models.AzureBackupParams` was modified

* `validate()` was removed

#### `models.ValidateForModifyBackupRequest` was modified

* `validate()` was removed

#### `models.DppResource` was modified

* `validate()` was removed

#### `models.StorageSetting` was modified

* `validate()` was removed

#### `models.DppResourceList` was modified

* `validate()` was removed
* `models.DppResourceList withNextLink(java.lang.String)` -> `models.DppResourceList withNextLink(java.lang.String)`

#### `models.ProtectionStatusDetails` was modified

* `withErrorDetails(models.UserFacingError)` was removed
* `validate()` was removed
* `withStatus(models.Status)` was removed

#### `models.RangeBasedItemLevelRestoreCriteria` was modified

* `validate()` was removed

#### `models.SecretStoreResource` was modified

* `validate()` was removed

#### `models.AzureBackupRestoreRequest` was modified

* `validate()` was removed

#### `models.BackupSchedule` was modified

* `validate()` was removed

#### `DataProtectionManager` was modified

* `fluent.DataProtectionClient serviceClient()` -> `fluent.DataProtectionManagementClient serviceClient()`

#### `models.PatchBackupVaultInput` was modified

* `validate()` was removed

#### `models.DeletedBackupInstance` was modified

* `withPolicyInfo(models.PolicyInfo)` was removed
* `withDatasourceAuthCredentials(models.AuthCredentials)` was removed
* `withObjectType(java.lang.String)` was removed
* `withDataSourceInfo(models.Datasource)` was removed
* `withResourceGuardOperationRequests(java.util.List)` was removed
* `validate()` was removed
* `withIdentityDetails(models.IdentityDetails)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withDataSourceSetInfo(models.DatasourceSet)` was removed
* `withValidationType(models.ValidationType)` was removed

#### `models.CrossSubscriptionRestoreSettings` was modified

* `validate()` was removed

#### `models.AbsoluteDeleteOption` was modified

* `validate()` was removed

#### `models.SecuritySettings` was modified

* `validate()` was removed

#### `models.DefaultResourceProperties` was modified

* `validate()` was removed

#### `models.AzureBackupRestoreWithRehydrationRequest` was modified

* `validate()` was removed

#### `models.RestoreJobRecoveryPointDetails` was modified

* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed
* `validate()` was removed
* `withRecoveryPointId(java.lang.String)` was removed

#### `models.BaseResourceProperties` was modified

* `validate()` was removed

#### `models.NamespacedNameResource` was modified

* `validate()` was removed

#### `models.TriggerContext` was modified

* `validate()` was removed

#### `models.BasePolicyRule` was modified

* `validate()` was removed

#### `models.EncryptionSettings` was modified

* `validate()` was removed

#### `models.KubernetesClusterBackupDatasourceParameters` was modified

* `validate()` was removed

#### `models.UnlockDeleteRequest` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed
* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID clientId()` -> `java.lang.String clientId()`

#### `models.ResourceGuardOperationDetail` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.UserFacingError` was modified

* `validate()` was removed
* `withProperties(java.util.Map)` was removed
* `withCode(java.lang.String)` was removed
* `withIsUserError(java.lang.Boolean)` was removed
* `withTarget(java.lang.String)` was removed
* `withInnerError(models.InnerError)` was removed
* `withDetails(java.util.List)` was removed
* `withMessage(java.lang.String)` was removed
* `withRecommendedAction(java.util.List)` was removed
* `withIsRetryable(java.lang.Boolean)` was removed

#### `models.DatasourceSet` was modified

* `validate()` was removed

#### `models.RestoreFilesTargetInfo` was modified

* `validate()` was removed

#### `models.RestoreTargetInfo` was modified

* `validate()` was removed

#### `models.DeleteOption` was modified

* `validate()` was removed

#### `models.BackupCriteria` was modified

* `validate()` was removed

#### `models.UserFacingWarningDetail` was modified

* `withResourceName(java.lang.String)` was removed
* `withWarning(models.UserFacingError)` was removed
* `validate()` was removed

#### `models.DeletionInfo` was modified

* `validate()` was removed

#### `models.BackupInstance` was modified

* `validate()` was removed

#### `models.ValidateCrossRegionRestoreRequestObject` was modified

* `validate()` was removed

#### `models.CustomCopyOption` was modified

* `validate()` was removed

#### `models.TargetDetails` was modified

* `validate()` was removed

#### `models.ItemLevelRestoreCriteria` was modified

* `validate()` was removed

#### `models.ItemPathBasedRestoreCriteria` was modified

* `validate()` was removed

#### `models.RestoreTargetInfoBase` was modified

* `validate()` was removed

#### `models.AzureOperationalStoreParameters` was modified

* `validate()` was removed

#### `models.BaseBackupPolicy` was modified

* `validate()` was removed

#### `models.InnerError` was modified

* `validate()` was removed
* `withCode(java.lang.String)` was removed
* `withAdditionalInfo(java.util.Map)` was removed
* `withEmbeddedInnerError(models.InnerError)` was removed

#### `models.AzureRetentionRule` was modified

* `validate()` was removed

#### `models.MonitoringSettings` was modified

* `validate()` was removed

#### `models.FeatureValidationRequestBase` was modified

* `validate()` was removed

#### `models.FeatureSettings` was modified

* `validate()` was removed

#### `models.AuthCredentials` was modified

* `validate()` was removed

#### `models.BackupVault` was modified

* `validate()` was removed

#### `models.Day` was modified

* `validate()` was removed

#### `models.ResourceGuardOperation` was modified

* `validate()` was removed

#### `models.SyncBackupInstanceRequest` was modified

* `validate()` was removed

#### `models.AzureBackupRule` was modified

* `validate()` was removed

#### `models.StopProtectionRequest` was modified

* `validate()` was removed

#### `models.CopyOption` was modified

* `validate()` was removed

#### `models.BackupDatasourceParameters` was modified

* `validate()` was removed

#### `models.ScheduleBasedBackupCriteria` was modified

* `validate()` was removed

#### `models.CrossRegionRestoreSettings` was modified

* `validate()` was removed

#### `models.CrossRegionRestoreJobsRequest` was modified

* `validate()` was removed

#### `models.AzureBackupFindRestorableTimeRangesResponse` was modified

* `withRestorableTimeRanges(java.util.List)` was removed
* `withObjectType(java.lang.String)` was removed
* `validate()` was removed

#### `models.CmkKeyVaultProperties` was modified

* `validate()` was removed

#### `models.CmkKekIdentity` was modified

* `validate()` was removed

#### `models.AzureMonitorAlertSettings` was modified

* `validate()` was removed

#### `models.IdentityDetails` was modified

* `validate()` was removed

#### `models.FeatureValidationResponse` was modified

* `withFeatureType(models.FeatureType)` was removed
* `validate()` was removed
* `withFeatures(java.util.List)` was removed

#### `models.DppTrackedResourceList` was modified

* `validate()` was removed
* `models.DppTrackedResourceList withNextLink(java.lang.String)` -> `models.DppTrackedResourceList withNextLink(java.lang.String)`

#### `models.FeatureValidationRequest` was modified

* `validate()` was removed

#### `models.KubernetesStorageClassRestoreCriteria` was modified

* `validate()` was removed

#### `models.ValidateRestoreRequestObject` was modified

* `validate()` was removed

#### `models.TriggerBackupRequest` was modified

* `validate()` was removed

#### `models.Datasource` was modified

* `validate()` was removed

#### `models.BlobBackupDatasourceParameters` was modified

* `validate()` was removed

#### `models.RetentionTag` was modified

* `validate()` was removed

#### `models.CrossRegionRestoreDetails` was modified

* `validate()` was removed

#### `models.ValidateForBackupRequest` was modified

* `validate()` was removed

#### `models.OperationResultsGetHeaders` was modified

* `validate()` was removed
* `withAzureAsyncOperation(java.lang.String)` was removed
* `withLocation(java.lang.String)` was removed
* `withRetryAfter(java.lang.Integer)` was removed

#### `models.TargetCopySetting` was modified

* `validate()` was removed

#### `models.OperationExtendedInfo` was modified

* `validate()` was removed

#### `models.JobSubTask` was modified

* `withTaskId(int)` was removed
* `withTaskStatus(java.lang.String)` was removed
* `withAdditionalDetails(java.util.Map)` was removed
* `validate()` was removed
* `withTaskName(java.lang.String)` was removed

#### `models.KubernetesClusterVaultTierRestoreCriteria` was modified

* `validate()` was removed

#### `models.PatchResourceRequestInput` was modified

* `validate()` was removed

#### `models.KubernetesPVRestoreCriteria` was modified

* `validate()` was removed

#### `models.SecretStoreBasedAuthCredentials` was modified

* `validate()` was removed

#### `models.AzureBackupRehydrationRequest` was modified

* `validate()` was removed

#### `models.ScheduleBasedTriggerContext` was modified

* `validate()` was removed

#### `models.BackupInstances` was modified

* `models.OperationJobExtendedInfo validateForModifyBackup(java.lang.String,java.lang.String,java.lang.String,models.ValidateForModifyBackupRequest,com.azure.core.util.Context)` -> `void validateForModifyBackup(java.lang.String,java.lang.String,java.lang.String,models.ValidateForModifyBackupRequest,com.azure.core.util.Context)`
* `models.OperationJobExtendedInfo validateForModifyBackup(java.lang.String,java.lang.String,java.lang.String,models.ValidateForModifyBackupRequest)` -> `void validateForModifyBackup(java.lang.String,java.lang.String,java.lang.String,models.ValidateForModifyBackupRequest)`

#### `models.RecoveryPointDataStoreDetails` was modified

* `withType(java.lang.String)` was removed
* `withState(java.lang.String)` was removed
* `withCreationTime(java.time.OffsetDateTime)` was removed
* `withVisible(java.lang.Boolean)` was removed
* `validate()` was removed
* `withExpiryTime(java.time.OffsetDateTime)` was removed
* `withId(java.lang.String)` was removed
* `withMetadata(java.lang.String)` was removed

#### `models.CopyOnExpiryOption` was modified

* `validate()` was removed

#### `models.ImmutabilitySettings` was modified

* `validate()` was removed

#### `models.DataStoreInfoBase` was modified

* `validate()` was removed

#### `models.AdHocBackupRuleOptions` was modified

* `validate()` was removed

#### `models.KubernetesClusterRestoreCriteria` was modified

* `validate()` was removed

#### `models.FetchSecondaryRPsRequestParameters` was modified

* `validate()` was removed

#### `models.ResourceGuardProxyBase` was modified

* `validate()` was removed

#### `models.CrossRegionRestoreRequestObject` was modified

* `validate()` was removed

#### `models.BackupPolicy` was modified

* `validate()` was removed

#### `models.AzureBackupRecoveryPoint` was modified

* `validate()` was removed

#### `models.ItemLevelRestoreTargetInfo` was modified

* `validate()` was removed

#### `models.SuspendBackupRequest` was modified

* `validate()` was removed

#### `models.AzureBackupFindRestorableTimeRangesRequest` was modified

* `validate()` was removed

#### `models.ResourceGuard` was modified

* `validate()` was removed

#### `models.AzureBackupDiscreteRecoveryPoint` was modified

* `withRecoveryPointId(java.lang.String)` was removed
* `withRetentionTagVersion(java.lang.String)` was removed
* `withRecoveryPointState(models.RecoveryPointCompletionState)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withRecoveryPointType(java.lang.String)` was removed
* `validate()` was removed
* `withPolicyVersion(java.lang.String)` was removed
* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed
* `withPolicyName(java.lang.String)` was removed
* `withRetentionTagName(java.lang.String)` was removed
* `withRecoveryPointDataStoresDetails(java.util.List)` was removed

#### `models.AzureBackupRecoveryPointBasedRestoreRequest` was modified

* `validate()` was removed

#### `models.ImmediateCopyOption` was modified

* `validate()` was removed

#### `models.AzureBackupJob` was modified

* `withBackupInstanceFriendlyName(java.lang.String)` was removed
* `withDataSourceName(java.lang.String)` was removed
* `withDataSourceSetName(java.lang.String)` was removed
* `withDestinationDataStoreName(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withSourceDataStoreName(java.lang.String)` was removed
* `withDataSourceLocation(java.lang.String)` was removed
* `withDuration(java.lang.String)` was removed
* `withIsUserTriggered(boolean)` was removed
* `validate()` was removed
* `withDataSourceType(java.lang.String)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withEtag(java.lang.String)` was removed
* `withProgressEnabled(boolean)` was removed
* `withSourceSubscriptionId(java.lang.String)` was removed
* `withSubscriptionId(java.lang.String)` was removed
* `withSupportedActions(java.util.List)` was removed
* `withActivityId(java.lang.String)` was removed
* `withVaultName(java.lang.String)` was removed
* `withSourceResourceGroup(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withDataSourceId(java.lang.String)` was removed
* `withOperationCategory(java.lang.String)` was removed

#### `models.PatchResourceGuardInput` was modified

* `validate()` was removed

#### `models.DppIdentityDetails` was modified

* `validate()` was removed

#### `models.PolicyInfo` was modified

* `validate()` was removed

#### `models.BackupParameters` was modified

* `validate()` was removed

### Features Added

* `models.AKSVolumeTypes` was added

* `models.OperationDisplay` was added

* `models.Origin` was added

* `models.ActionType` was added

* `models.Operation` was added

#### `models.DppBaseResource` was modified

* `systemData()` was added

## 1.4.0 (2025-08-15)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2025-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AksVolumeTypes` was added

* `models.AdlsBlobBackupDatasourceParameters` was added

* `models.ValidateForModifyBackupRequest` was added

#### `models.BackupInstanceResource` was modified

* `validateForModifyBackup(models.ValidateForModifyBackupRequest)` was added
* `validateForModifyBackup(models.ValidateForModifyBackupRequest,com.azure.core.util.Context)` was added

#### `models.KubernetesClusterBackupDatasourceParameters` was modified

* `includedVolumeTypes()` was added
* `withIncludedVolumeTypes(java.util.List)` was added

#### `models.ItemPathBasedRestoreCriteria` was modified

* `withRenameTo(java.lang.String)` was added
* `renameTo()` was added

#### `models.BackupInstances` was modified

* `validateForModifyBackup(java.lang.String,java.lang.String,java.lang.String,models.ValidateForModifyBackupRequest,com.azure.core.util.Context)` was added
* `validateForModifyBackup(java.lang.String,java.lang.String,java.lang.String,models.ValidateForModifyBackupRequest)` was added

## 1.3.0 (2024-07-22)

- Azure Resource Manager DataProtection client library for Java. This package contains Microsoft Azure SDK for DataProtection Management SDK. Open API 2.0 Specs for Azure Data Protection service. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

- IMPORTANT: use `AuxiliaryAuthenticationPolicy` from "azure-resourcemanager-resources" module via `DataProtectionManager.configure().withPolicy(..)`, for setting "x-ms-authorization-auxiliary" header.

### Breaking Changes

#### `models.BackupInstanceResource` was modified

* `suspendBackups(com.azure.core.util.Context)` was removed
* `stopProtection(com.azure.core.util.Context)` was removed

#### `models.BackupInstances` was modified

* `stopProtection(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `suspendBackups(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

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

* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AdhocBasedTaggingCriteria` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupRecoveryTimeBasedRestoreRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.ResourceMoveDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CrossRegionRestoreJobRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceLifeCycle` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DppWorkerRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TaggingCriteria` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobExtendedInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SoftDeleteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DppTrackedResource` was modified

* `type()` was added
* `name()` was added
* `systemData()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupInstanceResource` was modified

* `stopProtection(models.StopProtectionRequest,com.azure.core.util.Context)` was added
* `suspendBackups(models.SuspendBackupRequest,com.azure.core.util.Context)` was added

#### `models.AdhocBackupTriggerOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SupportedFeature` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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
* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClientDiscoveryDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DppResource` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `id()` was added

#### `models.StorageSetting` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DppResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ProtectionStatusDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupInstanceResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RangeBasedItemLevelRestoreCriteria` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecretStoreResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupRestoreRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `resourceGuardOperationRequests()` was added

#### `models.BackupSchedule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `DataProtectionManager` was modified

* `backupInstancesExtensionRoutings()` was added

#### `models.PatchBackupVaultInput` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `resourceGuardOperationRequests()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeletedBackupInstance` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `protectionStatus()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `currentProtectionState()` was added
* `protectionErrorDetails()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added

#### `models.CrossSubscriptionRestoreSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AbsoluteDeleteOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecuritySettings` was modified

* `encryptionSettings()` was added
* `withEncryptionSettings(models.EncryptionSettings)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DefaultResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBackupRestoreWithRehydrationRequest` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withResourceGuardOperationRequests(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RestoreJobRecoveryPointDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BaseResourceProperties` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupFindRestorableTimeRangesRequestResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NamespacedNameResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerContext` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BasePolicyRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.AzureBackupJobResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesClusterBackupDatasourceParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.UnlockDeleteRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceGuardOperationDetail` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RestoreTargetInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.DeleteOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.BackupCriteria` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.DeletedBackupInstanceResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserFacingWarningDetail` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DeletionInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupInstance` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `resourceGuardOperationRequests()` was added

#### `models.ValidateCrossRegionRestoreRequestObject` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClientDiscoveryForProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomCopyOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.TargetDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ItemLevelRestoreCriteria` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ItemPathBasedRestoreCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.RestoreTargetInfoBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.AzureOperationalStoreParameters` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BaseBackupPolicy` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InnerError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureRetentionRule` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MonitoringSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FeatureValidationRequestBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.FeatureSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AuthCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupVault` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `bcdrSecurityLevel()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `resourceGuardOperationRequests()` was added

#### `models.Day` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceGuardOperation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.BackupDatasourceParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added

#### `models.ScheduleBasedBackupCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.CrossRegionRestoreSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CrossRegionRestoreJobsRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupFindRestorableTimeRangesResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureMonitorAlertSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IdentityDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FeatureValidationResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.ClientDiscoveryForLogSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DppTrackedResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FeatureValidationRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.KubernetesStorageClassRestoreCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

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

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BlobBackupDatasourceParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
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

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationExtendedInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobSubTask` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KubernetesClusterVaultTierRestoreCriteria` was modified

* `resourceModifierReference()` was added
* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withResourceModifierReference(models.NamespacedNameResource)` was added

#### `models.PatchResourceRequestInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KubernetesPVRestoreCriteria` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecretStoreBasedAuthCredentials` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupRehydrationRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduleBasedTriggerContext` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BaseBackupPolicyResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackupInstances` was modified

* `stopProtection(java.lang.String,java.lang.String,java.lang.String,models.StopProtectionRequest,com.azure.core.util.Context)` was added
* `suspendBackups(java.lang.String,java.lang.String,java.lang.String,models.SuspendBackupRequest,com.azure.core.util.Context)` was added

#### `models.RecoveryPointDataStoreDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CopyOnExpiryOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImmutabilitySettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataStoreInfoBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AdHocBackupRuleOptions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesClusterRestoreCriteria` was modified

* `resourceModifierReference()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withResourceModifierReference(models.NamespacedNameResource)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FetchSecondaryRPsRequestParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceGuardProxyBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceGuardProxyBaseResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CrossRegionRestoreRequestObject` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceGuardResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupPolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.AzureBackupRecoveryPoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added

#### `models.ItemLevelRestoreTargetInfo` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupFindRestorableTimeRangesRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceGuard` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClientDiscoveryForServiceSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupDiscreteRecoveryPoint` was modified

* `objectType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBackupRecoveryPointBasedRestoreRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupVaultResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBackupRecoveryPointResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImmediateCopyOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DppProxyResource` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added

#### `models.FeatureValidationResponseBase` was modified

* `objectType()` was added

#### `models.DppBaseTrackedResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `name()` was added

#### `models.AzureBackupJob` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PatchResourceGuardInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DppIdentityDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackupParameters` was modified

* `objectType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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

