# Release History

## 1.0.0-beta.1 (2023-02-08)

- Azure Resource Manager ResourceMover client library for Java. This package contains Microsoft Azure SDK for ResourceMover Management SDK. A first party Azure service orchestrating the move of Azure resources from one Azure region to another or between zones within a region. Package tag package-2022-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
