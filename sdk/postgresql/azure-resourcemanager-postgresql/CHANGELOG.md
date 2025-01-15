# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2024-12-23)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.ServerPropertiesForRestore` was modified

* `createMode()` was added

#### `models.FirewallRule` was modified

* `resourceGroupName()` was added

#### `models.VirtualNetworkRule` was modified

* `resourceGroupName()` was added

#### `models.ServerPropertiesForReplica` was modified

* `createMode()` was added

#### `models.Database` was modified

* `resourceGroupName()` was added

#### `models.ServerPropertiesForCreate` was modified

* `createMode()` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

#### `PostgreSqlManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.ServerKey` was modified

* `resourceGroupName()` was added

#### `PostgreSqlManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Server` was modified

* `resourceGroupName()` was added

#### `models.Configuration` was modified

* `resourceGroupName()` was added

#### `models.ServerSecurityAlertPolicy` was modified

* `resourceGroupName()` was added

#### `models.ServerPropertiesForDefaultCreate` was modified

* `createMode()` was added

#### `models.ServerPropertiesForGeoRestore` was modified

* `createMode()` was added

## 1.0.2 (2022-01-24)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.1 (2021-07-12)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `PostgreSqlManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.Server` was modified

* `restart()` was added
* `restart(com.azure.core.util.Context)` was added

## 1.0.0 (2021-04-09)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ErrorAdditionalInfo` was removed

### New Feature

* `models.ServerSecurityAlertPolicyListResult` was added

#### `models.ServerSecurityAlertPolicies` was modified

* `listByServer(java.lang.String,java.lang.String)` was added
* `listByServer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2020-12-16)

- Azure Resource Manager PostgreSql client library for Java. This package contains Microsoft Azure SDK for PostgreSql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure PostgreSQL resources including servers, databases, firewall rules, VNET rules, security alert policies, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
