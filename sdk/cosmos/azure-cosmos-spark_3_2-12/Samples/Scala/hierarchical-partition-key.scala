// Databricks notebook source
import com.azure.cosmos.spark.udf.{GetFeedRangeForHierarchicalPartitionKeyValues}
import org.apache.spark.sql.types._

val cosmosEndpoint = "https://REPLACEME.documents.azure.com:443/"
val cosmosMasterKey = "REPLACEME"
val cosmosDatabaseName = "sampleDB"
val cosmosContainerName = "sampleContainer"

val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName
)

// Configure Catalog Api to be used
spark.conf.set(s"spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)

// create an Azure Cosmos DB database using catalog api
spark.sql(s"CREATE DATABASE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName};")

//create an Azure Cosmos DB container with hierarchical partitioning using catalog api
spark.sql(s"CREATE TABLE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} using cosmos.oltp TBLPROPERTIES(partitionKeyPath = '/tenantId,/userId,/sessionId', manualThroughput = '1100')")

// COMMAND ----------

//ingest some data
val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName,
  "spark.cosmos.read.partitioning.strategy" -> "Restrictive" 
)
spark.createDataFrame(Seq(("id1", "tenant 1", "User 1", "session 1"), ("id2", "tenant 1", "User 1", "session 1"), ("id3", "tenant 2", "User 1", "session 1")))
  .toDF("id","tenantId","userId","sessionId")
   .write
   .format("cosmos.oltp")
   .options(cfg)
   .mode("APPEND")
   .save()

// COMMAND ----------

//query by filtering the first two levels in the hierarchy without feedRangeFilter - this is less efficient as it will go through all physical partitions
val query1 = cfg + ("spark.cosmos.read.customQuery" -> "SELECT * from c where c.tenantId = 'tenant 1' and c.userId = 'User 1'")
val query_df1 = spark.read.format("cosmos.oltp").options(query1).load()
query_df1.show

// COMMAND ----------

//prepare feed range filter to filter on first two levels in the hierarchy
spark.udf.register("GetFeedRangeForPartitionKey", new GetFeedRangeForHierarchicalPartitionKeyValues(), StringType)
val pkDefinition = "{\"paths\":[\"/tenantId\",\"/userId\",\"/sessionId\"],\"kind\":\"MultiHash\"}"
val pkValues = "[\"tenant 1\", \"User 1\"]"
val feedRangeDf = spark.sql(s"SELECT GetFeedRangeForPartitionKey('$pkDefinition', '$pkValues')")
val feedRange = feedRangeDf.collect()(0).getAs[String](0)

//filtering the first two levels in the hierarchy using feedRangeFilter (will target the physical partition in which all sub-partitions are co-located)
val query2 = cfg + ("spark.cosmos.partitioning.feedRangeFilter" -> feedRange)
val query_df2 = spark.read.format("cosmos.oltp").options(query2).load()
query_df2.show
