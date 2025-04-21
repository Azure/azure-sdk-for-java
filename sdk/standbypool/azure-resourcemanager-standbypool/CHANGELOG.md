# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2025-04-21)

- Azure Resource Manager Standby Pool client library for Java. This package contains Microsoft Azure SDK for Standby Pool Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PoolResourceStateCount` was removed

#### `StandbyPoolManager` was modified

* `fluent.StandbyPoolClient serviceClient()` -> `fluent.StandbyPoolManagementClient serviceClient()`

### Features Added

* `models.PoolVirtualMachineState` was added

* `models.StandbyVirtualMachinePoolForecastValues` was added

* `models.HealthStateCode` was added

* `models.StandbyVirtualMachinePoolPrediction` was added

* `models.PoolContainerGroupState` was added

* `models.PoolContainerGroupStateCount` was added

* `models.PoolStatus` was added

* `models.PoolVirtualMachineStateCount` was added

* `models.StandbyContainerGroupPoolForecastValues` was added

* `models.StandbyContainerGroupPoolPrediction` was added

#### `models.StandbyContainerGroupPoolResourceProperties` was modified

* `zones()` was added
* `withZones(java.util.List)` was added

#### `models.StandbyVirtualMachinePoolRuntimeViewResourceProperties` was modified

* `status()` was added
* `prediction()` was added

#### `models.ContainerGroupInstanceCountSummary` was modified

* `zone()` was added

#### `models.StandbyContainerGroupPoolResourceUpdateProperties` was modified

* `withZones(java.util.List)` was added
* `zones()` was added

#### `models.StandbyContainerGroupPoolRuntimeViewResourceProperties` was modified

* `status()` was added
* `prediction()` was added

## 1.0.0 (2024-09-25)

- Azure Resource Manager Standby Pool client library for Java. This package contains Microsoft Azure SDK for Standby Pool Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.StandbyContainerGroupPoolRuntimeViewResource` was added

* `models.StandbyContainerGroupPoolRuntimeViews` was added

* `models.VirtualMachineInstanceCountSummary` was added

* `models.StandbyVirtualMachinePoolRuntimeViews` was added

* `models.StandbyVirtualMachinePoolRuntimeViewResourceProperties` was added

* `models.ContainerGroupInstanceCountSummary` was added

* `models.PoolResourceStateCount` was added

* `models.StandbyVirtualMachinePoolRuntimeViewResource` was added

* `models.StandbyContainerGroupPoolRuntimeViewResourceProperties` was added

#### `models.StandbyVirtualMachinePoolElasticityProfile` was modified

* `withMinReadyCapacity(java.lang.Long)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `minReadyCapacity()` was added

#### `models.StandbyContainerGroupPoolResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `StandbyPoolManager` was modified

* `standbyContainerGroupPoolRuntimeViews()` was added
* `standbyVirtualMachinePoolRuntimeViews()` was added

#### `models.StandbyVirtualMachinePoolResourceUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StandbyVirtualMachineResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StandbyVirtualMachinePoolResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerGroupProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StandbyContainerGroupPoolResourceUpdateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StandbyContainerGroupPoolResourceUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Subnet` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StandbyVirtualMachinePoolResourceUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StandbyContainerGroupPoolElasticityProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerGroupProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.1 (2024-04-25)

- Azure Resource Manager Standby Pool client library for Java. This package contains Microsoft Azure SDK for Standby Pool Management SDK.  Package tag package-preview-2023-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

