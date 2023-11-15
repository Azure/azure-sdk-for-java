# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2023-10-23)

- Azure Resource Manager ResourceMover client library for Java. This package contains Microsoft Azure SDK for ResourceMover Management SDK. A first party Azure service orchestrating the move of Azure resources from one Azure region to another or between zones within a region. Package tag package-2023-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.MoveType` was added

#### `models.AvailabilitySetResourceSettings` was modified

* `withTags(java.util.Map)` was added
* `withTargetResourceGroupName(java.lang.String)` was added
* `tags()` was added

#### `models.ResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added
* `targetResourceGroupName()` was added

#### `models.LoadBalancerResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added
* `withTags(java.util.Map)` was added
* `tags()` was added

#### `models.SqlElasticPoolResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added
* `withTags(java.util.Map)` was added
* `tags()` was added

#### `models.MoveCollectionProperties` was modified

* `withMoveRegion(java.lang.String)` was added
* `withVersion(java.lang.String)` was added
* `moveRegion()` was added
* `withMoveType(models.MoveType)` was added
* `version()` was added
* `moveType()` was added

#### `models.ResourceGroupResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.PublicIpAddressResourceSettings` was modified

* `withTags(java.util.Map)` was added
* `tags()` was added
* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.SqlServerResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.VirtualNetworkResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added
* `tags()` was added
* `withTags(java.util.Map)` was added

#### `models.KeyVaultResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.DiskEncryptionSetResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added

#### `models.NetworkSecurityGroupResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added
* `tags()` was added
* `withTags(java.util.Map)` was added

#### `models.SqlDatabaseResourceSettings` was modified

* `withTags(java.util.Map)` was added
* `withTargetResourceGroupName(java.lang.String)` was added
* `tags()` was added

#### `models.VirtualMachineResourceSettings` was modified

* `withTargetResourceGroupName(java.lang.String)` was added
* `userManagedIdentities()` was added
* `withTags(java.util.Map)` was added
* `tags()` was added
* `withUserManagedIdentities(java.util.List)` was added

#### `models.MoveResource` was modified

* `systemData()` was added

#### `models.MoveCollection` was modified

* `systemData()` was added

#### `models.NetworkInterfaceResourceSettings` was modified

* `tags()` was added
* `withTargetResourceGroupName(java.lang.String)` was added
* `withTags(java.util.Map)` was added

## 1.0.0 (2023-02-24)

- Azure Resource Manager ResourceMover client library for Java. This package contains Microsoft Azure SDK for ResourceMover Management SDK. A first party Azure service orchestrating the move of Azure resources from one Azure region to another or between zones within a region. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.2 (2023-01-19)

- Azure Resource Manager ResourceMover client library for Java. This package contains Microsoft Azure SDK for ResourceMover Management SDK. A first party Azure service orchestrating the move of Azure resources from one Azure region to another or between zones within a region. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.MoveResourceFilterProperties` was removed

* `models.UnresolvedDependenciesFilterProperties` was removed

#### `models.MoveCollections` was modified

* `prepare(java.lang.String,java.lang.String,models.PrepareRequest)` was removed
* `commit(java.lang.String,java.lang.String,models.CommitRequest)` was removed
* `initiateMove(java.lang.String,java.lang.String,models.ResourceMoveRequest)` was removed
* `discard(java.lang.String,java.lang.String,models.DiscardRequest)` was removed
* `bulkRemove(java.lang.String,java.lang.String,models.BulkRemoveRequest)` was removed

#### `models.MoveCollection` was modified

* `discard(models.DiscardRequest)` was removed
* `initiateMove(models.ResourceMoveRequest)` was removed
* `commit(models.CommitRequest)` was removed
* `prepare(models.PrepareRequest)` was removed
* `bulkRemove(models.BulkRemoveRequest)` was removed

### Features Added

#### `ResourceMoverManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.MoveCollection` was modified

* `resourceGroupName()` was added

#### `ResourceMoverManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager ResourceMover client library for Java. This package contains Microsoft Azure SDK for ResourceMover Management SDK. A first party Azure service orchestrating the move of Azure resources from one Azure region to another or between zones within a region. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
