// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val cosmosMasterKey = "<inserted by environment>"

println("SCENARIO: transactionalBulkItemDelete")

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val transactionalContainerName = s"${cosmosContainerName}_txnBulkItemDelete"

val cfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> transactionalContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "true",
  "spark.cosmos.enforceNativeTransport" -> "true",
  "spark.cosmos.read.consistencyStrategy" -> "LatestCommitted"
)

val transactionalDeleteCfg = cfg ++ Map(
  "spark.cosmos.write.strategy" -> "ItemDelete",
  "spark.cosmos.write.bulk.enabled" -> "true",
  "spark.cosmos.write.bulk.transactional" -> "true"
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
  // Scenario 1: Delete existing docs in one partition.
  appendRows(Seq(
    ("delete-1", "pk-A", "ToDelete-1"),
    ("delete-2", "pk-A", "ToDelete-2"),
    ("delete-3", "pk-A", "ToDelete-3")
  ), cfg)

  appendRows(Seq(
    ("delete-1", "pk-A", "placeholder"),
    ("delete-2", "pk-A", "placeholder"),
    ("delete-3", "pk-A", "placeholder")
  ), transactionalDeleteCfg)

  val scenario1 = readRowsForPks(Seq("pk-A"))
  scenario1.show(false)
  assert(scenario1.count() == 0, s"Scenario 1 expected 0 docs, got ${scenario1.count()}")
  println("PASS: Scenario 1 (delete existing docs)")

  // Scenario 2: Missing doc delete should be ignored; subsequent valid delete should succeed.
  appendRows(Seq(
    ("delete-existing-1", "pk-B", "Existing")
  ), cfg)

  appendRows(Seq(
    ("delete-missing-1", "pk-B", "Missing")
  ), transactionalDeleteCfg)

  appendRows(Seq(
    ("delete-existing-1", "pk-B", "placeholder")
  ), transactionalDeleteCfg)

  val scenario2 = readRowsForPks(Seq("pk-B"))
  scenario2.show(false)
  assert(scenario2.count() == 0, s"Scenario 2 expected 0 docs, got ${scenario2.count()}")
  println("PASS: Scenario 2 (missing delete ignored, existing delete succeeds)")

  // Scenario 3: Delete across multiple partition keys.
  appendRows(Seq(
    ("delete-pk-c-1", "pk-C", "Doc-C"),
    ("delete-pk-d-1", "pk-D", "Doc-D")
  ), cfg)

  appendRows(Seq(
    ("delete-pk-c-1", "pk-C", "placeholder"),
    ("delete-pk-d-1", "pk-D", "placeholder")
  ), transactionalDeleteCfg)

  val scenario3 = readRowsForPks(Seq("pk-C", "pk-D"))
  scenario3.show(false)
  assert(scenario3.count() == 0, s"Scenario 3 expected 0 docs, got ${scenario3.count()}")
  println("PASS: Scenario 3 (delete across partition keys)")

  println("PASS: transactional ItemDelete scenarios completed.")
} finally {
  spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
}

