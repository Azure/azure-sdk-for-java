# Release History

## 1.7.0-beta.1 (2026-02-09)

- Azure Resource Manager Recovery Services Backup client library for Java. This package contains Microsoft Azure SDK for Recovery Services Backup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package api-version 2026-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ProtectionIntentResourceList` was removed

#### `models.ResourceGuardProxies` was removed

#### `models.JobResourceList` was removed

#### `models.RecoveryPointResourceList` was removed

#### `models.ProtectableContainerResourceList` was removed

#### `models.WorkloadItemResourceList` was removed

#### `models.ResourceGuardProxyBaseResourceList` was removed

#### `models.ProtectedItemResourceList` was removed

#### `models.ProtectionContainerResourceList` was removed

#### `models.BackupManagementUsageList` was removed

#### `models.ProtectionPolicyResourceList` was removed

#### `models.BackupEngineBaseResourceList` was removed

#### `models.IaaSvmProtectableItem` was removed

#### `models.WorkloadProtectableItemResourceList` was removed

#### `models.ClientDiscoveryResponse` was removed

#### `models.AzureWorkloadSqlPointInTimeRecoveryPoint` was modified

* `AzureWorkloadSqlPointInTimeRecoveryPoint()` was changed to private access
* `withTimeRanges(java.util.List)` was removed
* `withRecoveryPointTimeInUtc(java.time.OffsetDateTime)` was removed
* `validate()` was removed
* `withType(models.RestorePointType)` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed
* `withRecoveryPointMoveReadinessInfo(java.util.Map)` was removed
* `withExtendedInfo(models.AzureWorkloadSqlRecoveryPointExtendedInfo)` was removed
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed

#### `models.MabJobTaskDetails` was modified

* `MabJobTaskDetails()` was changed to private access
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withStatus(java.lang.String)` was removed
* `validate()` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withDuration(java.time.Duration)` was removed
* `withTaskId(java.lang.String)` was removed

#### `models.AzureIaaSClassicComputeVMContainer` was modified

* `validate()` was removed

#### `models.IaaSvmContainer` was modified

* `validate()` was removed

#### `models.OperationStatusExtendedInfo` was modified

* `validate()` was removed

#### `models.TargetRestoreInfo` was modified

* `validate()` was removed

#### `models.AzureWorkloadSapAsePointInTimeRestoreRequest` was modified

* `validate()` was removed

#### `models.RestoreFileSpecs` was modified

* `validate()` was removed

#### `models.AzureIaaSComputeVMProtectedItem` was modified

* `validate()` was removed
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed

#### `models.AzureWorkloadPointInTimeRecoveryPoint` was modified

* `validate()` was removed
* `withRecoveryPointTimeInUtc(java.time.OffsetDateTime)` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed
* `models.AzureWorkloadPointInTimeRecoveryPoint withTimeRanges(java.util.List)` -> `models.AzureWorkloadPointInTimeRecoveryPoint withTimeRanges(java.util.List)`
* `withType(models.RestorePointType)` was removed
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withRecoveryPointMoveReadinessInfo(java.util.Map)` was removed

#### `models.AzureFileShareProvisionIlrRequest` was modified

* `validate()` was removed

#### `models.RetentionDuration` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapHanaHsr` was modified

* `AzureVmWorkloadSapHanaHsr()` was changed to private access
* `withFriendlyName(java.lang.String)` was removed
* `withParentName(java.lang.String)` was removed
* `withIsProtectable(java.lang.Boolean)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed
* `validate()` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withParentUniqueName(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withServerName(java.lang.String)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed

#### `models.AzureWorkloadRecoveryPoint` was modified

* `validate()` was removed
* `models.AzureWorkloadRecoveryPoint withRecoveryPointMoveReadinessInfo(java.util.Map)` -> `models.AzureWorkloadRecoveryPoint withRecoveryPointMoveReadinessInfo(java.util.Map)`
* `models.AzureWorkloadRecoveryPoint withRecoveryPointProperties(models.RecoveryPointProperties)` -> `models.AzureWorkloadRecoveryPoint withRecoveryPointProperties(models.RecoveryPointProperties)`
* `models.AzureWorkloadRecoveryPoint withRecoveryPointTimeInUtc(java.time.OffsetDateTime)` -> `models.AzureWorkloadRecoveryPoint withRecoveryPointTimeInUtc(java.time.OffsetDateTime)`
* `models.AzureWorkloadRecoveryPoint withRecoveryPointTierDetails(java.util.List)` -> `models.AzureWorkloadRecoveryPoint withRecoveryPointTierDetails(java.util.List)`
* `models.AzureWorkloadRecoveryPoint withType(models.RestorePointType)` -> `models.AzureWorkloadRecoveryPoint withType(models.RestorePointType)`

#### `models.OperationStatusValidateOperationExtendedInfo` was modified

* `OperationStatusValidateOperationExtendedInfo()` was changed to private access
* `withValidateOperationResponse(models.ValidateOperationResponse)` was removed
* `validate()` was removed

#### `models.AzureVMAppContainerProtectableContainer` was modified

* `AzureVMAppContainerProtectableContainer()` was changed to private access
* `withHealthStatus(java.lang.String)` was removed
* `withContainerId(java.lang.String)` was removed
* `validate()` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed

#### `models.AzureVmWorkloadSapHanaSystemWorkloadItem` was modified

* `AzureVmWorkloadSapHanaSystemWorkloadItem()` was changed to private access
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withSubWorkloadItemCount(java.lang.Integer)` was removed
* `validate()` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withParentName(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withServerName(java.lang.String)` was removed

#### `models.IaasVMRestoreRequest` was modified

* `validate()` was removed

#### `models.OperationStatusProvisionIlrExtendedInfo` was modified

* `OperationStatusProvisionIlrExtendedInfo()` was changed to private access
* `validate()` was removed
* `withRecoveryTarget(models.InstantItemRecoveryTarget)` was removed

#### `models.AzureVmWorkloadSapHanaDatabaseWorkloadItem` was modified

* `AzureVmWorkloadSapHanaDatabaseWorkloadItem()` was changed to private access
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withServerName(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withSubWorkloadItemCount(java.lang.Integer)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `validate()` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withParentName(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed

#### `models.MabFileFolderProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.GenericRecoveryPoint` was modified

* `GenericRecoveryPoint()` was changed to private access
* `withFriendlyName(java.lang.String)` was removed
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed
* `validate()` was removed
* `withRecoveryPointAdditionalInfo(java.lang.String)` was removed
* `withRecoveryPointType(java.lang.String)` was removed

#### `models.IaasVMBackupRequest` was modified

* `validate()` was removed

#### `models.ListRecoveryPointsRecommendedForMoveRequest` was modified

* `validate()` was removed

#### `models.DpmBackupEngine` was modified

* `DpmBackupEngine()` was changed to private access
* `withIsAzureBackupAgentUpgradeAvailable(java.lang.Boolean)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withDpmVersion(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withBackupEngineId(java.lang.String)` was removed
* `withIsDpmUpgradeAvailable(java.lang.Boolean)` was removed
* `withExtendedInfo(models.BackupEngineExtendedInfo)` was removed
* `withCanReRegister(java.lang.Boolean)` was removed
* `withRegistrationStatus(java.lang.String)` was removed
* `withAzureBackupAgentVersion(java.lang.String)` was removed
* `withHealthStatus(java.lang.String)` was removed
* `withBackupEngineState(java.lang.String)` was removed
* `validate()` was removed

#### `models.MabContainerExtendedInfo` was modified

* `validate()` was removed

#### `models.AzureWorkloadSqlRecoveryPoint` was modified

* `withRecoveryPointMoveReadinessInfo(java.util.Map)` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed
* `validate()` was removed
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withType(models.RestorePointType)` was removed
* `withRecoveryPointTimeInUtc(java.time.OffsetDateTime)` was removed
* `models.AzureWorkloadSqlRecoveryPoint withExtendedInfo(models.AzureWorkloadSqlRecoveryPointExtendedInfo)` -> `models.AzureWorkloadSqlRecoveryPoint withExtendedInfo(models.AzureWorkloadSqlRecoveryPointExtendedInfo)`

#### `models.GenericContainerExtendedInfo` was modified

* `validate()` was removed

#### `models.AzureStorageProtectableContainer` was modified

* `AzureStorageProtectableContainer()` was changed to private access
* `withContainerId(java.lang.String)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `validate()` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withHealthStatus(java.lang.String)` was removed

#### `models.NameInfo` was modified

* `NameInfo()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withLocalizedValue(java.lang.String)` was removed
* `validate()` was removed

#### `models.MoveRPAcrossTiersRequest` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapHanaDBInstance` was modified

* `AzureVmWorkloadSapHanaDBInstance()` was changed to private access
* `withIsProtectable(java.lang.Boolean)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withParentName(java.lang.String)` was removed
* `withServerName(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withParentUniqueName(java.lang.String)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `validate()` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed

#### `models.FetchTieringCostInfoForRehydrationRequest` was modified

* `validate()` was removed

#### `models.BackupEngineBase` was modified

* `models.BackupEngineBase withDpmVersion(java.lang.String)` -> `models.BackupEngineBase withDpmVersion(java.lang.String)`
* `validate()` was removed
* `models.BackupEngineBase withRegistrationStatus(java.lang.String)` -> `models.BackupEngineBase withRegistrationStatus(java.lang.String)`
* `models.BackupEngineBase withHealthStatus(java.lang.String)` -> `models.BackupEngineBase withHealthStatus(java.lang.String)`
* `models.BackupEngineBase withExtendedInfo(models.BackupEngineExtendedInfo)` -> `models.BackupEngineBase withExtendedInfo(models.BackupEngineExtendedInfo)`
* `models.BackupEngineBase withIsAzureBackupAgentUpgradeAvailable(java.lang.Boolean)` -> `models.BackupEngineBase withIsAzureBackupAgentUpgradeAvailable(java.lang.Boolean)`
* `models.BackupEngineBase withBackupEngineId(java.lang.String)` -> `models.BackupEngineBase withBackupEngineId(java.lang.String)`
* `models.BackupEngineBase withAzureBackupAgentVersion(java.lang.String)` -> `models.BackupEngineBase withAzureBackupAgentVersion(java.lang.String)`
* `models.BackupEngineBase withIsDpmUpgradeAvailable(java.lang.Boolean)` -> `models.BackupEngineBase withIsDpmUpgradeAvailable(java.lang.Boolean)`
* `models.BackupEngineBase withFriendlyName(java.lang.String)` -> `models.BackupEngineBase withFriendlyName(java.lang.String)`
* `models.BackupEngineBase withCanReRegister(java.lang.Boolean)` -> `models.BackupEngineBase withCanReRegister(java.lang.Boolean)`
* `models.BackupEngineBase withBackupEngineState(java.lang.String)` -> `models.BackupEngineBase withBackupEngineState(java.lang.String)`
* `models.BackupEngineBase withBackupManagementType(models.BackupManagementType)` -> `models.BackupEngineBase withBackupManagementType(models.BackupManagementType)`

#### `models.OperationWorkerResponse` was modified

* `validate()` was removed
* `models.OperationWorkerResponse withHeaders(java.util.Map)` -> `models.OperationWorkerResponse withHeaders(java.util.Map)`
* `models.OperationWorkerResponse withStatusCode(models.HttpStatusCode)` -> `models.OperationWorkerResponse withStatusCode(models.HttpStatusCode)`

#### `models.RestoreRequest` was modified

* `validate()` was removed

#### `models.ProtectableContainer` was modified

* `models.ProtectableContainer withContainerId(java.lang.String)` -> `models.ProtectableContainer withContainerId(java.lang.String)`
* `models.ProtectableContainer withFriendlyName(java.lang.String)` -> `models.ProtectableContainer withFriendlyName(java.lang.String)`
* `models.ProtectableContainer withBackupManagementType(models.BackupManagementType)` -> `models.ProtectableContainer withBackupManagementType(models.BackupManagementType)`
* `models.ProtectableContainer withHealthStatus(java.lang.String)` -> `models.ProtectableContainer withHealthStatus(java.lang.String)`
* `validate()` was removed

#### `models.PrepareDataMoveRequest` was modified

* `validate()` was removed

#### `models.AzureFileShareBackupRequest` was modified

* `validate()` was removed

#### `models.AzureWorkloadSapHanaPointInTimeRestoreWithRehydrateRequest` was modified

* `validate()` was removed

#### `models.IdentityInfo` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapHanaDatabaseProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.LongTermSchedulePolicy` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSqlInstanceProtectableItem` was modified

* `AzureVmWorkloadSqlInstanceProtectableItem()` was changed to private access
* `withBackupManagementType(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withServerName(java.lang.String)` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withIsProtectable(java.lang.Boolean)` was removed
* `withParentName(java.lang.String)` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed
* `withParentUniqueName(java.lang.String)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.AzureVmWorkloadSqlAvailabilityGroupProtectableItem` was modified

* `AzureVmWorkloadSqlAvailabilityGroupProtectableItem()` was changed to private access
* `withNodesList(java.util.List)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `withServerName(java.lang.String)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withIsProtectable(java.lang.Boolean)` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `withParentName(java.lang.String)` was removed
* `withParentUniqueName(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `validate()` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed

#### `models.YearlyRetentionSchedule` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapAseDatabaseProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.AzureFileShareProtectableItem` was modified

* `AzureFileShareProtectableItem()` was changed to private access
* `withWorkloadType(java.lang.String)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `validate()` was removed
* `withAzureFileShareType(models.AzureFileShareType)` was removed
* `withParentContainerFabricId(java.lang.String)` was removed
* `withParentContainerFriendlyName(java.lang.String)` was removed

#### `models.RecoveryPointMoveReadinessInfo` was modified

* `RecoveryPointMoveReadinessInfo()` was changed to private access
* `withIsReadyForMove(java.lang.Boolean)` was removed
* `withAdditionalInfo(java.lang.String)` was removed
* `validate()` was removed

#### `models.PointInTimeRange` was modified

* `PointInTimeRange()` was changed to private access
* `validate()` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed

#### `models.AzureWorkloadErrorInfo` was modified

* `AzureWorkloadErrorInfo()` was changed to private access
* `withRecommendations(java.util.List)` was removed
* `withErrorString(java.lang.String)` was removed
* `withErrorCode(java.lang.Integer)` was removed
* `withErrorTitle(java.lang.String)` was removed
* `validate()` was removed
* `withAdditionalDetails(java.lang.String)` was removed

#### `models.AzureWorkloadSqlRestoreWithRehydrateRequest` was modified

* `validate()` was removed

#### `models.MabContainer` was modified

* `validate()` was removed

#### `models.UserAssignedIdentityProperties` was modified

* `validate()` was removed

#### `models.AzureIaaSvmJobExtendedInfo` was modified

* `AzureIaaSvmJobExtendedInfo()` was changed to private access
* `withEstimatedRemainingDuration(java.lang.String)` was removed
* `withTasksList(java.util.List)` was removed
* `withPropertyBag(java.util.Map)` was removed
* `withInternalPropertyBag(java.util.Map)` was removed
* `validate()` was removed
* `withDynamicErrorMessage(java.lang.String)` was removed
* `withProgressPercentage(java.lang.Double)` was removed

#### `models.ResourceHealthDetails` was modified

* `validate()` was removed

#### `models.AzureWorkloadJobTaskDetails` was modified

* `AzureWorkloadJobTaskDetails()` was changed to private access
* `withTaskId(java.lang.String)` was removed
* `validate()` was removed
* `withStatus(java.lang.String)` was removed

#### `models.AzureIaaSvmErrorInfo` was modified

* `AzureIaaSvmErrorInfo()` was changed to private access
* `validate()` was removed

#### `models.BackupResourceVaultConfig` was modified

* `validate()` was removed

#### `models.GenericProtectionPolicy` was modified

* `validate()` was removed

#### `models.SqlDataDirectory` was modified

* `SqlDataDirectory()` was changed to private access
* `withPath(java.lang.String)` was removed
* `validate()` was removed
* `withLogicalName(java.lang.String)` was removed
* `withType(models.SqlDataDirectoryType)` was removed

#### `models.BackupResourceEncryptionConfig` was modified

* `validate()` was removed

#### `models.ClientDiscoveryForServiceSpecification` was modified

* `ClientDiscoveryForServiceSpecification()` was changed to private access
* `withLogSpecifications(java.util.List)` was removed
* `validate()` was removed

#### `models.IaasVMRestoreWithRehydrationRequest` was modified

* `validate()` was removed

#### `models.ValidateOperationRequest` was modified

* `validate()` was removed

#### `models.AzureWorkloadSapHanaPointInTimeRestoreRequest` was modified

* `validate()` was removed

#### `models.AzureWorkloadSqlPointInTimeRestoreRequest` was modified

* `validate()` was removed

#### `models.AzureWorkloadSqlAutoProtectionIntent` was modified

* `validate()` was removed

#### `models.BackupRequest` was modified

* `validate()` was removed

#### `models.ResourceGuardOperationDetail` was modified

* `validate()` was removed

#### `models.SubProtectionPolicy` was modified

* `validate()` was removed

#### `models.DpmContainerExtendedInfo` was modified

* `validate()` was removed

#### `models.MabJob` was modified

* `MabJob()` was changed to private access
* `withEntityFriendlyName(java.lang.String)` was removed
* `withErrorDetails(java.util.List)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withExtendedInfo(models.MabJobExtendedInfo)` was removed
* `withMabServerName(java.lang.String)` was removed
* `withWorkloadType(models.WorkloadType)` was removed
* `withDuration(java.time.Duration)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withActivityId(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withMabServerType(models.MabServerType)` was removed
* `validate()` was removed
* `withActionsInfo(java.util.List)` was removed

#### `models.AzureStorageJobTaskDetails` was modified

* `AzureStorageJobTaskDetails()` was changed to private access
* `withTaskId(java.lang.String)` was removed
* `validate()` was removed
* `withStatus(java.lang.String)` was removed

#### `models.AzureFileShareRestoreRequest` was modified

* `validate()` was removed

#### `models.KeyAndSecretDetails` was modified

* `KeyAndSecretDetails()` was changed to private access
* `withBekDetails(models.BekDetails)` was removed
* `withEncryptionMechanism(java.lang.String)` was removed
* `validate()` was removed
* `withKekDetails(models.KekDetails)` was removed

#### `models.ErrorDetail` was modified

* `validate()` was removed

#### `models.ProtectionContainer` was modified

* `validate()` was removed

#### `models.AzureWorkloadContainerExtendedInfo` was modified

* `validate()` was removed

#### `models.AzureWorkloadSapHanaPointInTimeRecoveryPoint` was modified

* `AzureWorkloadSapHanaPointInTimeRecoveryPoint()` was changed to private access
* `validate()` was removed
* `withRecoveryPointTimeInUtc(java.time.OffsetDateTime)` was removed
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withType(models.RestorePointType)` was removed
* `withTimeRanges(java.util.List)` was removed
* `withRecoveryPointMoveReadinessInfo(java.util.Map)` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed

#### `models.AzureWorkloadSapAseRestoreRequest` was modified

* `validate()` was removed

#### `models.AzureIaaSComputeVMProtectableItem` was modified

* `AzureIaaSComputeVMProtectableItem()` was changed to private access
* `withResourceGroup(java.lang.String)` was removed
* `withVirtualMachineId(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withVirtualMachineVersion(java.lang.String)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `validate()` was removed

#### `models.MonthlyRetentionSchedule` was modified

* `validate()` was removed

#### `models.RetentionPolicy` was modified

* `validate()` was removed

#### `models.TargetDiskNetworkAccessSettings` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.AzureWorkloadSapHanaRestoreWithRehydrateRequest` was modified

* `validate()` was removed

#### `models.BackupResourceEncryptionConfigExtended` was modified

* `BackupResourceEncryptionConfigExtended()` was changed to private access
* `withEncryptionAtRestType(models.EncryptionAtRestType)` was removed
* `validate()` was removed
* `withInfrastructureEncryptionState(models.InfrastructureEncryptionState)` was removed
* `withSubscriptionId(java.lang.String)` was removed
* `withLastUpdateStatus(models.LastUpdateStatus)` was removed
* `withKeyUri(java.lang.String)` was removed
* `withUserAssignedIdentity(java.lang.String)` was removed
* `withUseSystemAssignedIdentity(java.lang.Boolean)` was removed

#### `models.MabErrorInfo` was modified

* `MabErrorInfo()` was changed to private access
* `validate()` was removed

#### `models.Settings` was modified

* `validate()` was removed

#### `models.AzureIaaSvmProtectedItem` was modified

* `validate()` was removed
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed

#### `models.ContainerIdentityInfo` was modified

* `validate()` was removed

#### `models.AzureWorkloadContainerAutoProtectionIntent` was modified

* `validate()` was removed

#### `models.InstantItemRecoveryTarget` was modified

* `InstantItemRecoveryTarget()` was changed to private access
* `withClientScripts(java.util.List)` was removed
* `validate()` was removed

#### `models.WorkloadProtectableItemResource` was modified

* `etag()` was removed

#### `models.ProtectionPolicy` was modified

* `validate()` was removed

#### `models.ValidateOperationResponse` was modified

* `ValidateOperationResponse()` was changed to private access
* `validate()` was removed
* `withValidationResults(java.util.List)` was removed

#### `models.DpmErrorInfo` was modified

* `DpmErrorInfo()` was changed to private access
* `withErrorString(java.lang.String)` was removed
* `validate()` was removed
* `withRecommendations(java.util.List)` was removed

#### `models.InstantRPAdditionalDetails` was modified

* `validate()` was removed

#### `models.WorkloadItem` was modified

* `models.WorkloadItem withFriendlyName(java.lang.String)` -> `models.WorkloadItem withFriendlyName(java.lang.String)`
* `validate()` was removed
* `models.WorkloadItem withBackupManagementType(java.lang.String)` -> `models.WorkloadItem withBackupManagementType(java.lang.String)`
* `models.WorkloadItem withWorkloadType(java.lang.String)` -> `models.WorkloadItem withWorkloadType(java.lang.String)`
* `models.WorkloadItem withProtectionState(models.ProtectionStatus)` -> `models.WorkloadItem withProtectionState(models.ProtectionStatus)`

#### `models.HourlySchedule` was modified

* `validate()` was removed

#### `models.VaultJobExtendedInfo` was modified

* `VaultJobExtendedInfo()` was changed to private access
* `validate()` was removed
* `withPropertyBag(java.util.Map)` was removed

#### `models.RecoveryPointProperties` was modified

* `RecoveryPointProperties()` was changed to private access
* `withIsSoftDeleted(java.lang.Boolean)` was removed
* `validate()` was removed
* `withExpiryTime(java.lang.String)` was removed
* `withRuleName(java.lang.String)` was removed

#### `models.AzureWorkloadJob` was modified

* `AzureWorkloadJob()` was changed to private access
* `withEntityFriendlyName(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withActionsInfo(java.util.List)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `validate()` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withExtendedInfo(models.AzureWorkloadJobExtendedInfo)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withStatus(java.lang.String)` was removed
* `withDuration(java.time.Duration)` was removed
* `withActivityId(java.lang.String)` was removed
* `withErrorDetails(java.util.List)` was removed

#### `models.OperationResultInfoBase` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapAseDatabaseProtectableItem` was modified

* `AzureVmWorkloadSapAseDatabaseProtectableItem()` was changed to private access
* `withWorkloadType(java.lang.String)` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withIsProtectable(java.lang.Boolean)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withParentName(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `validate()` was removed
* `withServerName(java.lang.String)` was removed
* `withParentUniqueName(java.lang.String)` was removed

#### `models.DpmProtectedItemExtendedInfo` was modified

* `validate()` was removed

#### `models.AzureIaaSvmJob` was modified

* `AzureIaaSvmJob()` was changed to private access
* `withDuration(java.time.Duration)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withIsUserTriggered(java.lang.Boolean)` was removed
* `validate()` was removed
* `withEntityFriendlyName(java.lang.String)` was removed
* `withErrorDetails(java.util.List)` was removed
* `withActionsInfo(java.util.List)` was removed
* `withExtendedInfo(models.AzureIaaSvmJobExtendedInfo)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withContainerName(java.lang.String)` was removed
* `withVirtualMachineVersion(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withActivityId(java.lang.String)` was removed

#### `models.FetchTieringCostSavingsInfoForVaultRequest` was modified

* `validate()` was removed

#### `models.AzureWorkloadSapAsePointInTimeRecoveryPoint` was modified

* `AzureWorkloadSapAsePointInTimeRecoveryPoint()` was changed to private access
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed
* `withRecoveryPointTimeInUtc(java.time.OffsetDateTime)` was removed
* `withType(models.RestorePointType)` was removed
* `withRecoveryPointMoveReadinessInfo(java.util.Map)` was removed
* `withTimeRanges(java.util.List)` was removed
* `validate()` was removed

#### `models.AzureStorageErrorInfo` was modified

* `AzureStorageErrorInfo()` was changed to private access
* `validate()` was removed
* `withErrorCode(java.lang.Integer)` was removed
* `withRecommendations(java.util.List)` was removed
* `withErrorString(java.lang.String)` was removed

#### `models.ExtendedProperties` was modified

* `validate()` was removed

#### `models.DailyRetentionFormat` was modified

* `validate()` was removed

#### `models.SimpleRetentionPolicy` was modified

* `validate()` was removed

#### `models.BackupStatusRequest` was modified

* `validate()` was removed

#### `models.WorkloadInquiryDetails` was modified

* `validate()` was removed

#### `models.DpmJobExtendedInfo` was modified

* `DpmJobExtendedInfo()` was changed to private access
* `withDynamicErrorMessage(java.lang.String)` was removed
* `withTasksList(java.util.List)` was removed
* `validate()` was removed
* `withPropertyBag(java.util.Map)` was removed

#### `models.EncryptionDetails` was modified

* `validate()` was removed

#### `models.PreBackupValidation` was modified

* `PreBackupValidation()` was changed to private access
* `withStatus(models.InquiryStatus)` was removed
* `withCode(java.lang.String)` was removed
* `validate()` was removed
* `withMessage(java.lang.String)` was removed

#### `models.UserAssignedManagedIdentityDetails` was modified

* `validate()` was removed

#### `models.AzureIaaSvmProtectionPolicy` was modified

* `validate()` was removed

#### `models.LongTermRetentionPolicy` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapAseSystemWorkloadItem` was modified

* `AzureVmWorkloadSapAseSystemWorkloadItem()` was changed to private access
* `withFriendlyName(java.lang.String)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withSubWorkloadItemCount(java.lang.Integer)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `validate()` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withParentName(java.lang.String)` was removed
* `withServerName(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed

#### `models.IaasVmilrRegistrationRequest` was modified

* `validate()` was removed

#### `models.InquiryValidation` was modified

* `validate()` was removed

#### `models.DpmJobTaskDetails` was modified

* `DpmJobTaskDetails()` was changed to private access
* `withTaskId(java.lang.String)` was removed
* `validate()` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withDuration(java.time.Duration)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withStatus(java.lang.String)` was removed

#### `models.MabFileFolderProtectedItemExtendedInfo` was modified

* `validate()` was removed

#### `models.AzureSqlProtectedItemExtendedInfo` was modified

* `validate()` was removed

#### `models.DailyRetentionSchedule` was modified

* `validate()` was removed

#### `models.RecoveryPointDiskConfiguration` was modified

* `RecoveryPointDiskConfiguration()` was changed to private access
* `withExcludedDiskList(java.util.List)` was removed
* `validate()` was removed
* `withNumberOfDisksIncludedInBackup(java.lang.Integer)` was removed
* `withIncludedDiskList(java.util.List)` was removed
* `withNumberOfDisksAttachedToVm(java.lang.Integer)` was removed

#### `models.RestoreRequestResource` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed
* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.UnlockDeleteRequest` was modified

* `validate()` was removed

#### `models.AzureSqlProtectionPolicy` was modified

* `validate()` was removed

#### `models.ValidateIaasVMRestoreOperationRequest` was modified

* `validate()` was removed

#### `models.AzureStorageJob` was modified

* `AzureStorageJob()` was changed to private access
* `withStorageAccountName(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withEntityFriendlyName(java.lang.String)` was removed
* `withActionsInfo(java.util.List)` was removed
* `withActivityId(java.lang.String)` was removed
* `withExtendedInfo(models.AzureStorageJobExtendedInfo)` was removed
* `withStorageAccountVersion(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withDuration(java.time.Duration)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withIsUserTriggered(java.lang.Boolean)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withErrorDetails(java.util.List)` was removed
* `validate()` was removed

#### `models.WeeklyRetentionSchedule` was modified

* `validate()` was removed

#### `models.WeeklySchedule` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSqlDatabaseProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.RecoveryPointRehydrationInfo` was modified

* `validate()` was removed

#### `models.WorkloadProtectableItem` was modified

* `models.WorkloadProtectableItem withWorkloadType(java.lang.String)` -> `models.WorkloadProtectableItem withWorkloadType(java.lang.String)`
* `models.WorkloadProtectableItem withFriendlyName(java.lang.String)` -> `models.WorkloadProtectableItem withFriendlyName(java.lang.String)`
* `models.WorkloadProtectableItem withProtectionState(models.ProtectionStatus)` -> `models.WorkloadProtectableItem withProtectionState(models.ProtectionStatus)`
* `validate()` was removed
* `models.WorkloadProtectableItem withBackupManagementType(java.lang.String)` -> `models.WorkloadProtectableItem withBackupManagementType(java.lang.String)`

#### `models.SimpleSchedulePolicyV2` was modified

* `validate()` was removed

#### `models.VaultRetentionPolicy` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.AzureVmWorkloadItem` was modified

* `models.AzureVmWorkloadItem withSubinquireditemcount(java.lang.Integer)` -> `models.AzureVmWorkloadItem withSubinquireditemcount(java.lang.Integer)`
* `withBackupManagementType(java.lang.String)` was removed
* `validate()` was removed
* `models.AzureVmWorkloadItem withSubWorkloadItemCount(java.lang.Integer)` -> `models.AzureVmWorkloadItem withSubWorkloadItemCount(java.lang.Integer)`
* `withWorkloadType(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `models.AzureVmWorkloadItem withIsAutoProtectable(java.lang.Boolean)` -> `models.AzureVmWorkloadItem withIsAutoProtectable(java.lang.Boolean)`
* `models.AzureVmWorkloadItem withParentName(java.lang.String)` -> `models.AzureVmWorkloadItem withParentName(java.lang.String)`
* `models.AzureVmWorkloadItem withServerName(java.lang.String)` -> `models.AzureVmWorkloadItem withServerName(java.lang.String)`

#### `models.AzureVmWorkloadSqlInstanceWorkloadItem` was modified

* `AzureVmWorkloadSqlInstanceWorkloadItem()` was changed to private access
* `withFriendlyName(java.lang.String)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withServerName(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withSubWorkloadItemCount(java.lang.Integer)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withParentName(java.lang.String)` was removed
* `validate()` was removed
* `withDataDirectoryPaths(java.util.List)` was removed

#### `models.IdentityBasedRestoreDetails` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadProtectionPolicy` was modified

* `validate()` was removed

#### `models.SchedulePolicy` was modified

* `validate()` was removed

#### `models.ProtectionIntent` was modified

* `validate()` was removed

#### `models.AzureResourceProtectionIntent` was modified

* `validate()` was removed

#### `models.SecurityPinBase` was modified

* `validate()` was removed

#### `models.ProtectedItem` was modified

* `validate()` was removed
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `softDeleteRetentionPeriod()` was removed

#### `models.AzureSqlContainer` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapHanaSystemProtectableItem` was modified

* `AzureVmWorkloadSapHanaSystemProtectableItem()` was changed to private access
* `withBackupManagementType(java.lang.String)` was removed
* `withIsProtectable(java.lang.Boolean)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `withParentUniqueName(java.lang.String)` was removed
* `withServerName(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `validate()` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed
* `withParentName(java.lang.String)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.AzureSqlagWorkloadContainerProtectionContainer` was modified

* `validate()` was removed

#### `models.RecoveryPointTierInformationV2` was modified

* `RecoveryPointTierInformationV2()` was changed to private access
* `withStatus(models.RecoveryPointTierStatus)` was removed
* `withType(models.RecoveryPointTierType)` was removed
* `withExtendedInfo(java.util.Map)` was removed
* `validate()` was removed

#### `models.BackupResourceEncryptionConfigResource` was modified

* `validate()` was removed
* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.AzureWorkloadSqlRecoveryPointExtendedInfo` was modified

* `AzureWorkloadSqlRecoveryPointExtendedInfo()` was changed to private access
* `withDataDirectoryPaths(java.util.List)` was removed
* `validate()` was removed
* `withDataDirectoryTimeInUtc(java.time.OffsetDateTime)` was removed

#### `models.AzureIaaSvmJobTaskDetails` was modified

* `AzureIaaSvmJobTaskDetails()` was changed to private access
* `validate()` was removed
* `withDuration(java.time.Duration)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withStatus(java.lang.String)` was removed
* `withInstanceId(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withProgressPercentage(java.lang.Double)` was removed
* `withTaskId(java.lang.String)` was removed
* `withTaskExecutionDetails(java.lang.String)` was removed

#### `models.InquiryInfo` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapAseDatabaseWorkloadItem` was modified

* `AzureVmWorkloadSapAseDatabaseWorkloadItem()` was changed to private access
* `withWorkloadType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withServerName(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withParentName(java.lang.String)` was removed
* `withSubWorkloadItemCount(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.KekDetails` was modified

* `KekDetails()` was changed to private access
* `withKeyUrl(java.lang.String)` was removed
* `withKeyBackupData(java.lang.String)` was removed
* `withKeyVaultId(java.lang.String)` was removed
* `validate()` was removed

#### `models.MabJobExtendedInfo` was modified

* `MabJobExtendedInfo()` was changed to private access
* `withPropertyBag(java.util.Map)` was removed
* `validate()` was removed
* `withDynamicErrorMessage(java.lang.String)` was removed
* `withTasksList(java.util.List)` was removed

#### `models.TriggerDataMoveRequest` was modified

* `validate()` was removed

#### `models.AzureWorkloadRestoreRequest` was modified

* `validate()` was removed

#### `models.VaultJob` was modified

* `VaultJob()` was changed to private access
* `withOperation(java.lang.String)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withExtendedInfo(models.VaultJobExtendedInfo)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withStatus(java.lang.String)` was removed
* `withDuration(java.time.Duration)` was removed
* `withErrorDetails(java.util.List)` was removed
* `validate()` was removed
* `withActivityId(java.lang.String)` was removed
* `withEntityFriendlyName(java.lang.String)` was removed
* `withActionsInfo(java.util.List)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed

#### `models.IlrRequest` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSqlDatabaseWorkloadItem` was modified

* `AzureVmWorkloadSqlDatabaseWorkloadItem()` was changed to private access
* `withBackupManagementType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withSubWorkloadItemCount(java.lang.Integer)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withParentName(java.lang.String)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withServerName(java.lang.String)` was removed
* `validate()` was removed

#### `models.AzureVmWorkloadProtectableItem` was modified

* `models.AzureVmWorkloadProtectableItem withParentUniqueName(java.lang.String)` -> `models.AzureVmWorkloadProtectableItem withParentUniqueName(java.lang.String)`
* `withProtectionState(models.ProtectionStatus)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `models.AzureVmWorkloadProtectableItem withPrebackupvalidation(models.PreBackupValidation)` -> `models.AzureVmWorkloadProtectableItem withPrebackupvalidation(models.PreBackupValidation)`
* `models.AzureVmWorkloadProtectableItem withSubprotectableitemcount(java.lang.Integer)` -> `models.AzureVmWorkloadProtectableItem withSubprotectableitemcount(java.lang.Integer)`
* `withWorkloadType(java.lang.String)` was removed
* `validate()` was removed
* `models.AzureVmWorkloadProtectableItem withIsAutoProtectable(java.lang.Boolean)` -> `models.AzureVmWorkloadProtectableItem withIsAutoProtectable(java.lang.Boolean)`
* `models.AzureVmWorkloadProtectableItem withParentName(java.lang.String)` -> `models.AzureVmWorkloadProtectableItem withParentName(java.lang.String)`
* `models.AzureVmWorkloadProtectableItem withIsAutoProtected(java.lang.Boolean)` -> `models.AzureVmWorkloadProtectableItem withIsAutoProtected(java.lang.Boolean)`
* `models.AzureVmWorkloadProtectableItem withSubinquireditemcount(java.lang.Integer)` -> `models.AzureVmWorkloadProtectableItem withSubinquireditemcount(java.lang.Integer)`
* `models.AzureVmWorkloadProtectableItem withServerName(java.lang.String)` -> `models.AzureVmWorkloadProtectableItem withServerName(java.lang.String)`
* `models.AzureVmWorkloadProtectableItem withIsProtectable(java.lang.Boolean)` -> `models.AzureVmWorkloadProtectableItem withIsProtectable(java.lang.Boolean)`

#### `models.SecuredVMDetails` was modified

* `validate()` was removed

#### `models.GenericContainer` was modified

* `validate()` was removed

#### `models.ResourceProviders` was modified

* `void bmsTriggerDataMove(java.lang.String,java.lang.String,models.TriggerDataMoveRequest)` -> `models.OkResponse bmsTriggerDataMove(java.lang.String,java.lang.String,models.TriggerDataMoveRequest)`
* `void bmsPrepareDataMove(java.lang.String,java.lang.String,models.PrepareDataMoveRequest,com.azure.core.util.Context)` -> `models.OkResponse bmsPrepareDataMove(java.lang.String,java.lang.String,models.PrepareDataMoveRequest,com.azure.core.util.Context)`
* `void bmsPrepareDataMove(java.lang.String,java.lang.String,models.PrepareDataMoveRequest)` -> `models.OkResponse bmsPrepareDataMove(java.lang.String,java.lang.String,models.PrepareDataMoveRequest)`
* `void bmsTriggerDataMove(java.lang.String,java.lang.String,models.TriggerDataMoveRequest,com.azure.core.util.Context)` -> `models.OkResponse bmsTriggerDataMove(java.lang.String,java.lang.String,models.TriggerDataMoveRequest,com.azure.core.util.Context)`

#### `models.AzureVMAppContainerProtectionContainer` was modified

* `validate()` was removed

#### `models.ClientDiscoveryDisplay` was modified

* `ClientDiscoveryDisplay()` was changed to private access
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `validate()` was removed

#### `models.TieringCostRehydrationInfo` was modified

* `TieringCostRehydrationInfo()` was changed to private access
* `withRetailRehydrationCostPerGBPerMonth(double)` was removed
* `withRehydrationSizeInBytes(long)` was removed
* `validate()` was removed

#### `models.IaasVMRecoveryPoint` was modified

* `IaasVMRecoveryPoint()` was changed to private access
* `withOsType(java.lang.String)` was removed
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withRecoveryPointType(java.lang.String)` was removed
* `withRecoveryPointDiskConfiguration(models.RecoveryPointDiskConfiguration)` was removed
* `withIsInstantIlrSessionActive(java.lang.Boolean)` was removed
* `withOriginalStorageAccountOption(java.lang.Boolean)` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed
* `validate()` was removed
* `withRecoveryPointAdditionalInfo(java.lang.String)` was removed
* `withSecurityType(java.lang.String)` was removed
* `withSourceVMStorageType(java.lang.String)` was removed
* `withIsSourceVMEncrypted(java.lang.Boolean)` was removed
* `withIsPrivateAccessEnabledOnAnyDisk(java.lang.Boolean)` was removed
* `withVirtualMachineSize(java.lang.String)` was removed
* `withExtendedLocation(models.ExtendedLocation)` was removed
* `withZones(java.util.List)` was removed
* `withKeyAndSecret(models.KeyAndSecretDetails)` was removed
* `withRecoveryPointMoveReadinessInfo(java.util.Map)` was removed
* `withIsManagedVirtualMachine(java.lang.Boolean)` was removed
* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed

#### `models.ClientDiscoveryForProperties` was modified

* `ClientDiscoveryForProperties()` was changed to private access
* `withServiceSpecification(models.ClientDiscoveryForServiceSpecification)` was removed
* `validate()` was removed

#### `models.IlrRequestResource` was modified

* `withTags(java.util.Map)` was removed
* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.AzureVmWorkloadSapHanaDBInstanceProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.AzureBackupServerContainer` was modified

* `validate()` was removed

#### `models.AzureBackupServerEngine` was modified

* `AzureBackupServerEngine()` was changed to private access
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withIsAzureBackupAgentUpgradeAvailable(java.lang.Boolean)` was removed
* `withBackupEngineState(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withDpmVersion(java.lang.String)` was removed
* `withHealthStatus(java.lang.String)` was removed
* `withBackupEngineId(java.lang.String)` was removed
* `withExtendedInfo(models.BackupEngineExtendedInfo)` was removed
* `withCanReRegister(java.lang.Boolean)` was removed
* `withAzureBackupAgentVersion(java.lang.String)` was removed
* `withIsDpmUpgradeAvailable(java.lang.Boolean)` was removed
* `withRegistrationStatus(java.lang.String)` was removed
* `validate()` was removed

#### `models.DailySchedule` was modified

* `validate()` was removed

#### `models.AzureIaaSClassicComputeVMProtectableItem` was modified

* `AzureIaaSClassicComputeVMProtectableItem()` was changed to private access
* `withBackupManagementType(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withResourceGroup(java.lang.String)` was removed
* `withVirtualMachineVersion(java.lang.String)` was removed
* `validate()` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withVirtualMachineId(java.lang.String)` was removed

#### `models.ExportJobsOperationResultInfo` was modified

* `ExportJobsOperationResultInfo()` was changed to private access
* `withBlobSasKey(java.lang.String)` was removed
* `withExcelFileBlobSasKey(java.lang.String)` was removed
* `validate()` was removed
* `withBlobUrl(java.lang.String)` was removed
* `withExcelFileBlobUrl(java.lang.String)` was removed

#### `models.TieringPolicy` was modified

* `validate()` was removed

#### `models.ValidateOperationRequestResource` was modified

* `validate()` was removed

#### `models.AzureFileShareRecoveryPoint` was modified

* `AzureFileShareRecoveryPoint()` was changed to private access
* `withRecoveryPointSizeInGB(java.lang.Integer)` was removed
* `withRecoveryPointType(java.lang.String)` was removed
* `withFileShareSnapshotUri(java.lang.String)` was removed
* `validate()` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed

#### `models.OperationStatusJobExtendedInfo` was modified

* `OperationStatusJobExtendedInfo()` was changed to private access
* `validate()` was removed
* `withJobId(java.lang.String)` was removed

#### `models.ResourceList` was modified

* `models.ResourceList withNextLink(java.lang.String)` -> `models.ResourceList withNextLink(java.lang.String)`
* `validate()` was removed

#### `models.AzureWorkloadBackupRequest` was modified

* `validate()` was removed

#### `models.WorkloadItemResource` was modified

* `etag()` was removed

#### `models.AzureWorkloadContainer` was modified

* `validate()` was removed

#### `models.ResourceGuardProxyBase` was modified

* `validate()` was removed

#### `models.FeatureSupportRequest` was modified

* `validate()` was removed

#### `models.ValidateRestoreOperationRequest` was modified

* `validate()` was removed

#### `models.RecoveryPointTierInformation` was modified

* `models.RecoveryPointTierInformation withStatus(models.RecoveryPointTierStatus)` -> `models.RecoveryPointTierInformation withStatus(models.RecoveryPointTierStatus)`
* `validate()` was removed
* `models.RecoveryPointTierInformation withType(models.RecoveryPointTierType)` -> `models.RecoveryPointTierInformation withType(models.RecoveryPointTierType)`
* `models.RecoveryPointTierInformation withExtendedInfo(java.util.Map)` -> `models.RecoveryPointTierInformation withExtendedInfo(java.util.Map)`

#### `models.AzureWorkloadAutoProtectionIntent` was modified

* `validate()` was removed

#### `models.FetchTieringCostInfoRequest` was modified

* `validate()` was removed

#### `models.Day` was modified

* `validate()` was removed

#### `models.DpmJob` was modified

* `DpmJob()` was changed to private access
* `withActivityId(java.lang.String)` was removed
* `withErrorDetails(java.util.List)` was removed
* `withEntityFriendlyName(java.lang.String)` was removed
* `withExtendedInfo(models.DpmJobExtendedInfo)` was removed
* `withOperation(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withContainerType(java.lang.String)` was removed
* `withContainerName(java.lang.String)` was removed
* `validate()` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withActionsInfo(java.util.List)` was removed
* `withDpmServerName(java.lang.String)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withDuration(java.time.Duration)` was removed

#### `models.AzureVMResourceFeatureSupportRequest` was modified

* `validate()` was removed

#### `models.BackupEngineExtendedInfo` was modified

* `BackupEngineExtendedInfo()` was changed to private access
* `withRefreshedAt(java.time.OffsetDateTime)` was removed
* `withAzureProtectedInstances(java.lang.Integer)` was removed
* `withProtectedItemsCount(java.lang.Integer)` was removed
* `withProtectedServersCount(java.lang.Integer)` was removed
* `withDiskCount(java.lang.Integer)` was removed
* `withUsedDiskSpace(java.lang.Double)` was removed
* `withAvailableDiskSpace(java.lang.Double)` was removed
* `validate()` was removed
* `withDatabaseName(java.lang.String)` was removed

#### `models.AzureIaaSvmHealthDetails` was modified

* `validate()` was removed

#### `models.RecoveryPointTierStatus` was modified

* `valueOf(java.lang.String)` was removed
* `toString()` was removed
* `models.RecoveryPointTierStatus[] values()` -> `java.util.Collection values()`

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.SnapshotRestoreParameters` was modified

* `validate()` was removed

#### `models.AzureIaaSClassicComputeVMProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `RecoveryServicesBackupManager` was modified

* `fluent.RecoveryServicesBackupClient serviceClient()` -> `fluent.RecoveryServicesBackupManagementClient serviceClient()`
* `resourceGuardProxies()` was removed

#### `models.GenericProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.VaultJobErrorInfo` was modified

* `VaultJobErrorInfo()` was changed to private access
* `withRecommendations(java.util.List)` was removed
* `validate()` was removed
* `withErrorCode(java.lang.Integer)` was removed
* `withErrorString(java.lang.String)` was removed

#### `models.AzureVmWorkloadProtectedItemExtendedInfo` was modified

* `validate()` was removed

#### `models.AzureWorkloadSapHanaRecoveryPoint` was modified

* `AzureWorkloadSapHanaRecoveryPoint()` was changed to private access
* `validate()` was removed
* `withRecoveryPointMoveReadinessInfo(java.util.Map)` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed
* `withRecoveryPointTimeInUtc(java.time.OffsetDateTime)` was removed
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withType(models.RestorePointType)` was removed

#### `models.OperationStatusJobsExtendedInfo` was modified

* `OperationStatusJobsExtendedInfo()` was changed to private access
* `withFailedJobsError(java.util.Map)` was removed
* `withJobIds(java.util.List)` was removed
* `validate()` was removed

#### `models.RecoveryPoint` was modified

* `validate()` was removed

#### `models.OperationStatusError` was modified

* `OperationStatusError()` was changed to private access
* `withMessage(java.lang.String)` was removed
* `validate()` was removed
* `withCode(java.lang.String)` was removed

#### `models.PrivateEndpointConnection` was modified

* `validate()` was removed

#### `models.AzureBackupGoalFeatureSupportRequest` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSqlDatabaseProtectableItem` was modified

* `AzureVmWorkloadSqlDatabaseProtectableItem()` was changed to private access
* `withServerName(java.lang.String)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withParentName(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `withParentUniqueName(java.lang.String)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `withIsProtectable(java.lang.Boolean)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `validate()` was removed

#### `models.SimpleSchedulePolicy` was modified

* `validate()` was removed

#### `models.SqlDataDirectoryMapping` was modified

* `validate()` was removed

#### `models.AzureVmWorkloadSapAseSystemProtectableItem` was modified

* `AzureVmWorkloadSapAseSystemProtectableItem()` was changed to private access
* `validate()` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `withParentUniqueName(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withParentName(java.lang.String)` was removed
* `withServerName(java.lang.String)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `withIsProtectable(java.lang.Boolean)` was removed

#### `models.ClientScriptForConnect` was modified

* `ClientScriptForConnect()` was changed to private access
* `withUrl(java.lang.String)` was removed
* `withScriptExtension(java.lang.String)` was removed
* `withScriptNameSuffix(java.lang.String)` was removed
* `withScriptContent(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `validate()` was removed

#### `models.MabProtectionPolicy` was modified

* `validate()` was removed

#### `models.SnapshotBackupAdditionalDetails` was modified

* `validate()` was removed

#### `models.AzureFileshareProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.AzureStorageJobExtendedInfo` was modified

* `AzureStorageJobExtendedInfo()` was changed to private access
* `withTasksList(java.util.List)` was removed
* `withPropertyBag(java.util.Map)` was removed
* `validate()` was removed
* `withDynamicErrorMessage(java.lang.String)` was removed

#### `models.Job` was modified

* `models.Job withBackupManagementType(models.BackupManagementType)` -> `models.Job withBackupManagementType(models.BackupManagementType)`
* `models.Job withStartTime(java.time.OffsetDateTime)` -> `models.Job withStartTime(java.time.OffsetDateTime)`
* `models.Job withEndTime(java.time.OffsetDateTime)` -> `models.Job withEndTime(java.time.OffsetDateTime)`
* `models.Job withStatus(java.lang.String)` -> `models.Job withStatus(java.lang.String)`
* `models.Job withOperation(java.lang.String)` -> `models.Job withOperation(java.lang.String)`
* `validate()` was removed
* `models.Job withEntityFriendlyName(java.lang.String)` -> `models.Job withEntityFriendlyName(java.lang.String)`
* `models.Job withActivityId(java.lang.String)` -> `models.Job withActivityId(java.lang.String)`

#### `models.PreValidateEnableBackupRequest` was modified

* `validate()` was removed

#### `models.KpiResourceHealthDetails` was modified

* `validate()` was removed

#### `models.WeeklyRetentionFormat` was modified

* `validate()` was removed

#### `models.DistributedNodesInfo` was modified

* `validate()` was removed

#### `models.AzureWorkloadJobExtendedInfo` was modified

* `AzureWorkloadJobExtendedInfo()` was changed to private access
* `withPropertyBag(java.util.Map)` was removed
* `withDynamicErrorMessage(java.lang.String)` was removed
* `withTasksList(java.util.List)` was removed
* `validate()` was removed

#### `models.AzureRecoveryServiceVaultProtectionIntent` was modified

* `validate()` was removed

#### `models.AzureFileshareProtectedItemExtendedInfo` was modified

* `validate()` was removed

#### `models.AzureFileShareProtectionPolicy` was modified

* `validate()` was removed

#### `models.DiskExclusionProperties` was modified

* `validate()` was removed

#### `models.FetchTieringCostSavingsInfoForProtectedItemRequest` was modified

* `validate()` was removed

#### `models.AzureSqlProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.BackupRequestResource` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed
* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.AzureIaaSvmProtectedItemExtendedInfo` was modified

* `validate()` was removed

#### `models.OperationResultInfo` was modified

* `OperationResultInfo()` was changed to private access
* `validate()` was removed
* `withJobList(java.util.List)` was removed

#### `models.ClientDiscoveryForLogSpecification` was modified

* `ClientDiscoveryForLogSpecification()` was changed to private access
* `withBlobDuration(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed

#### `models.AzureWorkloadSapHanaRestoreRequest` was modified

* `validate()` was removed

#### `models.LogSchedulePolicy` was modified

* `validate()` was removed

#### `models.AzureWorkloadSqlPointInTimeRestoreWithRehydrateRequest` was modified

* `validate()` was removed

#### `models.ExtendedLocation` was modified

* `validate()` was removed

#### `models.DiskInformation` was modified

* `DiskInformation()` was changed to private access
* `withName(java.lang.String)` was removed
* `validate()` was removed
* `withLun(java.lang.Integer)` was removed

#### `models.AzureWorkloadPointInTimeRestoreRequest` was modified

* `validate()` was removed

#### `models.AzureWorkloadSqlRestoreRequest` was modified

* `validate()` was removed

#### `models.AzureWorkloadSapAseRecoveryPoint` was modified

* `AzureWorkloadSapAseRecoveryPoint()` was changed to private access
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was removed
* `withRecoveryPointTierDetails(java.util.List)` was removed
* `withType(models.RestorePointType)` was removed
* `validate()` was removed
* `withRecoveryPointMoveReadinessInfo(java.util.Map)` was removed
* `withRecoveryPointTimeInUtc(java.time.OffsetDateTime)` was removed

#### `models.BekDetails` was modified

* `BekDetails()` was changed to private access
* `withSecretUrl(java.lang.String)` was removed
* `withSecretData(java.lang.String)` was removed
* `validate()` was removed
* `withSecretVaultId(java.lang.String)` was removed

#### `models.AzureIaaSvmJobV2` was modified

* `AzureIaaSvmJobV2()` was changed to private access
* `withVirtualMachineVersion(java.lang.String)` was removed
* `withActivityId(java.lang.String)` was removed
* `withContainerName(java.lang.String)` was removed
* `withEntityFriendlyName(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withExtendedInfo(models.AzureIaaSvmJobExtendedInfo)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `validate()` was removed
* `withErrorDetails(java.util.List)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withStatus(java.lang.String)` was removed
* `withActionsInfo(java.util.List)` was removed
* `withDuration(java.time.Duration)` was removed

#### `models.PrepareDataMoveResponse` was modified

* `PrepareDataMoveResponse()` was changed to private access
* `withCorrelationId(java.lang.String)` was removed
* `withSourceVaultProperties(java.util.Map)` was removed
* `validate()` was removed

#### `models.AzureStorageContainer` was modified

* `validate()` was removed

#### `models.TieringCostSavingInfo` was modified

* `TieringCostSavingInfo()` was changed to private access
* `withSourceTierSizeReductionInBytes(long)` was removed
* `withRetailSourceTierCostPerGBPerMonth(double)` was removed
* `withTargetTierSizeIncreaseInBytes(long)` was removed
* `validate()` was removed
* `withRetailTargetTierCostPerGBPerMonth(double)` was removed

#### `models.FetchTieringCostSavingsInfoForPolicyRequest` was modified

* `validate()` was removed

#### `models.ProtectableContainerResource` was modified

* `etag()` was removed

#### `models.MabContainerHealthDetails` was modified

* `validate()` was removed

#### `models.TargetAfsRestoreInfo` was modified

* `validate()` was removed

#### `models.AzureIaaSComputeVMContainer` was modified

* `validate()` was removed

#### `models.DpmContainer` was modified

* `validate()` was removed

#### `models.BackupResourceConfig` was modified

* `validate()` was removed

#### `models.DpmProtectedItem` was modified

* `validate()` was removed
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was removed

#### `models.AzureVmWorkloadSapHanaDatabaseProtectableItem` was modified

* `AzureVmWorkloadSapHanaDatabaseProtectableItem()` was changed to private access
* `withServerName(java.lang.String)` was removed
* `withSubprotectableitemcount(java.lang.Integer)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withProtectionState(models.ProtectionStatus)` was removed
* `withParentName(java.lang.String)` was removed
* `validate()` was removed
* `withIsAutoProtectable(java.lang.Boolean)` was removed
* `withIsProtectable(java.lang.Boolean)` was removed
* `withSubinquireditemcount(java.lang.Integer)` was removed
* `withIsAutoProtected(java.lang.Boolean)` was removed
* `withWorkloadType(java.lang.String)` was removed
* `withParentUniqueName(java.lang.String)` was removed
* `withBackupManagementType(java.lang.String)` was removed
* `withPrebackupvalidation(models.PreBackupValidation)` was removed

### Features Added

* `models.DatabaseInRP` was added

* `models.ThreatInfo` was added

* `models.IaasVmProtectableItem` was added

* `models.InstanceProtectionReadiness` was added

* `models.SourceSideScanSummary` was added

* `models.OkResponse` was added

* `models.VMWorkloadPolicyType` was added

* `models.ProtectionLevel` was added

* `models.AzureVmWorkloadSAPHanaScaleoutProtectableItem` was added

* `models.ThreatStatus` was added

* `models.ThreatSeverity` was added

* `models.SourceSideScanStatus` was added

* `models.SourceSideScanInfo` was added

* `models.UpdateRecoveryPointRequest` was added

* `models.ThreatState` was added

* `models.PatchRecoveryPointPropertiesInput` was added

* `models.AzureVmWorkloadSQLInstanceProtectedItem` was added

* `models.PatchRecoveryPointInput` was added

#### `models.AzureIaaSComputeVMProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

#### `models.MabFileFolderProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

#### `models.RecoveryPoints` was modified

* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.UpdateRecoveryPointRequest,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.UpdateRecoveryPointRequest)` was added

#### `models.AzureVmWorkloadSapHanaDatabaseProtectedItem` was modified

* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added
* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added

#### `models.BackupResourceVaultConfigResource` was modified

* `systemData()` was added

#### `models.AzureVmWorkloadSapAseDatabaseProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

#### `models.BackupEngineBaseResource` was modified

* `systemData()` was added

#### `models.BackupResourceEncryptionConfigExtendedResource` was modified

* `systemData()` was added

#### `models.ProtectedItemResource` was modified

* `systemData()` was added

#### `models.AzureIaaSvmProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

#### `models.WorkloadProtectableItemResource` was modified

* `systemData()` was added
* `eTag()` was added

#### `models.RecoveryPointResource` was modified

* `systemData()` was added

#### `models.PrivateEndpointConnectionResource` was modified

* `systemData()` was added

#### `models.RestoreRequestResource` was modified

* `systemData()` was added
* `withETag(java.lang.String)` was added
* `location()` was added
* `eTag()` was added
* `tags()` was added

#### `models.AzureVmWorkloadSqlDatabaseProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `protectionLevel()` was added
* `withParentProtectedItem(java.lang.String)` was added
* `parentProtectedItem()` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added
* `withProtectionLevel(models.ProtectionLevel)` was added

#### `models.AzureVmWorkloadProtectedItem` was modified

* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added
* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added

#### `models.ResourceGuardProxyBaseResource` was modified

* `systemData()` was added

#### `models.AzureVmWorkloadProtectionPolicy` was modified

* `vmWorkloadPolicyType()` was added
* `withVmWorkloadPolicyType(models.VMWorkloadPolicyType)` was added

#### `models.ProtectedItem` was modified

* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added
* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `softDeleteRetentionPeriodInDays()` was added
* `sourceSideScanInfo()` was added

#### `models.RecoveryPointTierInformationV2` was modified

* `extendedInfo()` was added
* `status()` was added
* `type()` was added

#### `models.BackupResourceEncryptionConfigResource` was modified

* `systemData()` was added
* `location()` was added
* `tags()` was added

#### `models.AzureWorkloadSqlRecoveryPointExtendedInfo` was modified

* `includedDatabases()` was added

#### `models.IlrRequestResource` was modified

* `location()` was added
* `eTag()` was added
* `systemData()` was added
* `withETag(java.lang.String)` was added
* `tags()` was added

#### `models.AzureVmWorkloadSapHanaDBInstanceProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

#### `models.WorkloadItemResource` was modified

* `systemData()` was added
* `eTag()` was added

#### `models.RecoveryPointTierStatus` was modified

* `RecoveryPointTierStatus()` was added

#### `models.AzureIaaSClassicComputeVMProtectedItem` was modified

* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added
* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added

#### `models.GenericProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

#### `models.RecoveryPoint` was modified

* `threatStatus()` was added
* `threatInfo()` was added

#### `models.JobResource` was modified

* `systemData()` was added

#### `models.AzureFileshareProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

#### `models.ProtectionIntentResource` was modified

* `systemData()` was added

#### `models.ProtectionContainerResource` was modified

* `systemData()` was added

#### `models.AzureSqlProtectedItem` was modified

* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added
* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added

#### `models.BackupRequestResource` was modified

* `withETag(java.lang.String)` was added
* `eTag()` was added
* `tags()` was added
* `location()` was added
* `systemData()` was added

#### `models.BackupResourceConfigResource` was modified

* `systemData()` was added

#### `models.ProtectionPolicyResource` was modified

* `systemData()` was added

#### `models.ProtectableContainerResource` was modified

* `eTag()` was added
* `systemData()` was added

#### `models.ResourceGuardProxyOperations` was modified

* `list(java.lang.String,java.lang.String)` was added
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DpmProtectedItem` was modified

* `withSourceSideScanInfo(models.SourceSideScanInfo)` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

## 1.6.0 (2025-04-17)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2025-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AzureWorkloadSapAsePointInTimeRestoreRequest` was added

* `models.AzureWorkloadSapAseRestoreRequest` was added

* `models.AzureVmWorkloadSapAseDatabaseProtectableItem` was added

* `models.AzureWorkloadSapAsePointInTimeRecoveryPoint` was added

* `models.AzureWorkloadSapAseRecoveryPoint` was added

#### `models.AzureIaaSvmProtectedItem` was modified

* `policyType()` was added

#### `models.AzureFileShareRecoveryPoint` was modified

* `recoveryPointTierDetails()` was added
* `withRecoveryPointTierDetails(java.util.List)` was added

#### `models.AzureStorageContainer` was modified

* `operationType()` was added
* `withOperationType(models.OperationType)` was added

## 1.5.0 (2024-12-19)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.RestoreRequestResource` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.BackupResourceEncryptionConfigResource` was modified

* `id()` was added
* `type()` was added
* `name()` was added

#### `models.IlrRequestResource` was modified

* `type()` was added
* `id()` was added
* `name()` was added

#### `models.AzureIaaSvmHealthDetails` was modified

* `message()` was added
* `recommendations()` was added
* `title()` was added
* `code()` was added

#### `models.BackupRequestResource` was modified

* `id()` was added
* `name()` was added
* `type()` was added

## 1.4.0 (2024-05-23)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

- IMPORTANT: use `AuxiliaryAuthenticationPolicy` from "azure-resourcemanager-resources" module via `RecoveryServicesBackupManager.configure().withPolicy(..)`, for setting "x-ms-authorization-auxiliary" header.

### Features Added

* `models.IaasVMSnapshotConsistencyType` was added

#### `models.AzureWorkloadSqlPointInTimeRecoveryPoint` was modified

* `objectType()` was added

#### `models.AzureIaaSClassicComputeVMContainer` was modified

* `containerType()` was added

#### `models.IaaSvmContainer` was modified

* `containerType()` was added

#### `models.OperationStatusExtendedInfo` was modified

* `objectType()` was added

#### `models.AzureIaaSComputeVMProtectedItem` was modified

* `protectedItemType()` was added

#### `models.AzureWorkloadPointInTimeRecoveryPoint` was modified

* `objectType()` was added

#### `models.AzureFileShareProvisionIlrRequest` was modified

* `objectType()` was added

#### `models.AzureVmWorkloadSapHanaHsr` was modified

* `protectableItemType()` was added

#### `models.AzureWorkloadRecoveryPoint` was modified

* `objectType()` was added

#### `models.OperationStatusValidateOperationExtendedInfo` was modified

* `objectType()` was added

#### `models.AzureVMAppContainerProtectableContainer` was modified

* `protectableContainerType()` was added

#### `models.AzureVmWorkloadSapHanaSystemWorkloadItem` was modified

* `workloadItemType()` was added

#### `models.IaasVMRestoreRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.OperationStatusProvisionIlrExtendedInfo` was modified

* `objectType()` was added

#### `models.AzureVmWorkloadSapHanaDatabaseWorkloadItem` was modified

* `workloadItemType()` was added

#### `models.MabFileFolderProtectedItem` was modified

* `protectedItemType()` was added

#### `models.GenericRecoveryPoint` was modified

* `objectType()` was added

#### `models.IaasVMBackupRequest` was modified

* `objectType()` was added

#### `models.DpmBackupEngine` was modified

* `backupEngineType()` was added

#### `models.AzureWorkloadSqlRecoveryPoint` was modified

* `objectType()` was added

#### `models.AzureStorageProtectableContainer` was modified

* `protectableContainerType()` was added

#### `models.AzureVmWorkloadSapHanaDBInstance` was modified

* `protectableItemType()` was added

#### `models.FetchTieringCostInfoForRehydrationRequest` was modified

* `objectType()` was added

#### `models.BackupEngineBase` was modified

* `backupEngineType()` was added

#### `models.RestoreRequest` was modified

* `objectType()` was added
* `resourceGuardOperationRequests()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added

#### `models.ProtectableContainer` was modified

* `protectableContainerType()` was added

#### `models.AzureFileShareBackupRequest` was modified

* `objectType()` was added

#### `models.AzureWorkloadSapHanaPointInTimeRestoreWithRehydrateRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.AzureVmWorkloadSapHanaDatabaseProtectedItem` was modified

* `protectedItemType()` was added

#### `models.LongTermSchedulePolicy` was modified

* `schedulePolicyType()` was added

#### `models.AzureVmWorkloadSqlInstanceProtectableItem` was modified

* `protectableItemType()` was added

#### `models.AzureVmWorkloadSqlAvailabilityGroupProtectableItem` was modified

* `protectableItemType()` was added

#### `models.AzureVmWorkloadSapAseDatabaseProtectedItem` was modified

* `protectedItemType()` was added

#### `models.AzureFileShareProtectableItem` was modified

* `protectableItemType()` was added

#### `models.AzureWorkloadSqlRestoreWithRehydrateRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.MabContainer` was modified

* `containerType()` was added

#### `models.GenericProtectionPolicy` was modified

* `backupManagementType()` was added

#### `models.IaasVMRestoreWithRehydrationRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.ValidateOperationRequest` was modified

* `objectType()` was added

#### `models.AzureWorkloadSapHanaPointInTimeRestoreRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.AzureWorkloadSqlPointInTimeRestoreRequest` was modified

* `objectType()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added

#### `models.AzureWorkloadSqlAutoProtectionIntent` was modified

* `protectionIntentItemType()` was added

#### `models.BackupRequest` was modified

* `objectType()` was added

#### `models.TieringCostInfo` was modified

* `objectType()` was added

#### `models.MabJob` was modified

* `jobType()` was added

#### `models.AzureFileShareRestoreRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.ProtectionContainer` was modified

* `containerType()` was added

#### `models.AzureWorkloadSapHanaPointInTimeRecoveryPoint` was modified

* `objectType()` was added

#### `models.AzureIaaSComputeVMProtectableItem` was modified

* `protectableItemType()` was added

#### `models.RetentionPolicy` was modified

* `retentionPolicyType()` was added

#### `models.AzureWorkloadSapHanaRestoreWithRehydrateRequest` was modified

* `objectType()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added

#### `models.AzureIaaSvmProtectedItem` was modified

* `protectedItemType()` was added

#### `models.AzureWorkloadContainerAutoProtectionIntent` was modified

* `protectionIntentItemType()` was added

#### `models.ProtectionPolicy` was modified

* `backupManagementType()` was added

#### `models.WorkloadItem` was modified

* `workloadItemType()` was added

#### `models.AzureWorkloadJob` was modified

* `jobType()` was added

#### `models.OperationResultInfoBase` was modified

* `objectType()` was added

#### `models.AzureIaaSvmJob` was modified

* `jobType()` was added

#### `models.FetchTieringCostSavingsInfoForVaultRequest` was modified

* `objectType()` was added

#### `models.SimpleRetentionPolicy` was modified

* `retentionPolicyType()` was added

#### `models.AzureIaaSvmProtectionPolicy` was modified

* `snapshotConsistencyType()` was added
* `backupManagementType()` was added
* `withSnapshotConsistencyType(models.IaasVMSnapshotConsistencyType)` was added

#### `models.LongTermRetentionPolicy` was modified

* `retentionPolicyType()` was added

#### `models.AzureVmWorkloadSapAseSystemWorkloadItem` was modified

* `workloadItemType()` was added

#### `models.IaasVmilrRegistrationRequest` was modified

* `objectType()` was added

#### `models.AzureSqlProtectionPolicy` was modified

* `backupManagementType()` was added

#### `models.ValidateIaasVMRestoreOperationRequest` was modified

* `objectType()` was added

#### `models.AzureStorageJob` was modified

* `jobType()` was added

#### `models.AzureVmWorkloadSqlDatabaseProtectedItem` was modified

* `protectedItemType()` was added

#### `models.WorkloadProtectableItem` was modified

* `protectableItemType()` was added

#### `models.SimpleSchedulePolicyV2` was modified

* `schedulePolicyType()` was added

#### `models.AzureVmWorkloadProtectedItem` was modified

* `protectedItemType()` was added

#### `models.AzureVmWorkloadItem` was modified

* `workloadItemType()` was added

#### `models.AzureVmWorkloadSqlInstanceWorkloadItem` was modified

* `workloadItemType()` was added

#### `models.AzureVmWorkloadProtectionPolicy` was modified

* `backupManagementType()` was added

#### `models.SchedulePolicy` was modified

* `schedulePolicyType()` was added

#### `models.ProtectionIntent` was modified

* `protectionIntentItemType()` was added

#### `models.AzureResourceProtectionIntent` was modified

* `protectionIntentItemType()` was added

#### `models.ProtectedItem` was modified

* `protectedItemType()` was added

#### `models.AzureSqlContainer` was modified

* `containerType()` was added

#### `models.AzureVmWorkloadSapHanaSystemProtectableItem` was modified

* `protectableItemType()` was added

#### `models.AzureSqlagWorkloadContainerProtectionContainer` was modified

* `containerType()` was added

#### `models.AzureVmWorkloadSapAseDatabaseWorkloadItem` was modified

* `workloadItemType()` was added

#### `models.AzureWorkloadRestoreRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.VaultJob` was modified

* `jobType()` was added

#### `models.IlrRequest` was modified

* `objectType()` was added

#### `models.AzureVmWorkloadSqlDatabaseWorkloadItem` was modified

* `workloadItemType()` was added

#### `models.AzureVmWorkloadProtectableItem` was modified

* `protectableItemType()` was added

#### `models.GenericContainer` was modified

* `containerType()` was added

#### `models.AzureVMAppContainerProtectionContainer` was modified

* `containerType()` was added

#### `models.TieringCostRehydrationInfo` was modified

* `objectType()` was added

#### `models.IaasVMRecoveryPoint` was modified

* `objectType()` was added

#### `models.AzureVmWorkloadSapHanaDBInstanceProtectedItem` was modified

* `protectedItemType()` was added

#### `models.IaaSvmProtectableItem` was modified

* `protectableItemType()` was added

#### `models.AzureBackupServerContainer` was modified

* `containerType()` was added

#### `models.AzureBackupServerEngine` was modified

* `backupEngineType()` was added

#### `models.AzureIaaSClassicComputeVMProtectableItem` was modified

* `protectableItemType()` was added

#### `models.ExportJobsOperationResultInfo` was modified

* `objectType()` was added

#### `models.AzureFileShareRecoveryPoint` was modified

* `objectType()` was added

#### `models.OperationStatusJobExtendedInfo` was modified

* `objectType()` was added

#### `models.AzureWorkloadBackupRequest` was modified

* `objectType()` was added

#### `models.AzureWorkloadContainer` was modified

* `containerType()` was added

#### `models.FeatureSupportRequest` was modified

* `featureType()` was added

#### `models.ValidateRestoreOperationRequest` was modified

* `objectType()` was added

#### `models.AzureWorkloadAutoProtectionIntent` was modified

* `protectionIntentItemType()` was added

#### `models.FetchTieringCostInfoRequest` was modified

* `objectType()` was added

#### `models.DpmJob` was modified

* `jobType()` was added

#### `models.AzureVMResourceFeatureSupportRequest` was modified

* `featureType()` was added

#### `models.AzureIaaSClassicComputeVMProtectedItem` was modified

* `protectedItemType()` was added

#### `models.GenericProtectedItem` was modified

* `protectedItemType()` was added

#### `models.AzureWorkloadSapHanaRecoveryPoint` was modified

* `objectType()` was added

#### `models.OperationStatusJobsExtendedInfo` was modified

* `objectType()` was added

#### `models.RecoveryPoint` was modified

* `objectType()` was added

#### `models.AzureBackupGoalFeatureSupportRequest` was modified

* `featureType()` was added

#### `models.AzureVmWorkloadSqlDatabaseProtectableItem` was modified

* `protectableItemType()` was added

#### `models.SimpleSchedulePolicy` was modified

* `schedulePolicyType()` was added

#### `models.AzureVmWorkloadSapAseSystemProtectableItem` was modified

* `protectableItemType()` was added

#### `models.MabProtectionPolicy` was modified

* `backupManagementType()` was added

#### `models.AzureFileshareProtectedItem` was modified

* `protectedItemType()` was added

#### `models.Job` was modified

* `jobType()` was added

#### `models.AzureRecoveryServiceVaultProtectionIntent` was modified

* `protectionIntentItemType()` was added

#### `models.AzureFileShareProtectionPolicy` was modified

* `backupManagementType()` was added

#### `models.FetchTieringCostSavingsInfoForProtectedItemRequest` was modified

* `objectType()` was added

#### `models.AzureSqlProtectedItem` was modified

* `protectedItemType()` was added

#### `models.OperationResultInfo` was modified

* `objectType()` was added

#### `models.AzureWorkloadSapHanaRestoreRequest` was modified

* `withResourceGuardOperationRequests(java.util.List)` was added
* `objectType()` was added

#### `models.LogSchedulePolicy` was modified

* `schedulePolicyType()` was added

#### `models.AzureWorkloadSqlPointInTimeRestoreWithRehydrateRequest` was modified

* `objectType()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added

#### `models.VaultStorageConfigOperationResultResponse` was modified

* `objectType()` was added

#### `models.AzureWorkloadPointInTimeRestoreRequest` was modified

* `objectType()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added

#### `models.AzureWorkloadSqlRestoreRequest` was modified

* `objectType()` was added
* `withResourceGuardOperationRequests(java.util.List)` was added

#### `models.AzureIaaSvmJobV2` was modified

* `jobType()` was added

#### `models.PrepareDataMoveResponse` was modified

* `objectType()` was added

#### `models.AzureStorageContainer` was modified

* `containerType()` was added

#### `models.TieringCostSavingInfo` was modified

* `objectType()` was added

#### `models.FetchTieringCostSavingsInfoForPolicyRequest` was modified

* `objectType()` was added

#### `models.AzureIaaSComputeVMContainer` was modified

* `containerType()` was added

#### `models.DpmContainer` was modified

* `containerType()` was added

#### `models.DpmProtectedItem` was modified

* `protectedItemType()` was added

#### `models.AzureVmWorkloadSapHanaDatabaseProtectableItem` was modified

* `protectableItemType()` was added

## 1.3.0 (2024-01-24)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ValidateOperations` was modified

* `trigger(java.lang.String,java.lang.String,models.ValidateOperationRequest)` was removed
* `trigger(java.lang.String,java.lang.String,models.ValidateOperationRequest,com.azure.core.util.Context)` was removed

#### `models.OperationOperations` was modified

* `validate(java.lang.String,java.lang.String,models.ValidateOperationRequest)` was removed
* `validateWithResponse(java.lang.String,java.lang.String,models.ValidateOperationRequest,com.azure.core.util.Context)` was removed

### Features Added

* `models.TieringCostOperationStatus` was added

* `models.FetchTieringCostInfoForRehydrationRequest` was added

* `models.UserAssignedIdentityProperties` was added

* `models.TieringCostInfo` was added

* `models.GetTieringCostOperationResults` was added

* `models.FetchTieringCostSavingsInfoForVaultRequest` was added

* `models.UserAssignedManagedIdentityDetails` was added

* `models.VaultRetentionPolicy` was added

* `models.FetchTieringCosts` was added

* `models.TieringCostRehydrationInfo` was added

* `models.ValidateOperationRequestResource` was added

* `models.FetchTieringCostInfoRequest` was added

* `models.SnapshotRestoreParameters` was added

* `models.SnapshotBackupAdditionalDetails` was added

* `models.FetchTieringCostSavingsInfoForProtectedItemRequest` was added

* `models.TieringCostSavingInfo` was added

* `models.FetchTieringCostSavingsInfoForPolicyRequest` was added

#### `models.AzureWorkloadSapHanaPointInTimeRestoreWithRehydrateRequest` was modified

* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `withTargetResourceGroupName(java.lang.String)` was added
* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added

#### `models.AzureWorkloadSqlRestoreWithRehydrateRequest` was modified

* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added
* `withTargetResourceGroupName(java.lang.String)` was added
* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added

#### `models.AzureWorkloadSapHanaPointInTimeRestoreRequest` was modified

* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added
* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.AzureWorkloadSqlPointInTimeRestoreRequest` was modified

* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added
* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.SubProtectionPolicy` was modified

* `withSnapshotBackupAdditionalDetails(models.SnapshotBackupAdditionalDetails)` was added
* `snapshotBackupAdditionalDetails()` was added

#### `models.AzureWorkloadSapHanaRestoreWithRehydrateRequest` was modified

* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added
* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.ProtectedItem` was modified

* `vaultId()` was added

#### `models.AzureWorkloadRestoreRequest` was modified

* `snapshotRestoreParameters()` was added
* `targetResourceGroupName()` was added
* `withTargetResourceGroupName(java.lang.String)` was added
* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `userAssignedManagedIdentityDetails()` was added
* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added

#### `models.ValidateOperations` was modified

* `trigger(java.lang.String,java.lang.String,models.ValidateOperationRequestResource,com.azure.core.util.Context)` was added
* `trigger(java.lang.String,java.lang.String,models.ValidateOperationRequestResource)` was added

#### `RecoveryServicesBackupManager` was modified

* `fetchTieringCosts()` was added
* `tieringCostOperationStatus()` was added
* `getTieringCostOperationResults()` was added

#### `models.AzureFileShareProtectionPolicy` was modified

* `vaultRetentionPolicy()` was added
* `withVaultRetentionPolicy(models.VaultRetentionPolicy)` was added

#### `models.AzureWorkloadSapHanaRestoreRequest` was modified

* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added
* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.AzureWorkloadSqlPointInTimeRestoreWithRehydrateRequest` was modified

* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `withTargetResourceGroupName(java.lang.String)` was added
* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added

#### `models.AzureWorkloadPointInTimeRestoreRequest` was modified

* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `withTargetResourceGroupName(java.lang.String)` was added
* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added

#### `models.AzureWorkloadSqlRestoreRequest` was modified

* `withSnapshotRestoreParameters(models.SnapshotRestoreParameters)` was added
* `withUserAssignedManagedIdentityDetails(models.UserAssignedManagedIdentityDetails)` was added
* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.OperationOperations` was modified

* `validate(java.lang.String,java.lang.String,models.ValidateOperationRequestResource)` was added
* `validateWithResponse(java.lang.String,java.lang.String,models.ValidateOperationRequestResource,com.azure.core.util.Context)` was added

## 1.2.0 (2023-10-12)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2023-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.VaultSubResourceType` was added

#### `models.AzureVmWorkloadSapHanaHsr` was modified

* `withIsProtectable(java.lang.Boolean)` was added

#### `models.AzureVmWorkloadSapHanaDBInstance` was modified

* `withIsProtectable(java.lang.Boolean)` was added

#### `models.AzureVmWorkloadSapHanaDatabaseProtectedItem` was modified

* `withNodesList(java.util.List)` was added

#### `models.AzureVmWorkloadSqlInstanceProtectableItem` was modified

* `withIsProtectable(java.lang.Boolean)` was added

#### `models.AzureVmWorkloadSqlAvailabilityGroupProtectableItem` was modified

* `withNodesList(java.util.List)` was added
* `withIsProtectable(java.lang.Boolean)` was added
* `nodesList()` was added

#### `models.AzureVmWorkloadSapAseDatabaseProtectedItem` was modified

* `withNodesList(java.util.List)` was added

#### `models.BackupStatusResponse` was modified

* `protectedItemsCount()` was added
* `acquireStorageAccountLock()` was added

#### `models.BackupResourceVaultConfig` was modified

* `softDeleteRetentionPeriodInDays()` was added
* `withSoftDeleteRetentionPeriodInDays(java.lang.Integer)` was added

#### `models.InquiryValidation` was modified

* `protectableItemCount()` was added

#### `models.AzureVmWorkloadSqlDatabaseProtectedItem` was modified

* `withNodesList(java.util.List)` was added

#### `models.AzureVmWorkloadProtectedItem` was modified

* `withNodesList(java.util.List)` was added
* `nodesList()` was added

#### `models.AzureVmWorkloadSapHanaSystemProtectableItem` was modified

* `withIsProtectable(java.lang.Boolean)` was added

#### `models.AzureVmWorkloadProtectableItem` was modified

* `isProtectable()` was added
* `withIsProtectable(java.lang.Boolean)` was added

#### `models.IaasVMRecoveryPoint` was modified

* `extendedLocation()` was added
* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.AzureVmWorkloadSapHanaDBInstanceProtectedItem` was modified

* `withNodesList(java.util.List)` was added

#### `models.PrivateEndpointConnection` was modified

* `withGroupIds(java.util.List)` was added
* `groupIds()` was added

#### `models.AzureVmWorkloadSqlDatabaseProtectableItem` was modified

* `withIsProtectable(java.lang.Boolean)` was added

#### `models.AzureVmWorkloadSapAseSystemProtectableItem` was modified

* `withIsProtectable(java.lang.Boolean)` was added

#### `models.DistributedNodesInfo` was modified

* `withSourceResourceId(java.lang.String)` was added
* `sourceResourceId()` was added

#### `models.AzureVmWorkloadSapHanaDatabaseProtectableItem` was modified

* `withIsProtectable(java.lang.Boolean)` was added

## 1.1.0 (2023-03-16)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2023-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.TargetDiskNetworkAccessSettings` was added

* `models.TargetDiskNetworkAccessOption` was added

* `models.SecuredVMDetails` was added

* `models.ExtendedLocation` was added

#### `models.IaasVMRestoreRequest` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `withTargetDiskNetworkAccessSettings(models.TargetDiskNetworkAccessSettings)` was added
* `targetDiskNetworkAccessSettings()` was added
* `securedVMDetails()` was added
* `withSecuredVMDetails(models.SecuredVMDetails)` was added
* `extendedLocation()` was added

#### `models.IaasVMRestoreWithRehydrationRequest` was modified

* `withSecuredVMDetails(models.SecuredVMDetails)` was added
* `withTargetDiskNetworkAccessSettings(models.TargetDiskNetworkAccessSettings)` was added
* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.RecoveryPointProperties` was modified

* `withIsSoftDeleted(java.lang.Boolean)` was added
* `isSoftDeleted()` was added

#### `models.IaasVMRecoveryPoint` was modified

* `withSecurityType(java.lang.String)` was added
* `isPrivateAccessEnabledOnAnyDisk()` was added
* `withIsPrivateAccessEnabledOnAnyDisk(java.lang.Boolean)` was added
* `securityType()` was added

## 1.0.0 (2023-02-27)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.7 (2023-01-19)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.RecoveryPointProperties` was added

#### `models.AzureWorkloadSqlPointInTimeRecoveryPoint` was modified

* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added

#### `models.AzureWorkloadPointInTimeRecoveryPoint` was modified

* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added

#### `models.AzureWorkloadRecoveryPoint` was modified

* `recoveryPointProperties()` was added
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added

#### `models.GenericRecoveryPoint` was modified

* `recoveryPointProperties()` was added
* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added

#### `models.AzureWorkloadSqlRecoveryPoint` was modified

* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added

#### `models.AzureWorkloadSapHanaPointInTimeRecoveryPoint` was modified

* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added

#### `models.IaasVMRecoveryPoint` was modified

* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added
* `recoveryPointProperties()` was added

#### `models.AzureFileShareRecoveryPoint` was modified

* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added
* `recoveryPointProperties()` was added

#### `models.AzureWorkloadSapHanaRecoveryPoint` was modified

* `withRecoveryPointProperties(models.RecoveryPointProperties)` was added

## 1.0.0-beta.6 (2022-10-24)

- Azure Resource Manager RecoveryServicesBackup client library for Java. This package contains Microsoft Azure SDK for RecoveryServicesBackup Management SDK. Open API 2.0 Specs for Azure RecoveryServices Backup service. Package tag package-2022-09-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ContainerType` was removed

#### `models.AzureIaaSComputeVMProtectedItem` was modified

* `withHealthStatus(models.HealthStatus)` was removed
* `withWorkloadType(models.DataSourceType)` was removed
* `withVirtualMachineId(java.lang.String)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withHealthStatus(models.HealthStatus)` was removed
* `withProtectedItemDataId(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withLastBackupTime(java.time.OffsetDateTime)` was removed

#### `models.MabFileFolderProtectedItem` was modified

* `withWorkloadType(models.DataSourceType)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed

#### `models.AzureVmWorkloadSapHanaDatabaseProtectedItem` was modified

* `withWorkloadType(models.DataSourceType)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withProtectionStatus(java.lang.String)` was removed

#### `models.AzureVmWorkloadSapAseDatabaseProtectedItem` was modified

* `withBackupManagementType(models.BackupManagementType)` was removed
* `withProtectionStatus(java.lang.String)` was removed
* `withWorkloadType(models.DataSourceType)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.AzureIaaSvmProtectedItem` was modified

* `withWorkloadType(models.DataSourceType)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withLastBackupTime(java.time.OffsetDateTime)` was removed
* `withHealthStatus(models.HealthStatus)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withProtectedItemDataId(java.lang.String)` was removed
* `withVirtualMachineId(java.lang.String)` was removed

#### `models.AzureVmWorkloadSqlDatabaseProtectedItem` was modified

* `withBackupManagementType(models.BackupManagementType)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withProtectionStatus(java.lang.String)` was removed
* `withWorkloadType(models.DataSourceType)` was removed
* `withProtectionStatus(java.lang.String)` was removed

#### `models.AzureVmWorkloadProtectedItem` was modified

* `withFriendlyName(java.lang.String)` was removed
* `withProtectionStatus(java.lang.String)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withWorkloadType(models.DataSourceType)` was removed

#### `models.ProtectedItem` was modified

* `withWorkloadType(models.DataSourceType)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed

#### `models.AzureIaaSClassicComputeVMProtectedItem` was modified

* `withLastBackupTime(java.time.OffsetDateTime)` was removed
* `withHealthStatus(models.HealthStatus)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed
* `withProtectedItemDataId(java.lang.String)` was removed
* `withWorkloadType(models.DataSourceType)` was removed
* `withHealthStatus(models.HealthStatus)` was removed
* `withVirtualMachineId(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.GenericProtectedItem` was modified

* `withBackupManagementType(models.BackupManagementType)` was removed
* `withWorkloadType(models.DataSourceType)` was removed

#### `models.AzureFileshareProtectedItem` was modified

* `withWorkloadType(models.DataSourceType)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed

#### `models.AzureSqlProtectedItem` was modified

* `withWorkloadType(models.DataSourceType)` was removed
* `withBackupManagementType(models.BackupManagementType)` was removed

#### `models.DpmProtectedItem` was modified

* `withBackupManagementType(models.BackupManagementType)` was removed
* `withWorkloadType(models.DataSourceType)` was removed

### Features Added

* `models.AzureVmWorkloadSapHanaHsr` was added

* `models.AzureVmWorkloadSapHanaDBInstance` was added

* `models.DeletedProtectionContainers` was added

* `models.AzureVmWorkloadSapHanaDBInstanceProtectedItem` was added

* `models.ProtectableContainerType` was added

* `models.TieringPolicy` was added

* `models.TieringMode` was added

#### `models.AzureIaaSComputeVMProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.MabFileFolderProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.AzureVmWorkloadSapHanaDatabaseProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.AzureVmWorkloadSapAseDatabaseProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.SubProtectionPolicy` was modified

* `withTieringPolicy(java.util.Map)` was added
* `tieringPolicy()` was added

#### `models.AzureIaaSvmProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.AzureIaaSvmProtectionPolicy` was modified

* `tieringPolicy()` was added
* `withTieringPolicy(java.util.Map)` was added

#### `models.AzureVmWorkloadSqlDatabaseProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.AzureVmWorkloadProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.ProtectedItem` was modified

* `softDeleteRetentionPeriod()` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.AzureIaaSClassicComputeVMProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `RecoveryServicesBackupManager` was modified

* `deletedProtectionContainers()` was added

#### `models.GenericProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.AzureVmWorkloadProtectedItemExtendedInfo` was modified

* `withOldestRecoveryPointInArchive(java.time.OffsetDateTime)` was added
* `newestRecoveryPointInArchive()` was added
* `withOldestRecoveryPointInVault(java.time.OffsetDateTime)` was added
* `oldestRecoveryPointInVault()` was added
* `withNewestRecoveryPointInArchive(java.time.OffsetDateTime)` was added
* `oldestRecoveryPointInArchive()` was added

#### `models.AzureFileshareProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.AzureSqlProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

#### `models.AzureIaaSvmProtectedItemExtendedInfo` was modified

* `newestRecoveryPointInArchive()` was added
* `oldestRecoveryPointInArchive()` was added
* `oldestRecoveryPointInVault()` was added
* `withNewestRecoveryPointInArchive(java.time.OffsetDateTime)` was added
* `withOldestRecoveryPointInArchive(java.time.OffsetDateTime)` was added
* `withOldestRecoveryPointInVault(java.time.OffsetDateTime)` was added

#### `models.DpmProtectedItem` was modified

* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added
* `withSoftDeleteRetentionPeriod(java.lang.Integer)` was added

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
