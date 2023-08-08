# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-18)

- Azure Resource Manager OperationsManagement client library for Java. This package contains Microsoft Azure SDK for OperationsManagement Management SDK. Operations Management Client. Package tag package-2015-11-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ManagementConfigurations` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.Solution` was modified

* `resourceGroupName()` was added

#### `models.ManagementConfiguration` was modified

* `resourceGroupName()` was added

#### `models.ManagementConfigurations` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `OperationsManagementManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `OperationsManagementManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-21)

- Azure Resource Manager OperationsManagement client library for Java. This package contains Microsoft Azure SDK for OperationsManagement Management SDK. Operations Management Client. Package tag package-2015-11-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
