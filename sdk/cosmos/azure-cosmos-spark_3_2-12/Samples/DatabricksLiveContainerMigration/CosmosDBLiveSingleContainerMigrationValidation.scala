// Databricks notebook source
val cosmosEndpoint_source = "" //enter your Cosmos DB Account URI
val cosmosMasterKey_source = "" //enter your Cosmos DB Account PRIMARY KEY

// Databricks notebook target
val cosmosEndpoint_target = "" //enter your Cosmos DB Account URI
val cosmosMasterKey_target = "" //enter your Cosmos DB Account PRIMARY KEY

// COMMAND ----------

spark.conf.set("spark.sql.catalog.cosmosCatalogSrc", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalogSrc.spark.cosmos.accountEndpoint", cosmosEndpoint_source)
spark.conf.set("spark.sql.catalog.cosmosCatalogSrc.spark.cosmos.accountKey", cosmosMasterKey_source)
spark.conf.set("spark.sql.catalog.cosmosCatalogSrc.spark.cosmos.views.repositoryPath", "/viewDefinitions" +  java.util.UUID.randomUUID.toString)

spark.conf.set("spark.sql.catalog.cosmosCatalogTgt", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalogTgt.spark.cosmos.accountEndpoint", cosmosEndpoint_target)
spark.conf.set("spark.sql.catalog.cosmosCatalogTgt.spark.cosmos.accountKey", cosmosMasterKey_target)
spark.conf.set("spark.sql.catalog.cosmosCatalogTgt.spark.cosmos.views.repositoryPath", "/viewDefinitions" +  java.util.UUID.randomUUID.toString)

// COMMAND ----------

// MAGIC %sql
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalogSrc.`database-v4`.SourceView 
// MAGIC   (id STRING, _etag STRING)
// MAGIC USING cosmos.oltp
// MAGIC TBLPROPERTIES(isCosmosView = 'True')
// MAGIC OPTIONS (
// MAGIC   spark.cosmos.database = 'database-v4', -- change database-v4 to be the value of your source database 
// MAGIC   spark.cosmos.container = 'customer', -- change customer to be the value of your source container 
// MAGIC   spark.cosmos.read.inferSchema.enabled = 'False',  
// MAGIC   spark.cosmos.read.partitioning.strategy = 'Default');
// MAGIC 
// MAGIC SELECT * FROM cosmosCatalogSrc.`database-v4`.SourceView

// COMMAND ----------

// MAGIC %sql
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalogTgt.`database-v4`.SinkView 
// MAGIC   (id STRING, _origin_etag STRING)
// MAGIC USING cosmos.oltp
// MAGIC TBLPROPERTIES(isCosmosView = 'True')
// MAGIC OPTIONS (
// MAGIC   spark.cosmos.database = 'database-v4', -- change database-v4 to be the value of your target database 
// MAGIC   spark.cosmos.container = 'customer_v2', -- change customer_v2 to be the value of your target container
// MAGIC   spark.cosmos.read.inferSchema.enabled = 'False',  
// MAGIC   spark.cosmos.read.partitioning.strategy = 'Default');
// MAGIC   
// MAGIC SELECT * FROM cosmosCatalogTgt.`database-v4`.SinkView

// COMMAND ----------

// MAGIC %sql
// MAGIC -- anti join shows all documents(versions) in the SourceView not present in SinkView
// MAGIC SELECT * FROM cosmosCatalogSrc.`database-v4`.SourceView src -- change database-v4 to be the value of your source database
// MAGIC LEFT ANTI JOIN cosmosCatalogTgt.`database-v4`.SinkView sink -- change database-v4 to be the value of your target database
// MAGIC ON src.id = sink.id and src._etag == sink._origin_etag

// COMMAND ----------

// MAGIC %sql
// MAGIC -- anti join shows count of all documents(versions) in the SourceView not present in SinkView
// MAGIC SELECT count(*) FROM cosmosCatalogSrc.`database-v4`.SourceView src  -- change database-v4 to be the value of your source database
// MAGIC LEFT ANTI JOIN cosmosCatalogTgt.`database-v4`.SinkView sink -- change database-v4 to be the value of your target database
// MAGIC ON src.id = sink.id and src._etag == sink._origin_etag
