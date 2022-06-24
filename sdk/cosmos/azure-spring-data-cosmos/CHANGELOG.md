## Release History

### 3.23.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes
* Performance improvement for case insensitive queries - See [PR 29597](https://github.com/Azure/azure-sdk-for-java/pull/29597)

### 3.22.0 (2022-06-08)
#### Features Added
* Exposed `maxDegreeOfParallelism` feature from CosmosQueryRequestOptions through application.properties flag - See [PR 28756](https://github.com/Azure/azure-sdk-for-java/pull/28756)

#### Other Changes
* Updated `azure-cosmos` to version `4.31.0`.

### 3.21.0 (2022-05-20)
#### Other Changes
* Updated `azure-cosmos` to version `4.30.0`.
* Updated `Spring Boot` to version `2.6.7`.

### 3.20.0 (2022-04-22)
#### Other Changes
* Updated `azure-cosmos` to version `4.29.0`.
* Updated `Spring Boot` to version `2.6.6`.

### 3.19.1 (2022-04-08)
#### Other Changes
* Updated `azure-cosmos` to version `4.28.1`.

### 3.19.0 (2022-03-10)
#### Features Added
* Updated `Spring Boot` to version `2.6.3`.
* Updated `azure-cosmos` to version `4.27.0`.
* Added support for container `UniqueKey` policies - See [PR 27270](https://github.com/Azure/azure-sdk-for-java/pull/27270)

#### Bugs Fixed
* Fixed an issue with `userAgent` in `azure-spring-data-cosmos` being overridden by other spring modules - See [PR 27311](https://github.com/Azure/azure-sdk-for-java/pull/27311)

### 3.18.0 (2022-02-11)
#### Features Added
* Updated `azure-cosmos` to version `4.26.0`.

### 3.17.0 (2022-01-14)
#### Features Added
* Updated `Spring Boot` to version `2.6.2`.
* Updated `azure-cosmos` to version `4.25.0`.

### 3.16.0 (2021-12-21)
#### Features Added
* Updated `azure-cosmos` to version `4.24.0`.

### 3.15.0 (2021-12-10)
#### Features Added
* Updated `Spring Boot` to version `2.6.1`.
* Updated `azure-cosmos` to version `4.23.0`.

#### Bugs Fixed
* Added support for capturing CosmosException diagnostics on `CosmosRepository` and `ReactiveRepository` APIs.
* Solved `UnsatisfiedDependencyException` when upgrading to spring boot 2.6.1 by removing `CosmosMappingContextLookup`.

### 3.14.0 (2021-11-12)
#### Features Added
* Updated `azure-cosmos` to version `4.21.0`.

#### Bugs Fixed
* Fixed issue with spring onLoad event not firing for paged queries.
* Exceptions thrown from spring application event listeners for onLoad events will now propagate up the call stack.

### 3.13.1 (2021-10-27)
#### Bugs Fixed
* Fixed support for slice query in annotated `@Query` queries.
* Fixed issue with preserving priority for closed queries.
* Updated `azure-cosmos` to version `4.20.1`.

### 3.13.0 (2021-10-14)
#### New Features
* Added support for setting throughput on database creation.
* Pagination improvement with slice query API.
* Updated `azure-cosmos` to version `4.20.0`.

### 3.12.0 (2021-09-24)
#### New Features
* Added support for UUID id type to spring data cosmos SDK.
* Updated `azure-cosmos` to version 4.19.1.

### 3.11.0 (2021-09-09)
#### New Features
* Added Spring `ApplicationEventListener` support.
* Updated `Spring Boot` to version 2.5.4.
* Updated `azure-cosmos` to version 4.19.0.

#### Bugs Fixed
* Fixed spring data cosmos `query plan` caching.
* Fixed query parameter name generation for nested properties containing space.

### 3.11.0-beta.1 (2021-09-02)
#### New Features
* Updated `azure-cosmos` to version 4.19.0-beta.1.

#### Bugs Fixed
* Fixed spring data cosmos `query plan` caching.

### 3.10.0 (2021-08-16)
> [!IMPORTANT]
> We strongly recommend our customers to use version 3.10.0 and above.
#### New Features
* Updated `Spring Boot` to version 2.5.3.
* Updated `azure-cosmos` to version 4.18.0.

### 3.9.0 (2021-07-08)
#### New Features
* Added `PartitionKey` support to spring data single partition queries.
* Updated `Spring Boot` to version 2.5.2.
* Updated `azure-cosmos` to version 4.17.0.

### 3.8.0 (2021-06-11)
#### New Features
* Updated `Spring Boot` to version 2.5.0.
* Updated `azure-cosmos` to version 4.16.0.
* Added Autoscale RU support feature.
* Added support for `countBy*` methods on `Repository` and `@Query` annotation.

#### Bugs Fixed
* Fixed an issue with `MappingCosmosConverter` handling query with value types.
* Fixed an issue with `CosmosIndexingPolicy` getting reset on application bootup.

### 3.7.0 (2021-05-12)
#### New Features
* Updated Spring Boot to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/).
* Updated Spring Cloud to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/).
* Added `slice` support for queries that do not require page count.
* Updated `azure-cosmos` to version 4.15.0.

### 3.6.0 (2021-04-06)
#### New Features
* Updated `azure-cosmos` to version 4.14.0.

### 3.5.1 (2021-03-24)
#### Bugs Fixed
* Updated `azure-cosmos` to hotfix version 4.13.1.

### 3.5.0 (2021-03-11)
> [!IMPORTANT] 
> This release supports spring boot 2.4.3 and above.
#### New Features
* Updated `spring-boot` to major version 2.4.3.
* Updated `spring-core` to major version 5.3.4.
* Updated `spring-data-commons` to major version 2.4.5.
* Updated `azure-cosmos` version to 4.13.0.
* Added support for `org.springframework.data.domain.Persistable` entity type.
* Added support to log SQL Queries.
* Added support for `Pageable` and `Sort` for `@query` annotated queries.

#### Bugs Fixed
* Fixed issue when using automatic id generation with the auditable framework.
* Fixed query deserialization issue with `@query` annotated queries.

### 3.4.0 (2021-02-09)
#### New Features
* Updated `azure-cosmos` version to 4.12.0.

#### Bugs Fixed
* Fixed `@EnableAuditing` for Java 11 modules.

### 3.3.0 (2021-01-15)
#### New Features
* Support for composite indexes in `CosmosIndexPolicy` annotation.
* Support for changes in `CosmosIndexPolicy` annotation.
* Updated azure-cosmos version to 4.11.0.

#### Bugs Fixed
* Fixed query generation with sort and limit.

### 3.2.0 (2020-12-11)
#### New Features
* Updated Spring Data Commons version to 2.3.5.RELEASE.
* Updated Spring Core version to 5.2.10.RELEASE.

#### Bugs Fixed
* Fixed publishing of `spring.factories` file with released jar.
* Fixed repository query with repeated parameters. 

### 3.1.0 (2020-10-21)
#### New Features
* Added support for `ARRAY_CONTAINS` `CriteriaType`.
* Updated azure-cosmos version to 4.7.1.

#### Bugs Fixed
* Fixed an issue where annotated queries do not pick the annotated container name.

### 3.0.0 (2020-09-30)
#### New Features
* Updated azure-cosmos dependency to `4.6.0`

### 3.0.0-beta.2 (2020-09-17)
#### New Features
* Updated artifact id to `azure-spring-data-cosmos`.
* Updated azure-cosmos dependency to `4.5.0`.
* `Query Annotation` support for native queries.
* Support for Java 11.
* Added support for Nested Partition Key by exposing `partitionKeyPath` field in `@Container` annotation.
* Added support for `limit` query type allowing `top` and `first` to be used when defining repository APIs.
#### Bugs Fixed
* Fixed nested partition key bug when used with `@GeneratedValue` annotation.

### 3.0.0-beta.1 (2020-08-17)
#### New Features
* Updated group id to `com.azure`.
* Updated artifact id to `azure-spring-data-cosmos-core`.
* Updated azure-cosmos SDK dependency to `4.3.2-beta.2`.
* Support for auditing entities - automatic management of createdBy, createdDate, lastModifiedBy and lastModifiedDate annotated fields.
* `@GeneratedValue` annotation support for automatic id generation for id fields of `String` type.
* Multi-database configuration support for single cosmos account with multiple databases and multiple cosmos accounts with multiple databases.
* Support for `@Version` annotation on any string field.
* Updated sync APIs return types to `Iterable` types instead of `List`.
* Exposed `CosmosClientBuilder` from Cosmos SDK as spring bean to `@Configuration` class.
* Updated `CosmosConfig` to contain query metrics and response diagnostics processor implementation.
* Support for returning `Optional` data type for single result queries.
#### Renames
* `CosmosDbFactory` to `CosmosFactory`.
* `CosmosDBConfig` to `CosmosConfig`.
* `CosmosDBAccessException` to `CosmosAccessException`.
* `Document` annotation to `Container` annotation.
* `DocumentIndexingPolicy` annotation to `CosmosIndexingPolicy` annotation.
* `DocumentQuery` to `CosmosQuery`.
* application.properties flag `populateQueryMetrics` to `queryMetricsEnabled`.
#### Bugs Fixed
* Scheduling diagnostics logging task to `Parallel` threads to avoid blocking Netty I/O threads.
* Fixed optimistic locking on delete operation.
* Fixed issue with escaping queries for `IN` clause.
* Fixed issue by allowing `long` data type for `@Id`.
* Fixed issue by allowing `boolean`, `long`, `int`, `double` as data types for `@PartitionKey` annotation.
* Fixed `IgnoreCase` & `AllIgnoreCase` keywords for ignore case queries.
* Removed default request unit value of 4000 when creating containers automatically.
