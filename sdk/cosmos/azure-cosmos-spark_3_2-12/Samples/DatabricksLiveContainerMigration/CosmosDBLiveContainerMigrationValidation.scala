// Databricks notebook source
val cosmosEndpoint = "" //enter your Cosmos DB Account URI
val cosmosMasterKey = "" //enter your Cosmos DB Account PRIMARY KEY

// COMMAND ----------

spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.views.repositoryPath", "/viewDefinitions" +  java.util.UUID.randomUUID.toString)

// COMMAND ----------

// MAGIC %sql
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.`database-v4`.SourceView 
// MAGIC   (id STRING, _etag STRING)
// MAGIC USING cosmos.oltp
// MAGIC TBLPROPERTIES(isCosmosView = 'True')
// MAGIC OPTIONS (
// MAGIC   spark.cosmos.database = 'database-v4', -- change database-v4 to be the value of your source database 
// MAGIC   spark.cosmos.container = 'customer', -- change customer to be the value of your source container 
// MAGIC   spark.cosmos.read.inferSchema.enabled = 'False',  
// MAGIC   spark.cosmos.read.partitioning.strategy = 'Default');
// MAGIC 
// MAGIC SELECT * FROM cosmosCatalog.`database-v4`.SourceView

// COMMAND ----------

// MAGIC %sql
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.`database-v4`.SinkView 
// MAGIC   (id STRING, _origin_etag STRING)
// MAGIC USING cosmos.oltp
// MAGIC TBLPROPERTIES(isCosmosView = 'True')
// MAGIC OPTIONS (
// MAGIC   spark.cosmos.database = 'database-v4', -- change database-v4 to be the value of your target database 
// MAGIC   spark.cosmos.container = 'customer_v2', -- change customer_v2 to be the value of your target container
// MAGIC   spark.cosmos.read.inferSchema.enabled = 'False',  
// MAGIC   spark.cosmos.read.partitioning.strategy = 'Default');
// MAGIC   
// MAGIC SELECT * FROM cosmosCatalog.`database-v4`.SinkView

// COMMAND ----------

// MAGIC %sql
// MAGIC -- anti join shows all documents(versions) in the SourceView not present in SinkView
// MAGIC SELECT * FROM cosmosCatalog.`database-v4`.SourceView src -- change database-v4 to be the value of your source database
// MAGIC LEFT ANTI JOIN cosmosCatalog.`database-v4`.SinkView sink -- change database-v4 to be the value of your target database
// MAGIC ON src.id = sink.id and src._etag == sink._origin_etag

// COMMAND ----------

// MAGIC %sql
// MAGIC -- anti join shows count of all documents(versions) in the SourceView not present in SinkView
// MAGIC SELECT count(*) FROM cosmosCatalog.`database-v4`.SourceView src  -- change database-v4 to be the value of your source database
// MAGIC LEFT ANTI JOIN cosmosCatalog.`database-v4`.SinkView sink -- change database-v4 to be the value of your target database
// MAGIC ON src.id = sink.id and src._etag == sink._origin_etag
