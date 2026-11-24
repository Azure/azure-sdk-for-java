// Databricks notebook source
// global config
dbutils.widgets.text("cosmosEndpoint", "") // enter the Cosmos DB Account URI of the source account
dbutils.widgets.text("cosmosMasterKey", "") // enter the Cosmos DB Account PRIMARY KEY of the source account
dbutils.widgets.text("cosmosRegion", "") // enter the Cosmos DB Region

// source config
dbutils.widgets.text("cosmosSourceDatabaseName", "") // enter the name of your source database
dbutils.widgets.text("cosmosSourceContainerName", "") // enter the name of the container you want to migrate

// target config
dbutils.widgets.text("cosmosTargetDatabaseName", "") // enter the name of your target database
dbutils.widgets.text("cosmosTargetContainerName", "") // enter the name of the target container

// COMMAND ----------

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosRegion = dbutils.widgets.get("cosmosRegion")

val cosmosSourceDatabaseName = dbutils.widgets.get("cosmosSourceDatabaseName")
val cosmosSourceContainerName = dbutils.widgets.get("cosmosSourceContainerName") 

val cosmosTargetDatabaseName = dbutils.widgets.get("cosmosTargetDatabaseName")
val cosmosTargetContainerName = dbutils.widgets.get("cosmosTargetContainerName")

val randomUUID = java.util.UUID.randomUUID.toString.subSequence(0,8);

// COMMAND ----------

import org.apache.spark.sql.DataFrame

def connectToCosmos(cosmosEndpoint: String, cosmosMasterKey: String) {
  println("Connecting to Cosmos DB...")
  spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
  spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
  spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)
  spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.views.repositoryPath", s"/viewDefinitions$randomUUID")
}

def createCosmosView(cosmosDatabaseName: String, cosmosContainerName: String, cosmosViewDatabaseName: String, cosmosViewName: String) {
  val tag = if (cosmosViewName contains "Target") {"_origin_etag"} else {"_etag"}
  val query = s"""
                  CREATE TABLE IF NOT EXISTS cosmosCatalog.`$cosmosViewDatabaseName`.`$cosmosViewName` (id STRING, ${tag} STRING)
                  USING cosmos.oltp 
                  TBLPROPERTIES(isCosmosView = 'True')
                  OPTIONS (
                            spark.cosmos.database = '$cosmosDatabaseName',
                            spark.cosmos.container = '$cosmosContainerName',
                            spark.cosmos.read.inferSchema.enabled = 'False',  
                            spark.cosmos.read.partitioning.strategy = 'Restrictive'
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
                        ON source.id = target.id and source._etag == target._origin_etag
                """
    println(query)
    return spark.sql(query)
}

def checkCosmosSourceTargetCountDiff(cosmosViewDatabaseName: String, cosmosSourceViewName: String, cosmosTargetViewName: String): DataFrame = {
    val query = s"""-- anti join shows all documents(versions) in the SourceView not present in SinkView
                        SELECT COUNT(*) FROM cosmosCatalog.`$cosmosViewDatabaseName`.`$cosmosSourceViewName` source
                        LEFT ANTI JOIN cosmosCatalog.`$cosmosViewDatabaseName`.`$cosmosTargetViewName` target
                        ON source.id = target.id and source._etag == target._origin_etag
                """
    println(query)
    return spark.sql(query)
}

// COMMAND ----------

connectToCosmos(cosmosEndpoint = cosmosEndpoint, cosmosMasterKey = cosmosMasterKey)

// COMMAND ----------

val cosmosSourceViewName = s"Source_${cosmosSourceContainerName}_${randomUUID}"
val cosmosTargetViewName = s"Target_${cosmosSourceContainerName}_${randomUUID}"
createCosmosView(cosmosDatabaseName = cosmosSourceDatabaseName, cosmosContainerName = cosmosSourceContainerName, cosmosViewDatabaseName = cosmosSourceDatabaseName, cosmosViewName = cosmosSourceViewName)
createCosmosView(cosmosDatabaseName = cosmosTargetDatabaseName, cosmosContainerName = cosmosTargetContainerName, cosmosViewDatabaseName = cosmosSourceDatabaseName, cosmosViewName = cosmosTargetViewName)

// COMMAND ----------

val sourceTargetDiff = checkCosmosSourceTargetDiff(cosmosViewDatabaseName = cosmosSourceDatabaseName, cosmosSourceViewName = cosmosSourceViewName, cosmosTargetViewName = cosmosTargetViewName)
val sourceTargetCountDiff = checkCosmosSourceTargetCountDiff(cosmosViewDatabaseName = cosmosSourceDatabaseName, cosmosSourceViewName = cosmosSourceViewName, cosmosTargetViewName = cosmosTargetViewName)

// COMMAND ----------

// this shouldn't return any results if the migration is completed, e.g. all records are available in the target, otherwise the missing records will be displayed
display(sourceTargetDiff)

// COMMAND ----------

// this will display the number of rows which are in the source container but not in target
display(sourceTargetCountDiff)