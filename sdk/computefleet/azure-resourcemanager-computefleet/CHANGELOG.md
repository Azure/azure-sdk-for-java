# Release History

## 1.1.0-beta.1 (2025-08-13)

- Azure Resource Manager Compute Fleet client library for Java. This package contains Microsoft Azure SDK for Compute Fleet Management SDK.  Package api-version 2025-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ZoneDistributionStrategy` was added

* `models.ZonePreference` was added

* `models.FleetMode` was added

* `models.ZoneAllocationPolicy` was added

* `models.CapacityType` was added

* `models.VirtualMachine` was added

* `models.VMOperationStatus` was added

#### `models.Fleet` was modified

* `cancel(com.azure.core.util.Context)` was added
* `cancel()` was added

#### `models.FleetProperties` was modified

* `withCapacityType(models.CapacityType)` was added
* `withZoneAllocationPolicy(models.ZoneAllocationPolicy)` was added
* `withMode(models.FleetMode)` was added
* `mode()` was added
* `capacityType()` was added
* `zoneAllocationPolicy()` was added

#### `models.Fleets` was modified

* `listVirtualMachines(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `cancel(java.lang.String,java.lang.String)` was added
* `cancel(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listVirtualMachines(java.lang.String,java.lang.String)` was added

## 1.0.0 (2024-10-22)

- Azure Resource Manager Compute Fleet client library for Java. This package contains Microsoft Azure SDK for Compute Fleet Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `ComputeFleetManager` was modified

* `fluent.AzureFleetClient serviceClient()` -> `fluent.ComputeFleetManagementClient serviceClient()`

### Features Added

* `models.AdditionalCapabilities` was added

* `models.VMAttributeMinMaxInteger` was added

* `models.VMCategory` was added

* `models.VMAttributes` was added

* `models.CpuManufacturer` was added

* `models.ArchitectureType` was added

* `models.AcceleratorManufacturer` was added

* `models.VMAttributeMinMaxDouble` was added

* `models.LocalStorageDiskType` was added

* `models.LocationProfile` was added

* `models.AdditionalLocationsProfile` was added

* `models.VMAttributeSupport` was added

* `models.AcceleratorType` was added

#### `models.ComputeProfile` was modified

* `additionalVirtualMachineCapabilities()` was added
* `withAdditionalVirtualMachineCapabilities(models.AdditionalCapabilities)` was added

#### `models.FleetProperties` was modified

* `vmAttributes()` was added
* `withVmAttributes(models.VMAttributes)` was added
* `additionalLocationsProfile()` was added
* `withAdditionalLocationsProfile(models.AdditionalLocationsProfile)` was added

## 1.0.0-beta.2 (2024-07-23)

- Azure Resource Manager Compute Fleet client library for Java. This package contains Microsoft Azure SDK for Compute Fleet Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* Change `listVirtualMachineScaleSets` to pageable operation (as there is a bug fix in spec). 

## 1.0.0-beta.1 (2024-07-22)

- Azure Resource Manager Compute Fleet client library for Java. This package contains Microsoft Azure SDK for Compute Fleet Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-computefleet Java SDK.
