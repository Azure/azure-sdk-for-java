## Release History

### 3.7.0-beta.1 (Unreleased)
#### New Features
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/).


### 3.6.0 (2021-04-06)
#### New Features
* Updated `azure-cosmos` to version 4.14.0.

### 3.5.1 (2021-03-24)
#### Key Bug Fixes
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

#### Key Bug Fixes
* Fixed issue when using automatic id generation with the auditable framework.
* Fixed query deserialization issue with `@query` annotated queries.

### 3.4.0 (2021-02-09)
#### New Features
* Updated `azure-cosmos` version to 4.12.0.

#### Key Bug Fixes
* Fixed `@EnableAuditing` for Java 11 modules.

### 3.3.0 (2021-01-15)
#### New Features
* Support for composite indexes in `CosmosIndexPolicy` annotation.
* Support for changes in `CosmosIndexPolicy` annotation.
* Updated azure-cosmos version to 4.11.0.

#### Key Bug Fixes
* Fixed query generation with sort and limit.

### 3.2.0 (2020-12-11)
#### New Features
* Updated Spring Data Commons version to 2.3.5.RELEASE.
* Updated Spring Core version to 5.2.10.RELEASE.

#### Key Bug Fixes
* Fixed publishing of `spring.factories` file with released jar.
* Fixed repository query with repeated parameters. 

### 3.1.0 (2020-10-21)
#### New Features
* Added support for `ARRAY_CONTAINS` `CriteriaType`.
* Updated azure-cosmos version to 4.7.1.

#### Key Bug Fixes
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
#### Key Bug Fixes
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
#### Key Bug Fixes
* Scheduling diagnostics logging task to `Parallel` threads to avoid blocking Netty I/O threads.
* Fixed optimistic locking on delete operation.
* Fixed issue with escaping queries for `IN` clause.
* Fixed issue by allowing `long` data type for `@Id`.
* Fixed issue by allowing `boolean`, `long`, `int`, `double` as data types for `@PartitionKey` annotation.
* Fixed `IgnoreCase` & `AllIgnoreCase` keywords for ignore case queries.
* Removed default request unit value of 4000 when creating containers automatically.
