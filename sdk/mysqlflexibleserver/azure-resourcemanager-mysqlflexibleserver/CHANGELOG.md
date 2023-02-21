# Release History

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

