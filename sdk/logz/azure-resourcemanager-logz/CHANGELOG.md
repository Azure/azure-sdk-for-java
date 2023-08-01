# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
