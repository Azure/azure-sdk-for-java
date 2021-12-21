# Ingesting data into Azure Cosmos DB via the Spark Connector for Spark 3.*

## Introduction
The `cosmos.oltp` data source can be used to write a data frame into Cosmos DB. By default the ingestion will happen using bulk mode and parameters like the `micro batch size` are automatically modified depending on the rate of throttled requests and transient errors like request timeouts. The `cosmos.oltp` data source will attempt to saturate the available throughput. So, a certain amount of 429 (Throttled requests) are expected and are actually a good sign, because it means the available throughput is really  saturated. If it is not desired to saturate the entire available throughput, client-side throughput control can be enabled - which means the ingestion will only attempt to saturate the throughput budget provided via throughput control.

### How bulk ingestion works
In Spark the DataFrame containing the input data will usually be partitioned. For each partition of the input data Spark will create a Spark task, which will process this partition (a slice of the input data) independently of other Spark tasks. 

![Spark partitioning overview](./media/Spark_partitioning.png)

The `cosmos.oltp` data source will create one singleton `CosmosAsyncClient` per Executor/Driver-node - so, this client instance will be shared across multiple Spark tasks being executed concurrently on a single Executor node. But it will create one `BulkWriter` for each Spark task/partition - this `BulkWriter` will only be responsible for ingesting the data of a single Spark partition.
Each `BulkWriter` will buffer data in-memory for each physical partition in Cosmos DB and "flush" it by sending a single request ingesting multiple documents when certain thresholds are met (thresholds resulting in "flushing" documents for a physical partition into the Cosmos DB backend are based on the currently determined micro batch size, the total payload size for all buffered documents and an interval measuring how long the documents buffered have not been persisted yet (to make sure that even on partitions with just a few documents, the documents get persisted after a couple of seconds at least).
The benefit of this model where each `BulkWriter` can write data across all Cosmos partitions independently is, that it maintains the isolation of Spark tasks and partitions, plays well with built-in Spark retry policies and its scalabilty behavior. The drawback is that when ingesting a large data volume into a target Cosmos DB container with many physical partitions (significantly above 100 physical partitions or 5 TB of data), there is a relatively large client-side compute overhead, because each Spark Task will create its own `BulkWriter` which will have to buffer data for hundreds of partitions. So, when for example using a Spark Cluster with Executor nodes with 16 or 32 cores - there will be 16 (or 32) `BulkWriter` instances all buffering data (and processing it in separate reactor pipelines) for several hundred partitions. In this use case it can be beneficial to repartition the input data first - to avoid that each Spark partition contains data for all Cosmos partitions (see the `Optimization recommendations when migrating  into large containers (>> 100 physical partitions or >> 5 TB of data)` section below)

### Throughput control
The client-throughput control capability in the `cosmos.oltp` data source allows you to limit the "RU budget" that can be used for a certain Spark job - the enforcement of the "RU budget" will happen client-side - so it is a best effort enforcement and no hard guarantee - but usually you will see that the average RU consumption follows the configured restrictions very well (plus/minus a few single digit percent). The client-side through put control happens across all Spark executors and the driver - so, it is a global throughput enforcement. To be able to coordinate the "RU budget" each executor can consume, a dedicated Cosmos DB container is used, to store the meta-data and balance the load and RU-usage between executors.

The below sample configuration would limit the "RU budget" for a Spark job to 60% of the total provisioned throughput (manually provisioned RU or max. AutoScale RU) of the container.
``
df \
   .write \
   .format("cosmos.oltp") \
   .options(**cfg) \
   .option("spark.cosmos.throughputControl.enabled", "true") \
   .option("spark.cosmos.throughputControl.name", "MyTestIngestionJobWithLimitedRU") \
   .option("spark.cosmos.throughputControl.targetThroughputThreshold", "0.6") \
   .option("spark.cosmos.throughputControl.globalControl.database", "YourDatabase") \
   .option("spark.cosmos.throughputControl.globalControl.container", "ThroughputControl") \
   .mode("APPEND") \
   .save()
``

You can find more details about the client-throughput control feature and its configuration options here - see [Client throughput control](./ThroughputControl.md)

### Retry policies and data validation
All transient errors (Throttled requests, network timeouts, any recoverable service errors etc.) are retried automatically by the `cosmos.oltp` data source. Any non-transient errors - for example "400-Bad request" when the value of the "id" column is invalid - which would not be recoverable by retries will result in the Spark job failing. When your container has a "unique key constraint policy" any 409 "Conflict" (indicating violation of unique key constraint) handling will depend on the `spark.cosmos.write.strategy` - for `ItemOverwrite` a 409 - Conflict due to unique key violation will result in an error - and the Spark hob will fail. For `ItemAppend` like conflicts on pk+id any unique key policy constraint violation will be ignored.

## Preparation
Below are a couple of tipps/best-practices that can help you to prepare for a data migration into a Cosmos DB container.

### Choosing a good partition key
To be able to fully saturate the provisioned throughput of a container it is important to choose a partition key that ensures the ingested data is spread over all physical partitions. A really bad choice for example would be a date/time based partition key. Imagine a daily batch job ingests telemetry events and a partition key of the form YYYYMMDD is used. In this cass all documents being ingested daily would target a single logical partitions. So independent of the total provisdioned throughput at most 10,000 RU (current maximum throughput per physical/logical partition) could be used for the ingestion job. You can find some more info abut [partitioning in Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/partitioning-overview) and (how to choose a good partition key)[https://docs.microsoft.com/en-us/azure/cosmos-db/partitioning-overview#choose-partitionkey] in the documentation.

### Indexing policy
The RU-charge when inserting or updating a document in Cosmos DB depends on the size of the document as well as the number of "tokens" that need to be indexed. A little simplified you can think of the number of tokens as the number of json properties that need to be indexed. So, to optimize throughput for the ingestion it would be beneficial to use a scoped indexing policy (not the default policy of indexing all properties, but only the properties that really need to be indexed). The two documents below contain more information about how to choose the right indexing policy.
- [Indexing policies in Azure Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/index-policy)
- [Indexing in Azure Cosmos DB - Overview](https://docs.microsoft.com/en-us/azure/cosmos-db/index-overview)

Sample01: How to create a new container with default indexing policy (index all properties) in Spark
``
%sql
CREATE TABLE IF NOT EXISTS cosmosCatalog.SampleDatabase.ThroughputControl
USING cosmos.oltp
TBLPROPERTIES(partitionKeyPath = '/groupId', autoScaleMaxThroughput = '4000', indexingPolicy = 'AllProperties', defaultTtlInSeconds = '-1');
``

Sample02: How to create a new container with minimal indexing policy (index only required system properties) in Spark
``
%sql
CREATE TABLE IF NOT EXISTS cosmosCatalog.SampleDatabase.GreenTaxiRecords
USING cosmos.oltp
TBLPROPERTIES(partitionKeyPath = '/id', autoScaleMaxThroughput = '100000', indexingPolicy = 'OnlySystemProperties');
``

Sample03: How to create a new container with custom indexing policy in Spark
``
myCustomIndexPolicyJson = '{"indexingMode":"consistent","automatic":true,"includedPaths":[{"path":"\/somePropertyName\/?"},{"path":"\/mypk\/?"}],"excludedPaths":[{"path":"\/*"}]}'
spark.sql("""
CREATE TABLE IF NOT EXISTS cosmosCatalog.SampleDatabase.TabeWithCustomIndexingPolicy
USING cosmos.oltp
TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '400', indexingPolicy = '{customIndexingPolicy}');
""".format(customIndexingPolicy = myCustomIndexPolicyJson))
``

### Creating a new container if the ingestion via the Cosmos Spark connector is for the initial migration

### Input data considerations

#### Populating "id" column

#### Check whether repartitioning is required

#### Optimization recommendations when migrating  into large containers (>> 100 physical partitions or >> 5 TB of data)

## Data migration

### Serialization settings

### Choosing the right Spark cluster size for the migration job



### Throughput control



### Estimating the necessary duration for the migration

## Checklist

## Troubleshooting