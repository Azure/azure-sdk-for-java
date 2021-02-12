# Release History

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
