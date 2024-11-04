# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
