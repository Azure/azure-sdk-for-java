# Release History

## 2.1.0 (2025-10-15)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-2025-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Database` was modified

* `flush(models.FlushParameters)` was removed

#### `models.Databases` was modified

* `flush(java.lang.String,java.lang.String,java.lang.String,models.FlushParameters)` was removed

### Features Added

* `models.Kind` was added

* `models.ForceLinkParametersGeoReplication` was added

* `models.AccessKeysAuthentication` was added

* `models.PublicNetworkAccess` was added

* `models.AccessPolicyAssignment$DefinitionStages` was added

* `models.HighAvailability` was added

* `models.AccessPolicyAssignments` was added

* `models.AccessPolicyAssignment$Definition` was added

* `models.AccessPolicyAssignmentPropertiesUser` was added

* `models.AccessPolicyAssignment$UpdateStages` was added

* `models.RedundancyMode` was added

* `models.AccessPolicyAssignmentList` was added

* `models.ClusterCommonProperties` was added

* `models.AccessPolicyAssignment` was added

* `models.ForceLinkParameters` was added

* `models.SkuDetailsList` was added

* `models.DatabaseCommonProperties` was added

* `models.AccessPolicyAssignment$Update` was added

* `models.SkuDetails` was added

* `models.DeferUpgradeSetting` was added

#### `models.ManagedServiceIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterUpdate` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `highAvailability()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withHighAvailability(models.HighAvailability)` was added
* `publicNetworkAccess()` was added
* `redundancyMode()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForceUnlinkParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RedisEnterprises` was modified

* `listSkusForScalingWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listSkusForScaling(java.lang.String,java.lang.String)` was added

#### `models.PrivateLinkResourceListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseUpdate` was modified

* `deferUpgrade()` was added
* `accessKeysAuthentication()` was added
* `redisVersion()` was added
* `withDeferUpgrade(models.DeferUpgradeSetting)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withAccessKeysAuthentication(models.AccessKeysAuthentication)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Module` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster$Update` was modified

* `withHighAvailability(models.HighAvailability)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPropertiesEncryption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabasePropertiesGeoReplication` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegenerateKeyParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinkedDatabase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `RedisEnterpriseManager` was modified

* `accessPolicyAssignments()` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPropertiesEncryptionCustomerManagedKeyEncryptionKeyIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Database$Definition` was modified

* `withDeferUpgrade(models.DeferUpgradeSetting)` was added
* `withAccessKeysAuthentication(models.AccessKeysAuthentication)` was added

#### `models.ImportClusterParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster$Definition` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withHighAvailability(models.HighAvailability)` was added

#### `models.Database$Update` was modified

* `withClusteringPolicy(models.ClusteringPolicy)` was added
* `withAccessKeysAuthentication(models.AccessKeysAuthentication)` was added
* `withDeferUpgrade(models.DeferUpgradeSetting)` was added

#### `models.FlushParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Database` was modified

* `upgradeDBRedisVersion(com.azure.core.util.Context)` was added
* `flush()` was added
* `upgradeDBRedisVersion()` was added
* `deferUpgrade()` was added
* `forceLinkToReplicationGroup(models.ForceLinkParameters,com.azure.core.util.Context)` was added
* `accessKeysAuthentication()` was added
* `forceLinkToReplicationGroup(models.ForceLinkParameters)` was added
* `redisVersion()` was added
* `systemData()` was added

#### `models.ClusterList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExportClusterParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Persistence` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPropertiesEncryptionCustomerManagedKeyEncryption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Cluster` was modified

* `listSkusForScalingWithResponse(com.azure.core.util.Context)` was added
* `kind()` was added
* `highAvailability()` was added
* `redundancyMode()` was added
* `listSkusForScaling()` was added
* `publicNetworkAccess()` was added

#### `models.Databases` was modified

* `forceLinkToReplicationGroup(java.lang.String,java.lang.String,java.lang.String,models.ForceLinkParameters,com.azure.core.util.Context)` was added
* `forceLinkToReplicationGroup(java.lang.String,java.lang.String,java.lang.String,models.ForceLinkParameters)` was added
* `upgradeDBRedisVersion(java.lang.String,java.lang.String,java.lang.String)` was added
* `flush(java.lang.String,java.lang.String,java.lang.String)` was added
* `upgradeDBRedisVersion(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 2.1.0-beta.3 (2025-05-06)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-preview-2025-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ForceLinkParameters` was modified

* `linkedDatabases()` was removed
* `groupNickname()` was removed
* `withGroupNickname(java.lang.String)` was removed
* `withLinkedDatabases(java.util.List)` was removed

#### `models.Database` was modified

* `flush(models.FlushParameters)` was removed

#### `models.Databases` was modified

* `flush(java.lang.String,java.lang.String,java.lang.String,models.FlushParameters)` was removed

### Features Added

* `models.Kind` was added

* `models.ForceLinkParametersGeoReplication` was added

* `models.SkuDetailsList` was added

* `models.SkuDetails` was added

#### `models.RedisEnterprises` was modified

* `listSkusForScalingWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listSkusForScaling(java.lang.String,java.lang.String)` was added

#### `models.Database$Update` was modified

* `withClusteringPolicy(models.ClusteringPolicy)` was added

#### `models.ForceLinkParameters` was modified

* `geoReplication()` was added
* `withGeoReplication(models.ForceLinkParametersGeoReplication)` was added

#### `models.Database` was modified

* `flush()` was added

#### `models.Cluster` was modified

* `listSkusForScalingWithResponse(com.azure.core.util.Context)` was added
* `listSkusForScaling()` was added
* `kind()` was added

#### `models.Databases` was modified

* `flush(java.lang.String,java.lang.String,java.lang.String)` was added

## 2.1.0-beta.2 (2024-10-21)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-preview-2024-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AccessKeysAuthentication` was added

* `models.AccessPolicyAssignment$DefinitionStages` was added

* `models.HighAvailability` was added

* `models.AccessPolicyAssignments` was added

* `models.AccessPolicyAssignment$Definition` was added

* `models.AccessPolicyAssignmentPropertiesUser` was added

* `models.AccessPolicyAssignment$UpdateStages` was added

* `models.RedundancyMode` was added

* `models.AccessPolicyAssignmentList` was added

* `models.AccessPolicyAssignment` was added

* `models.AccessPolicyAssignment$Update` was added

#### `models.ManagedServiceIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterUpdate` was modified

* `withHighAvailability(models.HighAvailability)` was added
* `redundancyMode()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `highAvailability()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ForceUnlinkParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResourceListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseUpdate` was modified

* `withAccessKeysAuthentication(models.AccessKeysAuthentication)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `accessKeysAuthentication()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Module` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Cluster$Update` was modified

* `withHighAvailability(models.HighAvailability)` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Sku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPropertiesEncryption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatabasePropertiesGeoReplication` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegenerateKeyParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkedDatabase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `RedisEnterpriseManager` was modified

* `accessPolicyAssignments()` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPropertiesEncryptionCustomerManagedKeyEncryptionKeyIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Database$Definition` was modified

* `withAccessKeysAuthentication(models.AccessKeysAuthentication)` was added

#### `models.ImportClusterParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster$Definition` was modified

* `withHighAvailability(models.HighAvailability)` was added

#### `models.Database$Update` was modified

* `withAccessKeysAuthentication(models.AccessKeysAuthentication)` was added

#### `models.ForceLinkParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FlushParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Database` was modified

* `systemData()` was added
* `accessKeysAuthentication()` was added

#### `models.ClusterList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExportClusterParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Persistence` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPropertiesEncryptionCustomerManagedKeyEncryption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Cluster` was modified

* `highAvailability()` was added
* `redundancyMode()` was added

## 2.1.0-beta.1 (2024-05-20)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-preview-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ForceLinkParameters` was added

* `models.DeferUpgradeSetting` was added

#### `models.DatabaseUpdate` was modified

* `withDeferUpgrade(models.DeferUpgradeSetting)` was added
* `redisVersion()` was added
* `deferUpgrade()` was added

#### `models.Database$Definition` was modified

* `withDeferUpgrade(models.DeferUpgradeSetting)` was added

#### `models.Database$Update` was modified

* `withDeferUpgrade(models.DeferUpgradeSetting)` was added

#### `models.Database` was modified

* `deferUpgrade()` was added
* `redisVersion()` was added
* `upgradeDBRedisVersion(com.azure.core.util.Context)` was added
* `upgradeDBRedisVersion()` was added
* `forceLinkToReplicationGroup(models.ForceLinkParameters)` was added
* `forceLinkToReplicationGroup(models.ForceLinkParameters,com.azure.core.util.Context)` was added

#### `models.Databases` was modified

* `upgradeDBRedisVersion(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `forceLinkToReplicationGroup(java.lang.String,java.lang.String,java.lang.String,models.ForceLinkParameters)` was added
* `upgradeDBRedisVersion(java.lang.String,java.lang.String,java.lang.String)` was added
* `forceLinkToReplicationGroup(java.lang.String,java.lang.String,java.lang.String,models.ForceLinkParameters,com.azure.core.util.Context)` was added

## 2.0.0 (2024-03-14)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ImportClusterParameters` was modified

* `sasUri()` was removed
* `withSasUri(java.lang.String)` was removed

#### `models.Database$Update` was modified

* `withModules(java.util.List)` was removed
* `withClusteringPolicy(models.ClusteringPolicy)` was removed

#### `models.PrivateEndpointConnections` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

### Features Added

* `models.ManagedServiceIdentity` was added

* `models.ForceUnlinkParameters` was added

* `models.UserAssignedIdentity` was added

* `models.ClusterPropertiesEncryption` was added

* `models.DatabasePropertiesGeoReplication` was added

* `models.LinkedDatabase` was added

* `models.ClusterPropertiesEncryptionCustomerManagedKeyEncryptionKeyIdentity` was added

* `models.LinkState` was added

* `models.ManagedServiceIdentityType` was added

* `models.FlushParameters` was added

* `models.CmkIdentityType` was added

* `models.ClusterPropertiesEncryptionCustomerManagedKeyEncryption` was added

#### `models.ClusterUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added
* `encryption()` was added
* `withEncryption(models.ClusterPropertiesEncryption)` was added

#### `models.DatabaseUpdate` was modified

* `withGeoReplication(models.DatabasePropertiesGeoReplication)` was added
* `geoReplication()` was added

#### `models.Cluster$Update` was modified

* `withEncryption(models.ClusterPropertiesEncryption)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `RedisEnterpriseManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Database$Definition` was modified

* `withGeoReplication(models.DatabasePropertiesGeoReplication)` was added

#### `models.ImportClusterParameters` was modified

* `sasUris()` was added
* `withSasUris(java.util.List)` was added

#### `models.Cluster$Definition` was modified

* `withEncryption(models.ClusterPropertiesEncryption)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `RedisEnterpriseManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.PrivateEndpointConnections` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Database` was modified

* `flush(models.FlushParameters,com.azure.core.util.Context)` was added
* `geoReplication()` was added
* `forceUnlink(models.ForceUnlinkParameters,com.azure.core.util.Context)` was added
* `forceUnlink(models.ForceUnlinkParameters)` was added
* `flush(models.FlushParameters)` was added
* `resourceGroupName()` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

#### `models.Cluster` was modified

* `encryption()` was added
* `resourceGroupName()` was added
* `identity()` was added

#### `models.Databases` was modified

* `flush(java.lang.String,java.lang.String,java.lang.String,models.FlushParameters,com.azure.core.util.Context)` was added
* `forceUnlink(java.lang.String,java.lang.String,java.lang.String,models.ForceUnlinkParameters)` was added
* `flush(java.lang.String,java.lang.String,java.lang.String,models.FlushParameters)` was added
* `forceUnlink(java.lang.String,java.lang.String,java.lang.String,models.ForceUnlinkParameters,com.azure.core.util.Context)` was added

## 1.1.0-beta.3 (2023-03-21)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-preview-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Name` was removed

#### `models.SkuDetail` was modified

* `withName(models.Name)` was removed
* `models.Name name()` -> `models.SkuName name()`

### Features Added

#### `models.SkuDetail` was modified

* `withName(models.SkuName)` was added

## 1.1.0-beta.2 (2023-03-13)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-preview-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ManagedServiceIdentity` was added

* `models.Name` was added

* `models.UserAssignedIdentity` was added

* `models.SkuDetail` was added

* `models.LocationInfo` was added

* `models.RegionSkuDetail` was added

* `models.Capability` was added

* `models.ClusterPropertiesEncryption` was added

* `models.ClusterPropertiesEncryptionCustomerManagedKeyEncryptionKeyIdentity` was added

* `models.RegionSkuDetails` was added

* `models.ManagedServiceIdentityType` was added

* `models.FlushParameters` was added

* `models.CmkIdentityType` was added

* `models.ClusterPropertiesEncryptionCustomerManagedKeyEncryption` was added

* `models.Skus` was added

#### `models.ClusterUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withEncryption(models.ClusterPropertiesEncryption)` was added
* `encryption()` was added
* `identity()` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

#### `models.Cluster$Update` was modified

* `withEncryption(models.ClusterPropertiesEncryption)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `RedisEnterpriseManager` was modified

* `skus()` was added

#### `models.Cluster$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withEncryption(models.ClusterPropertiesEncryption)` was added

#### `models.Database` was modified

* `flush(models.FlushParameters,com.azure.core.util.Context)` was added
* `flush(models.FlushParameters)` was added
* `resourceGroupName()` was added
* `systemData()` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added
* `systemData()` was added

#### `models.Cluster` was modified

* `identity()` was added
* `resourceGroupName()` was added
* `systemData()` was added
* `encryption()` was added

#### `models.Databases` was modified

* `flush(java.lang.String,java.lang.String,java.lang.String,models.FlushParameters,com.azure.core.util.Context)` was added
* `flush(java.lang.String,java.lang.String,java.lang.String,models.FlushParameters)` was added

## 1.1.0-beta.1 (2022-04-19)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-2022-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ImportClusterParameters` was modified

* `sasUri()` was removed
* `withSasUri(java.lang.String)` was removed

#### `models.Database$Update` was modified

* `withModules(java.util.List)` was removed
* `withClusteringPolicy(models.ClusteringPolicy)` was removed

### Features Added

* `models.ForceUnlinkParameters` was added

* `models.LinkState` was added

* `models.DatabasePropertiesGeoReplication` was added

* `models.LinkedDatabase` was added

#### `models.Database$Definition` was modified

* `withGeoReplication(models.DatabasePropertiesGeoReplication)` was added

#### `models.ImportClusterParameters` was modified

* `sasUris()` was added
* `withSasUris(java.util.List)` was added

#### `RedisEnterpriseManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.DatabaseUpdate` was modified

* `geoReplication()` was added
* `withGeoReplication(models.DatabasePropertiesGeoReplication)` was added

#### `models.Database` was modified

* `forceUnlink(models.ForceUnlinkParameters,com.azure.core.util.Context)` was added
* `forceUnlink(models.ForceUnlinkParameters)` was added
* `geoReplication()` was added

#### `models.Databases` was modified

* `forceUnlink(java.lang.String,java.lang.String,java.lang.String,models.ForceUnlinkParameters)` was added
* `forceUnlink(java.lang.String,java.lang.String,java.lang.String,models.ForceUnlinkParameters,com.azure.core.util.Context)` was added

#### `RedisEnterpriseManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0 (2021-04-21)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ErrorAdditionalInfo` was removed

## 1.0.0-beta.2 (2021-03-02)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2021-02-23)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-preview-2021-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
