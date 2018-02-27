## Changelog


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
