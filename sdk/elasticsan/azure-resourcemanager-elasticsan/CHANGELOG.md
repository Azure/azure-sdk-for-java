# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-07-25)

- Azure Resource Manager ElasticSan client library for Java. This package contains Microsoft Azure SDK for ElasticSan Management SDK.  Package tag package-preview-2022-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ElasticSanRPOperation` was removed

* `models.ElasticSanOperationListResult` was removed

* `models.ElasticSanOperationDisplay` was removed

#### `models.Volume$DefinitionStages` was modified

* Stage 2 was added

#### `models.VolumeUpdate` was modified

* `withTags(java.util.Map)` was removed
* `tags()` was removed

#### `models.VolumeGroup$Update` was modified

* `withTags(java.util.Map)` was removed

#### `models.VolumeGroupUpdate` was modified

* `withTags(java.util.Map)` was removed
* `tags()` was removed

#### `models.Volume` was modified

* `java.lang.Long sizeGiB()` -> `long sizeGiB()`
* `systemData()` was removed
* `tags()` was removed

#### `models.Volume$Update` was modified

* `withTags(java.util.Map)` was removed

#### `models.Volume$Definition` was modified

* `withTags(java.util.Map)` was removed
* `withSizeGiB(java.lang.Long)` was removed

#### `models.VolumeGroup$Definition` was modified

* `withTags(java.util.Map)` was removed

#### `models.VolumeGroup` was modified

* `tags()` was removed

### Features Added

* `models.PrivateLinkResourceListResult` was added

* `models.PrivateEndpointConnections` was added

* `models.Operation` was added

* `models.ActionType` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.PrivateEndpoint` was added

* `models.PrivateLinkResources` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.PrivateLinkResource` was added

* `models.Origin` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.OperationDisplay` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.OperationListResult` was added

#### `models.ElasticSan` was modified

* `privateEndpointConnections()` was added

#### `models.SkuInformationList` was modified

* `nextLink()` was added

#### `ElasticSanManager` was modified

* `privateEndpointConnections()` was added
* `privateLinkResources()` was added

#### `models.Volume$Definition` was modified

* `withSizeGiB(long)` was added

#### `models.VolumeGroup` was modified

* `privateEndpointConnections()` was added

## 1.0.0-beta.1 (2022-10-21)

- Azure Resource Manager ElasticSan client library for Java. This package contains Microsoft Azure SDK for ElasticSan Management SDK.  Package tag package-2021-11-20-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

