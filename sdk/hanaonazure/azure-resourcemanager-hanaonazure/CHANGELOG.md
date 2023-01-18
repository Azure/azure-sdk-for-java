# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-16)

- Azure Resource Manager Hana client library for Java. This package contains Microsoft Azure SDK for Hana Management SDK. HANA on Azure Client. Package tag package-2017-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.HardwareProfile` was modified

* `withHanaInstanceSize(models.HanaInstanceSizeNamesEnum)` was removed
* `withHardwareType(models.HanaHardwareTypeNamesEnum)` was removed

#### `models.NetworkProfile` was modified

* `withCircuitId(java.lang.String)` was removed

#### `models.HanaInstance$Definition` was modified

* `withHanaInstanceId(java.lang.String)` was removed
* `withPowerState(models.HanaInstancePowerStateEnum)` was removed
* `withHwRevision(java.lang.String)` was removed
* `withProximityPlacementGroup(java.lang.String)` was removed
* `withProvisioningState(models.HanaProvisioningStatesEnum)` was removed

#### `models.OSProfile` was modified

* `withVersion(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed

#### `models.StorageProfile` was modified

* `withNfsIpAddress(java.lang.String)` was removed

### Features Added

* `models.SapSystemId` was added

#### `models.HanaInstance` was modified

* `resourceGroupName()` was added

#### `HanaManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.StorageProfile` was modified

* `hanaSids()` was added
* `withHanaSids(java.util.List)` was added

#### `HanaManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-15)

- Azure Resource Manager Hana client library for Java. This package contains Microsoft Azure SDK for Hana Management SDK. HANA on Azure Client. Package tag package-2017-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
