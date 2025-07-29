# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2025-02-14)

- Please note, this package has been deprecated and will no longer be maintained after 01/01/2025. Refer to our deprecation policy (https://aka.ms/azsdk/support-policies) for more details.

### Other Changes

- Please note, this package has been deprecated and will no longer be maintained after 01/01/2025. Refer to our deprecation policy (https://aka.ms/azsdk/support-policies) for more details.

## 1.0.0-beta.3 (2024-10-17)

- Azure Resource Manager logz client library for Java. This package contains Microsoft Azure SDK for logz Management SDK.  Package tag package-2020-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.FilteringTag` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogzSingleSignOnProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VMHostUpdateRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MonitorProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LogzMonitorResourceUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LogzMonitorResourceListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MonitorUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PlanData` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MonitoredResourceListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogzSingleSignOnResourceListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserRoleRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IdentityProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VMResourcesListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MonitoringTagRulesListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MonitoringTagRulesProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogRules` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogzOrganizationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserRoleListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2023-01-17)

- Azure Resource Manager logz client library for Java. This package contains Microsoft Azure SDK for logz Management SDK.  Package tag package-2020-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.MonitorsDeleteHeaders` was removed

* `models.SubAccountsDeleteResponse` was removed

* `models.MonitorsDeleteResponse` was removed

* `models.SubAccountsDeleteHeaders` was removed

#### `models.SubAccounts` was modified

* `create(java.lang.String,java.lang.String,java.lang.String,fluent.models.LogzMonitorResourceInner)` was removed

### Features Added

#### `models.LogzMonitorResource` was modified

* `resourceGroupName()` was added

#### `models.LogzSingleSignOnResource` was modified

* `resourceGroupName()` was added

#### `LogzManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.MonitoringTagRules` was modified

* `resourceGroupName()` was added

#### `LogzManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.1 (2021-10-08)

- Azure Resource Manager logz client library for Java. This package contains Microsoft Azure SDK for logz Management SDK.  Package tag package-2020-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
