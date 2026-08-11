// Databricks notebook source
dbutils.widgets.removeAll()

// COMMAND ----------

// source config
dbutils.widgets.text("cosmosSourceEndpoint", "") // enter the Cosmos DB Account URI of the source account
dbutils.widgets.text("cosmosSourceMasterKey", "") // enter the Cosmos DB Account PRIMARY KEY of the source account
dbutils.widgets.text("cosmosSourcePreferredRegions", "[UK South]") // enter the Cosmos DB Region of the source account
dbutils.widgets.text("cosmosSourceDatabaseName", "") // enter the name of your source database
dbutils.widgets.text("cosmosSourceContainerName", "") // enter the name of the container you want to migrate
dbutils.widgets.text("cosmosSourceContainerThroughputControl", "0.95") // defines target percentage of available throughput you want the migration to use

// target config
dbutils.widgets.text("cosmosTargetEndpoint", "") // enter the Cosmos DB Account URI of the target account
dbutils.widgets.text("cosmosTargetMasterKey", "") // enter the Cosmos DB Account PRIMARY KEY of the target account
dbutils.widgets.text("cosmosTargetDatabaseName", "") // enter the name of your target database
dbutils.widgets.text("cosmosTargetContainerName", "") // enter the name of the target container
dbutils.widgets.text("cosmosTargetContainerPartitionKey", "/pk") // replace "pk" with partition key field used by the target container (keep forward slash "/" at start)
dbutils.widgets.text("cosmosTargetContainerProvisionedThroughput", "10000") // enter the provisioned throughput for the target container

// COMMAND ----------

val cosmosSourceEndpoint = dbutils.widgets.get("cosmosSourceEndpoint")
val cosmosSourceMasterKey = dbutils.widgets.get("cosmosSourceMasterKey")
val cosmosSourcePreferredRegions = dbutils.widgets.get("cosmosSourcePreferredRegions")
val cosmosSourceDatabaseName = dbutils.widgets.get("cosmosSourceDatabaseName")
val cosmosSourceContainerName = dbutils.widgets.get("cosmosSourceContainerName") 
val cosmosSourceContainerThroughputControl = dbutils.widgets.get("cosmosSourceContainerThroughputControl")

val cosmosTargetEndpoint = dbutils.widgets.get("cosmosTargetEndpoint")
val cosmosTargetMasterKey = dbutils.widgets.get("cosmosTargetMasterKey")
val cosmosTargetDatabaseName = dbutils.widgets.get("cosmosTargetDatabaseName")
val cosmosTargetContainerName = dbutils.widgets.get("cosmosTargetContainerName")
val cosmosTargetContainerPartitionKey = dbutils.widgets.get("cosmosTargetContainerPartitionKey")
val cosmosTargetContainerProvisionedThroughput = dbutils.widgets.get("cosmosTargetContainerProvisionedThroughput")

// COMMAND ----------

import org.apache.spark.sql.SparkSession
val spark = SparkSession
  .builder()
  .config("spark.sql.catalog.cosmosCatalogSrc", "com.azure.cosmos.spark.CosmosCatalog")
  .config("spark.sql.catalog.cosmosCatalogSrc.spark.cosmos.accountEndpoint", cosmosSourceEndpoint)
  .config("spark.sql.catalog.cosmosCatalogSrc.spark.cosmos.accountKey", cosmosSourceMasterKey)
  .config("spark.sql.catalog.cosmosCatalogTgt", "com.azure.cosmos.spark.CosmosCatalog")
  .config("spark.sql.catalog.cosmosCatalogTgt.spark.cosmos.accountEndpoint", cosmosTargetEndpoint)
  .config("spark.sql.catalog.cosmosCatalogTgt.spark.cosmos.accountKey", cosmosTargetMasterKey)
  .getOrCreate()

// COMMAND ----------

var createThroughputControl = s"""
/* NOTE: It is important to enable TTL (can be off/-1 by default) on the throughput control container */
CREATE TABLE IF NOT EXISTS cosmosCatalogSrc.`${cosmosSourceDatabaseName}`.ThroughputControl -- source database name - ThroughputControl table will be created there
USING cosmos.oltp
OPTIONS(spark.cosmos.database = '${cosmosSourceDatabaseName}') -- reference source database. Do NOT change value partitionKeyPath = '/groupId' below - it must be named '/groupId' for Throughput control feature to work
TBLPROPERTIES(partitionKeyPath = '/groupId', autoScaleMaxThroughput = '4000', indexingPolicy = 'AllProperties', defaultTtlInSeconds = '-1')
"""
var createTargetResources= s"""
CREATE TABLE IF NOT EXISTS cosmosCatalogTgt.`$cosmosTargetDatabaseName`.`$cosmosTargetContainerName` -- reference target database and container - it will be created here
USING cosmos.oltp 
TBLPROPERTIES(partitionKeyPath = '$cosmosTargetContainerPartitionKey', autoScaleMaxThroughput = $cosmosTargetContainerProvisionedThroughput, indexingPolicy = 'OnlySystemProperties')
"""
spark.sql(createThroughputControl)
spark.sql(createTargetResources)

// COMMAND ----------

  val changeFeedCfg = Map("spark.cosmos.accountEndpoint" -> cosmosSourceEndpoint,
    "spark.cosmos.applicationName" -> "LiveMigrationRead_",
    "spark.cosmos.accountKey" -> cosmosSourceMasterKey,
    "spark.cosmos.database" -> cosmosSourceDatabaseName,
    "spark.cosmos.container" -> cosmosSourceContainerName,
    "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
    "spark.cosmos.read.inferSchema.enabled" -> "false",   
    "spark.cosmos.changeFeed.startFrom" -> "Beginning",
    "spark.cosmos.changeFeed.mode" -> "Incremental",
    "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "50000", 
    "spark.cosmos.throughputControl.enabled" -> "true",
    "spark.cosmos.throughputControl.name" -> "SourceContainerThroughputControl",
    "spark.cosmos.throughputControl.targetThroughputThreshold" -> cosmosSourceContainerThroughputControl, 
    "spark.cosmos.throughputControl.globalControl.database" -> cosmosSourceDatabaseName,
    "spark.cosmos.throughputControl.globalControl.container" -> "ThroughputControl",
    "spark.cosmos.preferredRegionsList" -> cosmosSourcePreferredRegions
  )

  //when running this notebook is stopped (or if a problem causes a crash) change feed processing will be picked up from last processed document
  //if you want to start from beginning, delete this folder or change checkpointLocation value
  val checkpointLocation = "/tmp/LiveMigration_checkpoint"

  val writeCfg = Map("spark.cosmos.accountEndpoint" -> cosmosTargetEndpoint,
    "spark.cosmos.accountKey" -> cosmosTargetMasterKey,
    "spark.cosmos.applicationName" -> "LivemigrationWrite_",                     
    "spark.cosmos.database" -> cosmosTargetDatabaseName,
    "spark.cosmos.container" -> cosmosTargetContainerName,
    "spark.cosmos.write.strategy" -> "ItemOverwrite",  //default                
    "checkpointLocation" -> checkpointLocation
  )

// COMMAND ----------

val changeFeedDF = spark.readStream.format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)  
      .load
/*this will preserve the source document fields and retain the "_etag" and "_ts" property values as "_origin_etag" and "_origin_ts" in the sink documnet*/
 val df_withAuditFields = changeFeedDF.withColumnRenamed("_rawbody", "_origin_rawBody")

// COMMAND ----------

import scala.util.parsing.json.{JSON, JSONObject}
import scala.collection.immutable.{Map}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

val parser = new ObjectMapper()

def calculatePK(rawBody: String): String = {
  val body = parser.readTree(rawBody).asInstanceOf[ObjectNode]
  //change the below to be the field names whose values you want to add to the synthetic key
  val syntheticPKValue = body.get("id").textValue() + "_" + body.get("id").textValue() 
  val originalEtag = body.get("_etag").textValue()
  val originalTimestamp = body.get("_ts")
  //change _syntheticPK below to be the name if the synthetic pk field     
  body.put("_syntheticPK", syntheticPKValue)
  //retain orignal _etag and _ts to use in later validation
  body.put("_origin_etag", originalEtag)
  body.put("_origin_ts", originalTimestamp)
  parser.writeValueAsString(body)
}

// COMMAND ----------

import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

val pkUdf = spark.udf.register("calculatePK",(b: String) => calculatePK(b))

val changeFeed_df_withSyntheticPK = df_withAuditFields
  // uncomment the below to apply synthetic partition key mapping in cell above 
  //.withColumn("_rawBody", pkUdf(col("_origin_rawBody")))
  //.drop("_origin_rawBody")

// COMMAND ----------

  val runId = java.util.UUID.randomUUID.toString;
    changeFeed_df_withSyntheticPK.writeStream
      .format("cosmos.oltp")
      .queryName(runId)
      .options(writeCfg)
      .outputMode("append")
      .start()
