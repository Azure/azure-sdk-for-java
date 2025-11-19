# Release History

## 2.0.0 (2025-11-19)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Azure Database for PostgreSQL management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, network configuration, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2025-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CheckNameAvailabilities` was removed

#### `models.LtrBackupResponse` was removed

#### `models.CheckNameAvailabilityWithLocations` was removed

#### `models.OperationListResult` was removed

#### `models.ZoneRedundantHaSupportedEnum` was removed

#### `models.VirtualEndpointResource$Definition` was removed

#### `models.AzureManagedDiskPerformanceTiers` was removed

#### `models.ActiveDirectoryAdministrator` was removed

#### `models.MigrationResource$Update` was removed

#### `models.ResourceProviders` was removed

#### `models.PrivateLinkResourceListResult` was removed

#### `models.StartDataMigrationEnum` was removed

#### `models.ZoneRedundantHaAndGeoBackupSupportedEnum` was removed

#### `models.VirtualNetworkSubnetUsageResult` was removed

#### `models.ServerThreatProtectionSettingsModel$Definition` was removed

#### `models.FlexibleServers` was removed

#### `models.DatabaseListResult` was removed

#### `models.MigrationDbState` was removed

#### `models.ServerBackup` was removed

#### `models.CreateModeForUpdate` was removed

#### `models.LtrServerBackupOperation` was removed

#### `models.ReplicationPromoteOption` was removed

#### `models.FlexibleServerEditionCapability` was removed

#### `models.FastProvisioningSupportedEnum` was removed

#### `models.Administrators` was removed

#### `models.ServerBackupListResult` was removed

#### `models.PrivateEndpointConnectionOperations` was removed

#### `models.MigrationSubStateDetails` was removed

#### `models.ConfigurationListResult` was removed

#### `models.DbMigrationStatus` was removed

#### `models.ServerThreatProtectionSettingsModel$DefinitionStages` was removed

#### `models.MigrationResource` was removed

#### `models.GeoBackupSupportedEnum` was removed

#### `models.NameAvailability` was removed

#### `models.LocationBasedCapabilities` was removed

#### `models.ActiveDirectoryAuthEnum` was removed

#### `models.FlexibleServersTriggerLtrPreBackupHeaders` was removed

#### `models.ServerThreatProtectionSettingsModel$UpdateStages` was removed

#### `models.MigrationResource$Definition` was removed

#### `models.ActiveDirectoryAdministrator$DefinitionStages` was removed

#### `models.LogFiles` was removed

#### `models.MigrationResourceListResult` was removed

#### `models.ServerThreatProtectionSettingsModel` was removed

#### `models.Backups` was removed

#### `models.ServerThreatProtectionSettingsModel$Update` was removed

#### `models.ActiveDirectoryAdministratorAdd` was removed

#### `models.Origin` was removed

#### `models.LtrBackupRequest` was removed

#### `models.FlexibleServersTriggerLtrPreBackupResponse` was removed

#### `models.ServerThreatProtectionListResult` was removed

#### `models.RestrictedEnum` was removed

#### `models.OverwriteDbsInTargetEnum` was removed

#### `models.ArmServerKeyType` was removed

#### `models.StorageAutoGrowthSupportedEnum` was removed

#### `models.VirtualEndpointResource$Update` was removed

#### `models.PasswordAuthEnum` was removed

#### `models.OnlineResizeSupportedEnum` was removed

#### `models.MigrationNameAvailabilityResource` was removed

#### `models.ServerForUpdate` was removed

#### `models.ActiveDirectoryAdministrator$Definition` was removed

#### `models.MigrationResource$UpdateStages` was removed

#### `models.LogicalReplicationOnSourceDbEnum` was removed

#### `models.KeyStatusEnum` was removed

#### `models.CancelEnum` was removed

#### `models.FlexibleServerCapability` was removed

#### `models.ServerVersion` was removed

#### `models.FirewallRuleListResult` was removed

#### `models.LtrBackupOperations` was removed

#### `models.ServerCapabilities` was removed

#### `models.AdministratorListResult` was removed

#### `models.LogFile` was removed

#### `models.VirtualEndpointResource` was removed

#### `models.CapabilitiesListResult` was removed

#### `models.LogFileListResult` was removed

#### `models.ServerHAState` was removed

#### `models.VirtualEndpointsListResult` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.MigrateRolesEnum` was removed

#### `models.GeoRedundantBackupEnum` was removed

#### `models.HaMode` was removed

#### `models.GetPrivateDnsZoneSuffixes` was removed

#### `models.TriggerCutoverEnum` was removed

#### `models.MigrationResource$DefinitionStages` was removed

#### `models.ServerListResult` was removed

#### `models.MigrationSubState` was removed

#### `models.VirtualEndpointResource$DefinitionStages` was removed

#### `models.VirtualEndpointResource$UpdateStages` was removed

#### `models.Backup` was modified

* `withGeoRedundantBackup(models.GeoRedundantBackupEnum)` was removed
* `models.GeoRedundantBackupEnum geoRedundantBackup()` -> `models.GeographicallyRedundantBackup geoRedundantBackup()`

#### `models.Operations` was modified

* `models.OperationListResult list()` -> `com.azure.core.http.rest.PagedIterable list()`
* `listWithResponse(com.azure.core.util.Context)` was removed

#### `models.Server$Update` was modified

* `withAdministratorLogin(java.lang.String)` was removed
* `withAuthConfig(models.AuthConfig)` was removed
* `withMaintenanceWindow(models.MaintenanceWindow)` was removed
* `withHighAvailability(models.HighAvailability)` was removed
* `withCreateMode(models.CreateModeForUpdate)` was removed
* `withVersion(models.ServerVersion)` was removed
* `withSku(models.Sku)` was removed
* `withBackup(models.Backup)` was removed

#### `models.Server$Definition` was modified

* `withVersion(models.ServerVersion)` was removed

#### `PostgreSqlManager` was modified

* `getPrivateDnsZoneSuffixes()` was removed
* `locationBasedCapabilities()` was removed
* `administrators()` was removed
* `ltrBackupOperations()` was removed
* `checkNameAvailabilities()` was removed
* `checkNameAvailabilityWithLocations()` was removed
* `serverCapabilities()` was removed
* `flexibleServers()` was removed
* `logFiles()` was removed
* `backups()` was removed
* `privateEndpointConnectionOperations()` was removed
* `resourceProviders()` was removed

#### `models.HighAvailability` was modified

* `models.ServerHAState state()` -> `models.HighAvailabilityState state()`

#### `models.Migrations` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.MigrationResource getById(java.lang.String)` -> `models.Migration getById(java.lang.String)`
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `listByTargetServer(java.lang.String,java.lang.String,java.lang.String)` was removed
* `models.MigrationResource$DefinitionStages$Blank define(java.lang.String)` -> `models.Migration$DefinitionStages$Blank define(java.lang.String)`
* `listByTargetServer(java.lang.String,java.lang.String,java.lang.String,models.MigrationListFilter,com.azure.core.util.Context)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteById(java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.Replica` was modified

* `models.ReplicationPromoteOption promoteOption()` -> `models.ReadReplicaPromoteOption promoteOption()`
* `withPromoteOption(models.ReplicationPromoteOption)` was removed

#### `models.MigrationResourceForPatch` was modified

* `models.CancelEnum cancel()` -> `models.Cancel cancel()`
* `models.LogicalReplicationOnSourceDbEnum setupLogicalReplicationOnSourceDbIfNeeded()` -> `models.LogicalReplicationOnSourceServer setupLogicalReplicationOnSourceDbIfNeeded()`
* `withCancel(models.CancelEnum)` was removed
* `withSecretParameters(models.MigrationSecretParameters)` was removed
* `withTriggerCutover(models.TriggerCutoverEnum)` was removed
* `withMigrateRoles(models.MigrateRolesEnum)` was removed
* `models.TriggerCutoverEnum triggerCutover()` -> `models.TriggerCutover triggerCutover()`
* `models.MigrationSecretParameters secretParameters()` -> `models.MigrationSecretParametersForPatch secretParameters()`
* `withStartDataMigration(models.StartDataMigrationEnum)` was removed
* `models.StartDataMigrationEnum startDataMigration()` -> `models.StartDataMigration startDataMigration()`
* `models.OverwriteDbsInTargetEnum overwriteDbsInTarget()` -> `models.OverwriteDatabasesOnTargetServer overwriteDbsInTarget()`
* `withSetupLogicalReplicationOnSourceDbIfNeeded(models.LogicalReplicationOnSourceDbEnum)` was removed
* `withOverwriteDbsInTarget(models.OverwriteDbsInTargetEnum)` was removed
* `models.MigrateRolesEnum migrateRoles()` -> `models.MigrateRolesAndPermissions migrateRoles()`

#### `models.Storage` was modified

* `models.AzureManagedDiskPerformanceTiers tier()` -> `models.AzureManagedDiskPerformanceTier tier()`
* `withTier(models.AzureManagedDiskPerformanceTiers)` was removed

#### `models.VirtualEndpoints` was modified

* `models.VirtualEndpointResource getById(java.lang.String)` -> `models.VirtualEndpoint getById(java.lang.String)`
* `models.VirtualEndpointResource get(java.lang.String,java.lang.String,java.lang.String)` -> `models.VirtualEndpoint get(java.lang.String,java.lang.String,java.lang.String)`
* `models.VirtualEndpointResource$DefinitionStages$Blank define(java.lang.String)` -> `models.VirtualEndpoint$DefinitionStages$Blank define(java.lang.String)`

#### `models.MigrationStatus` was modified

* `models.MigrationSubStateDetails currentSubStateDetails()` -> `models.MigrationSubstateDetails currentSubStateDetails()`

#### `models.DataEncryption` was modified

* `withGeoBackupEncryptionKeyStatus(models.KeyStatusEnum)` was removed
* `models.KeyStatusEnum geoBackupEncryptionKeyStatus()` -> `models.EncryptionKeyStatus geoBackupEncryptionKeyStatus()`
* `models.KeyStatusEnum primaryEncryptionKeyStatus()` -> `models.EncryptionKeyStatus primaryEncryptionKeyStatus()`
* `withType(models.ArmServerKeyType)` was removed
* `withPrimaryEncryptionKeyStatus(models.KeyStatusEnum)` was removed
* `models.ArmServerKeyType type()` -> `models.DataEncryptionType type()`

#### `models.Operation` was modified

* `Operation()` was removed
* `java.lang.Boolean isDataAction()` -> `java.lang.Boolean isDataAction()`
* `withIsDataAction(java.lang.Boolean)` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `java.lang.String name()` -> `java.lang.String name()`
* `models.OperationOrigin origin()` -> `models.OperationOrigin origin()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `validate()` was removed
* `models.OperationDisplay display()` -> `models.OperationDisplay display()`
* `java.util.Map properties()` -> `java.util.Map properties()`

#### `models.VirtualNetworkSubnetUsages` was modified

* `execute(java.lang.String,models.VirtualNetworkSubnetUsageParameter)` was removed
* `executeWithResponse(java.lang.String,models.VirtualNetworkSubnetUsageParameter,com.azure.core.util.Context)` was removed

#### `models.Server` was modified

* `models.ServerVersion version()` -> `models.PostgresMajorVersion version()`

#### `models.ServerThreatProtectionSettings` was modified

* `models.ServerThreatProtectionSettingsModel$DefinitionStages$Blank define(models.ThreatProtectionName)` -> `models.AdvancedThreatProtectionSettingsModel$DefinitionStages$Blank define(models.ThreatProtectionName)`
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String)` was removed
* `listByServer(java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,models.ThreatProtectionName,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,models.ThreatProtectionName)` was removed
* `listByServer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AuthConfig` was modified

* `withPasswordAuth(models.PasswordAuthEnum)` was removed
* `withActiveDirectoryAuth(models.ActiveDirectoryAuthEnum)` was removed
* `models.PasswordAuthEnum passwordAuth()` -> `models.PasswordBasedAuth passwordAuth()`
* `models.ActiveDirectoryAuthEnum activeDirectoryAuth()` -> `models.MicrosoftEntraAuth activeDirectoryAuth()`

### Features Added

* `models.FastProvisioningSupport` was added

* `models.QuotaUsages` was added

* `models.AdministratorMicrosoftEntra` was added

* `models.NameAvailabilityModel` was added

* `models.AdministratorMicrosoftEntra$Update` was added

* `models.PasswordBasedAuth` was added

* `models.VirtualEndpoint$DefinitionStages` was added

* `models.ServerList` was added

* `models.ObjectRecommendationDetails` was added

* `models.FirewallRuleList` was added

* `models.LogicalReplicationOnSourceServer` was added

* `models.PrivateEndpointConnectionList` was added

* `models.NameProperty` was added

* `models.TuningOptionsList` was added

* `models.SkuForPatch` was added

* `models.CapabilitiesByServers` was added

* `models.Migration$Definition` was added

* `models.ObjectRecommendationList` was added

* `models.ObjectRecommendationPropertiesImplementationDetails` was added

* `models.RecommendationTypeEnum` was added

* `models.BackupsLongTermRetentionResponse` was added

* `models.EncryptionKeyStatus` was added

* `models.SupportedFeature` was added

* `models.AdministratorMicrosoftEntra$DefinitionStages` was added

* `models.CapturedLog` was added

* `models.Migration` was added

* `models.Migration$Update` was added

* `models.AdvancedThreatProtectionSettingsModel$Definition` was added

* `models.StorageAutoGrowthSupport` was added

* `models.ConfigurationList` was added

* `models.BackupsLongTermRetentions` was added

* `models.StartDataMigration` was added

* `models.OperationList` was added

* `models.BackupsLongTermRetentionsCheckPrerequisitesResponse` was added

* `models.BackupForPatch` was added

* `models.PrivateLinkResourceList` was added

* `models.ServerForPatch` was added

* `models.Cluster` was added

* `models.QuotaUsageList` was added

* `models.BackupsLongTermRetentionsCheckPrerequisitesHeaders` was added

* `models.TuningOptionParameterEnum` was added

* `models.MigrateRolesAndPermissions` was added

* `models.Migration$DefinitionStages` was added

* `models.CapturedLogs` was added

* `models.ReadReplicaPromoteOption` was added

* `models.BackupType` was added

* `models.PrivateDnsZoneSuffixes` was added

* `models.MigrationDatabaseState` was added

* `models.MigrationSubstate` was added

* `models.FeatureStatus` was added

* `models.ObjectRecommendation` was added

* `models.QuotaUsage` was added

* `models.AdministratorsMicrosoftEntras` was added

* `models.BackupAutomaticAndOnDemandList` was added

* `models.ImpactRecord` was added

* `models.ZoneRedundantHighAvailabilityAndGeographicallyRedundantBackupSupport` was added

* `models.MicrosoftEntraAuth` was added

* `models.LocationRestricted` was added

* `models.CapabilitiesByLocations` was added

* `models.GeographicallyRedundantBackup` was added

* `models.HighAvailabilityForPatch` was added

* `models.Migration$UpdateStages` was added

* `models.NameAvailabilities` was added

* `models.DatabaseList` was added

* `models.TuningOptions` was added

* `models.MaintenanceWindowForPatch` was added

* `models.TriggerCutover` was added

* `models.AuthConfigForPatch` was added

* `models.AdvancedThreatProtectionSettingsList` was added

* `models.AdvancedThreatProtectionSettingsModel$Update` was added

* `models.CreateModeForPatch` was added

* `models.VirtualNetworkSubnetUsageModel` was added

* `models.AdministratorMicrosoftEntraList` was added

* `models.BackupsAutomaticAndOnDemands` was added

* `models.HighAvailabilityState` was added

* `models.GeographicallyRedundantBackupSupport` was added

* `models.VirtualEndpoint$Update` was added

* `models.AdministratorMicrosoftEntraAdd` was added

* `models.DatabaseMigrationState` was added

* `models.AdvancedThreatProtectionSettingsModel$DefinitionStages` was added

* `models.BackupAutomaticAndOnDemand` was added

* `models.VirtualEndpoint$UpdateStages` was added

* `models.BackupsLongTermRetentionOperation` was added

* `models.OnlineStorageResizeSupport` was added

* `models.VirtualEndpoint$Definition` was added

* `models.TuningOptionsOperations` was added

* `models.AdvancedThreatProtectionSettingsModel` was added

* `models.BackupsLongTermRetentionRequest` was added

* `models.MigrationList` was added

* `models.CapabilityList` was added

* `models.AzureManagedDiskPerformanceTier` was added

* `models.VirtualEndpointsList` was added

* `models.AdminCredentialsForPatch` was added

* `models.Cancel` was added

* `models.CapturedLogList` was added

* `models.ZoneRedundantHighAvailabilitySupport` was added

* `models.AdvancedThreatProtectionSettings` was added

* `models.MigrationSecretParametersForPatch` was added

* `models.VirtualEndpoint` was added

* `models.PostgresMajorVersion` was added

* `models.AdvancedThreatProtectionSettingsModel$UpdateStages` was added

* `models.OverwriteDatabasesOnTargetServer` was added

* `models.Capability` was added

* `models.MigrationNameAvailability` was added

* `models.DataEncryptionType` was added

* `models.RecommendationTypeParameterEnum` was added

* `models.ObjectRecommendationPropertiesAnalyzedWorkload` was added

* `models.AdministratorMicrosoftEntra$Definition` was added

* `models.AdministratorMicrosoftEntra$UpdateStages` was added

* `models.MigrationSubstateDetails` was added

* `models.ServerEditionCapability` was added

#### `models.Backup` was modified

* `withGeoRedundantBackup(models.GeographicallyRedundantBackup)` was added

#### `models.ServerVersionCapability` was modified

* `supportedFeatures()` was added

#### `models.Operations` was modified

* `list(com.azure.core.util.Context)` was added

#### `models.Server$Update` was modified

* `withSku(models.SkuForPatch)` was added
* `withAuthConfig(models.AuthConfigForPatch)` was added
* `withCluster(models.Cluster)` was added
* `withAvailabilityZone(java.lang.String)` was added
* `withMaintenanceWindow(models.MaintenanceWindowForPatch)` was added
* `withHighAvailability(models.HighAvailabilityForPatch)` was added
* `withBackup(models.BackupForPatch)` was added
* `withCreateMode(models.CreateModeForPatch)` was added
* `withVersion(models.PostgresMajorVersion)` was added

#### `models.Server$Definition` was modified

* `withCluster(models.Cluster)` was added
* `withVersion(models.PostgresMajorVersion)` was added

#### `PostgreSqlManager` was modified

* `advancedThreatProtectionSettings()` was added
* `capabilitiesByServers()` was added
* `administratorsMicrosoftEntras()` was added
* `quotaUsages()` was added
* `capabilitiesByLocations()` was added
* `nameAvailabilities()` was added
* `backupsAutomaticAndOnDemands()` was added
* `backupsLongTermRetentions()` was added
* `capturedLogs()` was added
* `tuningOptionsOperations()` was added
* `privateDnsZoneSuffixes()` was added

#### `models.Migrations` was modified

* `checkNameAvailability(java.lang.String,java.lang.String,fluent.models.MigrationNameAvailabilityInner)` was added
* `listByTargetServer(java.lang.String,java.lang.String,models.MigrationListFilter,com.azure.core.util.Context)` was added
* `checkNameAvailabilityWithResponse(java.lang.String,java.lang.String,fluent.models.MigrationNameAvailabilityInner,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `cancelWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByTargetServer(java.lang.String,java.lang.String)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added
* `cancel(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.Replica` was modified

* `withPromoteOption(models.ReadReplicaPromoteOption)` was added

#### `models.MigrationResourceForPatch` was modified

* `withOverwriteDbsInTarget(models.OverwriteDatabasesOnTargetServer)` was added
* `withCancel(models.Cancel)` was added
* `withTriggerCutover(models.TriggerCutover)` was added
* `withStartDataMigration(models.StartDataMigration)` was added
* `withSecretParameters(models.MigrationSecretParametersForPatch)` was added
* `withSetupLogicalReplicationOnSourceDbIfNeeded(models.LogicalReplicationOnSourceServer)` was added
* `withMigrateRoles(models.MigrateRolesAndPermissions)` was added

#### `models.Storage` was modified

* `withTier(models.AzureManagedDiskPerformanceTier)` was added

#### `models.UserAssignedIdentity` was modified

* `principalId()` was added
* `withPrincipalId(java.lang.String)` was added

#### `models.PrivateEndpointConnections` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,com.azure.core.util.Context)` was added

#### `models.DataEncryption` was modified

* `withGeoBackupEncryptionKeyStatus(models.EncryptionKeyStatus)` was added
* `withPrimaryEncryptionKeyStatus(models.EncryptionKeyStatus)` was added
* `withType(models.DataEncryptionType)` was added

#### `models.ServerSkuCapability` was modified

* `supportedFeatures()` was added
* `securityProfile()` was added

#### `models.Operation` was modified

* `innerModel()` was added

#### `models.VirtualNetworkSubnetUsages` was modified

* `list(java.lang.String,models.VirtualNetworkSubnetUsageParameter)` was added
* `listWithResponse(java.lang.String,models.VirtualNetworkSubnetUsageParameter,com.azure.core.util.Context)` was added

#### `models.Server` was modified

* `cluster()` was added

#### `models.AuthConfig` was modified

* `withPasswordAuth(models.PasswordBasedAuth)` was added
* `withActiveDirectoryAuth(models.MicrosoftEntraAuth)` was added

## 1.2.0-beta.1 (2025-05-16)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2025-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was modified

* `innerModel()` was removed
* `java.lang.String nextLink()` -> `java.lang.String nextLink()`
* `java.util.List value()` -> `java.util.List value()`

#### `models.Operation` was modified

* `toJson(com.azure.json.JsonWriter)` was removed
* `withIsDataAction(java.lang.Boolean)` was removed
* `java.util.Map properties()` -> `java.util.Map properties()`
* `java.lang.Boolean isDataAction()` -> `java.lang.Boolean isDataAction()`
* `java.lang.String name()` -> `java.lang.String name()`
* `models.OperationDisplay display()` -> `models.OperationDisplay display()`
* `models.OperationOrigin origin()` -> `models.OperationOrigin origin()`
* `validate()` was removed
* `fromJson(com.azure.json.JsonReader)` was removed

#### `models.Operations` was modified

* `listWithResponse(com.azure.core.util.Context)` was removed
* `models.OperationListResult list()` -> `com.azure.core.http.rest.PagedIterable list()`

### Features Added

* `models.SupportedFeatureStatusEnum` was added

* `models.QuotaUsages` was added

* `models.ConfigTuningRequestParameter` was added

* `models.TuningOptionsListResult` was added

* `models.SessionResource` was added

* `models.QuotaUsage` was added

* `models.IndexRecommendationListResult` was added

* `models.ImpactRecord` was added

* `models.SessionDetailsListResult` was added

* `models.QuotaUsagesListResult` was added

* `models.NameProperty` was added

* `models.IndexRecommendationResourcePropertiesAnalyzedWorkload` was added

* `models.TuningOptions` was added

* `models.IndexRecommendationDetails` was added

* `models.IndexRecommendationResource` was added

* `models.RecommendationTypeEnum` was added

* `models.SupportedFeature` was added

* `models.IndexRecommendationResourcePropertiesImplementationDetails` was added

* `models.SessionsListResult` was added

* `models.RecommendationType` was added

* `models.TuningOptionEnum` was added

* `models.TuningConfigurations` was added

* `models.Cluster` was added

* `models.TuningIndexes` was added

* `models.TuningOptionsResource` was added

* `models.SessionDetailsResource` was added

#### `models.OperationListResult` was modified

* `validate()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withNextLink(java.lang.String)` was added

#### `models.UserAssignedIdentity` was modified

* `withPrincipalId(java.lang.String)` was added
* `principalId()` was added

#### `models.ServerForUpdate` was modified

* `withCluster(models.Cluster)` was added
* `cluster()` was added

#### `models.FlexibleServerCapability` was modified

* `supportedFeatures()` was added

#### `models.ServerVersionCapability` was modified

* `supportedFeatures()` was added

#### `models.ServerSkuCapability` was modified

* `securityProfile()` was added
* `supportedFeatures()` was added

#### `models.Operation` was modified

* `innerModel()` was added

#### `models.Operations` was modified

* `list(com.azure.core.util.Context)` was added

#### `models.Server$Update` was modified

* `withCluster(models.Cluster)` was added

#### `models.Server` was modified

* `cluster()` was added

#### `models.Server$Definition` was modified

* `withCluster(models.Cluster)` was added

#### `PostgreSqlManager` was modified

* `tuningIndexes()` was added
* `quotaUsages()` was added
* `tuningOptions()` was added
* `tuningConfigurations()` was added

## 1.1.0 (2025-01-06)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2024-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NodeTypeCapability` was removed

#### `models.StorageMBCapability` was removed

#### `models.HyperscaleNodeEditionCapability` was removed

#### `models.CapabilityProperties` was removed

#### `models.VcoreCapability` was removed

#### `models.StorageTierCapability` was modified

* `tierName()` was removed
* `java.lang.Long iops()` -> `java.lang.Integer iops()`
* `isBaseline()` was removed
* `java.lang.String status()` -> `models.CapabilityStatus status()`

#### `models.FastProvisioningEditionCapability` was modified

* `java.lang.Long supportedStorageGb()` -> `java.lang.Integer supportedStorageGb()`

#### `models.ServerVersionCapability` was modified

* `supportedVcores()` was removed
* `java.lang.String status()` -> `models.CapabilityStatus status()`

#### `models.FlexibleServerEditionCapability` was modified

* `supportedServerVersions()` was removed
* `java.lang.String status()` -> `models.CapabilityStatus status()`

#### `models.StorageEditionCapability` was modified

* `supportedStorageMB()` was removed
* `java.lang.String status()` -> `models.CapabilityStatus status()`

### Features Added

* `models.ServerThreatProtectionSettingsModel$UpdateStages` was added

* `models.ExecutionStatus` was added

* `models.MigrationResource$Definition` was added

* `models.LogFiles` was added

* `models.ThreatProtectionState` was added

* `models.MigrationResourceListResult` was added

* `models.ServerThreatProtectionSettingsModel` was added

* `models.LtrBackupResponse` was added

* `models.MigrationOption` was added

* `models.PrivateLinkResource` was added

* `models.Migrations` was added

* `models.Replica` was added

* `models.ServerThreatProtectionSettingsModel$Update` was added

* `models.MigrationMode` was added

* `models.LtrBackupRequest` was added

* `models.FlexibleServersTriggerLtrPreBackupResponse` was added

* `models.ZoneRedundantHaSupportedEnum` was added

* `models.ValidationState` was added

* `models.ReadReplicaPromoteMode` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.MigrationResourceForPatch` was added

* `models.ServerThreatProtectionListResult` was added

* `models.RestrictedEnum` was added

* `models.PrivateLinkResources` was added

* `models.VirtualEndpoints` was added

* `models.OverwriteDbsInTargetEnum` was added

* `models.VirtualEndpointResource$Definition` was added

* `models.MigrationState` was added

* `models.CapabilityStatus` was added

* `models.AzureManagedDiskPerformanceTiers` was added

* `models.StorageAutoGrowthSupportedEnum` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.MigrationResource$Update` was added

* `models.VirtualEndpointResource$Update` was added

* `models.ResourceProviders` was added

* `models.PrivateLinkResourceListResult` was added

* `models.MigrationStatus` was added

* `models.PrivateEndpointConnections` was added

* `models.StartDataMigrationEnum` was added

* `models.ZoneRedundantHaAndGeoBackupSupportedEnum` was added

* `models.StorageMbCapability` was added

* `models.OnlineResizeSupportedEnum` was added

* `models.MigrationNameAvailabilityResource` was added

* `models.ServerSku` was added

* `models.ThreatProtectionName` was added

* `models.PrivateEndpoint` was added

* `models.BackupStoreDetails` was added

* `models.ServerThreatProtectionSettingsModel$Definition` was added

* `models.MigrationResource$UpdateStages` was added

* `models.FlexibleServers` was added

* `models.LogicalReplicationOnSourceDbEnum` was added

* `models.MigrationDbState` was added

* `models.KeyStatusEnum` was added

* `models.CancelEnum` was added

* `models.MigrationListFilter` was added

* `models.FlexibleServerCapability` was added

* `models.ReplicationState` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.CapabilityBase` was added

* `models.MigrationNameAvailabilityReason` was added

* `models.LtrBackupOperations` was added

* `models.LtrServerBackupOperation` was added

* `models.ServerCapabilities` was added

* `models.ValidationDetails` was added

* `models.ReplicationPromoteOption` was added

* `models.FastProvisioningSupportedEnum` was added

* `models.LogFile` was added

* `models.VirtualEndpointResourceForPatch` was added

* `models.BackupRequestBase` was added

* `models.ServerSkuCapability` was added

* `models.StorageType` was added

* `models.ValidationMessage` was added

* `models.VirtualEndpointResource` was added

* `models.SourceType` was added

* `models.LogFileListResult` was added

* `models.StorageAutoGrow` was added

* `models.PrivateEndpointConnectionOperations` was added

* `models.VirtualEndpointsListResult` was added

* `models.BackupSettings` was added

* `models.MigrationSubStateDetails` was added

* `models.LtrServerBackupOperationList` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.DbMigrationStatus` was added

* `models.MigrateRolesEnum` was added

* `models.ValidationSummaryItem` was added

* `models.LtrPreBackupRequest` was added

* `models.LtrPreBackupResponse` was added

* `models.ServerThreatProtectionSettingsModel$DefinitionStages` was added

* `models.MigrationResource` was added

* `models.HaMode` was added

* `models.GeoBackupSupportedEnum` was added

* `models.MigrationSecretParameters` was added

* `models.TriggerCutoverEnum` was added

* `models.VirtualEndpointType` was added

* `models.DbLevelValidationStatus` was added

* `models.ServerThreatProtectionSettings` was added

* `models.MigrationResource$DefinitionStages` was added

* `models.MigrationSubState` was added

* `models.PrivateEndpointConnection` was added

* `models.VirtualEndpointResource$DefinitionStages` was added

* `models.DbServerMetadata` was added

* `models.FlexibleServersTriggerLtrPreBackupHeaders` was added

* `models.AdminCredentials` was added

* `models.VirtualEndpointResource$UpdateStages` was added

* `models.SslMode` was added

#### `models.Backup` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageTierCapability` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `reason()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HighAvailability` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkSubnetUsageParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Backups` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `create(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `create(java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ActiveDirectoryAdministratorAdd` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FastProvisioningEditionCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `supportedTier()` was added
* `status()` was added
* `reason()` was added
* `serverCount()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Storage` was modified

* `withThroughput(java.lang.Integer)` was added
* `withAutoGrow(models.StorageAutoGrow)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withTier(models.AzureManagedDiskPerformanceTiers)` was added
* `iops()` was added
* `autoGrow()` was added
* `withType(models.StorageType)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `tier()` was added
* `withIops(java.lang.Integer)` was added
* `throughput()` was added
* `type()` was added

#### `models.UserIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DelegatedSubnetUsage` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerForUpdate` was modified

* `replica()` was added
* `withAdministratorLogin(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `administratorLogin()` was added
* `withReplica(models.Replica)` was added

#### `models.DatabaseListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FirewallRuleListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataEncryption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withPrimaryEncryptionKeyStatus(models.KeyStatusEnum)` was added
* `geoBackupEncryptionKeyStatus()` was added
* `geoBackupUserAssignedIdentityId()` was added
* `geoBackupKeyUri()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withGeoBackupEncryptionKeyStatus(models.KeyStatusEnum)` was added
* `primaryEncryptionKeyStatus()` was added
* `withGeoBackupKeyUri(java.lang.String)` was added
* `withGeoBackupUserAssignedIdentityId(java.lang.String)` was added

#### `models.RestartParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerVersionCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `reason()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AdministratorListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FlexibleServerEditionCapability` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `reason()` was added
* `supportedServerSkus()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `defaultSkuName()` was added

#### `models.CapabilitiesListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConfigurationForUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Operation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerBackupListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MaintenanceWindow` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Network` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withPublicNetworkAccess(models.ServerPublicNetworkAccessState)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Sku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConfigurationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Server$Update` was modified

* `withReplica(models.Replica)` was added
* `withAdministratorLogin(java.lang.String)` was added

#### `models.Server` was modified

* `replica()` was added
* `privateEndpointConnections()` was added

#### `models.ServerListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageEditionCapability` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `reason()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `supportedStorageMb()` was added
* `defaultStorageSizeMb()` was added

#### `models.AuthConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `PostgreSqlManager` was modified

* `serverThreatProtectionSettings()` was added
* `privateEndpointConnections()` was added
* `migrations()` was added
* `privateEndpointConnectionOperations()` was added
* `logFiles()` was added
* `virtualEndpoints()` was added
* `resourceProviders()` was added
* `flexibleServers()` was added
* `serverCapabilities()` was added
* `ltrBackupOperations()` was added
* `privateLinkResources()` was added

## 1.1.0-beta.4 (2024-12-06)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2023-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.StorageTierCapability` was modified

* `status()` was added
* `reason()` was added

#### `models.FastProvisioningEditionCapability` was modified

* `reason()` was added
* `status()` was added

#### `models.StorageMbCapability` was modified

* `status()` was added
* `reason()` was added

#### `models.ServerVersionCapability` was modified

* `reason()` was added
* `status()` was added

#### `models.FlexibleServerEditionCapability` was modified

* `status()` was added
* `reason()` was added

#### `models.ServerSkuCapability` was modified

* `status()` was added
* `reason()` was added

#### `models.StorageEditionCapability` was modified

* `status()` was added
* `reason()` was added

## 1.1.0-beta.3 (2024-04-18)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2023-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.MigrateRolesEnum` was added

#### `models.MigrationResource$Definition` was modified

* `withMigrateRoles(models.MigrateRolesEnum)` was added
* `withMigrationInstanceResourceId(java.lang.String)` was added

#### `models.MigrationResourceForPatch` was modified

* `migrateRoles()` was added
* `withMigrateRoles(models.MigrateRolesEnum)` was added

#### `models.MigrationResource$Update` was modified

* `withMigrateRoles(models.MigrateRolesEnum)` was added

#### `models.MigrationResource` was modified

* `migrateRoles()` was added
* `migrationInstanceResourceId()` was added

## 1.1.0-beta.2 (2024-01-22)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2023-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.1.0-beta.1 (2023-11-14)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2023-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.NodeTypeCapability` was removed

* `models.StorageMBCapability` was removed

* `models.HyperscaleNodeEditionCapability` was removed

* `models.CapabilityProperties` was removed

* `models.VcoreCapability` was removed

#### `models.StorageTierCapability` was modified

* `status()` was removed
* `isBaseline()` was removed
* `tierName()` was removed
* `java.lang.Long iops()` -> `java.lang.Integer iops()`

#### `models.FastProvisioningEditionCapability` was modified

* `java.lang.Long supportedStorageGb()` -> `java.lang.Integer supportedStorageGb()`

#### `models.ServerVersionCapability` was modified

* `status()` was removed
* `supportedVcores()` was removed

#### `models.FlexibleServerEditionCapability` was modified

* `supportedServerVersions()` was removed
* `status()` was removed

#### `models.StorageEditionCapability` was modified

* `status()` was removed
* `supportedStorageMB()` was removed

### Features Added

* `models.QuotaUsages` was added

* `models.ServerThreatProtectionSettingsModel$UpdateStages` was added

* `models.ExecutionStatus` was added

* `models.MigrationResource$Definition` was added

* `models.LogFiles` was added

* `models.ThreatProtectionState` was added

* `models.QuotaUsage` was added

* `models.MigrationResourceListResult` was added

* `models.ServerThreatProtectionSettingsModel` was added

* `models.LtrBackupResponse` was added

* `models.MigrationOption` was added

* `models.PrivateLinkResource` was added

* `models.Migrations` was added

* `models.Replica` was added

* `models.ServerThreatProtectionSettingsModel$Update` was added

* `models.MigrationMode` was added

* `models.QuotaUsagesListResult` was added

* `models.LtrBackupRequest` was added

* `models.FlexibleServersTriggerLtrPreBackupResponse` was added

* `models.ZoneRedundantHaSupportedEnum` was added

* `models.NameProperty` was added

* `models.ValidationState` was added

* `models.ReadReplicaPromoteMode` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.MigrationResourceForPatch` was added

* `models.ServerThreatProtectionListResult` was added

* `models.RestrictedEnum` was added

* `models.PrivateLinkResources` was added

* `models.VirtualEndpoints` was added

* `models.OverwriteDbsInTargetEnum` was added

* `models.VirtualEndpointResource$Definition` was added

* `models.MigrationState` was added

* `models.CapabilityStatus` was added

* `models.AzureManagedDiskPerformanceTiers` was added

* `models.StorageAutoGrowthSupportedEnum` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.MigrationResource$Update` was added

* `models.VirtualEndpointResource$Update` was added

* `models.ResourceProviders` was added

* `models.PrivateLinkResourceListResult` was added

* `models.MigrationStatus` was added

* `models.PrivateEndpointConnections` was added

* `models.StartDataMigrationEnum` was added

* `models.ZoneRedundantHaAndGeoBackupSupportedEnum` was added

* `models.StorageMbCapability` was added

* `models.OnlineResizeSupportedEnum` was added

* `models.MigrationNameAvailabilityResource` was added

* `models.ServerSku` was added

* `models.ThreatProtectionName` was added

* `models.PrivateEndpoint` was added

* `models.BackupStoreDetails` was added

* `models.ServerThreatProtectionSettingsModel$Definition` was added

* `models.MigrationResource$UpdateStages` was added

* `models.FlexibleServers` was added

* `models.LogicalReplicationOnSourceDbEnum` was added

* `models.MigrationDbState` was added

* `models.KeyStatusEnum` was added

* `models.CancelEnum` was added

* `models.MigrationListFilter` was added

* `models.FlexibleServerCapability` was added

* `models.ReplicationState` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.CapabilityBase` was added

* `models.MigrationNameAvailabilityReason` was added

* `models.LtrBackupOperations` was added

* `models.LtrServerBackupOperation` was added

* `models.ServerCapabilities` was added

* `models.ValidationDetails` was added

* `models.ReplicationPromoteOption` was added

* `models.FastProvisioningSupportedEnum` was added

* `models.LogFile` was added

* `models.VirtualEndpointResourceForPatch` was added

* `models.BackupRequestBase` was added

* `models.ServerSkuCapability` was added

* `models.StorageType` was added

* `models.ValidationMessage` was added

* `models.VirtualEndpointResource` was added

* `models.SourceType` was added

* `models.LogFileListResult` was added

* `models.StorageAutoGrow` was added

* `models.PrivateEndpointConnectionOperations` was added

* `models.VirtualEndpointsListResult` was added

* `models.BackupSettings` was added

* `models.MigrationSubStateDetails` was added

* `models.LtrServerBackupOperationList` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.DbMigrationStatus` was added

* `models.ValidationSummaryItem` was added

* `models.LtrPreBackupRequest` was added

* `models.LtrPreBackupResponse` was added

* `models.ServerThreatProtectionSettingsModel$DefinitionStages` was added

* `models.MigrationResource` was added

* `models.HaMode` was added

* `models.GeoBackupSupportedEnum` was added

* `models.MigrationSecretParameters` was added

* `models.TriggerCutoverEnum` was added

* `models.VirtualEndpointType` was added

* `models.DbLevelValidationStatus` was added

* `models.ServerThreatProtectionSettings` was added

* `models.MigrationResource$DefinitionStages` was added

* `models.MigrationSubState` was added

* `models.PrivateEndpointConnection` was added

* `models.VirtualEndpointResource$DefinitionStages` was added

* `models.DbServerMetadata` was added

* `models.FlexibleServersTriggerLtrPreBackupHeaders` was added

* `models.AdminCredentials` was added

* `models.VirtualEndpointResource$UpdateStages` was added

* `models.SslMode` was added

#### `models.FastProvisioningEditionCapability` was modified

* `supportedTier()` was added
* `serverCount()` was added

#### `models.Storage` was modified

* `withAutoGrow(models.StorageAutoGrow)` was added
* `iops()` was added
* `tier()` was added
* `withType(models.StorageType)` was added
* `throughput()` was added
* `withThroughput(java.lang.Integer)` was added
* `withTier(models.AzureManagedDiskPerformanceTiers)` was added
* `type()` was added
* `withIops(java.lang.Integer)` was added
* `autoGrow()` was added

#### `models.ServerForUpdate` was modified

* `replica()` was added
* `withReplica(models.Replica)` was added

#### `models.DataEncryption` was modified

* `geoBackupKeyUri()` was added
* `primaryEncryptionKeyStatus()` was added
* `withGeoBackupEncryptionKeyStatus(models.KeyStatusEnum)` was added
* `withGeoBackupUserAssignedIdentityId(java.lang.String)` was added
* `geoBackupEncryptionKeyStatus()` was added
* `withGeoBackupKeyUri(java.lang.String)` was added
* `geoBackupUserAssignedIdentityId()` was added
* `withPrimaryEncryptionKeyStatus(models.KeyStatusEnum)` was added

#### `models.FlexibleServerEditionCapability` was modified

* `defaultSkuName()` was added
* `supportedServerSkus()` was added

#### `models.Network` was modified

* `withPublicNetworkAccess(models.ServerPublicNetworkAccessState)` was added

#### `models.Server$Update` was modified

* `withReplica(models.Replica)` was added

#### `models.Server` was modified

* `privateEndpointConnections()` was added
* `replica()` was added

#### `models.StorageEditionCapability` was modified

* `defaultStorageSizeMb()` was added
* `supportedStorageMb()` was added

#### `PostgreSqlManager` was modified

* `resourceProviders()` was added
* `flexibleServers()` was added
* `logFiles()` was added
* `privateEndpointConnectionOperations()` was added
* `migrations()` was added
* `serverThreatProtectionSettings()` was added
* `privateEndpointConnections()` was added
* `serverCapabilities()` was added
* `privateLinkResources()` was added
* `ltrBackupOperations()` was added
* `virtualEndpoints()` was added
* `quotaUsages()` was added

## 1.0.0 (2023-10-11)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.7 (2023-06-26)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2023-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.NodeTypeCapability` was removed

* `models.StorageMBCapability` was removed

* `models.HyperscaleNodeEditionCapability` was removed

* `models.CapabilityProperties` was removed

* `models.VcoreCapability` was removed

#### `models.StorageTierCapability` was modified

* `isBaseline()` was removed
* `status()` was removed
* `java.lang.Long iops()` -> `java.lang.Integer iops()`
* `tierName()` was removed

#### `models.FastProvisioningEditionCapability` was modified

* `java.lang.Long supportedStorageGb()` -> `java.lang.Integer supportedStorageGb()`

#### `models.ServerVersionCapability` was modified

* `supportedVcores()` was removed
* `status()` was removed

#### `models.FlexibleServerEditionCapability` was modified

* `supportedServerVersions()` was removed
* `status()` was removed

#### `models.StorageEditionCapability` was modified

* `supportedStorageMB()` was removed
* `status()` was removed

#### `models.Server$Definition` was modified

* `withReplicaCapacity(java.lang.Integer)` was removed

### Features Added

* `models.ExecutionStatus` was added

* `models.MigrationResource$Definition` was added

* `models.LogFiles` was added

* `models.MigrationResourceListResult` was added

* `models.LtrBackupResponse` was added

* `models.Migrations` was added

* `models.MigrationMode` was added

* `models.LtrBackupRequest` was added

* `models.FlexibleServersTriggerLtrPreBackupResponse` was added

* `models.ZoneRedundantHaSupportedEnum` was added

* `models.MigrationResourceForPatch` was added

* `models.RestrictedEnum` was added

* `models.OverwriteDbsInTargetEnum` was added

* `models.MigrationState` was added

* `models.CapabilityStatus` was added

* `models.AzureManagedDiskPerformanceTiers` was added

* `models.StorageAutoGrowthSupportedEnum` was added

* `models.MigrationResource$Update` was added

* `models.ResourceProviders` was added

* `models.MigrationStatus` was added

* `models.StartDataMigrationEnum` was added

* `models.ZoneRedundantHaAndGeoBackupSupportedEnum` was added

* `models.StorageMbCapability` was added

* `models.OnlineResizeSupportedEnum` was added

* `models.MigrationNameAvailabilityResource` was added

* `models.ServerSku` was added

* `models.BackupStoreDetails` was added

* `models.MigrationResource$UpdateStages` was added

* `models.FlexibleServers` was added

* `models.LogicalReplicationOnSourceDbEnum` was added

* `models.KeyStatusEnum` was added

* `models.CancelEnum` was added

* `models.MigrationListFilter` was added

* `models.FlexibleServerCapability` was added

* `models.CapabilityBase` was added

* `models.MigrationNameAvailabilityReason` was added

* `models.LtrBackupOperations` was added

* `models.LtrServerBackupOperation` was added

* `models.ServerCapabilities` was added

* `models.FastProvisioningSupportedEnum` was added

* `models.LogFile` was added

* `models.BackupRequestBase` was added

* `models.ServerSkuCapability` was added

* `models.LogFileListResult` was added

* `models.StorageAutoGrow` was added

* `models.BackupSettings` was added

* `models.MigrationSubStateDetails` was added

* `models.LtrServerBackupOperationList` was added

* `models.LtrPreBackupRequest` was added

* `models.LtrPreBackupResponse` was added

* `models.MigrationResource` was added

* `models.HaMode` was added

* `models.GeoBackupSupportedEnum` was added

* `models.MigrationSecretParameters` was added

* `models.TriggerCutoverEnum` was added

* `models.MigrationResource$DefinitionStages` was added

* `models.MigrationSubState` was added

* `models.DbServerMetadata` was added

* `models.FlexibleServersTriggerLtrPreBackupHeaders` was added

* `models.AdminCredentials` was added

#### `models.FastProvisioningEditionCapability` was modified

* `serverCount()` was added
* `supportedTier()` was added

#### `models.Storage` was modified

* `withTier(models.AzureManagedDiskPerformanceTiers)` was added
* `autoGrow()` was added
* `iops()` was added
* `withAutoGrow(models.StorageAutoGrow)` was added
* `tier()` was added

#### `models.UserAssignedIdentity` was modified

* `tenantId()` was added

#### `models.ServerForUpdate` was modified

* `network()` was added
* `withNetwork(models.Network)` was added

#### `models.DataEncryption` was modified

* `withPrimaryEncryptionKeyStatus(models.KeyStatusEnum)` was added
* `withGeoBackupUserAssignedIdentityId(java.lang.String)` was added
* `withGeoBackupKeyUri(java.lang.String)` was added
* `geoBackupUserAssignedIdentityId()` was added
* `withGeoBackupEncryptionKeyStatus(models.KeyStatusEnum)` was added
* `geoBackupEncryptionKeyStatus()` was added
* `geoBackupKeyUri()` was added
* `primaryEncryptionKeyStatus()` was added

#### `models.FlexibleServerEditionCapability` was modified

* `defaultSkuName()` was added
* `supportedServerSkus()` was added

#### `models.Server$Update` was modified

* `withNetwork(models.Network)` was added

#### `models.StorageEditionCapability` was modified

* `defaultStorageSizeMb()` was added
* `supportedStorageMb()` was added

#### `PostgreSqlManager` was modified

* `serverCapabilities()` was added
* `flexibleServers()` was added
* `ltrBackupOperations()` was added
* `logFiles()` was added
* `migrations()` was added
* `resourceProviders()` was added

## 1.0.0-beta.6 (2023-05-19)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.5 (2023-01-11)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.NameAvailabilityRequest` was removed

#### `models.CheckNameAvailabilities` was modified

* `executeWithResponse(models.NameAvailabilityRequest,com.azure.core.util.Context)` was removed
* `execute(models.NameAvailabilityRequest)` was removed

#### `models.Servers` was modified

* `restart(java.lang.String,java.lang.String,models.RestartParameter)` was removed

#### `models.Server` was modified

* `tagsPropertiesTags()` was removed
* `restart(models.RestartParameter)` was removed

#### `models.ServerForUpdate` was modified

* `location()` was removed
* `withLocation(java.lang.String)` was removed

#### `models.Server$Definition` was modified

* `withTagsPropertiesTags(java.util.Map)` was removed

### Features Added

* `models.ReplicationRole` was added

* `models.StorageTierCapability` was added

* `models.ActiveDirectoryAdministrator$DefinitionStages` was added

* `models.DataEncryption` was added

* `models.ServerBackup` was added

* `models.AdministratorListResult` was added

* `models.CheckNameAvailabilityWithLocations` was added

* `models.Backups` was added

* `models.ActiveDirectoryAdministratorAdd` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.Origin` was added

* `models.Administrators` was added

* `models.ConfigurationForUpdate` was added

* `models.ServerBackupListResult` was added

* `models.FastProvisioningEditionCapability` was added

* `models.PrincipalType` was added

* `models.UserIdentity` was added

* `models.ArmServerKeyType` was added

* `models.ActiveDirectoryAdministrator` was added

* `models.UserAssignedIdentity` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.Replicas` was added

* `models.PasswordAuthEnum` was added

* `models.IdentityType` was added

* `models.CheckNameAvailabilityReason` was added

* `models.ActiveDirectoryAuthEnum` was added

* `models.ActiveDirectoryAdministrator$Definition` was added

* `models.AuthConfig` was added

#### `models.ServerVersionCapability` was modified

* `supportedVersionsToUpgrade()` was added

#### `models.CheckNameAvailabilities` was modified

* `execute(models.CheckNameAvailabilityRequest)` was added
* `executeWithResponse(models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was added

#### `models.StorageMBCapability` was modified

* `supportedUpgradableTierList()` was added

#### `models.FirewallRule` was modified

* `resourceGroupName()` was added

#### `models.CapabilityProperties` was modified

* `supportedHAMode()` was added
* `fastProvisioningSupported()` was added
* `supportedFastProvisioningEditions()` was added

#### `PostgreSqlManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Server$Update` was modified

* `withReplicationRole(models.ReplicationRole)` was added
* `withDataEncryption(models.DataEncryption)` was added
* `withIdentity(models.UserAssignedIdentity)` was added
* `withVersion(models.ServerVersion)` was added
* `withAuthConfig(models.AuthConfig)` was added

#### `models.NameAvailability` was modified

* `reason()` was added

#### `models.Configuration` was modified

* `documentationLink()` was added
* `isReadOnly()` was added
* `unit()` was added
* `isConfigPendingRestart()` was added
* `isDynamicConfig()` was added
* `resourceGroupName()` was added

#### `models.Server` was modified

* `authConfig()` was added
* `dataEncryption()` was added
* `resourceGroupName()` was added
* `replicationRole()` was added
* `replicaCapacity()` was added
* `identity()` was added

#### `models.ServerForUpdate` was modified

* `identity()` was added
* `withVersion(models.ServerVersion)` was added
* `authConfig()` was added
* `version()` was added
* `replicationRole()` was added
* `withReplicationRole(models.ReplicationRole)` was added
* `dataEncryption()` was added
* `withAuthConfig(models.AuthConfig)` was added
* `withIdentity(models.UserAssignedIdentity)` was added
* `withDataEncryption(models.DataEncryption)` was added

#### `models.VirtualNetworkSubnetUsageResult` was modified

* `subscriptionId()` was added
* `location()` was added

#### `models.Server$Definition` was modified

* `withIdentity(models.UserAssignedIdentity)` was added
* `withReplicaCapacity(java.lang.Integer)` was added
* `withAuthConfig(models.AuthConfig)` was added
* `withReplicationRole(models.ReplicationRole)` was added
* `withDataEncryption(models.DataEncryption)` was added

#### `PostgreSqlManager` was modified

* `administrators()` was added
* `checkNameAvailabilityWithLocations()` was added
* `replicas()` was added
* `backups()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.4 (2021-10-09)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.3 (2021-08-30)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.RestartParameter` was modified

* `withFailoverMode(java.lang.String)` was removed
* `java.lang.String failoverMode()` -> `models.FailoverMode failoverMode()`

### Features Added

* `models.FailoverMode` was added

#### `models.RestartParameter` was modified

* `withFailoverMode(models.FailoverMode)` was added

## 1.0.0-beta.2 (2021-07-26)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Identity` was removed

* `models.ResourceIdentityType` was removed

#### `models.Server` was modified

* `identity()` was removed

#### `models.Server$Definition` was modified

* `withIdentity(models.Identity)` was removed

## 1.0.0-beta.1 (2021-07-12)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
