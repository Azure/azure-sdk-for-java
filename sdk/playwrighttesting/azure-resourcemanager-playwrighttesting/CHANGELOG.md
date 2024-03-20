# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2024-02-27)

- Azure Resource Manager Playwright Testing client library for Java. This package contains Microsoft Azure SDK for Playwright Testing Management SDK. Azure Playwright testing management service. Package tag package-2023-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Account$Definition` was modified

* `withScalableExecution(models.EnablementStatus)` was removed
* `withReporting(models.EnablementStatus)` was removed
* `withRegionalAffinity(models.EnablementStatus)` was removed

#### `models.Account$Update` was modified

* `withReporting(models.EnablementStatus)` was removed
* `withRegionalAffinity(models.EnablementStatus)` was removed
* `withScalableExecution(models.EnablementStatus)` was removed

#### `models.AccountUpdate` was modified

* `reporting()` was removed
* `withScalableExecution(models.EnablementStatus)` was removed
* `withReporting(models.EnablementStatus)` was removed
* `scalableExecution()` was removed
* `regionalAffinity()` was removed
* `withRegionalAffinity(models.EnablementStatus)` was removed

#### `models.Quota` was modified

* `provisioningState()` was removed
* `freeTrial()` was removed

#### `models.Account` was modified

* `reporting()` was removed
* `dashboardUri()` was removed
* `provisioningState()` was removed
* `regionalAffinity()` was removed
* `scalableExecution()` was removed

### Features Added

* `models.QuotaProperties` was added

* `models.AccountProperties` was added

* `models.AccountUpdateProperties` was added

#### `models.Account$Definition` was modified

* `withProperties(models.AccountProperties)` was added

#### `models.Account$Update` was modified

* `withProperties(models.AccountUpdateProperties)` was added

#### `models.AccountUpdate` was modified

* `properties()` was added
* `withProperties(models.AccountUpdateProperties)` was added

#### `models.Quota` was modified

* `properties()` was added

#### `models.Account` was modified

* `properties()` was added

## 1.0.0-beta.1 (2023-09-27)

- Azure Resource Manager Playwright Testing client library for Java. This package contains Microsoft Azure SDK for Playwright Testing Management SDK. Azure Playwright testing management service. Package tag package-2023-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
