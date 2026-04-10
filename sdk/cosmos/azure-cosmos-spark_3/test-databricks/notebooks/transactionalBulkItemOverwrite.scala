// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val cosmosMasterKey = "<inserted by environment>"

println("SCENARIO: transactionalBulkItemOverwrite")

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val transactionalContainerName = s"${cosmosContainerName}_txnBulkItemOverwrite"

val cfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> transactionalContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "true",
  "spark.cosmos.enforceNativeTransport" -> "true"
)

val transactionalWriteCfg = cfg ++ Map(
  "spark.cosmos.write.strategy" -> "ItemOverwrite",
  "spark.cosmos.write.bulk.enabled" -> "true",
  "spark.cosmos.write.bulk.transactional" -> "true"
)

// COMMAND ----------

// Create an isolated transactional test container with /pk partition key.
spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)

spark.sql(s"CREATE DATABASE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName};")
spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
spark.sql(
  s"CREATE TABLE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName} " +
    s"using cosmos.oltp TBLPROPERTIES(partitionKeyPath = '/pk', manualThroughput = '400')")

// COMMAND ----------

// Seed documents that should be replaced by ItemOverwrite.
spark.createDataFrame(Seq(
  ("existing-1", "pk-A", "Alice-old"),
  ("existing-2", "pk-A", "Bob-old")
)).toDF("id", "pk", "name")
  .write
  .format("cosmos.oltp")
  .options(cfg)
  .mode("APPEND")
  .save()

// COMMAND ----------

// Transactional bulk write with mixed existing and new items.
spark.createDataFrame(Seq(
  ("existing-1", "pk-A", "Alice-updated"),
  ("existing-2", "pk-A", "Bob-updated"),
  ("new-3", "pk-A", "Charlie"),
  ("new-4", "pk-B", "Diana"),
  ("new-5", "pk-B", "Eve")
)).toDF("id", "pk", "name")
  .write
  .format("cosmos.oltp")
  .options(transactionalWriteCfg)
  .mode("APPEND")
  .save()

// COMMAND ----------

val resultDf = spark.read
  .format("cosmos.oltp")
  .options(cfg)
  .load()
  .filter("pk = 'pk-A' OR pk = 'pk-B'")
  .select("id", "pk", "name")
  .orderBy("pk", "id")

resultDf.show(false)

val count = resultDf.count()
assert(count == 5, s"Expected 5 documents, got $count")

val namesById = resultDf.collect().map(row => row.getAs[String]("id") -> row.getAs[String]("name")).toMap
assert(namesById.get("existing-1").contains("Alice-updated"),
  s"existing-1 should be Alice-updated, got ${namesById.get("existing-1")}")
assert(namesById.get("existing-2").contains("Bob-updated"),
  s"existing-2 should be Bob-updated, got ${namesById.get("existing-2")}")
assert(namesById.get("new-3").contains("Charlie"),
  s"new-3 should be Charlie, got ${namesById.get("new-3")}")
assert(namesById.get("new-4").contains("Diana"),
  s"new-4 should be Diana, got ${namesById.get("new-4")}")
assert(namesById.get("new-5").contains("Eve"),
  s"new-5 should be Eve, got ${namesById.get("new-5")}")

println(s"PASS: transactional ItemOverwrite validated with $count documents.")

// COMMAND ----------

spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
