# Release History

## 12.4.3 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 12.4.2 (2024-06-13)

### Bugs Fixed

- Fixed [the bug](https://github.com/Azure/azure-sdk-for-java/pull/40392) where the deserialization of TableEntity was inconsistent between `ListEntities` and `getEntity` methods.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.

## 12.4.1 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.

## 12.4.0 (2024-04-12)

### Breaking Changes

- Removed `jackson-dataformat-xml` as a dependency.

### Bugs Fixed

- Fixed the issue where `createTableIfNotExists` utilized improper logic for checking for 409 status code.

### Other Changes

- Migrating from Jackson to stream-style serialization.

#### Dependency Updates

- Introducing `azure-xml` version `1.0.0` as a dependency.
- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.

## 12.4.0-beta.1 (2024-03-13)

### Breaking Changes

- Removed `jackson-dataformat-xml` as a dependency.

### Other Changes

- Migrating from Jackson to stream-style serialization.

#### Dependency Updates

- Introducing `azure-xml` version `1.0.0-beta.3` as a beta dependency.

## 12.3.20 (2024-03-07)

### Bugs Fixed
- Removed the 409 status-code error-level log message "TableAlreadyExists" that was printed when calling `TableServiceClient.createTableIfNotExists()` on a table that already exists.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.

## 12.3.19 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.


## 12.3.18 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 12.3.17 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.


## 12.3.16 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.
- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.


## 12.3.15 (2023-09-18)

### Bugs Fixed
- Fixed the issue with `TableClient` and `TableAsyncClient` where `deleteEntity` did not work on entities with empty primary keys.[(33390)](https://github.com/Azure/azure-sdk-for-java/issues/36690)
- Fixed the issue with `TableClient` and `TableAsyncClient` where `getEntity` did not work on entities with empty primary keys.

### Other Changes
- Migrate test recordings to assets repo

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.
- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.

## 12.3.14 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 12.3.13 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.
- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.


## 12.3.12 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.
- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
 
## 12.3.11 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 12.3.10 (2023-04-17)

### Bugs Fixed

- Fixed bug that disallowed empty strings as partition and row keys
- Fixed handling of paging headers when Table Storage returned a `x-ms-continuation-NextPartitionKey` but no `x-ms-continuation-NextRowKey`. This was causing the subsequent pages are not fetched.

### Other Changes

- Test proxy migration

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.

## 12.3.9 (2023-03-08)

### Other Changes

- Removed Reactor from synchronous clients

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.

## 12.3.8 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.

## 12.3.7 (2023-01-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` dependency to `1.35.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.8`.

## 12.3.6 (2022-11-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` dependency to `1.34.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.7`.

## 12.3.5 (2022-10-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` dependency to `1.33.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.6`.
- Utilizes `jackson-dataformat-xml` version `2.13.4`

## 12.3.4 (2022-09-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.32.0`
- Upgraded `azure-core-http-netty` dependency to `1.12.5`

## 12.3.3 (2022-08-15)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.31.0`
- Upgraded `azure-core-http-netty` dependency to `1.12.4`

## 12.3.2 (2022-07-07)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.30.0`
- Upgraded `azure-core-http-netty` dependency to `1.12.3`

## 12.3.1 (2022-06-10)

### Other Changes
- Added small performance improvements to the authentication flow.

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.29.1`
- Upgraded `azure-core-http-netty` dependency to `1.12.2`

## 12.3.0 (2022-05-11)

### Features Added
- TenantId can now be discovered through the service OAuth challenge response, when using a `TokenCredential` for authorization against Storage Table Service endpoints with version `2020_12_06`.
- Added method `enableTenantDiscovery()` to `TableClientBuilder` and `TableServiceClientBuilder`. If enabled, the resulting client will attempt an initial unauthorized request to the service to prompt an OAuth challenge containing the tenantId of the resource. This tenantId will then be used by the `TokenCredential`.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.28.0`
- Upgraded `azure-core-http-netty` dependency to `1.12.0`

## 12.2.2 (2022-04-06)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.27.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.9`

## 12.2.1 (2022-03-11)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.26.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.8`

## 12.2.0 (2022-02-10)

### Features added
- Implemented new traits (micro-interfaces) in `TableClientBuilder` and `TableServiceClientBuilder`. This makes the experience of using client builders more consistent across libraries in the Azure SDK for Java.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.25.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.7`

## 12.1.5 (2022-01-11)

### Bugs fixed
- Fixed issue that made it so single quotes in entity names were not properly escaped according to OData standards. [[25066]](https://github.com/Azure/azure-sdk-for-java/pull/25066)

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.24.1`
- Upgraded `azure-core-http-netty` dependency to `1.11.6`

## 12.1.4 (2021-11-19)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.22.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.2`

## 12.1.3 (2021-10-06)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.21.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.1`

## 12.1.2 (2021-09-09)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.20.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.0`

## 12.1.1 (2021-08-13)

### Bugs Fixed
- Fixed an issue that made getting entities from a Cosmos endpoint fail as it does not return a `Timestamp@odata.type` property in a `TableEntity` alongside the `Timestamp` property, like Storage endpoint do. This is apparently intended behavior, so we now always convert `Timestamp` to `OffsetDateTime` as it is a reserved property name and will always be provided by the service.
- Updated clients to properly map an internal HTTP exception to the public type `TableServiceException` in operations such as `getAccessPolicies()`, `getProperties()`, `setProperties()` and `getStatistics()`, including their `withResponse` variants.
- Fixed batch operations to properly log exceptions other than `TableTransactionFailedException`.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.19.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.2`

## 12.1.0 (2021-07-08)

### Features Added
- Added support for Azure Active Directory (AAD) authorization to `TableServiceClient` and `TableClient`. This enables the use of `TokenCredential` credentials in client builders. Note: Only Azure Storage API endpoints currently support AAD authorization.

### Bugs fixed
- Fixed issue where HTTP headers set in a `ClientOptions` object passed to a client builder would not be set on a client instantiated by said builder.
- Fixed an issue where a `connectionString` with an account name and key would override a `sasToken`'s authentication settings in client builders.
- Fixed an issue that made `TableClient.listEntities()` and `TableServiceClient.listTables()` throw a `ClassCastException` when passing a non-null value for `timeout`.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.18.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.1`

## 12.0.0 (2021-06-11)

### Bug fixes
- Fixed issue where clients builders would not throw when calling `buildClient()` or `buildAsyncClient()` if no `endpoint` had been set.
- Fixed issue where `TableClient`'s and `TableClientAsync`'s `submitTransaction()` and `submitTransactionWithResponse()` could not be called from inside a Reactor thread (e.g. calling it inside a chain of reactive operations, such as `myOtherOperation.then(result -> client.submitTransaction(transactionActions)`).
- Fixed issue that would make an exception be raised when calling `submitTransaction()` if Java's SecurityManager was enabled and no `ReflectPermission` had been granted.

### New Features

- Introduced the following classes:
    - `TableAccessPolicies`
    - `TableTransactionAction`
    - `TableTransactionActionType`
    - `TableTransactionFailedException`
    - `TableTransactionResult`
- Added support for generating SAS tokens at the Account and Table Service level in all clients. Introduced the following related classes:
    - `TableAccountSasPermission`
    - `TableAccountSasResourceType`
    - `TableAccountSasService`
    - `TableAccountSasSignatureValues`
    - `TableSasIpRange`
    - `TableSasPermission`
    - `TableSasSignatureValues`
- Added the following methods to `TableClient`, `TableAsyncClient`:
    - `listAccessPolicies()`
    - `setAccessPolicies()`
    - `setAccessPoliciesWithResponse()`
    - `generateSasToken()`
- Added the following methods to `TableServiceClient`, `TableServiceAsyncClient`:
    - `getProperties()`
    - `getPropertiesWithResponse()`
    - `setProperties()`
    - `setPropertiesWithResponse()`
    - `getStatistics()`
    - `getStatisticsWithResponse()`
    - `generateAccountSasToken()`

### Breaking Changes

- Removed the `TableBatch` and `TableAsyncBatch` types, as well as the methods `TableAsyncClient.createBatch()` and `TableClient.createBatch()`. In their place, batch operations can now be submitted via the following methods:
    - `TableAsyncClient.submitTransaction(List<TableTransactionAction> transactionalBatch)`
    - `TableAsyncClient.submitTransactionWithResponse(List<TableTransactionAction> transactionalBatch)`
    - `TableClient.submitTransaction(List<TableTransactionAction> transactionalBatch)`
    - `TableClient.submitTransactionWithResponse(List<TableTransactionAction> transactionalBatch, Duration timeout, Context context)`
- Renamed `BatchOperationResponse` to `TableTransactionActionResponse`
- `deleteEntity()` variants in `TableClient` and `TableAsyncClient` now accept an `ifUnchanged` flag instead of an `eTag` parameter for conditional operations. When said flag is set to `true`, the ETag of a given `TableEntity` will be matched with the ETag of the entity in the Table service.
- Replaced `deleteEntityWithResponse(String partitionKey, String rowKey, String eTag)` with `deleteEntityWithResponse(TableEntity entity, boolean ifUnchanged)` in `TableAsyncClient`.
- Replaced `deleteEntityWithResponse(String partitionKey, String rowKey, String eTag, Duration timeout, Context context)` with `deleteEntityWithResponse(TableEntity entity, boolean ifUnchanged, Duration timeout, Context context)` in `TableClient`.
- Removed remaining public APIs supporting the use of `TableEntity` subclasses from `TableAsyncClient`.
- Removed the following method overloads from `TableClient` and `TableAsyncClient`:
    - `upsertEntity(TableEntity entity, TableEntityUpdateMode updateMode)`
    - `updateEntity(TableEntity entity, TableEntityUpdateMode updateMode,
      boolean ifUnchanged)`
    - `getEntity(String partitionKey, String rowKey, List<String> select)`
- Client builders now also throw an `IllegalStateException` when calling `buildClient()` and `buildAsyncClient()` if multiple forms of authentication are provided, with the exception of `sasToken` + `connectionString`; or if `endpoint` and/or `sasToken` are set alongside a `connectionString` and the endpoint and/or SAS token in the latter are different than the former, respectively.
- The following methods and their `WithResponse` variants in `TableClient` and `TableAsyncClient` now throw an `IllegalArgumentException` instead of an `IllegalStateException` when an empty `TableEntity` is provided:
    - `createEntity()`
    - `upsertEntity()`
    - `updateEntity()`
    - `deleteEntity()`

## 12.0.0-beta.7 (2021-05-15)

### New Features

- Added `getAccessPolicy()` and `setAccessPolicy()` to `TableClient` and `TableAsyncClient`.
- Added `getProperties()`, `setProperties()` and `getStatistics()` to `TableServiceClient` and `TableServiceAsyncClient`.
- Added the following models:
    - `TableAccessPolicy`
    - `TableServiceCorsRule`
    - `TableServiceGeoReplication`
    - `TableServiceGeoReplicationStatus`
    - `TableServiceLogging`
    - `TableServiceMetrics`
    - `TableServiceProperties`
    - `TableServiceRetentionPolicy`
    - `TableServiceStatistics`
    - `TableSignedIdentifier`

### Breaking Changes

- Renamed `create()` and `delete()` methods to `createTable()` and `deleteTable()` on `TableClient` and `TableAsyncClient`. Also made `createTable()` and its variants return a `TableItem`.
- Removed `deleteEntity(String partitionKey, String rowKey, String eTag)` and added `deleteEntity(TableEntity tableEntity)` in both `TableClient` and `TableAsyncClient`.
- Made it so that when deleting a table or entity that does not exist, the resulting `404` error gets swallowed instead of thrown.
- Removed public APIs supporting the use of `TableEntity` subclasses.
- Made the following classes `final`:
    - `TableEntity`
    - `TableItem`
    - `TableClient`
    - `TableServiceClient`
    - `TableServiceAsyncClient`
    - `TableClientBuilder`
    - `TableServiceClientBuilder`.
- Removed method overloads that used `timeout`, except in the maximal overload for a method (the `withResponse` variant).
- Ensured that all timeout usages are client-side and not server-side.
- Made `createTable()` and `createTableIfNotExists()` in `TableServiceClient` and `TableServiceAsyncClient` return a `TableClient` and `TableAsyncClient` respectively.
- Made select in `ListEntitiesOptions` a `List` of `Strings` instead of a single `String`. Did the same for select in `getEntity()` and `getEntityWithResponse()` in `TableClient` and `TableAsyncClient`.
- Replaced `retryOptions(RequestRetryOptions)` with `retryPolicy(RetryPolicy)` in `TableClientBuilder` and `TableServiceClientBuilder`.
- Removed `TableSharedKeyCredential` in favor of using Azure Core's `AzureNamedKeyCredential`.
- Replaced `TableSharedKeyCredentialPolicy` with `AzureNamedKeyCredentialPolicy`.
- Renamed `UpdateMode` to `TableEntityUpdateMode`.
- Renamed `TablesServiceVersion` to `TableServiceVersion`.
- Renamed `getTableUrl()` and `getApiVersion()` to `getTableEndpoint()` and `getServiceVersion()` respectively, in `TableClient` and `TableAsyncClient`.
- Renamed `getServiceUrl()` and `getApiVersion()` to `getServiceEndpoint()` and `getServiceVersion()` respectively, in `TableClient` and `TableAsyncClient`.
- Renamed `addProperties()` to `setProperties()` in `TableEntity`. Also made `setProperties()` replace the contents of properties map with those of the argument, instead of adding them to the existing properties.
- Removed dependency on `azure-storage-common` and added direct dependency on `azure-core-http-netty`.

### Bug Fixes

- Merge operations no longer fail for Cosmos table endpoints.
- Fixed issue with `TablesJacksonSerializer` where it could not handle HTTP responses with empty bodies.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.16.0`.

## 12.0.0-beta.6 (2021-04-07)

### Breaking Changes

- All clients and batch classes now throw a public `TableServiceErrorException` instead of the one in the implementation package.
- All operation error details (`errorCode`, `languageCode`, `errorMessage`) are now contained in the public `TableServiceError`, as opposed to the implementation `TableServiceError` that contains a `TableServiceErrorOdataError` containing a `TableServiceErrorOdataErrorMessage`.
- `TableClientBuilder` fluent setters now throw an `IllegalArgumentException` instead of `NullPointerException` when given invalid arguments.

### Dependency Updates
- Updated dependency version of `azure-core` to 1.15.0.
- Updated dependency version of `azure-storage-common` to 12.10.1.

## 12.0.0-beta.5 (2021-03-10)

### New Features

- Added support to specify whether or not a pipeline policy should be added per call or per retry.
- Added support for passing Azure Core's `ClientOptions` to client builders.

### Dependency Updates

- Updated dependency version of `azure-core` to 1.14.0.

## 12.0.0-beta.4 (2021-02-11)

### Dependency Updates

- Updated dependency version of `azure-core` to 1.13.0.
- Updated dependency version of `azure-storage-common` to 12.10.0.

## 12.0.0-beta.3 (2020-11-12)

### New Features

- Developers can now perform multiple insert, update, or delete entity operations as part of a transactional batch. For
  more information on transactional batching with Azure Tables, see
  [Performing entity group transactions](https://docs.microsoft.com/rest/api/storageservices/performing-entity-group-transactions).
  Performing query operations as part of a transactional batch is not currently supported.
  [#15091](https://github.com/Azure/azure-sdk-for-java/issues/15901)

### Key Bug Fixes

- The table client returned from a service client's `getTableClient(tableName)` method was incorrectly configured,
  causing operations to fail. [#16292](https://github.com/Azure/azure-sdk-for-java/issues/16292)
- Calling `getApiVersion()` on any client no longer causes an exception.
- Passing a `TokenCredential` to a client builder mistakenly assumed it was always a shared key credential.
- Client methods that accept a `timeout` and/or `context` parameter will use default values if either parameter is set
  to `null`. [#16386](https://github.com/Azure/azure-sdk-for-java/issues/16386)
- Methods that perform upsert entity operations were mistakenly performing update operations instead.

### Dependency Updates

- Updated dependency version of `azure-core` to 1.10.0.
- Updated dependency version of `azure-storage-common` to 12.9.0.

## 12.0.0-beta.2 (2020-10-06)

### New Features

- Developers can now subclass `TableEntity` and decorate the subclass with properties, rather than adding properties
  manually by calling `addProperty()`. Client methods that perform read operations now accept an additional parameter
  `resultType` to return the result of the read operation as the specified type instead of always returning
  `TableEntity` instances. Client methods that perform write operations accept subclasses of `TableEntity` in addition
  to instances of the base class itself. [#13692](https://github.com/azure/azure-sdk-for-java/issues/13692)
- The `getEntity` methods have gained the `select` query option to allow for more efficient existence checks for a table
  entity. [#15289](https://github.com/Azure/azure-sdk-for-java/issues/15289)

### Breaking Changes

- The non-functional `TableClient.listEntities(options, timeout)` method was removed.

### Key Bug Fixes

- TableClientBuilder's constructor was mistakenly hidden from the public API.
  [#15294](https://github.com/Azure/azure-sdk-for-java/issues/15294)
- The library was missing a module-info.java. [#15296](https://github.com/Azure/azure-sdk-for-java/issues/15296)
- The `TableClient.updateEntity(entity)` method was mistakenly performing an upsert operation rather than an update.
- The `TableAsyncClient.updateEntity(entity)` method always returned an empty result.

### Other Changes

- The JavaDoc API documentation has been significantly improved
  [#13684](https://github.com/Azure/azure-sdk-for-java/issues/13684)

### Dependency Updates

- Updated dependency version of `azure-core` to 1.9.0.

## 12.0.0-beta.1 (2020-09-10)

Version 12.0.0-beta.1 is a beta of our efforts in creating a client library that is developer-friendly, idiomatic to
the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### New Features

- Support for both [Azure Table storage](https://docs.microsoft.com/azure/cosmos-db/table-storage-overview) and
  the [Cosmos DB Table API](https://docs.microsoft.com/azure/cosmos-db/table-introduction).
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Create, list, query, and delete tables.
- Insert, update, upsert, merge, list, query, and delete table entities.

### Known Issues

- Entity merge operations using the Cosmos DB Table API are currently broken.
- When using the Cosmos DB Table API, if a client is rate-limited by the service, the client returns the error to the
  caller rather than automatically retrying the request after a delay.
- Upon failure, operations do not correctly throw a `TableStorageException` as documented, but instead currently throw
  the internal type `TableServiceErrorException`.
