# Release History

## 1.0.0-beta.4 (2026-05-08)

- Azure Resource Manager Hana client library for Java. This package contains Microsoft Azure SDK for Hana Management SDK. The SAP HANA on Azure Management Client. Package api-version 2020-02-07-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SapSystemId` was removed

#### `models.HanaInstance` was removed

#### `models.HanaInstancePowerStateEnum` was removed

#### `models.HardwareProfile` was removed

#### `models.HanaInstancesListResult` was removed

#### `models.HanaInstance$Definition` was removed

#### `models.HanaInstanceSizeNamesEnum` was removed

#### `models.StorageProfile` was removed

#### `models.HanaHardwareTypeNamesEnum` was removed

#### `models.HanaInstance$Update` was removed

#### `models.OperationList` was removed

#### `models.HanaInstance$UpdateStages` was removed

#### `models.NetworkProfile` was removed

#### `models.HanaInstance$DefinitionStages` was removed

#### `models.IpAddress` was removed

#### `models.HanaInstances` was removed

#### `models.Disk` was removed

#### `models.OSProfile` was removed

#### `models.Display` was modified

* `Display()` was changed to private access
* `validate()` was removed

#### `HanaManager` was modified

* `hanaInstances()` was removed

#### `models.Operation` was modified

* `isDataAction()` was removed

#### `models.Tags` was modified

* `validate()` was removed

### Features Added

* `models.SapMonitor$Definition` was added

* `models.SapMonitor` was added

* `models.SapMonitors` was added

* `models.ProviderInstances` was added

* `models.ProviderInstance` was added

* `models.ProviderInstance$Definition` was added

* `models.SapMonitor$UpdateStages` was added

* `models.SapMonitor$DefinitionStages` was added

* `models.ProviderInstance$DefinitionStages` was added

* `models.SapMonitor$Update` was added

#### `HanaManager` was modified

* `sapMonitors()` was added
* `providerInstances()` was added

## 1.0.0-beta.3 (2024-10-17)

- Azure Resource Manager Hana client library for Java. This package contains Microsoft Azure SDK for Hana Management SDK. HANA on Azure Client. Package tag package-2017-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.SapSystemId` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Display` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HardwareProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HanaInstancesListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IpAddress` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Tags` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Disk` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OSProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2023-01-16)

- Azure Resource Manager Hana client library for Java. This package contains Microsoft Azure SDK for Hana Management SDK. HANA on Azure Client. Package tag package-2017-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.HardwareProfile` was modified

* `withHanaInstanceSize(models.HanaInstanceSizeNamesEnum)` was removed
* `withHardwareType(models.HanaHardwareTypeNamesEnum)` was removed

#### `models.NetworkProfile` was modified

* `withCircuitId(java.lang.String)` was removed

#### `models.HanaInstance$Definition` was modified

* `withHanaInstanceId(java.lang.String)` was removed
* `withPowerState(models.HanaInstancePowerStateEnum)` was removed
* `withHwRevision(java.lang.String)` was removed
* `withProximityPlacementGroup(java.lang.String)` was removed
* `withProvisioningState(models.HanaProvisioningStatesEnum)` was removed

#### `models.OSProfile` was modified

* `withVersion(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed

#### `models.StorageProfile` was modified

* `withNfsIpAddress(java.lang.String)` was removed

### Features Added

* `models.SapSystemId` was added

#### `models.HanaInstance` was modified

* `resourceGroupName()` was added

#### `HanaManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.StorageProfile` was modified

* `hanaSids()` was added
* `withHanaSids(java.util.List)` was added

#### `HanaManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-15)

- Azure Resource Manager Hana client library for Java. This package contains Microsoft Azure SDK for Hana Management SDK. HANA on Azure Client. Package tag package-2017-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
