## Changelog

### 2.0.0
- Replaced org.json dependency by jackson due to performance reasons and licensing (github #29)
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
- Dependency on ``azure-documentdb`` SDK removed.
- Artifact id changed to ``azure-cosmosdb`` from ``azure-documentdb-rx`` in 0.9.0-rc2.
- Java package name changed to ``com.microsoft.azure.cosmosdb`` from ``com.microsoft.azure.documentdb`` in 0.9.0-rc2.

### 0.9.0-rc2
- ``FeedResponsePage`` renamed to ``FeedReponse``
- Some minor modifications to ``ConnectionPolicy`` configuration.
All time fields and methods in ConnectionPolicy suffixed with "InMillis" to be more precise of the time unit.
- ``ConnectionPolicy#setProxy()`` removed.
- ``FeedOptions#pageSize`` renamed to
``FeedOptions#maxItemCount``
- Release 1.0.0 deprecates 0.9.x releases.

### 0.9.0-rc1
- First release of ``azure-documentdb-rx`` SDK.
- CRUD Document API fully non-blocking using netty. Query async API implemented as a wrapper using blocking SDK ``azure-documentdb``.
