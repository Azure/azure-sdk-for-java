// Databricks notebook source
  val cosmosEndpoint_cf = "" //enter the Cosmos DB Account URI of the source account
  val cosmosMasterKey_cf = "" //enter the Cosmos DB Account PRIMARY KEY of the source account
  val cosmosDatabaseName_cf = "database-v4" //replace database-v4 with the name of your source database
  val cosmosContainerName_cf = "customer" //replace customer with the name of the container you want to migrate
  val cosmosContainerName_throughputControl = "0.95" //targetThroughputThreshold defines target percentage (here it is 95%) of available throughput you want the migration to use

  val cosmosEndpoint_write = "" //enter the Cosmos DB Account URI of the target account
  val cosmosMasterKey_write = "" //enter the Cosmos DB Account PRIMARY KEY of the target account
  val cosmosDatabaseName_write = "database-v4" //replace this with the name of your target database
  val cosmosContainerName_write = "customer_v2" //replace this with what you want to name your target container

// COMMAND ----------

spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint_cf)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey_cf)

// COMMAND ----------

// MAGIC %sql
// MAGIC /* NOTE: It is important to enable TTL (can be off/-1 by default) on the throughput control container */
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.`database-v4`.ThroughputControl -- replace database-v4 with source database name - ThroughputControl table will be created there
// MAGIC USING cosmos.oltp
// MAGIC OPTIONS(spark.cosmos.database = 'database-v4') -- replace database-v4 with the name of your source database
// MAGIC TBLPROPERTIES(partitionKeyPath = '/groupId', autoScaleMaxThroughput = '4000', indexingPolicy = 'AllProperties', defaultTtlInSeconds = '-1');
// MAGIC 
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.`database-v4`.customer_v2 -- replace database-v4 with the name of your source database, and customer_v2 with what you want to name your target container - it will be created here
// MAGIC USING cosmos.oltp 
// MAGIC -- replace /customerId with the name of the field that you want to be used as the partition key in the new version of the container
// MAGIC TBLPROPERTIES(partitionKeyPath = '/customerId', autoScaleMaxThroughput = '100000', indexingPolicy = 'OnlySystemProperties');

// COMMAND ----------

  val changeFeedCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint_cf,
    "spark.cosmos.applicationName" -> "LiveMigrationRead_",
    "spark.cosmos.accountKey" -> cosmosMasterKey_cf,
    "spark.cosmos.database" -> cosmosDatabaseName_cf,
    "spark.cosmos.container" -> cosmosContainerName_cf,
    "spark.cosmos.read.partitioning.strategy" -> "Default",
    "spark.cosmos.read.inferSchema.enabled" -> "false",   
    "spark.cosmos.changeFeed.startFrom" -> "Beginning",
    "spark.cosmos.changeFeed.mode" -> "Incremental",
    "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "50000", 
    "spark.cosmos.throughputControl.enabled" -> "true",
    "spark.cosmos.throughputControl.name" -> "SourceContainerThroughputControl",
    "spark.cosmos.throughputControl.targetThroughputThreshold" -> cosmosContainerName_throughputControl, 
    "spark.cosmos.throughputControl.globalControl.database" -> "database-v4", //replace database-v4 with the name of your source database
    "spark.cosmos.throughputControl.globalControl.container" -> "ThroughputControl",
    "spark.cosmos.preferredRegionsList" -> "[UK South]" //replace this with comma separate list of regions appropriate for your source container
  )

  //when running this notebook is stopped (or if a problem causes a crash) change feed processing will be picked up from last processed document
  //if you want to start from beginning, delete this folder or change checkpointLocation value
  val checkpointLocation = "/tmp/LiveMigration_checkpoint"

  val writeCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint_write,
    "spark.cosmos.accountKey" -> cosmosMasterKey_write,
    "spark.cosmos.applicationName" -> "LivemigrationWrite_",                     
    "spark.cosmos.database" -> cosmosDatabaseName_write,
    "spark.cosmos.container" -> cosmosContainerName_write,
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

  val runId = java.util.UUID.randomUUID.toString;
    df_withAuditFields.writeStream
      .format("cosmos.oltp")
      .queryName(runId)
      .options(writeCfg)
      .outputMode("append")
      .start()
