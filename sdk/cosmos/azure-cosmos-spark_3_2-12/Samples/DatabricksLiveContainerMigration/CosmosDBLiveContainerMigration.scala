// Databricks notebook source
dbutils.widgets.removeAll()

// COMMAND ----------

// global config
dbutils.widgets.text("cosmosEndpoint", "") // enter the Cosmos DB Account URI of the source account
dbutils.widgets.text("cosmosMasterKey", "") // enter the Cosmos DB Account PRIMARY KEY of the source account
dbutils.widgets.text("cosmosRegion", "") // enter the Cosmos DB Region

// source config
dbutils.widgets.text("cosmosSourceDatabaseName", "") // enter the name of your source database
dbutils.widgets.text("cosmosSourceContainerName", "") // enter the name of the container you want to migrate
dbutils.widgets.text("cosmosSourceContainerThroughputControl", "") // targetThroughputThreshold defines target percentage of available throughput you want the migration to use

// target config
dbutils.widgets.text("cosmosTargetDatabaseName", "") // enter the name of your target database
dbutils.widgets.text("cosmosTargetContainerName", "") // enter the name of the target container
dbutils.widgets.text("cosmosTargetContainerPartitionKey", "") // enter the partition key for how data is stored in the target container
dbutils.widgets.text("cosmosTargetContainerProvisionedThroughput", "") // enter the partition key for how data is stored in the target container

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

import org.apache.spark.sql.DataFrame

def connectToCosmos(cosmosEndpoint: String, cosmosMasterKey: String) {
  spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
  spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
  spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)
}

def returnCosmosDBProperties(cosmosContainerProvisionedThroughput: String): String = {
  if (cosmosTargetContainerProvisionedThroughput contains "shared") { return "WITH DBPROPERTIES (%s)".format(cosmosTargetContainerProvisionedThroughput.replace("sharedDB", "")) } else return ""
}

def returnCosmosContainerProperties(cosmosContainerProvisionedThroughput: String): String = {
  if(!(cosmosTargetContainerProvisionedThroughput contains "shared")) { return "%s,".format(cosmosTargetContainerProvisionedThroughput) } else return ""
}

def createCosmosDB(cosmosDatabaseName: String, cosmosContainerProvisionedThroughput: String){
  val cosmosDatabaseOptions =  returnCosmosDBProperties(cosmosContainerProvisionedThroughput)
  
  val query = s"CREATE DATABASE IF NOT EXISTS cosmosCatalog.`$cosmosDatabaseName` $cosmosDatabaseOptions;"
  println("Executing create database...")
  println(query.trim())
  try {
      spark.sql(query)
  } catch {
      case e:Exception=> println(e)
  }    
}

def createThroughtputControlTable(cosmosDatabaseName: String){/* NOTE: It is important to enable TTL (can be off/-1 by default) on the throughput control container */
  val query = s"""
                  CREATE TABLE IF NOT EXISTS cosmosCatalog.`$cosmosDatabaseName`.`ThroughputControl` USING cosmos.oltp 
                  OPTIONS (
                          spark.cosmos.database = '$cosmosDatabaseName'
                          ) 
                  TBLPROPERTIES(
                                  partitionKeyPath = '/groupId',
                                  autoScaleMaxThroughput = '4000',
                                  indexingPolicy = 'AllProperties',
                                  defaultTtlInSeconds = '-1'
                                  );
              """
  println("Executing create ThroughputControl Container...")
  println(query.trim())
  try {
      spark.sql(query)
  } catch {
      case e:Exception=> println(e)
  }
}

def createCosmosContainer(cosmosDatabaseName: String, cosmosContainerName: String, cosmosPartitionKey: String, cosmosContainerProvisionedThroughput: String) {
  val cosmosContainerOptions =  returnCosmosContainerProperties(cosmosContainerProvisionedThroughput)
  
  val query = s"""
                 CREATE TABLE IF NOT EXISTS cosmosCatalog.`$cosmosDatabaseName`.`$cosmosContainerName` USING cosmos.oltp 
                 TBLPROPERTIES (
                              partitionKeyPath = '$cosmosPartitionKey',
                              $cosmosContainerOptions
                              indexingPolicy = 'OnlySystemProperties'
                              );
              """
  println("Executing create Container...")
  println(query.trim())
  try {
      spark.sql(query)
  } catch {
      case e:Exception=> println(e)
  }
}

def cosmosInitReadStream(cosmosEndpoint: String, cosmosMasterKey: String, cosmosRegion: String, cosmosDatabaseName: String, cosmosContainerName: String, cosmosContainerThroughputControl: String): DataFrame = {
  val changeFeedConfig = Map(
                             "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
                             "spark.cosmos.accountKey" -> cosmosMasterKey,
                             "spark.cosmos.applicationName" -> s"${cosmosDatabaseName}_${cosmosContainerName}_LiveMigrationRead_",   
                             "spark.cosmos.database" -> cosmosDatabaseName,
                             "spark.cosmos.container" -> cosmosContainerName,
                             "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
                             "spark.cosmos.read.inferSchema.enabled" -> "false",
                             "spark.cosmos.read.maxItemCount" -> "5",
                             "spark.cosmos.changeFeed.startFrom" -> "Beginning",
                             "spark.cosmos.changeFeed.mode" -> "Incremental",
                             "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "50000", 
                             "spark.cosmos.throughputControl.enabled" -> "true",
                             "spark.cosmos.throughputControl.name" -> "SourceContainerThroughputControl",
                             "spark.cosmos.throughputControl.targetThroughputThreshold" -> cosmosContainerThroughputControl, 
                             "spark.cosmos.throughputControl.globalControl.database" -> cosmosDatabaseName,
                             "spark.cosmos.throughputControl.globalControl.container" -> "ThroughputControl",
                             "spark.cosmos.preferredRegionsList" -> cosmosRegion
                            )
  
  val changeFeedDF = spark.readStream.format("cosmos.oltp.changeFeed")
      .options(changeFeedConfig)
      .load
  
  /*this will preserve the source document fields and retain the "_etag" and "_ts" property values as "_origin_etag" and "_origin_ts" in the sink documnet*/
  return changeFeedDF.withColumnRenamed("_rawbody", "_origin_rawBody")
}

def cosmosInitWriteConfig(cosmosEndpoint: String, cosmosMasterKey: String, cosmosSourceDatabaseName: String, cosmosSourceContainerName: String, cosmosTargetDatabaseName: String, cosmosTargetContainerName: String): Map[String, String] = {   
    // when running this notebook is stopped (or if a problem causes a crash) change feed processing will be picked up from last processed document
    // if you want to start from beginning, delete this folder or change checkpointLocation value
    val checkpointLocation = s"/tmp/live_migration_checkpoint/${cosmosSourceDatabaseName}/${cosmosSourceContainerName}"
    val applicationName = s"${cosmosSourceDatabaseName}_${cosmosSourceContainerName}_"

    return Map(
    "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.accountKey" -> cosmosMasterKey,
    "spark.cosmos.applicationName" -> applicationName,                     
    "spark.cosmos.database" -> cosmosTargetDatabaseName,
    "spark.cosmos.container" -> cosmosTargetContainerName,
    "spark.cosmos.write.strategy" -> "ItemOverwrite",
    "checkpointLocation" -> checkpointLocation
    )
}

// COMMAND ----------

connectToCosmos(cosmosEndpoint = cosmosEndpoint, cosmosMasterKey = cosmosMasterKey)

createCosmosDB(cosmosDatabaseName = cosmosTargetDatabaseName, cosmosContainerProvisionedThroughput = cosmosTargetContainerProvisionedThroughput)

createThroughtputControlTable(cosmosDatabaseName = cosmosSourceDatabaseName)

createCosmosContainer(cosmosDatabaseName = cosmosTargetDatabaseName, cosmosContainerName = cosmosTargetContainerName, cosmosPartitionKey = cosmosTargetContainerPartitionKey, cosmosContainerProvisionedThroughput = cosmosTargetContainerProvisionedThroughput)

// COMMAND ----------

val readStream = cosmosInitReadStream(cosmosEndpoint = cosmosEndpoint, cosmosMasterKey = cosmosMasterKey, cosmosRegion = cosmosRegion, cosmosDatabaseName = cosmosSourceDatabaseName, cosmosContainerName = cosmosSourceContainerName, cosmosContainerThroughputControl = cosmosSourceContainerThroughputControl)

val writeConfig = cosmosInitWriteConfig(cosmosEndpoint = cosmosEndpoint, cosmosMasterKey = cosmosMasterKey, cosmosSourceDatabaseName = cosmosSourceDatabaseName, cosmosSourceContainerName = cosmosSourceContainerName, cosmosTargetDatabaseName = cosmosTargetDatabaseName, cosmosTargetContainerName = cosmosTargetContainerName)

val runId = java.util.UUID.randomUUID.toString;

// COMMAND ----------

readStream.writeStream
  .format("cosmos.oltp")
  .queryName(runId)
  .options(writeConfig)
  .outputMode("append")
  .start()