# Release History

## 1.0.0 (2023-10-11)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ExecutionStatus` was removed

* `models.MigrationResource$Definition` was removed

* `models.LogFiles` was removed

* `models.MigrationResourceListResult` was removed

* `models.LtrBackupResponse` was removed

* `models.Migrations` was removed

* `models.MigrationMode` was removed

* `models.LtrBackupRequest` was removed

* `models.FlexibleServersTriggerLtrPreBackupResponse` was removed

* `models.ZoneRedundantHaSupportedEnum` was removed

* `models.MigrationResourceForPatch` was removed

* `models.RestrictedEnum` was removed

* `models.OverwriteDbsInTargetEnum` was removed

* `models.MigrationState` was removed

* `models.CapabilityStatus` was removed

* `models.AzureManagedDiskPerformanceTiers` was removed

* `models.StorageAutoGrowthSupportedEnum` was removed

* `models.MigrationResource$Update` was removed

* `models.ResourceProviders` was removed

* `models.MigrationStatus` was removed

* `models.StartDataMigrationEnum` was removed

* `models.ZoneRedundantHaAndGeoBackupSupportedEnum` was removed

* `models.StorageMbCapability` was removed

* `models.OnlineResizeSupportedEnum` was removed

* `models.MigrationNameAvailabilityResource` was removed

* `models.ServerSku` was removed

* `models.BackupStoreDetails` was removed

* `models.MigrationResource$UpdateStages` was removed

* `models.FlexibleServers` was removed

* `models.LogicalReplicationOnSourceDbEnum` was removed

* `models.KeyStatusEnum` was removed

* `models.CancelEnum` was removed

* `models.MigrationListFilter` was removed

* `models.FlexibleServerCapability` was removed

* `models.CapabilityBase` was removed

* `models.MigrationNameAvailabilityReason` was removed

* `models.LtrBackupOperations` was removed

* `models.LtrServerBackupOperation` was removed

* `models.ServerCapabilities` was removed

* `models.FastProvisioningSupportedEnum` was removed

* `models.LogFile` was removed

* `models.BackupRequestBase` was removed

* `models.ServerSkuCapability` was removed

* `models.LogFileListResult` was removed

* `models.StorageAutoGrow` was removed

* `models.BackupSettings` was removed

* `models.MigrationSubStateDetails` was removed

* `models.LtrServerBackupOperationList` was removed

* `models.LtrPreBackupRequest` was removed

* `models.LtrPreBackupResponse` was removed

* `models.MigrationResource` was removed

* `models.HaMode` was removed

* `models.GeoBackupSupportedEnum` was removed

* `models.MigrationSecretParameters` was removed

* `models.TriggerCutoverEnum` was removed

* `models.MigrationResource$DefinitionStages` was removed

* `models.MigrationSubState` was removed

* `models.DbServerMetadata` was removed

* `models.FlexibleServersTriggerLtrPreBackupHeaders` was removed

* `models.AdminCredentials` was removed

#### `models.StorageTierCapability` was modified

* `java.lang.Integer iops()` -> `java.lang.Long iops()`

#### `models.FastProvisioningEditionCapability` was modified

* `serverCount()` was removed
* `supportedTier()` was removed
* `java.lang.Integer supportedStorageGb()` -> `java.lang.Long supportedStorageGb()`

#### `models.Storage` was modified

* `iops()` was removed
* `autoGrow()` was removed
* `withAutoGrow(models.StorageAutoGrow)` was removed
* `tier()` was removed
* `withTier(models.AzureManagedDiskPerformanceTiers)` was removed

#### `models.DataEncryption` was modified

* `withPrimaryEncryptionKeyStatus(models.KeyStatusEnum)` was removed
* `geoBackupUserAssignedIdentityId()` was removed
* `geoBackupKeyUri()` was removed
* `primaryEncryptionKeyStatus()` was removed
* `withGeoBackupUserAssignedIdentityId(java.lang.String)` was removed
* `withGeoBackupKeyUri(java.lang.String)` was removed
* `geoBackupEncryptionKeyStatus()` was removed
* `withGeoBackupEncryptionKeyStatus(models.KeyStatusEnum)` was removed

#### `models.FlexibleServerEditionCapability` was modified

* `supportedServerSkus()` was removed
* `defaultSkuName()` was removed

#### `models.StorageEditionCapability` was modified

* `defaultStorageSizeMb()` was removed
* `supportedStorageMb()` was removed

#### `PostgreSqlManager` was modified

* `ltrBackupOperations()` was removed
* `migrations()` was removed
* `logFiles()` was removed
* `serverCapabilities()` was removed
* `resourceProviders()` was removed
* `flexibleServers()` was removed

### Features Added

* `models.NodeTypeCapability` was added

* `models.StorageMBCapability` was added

* `models.HyperscaleNodeEditionCapability` was added

* `models.CapabilityProperties` was added

* `models.VcoreCapability` was added

#### `models.StorageTierCapability` was modified

* `status()` was added
* `tierName()` was added
* `isBaseline()` was added

#### `models.ServerVersionCapability` was modified

* `status()` was added
* `supportedVcores()` was added

#### `models.FlexibleServerEditionCapability` was modified

* `supportedServerVersions()` was added
* `status()` was added

#### `models.StorageEditionCapability` was modified

* `status()` was added
* `supportedStorageMB()` was added

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
