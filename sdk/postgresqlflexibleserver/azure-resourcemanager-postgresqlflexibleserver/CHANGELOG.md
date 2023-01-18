# Release History

## 1.0.0-beta.6 (2023-01-18)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-flexibleserver-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ReplicationRole` was removed

* `models.StorageTierCapability` was removed

* `models.ActiveDirectoryAdministrator$DefinitionStages` was removed

* `models.DataEncryption` was removed

* `models.ServerBackup` was removed

* `models.AdministratorListResult` was removed

* `models.CheckNameAvailabilityWithLocations` was removed

* `models.Backups` was removed

* `models.ActiveDirectoryAdministratorAdd` was removed

* `models.CheckNameAvailabilityResponse` was removed

* `models.Origin` was removed

* `models.Administrators` was removed

* `models.ConfigurationForUpdate` was removed

* `models.ServerBackupListResult` was removed

* `models.FastProvisioningEditionCapability` was removed

* `models.PrincipalType` was removed

* `models.UserIdentity` was removed

* `models.ArmServerKeyType` was removed

* `models.ActiveDirectoryAdministrator` was removed

* `models.UserAssignedIdentity` was removed

* `models.CheckNameAvailabilityRequest` was removed

* `models.Replicas` was removed

* `models.PasswordAuthEnum` was removed

* `models.IdentityType` was removed

* `models.CheckNameAvailabilityReason` was removed

* `models.ActiveDirectoryAuthEnum` was removed

* `models.ActiveDirectoryAdministrator$Definition` was removed

* `models.AuthConfig` was removed

#### `models.ServerVersionCapability` was modified

* `supportedVersionsToUpgrade()` was removed

#### `models.CheckNameAvailabilities` was modified

* `execute(models.CheckNameAvailabilityRequest)` was removed
* `executeWithResponse(models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was removed

#### `models.StorageMBCapability` was modified

* `supportedUpgradableTierList()` was removed

#### `models.CapabilityProperties` was modified

* `supportedFastProvisioningEditions()` was removed
* `fastProvisioningSupported()` was removed

#### `models.Server$Update` was modified

* `withDataEncryption(models.DataEncryption)` was removed
* `withAuthConfig(models.AuthConfig)` was removed
* `withVersion(models.ServerVersion)` was removed
* `withReplicationRole(models.ReplicationRole)` was removed
* `withIdentity(models.UserAssignedIdentity)` was removed

#### `models.NameAvailability` was modified

* `models.CheckNameAvailabilityReason reason()` -> `models.Reason reason()`

#### `models.Server` was modified

* `identity()` was removed
* `replicaCapacity()` was removed
* `authConfig()` was removed
* `replicationRole()` was removed
* `dataEncryption()` was removed

#### `models.ServerForUpdate` was modified

* `identity()` was removed
* `withAuthConfig(models.AuthConfig)` was removed
* `authConfig()` was removed
* `version()` was removed
* `replicationRole()` was removed
* `withReplicationRole(models.ReplicationRole)` was removed
* `withIdentity(models.UserAssignedIdentity)` was removed
* `withVersion(models.ServerVersion)` was removed
* `withDataEncryption(models.DataEncryption)` was removed
* `dataEncryption()` was removed

#### `models.Server$Definition` was modified

* `withAuthConfig(models.AuthConfig)` was removed
* `withReplicaCapacity(java.lang.Integer)` was removed
* `withDataEncryption(models.DataEncryption)` was removed
* `withIdentity(models.UserAssignedIdentity)` was removed
* `withReplicationRole(models.ReplicationRole)` was removed

#### `PostgreSqlManager` was modified

* `administrators()` was removed
* `checkNameAvailabilityWithLocations()` was removed
* `backups()` was removed
* `replicas()` was removed

### Features Added

* `models.Reason` was added

* `models.NameAvailabilityRequest` was added

#### `models.CheckNameAvailabilities` was modified

* `execute(models.NameAvailabilityRequest)` was added
* `executeWithResponse(models.NameAvailabilityRequest,com.azure.core.util.Context)` was added

#### `models.ServerForUpdate` was modified

* `withLocation(java.lang.String)` was added
* `location()` was added

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
