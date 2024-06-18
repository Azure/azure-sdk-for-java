# Release History

## 1.0.0-beta.2 (2024-06-18)

- Azure Resource Manager OracleDatabase client library for Java. This package contains Microsoft Azure SDK for OracleDatabase Management SDK.  Package tag package-2023-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ResourcePlanTypeUpdate` was removed

#### `models.OracleSubscriptionUpdate` was modified

* `models.ResourcePlanTypeUpdate plan()` -> `models.PlanUpdate plan()`
* `withPlan(models.ResourcePlanTypeUpdate)` was removed

#### `models.CloudExadataInfrastructureProperties` was modified

* `java.lang.Integer dataStorageSizeInTbs()` -> `java.lang.Double dataStorageSizeInTbs()`

#### `models.AutonomousDatabaseBackupProperties` was modified

* `autonomousDatabaseId()` was removed
* `databaseSizeInTBs()` was removed
* `sizeInTBs()` was removed
* `type()` was removed

### Features Added

* `models.PlanUpdate` was added

* `models.SystemVersionProperties` was added

* `models.LongTermBackUpScheduleDetails` was added

* `models.SystemVersions` was added

* `models.SystemVersionListResult` was added

* `models.RepeatCadenceType` was added

* `models.SystemVersion` was added

* `models.RestoreAutonomousDatabaseDetails` was added

#### `models.CloudVmClusterProperties` was modified

* `withSystemVersion(java.lang.String)` was added

#### `models.DnsPrivateZoneListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.GiVersionListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.VirtualNetworkAddressListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AutonomousDatabase` was modified

* `shrink()` was added
* `restore(models.RestoreAutonomousDatabaseDetails)` was added
* `shrink(com.azure.core.util.Context)` was added
* `restore(models.RestoreAutonomousDatabaseDetails,com.azure.core.util.Context)` was added

#### `models.DbServerListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AutonomousDbVersionListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AutonomousDatabaseCloneProperties` was modified

* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added

#### `models.AutonomousDatabaseListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.OracleSubscriptionUpdate` was modified

* `withPlan(models.PlanUpdate)` was added

#### `models.DbNodeListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AutonomousDatabaseUpdateProperties` was modified

* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `longTermBackupSchedule()` was added

#### `models.DnsPrivateViewListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AutonomousDatabaseCharacterSetListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `OracleDatabaseManager` was modified

* `systemVersions()` was added

#### `models.AutonomousDatabases` was modified

* `shrink(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `restore(java.lang.String,java.lang.String,models.RestoreAutonomousDatabaseDetails)` was added
* `restore(java.lang.String,java.lang.String,models.RestoreAutonomousDatabaseDetails,com.azure.core.util.Context)` was added
* `shrink(java.lang.String,java.lang.String)` was added

#### `models.AutonomousDatabaseBackupListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.DbSystemShapeListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AutonomousDatabaseBaseProperties` was modified

* `longTermBackupSchedule()` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `nextLongTermBackupTimestamp()` was added

#### `models.CloudVmClusterListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AutonomousDatabaseProperties` was modified

* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added

#### `models.AutonomousDatabaseNationalCharacterSetListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.OracleSubscriptionListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.VirtualNetworkAddress$Update` was modified

* `withProperties(models.VirtualNetworkAddressProperties)` was added

#### `models.CloudExadataInfrastructureListResult` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AutonomousDatabaseBackupProperties` was modified

* `databaseSizeInTbs()` was added
* `backupType()` was added
* `sizeInTbs()` was added
* `autonomousDatabaseOcid()` was added
* `timeStarted()` was added

## 1.0.0-beta.1 (2024-05-28)

- Azure Resource Manager OracleDatabase client library for Java. This package contains Microsoft Azure SDK for OracleDatabase Management SDK.  Package tag package-2023-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
