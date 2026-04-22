# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2026-03-04)

- Azure Resource Manager Azure Traffic Collector client library for Java. This package contains Microsoft Azure SDK for Azure Traffic Collector Management SDK. Azure Traffic Collector service. Package api-version 2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CollectorPolicyListResult` was removed

#### `models.AzureTrafficCollectorListResult` was removed

#### `models.OperationListResult` was removed

#### `models.EmissionPolicyDestination` was modified

* `validate()` was removed

#### `models.ResourceReference` was modified

* `validate()` was removed

#### `models.IngestionSourcesPropertiesFormat` was modified

* `validate()` was removed

#### `models.EmissionPoliciesPropertiesFormat` was modified

* `validate()` was removed

#### `models.IngestionPolicyPropertiesFormat` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.TagsObject` was modified

* `validate()` was removed

## 1.0.0-beta.3 (2024-10-14)

- Azure Resource Manager AzureTrafficCollector client library for Java. This package contains Microsoft Azure SDK for AzureTrafficCollector Management SDK. Azure Traffic Collector service. Package tag package-2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.EmissionPolicyDestination` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CollectorPolicyListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureTrafficCollectorListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IngestionSourcesPropertiesFormat` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EmissionPoliciesPropertiesFormat` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IngestionPolicyPropertiesFormat` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TagsObject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2022-11-24)

- Azure Resource Manager AzureTrafficCollector client library for Java. This package contains Microsoft Azure SDK for AzureTrafficCollector Management SDK. Azure Traffic Collector service. Package tag package-2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CollectorPolicy$Update` was modified

* `withIngestionPolicy(models.IngestionPolicyPropertiesFormat)` was removed
* `withEmissionPolicies(java.util.List)` was removed

#### `models.AzureTrafficCollector$Definition` was modified

* `withCollectorPolicies(java.util.List)` was removed

### Features Added

#### `models.CollectorPolicy` was modified

* `region()` was added
* `regionName()` was added
* `location()` was added
* `tags()` was added

#### `models.CollectorPolicy$Update` was modified

* `withTags(java.util.Map)` was added

#### `models.CollectorPolicy$Definition` was modified

* `withRegion(java.lang.String)` was added
* `withRegion(com.azure.core.management.Region)` was added
* `withTags(java.util.Map)` was added

## 1.0.0-beta.1 (2022-07-08)

- Azure Resource Manager AzureTrafficCollector client library for Java. This package contains Microsoft Azure SDK for AzureTrafficCollector Management SDK. Azure Traffic Collector service. Package tag package-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
