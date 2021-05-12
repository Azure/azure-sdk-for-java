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
| `spark.cosmos.applicationName`      | None    | Application name  |
| `spark.cosmos.preferredRegionsList`      | None    | Preferred regions list to be used for a multi region Cosmos DB account. This is a comma separated value (e.g., `[East US, West US]` or `East US, West US`) provided preferred regions will be used as hint. You should use a collocated spark cluster with your Cosmos DB account and pass the spark cluster region as preferred region. See list of azure regions [here](https://docs.microsoft.com/dotnet/api/microsoft.azure.documents.locationnames?view=azure-dotnet&preserve-view=true) |

### Write Config

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.write.strategy`      | `ItemOverwrite`    | Cosmos DB Item write Strategy: `ItemOverwrite` (using upsert), `ItemAppend` (using create, ignore pre-existing items i.e., Conflicts)  |
| `spark.cosmos.write.maxRetryCount`      | `10`    | Cosmos DB Write Max Retry Attempts on retriable failures (e.g., connection error, moderakh add more details)   |
| `spark.cosmos.write.point.maxConcurrency`   | None   | Cosmos DB Item Write Max concurrency. If not specified it will be determined based on the Spark executor VM Size |
| `spark.cosmos.write.bulk.maxPendingOperations`   | None   | Cosmos DB Item Write Max concurrency. If not specified it will be determined based on the Spark executor VM Size |
| `spark.cosmos.write.bulk.enabled`      | `true`   | Cosmos DB Item Write bulk enabled |

### Query Config

#### Schema Inference Config

When doing read operations, users can specify a custom schema or allow the connector to infer it. Schema inference is enabled by default.

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.inferSchema.enabled`     | `true`    | When schema inference is disabled and user is not providing a schema, raw json will be returned. |
| `spark.cosmos.read.inferSchema.query`      | `SELECT * FROM r`    | When schema inference is enabled, used as custom query to infer it. For example, if you store multiple entities with different schemas within a container and you want to ensure inference only looks at certain document types or you want to project only particular columns. |
| `spark.cosmos.read.inferSchema.samplingSize`      | `1000`    | Sampling size to use when inferring schema and not using a query. |
| `spark.cosmos.read.inferSchema.includeSystemProperties`     | `false`    | When schema inference is enabled, whether the resulting schema will include all [Cosmos DB system properties](https://docs.microsoft.com/azure/cosmos-db/account-databases-containers-items#properties-of-an-item). |
| `spark.cosmos.read.inferSchema.includeTimestamp`     | `false`    | When schema inference is enabled, whether the resulting schema will include the document Timestamp (`_ts`). Not required if `spark.cosmos.read.inferSchema.includeSystemProperties` is enabled, as it will already include all system properties. |
| `spark.cosmos.read.inferSchema.forceNullableProperties`     | `false`    | When schema inference is enabled, whether the resulting schema will make all columns nullable. By default whether inferred columns are treated as nullable or not will depend on whether any record in the sample set has null-values within a column. If set to `true` all columns will be treated as nullable even if all rows within the sample set have non-null values. |

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


