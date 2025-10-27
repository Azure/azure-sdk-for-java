// Databricks notebook source
dbutils.widgets.text("cosmosEndpoint", "") // enter the Cosmos DB Account URI
dbutils.widgets.text("cosmosMasterKey", "") // enter the Cosmos DB Account PRIMARY KEY
dbutils.widgets.text("cosmosDatabaseName", "ReadManyDB") // name of database
dbutils.widgets.text("cosmosContainerName", "ReadManyContainer") // name of container
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")


// COMMAND ----------

// Configure Catalog Api  
spark.conf.set(s"spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", dbutils.widgets.get("cosmosEndpoint"))
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", dbutils.widgets.get("cosmosMasterKey"))

// COMMAND ----------

//generate a synthetic dataframe for the purpose of writing 2 million records across 500 logical partitions
import org.apache.spark.sql.functions._
import scala.util.Random
val idList = (1 to 2000000).map(_.toString)
val partitionKeyList = (1 to 2000000).map(_ => Random.nextInt(500).toString)
val propertyList = (1 to 2000000).map(_ => "value")
val data = idList.zip(partitionKeyList).zip(propertyList).map{ case ((id, partitionKey), property) => (id, partitionKey, property)}
val df = spark.createDataFrame(data).toDF("id", "partitionKey", "property")

// COMMAND ----------

//generating another 2 million records, but this dataframe will be used to read those records back with readMany API
import org.apache.spark.sql.functions._
import scala.util.Random
val idList2 = (2000001 to 4000000).map(_.toString)
val partitionKeyList2 = (2000001 to 4000000).map(_ => Random.nextInt(500).toString)
val propertyList2 = (2000001 to 4000000).map(_ => "value")
val data2 = idList2.zip(partitionKeyList2).zip(propertyList2).map{ case ((id, partitionKey), property) => (id, partitionKey, property)}
val dfsubset = spark.createDataFrame(data2).toDF("id", "partitionKey", "property")

// COMMAND ----------

//create database and container with 100000 request units
var createDatabase = s"""
  CREATE DATABASE IF NOT EXISTS cosmosCatalog.`${cosmosDatabaseName}`
"""

var createContainer = s"""
  CREATE TABLE IF NOT EXISTS cosmosCatalog.`${cosmosDatabaseName}`.`${cosmosContainerName}`
  USING cosmos.oltp
  TBLPROPERTIES(
    partitionKeyPath = '/partitionKey',
    autoScaleMaxThroughput = 100000,
    indexingPolicy = 'OnlySystemProperties'
  )
"""

//setup throughput control container so that throughput is balanced
var createThroughputControl = s"""
/* NOTE: It is important to enable TTL (can be off/-1 by default) on the throughput control container */
CREATE TABLE IF NOT EXISTS cosmosCatalog.`${cosmosDatabaseName}`.ThroughputControl 
USING cosmos.oltp
OPTIONS(spark.cosmos.database = '${cosmosDatabaseName}') 
TBLPROPERTIES(partitionKeyPath = '/groupId', autoScaleMaxThroughput = '4000', indexingPolicy = 'AllProperties', defaultTtlInSeconds = '-1')
"""
spark.sql(createDatabase)
spark.sql(createContainer)
spark.sql(createThroughputControl)

// COMMAND ----------

//write the 4 million rows in df and dfsubset to container (with 100000 RUs, should take 6-8 minutes)
import org.apache.spark.sql.functions._

val writeCfg = Map(
  "spark.cosmos.accountEndpoint" -> dbutils.widgets.get("cosmosEndpoint"),
  "spark.cosmos.accountKey" -> dbutils.widgets.get("cosmosMasterKey"),
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "false",
  "spark.cosmos.write.strategy" -> "ItemAppend",
  "spark.cosmos.throughputControl.enabled" -> "true",
  "spark.cosmos.throughputControl.name" -> "ContainerThroughputControl",
  "spark.cosmos.throughputControl.targetThroughputThreshold" -> "0.95", 
  "spark.cosmos.throughputControl.globalControl.database" -> cosmosDatabaseName,
  "spark.cosmos.throughputControl.globalControl.container" -> "ThroughputControl",
  "spark.cosmos.read.maxItemCount" -> "100"
)
df.write.format("cosmos.oltp").mode("Append").options(writeCfg).save()
dfsubset.write.format("cosmos.oltp").mode("Append").options(writeCfg).save()

// COMMAND ----------

//use readMany to read back the 2 million records subset - this should complete much faster than the query example in below cell (can be significantly faster when comparing larger datasets)
import com.azure.cosmos.spark.udf.{GetCosmosItemIdentityValue}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import scala.collection.JavaConverters._
import java.time.{Instant, Duration}

val readCfg = Map(
  "spark.cosmos.accountEndpoint" -> dbutils.widgets.get("cosmosEndpoint"),
  "spark.cosmos.accountKey" -> dbutils.widgets.get("cosmosMasterKey"),
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "false",
  "spark.cosmos.throughputControl.enabled" -> "true",
  "spark.cosmos.throughputControl.name" -> "ContainerThroughputControl",
  "spark.cosmos.throughputControl.targetThroughputThreshold" -> "0.95", 
  "spark.cosmos.throughputControl.globalControl.database" -> cosmosDatabaseName,
  "spark.cosmos.throughputControl.globalControl.container" -> "ThroughputControl",
  "spark.cosmos.read.maxItemCount" -> "1000"
)
//convert id + partitionKey into required format since this container is partitioned by a value other than id
spark.udf.register("GetCosmosItemIdentityValue", new GetCosmosItemIdentityValue(), StringType)
val df_with_itemIdentity = dfsubset.withColumn("_itemIdentity", expr("GetCosmosItemIdentityValue(id, array(partitionKey))"))

val schema = StructType(Array(StructField("id", StringType, false),StructField("partitionKey", StringType, false),StructField("property", StringType, false)))

//a dataframe containing all the ids (or _itemIdentity column with concatenation of id + partitionKey as done above where container is not partitioned by id) must be passed to readMany API
val readManyDf = com.azure.cosmos.spark.CosmosItemsDataSource.readMany(df_with_itemIdentity, readCfg.asJava, schema)
readManyDf.schema


// COMMAND ----------

//NOT RECOMMENDED, FOR ILLUSTRATION ONLY....
//Below is a query to get 2 million of the 4 million rows by id, which might reflect typical approach without using readMany API. Will typically be much slower than using readMany API above (especially for larger datasets).

import org.apache.spark.sql.functions._
import org.apache.spark.sql.DataFrame

val idList = dfsubset.select("id").collect.map(_.getString(0))
val commaSeparatedStringList = idList.map("'" + _ + "'").mkString(",")
// Split the commaSeparatedStringList into chunks of a specific size
val chunkSize = 1000000 // Choose an appropriate chunk size
val stringChunks = commaSeparatedStringList.split(",").grouped(chunkSize).toList

// Process the chunks and collect the results
var queryDF: DataFrame = null // Initialize the queryDF DataFrame

stringChunks.foreach { chunk =>
  val chunkList = chunk.mkString("','") // Convert the chunk into a comma-separated string
  var queryData = spark.sql(s"SELECT * FROM cosmosCatalog.`${cosmosDatabaseName}`.`${cosmosContainerName}` WHERE cosmosCatalog.`${cosmosDatabaseName}`.`${cosmosContainerName}`.id IN ($chunkList)")
  
  if (queryDF == null) {
    queryDF = queryData // Assign the first queryData DataFrame to queryDF
  } else {
    // Make sure the queryData DataFrame has the same number of columns as queryDF
    val missingColumns = queryDF.columns.diff(queryData.columns)
    
    missingColumns.foreach { column =>
      queryData = queryData.withColumn(column, lit(null).cast(queryDF.schema(column).dataType))
    }
    
    // Append the queryData DataFrame to queryDF
    queryDF = queryDF.union(queryData)
  }
}
queryDF.schema
