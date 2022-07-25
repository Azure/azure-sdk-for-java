## Configuration Reference:


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
| `spark.cosmos.preferredRegionsList`      | None    | Preferred regions list to be used for a multi region Cosmos DB account. This is a comma separated value (e.g., `[East US, West US]` or `East US, West US`) provided preferred regions will be used as hint. You should use a collocated spark cluster with your Cosmos DB account and pass the spark cluster region as preferred region. See list of azure regions [here](https://docs.microsoft.com/dotnet/api/microsoft.azure.documents.locationnames?view=azure-dotnet&preserve-view=true). Please note that you can also use `spark.cosmos.preferredRegions` as alias |
| `spark.cosmos.diagnostics`      | None    | Can be used to enable more verbose diagnostics. Currently the only supported option is to set this property to `simple` - which will result in additional logs being emitted as `INFO` logs in the Driver and Executor logs.|
| `spark.cosmos.disableTcpConnectionEndpointRediscovery`      | `false`    | Can be used to disable TCP connection endpoint rediscovery. TCP connection endpoint rediscovery should only be disabled when using custom domain names with private endpoints when using a custom Spark environment. When using Azure Databricks or Azure Synapse as Spark runtime it should never be required to disable endpoint rediscovery.|

### Write Config
| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- |
| `spark.cosmos.write.strategy`      | `ItemOverwrite`    | Cosmos DB Item write Strategy: <br/> - `ItemOverwrite` (using upsert), <br/> - `ItemOverwriteIfNotModified` (if etag property of the row is empty/null it will just do an insert and ignore if the document already exists - same as `ItemAppend`, if an etag value exists it will attempt to replace the document with etag pre-condition. If the document changed - identified by precondition failure - the update is skipped and the document is not updated with the content of the data frame row), <br/> - `ItemAppend` (using create, ignore pre-existing items i.e., Conflicts), <br/> - `ItemDelete` (delete all documents), <br/> - `ItemDeleteIfNotModified` (delete all documents for which the etag hasn't changed), <br/> - `ItemPatch` (Partial update all documents based on the patch config) |
| `spark.cosmos.write.maxRetryCount`      | `10`    | Cosmos DB Write Max Retry Attempts on retriable failures (e.g., connection error, moderakh add more details)   |
| `spark.cosmos.write.point.maxConcurrency`   | None   | Cosmos DB Item Write Max concurrency. If not specified it will be determined based on the Spark executor VM Size |
| `spark.cosmos.write.bulk.maxPendingOperations`   | None   | Cosmos DB Item Write bulk mode maximum pending operations. Defines a limit of bulk operations being processed concurrently. If not specified it will be determined based on the Spark executor VM Size. If the volume of data is large for the provisioned throughput on the destination container, this setting can be adjusted by following the estimation of `1000 x Cores` |
| `spark.cosmos.write.bulk.enabled`      | `true`   | Cosmos DB Item Write bulk enabled |

#### Patch Config
| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- |
| `spark.cosmos.write.patch.defaultOperationType`      | `Replace`   | Default Cosmos DB patch operation type. Supported ones include none, add, set, replace, remove, increment. Choose none for no-op, for others please reference [here](https://docs.microsoft.com/azure/cosmos-db/partial-document-update#supported-operations) for full context. |
| `spark.cosmos.write.patch.columnConfigs`      | None        | Cosmos DB patch column configs. It can container multiple definitions matching the following patterns separated by comma. col(column).op(operationType) or col(column).path(patchInCosmosdb).op(operationType) - The difference of the second pattern is that it also allows you to define a different cosmosdb path. |
| `spark.cosmos.write.patch.filter`      | None        | Used for [Conditional patch](https://docs.microsoft.com/azure/cosmos-db/partial-document-update-getting-started#java) |

### Query Config
| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.customQuery`      | None   | When provided the custom query will be processed against the Cosmos endpoint instead of dynamically generating the query via predicate push down. Usually it is recommended to rely on Spark's predicate push down because that will allow to generate the most efficient set of filters based on the query plan. But there are a couple of predicates like aggregates (count, group by, avg, sum etc.) that cannot be pushed down yet (at least in Spark 3.1) - so the custom query is a fallback to allow them to be pushed into the query sent to Cosmos. If specified, with schema inference enabled, the custom query will also be used to infer the schema. |
| `spark.cosmos.read.maxItemCount`  | `1000`    | Overrides the maximum number of documents that can be returned for a single query- or change feed request. The default value is `1000` - consider increasing this only for average document sizes significantly smaller than 1KB or when projection reduces the number of properties selected in queries significantly (like when only selecting "id" of documents etc.).  |

#### Schema Inference Config
When doing read operations, users can specify a custom schema or allow the connector to infer it. Schema inference is enabled by default.

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.inferSchema.enabled`     | `true`    | When schema inference is disabled and user is not providing a schema, raw json will be returned. |
| `spark.cosmos.read.inferSchema.query`      | `SELECT * FROM r`    | When schema inference is enabled, used as custom query to infer it. For example, if you store multiple entities with different schemas within a container and you want to ensure inference only looks at certain document types or you want to project only particular columns. |
| `spark.cosmos.read.inferSchema.samplingSize`      | `1000`    | Sampling size to use when inferring schema and not using a query. |
| `spark.cosmos.read.inferSchema.includeSystemProperties`     | `false`    | When schema inference is enabled, whether the resulting schema will include all [Cosmos DB system properties](https://docs.microsoft.com/azure/cosmos-db/account-databases-containers-items#properties-of-an-item). |
| `spark.cosmos.read.inferSchema.includeTimestamp`     | `false`    | When schema inference is enabled, whether the resulting schema will include the document Timestamp (`_ts`). Not required if `spark.cosmos.read.inferSchema.includeSystemProperties` is enabled, as it will already include all system properties. |
| `spark.cosmos.read.inferSchema.forceNullableProperties`     | `true`    | When schema inference is enabled, whether the resulting schema will make all columns nullable. By default, all columns (except cosmos system properties) will be treated as nullable even if all rows within the sample set have non-null values. When disabled, the inferred columns are treated as nullable or not depending on whether any record in the sample set has null-values within a column.  |

#### Serialization Config
Used to influence the json serialization/deserialization behavior

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.serialization.inclusionMode`     | `Always`    | Determines whether null/default values will be serialized to json or whether properties with null/default value will be skipped. The behavior follows the same ideas as [Jackson's JsonInclude.Include](https://github.com/FasterXML/jackson-annotations/blob/d0820002721c76adad2cc87fcd88bf60f56b64de/src/main/java/com/fasterxml/jackson/annotation/JsonInclude.java#L98-L227). `Always` means json properties are created even for null and default values. `NonNull` means no json properties will be created for explicit null values. `NonEmpty` means json properties will not be created for empty string values or empty arrays/mpas. `NonDefault` means json properties will be skipped not just for null/empty but also when the value is identical to the default value `0` for numeric properties for example. |
| `spark.cosmos.serialization.dateTimeConversionMode`     | `Default`    | The date/time conversion mode (`Default`, `AlwaysEpochMilliseconds`, `AlwaysEpochMillisecondsWithSystemDefaultTimezone`). With `Default` the standard Spark 3.* behavior is used (`java.sql.Date`/`java.time.LocalDate` are converted to EpochDay, `java.sql.Timestamp`/`java.time.Instant` are converted to MicrosecondsFromEpoch). With `AlwaysEpochMilliseconds` the same behavior the Cosmos DB connector for Spark 2.4 used is applied - `java.sql.Date`, `java.time.LocalDate`, `java.sql.Timestamp` and `java.time.Instant` are converted to MillisecondsFromEpoch. The behavior for `AlwaysEpochMillisecondsWithSystemDefaultTimezone` is identical with `AlwaysEpochMilliseconds` except that it will assume System default time zone / Spark session time zone (specified via `spark.sql.session.timezone`) instead of UTC when the date/time to be parsed has no explicit time zone.|

#### Change feed (only for Spark-Streaming using `cosmos.oltp.changeFeed` data source, which is read-only) configuration
| Config Property Name                            | Default       | Description |
| :---                                            | :----         | :---        | 
| `spark.cosmos.changeFeed.startFrom`               | `Beginning`   | ChangeFeed Start from settings (`Now`, `Beginning`  or a certain point in time (UTC) for example `2020-02-10T14:15:03`) - the default value is `Beginning`. If the write config contains a `checkpointLocation` and any checkpoints exist, the stream is always continued independent of the `spark.cosmos.changeFeed.startFrom` settings - you need to change `checkpointLocation` or delete checkpoints to restart the stream if that is the intention. | 
| `spark.cosmos.changeFeed.mode`                    | `Incremental` | ChangeFeed mode (`Incremental` or `FullFidelity`) - NOTE: `FullFidelity` is in experimental state right now. It requires that the subscription/account has been enabled for the private preview and there are known breaking changes that will happen for `FullFidelity` (schema of the returned documents). It is recommended to only use `FullFidelity` for non-production scenarios at this point. | 
| `spark.cosmos.changeFeed.itemCountPerTriggerHint` | None          | Approximate maximum number of items read from change feed for each micro-batch/trigger |
| `spark.cosmos.changeFeed.batchCheckpointLocation` | None          | Can be used to generate checkpoints when using change feed queries in batch mode - and proceeding on the next iteration where the previous left off. |

#### Json conversion configuration
| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.schemaConversionMode`     | `Relaxed`    | The schema conversion behavior (`Relaxed`, `Strict`). When reading json documents, if a document contains an attribute that does not map to the schema type, the user can decide whether to use a `null` value (Relaxed) or an exception (Strict). |

#### Partitioning Strategy Config
| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.partitioning.strategy`     | `Default`    | The partitioning strategy used (Default, Custom, Restrictive or Aggressive) |
| `spark.cosmos.partitioning.targetedCount`      | None    | The targeted Partition Count. This parameter is optional and ignored unless strategy==Custom is used. In this case the Spark Connector won't dynamically calculate number of partitions but stick with this value.  |
| `spark.cosmos.partitioning.feedRangeFilter`      | None    | Can be used to scope the query to a single logical Cosmos partition (or a subset of logical partitions). If this parameter is optionally provided, the partitioning strategy will be modified - only partitions for the scoped logical partitions will be created. So, the main benefit of this config option is to reduce the necessary SparkTasks/Partitions. |

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