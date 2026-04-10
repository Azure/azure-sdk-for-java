// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val cosmosMasterKey = "<inserted by environment>"

println("SCENARIO: transactionalBulkItemAppend")

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val transactionalContainerName = s"${cosmosContainerName}_txnBulkItemAppend"

val cfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> transactionalContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "true",
  "spark.cosmos.enforceNativeTransport" -> "true",
  "spark.cosmos.read.consistencyStrategy" -> "LatestCommitted"
)

val transactionalWriteCfg = cfg ++ Map(
  "spark.cosmos.write.strategy" -> "ItemAppend",
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
  // Scenario 1: Create-only insert for new ids across multiple partitions.
  appendRows(Seq(
    ("append-new-1", "pk-A", "A1"),
    ("append-new-2", "pk-A", "A2"),
    ("append-new-3", "pk-B", "B1")
  ), transactionalWriteCfg)

  val scenario1 = readRowsForPks(Seq("pk-A", "pk-B"))
  scenario1.show(false)
  val scenario1Map = toKeyedNameMap(scenario1)
  assert(scenario1.count() == 3, s"Scenario 1 expected 3 docs, got ${scenario1.count()}")
  assert(scenario1Map.get("pk-A|append-new-1").contains("A1"))
  assert(scenario1Map.get("pk-A|append-new-2").contains("A2"))
  assert(scenario1Map.get("pk-B|append-new-3").contains("B1"))
  println("PASS: Scenario 1 (create-only new docs)")

  // Scenario 2: Conflict reconstruction on existing id should keep existing item unchanged and create new item.
  appendRows(Seq(
    ("append-existing-1", "pk-C", "Original")
  ), cfg)

  appendRows(Seq(
    ("append-existing-1", "pk-C", "ShouldBeIgnored"),
    ("append-new-4", "pk-C", "Created")
  ), transactionalWriteCfg)

  val scenario2 = readRowsForPks(Seq("pk-C"))
  scenario2.show(false)
  val scenario2Map = toKeyedNameMap(scenario2)
  assert(scenario2.count() == 2, s"Scenario 2 expected 2 docs, got ${scenario2.count()}")
  assert(scenario2Map.get("pk-C|append-existing-1").contains("Original"))
  assert(scenario2Map.get("pk-C|append-new-4").contains("Created"))
  println("PASS: Scenario 2 (409 conflict ignored, new doc still created)")

  // Scenario 3: Same id under different partition keys is allowed.
  appendRows(Seq(
    ("append-shared-id", "pk-D", "D-value"),
    ("append-shared-id", "pk-E", "E-value")
  ), transactionalWriteCfg)

  val scenario3 = readRowsForPks(Seq("pk-D", "pk-E"))
  scenario3.show(false)
  val scenario3Map = toKeyedNameMap(scenario3)
  assert(scenario3.count() == 2, s"Scenario 3 expected 2 docs, got ${scenario3.count()}")
  assert(scenario3Map.get("pk-D|append-shared-id").contains("D-value"))
  assert(scenario3Map.get("pk-E|append-shared-id").contains("E-value"))
  println("PASS: Scenario 3 (same id across different pks)")

  println("PASS: transactional ItemAppend scenarios completed.")
} finally {
  spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
}

