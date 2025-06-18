# Release History

## 1.2.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0-beta.2 (2025-04-18)

- Azure Resource Manager ElasticSan client library for Java. This package contains Microsoft Azure SDK for ElasticSan Management SDK.  Package tag package-2024-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.VolumeGroups` was modified

* `listByElasticSan(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Volumes` was modified

* `listByVolumeGroup(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.XMsDeleteSnapshots,models.XMsForceDelete,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,models.XMsDeleteSnapshots,models.XMsForceDelete,com.azure.core.util.Context)` was removed

### Features Added

* `models.PreValidationResponse` was added

* `models.PolicyState` was added

* `models.XMsAccessSoftDeletedResources` was added

* `models.DiskSnapshotList` was added

* `models.VolumeNameList` was added

* `models.DeleteRetentionPolicy` was added

* `models.DeleteType` was added

* `models.ResourceProviders` was added

#### `models.VolumeGroup$Update` was modified

* `withDeleteRetentionPolicy(models.DeleteRetentionPolicy)` was added

#### `models.VolumeGroups` was modified

* `listByElasticSan(java.lang.String,java.lang.String,models.XMsAccessSoftDeletedResources,com.azure.core.util.Context)` was added

#### `models.VolumeGroupUpdate` was modified

* `deleteRetentionPolicy()` was added
* `withDeleteRetentionPolicy(models.DeleteRetentionPolicy)` was added

#### `models.Volumes` was modified

* `listByVolumeGroup(java.lang.String,java.lang.String,java.lang.String,models.XMsAccessSoftDeletedResources,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.XMsDeleteSnapshots,models.XMsForceDelete,models.DeleteType,com.azure.core.util.Context)` was added
* `preBackup(java.lang.String,java.lang.String,java.lang.String,models.VolumeNameList,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,models.XMsDeleteSnapshots,models.XMsForceDelete,models.DeleteType,com.azure.core.util.Context)` was added
* `preRestore(java.lang.String,java.lang.String,java.lang.String,models.DiskSnapshotList,com.azure.core.util.Context)` was added
* `preRestore(java.lang.String,java.lang.String,java.lang.String,models.DiskSnapshotList)` was added
* `preBackup(java.lang.String,java.lang.String,java.lang.String,models.VolumeNameList)` was added

#### `ElasticSanManager` was modified

* `resourceProviders()` was added

#### `models.VolumeGroup$Definition` was modified

* `withDeleteRetentionPolicy(models.DeleteRetentionPolicy)` was added

#### `models.VolumeGroup` was modified

* `deleteRetentionPolicy()` was added

## 1.2.0-beta.1 (2024-10-23)

- Azure Resource Manager ElasticSan client library for Java. This package contains Microsoft Azure SDK for ElasticSan Management SDK.  Package tag package-preview-2024-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AutoScaleProperties` was added

* `models.AutoScalePolicyEnforcement` was added

* `models.ScaleUpProperties` was added

#### `models.ElasticSanUpdate` was modified

* `autoScaleProperties()` was added
* `withAutoScaleProperties(models.AutoScaleProperties)` was added

#### `models.ElasticSan` was modified

* `autoScaleProperties()` was added

#### `models.ElasticSan$Definition` was modified

* `withAutoScaleProperties(models.AutoScaleProperties)` was added

#### `models.ElasticSan$Update` was modified

* `withAutoScaleProperties(models.AutoScaleProperties)` was added

## 1.1.0 (2024-09-25)

- Azure Resource Manager ElasticSan client library for Java. This package contains Microsoft Azure SDK for ElasticSan Management SDK.  Package tag package-2024-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.NetworkRuleSet` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ElasticSanUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SnapshotList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SnapshotCreationData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VolumeUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceCreationData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VolumeGroup$Update` was modified

* `withEnforceDataIntegrityCheckForIscsi(java.lang.Boolean)` was added

#### `models.SkuCapability` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KeyVaultProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkuInformationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VolumeGroupUpdate` was modified

* `withEnforceDataIntegrityCheckForIscsi(java.lang.Boolean)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `enforceDataIntegrityCheckForIscsi()` was added

#### `models.VolumeList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuLocationInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EncryptionIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ElasticSanList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IscsiTargetInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.VolumeGroup$Definition` was modified

* `withEnforceDataIntegrityCheckForIscsi(java.lang.Boolean)` was added

#### `models.ManagedByInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VolumeGroupList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Identity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VolumeGroup` was modified

* `enforceDataIntegrityCheckForIscsi()` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0 (2024-01-25)

- Azure Resource Manager ElasticSan client library for Java. This package contains Microsoft Azure SDK for ElasticSan Management SDK.  Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.3 (2023-10-23)

- Azure Resource Manager ElasticSan client library for Java. This package contains Microsoft Azure SDK for ElasticSan Management SDK.  Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.State` was removed

#### `models.SourceCreationData` was modified

* `withSourceUri(java.lang.String)` was removed
* `sourceUri()` was removed

#### `models.Action` was modified

* `valueOf(java.lang.String)` was removed
* `toString()` was removed
* `models.Action[] values()` -> `java.util.Collection values()`

#### `models.VirtualNetworkRule` was modified

* `state()` was removed

#### `models.Volumes` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.VolumeCreateOption` was modified

* `valueOf(java.lang.String)` was removed
* `toString()` was removed
* `models.VolumeCreateOption[] values()` -> `java.util.Collection values()`

### Features Added

* `models.EncryptionProperties` was added

* `models.SnapshotList` was added

* `models.IdentityType` was added

* `models.SnapshotCreationData` was added

* `models.Snapshot$Definition` was added

* `models.UserAssignedIdentity` was added

* `models.VolumeSnapshots` was added

* `models.KeyVaultProperties` was added

* `models.EncryptionIdentity` was added

* `models.PublicNetworkAccess` was added

* `models.XMsDeleteSnapshots` was added

* `models.XMsForceDelete` was added

* `models.ManagedByInfo` was added

* `models.Identity` was added

* `models.Snapshot$DefinitionStages` was added

* `models.Snapshot` was added

#### `models.ElasticSanUpdate` was modified

* `publicNetworkAccess()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.VolumeUpdate` was modified

* `withManagedBy(models.ManagedByInfo)` was added
* `managedBy()` was added

#### `models.SourceCreationData` was modified

* `withSourceId(java.lang.String)` was added
* `sourceId()` was added

#### `models.VolumeGroup$Update` was modified

* `withNetworkAcls(models.NetworkRuleSet)` was added
* `withIdentity(models.Identity)` was added
* `withEncryptionProperties(models.EncryptionProperties)` was added

#### `models.ElasticSan` was modified

* `publicNetworkAccess()` was added

#### `models.VolumeGroupUpdate` was modified

* `withIdentity(models.Identity)` was added
* `encryptionProperties()` was added
* `identity()` was added
* `withEncryptionProperties(models.EncryptionProperties)` was added

#### `models.Volume` was modified

* `managedBy()` was added
* `systemData()` was added
* `provisioningState()` was added

#### `models.Volumes` was modified

* `deleteByIdWithResponse(java.lang.String,models.XMsDeleteSnapshots,models.XMsForceDelete,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.XMsDeleteSnapshots,models.XMsForceDelete,com.azure.core.util.Context)` was added

#### `ElasticSanManager` was modified

* `volumeSnapshots()` was added

#### `models.Volume$Update` was modified

* `withManagedBy(models.ManagedByInfo)` was added

#### `models.Volume$Definition` was modified

* `withManagedBy(models.ManagedByInfo)` was added

#### `models.ElasticSan$Definition` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.ElasticSan$Update` was modified

* `withExtendedCapacitySizeTiB(java.lang.Long)` was added
* `withBaseSizeTiB(java.lang.Long)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.VolumeGroup$Definition` was modified

* `withEncryptionProperties(models.EncryptionProperties)` was added
* `withIdentity(models.Identity)` was added

#### `models.VolumeGroup` was modified

* `identity()` was added
* `encryptionProperties()` was added

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

