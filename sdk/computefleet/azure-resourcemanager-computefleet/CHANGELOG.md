# Release History

## 1.0.0-beta.2 (2024-07-23)

- Azure Resource Manager Compute Fleet client library for Java. This package contains Microsoft Azure SDK for Compute Fleet Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.VirtualMachineScaleSetListResult` was removed

#### `models.VirtualMachineScaleSet` was modified

* `java.lang.String type()` -> `java.lang.String type()`
* `validate()` was removed
* `models.ProvisioningState operationStatus()` -> `models.ProvisioningState operationStatus()`
* `java.lang.String id()` -> `java.lang.String id()`
* `models.ApiError error()` -> `models.ApiError error()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `toJson(com.azure.json.JsonWriter)` was removed

#### `models.Fleets` was modified

* `listVirtualMachineScaleSetsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.VirtualMachineScaleSetListResult listVirtualMachineScaleSets(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listVirtualMachineScaleSets(java.lang.String,java.lang.String)`

### Features Added

* `implementation.models.VirtualMachineScaleSetListResult` was added

#### `models.VirtualMachineScaleSet` was modified

* `innerModel()` was added

#### `models.Fleets` was modified

* `listVirtualMachineScaleSets(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2024-07-22)

- Azure Resource Manager Compute Fleet client library for Java. This package contains Microsoft Azure SDK for Compute Fleet Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-computefleet Java SDK.
