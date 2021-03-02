# Release History

## 1.0.0-beta.2 (2021-03-02)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ForceUnlinkParameters` was removed

* `models.LinkState` was removed

* `models.DatabasePropertiesGeoReplication` was removed

* `models.LinkedDatabase` was removed

#### `models.Database$Definition` was modified

* `withGeoReplication(models.DatabasePropertiesGeoReplication)` was removed

#### `models.Database$Update` was modified

* `withGeoReplication(models.DatabasePropertiesGeoReplication)` was removed

#### `models.DatabaseUpdate` was modified

* `withGeoReplication(models.DatabasePropertiesGeoReplication)` was removed
* `geoReplication()` was removed

#### `models.Database` was modified

* `geoReplication()` was removed
* `forceUnlink(models.ForceUnlinkParameters)` was removed
* `forceUnlink(models.ForceUnlinkParameters,com.azure.core.util.Context)` was removed

#### `models.Databases` was modified

* `forceUnlink(java.lang.String,java.lang.String,java.lang.String,models.ForceUnlinkParameters,com.azure.core.util.Context)` was removed
* `forceUnlink(java.lang.String,java.lang.String,java.lang.String,models.ForceUnlinkParameters)` was removed

## 1.0.0-beta.1 (2021-02-23)

- Azure Resource Manager RedisEnterprise client library for Java. This package contains Microsoft Azure SDK for RedisEnterprise Management SDK. REST API for managing Redis Enterprise resources in Azure. Package tag package-preview-2021-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
