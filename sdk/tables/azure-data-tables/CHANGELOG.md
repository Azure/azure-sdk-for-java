# Release History

## 12.0.0-beta.3 (Unreleased)


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
