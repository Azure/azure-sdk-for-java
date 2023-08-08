# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-11)

- Azure Resource Manager Advisor client library for Java. This package contains Microsoft Azure SDK for Advisor Management SDK. REST APIs for Azure Advisor. Package tag package-2020-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Risk` was removed

#### `models.ResourceRecommendationBase` was modified

* `risk()` was removed

### Features Added

#### `AdvisorManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `AdvisorManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager Advisor client library for Java. This package contains Microsoft Azure SDK for Advisor Management SDK. REST APIs for Azure Advisor. Package tag package-2020-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
