// Databricks notebook source
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

val randomUUID = java.util.UUID.randomUUID.toString;

// COMMAND ----------

import org.apache.spark.sql.DataFrame

def connectToCosmos(cosmosEndpoint: String, cosmosMasterKey: String) {
  spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
  spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
  spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)
  spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.views.repositoryPath", "/viewDefinitions" +  java.util.UUID.randomUUID.toString)
}

def createCosmosView(cosmosDatabaseName: String, cosmosContainerName: String, cosmosViewDatabaseName: String, cosmosViewName: String) {
  val query = s"""
                  CREATE TABLE IF NOT EXISTS cosmosCatalog.`$cosmosViewDatabaseName`.`$cosmosViewName` (id STRING, _etag STRING)
                  USING cosmos.oltp 
                  TBLPROPERTIES(isCosmosView = 'True')
                  OPTIONS (
                            spark.cosmos.database = '$cosmosDatabaseName',
                            spark.cosmos.container = '$cosmosContainerName',
                            spark.cosmos.read.inferSchema.enabled = 'False',  
                            spark.cosmos.read.partitioning.strategy = 'Default'
                          );
              """
  println("Executing create View...")
  println(query.trim())
  try {
      spark.sql(query)
  } catch {
      case e:Exception=> println(e)
  }
}

def checkCosmosSourceTargetDiff(cosmosViewDatabaseName: String, cosmosSourceViewName: String, cosmosTargetViewName: String): DataFrame = {
    val query = s"""-- anti join shows all documents(versions) in the SourceView not present in SinkView
                        SELECT * FROM cosmosCatalog.`$cosmosViewDatabaseName`.`$cosmosSourceViewName` source
                        LEFT ANTI JOIN cosmosCatalog.`$cosmosViewDatabaseName`.`$cosmosTargetViewName` target
                        ON source.id = target.id and source._etag == target._etag
                """
    
    return spark.sql(query)
}

def checkCosmosSourceTargetCountDiff(cosmosViewDatabaseName: String, cosmosSourceViewName: String, cosmosTargetViewName: String): DataFrame = {
    val query = s"""-- anti join shows all documents(versions) in the SourceView not present in SinkView
                        SELECT COUNT(*) FROM cosmosCatalog.`$cosmosViewDatabaseName`.`$cosmosSourceViewName` source
                        LEFT ANTI JOIN cosmosCatalog.`$cosmosViewDatabaseName`.`$cosmosTargetViewName` target
                        ON source.id = target.id and source._etag == target._etag
                """
    
    return spark.sql(query)
}

// COMMAND ----------

val cosmosSourceViewName = cosmosSourceContainerName + '_' + randomUUID
val cosmosTargetViewName = cosmosTargetContainerName + '_' + randomUUID
createCosmosView(cosmosDatabaseName = cosmosSourceDatabaseName, cosmosContainerName = cosmosSourceContainerName, cosmosViewDatabaseName = cosmosSourceContainerName, cosmosViewName = cosmosSourceViewName)
createCosmosView(cosmosDatabaseName = cosmosTargetDatabaseName, cosmosContainerName = cosmosTargetContainerName, cosmosViewDatabaseName = cosmosSourceContainerName, cosmosViewName = cosmosTargetViewName)

// COMMAND ----------

val sourceTargetDiff = checkCosmosSourceTargetDiff(cosmosViewDatabaseName = cosmosSourceContainerName, cosmosSourceViewName = cosmosSourceViewName, cosmosTargetViewName = cosmosTargetViewName)
val sourceTargetCountDiff = checkCosmosSourceTargetCountDiff(cosmosViewDatabaseName = cosmosSourceContainerName, cosmosSourceViewName = cosmosSourceViewName, cosmosTargetViewName = cosmosTargetViewName)

// COMMAND ----------

display(sourceTargetDiff)

// COMMAND ----------

display(sourceTargetCountDiff)