# Release History

## 1.1.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.3 (2026-03-04)

- Azure Resource Manager Cosmos DB for PostgreSql client library for Java. This package contains Microsoft Azure SDK for Cosmos DB for PostgreSql Management SDK. Azure Cosmos DB for PostgreSQL database service resource provider REST APIs. Package api-version 2023-03-02-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ClusterListResult` was removed

#### `models.ServerConfigurationListResult` was removed

#### `models.ClusterServerListResult` was removed

#### `models.OperationOrigin` was removed

#### `models.OperationListResult` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.RoleListResult` was removed

#### `models.ClusterConfigurationListResult` was removed

#### `models.FirewallRuleListResult` was removed

#### `models.PrivateLinkResourceListResult` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `validate()` was removed
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.SimplePrivateEndpointConnection` was modified

* `SimplePrivateEndpointConnection()` was changed to private access
* `withPrivateEndpoint(models.PrivateEndpointProperty)` was removed
* `withGroupIds(java.util.List)` was removed
* `validate()` was removed
* `withPrivateLinkServiceConnectionState(models.PrivateLinkServiceConnectionState)` was removed

#### `models.AuthConfig` was modified

* `validate()` was removed

#### `models.ClusterForUpdate` was modified

* `validate()` was removed

#### `CosmosDBForPostgreSqlManager` was modified

* `fluent.CosmosDBForPostgreSql serviceClient()` -> `fluent.CosmosDBforPostgreSqlManagementClient serviceClient()`

#### `models.DataEncryption` was modified

* `validate()` was removed

#### `models.MaintenanceWindow` was modified

* `validate()` was removed

#### `models.IdentityProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.NameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.Operation` was modified

* `models.OperationOrigin origin()` -> `models.Origin origin()`
* `properties()` was removed

#### `models.ServerProperties` was modified

* `validate()` was removed
* `models.ServerProperties withVCores(java.lang.Integer)` -> `models.ServerProperties withVCores(java.lang.Integer)`
* `models.ServerProperties withEnableHa(java.lang.Boolean)` -> `models.ServerProperties withEnableHa(java.lang.Boolean)`
* `models.ServerProperties withServerEdition(java.lang.String)` -> `models.ServerProperties withServerEdition(java.lang.String)`
* `models.ServerProperties withStorageQuotaInMb(java.lang.Integer)` -> `models.ServerProperties withStorageQuotaInMb(java.lang.Integer)`

#### `models.PromoteRequest` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.PrivateEndpointProperty` was modified

* `PrivateEndpointProperty()` was changed to private access
* `withId(java.lang.String)` was removed
* `validate()` was removed

#### `models.ServerNameItem` was modified

* `ServerNameItem()` was changed to private access
* `validate()` was removed
* `withName(java.lang.String)` was removed

#### `models.ServerRoleGroupConfiguration` was modified

* `ServerRoleGroupConfiguration()` was changed to private access
* `withRole(models.ServerRole)` was removed
* `validate()` was removed
* `withValue(java.lang.String)` was removed

### Features Added

* `models.Origin` was added

* `models.ActionType` was added

#### `models.Operation` was modified

* `actionType()` was added

## 1.1.0-beta.2 (2024-12-03)

- Azure Resource Manager CosmosDBForPostgreSql client library for Java. This package contains Microsoft Azure SDK for CosmosDBForPostgreSql Management SDK. Azure Cosmos DB for PostgreSQL database service resource provider REST APIs. Package tag package-preview-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

* `models.UserAssignedIdentity` was added

* `models.DataEncryption` was added

* `models.IdentityType` was added

* `models.PasswordEnabledEnum` was added

* `models.IdentityProperties` was added

* `models.DataEncryptionType` was added

* `models.AadEnabledEnum` was added

#### `models.SimplePrivateEndpointConnection` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.Cluster$Update` was modified

* `withIdentity(models.IdentityProperties)` was added

#### `models.ClusterForUpdate` was modified

* `identity()` was added
* `withIdentity(models.IdentityProperties)` was added

#### `models.Cluster$Definition` was modified

* `withIdentity(models.IdentityProperties)` was added
* `withDataEncryption(models.DataEncryption)` was added

#### `models.Cluster` was modified

* `identity()` was added
* `passwordEnabled()` was added
* `aadAuthEnabled()` was added
* `dataEncryption()` was added

## 1.1.0-beta.1 (2024-03-15)

- Azure Resource Manager CosmosDBForPostgreSql client library for Java. This package contains Microsoft Azure SDK for CosmosDBForPostgreSql Management SDK. Azure Cosmos DB for PostgreSQL database service resource provider REST APIs. Package tag package-preview-2023-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Clusters` was modified

* `promoteReadReplica(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Cluster` was modified

* `promoteReadReplica(com.azure.core.util.Context)` was removed

### Features Added

* `models.PrincipalType` was added

* `models.RoleType` was added

* `models.AuthConfig` was added

* `models.PasswordAuth` was added

* `models.PromoteRequest` was added

* `models.ActiveDirectoryAuth` was added

#### `models.Cluster$Definition` was modified

* `withAuthConfig(models.AuthConfig)` was added
* `withEnableGeoBackup(java.lang.Boolean)` was added
* `withDatabaseName(java.lang.String)` was added

#### `models.Clusters` was modified

* `promoteReadReplica(java.lang.String,java.lang.String,models.PromoteRequest,com.azure.core.util.Context)` was added

#### `models.Cluster` was modified

* `promoteReadReplica(models.PromoteRequest,com.azure.core.util.Context)` was added
* `enableGeoBackup()` was added
* `databaseName()` was added
* `authConfig()` was added

#### `models.Role$Definition` was modified

* `withPrincipalType(models.PrincipalType)` was added
* `withObjectId(java.lang.String)` was added
* `withTenantId(java.lang.String)` was added
* `withRoleType(models.RoleType)` was added

#### `models.Role` was modified

* `principalType()` was added
* `roleType()` was added
* `objectId()` was added
* `tenantId()` was added

## 1.0.0 (2023-09-22)

- Azure Resource Manager CosmosDBForPostgreSql client library for Java. This package contains Microsoft Azure SDK for CosmosDBForPostgreSql Management SDK. Azure Cosmos DB for PostgreSQL database service resource provider REST APIs. Package tag package-2022-11-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-06-13)

- Azure Resource Manager CosmosDBForPostgreSql client library for Java. This package contains Microsoft Azure SDK for CosmosDBForPostgreSql Management SDK. Azure Cosmos DB for PostgreSQL database service resource provider REST APIs. Package tag package-2022-11-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
