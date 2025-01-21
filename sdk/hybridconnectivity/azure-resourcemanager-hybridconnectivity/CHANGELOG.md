# Release History

## 1.2.0 (2025-01-21)

- Azure Resource Manager HybridConnectivity client library for Java. This package contains Microsoft Azure SDK for HybridConnectivity Management SDK. REST API for public clouds. Package tag package-2024-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SolutionConfiguration` was added

* `models.GenerateAwsTemplateRequest` was added

* `models.PublicCloudConnector` was added

* `models.Inventories` was added

* `models.SolutionConfiguration$Definition` was added

* `models.SolutionConfigurationUpdate` was added

* `models.OperationStatusResult` was added

* `models.PublicCloudConnectorUpdate` was added

* `models.SolutionTypeSettingsProperties` was added

* `models.SolutionTypeResource` was added

* `models.SolutionTypeResourceListResult` was added

* `models.AwsCloudProfile` was added

* `models.InventoryResource` was added

* `models.SolutionTypeSettings` was added

* `models.SolutionConfigurationStatus` was added

* `models.SolutionConfigurations` was added

* `models.AzureResourceManagerCommonTypesTrackedResourceUpdate` was added

* `models.SolutionConfigurationProperties` was added

* `models.ResourceProvisioningState` was added

* `models.HostType` was added

* `models.InventoryResourceListResult` was added

* `models.PublicCloudConnectors` was added

* `models.SolutionConfigurationPropertiesUpdate` was added

* `models.PublicCloudConnectorProperties` was added

* `models.SolutionConfiguration$Update` was added

* `models.AwsCloudProfileUpdate` was added

* `models.PublicCloudConnector$Definition` was added

* `models.SolutionTypes` was added

* `models.PublicCloudConnector$DefinitionStages` was added

* `models.PublicCloudConnector$Update` was added

* `models.SolutionTypeProperties` was added

* `models.PublicCloudConnector$UpdateStages` was added

* `models.SolutionConfiguration$DefinitionStages` was added

* `models.PublicCloudConnectorListResult` was added

* `models.InventoryProperties` was added

* `models.PublicCloudConnectorPropertiesUpdate` was added

* `models.SolutionConfigurationListResult` was added

* `models.CloudNativeType` was added

* `models.GenerateAwsTemplates` was added

* `models.SolutionConfiguration$UpdateStages` was added

#### `HybridConnectivityManager` was modified

* `solutionTypes()` was added
* `generateAwsTemplates()` was added
* `publicCloudConnectors()` was added
* `inventories()` was added
* `solutionConfigurations()` was added

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
