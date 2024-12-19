# Release History

## 1.1.0 (2024-12-19)

- Azure Resource Manager OracleDatabase client library for Java. This package contains Microsoft Azure SDK for OracleDatabase Management SDK.  Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DbSystemShapeProperties` was modified

* `java.lang.Integer availableCoreCount()` -> `int availableCoreCount()`

#### `models.AutonomousDatabaseCloneProperties` was modified

* `isPreview()` was removed
* `connectionUrls()` was removed
* `timeCreated()` was removed
* `dataSafeStatus()` was removed
* `timeOfLastRefreshPoint()` was removed
* `localStandbyDb()` was removed
* `ociUrl()` was removed
* `sqlWebDeveloperUrl()` was removed
* `timeMaintenanceBegin()` was removed
* `allocatedStorageSizeInTbs()` was removed
* `timeMaintenanceEnd()` was removed
* `isRemoteDataGuardEnabled()` was removed
* `failedDataRecoveryInSeconds()` was removed
* `localDisasterRecoveryType()` was removed
* `nextLongTermBackupTimestamp()` was removed
* `timeOfLastSwitchover()` was removed
* `memoryPerOracleComputeUnitInGbs()` was removed
* `provisionableCpus()` was removed
* `timeReclamationOfFreeAutonomousDatabase()` was removed
* `timeOfLastFailover()` was removed
* `lifecycleDetails()` was removed
* `provisioningState()` was removed
* `timeOfLastRefresh()` was removed
* `connectionStrings()` was removed
* `actualUsedDataStorageSizeInTbs()` was removed
* `inMemoryAreaInGbs()` was removed
* `peerDbIds()` was removed
* `serviceConsoleUrl()` was removed
* `privateEndpoint()` was removed
* `apexDetails()` was removed
* `usedDataStorageSizeInGbs()` was removed
* `timeDeletionOfFreeAutonomousDatabase()` was removed
* `lifecycleState()` was removed
* `operationsInsightsStatus()` was removed
* `timeDataGuardRoleChanged()` was removed
* `ocid()` was removed
* `usedDataStorageSizeInTbs()` was removed
* `timeLocalDataGuardEnabled()` was removed
* `supportedRegionsToCloneTo()` was removed
* `availableUpgradeVersions()` was removed

#### `models.DnsPrivateZoneProperties` was modified

* `java.lang.Integer serial()` -> `int serial()`
* `java.lang.Boolean isProtected()` -> `boolean isProtected()`

#### `models.AutonomousDatabaseProperties` was modified

* `timeOfLastRefreshPoint()` was removed
* `timeMaintenanceBegin()` was removed
* `actualUsedDataStorageSizeInTbs()` was removed
* `usedDataStorageSizeInGbs()` was removed
* `failedDataRecoveryInSeconds()` was removed
* `memoryPerOracleComputeUnitInGbs()` was removed
* `localStandbyDb()` was removed
* `lifecycleDetails()` was removed
* `connectionStrings()` was removed
* `usedDataStorageSizeInTbs()` was removed
* `localDisasterRecoveryType()` was removed
* `ociUrl()` was removed
* `timeDataGuardRoleChanged()` was removed
* `serviceConsoleUrl()` was removed
* `timeReclamationOfFreeAutonomousDatabase()` was removed
* `provisioningState()` was removed
* `allocatedStorageSizeInTbs()` was removed
* `dataSafeStatus()` was removed
* `timeOfLastFailover()` was removed
* `lifecycleState()` was removed
* `isRemoteDataGuardEnabled()` was removed
* `sqlWebDeveloperUrl()` was removed
* `timeLocalDataGuardEnabled()` was removed
* `timeOfLastRefresh()` was removed
* `isPreview()` was removed
* `operationsInsightsStatus()` was removed
* `timeDeletionOfFreeAutonomousDatabase()` was removed
* `nextLongTermBackupTimestamp()` was removed
* `ocid()` was removed
* `timeMaintenanceEnd()` was removed
* `timeOfLastSwitchover()` was removed
* `supportedRegionsToCloneTo()` was removed
* `provisionableCpus()` was removed
* `apexDetails()` was removed
* `inMemoryAreaInGbs()` was removed
* `peerDbIds()` was removed
* `connectionUrls()` was removed
* `privateEndpoint()` was removed
* `availableUpgradeVersions()` was removed
* `timeCreated()` was removed

#### `models.DnsPrivateViewProperties` was modified

* `java.lang.Boolean isProtected()` -> `boolean isProtected()`

### Features Added

#### `models.DbNodeProperties` was modified

* `withVnic2Id(java.lang.String)` was added
* `withFaultDomain(java.lang.String)` was added
* `withBackupIpId(java.lang.String)` was added
* `withBackupVnic2Id(java.lang.String)` was added
* `withOcid(java.lang.String)` was added
* `withMaintenanceType(models.DbNodeMaintenanceType)` was added
* `withHostname(java.lang.String)` was added
* `withBackupVnicId(java.lang.String)` was added
* `withVnicId(java.lang.String)` was added
* `withTimeCreated(java.time.OffsetDateTime)` was added
* `withAdditionalDetails(java.lang.String)` was added
* `withMemorySizeInGbs(java.lang.Integer)` was added
* `withLifecycleDetails(java.lang.String)` was added
* `withSoftwareStorageSizeInGb(java.lang.Integer)` was added
* `withHostIpId(java.lang.String)` was added
* `withTimeMaintenanceWindowStart(java.time.OffsetDateTime)` was added
* `withDbNodeStorageSizeInGbs(java.lang.Integer)` was added
* `withDbSystemId(java.lang.String)` was added
* `withLifecycleState(models.DbNodeProvisioningState)` was added
* `withCpuCoreCount(java.lang.Integer)` was added
* `withTimeMaintenanceWindowEnd(java.time.OffsetDateTime)` was added
* `withDbServerId(java.lang.String)` was added

#### `models.SystemVersionProperties` was modified

* `withSystemVersion(java.lang.String)` was added

#### `models.DbSystemShapeProperties` was modified

* `withAvailableMemoryPerNodeInGbs(java.lang.Integer)` was added
* `withMinimumNodeCount(java.lang.Integer)` was added
* `withRuntimeMinimumCoreCount(java.lang.Integer)` was added
* `withAvailableMemoryInGbs(java.lang.Integer)` was added
* `withMaxStorageCount(java.lang.Integer)` was added
* `withAvailableCoreCountPerNode(java.lang.Integer)` was added
* `withAvailableDbNodeStorageInGbs(java.lang.Integer)` was added
* `withMinStorageCount(java.lang.Integer)` was added
* `withMaximumNodeCount(java.lang.Integer)` was added
* `withAvailableDataStorageInTbs(java.lang.Integer)` was added
* `withAvailableCoreCount(int)` was added
* `withMinimumCoreCount(java.lang.Integer)` was added
* `withMinMemoryPerNodeInGbs(java.lang.Integer)` was added
* `withCoreCountIncrement(java.lang.Integer)` was added
* `withMinDataStorageInTbs(java.lang.Integer)` was added
* `withAvailableDataStoragePerServerInTbs(java.lang.Double)` was added
* `withMinCoreCountPerNode(java.lang.Integer)` was added
* `withAvailableDbNodePerNodeInGbs(java.lang.Integer)` was added
* `withMinDbNodeStoragePerNodeInGbs(java.lang.Integer)` was added
* `withShapeFamily(java.lang.String)` was added

#### `models.DnsPrivateZoneProperties` was modified

* `withTimeCreated(java.time.OffsetDateTime)` was added
* `withViewId(java.lang.String)` was added
* `withIsProtected(boolean)` was added
* `withSelf(java.lang.String)` was added
* `withSerial(int)` was added
* `withZoneType(models.ZoneType)` was added
* `withOcid(java.lang.String)` was added
* `withLifecycleState(models.DnsPrivateZonesLifecycleState)` was added
* `withVersion(java.lang.String)` was added

#### `models.AutonomousDbVersionProperties` was modified

* `withIsDefaultForFree(java.lang.Boolean)` was added
* `withVersion(java.lang.String)` was added
* `withIsDefaultForPaid(java.lang.Boolean)` was added
* `withIsFreeTierEnabled(java.lang.Boolean)` was added
* `withDbWorkload(models.WorkloadType)` was added
* `withIsPaidEnabled(java.lang.Boolean)` was added

#### `models.AutonomousDatabaseCharacterSetProperties` was modified

* `withCharacterSet(java.lang.String)` was added

#### `models.AutonomousDatabaseNationalCharacterSetProperties` was modified

* `withCharacterSet(java.lang.String)` was added

#### `models.DnsPrivateViewProperties` was modified

* `withDisplayName(java.lang.String)` was added
* `withLifecycleState(models.DnsPrivateViewsLifecycleState)` was added
* `withTimeCreated(java.time.OffsetDateTime)` was added
* `withSelf(java.lang.String)` was added
* `withTimeUpdated(java.time.OffsetDateTime)` was added
* `withOcid(java.lang.String)` was added
* `withIsProtected(boolean)` was added

#### `models.GiVersionProperties` was modified

* `withVersion(java.lang.String)` was added

## 1.0.0 (2024-07-04)

- Azure Resource Manager OracleDatabase client library for Java. This package contains Microsoft Azure SDK for OracleDatabase Management SDK.  Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DbSystemShapeProperties` was modified

* `int availableCoreCount()` -> `java.lang.Integer availableCoreCount()`

#### `models.DnsPrivateZoneProperties` was modified

* `boolean isProtected()` -> `java.lang.Boolean isProtected()`
* `int serial()` -> `java.lang.Integer serial()`

#### `models.DnsPrivateViewProperties` was modified

* `boolean isProtected()` -> `java.lang.Boolean isProtected()`

## 1.0.0-beta.2 (2024-06-26)

- Azure Resource Manager OracleDatabase client library for Java. This package contains Microsoft Azure SDK for OracleDatabase Management SDK.  Package tag package-2023-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

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

* `withPlan(models.ResourcePlanTypeUpdate)` was removed
* `models.ResourcePlanTypeUpdate plan()` -> `models.PlanUpdate plan()`

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

* `databaseSizeInTBs()` was removed
* `sizeInTBs()` was removed
* `type()` was removed
* `autonomousDatabaseId()` was removed

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

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DbNodeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudVmClusterProperties` was modified

* `withSystemVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DayOfWeek` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Month` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AddRemoveDbNode` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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

* `restore(models.RestoreAutonomousDatabaseDetails,com.azure.core.util.Context)` was added
* `restore(models.RestoreAutonomousDatabaseDetails)` was added
* `shrink()` was added
* `shrink(com.azure.core.util.Context)` was added

#### `models.DbServerListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDbVersionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudExadataInfrastructureUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GenerateAutonomousDatabaseWalletDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkAddressProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudVmClusterUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PortRange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseCloneProperties` was modified

* `peerDbIds()` was added
* `connectionUrls()` was added
* `provisionableCpus()` was added
* `failedDataRecoveryInSeconds()` was added
* `ocid()` was added
* `allocatedStorageSizeInTbs()` was added
* `isRemoteDataGuardEnabled()` was added
* `timeReclamationOfFreeAutonomousDatabase()` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `memoryPerOracleComputeUnitInGbs()` was added
* `inMemoryAreaInGbs()` was added
* `actualUsedDataStorageSizeInTbs()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `ociUrl()` was added
* `timeMaintenanceBegin()` was added
* `lifecycleState()` was added
* `nextLongTermBackupTimestamp()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `dataSafeStatus()` was added
* `sqlWebDeveloperUrl()` was added
* `timeCreated()` was added
* `timeOfLastRefresh()` was added
* `apexDetails()` was added
* `operationsInsightsStatus()` was added
* `serviceConsoleUrl()` was added
* `timeLocalDataGuardEnabled()` was added
* `usedDataStorageSizeInGbs()` was added
* `usedDataStorageSizeInTbs()` was added
* `localStandbyDb()` was added
* `timeDeletionOfFreeAutonomousDatabase()` was added
* `privateEndpoint()` was added
* `isPreview()` was added
* `timeOfLastSwitchover()` was added
* `timeOfLastRefreshPoint()` was added
* `timeDataGuardRoleChanged()` was added
* `connectionStrings()` was added
* `timeOfLastFailover()` was added
* `localDisasterRecoveryType()` was added
* `timeMaintenanceEnd()` was added
* `provisioningState()` was added
* `supportedRegionsToCloneTo()` was added
* `availableUpgradeVersions()` was added
* `lifecycleDetails()` was added

#### `models.AutonomousDatabaseListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OracleSubscriptionUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withPlan(models.PlanUpdate)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExadataIormConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DbNodeListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DnsPrivateZoneProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseUpdateProperties` was modified

* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `longTermBackupSchedule()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseCharacterSetListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `OracleDatabaseManager` was modified

* `systemVersions()` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DayOfWeekUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabases` was modified

* `shrink(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `shrink(java.lang.String,java.lang.String)` was added
* `restore(java.lang.String,java.lang.String,models.RestoreAutonomousDatabaseDetails,com.azure.core.util.Context)` was added
* `restore(java.lang.String,java.lang.String,models.RestoreAutonomousDatabaseDetails)` was added

#### `models.AutonomousDatabaseCharacterSetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseNationalCharacterSetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseBackupListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DbSystemShapeListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Plan` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutonomousDatabaseBaseProperties` was modified

* `longTermBackupSchedule()` was added
* `nextLongTermBackupTimestamp()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added

#### `models.DbIormConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudVmClusterListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionUrlType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DbServerPatchingDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomerContact` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseProperties` was modified

* `memoryPerOracleComputeUnitInGbs()` was added
* `failedDataRecoveryInSeconds()` was added
* `connectionStrings()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `timeOfLastFailover()` was added
* `allocatedStorageSizeInTbs()` was added
* `provisioningState()` was added
* `peerDbIds()` was added
* `timeOfLastRefreshPoint()` was added
* `isPreview()` was added
* `timeReclamationOfFreeAutonomousDatabase()` was added
* `provisionableCpus()` was added
* `usedDataStorageSizeInGbs()` was added
* `lifecycleState()` was added
* `serviceConsoleUrl()` was added
* `operationsInsightsStatus()` was added
* `ociUrl()` was added
* `timeMaintenanceEnd()` was added
* `actualUsedDataStorageSizeInTbs()` was added
* `localDisasterRecoveryType()` was added
* `timeCreated()` was added
* `timeDataGuardRoleChanged()` was added
* `lifecycleDetails()` was added
* `nextLongTermBackupTimestamp()` was added
* `timeMaintenanceBegin()` was added
* `sqlWebDeveloperUrl()` was added
* `connectionUrls()` was added
* `timeLocalDataGuardEnabled()` was added
* `apexDetails()` was added
* `timeDeletionOfFreeAutonomousDatabase()` was added
* `timeOfLastSwitchover()` was added
* `ocid()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withLongTermBackupSchedule(models.LongTermBackUpScheduleDetails)` was added
* `isRemoteDataGuardEnabled()` was added
* `availableUpgradeVersions()` was added
* `privateEndpoint()` was added
* `inMemoryAreaInGbs()` was added
* `dataSafeStatus()` was added
* `timeOfLastRefresh()` was added
* `usedDataStorageSizeInTbs()` was added
* `supportedRegionsToCloneTo()` was added
* `localStandbyDb()` was added

#### `models.DnsPrivateViewProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseStandbySummary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectionStringType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutonomousDatabaseNationalCharacterSetListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudVmClusterUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleSubscriptionListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateIpAddressesFilter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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

* `databaseSizeInTbs()` was added
* `timeStarted()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `autonomousDatabaseOcid()` was added
* `sizeInTbs()` was added
* `backupType()` was added

#### `models.ScheduledOperationsTypeUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GiVersionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.1 (2024-05-28)

- Azure Resource Manager OracleDatabase client library for Java. This package contains Microsoft Azure SDK for OracleDatabase Management SDK.  Package tag package-2023-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
