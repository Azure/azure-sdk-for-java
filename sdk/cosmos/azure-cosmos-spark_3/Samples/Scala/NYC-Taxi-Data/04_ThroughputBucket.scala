// Databricks notebook source
// MAGIC %md
// MAGIC **Secrets**
// MAGIC 
// MAGIC The secrets below  like the Cosmos account key are retrieved from a secret scope. If you don't have defined a secret scope for a Cosmos Account you want to use when going through this sample you can find the instructions on how to create one here:
// MAGIC - Here you can [Create a new secret scope](./#secrets/createScope) for the current Databricks workspace
// MAGIC   - See how you can create an [Azure Key Vault backed secret scope](https://docs.microsoft.com/azure/databricks/security/secrets/secret-scopes#--create-an-azure-key-vault-backed-secret-scope) 
// MAGIC   - See how you can create a [Databricks backed secret scope](https://docs.microsoft.com/azure/databricks/security/secrets/secret-scopes#create-a-databricks-backed-secret-scope)
// MAGIC - And here you can find information on how to [add secrets to your Spark configuration](https://docs.microsoft.com/azure/databricks/security/secrets/secrets#read-a-secret)
// MAGIC If you don't want to use secrets at all you can of course also just assign the values in clear-text below - but for obvious reasons we recommend the usage of secrets.

// COMMAND ----------

val cosmosEndpoint = spark.conf.get("spark.cosmos.accountEndpoint")
val cosmosMasterKey = spark.conf.get("spark.cosmos.accountKey")

// COMMAND ----------

// MAGIC %md
// MAGIC **Preparation - creating the Cosmos DB container to ingest the data into**

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Configure the Catalog API to be used

// COMMAND ----------

spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.views.repositoryPath", "/viewDefinitions")

// COMMAND ----------

// MAGIC %md
// MAGIC And execute the command to create the new container with a throughput of up-to 100,000 RU (Autoscale - so 10,000 - 100,000 RU based on scale) and only system properties (like /id) being indexed.
// MAGIC 
// MAGIC **Note:** Unlike SDK-based throughput control, throughput buckets do NOT require a separate metadata container (ThroughputControl) because they are managed server-side.

// COMMAND ----------

// MAGIC %sql
// MAGIC CREATE DATABASE IF NOT EXISTS cosmosCatalog.SampleDatabase;
// MAGIC 
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.SampleDatabase.GreenTaxiRecords
// MAGIC USING cosmos.oltp
// MAGIC TBLPROPERTIES(partitionKeyPath = '/id', autoScaleMaxThroughput = '100000', indexingPolicy = 'OnlySystemProperties');
// MAGIC 
// MAGIC CREATE TABLE IF NOT EXISTS cosmosCatalog.SampleDatabase.GreenTaxiRecordsCFSink
// MAGIC USING cosmos.oltp
// MAGIC TBLPROPERTIES(partitionKeyPath = '/id', autoScaleMaxThroughput = '100000', indexingPolicy = 'OnlySystemProperties');

// COMMAND ----------

// MAGIC %md
// MAGIC **Preparation - loading data source "[NYC Taxi & Limousine Commission - green taxi trip records](https://azure.microsoft.com/services/open-datasets/catalog/nyc-taxi-limousine-commission-green-taxi-trip-records/)"**
// MAGIC 
// MAGIC The green taxi trip records include fields capturing pick-up and drop-off dates/times, pick-up and drop-off locations, trip distances, itemized fares, rate types, payment types, and driver-reported passenger counts. This data set has over 80 million records (>8 GB) of data and is available via a publicly accessible Azure Blob Storage Account located in the East-US Azure region.

// COMMAND ----------

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID

val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

println(s"Starting preparation: ${formatter.format(Instant.now)}")

// Azure storage access info
val blob_account_name = "azureopendatastorage"
val blob_container_name = "nyctlc"
val blob_relative_path = "green"
val blob_sas_token = ""
// Allow SPARK to read from Blob remotely
val wasbs_path = s"wasbs://${blob_container_name}@${blob_account_name}.blob.core.windows.net/${blob_relative_path}"
spark.conf.set(
  s"fs.azure.sas.${blob_container_name}.$blob_account_name{}.blob.core.windows.net",
  blob_sas_token)
print(s"Remote blob path: ${wasbs_path}")
// SPARK read parquet, note that it won't load any data yet by now
// NOTE - if you want to experiment with larger dataset sizes - consider switching to Option B (commenting code 
// for Option A/uncommenting code for option B) the lines below or increase the value passed into the 
// limit function restricting the dataset size below

// ------------------------------------------------------------------------------------
//  Option A - with limited dataset size
// ------------------------------------------------------------------------------------
val df_rawInputWithoutLimit = spark.read.parquet(wasbs_path)
val partitionCount = df_rawInputWithoutLimit.rdd.getNumPartitions
val df_rawInput = df_rawInputWithoutLimit.limit(1000 * 1000).repartition(partitionCount)
df_rawInput.persist()

// ------------------------------------------------------------------------------------
// Option B - entire dataset
// ------------------------------------------------------------------------------------
// val df_rawInput = spark.read.parquet(wasbs_path)

// Adding an id column with unique values
val uuidUdf=udf[String](() => UUID.randomUUID().toString)
val df_input_withId = df_rawInput.withColumn("id", uuidUdf())

print("Register the DataFrame as a SQL temporary view: source")
df_input_withId.createOrReplaceTempView("source")
print("Finished preparation: ${formatter.format(Instant.now)}")

// COMMAND ----------

// MAGIC %md
// MAGIC ** Sample - ingesting the NYC Green Taxi data into Cosmos DB using throughput bucket**
// MAGIC 
// MAGIC Throughput buckets provide server-side throughput control. Instead of using the SDK-based global throughput control
// MAGIC (which requires a separate metadata container), you configure a `throughputBucket` value between 1 and 5.
// MAGIC 
// MAGIC This is simpler to configure because it does not require a separate throughput control metadata container.
// MAGIC For more information, see [Throughput Buckets](https://learn.microsoft.com/azure/cosmos-db/throughput-buckets?tabs=dotnet).

// COMMAND ----------

println(s"Starting ingestion: ${formatter.format(Instant.now)}")

val writeCfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> "SampleDatabase",
  "spark.cosmos.container" -> "GreenTaxiRecords",
  "spark.cosmos.write.strategy" -> "ItemOverwrite",
  "spark.cosmos.write.bulk.enabled" -> "true",
  "spark.cosmos.throughputControl.enabled" -> "true",
  "spark.cosmos.throughputControl.name" -> "NYCGreenTaxiDataIngestion",
  "spark.cosmos.throughputControl.throughputBucket" -> "5",
)

val df_NYCGreenTaxi_Input = spark.sql("SELECT * FROM source")

df_NYCGreenTaxi_Input
  .write
  .format("cosmos.oltp")
  .mode("Append")
  .options(writeCfg)
  .save()

println(s"Finished ingestion: ${formatter.format(Instant.now)}")

// COMMAND ----------

// MAGIC %md
// MAGIC **Getting the reference record count**

// COMMAND ----------

val count_source = spark.sql("SELECT * FROM source").count()
println(s"Number of records in source: ${count_source}") 

// COMMAND ----------

// MAGIC %md
// MAGIC **Sample - validating the record count via query**

// COMMAND ----------

import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

println(s"Starting validation via query: ${formatter.format(Instant.now)}")
val readCfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> "SampleDatabase",
  "spark.cosmos.container" -> "GreenTaxiRecords",
  "spark.cosmos.read.partitioning.strategy" -> "Restrictive", // IMPORTANT - any other partitioning strategy will result in indexing not being use to count - so latency and RU would spike up
  "spark.cosmos.read.inferSchema.enabled" -> "false",
  "spark.cosmos.read.customQuery" -> "SELECT COUNT(0) AS Count FROM c"
)

val count_query_schema=StructType(Array(StructField("Count", LongType, true)))
val query_df = spark.read.format("cosmos.oltp").schema(count_query_schema).options(readCfg).load()
val count_query = query_df.agg(sum("Count").as("TotalCount")).first.getLong(0)
println(s"Number of records retrieved via query: ${count_query}") 
println(s"Finished validation via query: ${formatter.format(Instant.now)}")

assert(count_source == count_query)

// COMMAND ----------

// MAGIC %md
// MAGIC **Sample - validating the record count via change feed**

// COMMAND ----------

println(s"Starting validation via change feed: ${formatter.format(Instant.now)}")
val changeFeedCfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> "SampleDatabase",
  "spark.cosmos.container" -> "GreenTaxiRecords",
  "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
  "spark.cosmos.read.inferSchema.enabled" -> "false",
  "spark.cosmos.changeFeed.startFrom" -> "Beginning",
  "spark.cosmos.changeFeed.mode" -> "Incremental"
)
val changeFeed_df = spark.read.format("cosmos.oltp.changeFeed").options(changeFeedCfg).load()
val count_changeFeed = changeFeed_df.count()
println(s"Number of records retrieved via change feed: ${count_changeFeed}") 
println(s"Finished validation via change feed: ${formatter.format(Instant.now)}")

assert(count_source == count_changeFeed)

// COMMAND ----------

// MAGIC %md
// MAGIC **Sample - bulk deleting documents with throughput bucket and validating document count afterwards**

// COMMAND ----------

import scala.math._

println(s"Starting to identify to be deleted documents: ${formatter.format(Instant.now)}")
val readCfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> "SampleDatabase",
  "spark.cosmos.container" -> "GreenTaxiRecords",
  "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
  "spark.cosmos.read.inferSchema.enabled" -> "false",
)

val toBeDeleted_df = spark.read.format("cosmos.oltp").options(readCfg).load().limit(100000)
println(s"Number of records to be deleted: ${toBeDeleted_df.count}") 

println(s"Starting to bulk delete documents: ${formatter.format(Instant.now)}")
val deleteCfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> "SampleDatabase",
  "spark.cosmos.container" -> "GreenTaxiRecords",
  "spark.cosmos.write.strategy" -> "ItemDelete",
  "spark.cosmos.write.bulk.enabled" -> "true",
  "spark.cosmos.throughputControl.enabled" -> "true",
  "spark.cosmos.throughputControl.name" -> "NYCGreenTaxiDataDelete",
  "spark.cosmos.throughputControl.throughputBucket" -> "1",
)
toBeDeleted_df
        .write
        .format("cosmos.oltp")
        .mode("Append")
        .options(deleteCfg)
        .save()
println(s"Finished deleting documents: ${formatter.format(Instant.now)}")

println(s"Starting count validation via query: ${formatter.format(Instant.now)}")
val countCfg = readCfg + ("spark.cosmos.read.customQuery" -> "SELECT COUNT(0) AS Count FROM c")
val count_query_schema=StructType(Array(StructField("Count", LongType, true)))
val query_df = spark.read.format("cosmos.oltp").schema(count_query_schema).options(countCfg).load()
val count_query = query_df.agg(sum("Count").as("TotalCount")).first.getLong(0)
println(s"Number of records retrieved via query: ${count_query}") 
println(s"Finished count validation via query: ${formatter.format(Instant.now)}")

assert (math.max(0, count_source - 100000) == count_query)

// COMMAND ----------

// MAGIC %md
// MAGIC **Sample - showing the existing Containers**

// COMMAND ----------

// MAGIC %sql
// MAGIC SHOW TABLES FROM cosmosCatalog.SampleDatabase

// COMMAND ----------

val df_Tables = spark.sql("SHOW TABLES FROM cosmosCatalog.SampleDatabase")
assert(df_Tables.count() == 2)
