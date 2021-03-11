Configuration Reference:


## Generic Configuration


| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.accountEndpoint`      | None   | Cosmos DB Account Endpoint Uri |
| `spark.cosmos.accountKey`      | None    | Cosmos DB Account Key  |
| `spark.cosmos.database`      | None    | Cosmos DB database name  |
| `spark.cosmos.container`      | None    | Cosmos DB container name  |


### Additional tuning



| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.useGatewayMode`      | `false`    | Use gateway mode for the client operations  |
| `spark.cosmos.read.forceEventualConsistency`  | `true`    | Makes the client use Eventual consistency for read operations instead of using the default account level consistency |
| `spark.cosmos.applicationName`      | None    | Application name  |


### Write Config


| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.write.strategy`      | `ItemOverwrite`    | Cosmos DB Item write Strategy: ItemOverwrite (using upsert), ItemAppend (using create, ignore Conflicts)  |
| `spark.cosmos.write.maxRetryCount`      | `3`    | Cosmos DB Write Max Retry Attempts on failure  |
| `spark.cosmos.write.bulkEnabled`      | `false`   | Cosmos DB Item Write bulk enabled |

### Query Config

#### Schema Inference Config

When doing read operations, users can specify a custom schema or allow the connector to infer it. When no custom schema is provided and schema inference is not enabled, the connector will return raw json.
Enabling the schema inference is done by setting `spark.cosmos.read.inferSchemaEnabled` to be `true`.

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.inferSchemaEnabled`     | `false`    | Whether schema inference is enabled or should return raw json. We recommend to enable this option for query. |
| `spark.cosmos.read.inferSchemaQuery`      | `SELECT * FROM r`    | When schema inference is enabled, used as custom query to infer it. For example, if you store multiple entities with different schemas within a container and you want to ensure inference only looks at certain document types or you want to project only particular columns. |
| `spark.cosmos.read.inferSchemaSamplingSize`      | `1000`    | Sampling size to use when inferring schema and not using a query. |
| `spark.cosmos.read.inferSchemaIncludeSystemProperties`     | `false`    | When schema inference is enabled, whether the resulting schema will include all [Cosmos DB system properties](https://docs.microsoft.com/azure/cosmos-db/account-databases-containers-items#properties-of-an-item). |
| `spark.cosmos.read.inferSchemaIncludeTimestamp`     | `false`    | When schema inference is enabled, whether the resulting schema will include the document Timestamp (`_ts`). Not required if `spark.cosmos.read.inferSchemaIncludeSystemProperties` is enabled, as it will already include all system properties. |

#### Partitioning Strategy Config

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.partitioning.strategy`     | `Default`    | The partitioning strategy used (Default, Custom, Restrictive or Aggressive) |
| `spark.cosmos.partitioning.targetedCount`      | None    | The targeted Partition Count. This parameter is optional and ignored unless strategy==Custom is used. In this case the Spark Connector won't dynamically calculate number of partitions but stick with this value.  |  |



[//]: # (//TODO: fabianm, moderakh add streaming config once ready)


