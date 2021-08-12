Configuration Reference:


## Generic Configuration


| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.accountEndpoint`      | None   | Cosmos DB Account Endpoint Uri |
| `spark.cosmos.accountKey`      | None    | Cosmos DB Account Key  |
| `spark.cosmos.database`      | None    | Cosmos DB database name  |
| `spark.cosmos.container`      | None    | Cosmos DB container name  |


### Additional Tuning


| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.useGatewayMode`      | `false`    | Use gateway mode for the client operations  |
| `spark.cosmos.read.forceEventualConsistency`  | `true`    | Makes the client use Eventual consistency for read operations instead of using the default account level consistency |
| `spark.cosmos.read.maxItemCount`  | `1000`    | Overrides the maximum number of documents that can be returned for a single query- or change feed request. The default value is `1000` - consider increasing this only for average document sizes significantly smaller than 1KB.  |
| `spark.cosmos.applicationName`      | None    | Application name  |
| `spark.cosmos.preferredRegionsList`      | None    | Preferred regions list to be used for a multi region Cosmos DB account. This is a comma separated value (e.g., `[East US, West US]` or `East US, West US`) provided preferred regions will be used as hint. You should use a collocated spark cluster with your Cosmos DB account and pass the spark cluster region as preferred region. See list of azure regions [here](/dotnet/api/microsoft.azure.documents.locationnames?view=azure-dotnet&preserve-view=true). Please note that you can also use `spark.cosmos.preferredRegions` as alias |

### Write Config

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.write.strategy`      | `ItemOverwrite`    | Cosmos DB Item write Strategy: `ItemOverwrite` (using upsert), `ItemAppend` (using create, ignore pre-existing items i.e., Conflicts), `ItemDelete` (delete all documents), `ItemDeleteIfNotModified` (delete all documents for which the etag hasn't changed)  |
| `spark.cosmos.write.maxRetryCount`      | `10`    | Cosmos DB Write Max Retry Attempts on retriable failures (e.g., connection error, moderakh add more details)   |
| `spark.cosmos.write.point.maxConcurrency`   | None   | Cosmos DB Item Write Max concurrency. If not specified it will be determined based on the Spark executor VM Size |
| `spark.cosmos.write.bulk.maxPendingOperations`   | None   | Cosmos DB Item Write bulk mode maximum pending operations. Defines a limit of bulk operations being processed concurrently. If not specified it will be determined based on the Spark executor VM Size. If the volume of data is large for the provisioned throughput on the destination container, this setting can be adjusted by following the estimation of `1000 x Cores` |
| `spark.cosmos.write.bulk.enabled`      | `true`   | Cosmos DB Item Write bulk enabled |

### Query Config
| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.customQuery`      | None   | When provided the custom query will be processed against the Cosmos endpoint instead of dynamically generating the query via predicate push down. Usually it is recommended to rely on Spark's predicate push down because that will allow to generate the most efficient set of filters based on the query plan. But there are a couple of predicates like aggregates (count, group by, avg, sum etc.) that cannot be pushed down yet (at least in Spark 3.1) - so the custom query is a fallback to allow them to be pushed into the query sent to Cosmos. If specified, with schema inference enabled, the custom query will also be used to infer the schema. |

#### Schema Inference Config

When doing read operations, users can specify a custom schema or allow the connector to infer it. Schema inference is enabled by default.

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.inferSchema.enabled`     | `true`    | When schema inference is disabled and user is not providing a schema, raw json will be returned. |
| `spark.cosmos.read.inferSchema.query`      | `SELECT * FROM r`    | When schema inference is enabled, used as custom query to infer it. For example, if you store multiple entities with different schemas within a container and you want to ensure inference only looks at certain document types or you want to project only particular columns. |
| `spark.cosmos.read.inferSchema.samplingSize`      | `1000`    | Sampling size to use when inferring schema and not using a query. |
| `spark.cosmos.read.inferSchema.includeSystemProperties`     | `false`    | When schema inference is enabled, whether the resulting schema will include all [Cosmos DB system properties](account-databases-containers-items.md#properties-of-an-item). |
| `spark.cosmos.read.inferSchema.includeTimestamp`     | `false`    | When schema inference is enabled, whether the resulting schema will include the document Timestamp (`_ts`). Not required if `spark.cosmos.read.inferSchema.includeSystemProperties` is enabled, as it will already include all system properties. |
| `spark.cosmos.read.inferSchema.forceNullableProperties`     | `true`    | When schema inference is enabled, whether the resulting schema will make all columns nullable. By default, all columns (except cosmos system properties) will be treated as nullable even if all rows within the sample set have non-null values. When disabled, the inferred columns are treated as nullable or not depending on whether any record in the sample set has null-values within a column.  |

#### Json conversion configuration


| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.schemaConversionMode`     | `Relaxed`    | The schema conversion behavior (`Relaxed`, `Strict`). When reading json documents, if a document contains an attribute that does not map to the schema type, the user can decide whether to use a `null` value (Relaxed) or an exception (Strict). |

#### Partitioning Strategy Config

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.partitioning.strategy`     | `Default`    | The partitioning strategy used (Default, Custom, Restrictive or Aggressive) |
| `spark.cosmos.partitioning.targetedCount`      | None    | The targeted Partition Count. This parameter is optional and ignored unless strategy==Custom is used. In this case the Spark Connector won't dynamically calculate number of partitions but stick with this value.  |

### Throughput Control Config


| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.throughputControl.enabled`      | `false`    | Whether throughput control is enabled  |
| `spark.cosmos.throughputControl.name`      | None    | Throughput control group name   |
| `spark.cosmos.throughputControl.targetThroughput`      | None   | Throughput control group target throughput  |
| `spark.cosmos.throughputControl.targetThroughputThreshold`      | None    | Throughput control group target throughput threshold  |
| `spark.cosmos.throughputControl.globalControl.database`      | None    | Database which will be used for throughput global control  |
| `spark.cosmos.throughputControl.globalControl.container`      | None   | Container which will be used for throughput global control  |
| `spark.cosmos.throughputControl.globalControl.renewIntervalInMS`      | `5s`    | How often the client is going to update the throughput usage of itself  |
| `spark.cosmos.throughputControl.globalControl.expireIntervalInMS`      | `11s`   | How quickly an offline client will be detected |


[//]: # (//TODO: fabianm, moderakh add streaming config once ready)


