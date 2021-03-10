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


### Write Config


| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.write.strategy`      | `ItemOverwrite`    | Cosmos DB Item write Strategy: ItemOverwrite (using upsert), ItemAppend (using create, ignore Conflicts)  |
| `spark.cosmos.write.maxRetryCount`      | `3`    | Cosmos DB Write Max Retry Attempts on failure  |
| `spark.cosmos.write.bulkEnabled`      | `false`   | Cosmos DB Item Write bulk enabled |

### Query Config

#### Schema Inference Config

Note when running queries unless if are interested to get back the raw json payload 
we recommend setting `spark.cosmos.read.inferSchemaEnabled` to be `true`. 

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.read.inferSchemaEnabled`     | `false`    | Whether schema inference is enabled or should return raw json. We recommend to enable this option for query  |
| `spark.cosmos.read.inferSchemaQuery`      | `SELECT * FROM r`    | When schema inference is enabled, used as custom query to infer it for example if you store multiple entities with different schemas within a container and you want to ensure inference only looks at certain document types|
| `spark.cosmos.read.inferSchemaSamplingSize`      | `1000`    | Sampling size to use when inferring schema |

#### Partitioning Strategy Config

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.partitioning.strategy`     | `Default`    | The partitioning strategy used (Default, Custom, Restrictive or Aggressive) |
| `spark.cosmos.partitioning.targetedCount`      | None    | The targeted Partition Count. This parameter is optional and ignored unless strategy==Custom is used. In this case the Spark Connector won't dynamically calculate number of partitions but stick with this value.  |  |

### Throughput Control Config


| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `spark.cosmos.throughputControlEnabled`      | `false`    | Whether throughput control is enabled  |
| `spark.cosmos.throughputControl.name`      | None    | Throughput control group name   |
| `spark.cosmos.throughputControl.targetThroughput`      | None   | Throughput control group target throughput  |
| `spark.cosmos.throughputControl.targetThroughputThreshold`      | None    | Throughput control group target throughput threshold  |
| `spark.cosmos.throughputControl.globalControl.database`      | None    | Database which will be used for throughput global control  |
| `spark.cosmos.throughputControl.globalControl.container`      | None   | Container which will be used for throughput global control  |
| `spark.cosmos.throughputControl.globalControl.renewIntervalInMS`      | `5s`    | How often the client is going to update the throughput usage of itself  |
| `spark.cosmos.throughputControl.globalControl.expireIntervalInMS`      | `11s`   | How quickly an offline client will be detected |


[//]: # (//TODO: fabianm, moderakh add streaming config once ready)


