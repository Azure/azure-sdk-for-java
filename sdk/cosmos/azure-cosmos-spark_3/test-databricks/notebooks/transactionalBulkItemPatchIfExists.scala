// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val cosmosMasterKey = "<inserted by environment>"

println("SCENARIO: transactionalBulkItemPatchIfExists")

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val transactionalContainerName = s"${cosmosContainerName}_txnBulkItemPatchIfExists"

val cfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> transactionalContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "true",
  "spark.cosmos.enforceNativeTransport" -> "true",
  "spark.cosmos.read.consistencyStrategy" -> "LatestCommitted"
)

val transactionalPatchCfg = cfg ++ Map(
  "spark.cosmos.write.strategy" -> "ItemPatchIfExists",
  "spark.cosmos.write.bulk.enabled" -> "true",
  "spark.cosmos.write.bulk.transactional" -> "true",
  "spark.cosmos.write.patch.defaultOperationType" -> "Set",
  "spark.cosmos.write.patch.columnConfigs" -> "[col(name).op(set)]"
)

def appendRows(rows: Seq[(String, String, String)], options: Map[String, String]): Unit = {
  spark.createDataFrame(rows)
    .toDF("id", "pk", "name")
    .write
    .format("cosmos.oltp")
    .options(options)
    .mode("APPEND")
    .save()
}

def readRowsForPks(pks: Seq[String]) = {
  val pkFilter = pks.map(pk => s"'$pk'").mkString(",")
  spark.read
    .format("cosmos.oltp")
    .options(cfg)
    .load()
    .filter(s"pk IN ($pkFilter)")
    .select("id", "pk", "name")
    .orderBy("pk", "id")
}

def toKeyedNameMap(df: org.apache.spark.sql.DataFrame): Map[String, String] = {
  df.collect().map { row =>
    s"${row.getAs[String]("pk")}|${row.getAs[String]("id")}" -> row.getAs[String]("name")
  }.toMap
}

// COMMAND ----------

spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)

spark.sql(s"CREATE DATABASE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName};")
spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
spark.sql(
  s"CREATE TABLE IF NOT EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName} " +
    s"using cosmos.oltp TBLPROPERTIES(partitionKeyPath = '/pk', manualThroughput = '400')")

// COMMAND ----------

try {
  // Seed documents that should be patched.
  appendRows(Seq(
    ("patch-existing-1", "pk-P", "BeforePatch-1"),
    ("patch-existing-2", "pk-P", "BeforePatch-2"),
    ("patch-existing-3", "pk-Q", "BeforePatch-3")
  ), cfg)

  // Scenario 1: Patch existing docs and skip missing doc in same strategy.
  appendRows(Seq(
    ("patch-existing-1", "pk-P", "AfterPatch-1"),
    ("patch-missing-1", "pk-P", "ShouldNotBeCreated"),
    ("patch-existing-3", "pk-Q", "AfterPatch-3")
  ), transactionalPatchCfg)

  val scenario1 = readRowsForPks(Seq("pk-P", "pk-Q"))
  scenario1.show(false)
  val scenario1Map = toKeyedNameMap(scenario1)
  assert(scenario1.count() == 3, s"Scenario 1 expected 3 docs, got ${scenario1.count()}")
  assert(scenario1Map.get("pk-P|patch-existing-1").contains("AfterPatch-1"))
  assert(scenario1Map.get("pk-P|patch-existing-2").contains("BeforePatch-2"))
  assert(!scenario1Map.contains("pk-P|patch-missing-1"), "Missing patch target should not be created")
  assert(scenario1Map.get("pk-Q|patch-existing-3").contains("AfterPatch-3"))
  println("PASS: Scenario 1 (patch existing, skip missing)")

  // Scenario 2: Re-patch same item to validate deterministic updates.
  appendRows(Seq(
    ("patch-existing-1", "pk-P", "AfterPatch-1-Second")
  ), transactionalPatchCfg)

  val scenario2 = readRowsForPks(Seq("pk-P"))
  scenario2.show(false)
  val scenario2Map = toKeyedNameMap(scenario2)
  assert(scenario2Map.get("pk-P|patch-existing-1").contains("AfterPatch-1-Second"))
  println("PASS: Scenario 2 (repeat patch updates same target)")

  println("PASS: transactional ItemPatchIfExists scenarios completed.")
} finally {
  spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
}

