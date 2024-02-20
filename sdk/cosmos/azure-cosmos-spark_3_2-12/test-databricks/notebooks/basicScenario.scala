// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val cosmosMasterKey = "<inserted by environment>"

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName
)

val cfgWithAutoSchemaInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "true"
)

// COMMAND ----------

// create Cosmos Database and Cosmos Container using Catalog APIs
spark.conf.set(s"spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)

// create a cosmos database
spark.sql(s"CREATE DATABASE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName};")

// create a cosmos container
spark.sql(s"CREATE TABLE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} using cosmos.oltp " +
      s"TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '400')")

// update the throughput
spark.sql(s"ALTER TABLE cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} " +
  s"SET TBLPROPERTIES('manualThroughput' = '1100')")

// read database with client retrieved from cache
val clientFromCache = com.azure.cosmos.spark.udf.CosmosAsyncClientCache
  .getCosmosClientFromCache(cfg)
  .getClient
  .asInstanceOf[azure_cosmos_spark.com.azure.cosmos.CosmosAsyncClient]
val dbResponse = clientFromCache.getDatabase(cosmosDatabaseName).read().block()

assert(dbResponse.getProperties.getId.equals(cosmosDatabaseName))
clientFromCache.close

// COMMAND ----------

// ingestion
spark.createDataFrame(Seq(("cat-alive", "Schrodinger cat", 2, true), ("cat-dead", "Schrodinger cat", 2, false)))
  .toDF("id","name","age","isAlive")
   .write
   .format("cosmos.oltp")
   .options(cfg)
   .mode("APPEND")
   .save()

// COMMAND ----------

// Show the schema of the table and data without auto schema inference
val df = spark.read.format("cosmos.oltp").options(cfg).load()
df.printSchema()

df.show()

// COMMAND ----------

// Show the schema of the table and data with auto schema inference
val df = spark.read.format("cosmos.oltp").options(cfgWithAutoSchemaInference).load()
df.printSchema()

df.show()

// COMMAND ----------

import org.apache.spark.sql.functions.col

// Query to find the live cat and increment age of the alive cat
df.filter(col("isAlive") === true)
 .withColumn("age", col("age") + 1)
 .show()

// COMMAND ----------

// cleanup
spark.sql(s"DROP TABLE cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName};")
