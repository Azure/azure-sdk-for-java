## Quick Start Guide for Cosmos DB Spark Connector Preview

**NOTE this is a Preview build. This build has not been load or performance tested yet - and at this point is not recommended being used in production scenarios.**

This tutorial is a quick start guide to show how to use Cosmos DB Spark Connector Preview to read from or write to Cosmos DB. Cosmos DB Spark Connector is based on Spark 3.1.x. 

Throughout this quick tutorial we rely on 
[Azure Databricks Runtime 8.0 with Spark 3.1.1](https://docs.microsoft.com/azure/databricks/release-notes/runtime/8.0) and
JupyterNotebook to show how to use the Cosmos DB Spark Connector.

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

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will
link the SLF4J API with the logging implementation of your choice. See
the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

You need a Spark 3.0.1 Environment.

Install Cosmos DB Spark Connector, in your spark Cluster `azure-cosmos-spark_3-1_2-12-4.0.0-beta.1.jar`

[//]: # (//TODO: moderakh add maven coordinates once published)

The getting started guide is based on PySpark however you can use the equivalent scala version as well.
You can run the following code snippet in an Azure Databricks PySpark notebook.

### Create Cosmos DB Database and Container

Set Cosmos DB account credentials, and the Cosmos DB Database name and container name.
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

see [General Configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md#Generic-Configuration) for more detail.

You can use the new Catalog API to create a Cosmos DB Database and Container through Spark.
Configure Catalog Api to be used
```python
# Configure Catalog Api to be used
spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)
```

Create a Cosmos DB database
```python
# create a cosmos database using catalog api
spark.sql("CREATE DATABASE IF NOT EXISTS cosmosCatalog.{};".format(cosmosDatabaseName))
```

Create a Cosmos DB container:
```python
# create a cosmos container using catalog api
spark.sql("CREATE TABLE IF NOT EXISTS cosmosCatalog.{}.{} using cosmos.items TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '1100')".format(cosmosDatabaseName, cosmosContainerName))
```
Cosmos Catalog API for creating container supports setting throughput and partition-key-path for the container to be created.

see [Catalog API](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/catalog-api.md) for more detail.

### Ingest Data to Cosmos DB

The name of the Cosmos DB Data Source is "cosmos.items". following shows how you can write a memory dataframe consisting of two items to Cosmos DB.
```python
# Ingest data to Cosmos DB
spark.createDataFrame((("cat-alive", "Schrodinger cat", 2, True), ("cat-dead", "Schrodinger cat", 2, False)))\
  .toDF("id","name","age","isAlive") \
   .write\
   .format("cosmos.items")\
   .options(**cfg)\
   .mode("APPEND")\
   .save()
```
Note that `id` is a mandatory field for Cosmos DB.

see [Write Configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md#write-config) for more detail.


### Query Cosmos DB

```python
# Query data from Cosmos DB
from pyspark.sql.functions import col

df = spark.read.format("cosmos.items").options(**cfg)\
 .option("spark.cosmos.read.inferSchemaEnabled", "true")\
 .load()

df.filter(col("isAlive") == True)\
 .show()
```

see [Query Configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md#query-config) for more detail.

Note when running queries unless if are interested to get back the raw json payload
we recommend setting `spark.cosmos.read.inferSchemaEnabled` to be `true`.

see [Schema Inference Configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md#schema-inference-config) for more detail.


### See the Schema of Data Ingested in Cosmos DB Container

```python
# Show the inferred schema from Cosmos DB
df = spark.read.format("cosmos.items").options(**cfg)\
 .option("spark.cosmos.read.inferSchemaEnabled", "true")\
 .load()
 
df.printSchema()
```

see [Schema Inference Configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md#schema-inference-config) for more detail.

