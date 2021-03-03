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
spark.sql("CREATE DATABASE IF NOT EXISTS cosmosCatalog.{};".format(cosmosDatabaseName))
```

Create a Cosmos DB container:
```python
# create a cosmos container
spark.sql("CREATE TABLE IF NOT EXISTS cosmosCatalog.{}.{} using cosmos.items TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '1100')".format(cosmosDatabaseName, cosmosContainerName))
```

Supported Configuration in `TBLPROPERTIES`

| Config Property Name      | Default | Description |
| :---        |    :----   |         :--- | 
| `partitionKeyPath`     | None    | Specifies the Partition Key Path for the new Cosmos DB Container. This is a mandatory option|
| `manualThroughput`      | None    | Specifies the manual throughput for the new Cosmos DB Container |  |


