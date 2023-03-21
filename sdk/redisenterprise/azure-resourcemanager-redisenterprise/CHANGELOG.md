# Release History

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
