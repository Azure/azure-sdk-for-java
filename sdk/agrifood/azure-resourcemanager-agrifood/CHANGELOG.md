# Release History

## 1.0.0-beta.2 (2024-10-10)

- Azure Resource Manager AgriFood client library for Java. This package contains Microsoft Azure SDK for AgriFood Management SDK. APIs documentation for Azure AgFoodPlatform Resource Provider Service. Package tag package-preview-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Identity` was modified

* `java.lang.String principalId()` -> `java.util.UUID principalId()`
* `java.lang.String tenantId()` -> `java.util.UUID tenantId()`

#### `models.Extensions` was modified

* `createWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `create(java.lang.String,java.lang.String,java.lang.String)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.FarmBeatsModels` was modified

* `getOperationResult(java.lang.String,java.lang.String,java.lang.String)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getOperationResultWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ResourceParameter` was added

* `models.FarmBeatsSolution` was added

* `models.Solution$Definition` was added

* `models.SolutionListResponse` was added

* `models.ArmAsyncOperationError` was added

* `models.MarketplaceOfferDetails` was added

* `models.SolutionInstallationRequest` was added

* `models.SolutionProperties` was added

* `models.ExtensionInstallationRequest` was added

* `models.Extension$UpdateStages` was added

* `models.Solution$DefinitionStages` was added

* `models.SolutionsDiscoverabilities` was added

* `models.FarmBeatsSolutionListResponse` was added

* `models.Extension$Definition` was added

* `models.OperationResults` was added

* `models.Solutions` was added

* `models.InsightAttachment` was added

* `models.ApiProperties` was added

* `models.Solution` was added

* `models.Extension$Update` was added

* `models.Insight` was added

* `models.Measure` was added

* `models.FarmBeatsSolutionProperties` was added

* `models.Solution$Update` was added

* `models.Extension$DefinitionStages` was added

* `models.Solution$UpdateStages` was added

* `models.SolutionEvaluatedOutput` was added

#### `models.Extension` was modified

* `refresh()` was added
* `update()` was added
* `additionalApiProperties()` was added
* `resourceGroupName()` was added
* `systemData()` was added
* `refresh(com.azure.core.util.Context)` was added

#### `models.ExtensionListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `AgriFoodManager` was modified

* `solutions()` was added
* `solutionsDiscoverabilities()` was added
* `operationResults()` was added

#### `models.FarmBeatsUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FarmBeats` was modified

* `systemData()` was added

#### `models.DetailedInformation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FarmBeatsUpdateRequestModel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SensorIntegration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ArmAsyncOperation` was modified

* `error()` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FarmBeatsExtensionListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added

#### `models.FarmBeatsListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Identity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Extensions` was modified

* `getById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `define(java.lang.String)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UnitSystemsInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResourceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FarmBeatsModels` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2022-09-07)

- Azure Resource Manager AgriFood client library for Java. This package contains Microsoft Azure SDK for AgriFood Management SDK. APIs documentation for Azure AgFoodPlatform Resource Provider Service. Package tag package-preview-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
