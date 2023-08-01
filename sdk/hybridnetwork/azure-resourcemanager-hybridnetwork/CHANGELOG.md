# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-09-30)

- Azure Resource Manager HybridNetwork client library for Java. This package contains Microsoft Azure SDK for HybridNetwork Management SDK. The definitions in this swagger specification will be used to manage the Hybrid Network resources. Package tag package-2021-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Device$Definition` was modified

* `withAzureStackEdge(com.azure.core.management.SubResource)` was removed

#### `models.Device` was modified

* `status()` was removed
* `provisioningState()` was removed
* `azureStackEdge()` was removed
* `networkFunctions()` was removed

### Features Added

* `models.AzureStackEdgeFormat` was added

* `models.DevicePropertiesFormat` was added

#### `models.Device$Definition` was modified

* `withProperties(models.DevicePropertiesFormat)` was added

#### `models.Device` was modified

* `properties()` was added
* `resourceGroupName()` was added

#### `HybridNetworkManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `HybridNetworkManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.NetworkFunction` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-08-16)

- Azure Resource Manager HybridNetwork client library for Java. This package contains Microsoft Azure SDK for HybridNetwork Management SDK. The definitions in this swagger specification will be used to manage the Hybrid Network resources. Package tag package-2021-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

