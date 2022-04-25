// Databricks notebook source
val cosmosEndpoint = dbutils.secrets.get(scope = "cosmos", key = "cosmosEndpoint2") 
val cosmosMasterKey = dbutils.secrets.get(scope = "cosmos", key = "cosmosMasterKey")

// COMMAND ----------

// MAGIC %md
// MAGIC **Preparation - creating the Cosmos DB container to ingest the data into**
// MAGIC 
// MAGIC Configure the Catalog API to be used

// COMMAND ----------

spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.views.repositoryPath", "/viewDefinitions")

// COMMAND ----------

// MAGIC %md
// MAGIC And execute the command to create the new container with a throughput of up-to 100,000 RU (Autoscale - so 10,000 - 100,000 RU based on scale) and only system properties (like /id) being indexed. We will also create a second container that will be used to store metadata for the global throughput control

// COMMAND ----------

// MAGIC %sql
// MAGIC CREATE DATABASE IF NOT EXISTS cosmosCatalog.SampleDatabase;
// MAGIC 
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.SampleDatabase.SampleContainer
// MAGIC USING cosmos.oltp
// MAGIC TBLPROPERTIES(partitionKeyPath = '/pk', autoScaleMaxThroughput = '4000', indexingPolicy = 'OnlySystemProperties');
// MAGIC 
// MAGIC 
// MAGIC /* NOTE: It is important to enable TTL (can be off/-1 by default) on the throughput control container */
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.SampleDatabase.ThroughputControl
// MAGIC USING cosmos.oltp
// MAGIC OPTIONS(spark.cosmos.database = 'SampleDatabase')
// MAGIC TBLPROPERTIES(partitionKeyPath = '/groupId', autoScaleMaxThroughput = '4000', indexingPolicy = 'AllProperties', defaultTtlInSeconds = '-1');

// COMMAND ----------

// MAGIC %md
// MAGIC **Ingest sample data**

// COMMAND ----------

import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType};

val data = Seq(
    Row("1","1", Row("Bellevue","98009")),
    Row("2","2", Row("Seattle","98009")),
    Row("3","3", null)
  )

val dataSchema =
    new StructType()
      .add("id",StringType)
      .add("pk", StringType)
      .add("location", new StructType().add("city", StringType).add("zipCode", StringType))

val df = spark.createDataFrame(spark.sparkContext.parallelize(data), dataSchema)  
df.show(false)

val writeCfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> "SampleDatabase",
  "spark.cosmos.container" -> "SampleContainer")

df.write.format("cosmos.oltp").options(writeCfg).mode("append").save()

// COMMAND ----------

// MAGIC %md
// MAGIC **Query data**

// COMMAND ----------

val readCfg = Map (
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" ->  cosmosMasterKey,
  "spark.cosmos.database" ->  "SampleDatabase",
  "spark.cosmos.container" ->  "SampleContainer",
  "spark.cosmos.read.partitioning.strategy" ->  "Default",
  "spark.cosmos.read.inferSchema.enabled" ->  "false",
  "spark.cosmos.read.inferSchema.includeTimestamp" -> "false",
  "spark.cosmos.applicationName" -> "Test"
)

val query_df = spark.read.format("cosmos.oltp").options(readCfg).load()
println(s"Number of records retrieved via query: ${query_df.count()}") 


// COMMAND ----------

// MAGIC %md
// MAGIC **Merge json UDF**

// COMMAND ----------

import org.apache.spark.sql.Row
import org.apache.spark.sql.functions.{col, udf}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.ObjectNode

val mergeJson = (rawBody: String, row: Row) => {
  val objectMapper = new ObjectMapper()
  val rawBodyNode = objectMapper.readTree(rawBody).asInstanceOf[ObjectNode]

  row.schema.fieldNames.foreach(fieldName => {
  
    val segments = fieldName.split('/')
    var currentJson = rawBodyNode
    for (i <- 0 until segments.length) {
      print(i)
     if (i == segments.length - 1) {
       // we have reached to the end of the mapping path, set the fieldValue directly
      currentJson.set(segments(i), objectMapper.convertValue(row.getAs[String](fieldName), classOf[JsonNode]))
     } else {
       // check each segment of the mapping, if the node is Null or not defined, then add parent object node
       if (!currentJson.has(segments(i)) || currentJson.get(segments(i)).isNull()) {
         currentJson.set(segments(i), objectMapper.createObjectNode())
       }     
       currentJson = currentJson.get(segments(i)).asInstanceOf[ObjectNode]
     } 
    }
   })
    
  objectMapper.writeValueAsString(rawBodyNode)
 }

// change to udf
val mergeJsonUdf = udf(mergeJson)

// COMMAND ----------

import spark.implicits._
import org.apache.spark.sql.functions.{col, struct}
import org.apache.spark.sql.Row

// this is the dataframe need to be patched

val new_df = Seq(
  ("3", "3", "Renton", "98055")).toDF("id", "pk", "location/city", "location/zipCode'")

//join the two data frames on id column
val joined_df = query_df.join(new_df, Seq("id"))

val merged_df =
  joined_df.withColumn("mergedBody", mergeJsonUdf($"_rawBody", struct(new_df.columns map col: _*)))
           .select("id", "mergedBody")
           .withColumnRenamed("mergedBody", "_rawBody")

merged_df.show(false)

// COMMAND ----------

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID

val writeCfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> "SampleDatabase",
  "spark.cosmos.container" -> "SampleContainer",
  "spark.cosmos.write.strategy" -> "ItemOverwriteIfNotModified",
  "spark.cosmos.write.bulk.enabled" -> "true"
)

merged_df
  .write
  .format("cosmos.oltp")
  .mode("Append")
  .options(writeCfg)
  .save()

// COMMAND ----------


