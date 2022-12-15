// Databricks notebook source
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

val TargetContainerName = "CopyContainer"
val checkpointLocation = "/tmp/streaming_checkpoint"

val changeFeedCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "false",   
  "spark.cosmos.changeFeed.startFrom" -> "Beginning",
  "spark.cosmos.changeFeed.mode" -> "Incremental",
  "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "10000"
)

val writeCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> TargetContainerName,
  "checkpointLocation" -> checkpointLocation
)

// ----- EXAMPLE 1 -----


val changeFeedDF = spark.readStream.format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)  
      .load

println("Streaming DataFrame : " + changeFeedDF.isStreaming)


import org.apache.spark.sql.functions.{split}
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
// The following example uses spark.read.json, but uses a non streaming datasource to do so. Streaming sources do not support joins

val changeFeedDF = spark.read.format("cosmos.oltp")
      .options(changeFeedCfg)  
      .load
// parse the ids of records with a given doctorId value we are interested in to a new df
val ids = spark.read.json(changeFeedDF.select("_rawBody").as[String]).filter("doctorId=='5b15f027-74d1-4ab8-9ad3-cca848837f66'").select("id")

// join these two dfs together on id to isolate only the records we are interested in
val df_withAuditFields = changeFeedDF.join(ids, "id").withColumnRenamed("_rawbody", "_origin_rawBody")

df_withAuditFields
  .write
  .format("cosmos.oltp")
  .options(writeCfg)
  .mode("append")
  .save()


// ----- EXAMPLE 3 -----
// The following example uses split by logic to parse out rows where json property values match a given value, without the need for the spark.read.json feature or any joins

import org.apache.spark.sql.functions.{split}

val changeFeedDF = spark.readStream.format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)  
      .load

// First split by an example patientId we are interested in, splitting on the text 'patientId": ' and create a new column with all text following patientId": , 
val splitByPatientId = changeFeedDF.select($"*", split($"_rawBody","patientId\":").alias("json"))

// Create a new column with the remaining text after the expression we split by
val withJsonAfterPatientId = splitByPatientId.withColumn("jsonAfterPatientId", $"json".getItem(1))


// Then split by "," and create a new column with all text preceding "," to get a column with only the values of the patientId we are interested in
val splitByComma = withJsonAfterPatientId.select($"*", split($"jsonAfterPatientId",",").alias("jsonSplitByComma"))

val withValueColumn = splitByComma.withColumn("newColumn", $"jsonSplitByComma".getItem(0))

// Filter by items matching a given patientId's value to get only the columns we want to migrate, and drop helper columns
val filteredByPatientIdValue = withValueColumn.filter($"newColumn" === "\"9c9a1156-e936-40f3-a442-e9528b55a2fb\"").drop("json").drop("jsonAfterPatientId").drop("jsonSplitByComma").drop("newColumn")

val df_withAuditFields = filteredByPatientIdValue.withColumnRenamed("_rawbody", "_origin_rawBody")

// write streaming dataframe to the target container
df_withAuditFields
  .writeStream
  .format("cosmos.oltp")
  .options(writeCfg)
  .start()
  .awaitTermination()


// ----- EXAMPLE 4 ----- 
//In this example we will write data into a container with different partition key from source container

val TargetContainerName = "CopyWithDoctorId"

// Configure Catalog Api to be used
spark.conf.set(s"spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)


// create an Azure Cosmos DB container using catalog api
spark.sql(s"CREATE TABLE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} using cosmos.oltp TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '1100')")

val writeCfgWithNewPK = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> TargetContainerName,
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
