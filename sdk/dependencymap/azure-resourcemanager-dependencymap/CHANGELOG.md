# Release History

## 1.0.0-beta.2 (2025-09-23)

- Azure Resource Manager Dependency Map client library for Java. This package contains Microsoft Azure SDK for Dependency Map Management SDK. Microsoft.DependencyMap management service. Package api-version 2025-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DateTimeFilter` was modified

* `validate()` was removed

#### `models.DiscoverySourceResourceTagsUpdate` was modified

* `validate()` was removed

#### `models.DependencyMapVisualizationFilter` was modified

* `validate()` was removed

#### `models.GetConnectionsWithConnectedMachineForFocusedMachineRequest` was modified

* `validate()` was removed

#### `models.DiscoverySourceResourceProperties` was modified

* `validate()` was removed

#### `models.ProcessNameFilter` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.MapsResourceProperties` was modified

* `validate()` was removed

#### `models.GetDependencyViewForFocusedMachineRequest` was modified

* `validate()` was removed

#### `models.Maps` was modified

* `void exportDependencies(java.lang.String,java.lang.String,models.ExportDependenciesRequest)` -> `models.ExportDependenciesOperationResult exportDependencies(java.lang.String,java.lang.String,models.ExportDependenciesRequest)`
* `void exportDependencies(java.lang.String,java.lang.String,models.ExportDependenciesRequest,com.azure.core.util.Context)` -> `models.ExportDependenciesOperationResult exportDependencies(java.lang.String,java.lang.String,models.ExportDependenciesRequest,com.azure.core.util.Context)`

#### `models.MapsResource` was modified

* `void exportDependencies(models.ExportDependenciesRequest)` -> `models.ExportDependenciesOperationResult exportDependencies(models.ExportDependenciesRequest)`
* `void exportDependencies(models.ExportDependenciesRequest,com.azure.core.util.Context)` -> `models.ExportDependenciesOperationResult exportDependencies(models.ExportDependenciesRequest,com.azure.core.util.Context)`

#### `models.OffAzureDiscoverySourceResourceProperties` was modified

* `validate()` was removed

#### `models.ExportDependenciesRequest` was modified

* `validate()` was removed

#### `models.GetConnectionsForProcessOnFocusedMachineRequest` was modified

* `validate()` was removed

#### `models.MapsResourceTagsUpdate` was modified

* `validate()` was removed

### Features Added

* `models.ExportDependenciesStatusCode` was added

* `models.GetDependencyViewForAllMachinesRequest` was added

* `models.ExportDependenciesResultProperties` was added

* `models.DependencyProcessFilter` was added

* `models.ExportDependenciesAdditionalInfo` was added

* `models.ExportDependenciesOperationResult` was added

* `models.GetDependencyViewForAllMachinesOperationResult` was added

* `models.GetDependencyViewForAllMachinesResultProperties` was added

#### `models.Maps` was modified

* `getDependencyViewForAllMachines(java.lang.String,java.lang.String,models.GetDependencyViewForAllMachinesRequest)` was added
* `getDependencyViewForAllMachines(java.lang.String,java.lang.String,models.GetDependencyViewForAllMachinesRequest,com.azure.core.util.Context)` was added

#### `models.MapsResource` was modified

* `getDependencyViewForAllMachines(models.GetDependencyViewForAllMachinesRequest,com.azure.core.util.Context)` was added
* `getDependencyViewForAllMachines(models.GetDependencyViewForAllMachinesRequest)` was added

#### `models.ExportDependenciesRequest` was modified

* `applianceNameList()` was added
* `withApplianceNameList(java.util.List)` was added

## 1.0.0-beta.1 (2025-04-17)

- Azure Resource Manager Dependency Map client library for Java. This package contains Microsoft Azure SDK for Dependency Map Management SDK. Microsoft.DependencyMap management service. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-dependencymap Java SDK.
