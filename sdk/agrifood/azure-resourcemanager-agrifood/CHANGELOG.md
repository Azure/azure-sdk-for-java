# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2024-12-06)

- Azure Resource Manager AgriFood client library for Java. This package contains Microsoft Azure SDK for AgriFood Management SDK. APIs documentation for Azure AgFoodPlatform Resource Provider Service. Package tag package-preview-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.Extensions` was modified

* `createWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String)` was removed
* `create(java.lang.String,java.lang.String,java.lang.String)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.FarmBeatsModels` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getOperationResult(java.lang.String,java.lang.String,java.lang.String)` was removed
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

* `resourceGroupName()` was added
* `refresh()` was added
* `refresh(com.azure.core.util.Context)` was added
* `additionalApiProperties()` was added
* `update()` was added
* `systemData()` was added

#### `AgriFoodManager` was modified

* `solutionsDiscoverabilities()` was added
* `solutions()` was added
* `operationResults()` was added

#### `models.FarmBeats` was modified

* `systemData()` was added

#### `models.ArmAsyncOperation` was modified

* `error()` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added

#### `models.Extensions` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `deleteById(java.lang.String)` was added

#### `models.FarmBeatsModels` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2022-09-07)

- Azure Resource Manager AgriFood client library for Java. This package contains Microsoft Azure SDK for AgriFood Management SDK. APIs documentation for Azure AgFoodPlatform Resource Provider Service. Package tag package-preview-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
