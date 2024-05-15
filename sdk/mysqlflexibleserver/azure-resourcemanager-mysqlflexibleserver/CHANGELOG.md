# Release History

## 1.0.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.5 (2024-04-29)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-flexibleserver-2023-12-30. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Identity` was removed

* `models.Sku` was removed

* `models.SkuTier` was removed

#### `models.Server` was modified

* `models.Identity identity()` -> `models.MySqlServerIdentity identity()`
* `models.Sku sku()` -> `models.MySqlServerSku sku()`

#### `models.ServerForUpdate` was modified

* `withIdentity(models.Identity)` was removed
* `models.Sku sku()` -> `models.MySqlServerSku sku()`
* `models.Identity identity()` -> `models.MySqlServerIdentity identity()`
* `withSku(models.Sku)` was removed

#### `models.Server$Definition` was modified

* `withIdentity(models.Identity)` was removed
* `withSku(models.Sku)` was removed

#### `models.Server$Update` was modified

* `withIdentity(models.Identity)` was removed
* `withSku(models.Sku)` was removed

### Features Added

* `models.BackupAndExportResponseType` was added

* `models.ServerEditionCapabilityV2` was added

* `models.AdvancedThreatProtectionState` was added

* `models.SkuCapabilityV2` was added

* `models.ServerSkuTier` was added

* `models.AdvancedThreatProtectionProvisioningState` was added

* `models.ImportFromStorageResponseType` was added

* `models.ServerBackupV2ListResult` was added

* `models.HighAvailabilityValidationEstimation` was added

* `models.OperationProgressResponseType` was added

* `models.PrivateEndpoint` was added

* `models.OperationResults` was added

* `models.ServerBackupV2$DefinitionStages` was added

* `models.MySqlServerSku` was added

* `models.ServerBackupV2$Definition` was added

* `models.OperationStatusResult` was added

* `models.MaintenanceUpdate` was added

* `models.OperationStatusExtendedResult` was added

* `models.ServersMigrations` was added

* `models.AdvancedThreatProtectionSettings` was added

* `models.LocationBasedCapabilitySets` was added

* `models.ProvisioningState` was added

* `models.ImportSourceStorageType` was added

* `models.MaintenanceProvisioningState` was added

* `models.CapabilitySetsList` was added

* `models.ServerVersionCapabilityV2` was added

* `models.AdvancedThreatProtectionName` was added

* `models.MySqlServerIdentity` was added

* `models.ImportSourceProperties` was added

* `models.MaintenanceListResult` was added

* `models.OperationProgressResult` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.AdvancedThreatProtectionListResult` was added

* `models.ServerBackupV2` was added

* `models.LongRunningBackups` was added

* `models.ObjectType` was added

* `models.Maintenances` was added

* `models.MaintenanceType` was added

* `models.AdvancedThreatProtection` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.AdvancedThreatProtectionForUpdate` was added

* `models.MaintenanceState` was added

* `models.Capability` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.Maintenance` was added

* `models.BackupType` was added

* `models.LongRunningBackupsOperations` was added

* `models.OperationProgress` was added

#### `models.FullBackupStoreDetails` was modified

* `objectType()` was added

#### `models.BackupStoreDetails` was modified

* `objectType()` was added

#### `models.Server` was modified

* `importSourceProperties()` was added
* `validateEstimateHighAvailability(fluent.models.HighAvailabilityValidationEstimationInner)` was added
* `privateEndpointConnections()` was added
* `validateEstimateHighAvailabilityWithResponse(fluent.models.HighAvailabilityValidationEstimationInner,com.azure.core.util.Context)` was added

#### `models.BackupAndExportResponse` was modified

* `systemData()` was added

#### `models.ServerForUpdate` was modified

* `withIdentity(models.MySqlServerIdentity)` was added
* `withSku(models.MySqlServerSku)` was added

#### `models.StorageEditionCapability` was modified

* `minBackupIntervalHours()` was added
* `maxBackupIntervalHours()` was added

#### `MySqlManager` was modified

* `serversMigrations()` was added
* `locationBasedCapabilitySets()` was added
* `operationProgress()` was added
* `longRunningBackups()` was added
* `maintenances()` was added
* `operationResults()` was added
* `advancedThreatProtectionSettings()` was added
* `longRunningBackupsOperations()` was added

#### `models.Server$Definition` was modified

* `withSku(models.MySqlServerSku)` was added
* `withIdentity(models.MySqlServerIdentity)` was added
* `withImportSourceProperties(models.ImportSourceProperties)` was added

#### `models.Servers` was modified

* `validateEstimateHighAvailabilityWithResponse(java.lang.String,java.lang.String,fluent.models.HighAvailabilityValidationEstimationInner,com.azure.core.util.Context)` was added
* `validateEstimateHighAvailability(java.lang.String,java.lang.String,fluent.models.HighAvailabilityValidationEstimationInner)` was added

#### `models.Server$Update` was modified

* `withIdentity(models.MySqlServerIdentity)` was added
* `withSku(models.MySqlServerSku)` was added

#### `models.Backup` was modified

* `backupIntervalHours()` was added
* `withBackupIntervalHours(java.lang.Integer)` was added

## 1.0.0-beta.4 (2023-05-18)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-flexibleserver-2022-09-30-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Configurations` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.ConfigurationInner,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.ConfigurationInner)` was removed

#### `models.ManagedServiceIdentityType` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.ManagedServiceIdentityType[] values()` -> `java.util.Collection values()`

### Features Added

* `models.Configuration$Update` was added

* `models.BackupRequestBase` was added

* `models.BackupAndExports` was added

* `models.FullBackupStoreDetails` was added

* `models.ServerGtidSetParameter` was added

* `models.BackupAndExportRequest` was added

* `models.OperationStatus` was added

* `models.ValidateBackupResponse` was added

* `models.Configuration$UpdateStages` was added

* `models.Configuration$DefinitionStages` was added

* `models.Configuration$Definition` was added

* `models.BackupSettings` was added

* `models.BackupStoreDetails` was added

* `models.BackupAndExportResponse` was added

* `models.BackupFormat` was added

#### `models.Configurations` was modified

* `getById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added

#### `models.Servers` was modified

* `resetGtid(java.lang.String,java.lang.String,models.ServerGtidSetParameter)` was added
* `resetGtid(java.lang.String,java.lang.String,models.ServerGtidSetParameter,com.azure.core.util.Context)` was added

#### `models.Server` was modified

* `resetGtid(models.ServerGtidSetParameter)` was added
* `resetGtid(models.ServerGtidSetParameter,com.azure.core.util.Context)` was added

#### `models.Server$Update` was modified

* `withNetwork(models.Network)` was added

#### `models.ServerForUpdate` was modified

* `withNetwork(models.Network)` was added
* `network()` was added

#### `models.Network` was modified

* `withPublicNetworkAccess(models.EnableStatusEnum)` was added

#### `MySqlManager` was modified

* `backupAndExports()` was added

#### `models.Configuration` was modified

* `refresh(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `documentationLink()` was added
* `update()` was added
* `refresh()` was added
* `currentValue()` was added

#### `models.Storage` was modified

* `withLogOnDisk(models.EnableStatusEnum)` was added
* `logOnDisk()` was added

## 1.0.0-beta.3 (2023-02-21)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-flexibleserver-2021-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Configurations` was modified

* `listByServer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.LogFileListResult` was added

* `models.CheckNameAvailabilityWithoutLocations` was added

* `models.AzureADAdministrator` was added

* `models.AdministratorType` was added

* `models.AzureADAdministrator$UpdateStages` was added

* `models.ResetAllToDefault` was added

* `models.LogFiles` was added

* `models.AdministratorListResult` was added

* `models.AzureADAdministrators` was added

* `models.AzureADAdministrator$Definition` was added

* `models.AzureADAdministrator$DefinitionStages` was added

* `models.AdministratorName` was added

* `models.AzureADAdministrator$Update` was added

* `models.LogFile` was added

#### `models.FirewallRule` was modified

* `resourceGroupName()` was added

#### `models.Backups` was modified

* `putWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `put(java.lang.String,java.lang.String,java.lang.String)` was added

#### `MySqlManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Configurations` was modified

* `listByServer(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.ConfigurationListForBatchUpdate` was modified

* `withResetAllToDefault(models.ResetAllToDefault)` was added
* `resetAllToDefault()` was added

#### `models.Server` was modified

* `resourceGroupName()` was added

#### `models.Database` was modified

* `resourceGroupName()` was added

#### `models.VirtualNetworkSubnetUsageResult` was modified

* `location()` was added
* `subscriptionId()` was added

#### `models.Server$Update` was modified

* `withVersion(models.ServerVersion)` was added

#### `models.ServerForUpdate` was modified

* `version()` was added
* `withVersion(models.ServerVersion)` was added

#### `MySqlManager` was modified

* `checkNameAvailabilityWithoutLocations()` was added
* `logFiles()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `azureADAdministrators()` was added

#### `models.Storage` was modified

* `withAutoIoScaling(models.EnableStatusEnum)` was added
* `autoIoScaling()` was added

## 1.0.0-beta.2 (2022-03-09)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-flexibleserver-2021-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ManagedServiceIdentityType` was added

* `models.DataEncryption` was added

* `models.Identity` was added

* `models.DataEncryptionType` was added

#### `models.Server$Definition` was modified

* `withIdentity(models.Identity)` was added
* `withDataEncryption(models.DataEncryption)` was added

#### `models.Server` was modified

* `identity()` was added
* `dataEncryption()` was added

#### `models.Server$Update` was modified

* `withIdentity(models.Identity)` was added
* `withDataEncryption(models.DataEncryption)` was added

#### `models.ServerForUpdate` was modified

* `withDataEncryption(models.DataEncryption)` was added
* `dataEncryption()` was added
* `identity()` was added
* `withIdentity(models.Identity)` was added

## 1.0.0-beta.1 (2021-09-13)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-flexibleserver-2021-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

