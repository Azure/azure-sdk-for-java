# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-17)

- Azure Resource Manager HybridCompute client library for Java. This package contains Microsoft Azure SDK for HybridCompute Management SDK. The Hybrid Compute Management Client. Package tag package-preview-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Machines` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.MachineExtension` was modified

* `resourceGroupName()` was added

#### `HybridComputeManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.HybridComputePrivateLinkScope` was modified

* `resourceGroupName()` was added

#### `HybridComputeManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Machines` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-15)

- Azure Resource Manager HybridCompute client library for Java. This package contains Microsoft Azure SDK for HybridCompute Management SDK. The Hybrid Compute Management Client. Package tag package-preview-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
