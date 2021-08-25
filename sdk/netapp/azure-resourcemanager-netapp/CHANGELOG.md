# Release History

## 1.0.0-beta.5 (Unreleased)

### Breaking Changes

* `models.BackupPolicies` was removed

* `models.NetAppAccount$Definition` was removed

* `models.ServiceSpecification` was removed

* `models.MetricSpecification` was removed

* `models.SnapshotPolicyVolumeList` was removed

* `models.BackupStatus` was removed

* `models.ReplicationStatus` was removed

* `models.Volume` was removed

* `models.Operations` was removed

* `models.CheckAvailabilityResponse` was removed

* `models.BackupType` was removed

* `models.OperationDisplay` was removed

* `models.Volume$Update` was removed

* `models.SnapshotPolicy$DefinitionStages` was removed

* `models.VolumePatchPropertiesExportPolicy` was removed

* `models.ChownMode` was removed

* `models.VolumePatch` was removed

* `models.SnapshotPolicyPatch` was removed

* `models.CapacityPool$Definition` was removed

* `models.HourlySchedule` was removed

* `models.ActiveDirectory` was removed

* `models.SnapshotPoliciesList` was removed

* `models.Volume$UpdateStages` was removed

* `models.ActiveDirectoryStatus` was removed

* `models.Volume$DefinitionStages` was removed

* `models.BackupPolicy$Definition` was removed

* `models.MonthlySchedule` was removed

* `models.MountTargetProperties` was removed

* `models.SnapshotPolicies` was removed

* `models.BackupPoliciesList` was removed

* `models.CapacityPool$Update` was removed

* `models.CapacityPool$UpdateStages` was removed

* `models.Vaults` was removed

* `models.VolumePropertiesDataProtection` was removed

* `models.ResourceNameAvailabilityRequest` was removed

* `models.SnapshotPolicy$Definition` was removed

* `NetAppFilesManager` was removed

* `models.MirrorState` was removed

* `models.Backup` was removed

* `models.DailySchedule` was removed

* `models.Snapshot$Definition` was removed

* `models.CapacityPool` was removed

* `models.NetAppResources` was removed

* `models.SnapshotsList` was removed

* `models.Backups` was removed

* `models.ServiceLevel` was removed

* `models.Volume$Definition` was removed

* `models.SnapshotPolicy$Update` was removed

* `models.EndpointType` was removed

* `models.RelationshipStatus` was removed

* `models.VolumeSnapshotProperties` was removed

* `models.BackupPolicy$UpdateStages` was removed

* `models.VolumePatchPropertiesDataProtection` was removed

* `models.VolumeBackups` was removed

* `models.OperationListResult` was removed

* `models.ExportPolicyRule` was removed

* `models.QosType` was removed

* `models.VolumePropertiesExportPolicy` was removed

* `models.BackupsList` was removed

* `models.RestoreStatus` was removed

* `models.BackupPolicyPatch` was removed

* `models.Snapshot` was removed

* `models.SnapshotPolicy$UpdateStages` was removed

* `models.InAvailabilityReasonType` was removed

* `models.ReplicationObject` was removed

* `models.Operation` was removed

* `models.Volumes` was removed

* `models.NetAppAccountPatch` was removed

* `models.CapacityPool$DefinitionStages` was removed

* `models.NetAppAccount$UpdateStages` was removed

* `models.VolumeRevert` was removed

* `models.Snapshot$DefinitionStages` was removed

* `models.FilePathAvailabilityRequest` was removed

* `models.Vault` was removed

* `models.AuthorizeRequest` was removed

* `models.BreakReplicationRequest` was removed

* `models.WeeklySchedule` was removed

* `models.NetAppAccount$Update` was removed

* `models.Pools` was removed

* `models.CapacityPoolPatch` was removed

* `models.ReplicationSchedule` was removed

* `models.BackupPatch` was removed

* `models.Dimension` was removed

* `models.Backup$DefinitionStages` was removed

* `models.Accounts` was removed

* `models.Backup$Update` was removed

* `models.AccountBackups` was removed

* `models.SecurityStyle` was removed

* `models.QuotaAvailabilityRequest` was removed

* `models.BackupPolicy$Update` was removed

* `models.Backup$Definition` was removed

* `models.VolumeList` was removed

* `models.Backup$UpdateStages` was removed

* `models.BackupPolicy` was removed

* `NetAppFilesManager$Configurable` was removed

* `models.VaultList` was removed

* `models.NetAppAccount` was removed

* `models.NetAppAccount$DefinitionStages` was removed

* `models.CapacityPoolList` was removed

* `models.AccountEncryption` was removed

* `models.VolumeBackupProperties` was removed

* `models.CheckQuotaNameResourceTypes` was removed

* `models.CheckNameResourceTypes` was removed

* `models.PoolChangeRequest` was removed

* `models.BackupPolicy$DefinitionStages` was removed

* `models.NetAppAccountList` was removed

* `models.SnapshotPolicy` was removed

* `models.Snapshots` was removed

## 1.0.0-beta.4 (2021-06-16)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2021-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `models.NetAppResources` was modified

* `checkFilePathAvailabilityWithResponse(java.lang.String,models.ResourceNameAvailabilityRequest,com.azure.core.util.Context)` was removed
* `checkFilePathAvailability(java.lang.String,models.ResourceNameAvailabilityRequest)` was removed

### New Feature

* `models.ChownMode` was added

* `models.RestoreStatus` was added

* `models.FilePathAvailabilityRequest` was added

#### `models.BackupStatus` was modified

* `totalTransferBytes()` was added
* `lastTransferSize()` was added
* `lastTransferType()` was added

#### `models.Volume` was modified

* `coolnessPeriod()` was added
* `coolAccess()` was added
* `unixPermissions()` was added

#### `models.CapacityPool$Definition` was modified

* `withCoolAccess(java.lang.Boolean)` was added

#### `models.ActiveDirectory` was modified

* `withAdministrators(java.util.List)` was added
* `administrators()` was added

#### `models.CapacityPool` was modified

* `coolAccess()` was added

#### `models.NetAppResources` was modified

* `checkFilePathAvailabilityWithResponse(java.lang.String,models.FilePathAvailabilityRequest,com.azure.core.util.Context)` was added
* `checkFilePathAvailability(java.lang.String,models.FilePathAvailabilityRequest)` was added

#### `models.Backups` was modified

* `getVolumeRestoreStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getVolumeRestoreStatus(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.Volume$Definition` was modified

* `withUnixPermissions(java.lang.String)` was added
* `withCoolnessPeriod(java.lang.Integer)` was added
* `withCoolAccess(java.lang.Boolean)` was added

#### `models.ExportPolicyRule` was modified

* `withChownMode(models.ChownMode)` was added
* `chownMode()` was added

#### `NetAppFilesManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.3 (2021-05-13)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2021-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.VolumeBackupStatus` was removed

#### `NetAppFilesManager` was modified

* `volumeBackupStatus()` was removed

#### `models.Backup` was modified

* `java.lang.String backupType()` -> `models.BackupType backupType()`

#### `models.BackupsList` was modified

* `innerModel()` was removed
* `java.util.List value()` -> `java.util.List value()`

#### `models.BackupPatch` was modified

* `java.lang.String backupType()` -> `models.BackupType backupType()`

#### `models.AccountBackups` was modified

* `models.BackupsList list(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable list(java.lang.String,java.lang.String)`
* `listWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### New Feature

* `models.BackupType` was added

#### `models.Backup` was modified

* `useExistingSnapshot()` was added

#### `models.Backups` was modified

* `getStatusWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getStatus(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.VolumePatchPropertiesDataProtection` was modified

* `snapshot()` was added
* `withSnapshot(models.VolumeSnapshotProperties)` was added

#### `models.BackupsList` was modified

* `validate()` was added
* `withValue(java.util.List)` was added

#### `models.BackupPatch` was modified

* `withUseExistingSnapshot(java.lang.Boolean)` was added
* `useExistingSnapshot()` was added

#### `models.Backup$Update` was modified

* `withUseExistingSnapshot(java.lang.Boolean)` was added

#### `models.AccountBackups` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Backup$Definition` was modified

* `withUseExistingSnapshot(java.lang.Boolean)` was added

## 1.0.0-beta.2 (2021-03-15)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2020-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.KeySource` was removed

* `models.CreatedByType` was removed

* `models.SystemData` was removed

#### `models.SnapshotPolicyPatch` was modified

* `namePropertiesName()` was removed

#### `models.NetAppAccount` was modified

* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.AccountEncryption` was modified

* `models.KeySource keySource()` -> `java.lang.String keySource()`
* `withKeySource(models.KeySource)` was removed

### New Feature

* `models.BackupStatus` was added

* `models.VolumeBackupStatus` was added

#### `models.Volume` was modified

* `ldapEnabled()` was added

#### `models.ActiveDirectory` was modified

* `withAllowLocalNfsUsersWithLdap(java.lang.Boolean)` was added
* `allowLocalNfsUsersWithLdap()` was added

#### `NetAppFilesManager` was modified

* `volumeBackupStatus()` was added

#### `models.Backup` was modified

* `volumeName()` was added

#### `models.Volume$Definition` was modified

* `withLdapEnabled(java.lang.Boolean)` was added

#### `models.BackupPatch` was modified

* `volumeName()` was added

#### `models.AccountEncryption` was modified

* `withKeySource(java.lang.String)` was added

## 1.0.0-beta.1 (2021-02-22)

- Azure Resource Manager NetAppFiles client library for Java. This package contains Microsoft Azure SDK for NetAppFiles Management SDK. Microsoft NetApp Files Azure Resource Provider specification. Package tag package-netapp-2020-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
