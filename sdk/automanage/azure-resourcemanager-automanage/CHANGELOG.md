# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2024-10-10)

- Azure Resource Manager Automanage client library for Java. This package contains Microsoft Azure SDK for Automanage Management SDK. Automanage Client. Package tag package-2022-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Report` was modified

* `typePropertiesType()` was removed
* `resources()` was removed
* `status()` was removed
* `lastModifiedTime()` was removed
* `configurationProfile()` was removed
* `endTime()` was removed
* `duration()` was removed
* `reportFormatVersion()` was removed
* `startTime()` was removed
* `error()` was removed

#### `models.ConfigurationProfile$Definition` was modified

* `withProperties(fluent.models.ConfigurationProfileProperties)` was removed

#### `models.BestPractice` was modified

* `configuration()` was removed

#### `models.ConfigurationProfile$Update` was modified

* `withProperties(fluent.models.ConfigurationProfileProperties)` was removed

#### `models.ConfigurationProfileUpdate` was modified

* `fluent.models.ConfigurationProfileProperties properties()` -> `models.ConfigurationProfileProperties properties()`
* `withProperties(fluent.models.ConfigurationProfileProperties)` was removed

#### `models.ConfigurationProfile` was modified

* `fluent.models.ConfigurationProfileProperties properties()` -> `models.ConfigurationProfileProperties properties()`

#### `models.ServicePrincipal` was modified

* `authorizationSet()` was removed
* `servicePrincipalId()` was removed

#### `models.ConfigurationProfiles` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ServicePrincipalProperties` was added

* `models.AssignmentReportProperties` was added

* `models.ConfigurationProfileProperties` was added

#### `models.Report` was modified

* `properties()` was added

#### `models.ConfigurationProfileList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConfigurationProfile$Definition` was modified

* `withProperties(models.ConfigurationProfileProperties)` was added

#### `models.ConfigurationProfileAssignmentProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BestPractice` was modified

* `properties()` was added

#### `models.ConfigurationProfile$Update` was modified

* `withProperties(models.ConfigurationProfileProperties)` was added

#### `models.UpdateResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReportResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BestPracticeList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConfigurationProfileUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withProperties(models.ConfigurationProfileProperties)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServicePrincipalListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReportList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConfigurationProfileAssignmentList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServicePrincipal` was modified

* `properties()` was added

#### `models.ConfigurationProfiles` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2022-08-09)

- Azure Resource Manager Automanage client library for Java. This package contains Microsoft Azure SDK for Automanage Management SDK. Automanage Client. Package tag package-2022-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
