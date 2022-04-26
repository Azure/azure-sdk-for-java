# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
