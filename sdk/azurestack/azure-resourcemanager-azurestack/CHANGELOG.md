# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-12)

- Azure Resource Manager AzureStack client library for Java. This package contains Microsoft Azure SDK for AzureStack Management SDK. Azure Stack. Package tag package-2022-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.LinkedSubscription$DefinitionStages` was removed

* `models.LinkedSubscriptionParameter` was removed

* `models.LinkedSubscription$Definition` was removed

* `models.LinkedSubscription$UpdateStages` was removed

* `models.LinkedSubscriptionsList` was removed

* `models.LinkedSubscription$Update` was removed

* `models.LinkedSubscription` was removed

* `models.LinkedSubscriptions` was removed

* `models.ExtendedProductProperties` was removed

#### `models.ExtendedProduct` was modified

* `versionPropertiesVersion()` was removed

#### `models.Product` was modified

* `systemData()` was removed

#### `models.Registration` was modified

* `systemData()` was removed
* `kind()` was removed

#### `models.CustomerSubscription` was modified

* `systemData()` was removed

#### `models.Registrations` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `AzureStackManager` was modified

* `linkedSubscriptions()` was removed

### Features Added

* `models.DeploymentLicenseResponse` was added

* `models.DeploymentLicenses` was added

* `models.DeploymentLicenseRequest` was added

#### `AzureStackManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.Registration` was modified

* `resourceGroupName()` was added

#### `models.Registrations` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(com.azure.core.util.Context)` was added
* `list()` was added

#### `models.Products` was modified

* `listProductsWithResponse(java.lang.String,java.lang.String,java.lang.String,models.DeviceConfiguration,com.azure.core.util.Context)` was added
* `listProducts(java.lang.String,java.lang.String,java.lang.String)` was added

#### `AzureStackManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `deploymentLicenses()` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager AzureStack client library for Java. This package contains Microsoft Azure SDK for AzureStack Management SDK. Azure Stack. Package tag package-preview-2020-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
