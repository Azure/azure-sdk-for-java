# Release History

## 1.1.0 (2025-10-13)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package api-version 2024-12-30. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CapabilitiesListResult` was removed

#### `models.FirewallRuleListResult` was removed

#### `models.OperationListResult` was removed

#### `models.DatabaseListResult` was removed

#### `models.ServerBackupListResult` was removed

#### `models.ServerListResult` was removed

#### `models.OperationDisplay` was modified

* `withProvider(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withResource(java.lang.String)` was removed

#### `models.VirtualNetworkSubnetUsageParameter` was modified

* `validate()` was removed

#### `models.SkuCapability` was modified

* `validate()` was removed

#### `models.ConfigurationListForBatchUpdate` was modified

* `validate()` was removed

#### `models.NameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentityType` was modified

* `valueOf(java.lang.String)` was removed
* `models.ManagedServiceIdentityType[] values()` -> `java.util.Collection values()`
* `toString()` was removed

#### `models.ServerRestartParameter` was modified

* `validate()` was removed

#### `models.Identity` was modified

* `validate()` was removed

#### `models.HighAvailability` was modified

* `validate()` was removed

#### `models.ServerVersionCapability` was modified

* `validate()` was removed

#### `models.ServerForUpdate` was modified

* `validate()` was removed

#### `models.MaintenanceWindow` was modified

* `validate()` was removed

#### `models.Network` was modified

* `validate()` was removed

#### `models.StorageEditionCapability` was modified

* `validate()` was removed

#### `models.Storage` was modified

* `validate()` was removed

#### `models.Configurations` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.ConfigurationInner)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.ConfigurationInner,com.azure.core.util.Context)` was removed
* `listByServer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.DelegatedSubnetUsage` was modified

* `validate()` was removed

#### `models.ServerEditionCapability` was modified

* `validate()` was removed

#### `models.Operation` was modified

* `java.lang.String origin()` -> `models.Origin origin()`

#### `models.DataEncryption` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.ConfigurationForBatchUpdate` was modified

* `validate()` was removed

#### `models.Backup` was modified

* `validate()` was removed

### Features Added

* `models.Configuration$Update` was added

* `models.BackupAndExportResponseType` was added

* `models.ServerEditionCapabilityV2` was added

* `models.AdvancedThreatProtectionState` was added

* `models.SkuCapabilityV2` was added

* `models.BackupRequestBase` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.AdvancedThreatProtectionProvisioningState` was added

* `models.FullBackupStoreDetails` was added

* `models.ImportFromStorageResponseType` was added

* `models.AzureADAdministrator` was added

* `models.HighAvailabilityValidationEstimation` was added

* `models.ValidateBackupResponse` was added

* `models.AdministratorType` was added

* `models.OperationProgressResponseType` was added

* `models.ServerDetachVNetParameter` was added

* `models.Configuration$Definition` was added

* `models.PrivateEndpoint` was added

* `models.BackupStoreDetails` was added

* `models.AzureADAdministrator$UpdateStages` was added

* `models.ResetAllToDefault` was added

* `models.LogFiles` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.OperationResults` was added

* `models.ServerBackupV2$DefinitionStages` was added

* `models.BackupAndExportResponse` was added

* `models.ServerBackupV2$Definition` was added

* `models.BatchOfMaintenance` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.OperationStatusResult` was added

* `models.MaintenanceUpdate` was added

* `models.OperationStatusExtendedResult` was added

* `models.PrivateLinkResources` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.ServersMigrations` was added

* `models.AzureADAdministrator$Update` was added

* `models.AdvancedThreatProtectionSettings` was added

* `models.UserAssignedIdentity` was added

* `models.LocationBasedCapabilitySets` was added

* `models.ProvisioningState` was added

* `models.MaintenancePolicy` was added

* `models.BackupAndExports` was added

* `models.PrivateEndpointConnections` was added

* `models.ImportSourceStorageType` was added

* `models.ServerGtidSetParameter` was added

* `models.FeatureProperty` was added

* `models.PrivateLinkResource` was added

* `models.BackupAndExportRequest` was added

* `models.MaintenanceProvisioningState` was added

* `models.ServerVersionCapabilityV2` was added

* `models.OperationStatus` was added

* `models.Configuration$UpdateStages` was added

* `models.Configuration$DefinitionStages` was added

* `models.PrivateLinkResourceProperties` was added

* `models.AdvancedThreatProtectionName` was added

* `models.PatchStrategy` was added

* `models.ImportSourceProperties` was added

* `models.BackupSettings` was added

* `models.StorageRedundancyEnum` was added

* `models.OperationProgressResult` was added

* `models.PrivateEndpointConnection` was added

* `models.Origin` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.ServerBackupV2` was added

* `models.LongRunningBackupOperations` was added

* `models.LongRunningBackups` was added

* `models.ObjectType` was added

* `models.Maintenances` was added

* `models.MaintenanceType` was added

* `models.BackupFormat` was added

* `models.AzureADAdministrators` was added

* `models.AzureADAdministrator$Definition` was added

* `models.AdvancedThreatProtection` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.AzureADAdministrator$DefinitionStages` was added

* `models.AdvancedThreatProtectionForUpdate` was added

* `models.MaintenanceState` was added

* `models.AdministratorName` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.Capability` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.Maintenance` was added

* `models.BackupType` was added

* `models.LogFile` was added

* `models.OperationProgress` was added

#### `models.ConfigurationListForBatchUpdate` was modified

* `resetAllToDefault()` was added
* `withResetAllToDefault(models.ResetAllToDefault)` was added

#### `models.Server` was modified

* `fullVersion()` was added
* `validateEstimateHighAvailabilityWithResponse(fluent.models.HighAvailabilityValidationEstimationInner,com.azure.core.util.Context)` was added
* `maintenancePolicy()` was added
* `resetGtid(models.ServerGtidSetParameter)` was added
* `detachVNet(models.ServerDetachVNetParameter,com.azure.core.util.Context)` was added
* `privateEndpointConnections()` was added
* `databasePort()` was added
* `importSourceProperties()` was added
* `resetGtid(models.ServerGtidSetParameter,com.azure.core.util.Context)` was added
* `validateEstimateHighAvailability(fluent.models.HighAvailabilityValidationEstimationInner)` was added
* `detachVNet(models.ServerDetachVNetParameter)` was added

#### `models.ServerForUpdate` was modified

* `withNetwork(models.Network)` was added
* `withVersion(models.ServerVersion)` was added
* `withMaintenancePolicy(models.MaintenancePolicy)` was added
* `maintenancePolicy()` was added
* `version()` was added
* `network()` was added

#### `models.MaintenanceWindow` was modified

* `batchOfMaintenance()` was added
* `withBatchOfMaintenance(models.BatchOfMaintenance)` was added

#### `models.Network` was modified

* `withPublicNetworkAccess(models.EnableStatusEnum)` was added

#### `models.StorageEditionCapability` was modified

* `maxBackupIntervalHours()` was added
* `minBackupIntervalHours()` was added

#### `MySqlManager` was modified

* `operationProgress()` was added
* `backupAndExports()` was added
* `azureADAdministrators()` was added
* `serversMigrations()` was added
* `longRunningBackups()` was added
* `maintenances()` was added
* `privateLinkResources()` was added
* `operationResults()` was added
* `logFiles()` was added
* `longRunningBackupOperations()` was added
* `advancedThreatProtectionSettings()` was added
* `locationBasedCapabilitySets()` was added
* `privateEndpointConnections()` was added

#### `models.Storage` was modified

* `storageRedundancy()` was added
* `withAutoIoScaling(models.EnableStatusEnum)` was added
* `withLogOnDisk(models.EnableStatusEnum)` was added
* `withStorageRedundancy(models.StorageRedundancyEnum)` was added
* `autoIoScaling()` was added
* `logOnDisk()` was added

#### `models.Backups` was modified

* `put(java.lang.String,java.lang.String,java.lang.String)` was added
* `putWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Server$Definition` was modified

* `withDatabasePort(java.lang.Integer)` was added
* `withMaintenancePolicy(models.MaintenancePolicy)` was added
* `withImportSourceProperties(models.ImportSourceProperties)` was added

#### `models.Configurations` was modified

* `listByServer(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Integer,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.Servers` was modified

* `resetGtid(java.lang.String,java.lang.String,models.ServerGtidSetParameter)` was added
* `detachVNet(java.lang.String,java.lang.String,models.ServerDetachVNetParameter)` was added
* `detachVNet(java.lang.String,java.lang.String,models.ServerDetachVNetParameter,com.azure.core.util.Context)` was added
* `resetGtid(java.lang.String,java.lang.String,models.ServerGtidSetParameter,com.azure.core.util.Context)` was added
* `validateEstimateHighAvailability(java.lang.String,java.lang.String,fluent.models.HighAvailabilityValidationEstimationInner)` was added
* `validateEstimateHighAvailabilityWithResponse(java.lang.String,java.lang.String,fluent.models.HighAvailabilityValidationEstimationInner,com.azure.core.util.Context)` was added

#### `models.Server$Update` was modified

* `withNetwork(models.Network)` was added
* `withVersion(models.ServerVersion)` was added
* `withMaintenancePolicy(models.MaintenancePolicy)` was added

#### `models.Backup` was modified

* `backupIntervalHours()` was added
* `withBackupIntervalHours(java.lang.Integer)` was added

#### `models.Configuration` was modified

* `update()` was added
* `currentValue()` was added
* `refresh()` was added
* `resourceGroupName()` was added
* `refresh(com.azure.core.util.Context)` was added
* `documentationLink()` was added

## 1.0.0 (2024-12-20)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-flexibleserver-2021-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

Release for the azure-resourcemanager-mysqlflexibleserver Java SDK.

## 1.0.0-beta.6 (2024-12-04)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-flexibleserver-2023-12-30. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.PrivateEndpointConnection` was modified

* `type()` was added
* `id()` was added
* `name()` was added

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

