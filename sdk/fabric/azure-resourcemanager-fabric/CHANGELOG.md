# Release History

## 1.0.0 (2024-10-17)

- Azure Resource Manager fabric client library for Java. This package contains Microsoft Azure SDK for fabric Management SDK. Microsoft.Fabric Resource Provider management API. Package tag package-2023-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `implementation.models.RpSkuEnumerationForNewResourceResult` was removed

* `implementation.models.RpSkuEnumerationForExistingResourceResult` was removed

* `implementation.models.OperationListResult` was removed

* `models.FabricCapacityProperties` was removed

* `models.FabricCapacityUpdateProperties` was removed

* `implementation.models.FabricCapacityListResult` was removed

#### `models.FabricCapacity$DefinitionStages` was modified

* `withProperties(models.FabricCapacityProperties)` was removed in stage 3
* `withSku(models.RpSku)` was removed in stage 4

#### `FabricManager` was modified

* `fluent.FabricClient serviceClient()` -> `fluent.MicrosoftFabricManagementService serviceClient()`

#### `models.FabricCapacityUpdate` was modified

* `withProperties(models.FabricCapacityUpdateProperties)` was removed
* `properties()` was removed

#### `models.FabricCapacity$Definition` was modified

* `withProperties(models.FabricCapacityProperties)` was removed

#### `models.FabricCapacity` was modified

* `properties()` was removed

#### `models.FabricCapacity$Update` was modified

* `withProperties(models.FabricCapacityUpdateProperties)` was removed

### Features Added

* `models.FabricCapacityListResult` was added

* `models.RpSkuEnumerationForNewResourceResult` was added

* `models.OperationListResult` was added

* `models.RpSkuEnumerationForExistingResourceResult` was added

#### `models.FabricCapacityUpdate` was modified

* `withAdministration(models.CapacityAdministration)` was added
* `administration()` was added

#### `models.FabricCapacity$Definition` was modified

* `withAdministration(models.CapacityAdministration)` was added

#### `models.FabricCapacity` was modified

* `state()` was added
* `provisioningState()` was added
* `administration()` was added

#### `models.FabricCapacity$Update` was modified

* `withAdministration(models.CapacityAdministration)` was added

## 1.0.0-beta.1 (2024-09-23)

- Azure Resource Manager Fabric client library for Java. This package contains Microsoft Azure SDK for Fabric Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-fabric Java SDK.
