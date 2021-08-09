# Migrating from the Cosmos DB Spark Connector for Spark 2.4 to the new connector for Spark 3

## Configuration settings

### Commonly used config options
| Name - V1-3 (Spark 2.4) | Name - V4 and later (Spark >= 3.1) | Notes |
| :---        |    :----   |         :--- | 
| endpoint | spark.cosmos.accountEndpoint | |
| masterkey | spark.cosmos.accountKey | |
| database | spark.cosmos.database | |
| collection | spark.cosmos.container | Set this config value to `Beginning` |
| preferredregions | spark.cosmos.preferredRegionsList | |
| application_name | spark.cosmos.applicationName | |
| connectionmode | spark.cosmos.useGatewayMode | Direct (over TCP) is the default - setting this to `true` will switch it to Gateway mode |
| changefeedstartfromthebeginning | spark.cosmos.changeFeed.startFrom | Set this config value to `Beginning` |
| changefeedstartfromdatetime | spark.cosmos.changeFeed.startFrom | Set this config value to the point in time you want to start from - for example `2020-02-10T14:15:03Z`|
| changefeedmaxpagesperbatch | spark.cosmos.changeFeed.itemCountPerTriggerHint | |
| WritingBatchSize | spark.cosmos.write.bulk.maxPendingOperations | Recommendation would be to start with the default (not specifying this config entry - and only adjust (reduce) it when really necessary|
| Upsert | spark.cosmos.write.strategy | If you use `ItemOverwrite` here the behavior is the same as with Upsert==true before |
| WriteThroughputBudget | spark.cosmos.throughputControl.* | See the `Throughput control` section below|
| MaxIngestionTaskParallelism | n/a | Not relevant anymore - just remove this config entry |
| query_pagesize | n/a | Not relevant anymore - just remove this config entry |
| query_custom | spark.cosmos.read.customQuery | When provided the custom query will be processed against the Cosmos endpoint instead of dynamically generating the query via predicate push down. Usually it is recommended to rely on Spark's predicate push down because that will allow to generate the most efficient set of filters based on the query plan. But there are a couple of of predicates like aggregates (count, group by, avg, sum etc.) that cannot be pushed down yet (at least in Spark 3.1) - so the custom query is a fallback to allow them to be pushed into the query sent to Cosmos. |
| readchangefeed | n/a | See the `"One DataSource rules them all" vs. separate DataSource for ChangeFeed` section below. For change feed we now have a dedicated DataSource |
| changefeedqueryname | n/a | See the `Structured streaming` section below. Bookmarks/offsets are not stored in a proprietary way by the connector any longer but by Spark's Metadata Store |
| changefeedcheckpointlocation | n/a | See the `Structured streaming` section below. Bookmarks/offsets are not stored in a proprietary way by the connector any longer but by Spark's Metadata Store |


### Other config options
| Name - V1-3 (Spark 2.4) | Name - V4 and later (Spark >= 3.1) | Notes |
| :---        |    :----   |         :--- | 
| query_maxretryattemptsonthrottledrequests | n/a| The new connector will always retry for ever when throttling is happening |
| query_maxretrywaittimeinseconds | n/a| The new connector will always retry for ever when throttling is happening |
| query_maxdegreeofparallelism | n/a | Not relevant anymore - just remove this config entry |
| query_maxbuffereditemcount | n/a | Not relevant anymore - just remove this config entry |
| query_enablescan | n/a | Not relevant anymore - just remove this config entry |
| query_disableruperminuteusage | n/a | Not relevant anymore - just remove this config entry |
| query_emitverbosetraces | n/a | Not relevant anymore - just remove this config entry |
| query_maxbuffereditemcount | n/a | Not relevant anymore - just remove this config entry |
| consistencylevel | spark.cosmos.read.forceEventualConsistency | By default the new connector will use Eventual Consistency for all read operations. If instead you want read operations to use the default account's consistency-level you can override the `spark.cosmos.read.forceEventualConsistency` property to `false`. |
| rollingchangefeed | n/a | Not relevant anymore - just remove this config entry |
| changefeedusenexttoken | n/a | Not relevant anymore - just remove this config entry |
| changefeedusenexttoken | n/a | Not relevant anymore - just remove this config entry |
| writestreamretrypolicy.* | n/a | Not relevant anymore - or not supported yet (poison message handling) |
| resourcetoken | | Not supported yet with the new connector |
| connectionmaxpoolsize | n/a | Not relevant anymore - just remove this config entry |
| connectionidletimeout | n/a | Not relevant anymore - just remove this config entry |
| connectionrequesttimeout | n/a | Not relevant anymore - just remove this config entry |

## Conceptual differences
The list above shows the new names of the different config options. Below you can find an explanation for conceptual differences in the new connector. The Cosmos DB Connector for Spark 3 is implementing using the DataSource V2 API (vs. Data Source V1 with the old connector) which is driving some changes especially for structured streaming.

### Partitioning
When reading data in Spark via a DataSource, the number of partitions in the returned RDD is determined by the DataSource. In the Spark 2.4 version of the Cosmos DB connector the DataSource would create 1 Spark partition for each physical Cosmos DB partition. Especially for smaller Cosmos accounts this often lead to situations where the number of resulting Spark partitions was lower than the number of Cores available for the executors - so the latency of the spark job was relatively high because not all Executors would participate in processing the read operations from Cosmos. The main reason for this decision was that it wasn't possible to scope a query to just a fragment of a physical partition when the Cosmos DB Connector for Spark 2.4 was created. With the new connector for Spark 3 we now by default use a new capability available in the Cosmos DB Backend, and the Java V4 SDK that allows scoping queries to just a fragment of a physical partition. As a result the default partitioning strategy will ensure that at least as many Spark partitions as Executor Cores are created. The number of Spark partition created will depend on the number of available Cores for executors, and the storage size in each physical partition.
For most use cases the default partitioning strategy `Default` should be sufficient. For some use cases (especially when doing very targeted filtering to just one logical partition key etc.) it might be preferable to use the previous partitioning model (just one partition per physical Cosmos partition) = this can be achieved by using the `Restrictive` partitioning strategy.

### "One DataSource rules them all" vs. separate DataSource for ChangeFeed
In the Cosmos DB Connector for Spark 2.4 all operations (writing or reading documents/items from the container as well as processing change feed) were surfaced by one DataSource. With the new Cosmos DB Connector for Spark 3 we are using two different DataSources - `cosmos.oltp` and `cosmos.oltp.changeFeed` to be able to express the supported capabilities in the DataSource V2 API correctly - for example to expose to the Spark runtime that the change feed DataSource would not support writes but supports read stream operations - while the DataSource for items support writing (both streaming and batch) but no read stream operations. This means when migrating existing notebooks/programs, it will be necessary to not only change the configuration but also the identifier in the `format(xxx)` clause to use the right identifier - `cosmos.oltp` for operations on items and `cosmos.oltp.changeFeed` when processing change feed events.

### Structured streaming
The API for Structured Streaming between DataSource V1 and V2 has changed. The new Cosmos DB connector uses the DataSource V2 API. As a result, some proprietary approaches (like persisting the progress/bookmarks/offset in a Cosmos DB container) are replaced by default Spark mechanics where possible. So the new connector for example just uses Spark Offsets to expose the progress. Spark will store these offsets in the provided checkpoint location and when you want to restart/recover a query the mechanism is the same as with any other DataSource supporting Structured Streaming - like Kafka for example. (The Structured Streaming Programming Guide)[http://spark.apache.org/docs/latest/structured-streaming-programming-guide.html] is a good starting point to learn how to work with Structured Streaming.
Currently, the new Cosmos DB Connector allows to use the `cosmos.oltp` DataSource as a sink and `cosmos.oltp.changefed` as a source for MicroBatch queries. We will add support for Continuous Processing in addition to MicroBatching soon after GA.


