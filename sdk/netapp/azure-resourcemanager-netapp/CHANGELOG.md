# Release History

## 1.0.0-beta.3 (Unreleased)


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
