# Release History

## 1.0.0 (2024-06-25)

- Azure Resource Manager OracleDatabase client library for Java. This package contains Microsoft Azure SDK for OracleDatabase Management SDK.  Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ResourcePlanTypeUpdate` was removed

#### `models.DnsPrivateZoneListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.GiVersionListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.VirtualNetworkAddressListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.DbServerListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.AutonomousDbVersionListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.AutonomousDatabaseListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.OracleSubscriptionUpdate` was modified

* `models.ResourcePlanTypeUpdate plan()` -> `models.PlanUpdate plan()`
* `withPlan(models.ResourcePlanTypeUpdate)` was removed

#### `models.DbNodeListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.DnsPrivateViewListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.AutonomousDatabaseCharacterSetListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.CloudExadataInfrastructureProperties` was modified

* `java.lang.Integer dataStorageSizeInTbs()` -> `java.lang.Double dataStorageSizeInTbs()`

#### `models.AutonomousDatabaseBackupListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.DbSystemShapeListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.CloudVmClusterListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.AutonomousDatabaseNationalCharacterSetListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.OracleSubscriptionListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.CloudExadataInfrastructureListResult` was modified

* `withValue(java.util.List)` was removed

#### `models.AutonomousDatabaseBackupProperties` was modified

* `type()` was removed
* `autonomousDatabaseId()` was removed
* `sizeInTBs()` was removed
* `databaseSizeInTBs()` was removed

### Features Added

* `models.PlanUpdate` was added

* `models.SystemVersionProperties` was added

* `models.LongTermBackUpScheduleDetails` was added

* `models.SystemVersions` was added

* `models.SystemVersionListResult` was added

* `models.RepeatCadenceType` was added

* `models.SystemVersion` was added

* `models.RestoreAutonomousDatabaseDetails` was added

#### `models.OracleSubscriptionUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AllConnectionStringType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbNodeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudVmClusterProperties` was modified

* `withSystemVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DayOfWeek` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Month` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DbServerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ProfileType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DnsPrivateZoneListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GiVersionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkAddressListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NsgCidr` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AddRemoveDbNode` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PeerDbDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbSystemShapeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApexDetailsType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabase` was modified

* `restore(models.RestoreAutonomousDatabaseDetails)` was added
* `shrink()` was added
* `restore(models.RestoreAutonomousDatabaseDetails,com.azure.core.util.Context)` was added
* `shrink(com.azure.core.util.Context)` was added

#### `models.DbServerListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDbVersionListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudExadataInfrastructureUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GenerateAutonomousDatabaseWalletDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseBackupUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkAddressProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudVmClusterUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PortRange` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseCloneProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `allocatedStorageSizeInTbs()` was added
* `timeMaintenanceBegin()` was added
* `inMemoryAreaInGbs()` was added
* `isPreview()` was added
* `timeOfLastSwitchover()` was added
* `apexDetails()` was added
* `provisionableCpus()` was added
* `timeCreated()` was added
* `sqlWebDeveloperUrl()` was added
* `dataSafeStatus()` was added
* `privateEndpoint()` was added
* `timeLocalDataGuardEnabled()` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `ocid()` was added
* `ociUrl()` was added
* `localDisasterRecoveryType()` was added
* `timeDataGuardRoleChanged()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `timeMaintenanceEnd()` was added
* `timeReclamationOfFreeAutonomousDatabase()` was added
* `lifecycleState()` was added
* `lifecycleDetails()` was added
* `availableUpgradeVersions()` was added
* `connectionUrls()` was added
* `peerDbIds()` was added
* `memoryPerOracleComputeUnitInGbs()` was added
* `usedDataStorageSizeInGbs()` was added
* `timeOfLastFailover()` was added
* `nextLongTermBackupTimestamp()` was added
* `failedDataRecoveryInSeconds()` was added
* `usedDataStorageSizeInTbs()` was added
* `actualUsedDataStorageSizeInTbs()` was added
* `timeOfLastRefreshPoint()` was added
* `serviceConsoleUrl()` was added
* `connectionStrings()` was added
* `provisioningState()` was added
* `operationsInsightsStatus()` was added
* `supportedRegionsToCloneTo()` was added
* `timeDeletionOfFreeAutonomousDatabase()` was added
* `timeOfLastRefresh()` was added
* `isRemoteDataGuardEnabled()` was added
* `localStandbyDb()` was added

#### `models.AutonomousDatabaseListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OracleSubscriptionUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withPlan(models.PlanUpdate)` was added

#### `models.ExadataIormConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbNodeListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DnsPrivateZoneProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `longTermBackupSchedule()` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added

#### `models.AutonomousDatabaseBackupUpdateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EstimatedPatchingTime` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DnsPrivateViewListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudExadataInfrastructureUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseCharacterSetListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `OracleDatabaseManager` was modified

* `systemVersions()` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduledOperationsType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbNodeAction` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleSubscriptionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudExadataInfrastructureProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDbVersionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DayOfWeekUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabases` was modified

* `restore(java.lang.String,java.lang.String,models.RestoreAutonomousDatabaseDetails)` was added
* `shrink(java.lang.String,java.lang.String)` was added
* `restore(java.lang.String,java.lang.String,models.RestoreAutonomousDatabaseDetails,com.azure.core.util.Context)` was added
* `shrink(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AutonomousDatabaseCharacterSetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseNationalCharacterSetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DbSystemShapeListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Plan` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBaseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `longTermBackupSchedule()` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `nextLongTermBackupTimestamp()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbIormConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudVmClusterListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionUrlType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbServerPatchingDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomerContact` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseProperties` was modified

* `peerDbIds()` was added
* `timeDeletionOfFreeAutonomousDatabase()` was added
* `timeMaintenanceEnd()` was added
* `timeOfLastFailover()` was added
* `timeOfLastSwitchover()` was added
* `usedDataStorageSizeInTbs()` was added
* `lifecycleState()` was added
* `timeReclamationOfFreeAutonomousDatabase()` was added
* `timeLocalDataGuardEnabled()` was added
* `isPreview()` was added
* `provisionableCpus()` was added
* `timeOfLastRefresh()` was added
* `privateEndpoint()` was added
* `nextLongTermBackupTimestamp()` was added
* `isRemoteDataGuardEnabled()` was added
* `actualUsedDataStorageSizeInTbs()` was added
* `ocid()` was added
* `availableUpgradeVersions()` was added
* `timeMaintenanceBegin()` was added
* `timeDataGuardRoleChanged()` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `lifecycleDetails()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `failedDataRecoveryInSeconds()` was added
* `memoryPerOracleComputeUnitInGbs()` was added
* `connectionUrls()` was added
* `dataSafeStatus()` was added
* `ociUrl()` was added
* `inMemoryAreaInGbs()` was added
* `usedDataStorageSizeInGbs()` was added
* `connectionStrings()` was added
* `timeOfLastRefreshPoint()` was added
* `timeCreated()` was added
* `localDisasterRecoveryType()` was added
* `apexDetails()` was added
* `supportedRegionsToCloneTo()` was added
* `operationsInsightsStatus()` was added
* `allocatedStorageSizeInTbs()` was added
* `provisioningState()` was added
* `localStandbyDb()` was added
* `serviceConsoleUrl()` was added
* `sqlWebDeveloperUrl()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DnsPrivateViewProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseStandbySummary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectionStringType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseNationalCharacterSetListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudVmClusterUpdateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleSubscriptionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateIpAddressesFilter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkAddress$Update` was modified

* `withProperties(models.VirtualNetworkAddressProperties)` was added

#### `models.DataCollectionOptions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MaintenanceWindow` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudExadataInfrastructureListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `timeStarted()` was added
* `sizeInTbs()` was added
* `autonomousDatabaseOcid()` was added
* `databaseSizeInTbs()` was added
* `backupType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduledOperationsTypeUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GiVersionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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

* `type()` was removed
* `databaseSizeInTBs()` was removed
* `autonomousDatabaseId()` was removed
* `sizeInTBs()` was removed

### Features Added

* `models.PlanUpdate` was added

* `models.SystemVersionProperties` was added

* `models.LongTermBackUpScheduleDetails` was added

* `models.SystemVersions` was added

* `models.SystemVersionListResult` was added

* `models.RepeatCadenceType` was added

* `models.SystemVersion` was added

* `models.RestoreAutonomousDatabaseDetails` was added

#### `models.OracleSubscriptionUpdateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AllConnectionStringType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbNodeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudVmClusterProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withSystemVersion(java.lang.String)` was added

#### `models.DayOfWeek` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Month` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbServerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ProfileType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DnsPrivateZoneListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added

#### `models.GiVersionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkAddressListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NsgCidr` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AddRemoveDbNode` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeerDbDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DbSystemShapeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApexDetailsType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabase` was modified

* `shrink()` was added
* `shrink(com.azure.core.util.Context)` was added
* `restore(models.RestoreAutonomousDatabaseDetails)` was added
* `restore(models.RestoreAutonomousDatabaseDetails,com.azure.core.util.Context)` was added

#### `models.DbServerListResult` was modified

* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDbVersionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudExadataInfrastructureUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GenerateAutonomousDatabaseWalletDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkAddressProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudVmClusterUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PortRange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseCloneProperties` was modified

* `timeMaintenanceBegin()` was added
* `provisionableCpus()` was added
* `availableUpgradeVersions()` was added
* `timeReclamationOfFreeAutonomousDatabase()` was added
* `failedDataRecoveryInSeconds()` was added
* `lifecycleDetails()` was added
* `localStandbyDb()` was added
* `privateEndpoint()` was added
* `operationsInsightsStatus()` was added
* `timeOfLastSwitchover()` was added
* `usedDataStorageSizeInTbs()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `timeLocalDataGuardEnabled()` was added
* `peerDbIds()` was added
* `actualUsedDataStorageSizeInTbs()` was added
* `dataSafeStatus()` was added
* `isRemoteDataGuardEnabled()` was added
* `timeOfLastFailover()` was added
* `connectionStrings()` was added
* `timeOfLastRefreshPoint()` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `serviceConsoleUrl()` was added
* `ocid()` was added
* `ociUrl()` was added
* `nextLongTermBackupTimestamp()` was added
* `inMemoryAreaInGbs()` was added
* `supportedRegionsToCloneTo()` was added
* `apexDetails()` was added
* `localDisasterRecoveryType()` was added
* `timeDeletionOfFreeAutonomousDatabase()` was added
* `sqlWebDeveloperUrl()` was added
* `allocatedStorageSizeInTbs()` was added
* `memoryPerOracleComputeUnitInGbs()` was added
* `timeMaintenanceEnd()` was added
* `provisioningState()` was added
* `timeCreated()` was added
* `usedDataStorageSizeInGbs()` was added
* `connectionUrls()` was added
* `timeDataGuardRoleChanged()` was added
* `isPreview()` was added
* `lifecycleState()` was added
* `timeOfLastRefresh()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleSubscriptionUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withPlan(models.PlanUpdate)` was added

#### `models.ExadataIormConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbNodeListResult` was modified

* `withNextLink(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DnsPrivateZoneProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseUpdateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `longTermBackupSchedule()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupUpdateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EstimatedPatchingTime` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DnsPrivateViewListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added

#### `models.CloudExadataInfrastructureUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseCharacterSetListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added

#### `OracleDatabaseManager` was modified

* `systemVersions()` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduledOperationsType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbNodeAction` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleSubscriptionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudExadataInfrastructureProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDbVersionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DayOfWeekUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabases` was modified

* `shrink(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `restore(java.lang.String,java.lang.String,models.RestoreAutonomousDatabaseDetails)` was added
* `restore(java.lang.String,java.lang.String,models.RestoreAutonomousDatabaseDetails,com.azure.core.util.Context)` was added
* `shrink(java.lang.String,java.lang.String)` was added

#### `models.AutonomousDatabaseCharacterSetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseNationalCharacterSetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbSystemShapeListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added

#### `models.Plan` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseBaseProperties` was modified

* `longTermBackupSchedule()` was added
* `nextLongTermBackupTimestamp()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbIormConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudVmClusterListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added

#### `models.ConnectionUrlType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbServerPatchingDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomerContact` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseProperties` was modified

* `provisioningState()` was added
* `availableUpgradeVersions()` was added
* `failedDataRecoveryInSeconds()` was added
* `lifecycleDetails()` was added
* `allocatedStorageSizeInTbs()` was added
* `timeDeletionOfFreeAutonomousDatabase()` was added
* `usedDataStorageSizeInTbs()` was added
* `isRemoteDataGuardEnabled()` was added
* `lifecycleState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `timeCreated()` was added
* `dataSafeStatus()` was added
* `inMemoryAreaInGbs()` was added
* `privateEndpoint()` was added
* `timeOfLastRefresh()` was added
* `actualUsedDataStorageSizeInTbs()` was added
* `timeMaintenanceEnd()` was added
* `nextLongTermBackupTimestamp()` was added
* `peerDbIds()` was added
* `timeOfLastSwitchover()` was added
* `timeReclamationOfFreeAutonomousDatabase()` was added
* `connectionUrls()` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `ociUrl()` was added
* `ocid()` was added
* `timeOfLastFailover()` was added
* `apexDetails()` was added
* `supportedRegionsToCloneTo()` was added
* `timeDataGuardRoleChanged()` was added
* `timeLocalDataGuardEnabled()` was added
* `provisionableCpus()` was added
* `sqlWebDeveloperUrl()` was added
* `connectionStrings()` was added
* `localDisasterRecoveryType()` was added
* `usedDataStorageSizeInGbs()` was added
* `serviceConsoleUrl()` was added
* `timeMaintenanceBegin()` was added
* `operationsInsightsStatus()` was added
* `memoryPerOracleComputeUnitInGbs()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `localStandbyDb()` was added
* `timeOfLastRefreshPoint()` was added
* `isPreview()` was added

#### `models.DnsPrivateViewProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseStandbySummary` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionStringType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseNationalCharacterSetListResult` was modified

* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudVmClusterUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleSubscriptionListResult` was modified

* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateIpAddressesFilter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkAddress$Update` was modified

* `withProperties(models.VirtualNetworkAddressProperties)` was added

#### `models.DataCollectionOptions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MaintenanceWindow` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudExadataInfrastructureListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withNextLink(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupProperties` was modified

* `databaseSizeInTbs()` was added
* `timeStarted()` was added
* `sizeInTbs()` was added
* `backupType()` was added
* `autonomousDatabaseOcid()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduledOperationsTypeUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GiVersionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.1 (2024-05-28)

- Azure Resource Manager OracleDatabase client library for Java. This package contains Microsoft Azure SDK for OracleDatabase Management SDK.  Package tag package-2023-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
