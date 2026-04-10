// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val cosmosMasterKey = "<inserted by environment>"

println("SCENARIO: transactionalBulkItemPatch")

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val transactionalContainerName = s"${cosmosContainerName}_txnBulkItemPatch"

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
  "spark.cosmos.write.strategy" -> "ItemPatch",
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

def collectCauseMessages(ex: Throwable): String = {
  Iterator.iterate(ex)(_.getCause)
    .takeWhile(_ != null)
    .map(cause => Option(cause.getMessage).getOrElse(""))
    .mkString(" | ")
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
  // Scenario 1: patch existing docs successfully.
  appendRows(Seq(
    ("patch-existing-1", "pk-A", "BeforePatch-1"),
    ("patch-existing-2", "pk-A", "BeforePatch-2")
  ), cfg)

  appendRows(Seq(
    ("patch-existing-1", "pk-A", "AfterPatch-1"),
    ("patch-existing-2", "pk-A", "AfterPatch-2")
  ), transactionalPatchCfg)

  val scenario1 = readRowsForPks(Seq("pk-A"))
  scenario1.show(false)
  val scenario1Map = scenario1.collect().map { r =>
    s"${r.getAs[String]("pk")}|${r.getAs[String]("id")}" -> r.getAs[String]("name")
  }.toMap

  assert(scenario1Map.get("pk-A|patch-existing-1").contains("AfterPatch-1"))
  assert(scenario1Map.get("pk-A|patch-existing-2").contains("AfterPatch-2"))
  println("PASS: Scenario 1 (patch existing docs)")

  // Scenario 2: patch missing doc should fail for ItemPatch (404).
  var sawMissingFailure = false
  try {
    appendRows(Seq(
      ("patch-missing-1", "pk-B", "ShouldFail")
    ), transactionalPatchCfg)
  } catch {
    case ex: Exception =>
      val chain = collectCauseMessages(ex)
      sawMissingFailure = chain.contains("404") || chain.toLowerCase.contains("not found")
      if (!sawMissingFailure) {
        throw ex
      }
  }

  assert(sawMissingFailure, "Expected failure when patching a missing document with ItemPatch")
  val scenario2 = readRowsForPks(Seq("pk-B"))
  scenario2.show(false)
  assert(scenario2.count() == 0, s"Scenario 2 expected 0 docs in pk-B, got ${scenario2.count()}")
  println("PASS: Scenario 2 (patch missing doc fails)")

  // Scenario 3: predicate filter failure should keep docs unchanged for same-partition batch.
  appendRows(Seq(
    ("patch-filter-blocked", "pk-C", "Blocked"),
    ("patch-filter-allowed", "pk-C", "Allowed")
  ), cfg)

  val transactionalPatchWithFilterCfg = transactionalPatchCfg ++ Map(
    "spark.cosmos.write.patch.filter" -> "from c where c.name = 'Allowed'"
  )

  var sawPredicateFailure = false
  try {
    appendRows(Seq(
      ("patch-filter-blocked", "pk-C", "BlockedAfter"),
      ("patch-filter-allowed", "pk-C", "AllowedAfter")
    ), transactionalPatchWithFilterCfg)
  } catch {
    case ex: Exception =>
      val chain = collectCauseMessages(ex)
      sawPredicateFailure = chain.contains("412") || chain.toLowerCase.contains("precondition")
      if (!sawPredicateFailure) {
        throw ex
      }
  }

  assert(sawPredicateFailure, "Expected predicate failure for blocked item")

  val scenario3 = readRowsForPks(Seq("pk-C"))
  scenario3.show(false)
  val scenario3Map = scenario3.collect().map { r =>
    s"${r.getAs[String]("pk")}|${r.getAs[String]("id")}" -> r.getAs[String]("name")
  }.toMap

  assert(scenario3Map.get("pk-C|patch-filter-blocked").contains("Blocked"))
  assert(scenario3Map.get("pk-C|patch-filter-allowed").contains("Allowed"))
  println("PASS: Scenario 3 (predicate failure leaves docs unchanged)")

  println("PASS: transactional ItemPatch scenarios completed.")
} finally {
  spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
}

