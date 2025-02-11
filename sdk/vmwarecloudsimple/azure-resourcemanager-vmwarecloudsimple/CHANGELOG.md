# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2024-10-17)

- Azure Resource Manager VMwareCloudSimple client library for Java. This package contains Microsoft Azure SDK for VMwareCloudSimple Management SDK. Description of the new service. Package tag package-2019-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.SkuAvailabilityListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualDisk` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomizationIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateCloudList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UsageListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableOperationDisplayPropertyServiceSpecificationMetricsItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineStopMode` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualDiskController` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomizationIpSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomizationPoliciesListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GuestOsnicCustomization` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UsageName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableOperationsListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomizationIdentityUserData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomizationNicSetting` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DedicatedCloudNodeListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourcePoolsListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableOperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DedicatedCloudServiceListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PatchPayload` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomizationHostname` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomizationIpAddress` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomizationSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableOperationDisplayPropertyServiceSpecificationMetricsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GuestOSCustomization` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineTemplateListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2023-01-19)

- Azure Resource Manager VMwareCloudSimple client library for Java. This package contains Microsoft Azure SDK for VMwareCloudSimple Management SDK. Description of the new service. Package tag package-2019-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.VirtualMachinesDeleteHeaders` was removed

* `models.VirtualMachinesStopResponse` was removed

* `models.VirtualMachinesCreateOrUpdateResponse` was removed

* `models.VirtualMachinesStartHeaders` was removed

* `models.VirtualMachinesDeleteResponse` was removed

* `models.VirtualMachinesStopHeaders` was removed

* `models.DedicatedCloudNodesCreateOrUpdateResponse` was removed

* `models.DedicatedCloudNodesCreateOrUpdateHeaders` was removed

* `models.VirtualMachinesStartResponse` was removed

* `models.VirtualMachinesCreateOrUpdateHeaders` was removed

#### `models.VirtualMachine` was modified

* `java.lang.Integer numberOfCores()` -> `int numberOfCores()`
* `start(com.azure.core.util.Context)` was removed
* `stop(models.StopMode,models.VirtualMachineStopMode,com.azure.core.util.Context)` was removed
* `stop(models.StopMode,models.VirtualMachineStopMode)` was removed
* `start()` was removed
* `stop()` was removed
* `java.lang.Integer amountOfRam()` -> `int amountOfRam()`

#### `models.DedicatedCloudNode` was modified

* `java.lang.Object created()` -> `java.time.OffsetDateTime created()`
* `java.lang.Integer nodesCount()` -> `int nodesCount()`
* `namePropertiesSkuDescriptionName()` was removed
* `idPropertiesSkuDescriptionId()` was removed

#### `models.DedicatedCloudNode$Definition` was modified

* `withNodesCount(java.lang.Integer)` was removed
* `withNamePropertiesSkuDescriptionName(java.lang.String)` was removed
* `withIdPropertiesSkuDescriptionId(java.lang.String)` was removed

#### `models.Operations` was modified

* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String)` was removed

#### `models.DedicatedCloudNodes` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.VirtualMachines` was modified

* `stop(java.lang.String,java.lang.String,models.StopMode,models.VirtualMachineStopMode,com.azure.core.util.Context)` was removed
* `stop(java.lang.String,java.lang.String,models.StopMode,models.VirtualMachineStopMode)` was removed
* `start(java.lang.String,java.lang.String)` was removed
* `stop(java.lang.String,java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `start(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.VirtualMachine$Definition` was modified

* `withAmountOfRam(java.lang.Integer)` was removed
* `withNumberOfCores(java.lang.Integer)` was removed

### Features Added

* `models.VirtualMachineProperties` was added

#### `models.VirtualMachine` was modified

* `resourceGroupName()` was added

#### `models.DedicatedCloudNode` was modified

* `namePropertiesName()` was added
* `resourceGroupName()` was added
* `idPropertiesId()` was added

#### `models.DedicatedCloudService` was modified

* `resourceGroupName()` was added

#### `models.DedicatedCloudNode$Definition` was modified

* `withNodesCount(int)` was added
* `withIdPropertiesId(java.lang.String)` was added
* `withNamePropertiesName(java.lang.String)` was added
* `withReferer(java.lang.String)` was added

#### `models.Operations` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.DedicatedCloudNodes` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.VirtualMachines` was modified

* `stop(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String,models.StopMode,models.VirtualMachineStopMode,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.VirtualMachine$Definition` was modified

* `withReferer(java.lang.String)` was added
* `withAmountOfRam(int)` was added
* `withNumberOfCores(int)` was added

#### `VMwareCloudSimpleManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `VMwareCloudSimpleManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-22)

- Azure Resource Manager VMwareCloudSimple client library for Java. This package contains Microsoft Azure SDK for VMwareCloudSimple Management SDK. Description of the new service. Package tag package-2019-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
