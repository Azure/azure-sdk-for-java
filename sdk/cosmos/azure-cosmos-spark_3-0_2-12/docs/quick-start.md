## Quick Start Guide for Cosmos DB Spark Connector Preview
This tutorial is a quick start guide to show how to use Cosmos DB Spark Connector Preview to read from or write to Cosmos DB. Cosmos DB Spark Connector is based on Spark 3.0.x. 

Throughput this quick tutorial we rely on Azure DataBricks Spark 3.0.1 and JupyterNotebook to show how to use the Cosmos DB Spark Connector.

You can use any other Spark 3.0.x spark offering as well, also you should be able to use any language supported by Spark (PySpark, Scala, Java, etc), or any Spark interface you are familiar with (Jupyter Notebook, Livy, etc).

### Prerequisites

- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription].
  Alternatively, you can use the
  [use Azure Cosmos DB Emulator](local-emulator.md)] for development and testing.
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](https://www.slf4j.org/manual.html) is used to associate a
  specific logging framework with SLF4J.
- (Optional) Maven

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will
link the SLF4J API with the logging implementation of your choice. See
the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

You need a a Spark 3.0.1 Environment.

Install Cosmos DB Spark Connector, in your spark Cluster `azure-cosmos-spark_3-0_2-12-4.0.0-beta.1.jar`

[//]: # (//TODO: moderakh add maven coordinates once published)


### Create Cosmos DB Database and Container

You can use the new Catalog API to create a Cosmos DB Database and Container through Spark.

Set Cosmos DB account credentials and the Cosmos DB Database name and 
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

[//]: # (TODO: moderakh add link to configuration-reference.md)


[//]: # (TODO: moderakh add schema inference enable option)

configure Catalog Api to be used
```python
# create Cosmos Database and Cosmos Container using Catalog APIs
spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)
```

Create a Cosmos DB database
```python

# create a cosmos database
spark.sql("CREATE DATABASE IF NOT EXISTS cosmosCatalog.{};".format(cosmosDatabaseName))
```

Create a Cosmos DB container:
```python
# create a cosmos container
spark.sql("CREATE TABLE IF NOT EXISTS cosmosCatalog.{}.{} using cosmos.items TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '1100')".format(cosmosDatabaseName, cosmosContainerName))
```
Cosmos Catalog API for creating container supports setting throughput and partition-key-path for the container to be created.

[//]: # (TODO: moderakh add link to configuration-reference.md)


### Ingest Data to Cosmos DB

The name of the Cosmos DB Data Source is "cosmos.items". following shows how you can write an memory dataframe consisting of two items to Cosmos DB.
```python
spark.createDataFrame((("cat-alive", "Schrodinger cat", 2, True), ("cat-dead", "Schrodinger cat", 2, False)))\
  .toDF("id","name","age","isAlive") \
   .write\
   .format("cosmos.items")\
   .options(**cfg)\
   .mode("APPEND")\
   .save()
```
Note that `id` is a mandatory field for Cosmos DB.

[//]: # (TODO: moderakh add link to configuration-reference.md)


### Query Cosmos DB


```python
## Query to find the live cat and increment age of the alive cat
from pyspark.sql.functions import col

df = spark.read.format("cosmos.items").options(**cfg).load()
df.filter(col("isAlive") == True)\
 .show()
```

Note when running queries unless if are interested to get back the raw json payload
we recommend setting `spark.cosmos.read.inferSchemaEnabled` to be `true`.

[//]: # (TODO: moderakh add link to configuration-reference.md)


### See the Schema of Data Ingested in Cosmos DB Container

```python
df = spark.read.format("cosmos.items").options(**cfg).load()
df.printSchema()
```

Note when running queries unless if are interested to get back the raw json payload
we recommend setting `spark.cosmos.read.inferSchemaEnabled` to be `true`.

[//]: # (TODO: moderakh add link to configuration-reference.md)

