# Release History

## 1.2.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0-beta.1 (2025-02-26)

- Azure Resource Manager Hybrid Connectivity client library for Java. This package contains Microsoft Azure SDK for Hybrid Connectivity Management SDK. REST API for public clouds. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.EndpointsList` was removed

#### `models.OperationListResult` was removed

#### `models.ServiceConfigurationList` was removed

#### `HybridConnectivityManager` was modified

* `fluent.HybridConnectivityManagementApi serviceClient()` -> `fluent.HybridConnectivityMgmtClient serviceClient()`

### Features Added

* `models.SolutionConfiguration` was added

* `models.GenerateAwsTemplateRequest` was added

* `models.PublicCloudConnector` was added

* `models.SolutionSettings` was added

* `models.Inventories` was added

* `models.SolutionConfiguration$Definition` was added

* `implementation.models.ServiceConfigurationList` was added

* `models.OperationStatusResult` was added

* `models.SolutionTypeSettingsProperties` was added

* `models.SolutionTypeResource` was added

* `models.AwsCloudProfile` was added

* `implementation.models.SolutionConfigurationListResult` was added

* `implementation.models.SolutionTypeResourceListResult` was added

* `models.InventoryResource` was added

* `implementation.models.EndpointsList` was added

* `models.SolutionTypeSettings` was added

* `models.SolutionConfigurationStatus` was added

* `models.SolutionConfigurations` was added

* `models.SolutionConfigurationProperties` was added

* `models.ResourceProvisioningState` was added

* `models.HostType` was added

* `models.PublicCloudConnectors` was added

* `models.PublicCloudConnectorProperties` was added

* `models.SolutionConfiguration$Update` was added

* `models.PublicCloudConnector$Definition` was added

* `models.SolutionTypes` was added

* `implementation.models.PublicCloudConnectorListResult` was added

* `models.PublicCloudConnector$DefinitionStages` was added

* `models.PublicCloudConnector$Update` was added

* `models.SolutionTypeProperties` was added

* `models.PostResponse` was added

* `models.PublicCloudConnector$UpdateStages` was added

* `models.SolutionConfiguration$DefinitionStages` was added

* `implementation.models.OperationListResult` was added

* `models.InventoryProperties` was added

* `implementation.models.InventoryResourceListResult` was added

* `models.CloudNativeType` was added

* `models.GenerateAwsTemplates` was added

* `models.SolutionConfiguration$UpdateStages` was added

#### `HybridConnectivityManager` was modified

* `publicCloudConnectors()` was added
* `inventories()` was added
* `solutionConfigurations()` was added
* `solutionTypes()` was added
* `generateAwsTemplates()` was added

## 1.1.0 (2024-12-11)

- Azure Resource Manager HybridConnectivity client library for Java. This package contains Microsoft Azure SDK for HybridConnectivity Management SDK. REST API for Hybrid Connectivity. Package tag package-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0 (2023-09-22)

- Azure Resource Manager HybridConnectivity client library for Java. This package contains Microsoft Azure SDK for HybridConnectivity Management SDK. REST API for Hybrid Connectivity. Package tag package-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ServiceConfigurationResource` was modified

* `systemData()` was added

## 1.0.0-beta.1 (2023-08-30)

- Azure Resource Manager HybridConnectivity client library for Java. This package contains Microsoft Azure SDK for HybridConnectivity Management SDK. REST API for Hybrid Connectivity. Package tag package-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
