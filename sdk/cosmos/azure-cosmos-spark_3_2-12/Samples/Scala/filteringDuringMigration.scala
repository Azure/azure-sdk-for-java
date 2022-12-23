// Databricks notebook source
import org.apache.spark.sql.functions.{split}

val cosmosEndpoint = "<cosmos-endpoint>"
val cosmosMasterKey = "<cosmos-master-key>"
val cosmosDatabaseName = "ContosoHospital"
val cosmosContainerName = "Patient"


// Patient Document -- partition key: patientId
// {
//   "id": "9c9a1156-e936-40f3-a442-e9528b55a2fb",
//   "patientId": "423ab2cf-dd1c-4404-8524-86cee045c179",
//   "patientName": "John Doe",
//   "doctorId" : "629f49da-9cfc-45a4-8e1c-d4f8b7ab1f4e",
//   "doctorName": "Sung Ondricka"
// }


//-----filtering examples-with-schema-inference-disabled---------------------

val targetContainerName = "CopyContainer"
val checkpointLocation = "/tmp/streaming_checkpoint"

val changeFeedCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "false",   
  "spark.cosmos.changeFeed.startFrom" -> "Beginning",
  "spark.cosmos.changeFeed.mode" -> "Incremental",
  "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "100000"
  //optional configuration for throughput control
  // "spark.cosmos.throughputControl.enabled" -> "true",
  // "spark.cosmos.throughputControl.name" -> "SourceContainerThroughputControl",
  // "spark.cosmos.throughputControl.targetThroughputThreshold" -> "0.30", 
  // "spark.cosmos.throughputControl.globalControl.database" -> "database-v4", 
  // "spark.cosmos.throughputControl.globalControl.container" -> "ThroughputControl"
)

val writeCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> targetContainerName,
  "checkpointLocation" -> checkpointLocation
)

//optional configuration for creating throughput control metadata container

// spark.conf.set(s"spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
// spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
// spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)


// create an Azure Cosmos DB container using catalog api
// spark.sql("CREATE DATABASE IF NOT EXISTS cosmosCatalog.`database-v4`")
// spark.sql(s"CREATE TABLE IF NOT EXISTS cosmosCatalog.`database-v4`.`ThroughputControl` using cosmos.oltp TBLPROPERTIES(partitionKeyPath = '/groupId', autoScaleMaxThroughput  = '4000')")



// ----- EXAMPLE 1 -----


val changeFeedDF = spark.readStream.format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)  
      .load

// Here is an example of splitting the id column by "-" and creating a new column with only the second item
// e.g. Id = XXXXX-SubIdWeAreInterestedIn-XXXXX
val splitByDash = changeFeedDF.select($"*", split($"id","-").alias("idSplit"))
val withSplitId = splitByDash.withColumn("newId", $"idSplit".getItem(1))

// Filter by newId to get only the columns we want to migrate, then drop helper columns
val filteredByNewId = withSplitId.filter($"newId" === 7926).drop("idSplit").drop("newId")

// preserve system properties like _ts, _etag by renaming the original column
val df_withAuditFields = filteredByNewId.withColumnRenamed("_rawbody", "_origin_rawBody")

// write streaming dataframe to the target container
df_withAuditFields
  .writeStream
  .format("cosmos.oltp")
  .options(writeCfg)
  .start()
  .awaitTermination()


// ----- EXAMPLE 2 -----
// The following example uses filter and string matching to parse out rows where doctorId values match a given value, without the need for the spark.read.json feature or any joins

val changeFeedDF = spark.readStream.format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)  
      .load

// Filter by an example patientId we are interested in from raw document into a new df
val filteredDf = changeFeedDF.filter(col("_rawBody").contains("\"doctorId\":\"9c9a1156-e936-40f3-a442-e9528b55a2fb\""))

// preserve system properties like _ts, _etag by renaming the original column
val df_withAuditFields = filteredDf.withColumnRenamed("_rawbody", "_origin_rawBody")

// write streaming dataframe to the target container
df_withAuditFields
  .writeStream
  .format("cosmos.oltp")
  .options(writeCfg)
  .start()
  .awaitTermination()


// ----- EXAMPLE 3 ----- 
//In this example we will write data into a container with different partition key from source container

val targetContainerName = "CopyWithDoctorId"
val checkpointLocation = "/tmp/pk_doctorId_checkpoint"

// Configure Catalog Api to be used
spark.conf.set(s"spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)


// create an Azure Cosmos DB container using catalog api
spark.sql(s"CREATE TABLE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} using cosmos.oltp TBLPROPERTIES(partitionKeyPath = '/doctorId', manualThroughput = '1100')")

val writeCfgWithNewPK = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> targetContainerName,
  "checkpointLocation" -> checkpointLocation
)

// read streaming data from changeFeed
val changeFeedDF = spark.readStream.format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)  
      .load

// write streaming data into new container with different PK
changeFeedDF
  .writeStream
  .format("cosmos.oltp")
  .options(writeCfgWithNewPK)
  .start()
  .awaitTermination()
