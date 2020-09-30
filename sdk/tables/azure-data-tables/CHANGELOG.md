# Release History

## 12.0.0-beta.2 (Unreleased)

### Changed

- The `getEntity` methods have gained the `select` query option to allow for more efficient existence checks for a table entity [#15289](https://github.com/Azure/azure-sdk-for-java/issues/15289)

### Fixed

- Can Not Create TableClientBuilder [#15294](https://github.com/Azure/azure-sdk-for-java/issues/15294)
- Missing module-info.java [#15296](https://github.com/Azure/azure-sdk-for-java/issues/15296)
- The `TableClient.updateEntity(entity)` method was mistakenly performing an upsert operation rather than an update
- The `TableAsyncClient.updateEntity(entity)` method always returned an empty result
- The non-functional `TableClient.listEntities(options, timeout)` method was removed

## 12.0.0-beta.1 (2020-09-10):

Version 12.0.0-beta.1 is a beta of our efforts in creating a client library that is developer-friendly, idiomatic to
the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### Features

- Support for both [Azure Table storage](https://docs.microsoft.com/azure/cosmos-db/table-storage-overview) and
  the [Cosmos DB Table API](https://docs.microsoft.com/azure/cosmos-db/table-introduction).
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Create, list, query, and delete tables.
- Insert, update, upsert, merge, list, query, and delete table entities.

### Known issues

- Entity merge operations using the Cosmos DB Table API are currently broken.
- When using the Cosmos DB Table API, if a client is rate-limited by the service, the client returns the error to the
  caller rather than automatically retrying the request after a delay.
- Upon failure, operations do not correctly throw a `TableStorageException` as documented, but instead currently throw
  the internal type `TableServiceErrorException`.
