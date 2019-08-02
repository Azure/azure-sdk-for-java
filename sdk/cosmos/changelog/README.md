## Changelog

### 3.0.1
- Token Resolver null check fix in Cosmos Client Builder

### 3.0.0
- **Large API Changes**
- Changed library name to azure-cosmos
- Changed from Rx1 to Reactor based Async
- Change API from URI based to "Fluent" style.
    - Added CosmosDatabase, CosmosContainer, CosmosItem, etc. classes as sub-clients to CosmosClient
- Dropped attachment/media support
- Default changed to direct mode
- Changed name for Collection to Container
- Changed name for Document to Item
- Changed name for lots of types
    - AsyncDocumentClient -> CosmosClient
    - Document -> CosmosItemProperties
    - DocumentCollection -> CosmosContainerProperties
    - Database -> CosmosDatbaseProperties
    - etc.

### 2.5.1
- Bug fixes

### 2.5.0
- Direct mode now uses TCP by default (HTTP can be renabled via JAVA_OPTS)

### 2.4.5
- Bug fixes

### 2.4.4
- Bug fixes

### 2.4.3

- Fixed resource leak issue on closing client

### 2.4.2

- Fixed bugs in continuation token support for cross partition queries

### 2.4.1

- Fixed some bugs in Direct mode.
- Improved logging in Direct mode.
- Improved connection management.

### 2.4.0

- Direct GA.
- Added support for QueryMetrics.
- Changed the APIs accepting java.util.Collection for which order is important to accept java.util.List instead. Now ConnectionPolicy#getPreferredLocations(), JsonSerialization, and PartitionKey(.) accept List.

### 2.4.0-beta1

- Added support for Direct Https.
- Changed the APIs accepting java.util.Collection for which order is important to accept java.util.List instead.
  Now ConnectionPolicy#getPreferredLocations(), JsonSerialization, and PartitionKey(.) accept List.
- Fixed a Session bug for Document query in Gateway mode.
- Upgraded dependencies (netty 0.4.20 [github #79](https://github.com/Azure/azure-cosmosdb-java/issues/79), RxJava 1.3.8).

### 2.3.1

- Fix handling very large query responses.
- Fix resource token handling when instantiating client ([github #78](https://github.com/Azure/azure-cosmosdb-java/issues/78)).
- Upgraded vulnerable dependency jackson-databind ([github #77](https://github.com/Azure/azure-cosmosdb-java/pull/77)).

### 2.3.0

- Fixed a resource leak bug.
- Added support for MultiPolygon
- Added support for custom headers in RequestOptions.

### 2.2.2

- Fixed a packaging bug.

### 2.2.1

- Fixed a NPE bug in write retry path.
- Fixed a NPE bug in endpoint management.
- Upgraded vulnerable dependencies ([github #68](https://github.com/Azure/azure-cosmosdb-java/issues/68)).
- Added support for Netty network logging for troubleshooting.

### 2.2.0

- Added support for Multi-region write.

### 2.1.0

- Added support for Proxy.
- Added support for resource token authorization.
- Fixed a bug in handling large partition keys ([github #63](https://github.com/Azure/azure-cosmosdb-java/issues/63)).
- Documentation improved.
- SDK restructured into more granular modules.

### 2.0.1

- Fixed a bug for non-english locales ([github #51](https://github.com/Azure/azure-cosmosdb-java/issues/51)).
- Added helper methods for Conflict resource.

### 2.0.0

- Replaced org.json dependency by jackson due to performance reasons and licensing ([github #29](https://github.com/Azure/azure-cosmosdb-java/issues/29)).
- Removed deprecated OfferV2 class.
- Added accessor method to Offer class for throughput content.
- Any method in Document/Resource returning org.json types changed to return a jackson object type.
- getObject(.) method of classes extending JsonSerializable changed to return a jackson ObjectNode type.
- getCollection(.) method changed to return Collection of ObjectNode.
- Removed JsonSerializable subclasses' constructors with org.json.JSONObject arg.
- JsonSerializable.toJson (SerializationFormattingPolicy.Indented) now uses two spaces for indentation.

### 1.0.2

- Added support for Unique Index Policy.
- Added support for limiting response continuation token size in feed options.
- Added support for Partition Split in Cross Partition Query.
- Fixed a bug in Json timestamp serialization ([github #32](https://github.com/Azure/azure-cosmosdb-java/issues/32)).
- Fixed a bug in Json enum serialization.
- Fixed a bug in managing documents of 2MB size ([github #33](https://github.com/Azure/azure-cosmosdb-java/issues/33)).
- Dependency com.fasterxml.jackson.core:jackson-databind upgraded to 2.9.5 due to a bug ([jackson-databind: github #1599](https://github.com/FasterXML/jackson-databind/issues/1599))
- Dependency on rxjava-extras upgraded to 0.8.0.17 due to a bug ([rxjava-extras: github #30](https://github.com/davidmoten/rxjava-extras/issues/30)).
- The metadata description in pom file updated to be inline with the rest of documentation.
- Syntax improvement ([github #41](https://github.com/Azure/azure-cosmosdb-java/issues/41)), ([github #40](https://github.com/Azure/azure-cosmosdb-java/issues/40)).

### 1.0.1

- Added back-pressure support in query.
- Added support for partition key range id in query.
- Changed to allow larger continuation token in request header (bugfix github #24).
- netty dependency upgraded to 4.1.22.Final to ensure JVM shuts down after main thread finishes.
- Changed to avoid passing session token when reading master resources.
- Added more examples.
- Added more benchmarking scenarios.
- Fixed java header files for proper javadoc generation.

### 1.0.0

- Release 1.0.0 has fully end to end support for non-blocking IO using netty library in Gateway mode.
- Dependency on `azure-documentdb` SDK removed.
- Artifact id changed to `azure-cosmosdb` from `azure-documentdb-rx` in 0.9.0-rc2.
- Java package name changed to `com.azure.data.cosmos` from `com.microsoft.azure.documentdb` in 0.9.0-rc2.

### 0.9.0-rc2

- `FeedResponsePage` renamed to `FeedReponse`
- Some minor modifications to `ConnectionPolicy` configuration.
  All time fields and methods in ConnectionPolicy suffixed with "InMillis" to be more precise of the time unit.
- `ConnectionPolicy#setProxy()` removed.
- `FeedOptions#pageSize` renamed to
  `FeedOptions#maxItemCount`
- Release 1.0.0 deprecates 0.9.x releases.

### 0.9.0-rc1

- First release of `azure-documentdb-rx` SDK.
- CRUD Document API fully non-blocking using netty. Query async API implemented as a wrapper using blocking SDK `azure-documentdb`.
