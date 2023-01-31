# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-18)

- Azure Resource Manager Peering client library for Java. This package contains Microsoft Azure SDK for Peering Management SDK. Peering Client. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Peerings` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PeeringServices` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.PeeringServicePrefix` was modified

* `resourceGroupName()` was added

#### `models.PeeringRegisteredPrefix` was modified

* `resourceGroupName()` was added

#### `models.Peering` was modified

* `resourceGroupName()` was added

#### `models.Peerings` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PeeringServices` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `PeeringManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.PeeringService` was modified

* `resourceGroupName()` was added

#### `PeeringManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.PeeringRegisteredAsn` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-19)

- Azure Resource Manager Peering client library for Java. This package contains Microsoft Azure SDK for Peering Management SDK. Peering Client. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
