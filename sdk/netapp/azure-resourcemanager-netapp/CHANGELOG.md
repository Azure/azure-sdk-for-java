# Release History

## 1.0.0-beta.5 (Unreleased)


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
