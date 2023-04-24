// Databricks notebook source
dbutils.widgets.removeAll()

// COMMAND ----------

// global config
dbutils.widgets.text("cosmosEndpoint", "") // enter the Cosmos DB Account URI of the source account
dbutils.widgets.text("cosmosMasterKey", "") // enter the Cosmos DB Account PRIMARY KEY of the source account
dbutils.widgets.text("cosmosRegion", "[UK South]") // enter the Cosmos DB Region

// source config
dbutils.widgets.text("cosmosSourceDatabaseName", "") // enter the name of your source database
dbutils.widgets.text("cosmosSourceContainerName", "") // enter the name of the container you want to migrate
dbutils.widgets.text("cosmosSourceContainerThroughputControl", "0.95") // targetThroughputThreshold defines target percentage of available throughput you want the migration to use

// target config
dbutils.widgets.text("cosmosTargetDatabaseName", "") // enter the name of your target database
dbutils.widgets.text("cosmosTargetContainerName", "") // enter the name of the target container
dbutils.widgets.text("cosmosTargetContainerPartitionKey", "/pk") // enter the partition key used in the target container with forward slash "/" at start
dbutils.widgets.text("cosmosTargetContainerProvisionedThroughput", "10000") // enter the partition key for how data is stored in the target container

// COMMAND ----------

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosRegion = dbutils.widgets.get("cosmosRegion")

val cosmosSourceDatabaseName = dbutils.widgets.get("cosmosSourceDatabaseName")
val cosmosSourceContainerName = dbutils.widgets.get("cosmosSourceContainerName") 
val cosmosSourceContainerThroughputControl = dbutils.widgets.get("cosmosSourceContainerThroughputControl")

val cosmosTargetDatabaseName = dbutils.widgets.get("cosmosTargetDatabaseName")
val cosmosTargetContainerName = dbutils.widgets.get("cosmosTargetContainerName")
val cosmosTargetContainerPartitionKey = dbutils.widgets.get("cosmosTargetContainerPartitionKey")
val cosmosTargetContainerProvisionedThroughput = dbutils.widgets.get("cosmosTargetContainerProvisionedThroughput")

// COMMAND ----------

spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)

// COMMAND ----------

// MAGIC %sql
// MAGIC /* NOTE: It is important to enable TTL (can be off/-1 by default) on the throughput control container */
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.`$cosmosSourceDatabaseName`.ThroughputControl -- source database name - ThroughputControl table will be created there
// MAGIC USING cosmos.oltp
// MAGIC OPTIONS(spark.cosmos.database = '$cosmosSourceDatabaseName') -- reference source database. Do NOT change value partitionKeyPath = '/groupId' below - it must be named '/groupId' for Throughput control feature to work
// MAGIC TBLPROPERTIES(partitionKeyPath = '/groupId', autoScaleMaxThroughput = '4000', indexingPolicy = 'AllProperties', defaultTtlInSeconds = '-1');
// MAGIC 
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.`$cosmosTargetDatabaseName`.`$cosmosTargetContainerName` -- reference target database and container - it will be created here
// MAGIC USING cosmos.oltp 
// MAGIC -- replace /customerId with the name of the field that you want to be used as the partition key in the new version of the container
// MAGIC TBLPROPERTIES(partitionKeyPath = '$cosmosTargetContainerPartitionKey', autoScaleMaxThroughput = $cosmosTargetContainerProvisionedThroughput, indexingPolicy = 'OnlySystemProperties');

// COMMAND ----------

  val changeFeedCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.applicationName" -> "LiveMigrationRead_",
    "spark.cosmos.accountKey" -> cosmosMasterKey,
    "spark.cosmos.database" -> cosmosSourceDatabaseName,
    "spark.cosmos.container" -> cosmosSourceContainerName,
    "spark.cosmos.read.partitioning.strategy" -> "Default",
    "spark.cosmos.read.inferSchema.enabled" -> "false",   
    "spark.cosmos.changeFeed.startFrom" -> "Beginning",
    "spark.cosmos.changeFeed.mode" -> "Incremental",
    "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "50000", 
    "spark.cosmos.throughputControl.enabled" -> "true",
    "spark.cosmos.throughputControl.name" -> "SourceContainerThroughputControl",
    "spark.cosmos.throughputControl.targetThroughputThreshold" -> cosmosSourceContainerThroughputControl, 
    "spark.cosmos.throughputControl.globalControl.database" -> cosmosSourceDatabaseName, //replace database-v4 with the name of your source database
    "spark.cosmos.throughputControl.globalControl.container" -> "ThroughputControl",
    "spark.cosmos.preferredRegionsList" -> cosmosRegion //replace this with comma separate list of regions appropriate for your source container
  )

  //when running this notebook is stopped (or if a problem causes a crash) change feed processing will be picked up from last processed document
  //if you want to start from beginning, delete this folder or change checkpointLocation value
  val checkpointLocation = "/tmp/LiveMigration_checkpoint13"

  val writeCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.accountKey" -> cosmosMasterKey,
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
