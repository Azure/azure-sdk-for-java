# Release History

## 1.0.0-beta.3 (2022-07-25)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2022-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.MonitoringSettings` was added

* `models.VaultPropertiesRedundancySettings` was added

* `models.BackupStorageVersion` was added

* `models.StandardTierStorageRedundancy` was added

* `models.AzureMonitorAlertSettings` was added

* `models.ClassicAlertSettings` was added

* `models.CrossRegionRestore` was added

* `models.AlertsState` was added

#### `models.ResourceCertificateAndAadDetails` was modified

* `aadAudience()` was added
* `withAadAudience(java.lang.String)` was added

#### `models.VaultProperties` was modified

* `withMonitoringSettings(models.MonitoringSettings)` was added
* `backupStorageVersion()` was added
* `withRedundancySettings(models.VaultPropertiesRedundancySettings)` was added
* `redundancySettings()` was added
* `monitoringSettings()` was added

## 1.0.0-beta.2 (2022-07-19)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2021-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OperationResource` was added

* `models.ResourceProviders` was added

* `models.InfrastructureEncryptionState` was added

* `models.CmkKeyVaultProperties` was added

* `models.VaultPropertiesEncryption` was added

* `models.CmkKekIdentity` was added

* `models.UserIdentity` was added

* `models.VaultPropertiesMoveDetails` was added

* `models.ResourceMoveState` was added

#### `models.Sku` was modified

* `withSize(java.lang.String)` was added
* `family()` was added
* `withCapacity(java.lang.String)` was added
* `capacity()` was added
* `size()` was added
* `tier()` was added
* `withFamily(java.lang.String)` was added
* `withTier(java.lang.String)` was added

#### `RecoveryServicesManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `RecoveryServicesManager` was modified

* `resourceProviders()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.PrivateEndpointConnectionVaultProperties` was modified

* `name()` was added
* `type()` was added
* `location()` was added

#### `models.Vault` was modified

* `systemData()` was added
* `resourceGroupName()` was added

#### `models.IdentityData` was modified

* `withUserAssignedIdentities(java.util.Map)` was added
* `userAssignedIdentities()` was added

#### `models.ResourceCertificateAndAadDetails` was modified

* `serviceResourceId()` was added
* `withServiceResourceId(java.lang.String)` was added

#### `models.VaultProperties` was modified

* `moveDetails()` was added
* `encryption()` was added
* `withEncryption(models.VaultPropertiesEncryption)` was added
* `moveState()` was added
* `withMoveDetails(models.VaultPropertiesMoveDetails)` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager RecoveryServices client library for Java. This package contains Microsoft Azure SDK for RecoveryServices Management SDK. Recovery Services Client. Package tag package-2016-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
