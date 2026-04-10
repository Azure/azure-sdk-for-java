// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val cosmosMasterKey = "<inserted by environment>"

println("SCENARIO: transactionalBulkItemOverwriteIfNotModified")

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val transactionalContainerName = s"${cosmosContainerName}_txnBulkItemOverwriteIfNotModified"

val cfg = Map(
  "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> transactionalContainerName,
  "spark.cosmos.read.inferSchema.enabled" -> "true",
  "spark.cosmos.enforceNativeTransport" -> "true",
  "spark.cosmos.read.consistencyStrategy" -> "LatestCommitted"
)

val overwriteCfg = cfg ++ Map(
  "spark.cosmos.write.strategy" -> "ItemOverwrite",
  "spark.cosmos.write.bulk.enabled" -> "true",
  "spark.cosmos.write.bulk.transactional" -> "true"
)

val transactionalOverwriteIfNotModifiedCfg = cfg ++ Map(
  "spark.cosmos.write.strategy" -> "ItemOverwriteIfNotModified",
  "spark.cosmos.write.bulk.enabled" -> "true",
  "spark.cosmos.write.bulk.transactional" -> "true"
)

val overwriteIfNotModifiedSchema = StructType(Seq(
  StructField("id", StringType, nullable = false),
  StructField("pk", StringType, nullable = false),
  StructField("name", StringType, nullable = false),
  StructField("_etag", StringType, nullable = true)
))

def appendRows(rows: Seq[(String, String, String)], options: Map[String, String]): Unit = {
  spark.createDataFrame(rows)
    .toDF("id", "pk", "name")
    .write
    .format("cosmos.oltp")
    .options(options)
    .mode("APPEND")
    .save()
}

def writeRowsWithSchema(rows: Seq[Row], schema: StructType, options: Map[String, String]): Unit = {
  spark.createDataFrame(spark.sparkContext.parallelize(rows), schema)
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
    .select("id", "pk", "name", "_etag")
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
  // Seed docs for stale/current ETag checks.
  appendRows(Seq(
    ("ifnm-stale", "pk-A", "BeforeUpdate"),
    ("ifnm-current", "pk-A", "Current")
  ), cfg)

  val etagsBefore = readRowsForPks(Seq("pk-A"))
    .select("id", "_etag")
    .collect()
    .map(r => r.getAs[String]("id") -> r.getAs[String]("_etag"))
    .toMap

  // Force stale ETag for one row by updating it after collecting ETag values.
  appendRows(Seq(
    ("ifnm-stale", "pk-A", "AfterExternalUpdate")
  ), overwriteCfg)

  // Scenario 1: stale ETag skipped, current ETag replaced, null ETag row created.
  writeRowsWithSchema(Seq(
    Row("ifnm-stale", "pk-A", "ShouldNotOverwrite", etagsBefore("ifnm-stale")),
    Row("ifnm-current", "pk-A", "UpdatedWithCurrentEtag", etagsBefore("ifnm-current")),
    Row("ifnm-new", "pk-A", "CreatedViaNullEtag", null)
  ), overwriteIfNotModifiedSchema, transactionalOverwriteIfNotModifiedCfg)

  val scenario1 = readRowsForPks(Seq("pk-A"))
  scenario1.show(false)
  val scenario1Map = scenario1.collect().map { r =>
    s"${r.getAs[String]("pk")}|${r.getAs[String]("id")}" -> r.getAs[String]("name")
  }.toMap

  assert(scenario1Map.get("pk-A|ifnm-stale").contains("AfterExternalUpdate"))
  assert(scenario1Map.get("pk-A|ifnm-current").contains("UpdatedWithCurrentEtag"))
  assert(scenario1Map.get("pk-A|ifnm-new").contains("CreatedViaNullEtag"))
  println("PASS: Scenario 1 (stale skipped, current replaced, null-etag create)")

  // Scenario 2: null ETag for existing item should not overwrite existing content; new item still creates.
  writeRowsWithSchema(Seq(
    Row("ifnm-current", "pk-A", "ShouldNotReplaceExistingViaNullEtag", null),
    Row("ifnm-new-2", "pk-A", "CreatedViaFallback", null)
  ), overwriteIfNotModifiedSchema, transactionalOverwriteIfNotModifiedCfg)

  val scenario2 = readRowsForPks(Seq("pk-A"))
  scenario2.show(false)
  val scenario2Map = scenario2.collect().map { r =>
    s"${r.getAs[String]("pk")}|${r.getAs[String]("id")}" -> r.getAs[String]("name")
  }.toMap

  assert(scenario2Map.get("pk-A|ifnm-current").contains("UpdatedWithCurrentEtag"))
  assert(scenario2Map.get("pk-A|ifnm-new-2").contains("CreatedViaFallback"))
  println("PASS: Scenario 2 (null-etag existing ignored, null-etag new created)")

  println("PASS: transactional ItemOverwriteIfNotModified scenarios completed.")
} finally {
  spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
}

