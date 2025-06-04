# Release History

## 1.1.0 (2025-06-04)

- Azure Resource Manager Oracle Database client library for Java. This package contains Microsoft Azure SDK for Oracle Database Management SDK.  Package api-version 2025-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DnsPrivateZoneListResult` was removed

#### `models.GiVersionListResult` was removed

#### `models.VirtualNetworkAddressListResult` was removed

#### `models.DbServerListResult` was removed

#### `models.AutonomousDbVersionListResult` was removed

#### `models.SystemVersionListResult` was removed

#### `models.AutonomousDatabaseListResult` was removed

#### `models.DbNodeListResult` was removed

#### `models.DnsPrivateViewListResult` was removed

#### `models.AutonomousDatabaseCharacterSetListResult` was removed

#### `models.DayOfWeekUpdate` was removed

#### `models.AutonomousDatabaseBackupListResult` was removed

#### `models.DbSystemShapeListResult` was removed

#### `models.CloudVmClusterListResult` was removed

#### `models.AutonomousDatabaseNationalCharacterSetListResult` was removed

#### `models.OperationListResult` was removed

#### `models.OracleSubscriptionListResult` was removed

#### `models.CloudExadataInfrastructureListResult` was removed

#### `models.ScheduledOperationsTypeUpdate` was removed

#### `models.AllConnectionStringType` was modified

* `withMedium(java.lang.String)` was removed
* `withHigh(java.lang.String)` was removed
* `withLow(java.lang.String)` was removed

#### `models.CloudVmClusterProperties` was modified

* `java.lang.Float ocpuCount()` -> `java.lang.Double ocpuCount()`
* `withOcpuCount(java.lang.Float)` was removed

#### `models.ProfileType` was modified

* `withProtocol(models.ProtocolType)` was removed
* `withIsRegional(java.lang.Boolean)` was removed
* `withTlsAuthentication(models.TlsAuthenticationType)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed
* `withConsumerGroup(models.ConsumerGroup)` was removed
* `withSyntaxFormat(models.SyntaxFormatType)` was removed
* `withHostFormat(models.HostFormatType)` was removed
* `withSessionMode(models.SessionModeType)` was removed

#### `models.DbSystemShapeProperties` was modified

* `java.lang.Integer availableCoreCount()` -> `int availableCoreCount()`

#### `models.ApexDetailsType` was modified

* `withOrdsVersion(java.lang.String)` was removed
* `withApexVersion(java.lang.String)` was removed

#### `models.DbSystemShapes` was modified

* `listByLocation(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AutonomousDatabaseCloneProperties` was modified

* `localStandbyDb()` was removed
* `availableUpgradeVersions()` was removed
* `privateEndpoint()` was removed
* `memoryPerOracleComputeUnitInGbs()` was removed
* `supportedRegionsToCloneTo()` was removed
* `connectionStrings()` was removed
* `actualUsedDataStorageSizeInTbs()` was removed
* `nextLongTermBackupTimestamp()` was removed
* `lifecycleState()` was removed
* `lifecycleDetails()` was removed
* `timeReclamationOfFreeAutonomousDatabase()` was removed
* `inMemoryAreaInGbs()` was removed
* `apexDetails()` was removed
* `failedDataRecoveryInSeconds()` was removed
* `connectionUrls()` was removed
* `provisioningState()` was removed
* `dataSafeStatus()` was removed
* `sqlWebDeveloperUrl()` was removed
* `timeCreated()` was removed
* `isPreview()` was removed
* `peerDbIds()` was removed
* `usedDataStorageSizeInTbs()` was removed
* `timeDeletionOfFreeAutonomousDatabase()` was removed
* `timeMaintenanceEnd()` was removed
* `ociUrl()` was removed
* `withComputeCount(java.lang.Float)` was removed
* `localDisasterRecoveryType()` was removed
* `timeDataGuardRoleChanged()` was removed
* `timeOfLastRefreshPoint()` was removed
* `usedDataStorageSizeInGbs()` was removed
* `isRemoteDataGuardEnabled()` was removed
* `timeOfLastRefresh()` was removed
* `allocatedStorageSizeInTbs()` was removed
* `operationsInsightsStatus()` was removed
* `timeOfLastSwitchover()` was removed
* `serviceConsoleUrl()` was removed
* `timeLocalDataGuardEnabled()` was removed
* `timeMaintenanceBegin()` was removed
* `timeOfLastFailover()` was removed
* `ocid()` was removed
* `provisionableCpus()` was removed

#### `models.ExadataIormConfig` was modified

* `withLifecycleDetails(java.lang.String)` was removed
* `withObjective(models.Objective)` was removed
* `withDbPlans(java.util.List)` was removed
* `withLifecycleState(models.IormLifecycleState)` was removed

#### `models.DnsPrivateZoneProperties` was modified

* `java.lang.Boolean isProtected()` -> `boolean isProtected()`
* `java.lang.Integer serial()` -> `int serial()`

#### `models.AutonomousDatabaseUpdateProperties` was modified

* `models.ScheduledOperationsTypeUpdate scheduledOperations()` -> `models.ScheduledOperationsType scheduledOperations()`
* `withScheduledOperations(models.ScheduledOperationsTypeUpdate)` was removed
* `withComputeCount(java.lang.Float)` was removed
* `java.lang.Float computeCount()` -> `java.lang.Double computeCount()`

#### `models.GiVersions` was modified

* `listByLocation(java.lang.String,com.azure.core.util.Context)` was removed

#### `OracleDatabaseManager` was modified

* `fluent.OracleDatabaseResourceManager serviceClient()` -> `fluent.OracleDatabaseManagementClient serviceClient()`

#### `models.AutonomousDatabaseBaseProperties` was modified

* `java.lang.Float computeCount()` -> `java.lang.Double computeCount()`
* `withComputeCount(java.lang.Float)` was removed

#### `models.DbIormConfig` was modified

* `withShare(java.lang.Integer)` was removed
* `withFlashCacheLimit(java.lang.String)` was removed
* `withDbName(java.lang.String)` was removed

#### `models.ConnectionUrlType` was modified

* `withSqlDevWebUrl(java.lang.String)` was removed
* `withApexUrl(java.lang.String)` was removed
* `withOrdsUrl(java.lang.String)` was removed
* `withGraphStudioUrl(java.lang.String)` was removed
* `withMongoDbUrl(java.lang.String)` was removed
* `withMachineLearningNotebookUrl(java.lang.String)` was removed
* `withDatabaseTransformsUrl(java.lang.String)` was removed

#### `models.AutonomousDatabaseProperties` was modified

* `timeOfLastFailover()` was removed
* `allocatedStorageSizeInTbs()` was removed
* `isRemoteDataGuardEnabled()` was removed
* `actualUsedDataStorageSizeInTbs()` was removed
* `timeLocalDataGuardEnabled()` was removed
* `failedDataRecoveryInSeconds()` was removed
* `provisionableCpus()` was removed
* `timeMaintenanceBegin()` was removed
* `sqlWebDeveloperUrl()` was removed
* `timeOfLastSwitchover()` was removed
* `provisioningState()` was removed
* `withComputeCount(java.lang.Float)` was removed
* `ocid()` was removed
* `timeMaintenanceEnd()` was removed
* `isPreview()` was removed
* `availableUpgradeVersions()` was removed
* `usedDataStorageSizeInGbs()` was removed
* `connectionStrings()` was removed
* `serviceConsoleUrl()` was removed
* `privateEndpoint()` was removed
* `timeCreated()` was removed
* `peerDbIds()` was removed
* `memoryPerOracleComputeUnitInGbs()` was removed
* `timeOfLastRefreshPoint()` was removed
* `dataSafeStatus()` was removed
* `nextLongTermBackupTimestamp()` was removed
* `timeReclamationOfFreeAutonomousDatabase()` was removed
* `timeDeletionOfFreeAutonomousDatabase()` was removed
* `lifecycleState()` was removed
* `localDisasterRecoveryType()` was removed
* `operationsInsightsStatus()` was removed
* `connectionUrls()` was removed
* `timeOfLastRefresh()` was removed
* `timeDataGuardRoleChanged()` was removed
* `localStandbyDb()` was removed
* `inMemoryAreaInGbs()` was removed
* `apexDetails()` was removed
* `lifecycleDetails()` was removed
* `supportedRegionsToCloneTo()` was removed
* `ociUrl()` was removed
* `usedDataStorageSizeInTbs()` was removed

#### `models.DnsPrivateViewProperties` was modified

* `java.lang.Boolean isProtected()` -> `boolean isProtected()`

#### `models.AutonomousDatabaseStandbySummary` was modified

* `withLifecycleDetails(java.lang.String)` was removed
* `withLifecycleState(models.AutonomousDatabaseLifecycleState)` was removed
* `withLagTimeInSeconds(java.lang.Integer)` was removed
* `withTimeDataGuardRoleChanged(java.lang.String)` was removed
* `withTimeDisasterRecoveryRoleChanged(java.lang.String)` was removed

#### `models.ConnectionStringType` was modified

* `withMedium(java.lang.String)` was removed
* `withProfiles(java.util.List)` was removed
* `withHigh(java.lang.String)` was removed
* `withAllConnectionStrings(models.AllConnectionStringType)` was removed
* `withLow(java.lang.String)` was removed
* `withDedicated(java.lang.String)` was removed

#### `models.CloudVmClusterUpdateProperties` was modified

* `java.lang.Float ocpuCount()` -> `java.lang.Double ocpuCount()`
* `withOcpuCount(java.lang.Float)` was removed

### Features Added

* `models.DefinedFileSystemConfiguration` was added

* `models.ExadbVmClusterProperties` was added

* `models.GridImageType` was added

* `models.ExascaleDbStorageVault` was added

* `implementation.models.ExascaleDbStorageVaultListResult` was added

* `implementation.models.AutonomousDatabaseBackupListResult` was added

* `models.DbNodeDetails` was added

* `models.ExascaleDbStorageVault$UpdateStages` was added

* `models.FlexComponent` was added

* `models.ExascaleDbStorageVaults` was added

* `implementation.models.ExascaleDbNodeListResult` was added

* `implementation.models.CloudExadataInfrastructureListResult` was added

* `models.ExascaleDbStorageDetails` was added

* `models.ExadbVmClusters` was added

* `implementation.models.GiMinorVersionListResult` was added

* `models.AutonomousDatabaseCrossRegionDisasterRecoveryProperties` was added

* `implementation.models.OracleSubscriptionListResult` was added

* `models.RemoveVirtualMachineFromExadbVmClusterDetails` was added

* `models.FlexComponents` was added

* `models.AutonomousDatabaseFromBackupTimestampProperties` was added

* `models.AzureSubscriptions` was added

* `models.ExadbVmClusterUpdate` was added

* `models.ExascaleDbStorageVaultLifecycleState` was added

* `models.DisasterRecoveryConfigurationDetails` was added

* `models.ShapeFamily` was added

* `implementation.models.AutonomousDatabaseCharacterSetListResult` was added

* `implementation.models.DnsPrivateZoneListResult` was added

* `implementation.models.DbSystemShapeListResult` was added

* `implementation.models.DnsPrivateViewListResult` was added

* `models.ExadbVmClusterUpdateProperties` was added

* `models.ExadbVmCluster` was added

* `models.ExadbVmCluster$UpdateStages` was added

* `models.GiMinorVersions` was added

* `implementation.models.OperationListResult` was added

* `implementation.models.DbServerListResult` was added

* `models.ExascaleDbStorageVault$DefinitionStages` was added

* `models.ExadbVmCluster$Definition` was added

* `implementation.models.GiVersionListResult` was added

* `models.FileSystemConfigurationDetails` was added

* `implementation.models.DbNodeListResult` was added

* `implementation.models.FlexComponentListResult` was added

* `models.ExascaleDbNodeProperties` was added

* `models.SystemShapes` was added

* `models.AddSubscriptionOperationState` was added

* `models.ExadbVmClusterLifecycleState` was added

* `implementation.models.CloudVmClusterListResult` was added

* `models.ExadbVmCluster$DefinitionStages` was added

* `models.FlexComponentProperties` was added

* `models.ExascaleDbNodes` was added

* `models.HardwareType` was added

* `models.ExascaleDbStorageVault$Definition` was added

* `implementation.models.ExadbVmClusterListResult` was added

* `implementation.models.AutonomousDatabaseNationalCharacterSetListResult` was added

* `models.GiMinorVersionProperties` was added

* `models.ExadbVmClusterStorageDetails` was added

* `models.ExascaleDbStorageVaultProperties` was added

* `models.ExascaleDbStorageVaultTagsUpdate` was added

* `models.DbActionResponse` was added

* `implementation.models.AutonomousDbVersionListResult` was added

* `implementation.models.AutonomousDatabaseListResult` was added

* `models.ExascaleDbStorageInputDetails` was added

* `models.GiMinorVersion` was added

* `models.ExascaleDbStorageVault$Update` was added

* `implementation.models.VirtualNetworkAddressListResult` was added

* `models.ExascaleDbNode` was added

* `implementation.models.SystemVersionListResult` was added

* `models.ExadbVmCluster$Update` was added

#### `models.CloudVmClusterProperties` was modified

* `withOcpuCount(java.lang.Double)` was added
* `fileSystemConfigurationDetails()` was added
* `computeModel()` was added
* `withFileSystemConfigurationDetails(java.util.List)` was added

#### `models.DbServerProperties` was modified

* `computeModel()` was added

#### `models.OracleSubscriptions` was modified

* `addAzureSubscriptions(models.AzureSubscriptions)` was added
* `addAzureSubscriptions(models.AzureSubscriptions,com.azure.core.util.Context)` was added

#### `models.PeerDbDetails` was modified

* `withPeerDbLocation(java.lang.String)` was added
* `peerDbLocation()` was added
* `withPeerDbOcid(java.lang.String)` was added
* `peerDbOcid()` was added

#### `models.DbSystemShapeProperties` was modified

* `areServerTypesSupported()` was added
* `displayName()` was added
* `shapeName()` was added
* `computeModel()` was added

#### `models.AutonomousDatabase` was modified

* `changeDisasterRecoveryConfiguration(models.DisasterRecoveryConfigurationDetails)` was added
* `changeDisasterRecoveryConfiguration(models.DisasterRecoveryConfigurationDetails,com.azure.core.util.Context)` was added

#### `models.DbSystemShapes` was modified

* `listByLocation(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AutonomousDatabaseCloneProperties` was modified

* `withComputeCount(java.lang.Double)` was added

#### `models.AutonomousDatabaseUpdateProperties` was modified

* `withScheduledOperations(models.ScheduledOperationsType)` was added
* `withComputeCount(java.lang.Double)` was added

#### `models.GiVersions` was modified

* `listByLocation(java.lang.String,models.SystemShapes,java.lang.String,com.azure.core.util.Context)` was added

#### `OracleDatabaseManager` was modified

* `exascaleDbNodes()` was added
* `exadbVmClusters()` was added
* `flexComponents()` was added
* `giMinorVersions()` was added
* `exascaleDbStorageVaults()` was added

#### `models.OracleSubscriptionProperties` was modified

* `addSubscriptionOperationState()` was added
* `lastOperationStatusDetail()` was added
* `azureSubscriptionIds()` was added

#### `models.CloudExadataInfrastructureProperties` was modified

* `computeModel()` was added
* `databaseServerType()` was added
* `withStorageServerType(java.lang.String)` was added
* `storageServerType()` was added
* `withDatabaseServerType(java.lang.String)` was added
* `definedFileSystemConfiguration()` was added

#### `models.AutonomousDatabases` was modified

* `changeDisasterRecoveryConfiguration(java.lang.String,java.lang.String,models.DisasterRecoveryConfigurationDetails)` was added
* `changeDisasterRecoveryConfiguration(java.lang.String,java.lang.String,models.DisasterRecoveryConfigurationDetails,com.azure.core.util.Context)` was added

#### `models.AutonomousDatabaseBaseProperties` was modified

* `remoteDisasterRecoveryConfiguration()` was added
* `timeDisasterRecoveryRoleChanged()` was added
* `withComputeCount(java.lang.Double)` was added

#### `models.AutonomousDatabaseProperties` was modified

* `withComputeCount(java.lang.Double)` was added

#### `models.CloudVmClusterUpdateProperties` was modified

* `withFileSystemConfigurationDetails(java.util.List)` was added
* `withOcpuCount(java.lang.Double)` was added
* `fileSystemConfigurationDetails()` was added

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
