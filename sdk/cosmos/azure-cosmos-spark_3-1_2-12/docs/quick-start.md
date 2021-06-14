## Quick Start Guide for Cosmos DB Spark Connector Preview

This tutorial is a quick start guide to show how to use Cosmos DB Spark Connector to read from or write to Cosmos DB. Cosmos DB Spark Connector is based on Spark 3.1.x.

Throughout this quick tutorial we rely on [Azure Databricks Runtime 8.0 with Spark 3.1.1](https://docs.microsoft.com/azure/databricks/release-notes/runtime/8.0) and a Jupyter Notebook to show how to use the Cosmos DB Spark Connector.

You can use any other Spark 3.1.1 spark offering as well, also you should be able to use any language supported by Spark (PySpark, Scala, Java, etc), or any Spark interface you are familiar with (Jupyter Notebook, Livy, etc).

### Prerequisites

- An active Azure account. If you don't have one, you can sign up for a 
  [free account](https://azure.microsoft.com/try/cosmosdb/).
  Alternatively, you can use the
  [use Azure Cosmos DB Emulator](https://docs.microsoft.com/azure/cosmos-db/local-emulator) for development and testing.
- [Azure Databricks](https://docs.microsoft.com/azure/databricks/release-notes/runtime/8.0)
  Runtime 8.0 with Spark 3.1.1.
- (Optional) [SLF4J binding](https://www.slf4j.org/manual.html) is used to associate a
  specific logging framework with SLF4J.

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

Install Cosmos DB Spark Connector, in your spark Cluster [azure-cosmos-spark_3-1_2-12-4.1.0.jar](https://search.maven.org/artifact/com.azure.cosmos.spark/azure-cosmos-spark_3-1_2-12/4.1.0/jar)

The getting started guide is based on PySpark however you can use the equivalent scala version as well, and you can run the following code snippet in an Azure Databricks PySpark notebook.

### Create databases and containers

First, set Cosmos DB account credentials, and the Cosmos DB Database name and container name.

```python
cosmosEndpoint = "https://REPLACEME.documents.azure.com:443/"
cosmosMasterKey = "REPLACEME"
cosmosDatabaseName = "sampleDB"
cosmosContainerName = "sampleContainer"

cfg = {
  "spark.cosmos.accountEndpoint" : cosmosEndpoint,
  "spark.cosmos.accountKey" : cosmosMasterKey,
  "spark.cosmos.database" : cosmosDatabaseName,
  "spark.cosmos.container" : cosmosContainerName,
}
```

Next, you can use the new Catalog API to create a Cosmos DB Database and Container through Spark.

```python
# Configure Catalog Api to be used
spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)

# create a cosmos database using catalog api
spark.sql("CREATE DATABASE IF NOT EXISTS cosmosCatalog.{};".format(cosmosDatabaseName))

# create a cosmos container using catalog api
spark.sql("CREATE TABLE IF NOT EXISTS cosmosCatalog.{}.{} using cosmos.oltp TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '1100')".format(cosmosDatabaseName, cosmosContainerName))
```

When creating containers with the Catalog API you can set the throughput and [partition key path](https://docs.microsoft.com/azure/cosmos-db/partitioning-overview#choose-partitionkey) for the container to be created.

For more details, see the full [Catalog API](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/catalog-api.md) documentation.

### Ingesting data

The name of the data source is `cosmos.oltp`, and the following example shows how you can write a memory dataframe consisting of two items to Cosmos DB:

```python
spark.createDataFrame((("cat-alive", "Schrodinger cat", 2, True), ("cat-dead", "Schrodinger cat", 2, False)))\
  .toDF("id","name","age","isAlive") \
   .write\
   .format("cosmos.oltp")\
   .options(**cfg)\
   .mode("APPEND")\
   .save()
```

Note that `id` is a mandatory field for Cosmos DB.

For more details related to ingesting data, see the full [write configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md#write-config) documentation.

### Querying data

Using the same `cosmos.oltp` data source, we can query data and use `filter` to push down filters:

```python
from pyspark.sql.functions import col

df = spark.read.format("cosmos.oltp").options(**cfg)\
 .option("spark.cosmos.read.inferSchema.enabled", "true")\
 .load()

df.filter(col("isAlive") == True)\
 .show()
```

For more details related to querying data, see the full [query configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md#query-config) documentation.

### Schema inference

When querying data, the Spark Connector can infer the schema based on sampling existing items by setting `spark.cosmos.read.inferSchema.enabled` to `true`.

```python
df = spark.read.format("cosmos.oltp").options(**cfg)\
 .option("spark.cosmos.read.inferSchema.enabled", "true")\
 .load()
 
df.printSchema()
```

Alternatively, you can pass the custom schema you want to be used to read the data:

```python
customSchema = StructType([
      StructField("id", StringType()),
      StructField("name", StringType()),
      StructField("type", StringType()),
      StructField("age", IntegerType()),
      StructField("isAlive", BooleanType())
    ])

df = spark.read.schema(schema).format("cosmos.oltp").options(**cfg)\
 .load()
 
df.printSchema()
```

If no custom schema is specified and schema inference is disabled, then the resulting data will be returning the raw Json content of the items:

```python
df = spark.read.format("cosmos.oltp").options(**cfg)\
 .load()
 
df.printSchema()
```

For more details related to schema inference, see the full [schema inference configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md#schema-inference-config) documentation.
