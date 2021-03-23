## Release History

## 4.0.0-beta.2 (Unreleased)

## 4.0.0-beta.1 (2021-03-22)
* Cosmos DB Spark 3.1.1 Connector Preview `4.0.0-beta.1` Release.
### Features
* Supports Spark 3.1.1 and Scala 2.12.
* Integrated against Spark3 DataSourceV2 API.
* Devloped ground up using Cosmos DB Java V4 SDK.
* Added support for Spark Query, Write, and Streaming.
* Added support for Spark3 Catalog metadata APIs.
* Added support for Java V4 Throughput Control.
* Added support for different partitioning strategies.
* Integrated against Cosmos DB TCP protocol.
* Added support for Databricks automated Maven Resolver.
* Added support for broadcasting CosmosClient caches to reduce bootstrapping RU throttling.
* Added support for unified jackson ObjectNode to SparkRow Converter.
* Added support for Raw Json format.
* Added support for Config Validation.
* Added support for Spark application configuration consolidation.
* Integrated against Cosmos DB FeedRange API to support Partition Split Proofing.
* Automated CI testing on DataBricks and Cosmos DB live endpoint.
* Automated CI Testing on Cosmos DB Emulator.

### Known limitations
* Spark structured streaming (micro batches) for consuming change feed has been implemented but not tested end-to-end fully so is considered experimental at this point.
* No support for continuous processing (change feed) yet.
* No perf tests / optimizations have been done yet - we will iterate on perf in the next preview releases. So usage should be limited to non-production environments with this preview.

## 4.0.0-alpha.1 (2021-03-17)
* Cosmos DB Spark 3.1.1 Connector Test Release.
