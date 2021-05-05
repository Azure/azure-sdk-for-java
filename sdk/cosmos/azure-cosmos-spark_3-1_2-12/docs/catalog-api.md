### Registering Cosmos Catalog API

To use Catalog API first you need to register and load `com.azure.cosmos.spark.CosmosCatalog`: 
```python
# create Cosmos Database and Cosmos Container using Catalog APIs
spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", REPLACEME)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", REPLACEME)
```

Create a Database
```python
# create a cosmos database
spark.sql("CREATE DATABASE IF NOT EXISTS cosmosCatalog.{};".format(REPLACEME))
```

Create a Cosmos DB container:
```python
# create a cosmos container with the specified partition key path and throughput
spark.sql("CREATE TABLE IF NOT EXISTS cosmosCatalog.{}.{} using cosmos.oltp TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '1100')".format(REPLACEME, REPLACEME))
```

Supported Configuration in `TBLPROPERTIES`

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `partitionKeyPath`     | None    | Specifies the Partition Key Path for the new Cosmos DB Container. This is a mandatory option|
| `manualThroughput`     | None    | Specifies the manual throughput for the new Cosmos DB Container |
| `autoScaleMaxThroughput`     | None    | Specifies the max. throughput for the new Cosmos DB Container when using AutoScale. With AutoScale Cosmos DB will manage the provisioned throughput (within the bandwidth of 10% of autoScaleMaxThroughput - autoScaleMaxThroughput)|
| `indexingPolicy`     | AllProperties    | Can be used to specify the indexing policy. Possible values are `AllProperties` (default Cosmos DB indexing policy where all properies are indexed. The RU charge for inserts/updates is increasing with the number of properties being indexed - so it can be useful to use a more targeted indexing policy instead. `OnlySystemProperties` can be used ot only index system properties like 'id' or '_ts'. Or if you want to use a custom indexing policy you can just provide the [json representation of the indexing policy](https://docs.microsoft.com/azure/cosmos-db/index-policy) here. |
| `defaultTtlInSeconds`     | None    | Specifies the default TTL that should be used for the container. If no `defaultTtlInSeconds` is specified TTL enforcement is completely disabled at the container. If `-1` is specified documents by default won't be automatically deleted unless there is a ['ttl' property](https://docs.microsoft.com/azure/cosmos-db/how-to-access-system-properties-gremlin#time-to-live-ttl) defined in an individual document. If the `defaultTtlInSeconds` is a positive value documents which don't override the 'ttl' property will be automatically deleted after `defaultTtlInSeconds` seconds.|