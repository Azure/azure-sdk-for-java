# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
