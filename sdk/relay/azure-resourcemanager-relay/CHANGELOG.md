# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2024-10-10)

- Azure Resource Manager Relay client library for Java. This package contains Microsoft Azure SDK for Relay Management SDK. Use these API to manage Azure Relay resources through Azure Resource Manager. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.RelayUpdateParameters` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceNamespacePatch` was modified

* `id()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.RelayNamespaceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WcfRelaysListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CheckNameAvailability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RegenerateAccessKeyParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AuthorizationRuleListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HybridConnectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2022-07-19)

- Azure Resource Manager Relay client library for Java. This package contains Microsoft Azure SDK for Relay Management SDK. Use these API to manage Azure Relay resources through Azure Resource Manager. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.RelayNamespace` was modified

* `resourceGroupName()` was added

#### `RelayManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.HybridConnection` was modified

* `resourceGroupName()` was added

#### `RelayManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.AuthorizationRule` was modified

* `regenerateKeys(models.RegenerateAccessKeyParameters)` was added
* `resourceGroupName()` was added
* `listKeys()` was added
* `listKeysWithResponse(com.azure.core.util.Context)` was added
* `regenerateKeysWithResponse(models.RegenerateAccessKeyParameters,com.azure.core.util.Context)` was added

#### `models.WcfRelay` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager Relay client library for Java. This package contains Microsoft Azure SDK for Relay Management SDK. Use these API to manage Azure Relay resources through Azure Resource Manager. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
