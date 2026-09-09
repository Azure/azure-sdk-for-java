# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.1 (2026-08-12)

- Azure Resource Manager Fabric client library for Java. This package contains Microsoft Azure SDK for Fabric Management SDK.  Package api-version 2026-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.FabricCapacityUpdate` was modified

* `validate()` was removed

#### `models.FabricCapacityProperties` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.RpSku` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.FabricCapacityUpdateProperties` was modified

* `validate()` was removed

#### `models.CapacityAdministration` was modified

* `validate()` was removed

### Features Added

* `models.QuotaName` was added

* `models.Quota` was added

* `models.CapacityOverageState` was added

* `models.CapacityOverageProperties` was added

#### `models.FabricCapacityProperties` was modified

* `overage()` was added
* `withOverage(models.CapacityOverageProperties)` was added

#### `models.FabricCapacityUpdateProperties` was modified

* `overage()` was added
* `withOverage(models.CapacityOverageProperties)` was added

#### `models.FabricCapacities` was modified

* `listUsages(java.lang.String)` was added
* `listUsages(java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0 (2024-10-17)

- Azure Resource Manager Fabric client library for Java. This package contains Microsoft Azure SDK for Fabric Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `FabricManager` was modified

* `fluent.FabricClient serviceClient()` -> `fluent.FabricManagementClient serviceClient()`

## 1.0.0-beta.1 (2024-09-23)

- Azure Resource Manager Fabric client library for Java. This package contains Microsoft Azure SDK for Fabric Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-fabric Java SDK.
