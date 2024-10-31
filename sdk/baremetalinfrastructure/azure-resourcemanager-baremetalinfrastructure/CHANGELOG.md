# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2024-10-31)

- Azure Resource Manager BareMetalInfrastructure client library for Java. This package contains Microsoft Azure SDK for BareMetalInfrastructure Management SDK. The Bare Metal Infrastructure Management client. Package tag package-preview-2023-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.AzureBareMetalInstancesListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationStatusError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Disk` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HardwareProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageBillingProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ForceState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OSProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkInterface` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Tags` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBareMetalStorageInstancesListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.3 (2023-10-23)

- Azure Resource Manager BareMetalInfrastructure client library for Java. This package contains Microsoft Azure SDK for BareMetalInfrastructure Management SDK. The Bare Metal Infrastructure Management client. Package tag package-preview-2023-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.OperationList` was removed

* `models.Display` was removed

* `models.IpAddress` was removed

#### `models.Operation` was modified

* `models.Display display()` -> `models.OperationDisplay display()`

### Features Added

* `models.Origin` was added

* `models.AzureBareMetalInstanceForcePowerState` was added

* `models.AzureBareMetalStorageInstance` was added

* `models.OperationDisplay` was added

* `models.AzureBareMetalStorageInstance$UpdateStages` was added

* `models.AzureBareMetalStorageInstance$Update` was added

* `models.AsyncOperationStatus` was added

* `models.OperationStatusError` was added

* `models.StorageProperties` was added

* `models.OperationStatus` was added

* `models.AzureBareMetalStorageInstance$DefinitionStages` was added

* `models.AzureBareMetalStorageInstances` was added

* `models.ProvisioningState` was added

* `models.AzureBareMetalStorageInstance$Definition` was added

* `models.OperationListResult` was added

* `models.StorageBillingProperties` was added

* `models.ForceState` was added

* `models.NetworkInterface` was added

* `models.ActionType` was added

* `models.AzureBareMetalStorageInstancesListResult` was added

#### `models.Operation` was modified

* `actionType()` was added
* `origin()` was added

#### `models.AzureBareMetalInstances` was modified

* `start(java.lang.String,java.lang.String)` was added
* `shutdown(java.lang.String,java.lang.String)` was added
* `shutdown(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `restart(java.lang.String,java.lang.String,models.ForceState,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `restart(java.lang.String,java.lang.String)` was added

#### `BareMetalInfrastructureManager` was modified

* `azureBareMetalStorageInstances()` was added

## 1.0.0-beta.2 (2023-01-12)

- Azure Resource Manager BareMetalInfrastructure client library for Java. This package contains Microsoft Azure SDK for BareMetalInfrastructure Management SDK. The BareMetalInfrastructure Management client. Package tag package-2021-08-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `BareMetalInfrastructureManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `BareMetalInfrastructureManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.1 (2021-09-22)

- Azure Resource Manager BareMetalInfrastructure client library for Java. This package contains Microsoft Azure SDK for BareMetalInfrastructure Management SDK. The BareMetalInfrastructure Management client. Package tag package-2021-08-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

