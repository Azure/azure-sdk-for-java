# Release History

## 1.0.0 (2024-12-20)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-flexibleserver-2021-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Configuration$Update` was removed

#### `models.BackupAndExportResponseType` was removed

#### `models.ServerEditionCapabilityV2` was removed

#### `models.AdvancedThreatProtectionState` was removed

#### `models.SkuCapabilityV2` was removed

#### `models.BackupRequestBase` was removed

#### `models.ServerSkuTier` was removed

#### `models.LogFileListResult` was removed

#### `models.AdvancedThreatProtectionProvisioningState` was removed

#### `models.FullBackupStoreDetails` was removed

#### `models.ImportFromStorageResponseType` was removed

#### `models.ServerBackupV2ListResult` was removed

#### `models.AzureADAdministrator` was removed

#### `models.HighAvailabilityValidationEstimation` was removed

#### `models.ValidateBackupResponse` was removed

#### `models.AdministratorType` was removed

#### `models.OperationProgressResponseType` was removed

#### `models.Configuration$Definition` was removed

#### `models.PrivateEndpoint` was removed

#### `models.BackupStoreDetails` was removed

#### `models.AzureADAdministrator$UpdateStages` was removed

#### `models.ResetAllToDefault` was removed

#### `models.LogFiles` was removed

#### `models.AdministratorListResult` was removed

#### `models.OperationResults` was removed

#### `models.ServerBackupV2$DefinitionStages` was removed

#### `models.BackupAndExportResponse` was removed

#### `models.MySqlServerSku` was removed

#### `models.ServerBackupV2$Definition` was removed

#### `models.OperationStatusResult` was removed

#### `models.MaintenanceUpdate` was removed

#### `models.OperationStatusExtendedResult` was removed

#### `models.ServersMigrations` was removed

#### `models.AzureADAdministrator$Update` was removed

#### `models.AdvancedThreatProtectionSettings` was removed

#### `models.LocationBasedCapabilitySets` was removed

#### `models.ProvisioningState` was removed

#### `models.BackupAndExports` was removed

#### `models.ImportSourceStorageType` was removed

#### `models.ServerGtidSetParameter` was removed

#### `models.BackupAndExportRequest` was removed

#### `models.MaintenanceProvisioningState` was removed

#### `models.CapabilitySetsList` was removed

#### `models.ServerVersionCapabilityV2` was removed

#### `models.OperationStatus` was removed

#### `models.Configuration$UpdateStages` was removed

#### `models.Configuration$DefinitionStages` was removed

#### `models.AdvancedThreatProtectionName` was removed

#### `models.MySqlServerIdentity` was removed

#### `models.ImportSourceProperties` was removed

#### `models.BackupSettings` was removed

#### `models.MaintenanceListResult` was removed

#### `models.OperationProgressResult` was removed

#### `models.PrivateEndpointConnection` was removed

#### `models.PrivateLinkServiceConnectionState` was removed

#### `models.AdvancedThreatProtectionListResult` was removed

#### `models.ServerBackupV2` was removed

#### `models.LongRunningBackups` was removed

#### `models.ObjectType` was removed

#### `models.Maintenances` was removed

#### `models.MaintenanceType` was removed

#### `models.BackupFormat` was removed

#### `models.AzureADAdministrators` was removed

#### `models.AzureADAdministrator$Definition` was removed

#### `models.AdvancedThreatProtection` was removed

#### `models.PrivateEndpointServiceConnectionStatus` was removed

#### `models.AzureADAdministrator$DefinitionStages` was removed

#### `models.AdvancedThreatProtectionForUpdate` was removed

#### `models.MaintenanceState` was removed

#### `models.AdministratorName` was removed

#### `models.Capability` was removed

#### `models.PrivateEndpointConnectionProvisioningState` was removed

#### `models.Maintenance` was removed

#### `models.BackupType` was removed

#### `models.LogFile` was removed

#### `models.LongRunningBackupsOperations` was removed

#### `models.OperationProgress` was removed

#### `models.ConfigurationListForBatchUpdate` was modified

* `withResetAllToDefault(models.ResetAllToDefault)` was removed
* `resetAllToDefault()` was removed

#### `models.ManagedServiceIdentityType` was modified

* `java.util.Collection values()` -> `models.ManagedServiceIdentityType[] values()`

#### `models.Server` was modified

* `models.MySqlServerSku sku()` -> `models.Sku sku()`
* `importSourceProperties()` was removed
* `resetGtid(models.ServerGtidSetParameter,com.azure.core.util.Context)` was removed
* `privateEndpointConnections()` was removed
* `models.MySqlServerIdentity identity()` -> `models.Identity identity()`
* `resetGtid(models.ServerGtidSetParameter)` was removed
* `validateEstimateHighAvailabilityWithResponse(fluent.models.HighAvailabilityValidationEstimationInner,com.azure.core.util.Context)` was removed
* `validateEstimateHighAvailability(fluent.models.HighAvailabilityValidationEstimationInner)` was removed

#### `models.ServerForUpdate` was modified

* `models.MySqlServerIdentity identity()` -> `models.Identity identity()`
* `models.MySqlServerSku sku()` -> `models.Sku sku()`
* `withSku(models.MySqlServerSku)` was removed
* `version()` was removed
* `withVersion(models.ServerVersion)` was removed
* `withIdentity(models.MySqlServerIdentity)` was removed
* `network()` was removed
* `withNetwork(models.Network)` was removed

#### `models.Network` was modified

* `withPublicNetworkAccess(models.EnableStatusEnum)` was removed

#### `models.StorageEditionCapability` was modified

* `minBackupIntervalHours()` was removed
* `maxBackupIntervalHours()` was removed

#### `MySqlManager` was modified

* `advancedThreatProtectionSettings()` was removed
* `operationProgress()` was removed
* `operationResults()` was removed
* `locationBasedCapabilitySets()` was removed
* `maintenances()` was removed
* `logFiles()` was removed
* `longRunningBackupsOperations()` was removed
* `longRunningBackups()` was removed
* `serversMigrations()` was removed
* `azureADAdministrators()` was removed
* `backupAndExports()` was removed

#### `models.Storage` was modified

* `autoIoScaling()` was removed
* `withLogOnDisk(models.EnableStatusEnum)` was removed
* `logOnDisk()` was removed
* `withAutoIoScaling(models.EnableStatusEnum)` was removed

#### `models.Backups` was modified

* `put(java.lang.String,java.lang.String,java.lang.String)` was removed
* `putWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Server$Definition` was modified

* `withIdentity(models.MySqlServerIdentity)` was removed
* `withImportSourceProperties(models.ImportSourceProperties)` was removed
* `withSku(models.MySqlServerSku)` was removed

#### `models.Configurations` was modified

* `getById(java.lang.String)` was removed
* `define(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `listByServer(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Integer,com.azure.core.util.Context)` was removed

#### `models.Servers` was modified

* `resetGtid(java.lang.String,java.lang.String,models.ServerGtidSetParameter,com.azure.core.util.Context)` was removed
* `validateEstimateHighAvailability(java.lang.String,java.lang.String,fluent.models.HighAvailabilityValidationEstimationInner)` was removed
* `resetGtid(java.lang.String,java.lang.String,models.ServerGtidSetParameter)` was removed
* `validateEstimateHighAvailabilityWithResponse(java.lang.String,java.lang.String,fluent.models.HighAvailabilityValidationEstimationInner,com.azure.core.util.Context)` was removed

#### `models.Server$Update` was modified

* `withIdentity(models.MySqlServerIdentity)` was removed
* `withNetwork(models.Network)` was removed
* `withVersion(models.ServerVersion)` was removed
* `withSku(models.MySqlServerSku)` was removed

#### `models.Backup` was modified

* `backupIntervalHours()` was removed
* `withBackupIntervalHours(java.lang.Integer)` was removed

#### `models.Configuration` was modified

* `documentationLink()` was removed
* `refresh()` was removed
* `refresh(com.azure.core.util.Context)` was removed
* `currentValue()` was removed
* `resourceGroupName()` was removed
* `update()` was removed

### Features Added

* `models.Identity` was added

* `models.Sku` was added

* `models.SkuTier` was added

#### `models.ManagedServiceIdentityType` was modified

* `valueOf(java.lang.String)` was added
* `toString()` was added

#### `models.ServerForUpdate` was modified

* `withIdentity(models.Identity)` was added
* `withSku(models.Sku)` was added

#### `models.Server$Definition` was modified

* `withIdentity(models.Identity)` was added
* `withSku(models.Sku)` was added

#### `models.Configurations` was modified

* `listByServer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.ConfigurationInner)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,fluent.models.ConfigurationInner,com.azure.core.util.Context)` was added

#### `models.Server$Update` was modified

* `withIdentity(models.Identity)` was added
* `withSku(models.Sku)` was added

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

