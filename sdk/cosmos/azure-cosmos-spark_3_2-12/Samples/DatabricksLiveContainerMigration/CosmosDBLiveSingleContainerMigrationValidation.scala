// Databricks notebook source
dbutils.widgets.removeAll()

// COMMAND ----------

// source config
dbutils.widgets.text("cosmosSourceEndpoint", "") // enter the Cosmos DB Account URI of the source account
dbutils.widgets.text("cosmosSourceMasterKey", "") // enter the Cosmos DB Account PRIMARY KEY of the source account
dbutils.widgets.text("cosmosSourceDatabaseName", "") // enter the name of your source database
dbutils.widgets.text("cosmosSourceContainerName", "") // enter the name of the source container

// target config
dbutils.widgets.text("cosmosTargetEndpoint", "") // enter the Cosmos DB Account URI of the target account
dbutils.widgets.text("cosmosTargetMasterKey", "") // enter the Cosmos DB Account PRIMARY KEY of the target account
dbutils.widgets.text("cosmosTargetDatabaseName", "") // enter the name of your target database
dbutils.widgets.text("cosmosTargetContainerName", "") // enter the name of the target container

// COMMAND ----------

val cosmosSourceEndpoint = dbutils.widgets.get("cosmosSourceEndpoint")
val cosmosSourceMasterKey = dbutils.widgets.get("cosmosSourceMasterKey")
val cosmosSourceDatabaseName = dbutils.widgets.get("cosmosSourceDatabaseName")
val cosmosSourceContainerName = dbutils.widgets.get("cosmosSourceContainerName") 
val cosmosTargetEndpoint = dbutils.widgets.get("cosmosTargetEndpoint")
val cosmosTargetMasterKey = dbutils.widgets.get("cosmosTargetMasterKey")
val cosmosTargetDatabaseName = dbutils.widgets.get("cosmosTargetDatabaseName")
val cosmosTargetContainerName = dbutils.widgets.get("cosmosTargetContainerName")

// COMMAND ----------

import org.apache.spark.sql.SparkSession
val spark = SparkSession
  .builder()
  .config("spark.sql.catalog.cosmosCatalogSrc", "com.azure.cosmos.spark.CosmosCatalog")
  .config("spark.sql.catalog.cosmosCatalogSrc.spark.cosmos.accountEndpoint", cosmosSourceEndpoint)
  .config("spark.sql.catalog.cosmosCatalogSrc.spark.cosmos.accountKey", cosmosSourceMasterKey)
  .config("spark.sql.catalog.cosmosCatalogSrc.spark.cosmos.views.repositoryPath", "/viewDefinitions" +  java.util.UUID.randomUUID.toString)
  .config("spark.sql.catalog.cosmosCatalogTgt", "com.azure.cosmos.spark.CosmosCatalog")
  .config("spark.sql.catalog.cosmosCatalogTgt.spark.cosmos.accountEndpoint", cosmosTargetEndpoint)
  .config("spark.sql.catalog.cosmosCatalogTgt.spark.cosmos.accountKey", cosmosTargetMasterKey)
  .config("spark.sql.catalog.cosmosCatalogTgt.spark.cosmos.views.repositoryPath", "/viewDefinitions" +  java.util.UUID.randomUUID.toString)
  .getOrCreate()

// COMMAND ----------

var createSourceView = s"""
CREATE TABLE IF NOT EXISTS cosmosCatalogSrc.`${cosmosSourceDatabaseName}`.SourceView 
  (id STRING, _etag STRING)
USING cosmos.oltp
TBLPROPERTIES(isCosmosView = 'True')
OPTIONS (
  spark.cosmos.database = '${cosmosSourceDatabaseName}', -- source database 
  spark.cosmos.container = '${cosmosSourceContainerName}', -- source container 
  spark.cosmos.read.inferSchema.enabled = 'False',  
  spark.cosmos.read.partitioning.strategy = 'Restrictive');
"""

var selectView = s"""
SELECT * FROM cosmosCatalogSrc.`${cosmosSourceDatabaseName}`.SourceView
"""
spark.sql(createSourceView)
spark.sql(selectView).show

// COMMAND ----------

var createSinkView = s"""
CREATE TABLE IF NOT EXISTS cosmosCatalogTgt.`${cosmosTargetDatabaseName}`.SinkView 
  (id STRING, _origin_etag STRING)
USING cosmos.oltp
TBLPROPERTIES(isCosmosView = 'True')
OPTIONS (
  spark.cosmos.database = '${cosmosTargetDatabaseName}', --  target database 
  spark.cosmos.container = '${cosmosTargetContainerName}', -- target container
  spark.cosmos.read.inferSchema.enabled = 'False',  
  spark.cosmos.read.partitioning.strategy = 'Default');
"""
var selectSinkView = s"""
SELECT * FROM cosmosCatalogTgt.`${cosmosTargetDatabaseName}`.SinkView
"""
spark.sql(createSinkView)
spark.sql(selectSinkView).show

// COMMAND ----------

var getDocVersionDiff = s"""
-- anti join shows all documents(versions) in the SourceView not present in SinkView
SELECT * FROM cosmosCatalogSrc.`${cosmosSourceDatabaseName}`.SourceView src -- source database
LEFT ANTI JOIN cosmosCatalogTgt.`${cosmosTargetDatabaseName}`.SinkView sink -- target database
ON src.id = sink.id and src._etag == sink._origin_etag
"""
spark.sql(getDocVersionDiff).show

// COMMAND ----------

var getCountDiff = s"""
-- anti join shows count of all documents(versions) in the SourceView not present in SinkView
SELECT count(*) FROM cosmosCatalogSrc.`${cosmosSourceDatabaseName}`.SourceView src  -- source database
LEFT ANTI JOIN cosmosCatalogTgt.`${cosmosTargetDatabaseName}`.SinkView sink -- target database
ON src.id = sink.id and src._etag == sink._origin_etag
"""
spark.sql(getCountDiff).show
