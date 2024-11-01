# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2024-10-31)

- Azure Resource Manager Dynatrace client library for Java. This package contains Microsoft Azure SDK for Dynatrace Management SDK.  Package tag package-2023-04-27. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinkableEnvironmentListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AppServiceListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FilteringTag` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynatraceEnvironmentProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TagRuleListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SsoDetailsRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogRules` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MonitorResourceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkableEnvironmentRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EnvironmentInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IdentityProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VMHostsListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MonitorResourceUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AccountInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PlanData` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MarketplaceSaaSResourceDetailsRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynatraceSingleSignOnResourceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MonitoredResourceListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MetricRules` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.3 (2023-08-22)

- Azure Resource Manager Dynatrace client library for Java. This package contains Microsoft Azure SDK for Dynatrace Management SDK.  Package tag package-2023-04-27. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AccountInfoSecure` was removed

* `models.TagRuleUpdate` was removed

#### `models.Monitors` was modified

* `getAccountCredentials(java.lang.String,java.lang.String)` was removed
* `getAccountCredentialsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.MonitorResourceUpdate` was modified

* `planData()` was removed
* `withMonitoringStatus(models.MonitoringStatus)` was removed
* `withUserInfo(models.UserInfo)` was removed
* `marketplaceSubscriptionStatus()` was removed
* `monitoringStatus()` was removed
* `dynatraceEnvironmentProperties()` was removed
* `withMarketplaceSubscriptionStatus(models.MarketplaceSubscriptionStatus)` was removed
* `withDynatraceEnvironmentProperties(models.DynatraceEnvironmentProperties)` was removed
* `withPlanData(models.PlanData)` was removed
* `userInfo()` was removed

#### `models.MonitorResource` was modified

* `getAccountCredentialsWithResponse(com.azure.core.util.Context)` was removed
* `getAccountCredentials()` was removed

#### `models.MonitorResource$Update` was modified

* `withDynatraceEnvironmentProperties(models.DynatraceEnvironmentProperties)` was removed
* `withMarketplaceSubscriptionStatus(models.MarketplaceSubscriptionStatus)` was removed
* `withMonitoringStatus(models.MonitoringStatus)` was removed
* `withPlanData(models.PlanData)` was removed
* `withUserInfo(models.UserInfo)` was removed

### Features Added

* `models.MarketplaceSaaSResourceDetailsResponse` was added

* `models.MetricsStatusResponse` was added

* `models.MarketplaceSaaSResourceDetailsRequest` was added

#### `models.Monitors` was modified

* `getMetricStatus(java.lang.String,java.lang.String)` was added
* `getMarketplaceSaaSResourceDetailsWithResponse(models.MarketplaceSaaSResourceDetailsRequest,com.azure.core.util.Context)` was added
* `getMetricStatusWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getMarketplaceSaaSResourceDetails(models.MarketplaceSaaSResourceDetailsRequest)` was added

#### `models.MonitorResource` was modified

* `getMetricStatus()` was added
* `getMetricStatusWithResponse(com.azure.core.util.Context)` was added

#### `models.MetricRules` was modified

* `sendingMetrics()` was added
* `withSendingMetrics(models.SendingMetricsStatus)` was added

## 1.0.0-beta.2 (2022-09-19)

- Azure Resource Manager Dynatrace client library for Java. This package contains Microsoft Azure SDK for Dynatrace Management SDK.  Package tag package-2021-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2022-05-25)

- Azure Resource Manager Dynatrace client library for Java. This package contains Microsoft Azure SDK for Dynatrace Management SDK.  Package tag package-2021-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
