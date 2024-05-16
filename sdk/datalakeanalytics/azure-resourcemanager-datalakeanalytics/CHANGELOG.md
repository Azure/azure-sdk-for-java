# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-13)

- Azure Resource Manager DataLakeAnalytics client library for Java. This package contains Microsoft Azure SDK for DataLakeAnalytics Management SDK. Creates an Azure Data Lake Analytics account management client. Package tag package-2016-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.DataLakeAnalyticsAccountProperties` was removed

* `models.DataLakeAnalyticsAccountPropertiesBasic` was removed

### Features Added

#### `DataLakeAnalyticsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.DataLakeAnalyticsAccountListResult` was modified

* `count()` was added

#### `DataLakeAnalyticsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.FirewallRule` was modified

* `resourceGroupName()` was added

#### `models.ComputePolicy` was modified

* `resourceGroupName()` was added

#### `models.DataLakeAnalyticsAccount` was modified

* `maxQueuedJobCountPerUser()` was added
* `resourceGroupName()` was added
* `maxActiveJobCountPerUser()` was added
* `maxJobRunningTimeInMin()` was added

## 1.0.0-beta.1 (2021-04-22)

- Azure Resource Manager DataLakeAnalytics client library for Java. This package contains Microsoft Azure SDK for DataLakeAnalytics Management SDK. Creates an Azure Data Lake Analytics account management client. Package tag package-2016-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
