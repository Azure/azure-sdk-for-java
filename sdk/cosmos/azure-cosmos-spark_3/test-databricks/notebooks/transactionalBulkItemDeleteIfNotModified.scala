// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val cosmosMasterKey = "<inserted by environment>"

println("SCENARIO: transactionalBulkItemDeleteIfNotModified")

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}

val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val cosmosMasterKey = dbutils.widgets.get("cosmosMasterKey")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val transactionalContainerName = s"${cosmosContainerName}_txnBulkItemDeleteIfNotModified"

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

val transactionalDeleteIfNotModifiedCfg = cfg ++ Map(
  "spark.cosmos.write.strategy" -> "ItemDeleteIfNotModified",
  "spark.cosmos.write.bulk.enabled" -> "true",
  "spark.cosmos.write.bulk.transactional" -> "true"
)

val deleteWithEtagSchema = StructType(Seq(
  StructField("id", StringType, nullable = false),
  StructField("pk", StringType, nullable = false),
  StructField("name", StringType, nullable = true),
  StructField("_etag", StringType, nullable = false)
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
  // Seed docs for conditional delete checks.
  appendRows(Seq(
    ("ifnm-stale", "pk-A", "BeforeUpdate"),
    ("ifnm-current", "pk-A", "Current"),
    ("ifnm-extra", "pk-B", "KeepMe")
  ), cfg)

  val etagBefore = readRowsForPks(Seq("pk-A"))
    .select("id", "_etag")
    .collect()
    .map(r => r.getAs[String]("id") -> r.getAs[String]("_etag"))
    .toMap

  // Force stale ETag for one row by updating it after collecting ETags.
  appendRows(Seq(
    ("ifnm-stale", "pk-A", "AfterUpdate")
  ), overwriteCfg)

  val deleteRows = Seq(
    Row("ifnm-stale", "pk-A", "placeholder", etagBefore("ifnm-stale")),
    Row("ifnm-current", "pk-A", "placeholder", etagBefore("ifnm-current"))
  )

  spark.createDataFrame(spark.sparkContext.parallelize(deleteRows), deleteWithEtagSchema)
    .write
    .format("cosmos.oltp")
    .options(transactionalDeleteIfNotModifiedCfg)
    .mode("APPEND")
    .save()

  val scenario1 = readRowsForPks(Seq("pk-A", "pk-B"))
  scenario1.show(false)

  val scenario1Map = scenario1.collect().map { r =>
    s"${r.getAs[String]("pk")}|${r.getAs[String]("id")}" -> r.getAs[String]("name")
  }.toMap

  assert(scenario1Map.contains("pk-A|ifnm-stale"), "Stale ETag row should not be deleted")
  assert(!scenario1Map.contains("pk-A|ifnm-current"), "Current ETag row should be deleted")
  assert(scenario1Map.contains("pk-B|ifnm-extra"), "Unrelated partition row should remain")
  println("PASS: Scenario 1 (stale ETag skipped, current ETag deleted)")

  // Scenario 2: Missing _etag should fail for ItemDeleteIfNotModified.
  val missingEtagSchema = StructType(Seq(
    StructField("id", StringType, nullable = false),
    StructField("pk", StringType, nullable = false),
    StructField("name", StringType, nullable = true)
  ))

  val missingEtagDf = spark.createDataFrame(
    spark.sparkContext.parallelize(Seq(Row("ifnm-no-etag", "pk-C", "NoEtag"))),
    missingEtagSchema
  )

  var sawExpectedFailure = false
  try {
    missingEtagDf.write
      .format("cosmos.oltp")
      .options(transactionalDeleteIfNotModifiedCfg)
      .mode("APPEND")
      .save()
  } catch {
    case ex: Exception =>
      val chain = Iterator.iterate(ex)(_.getCause).takeWhile(_ != null).map(_.getMessage).mkString(" | ")
      val chainLower = chain.toLowerCase
      sawExpectedFailure = chainLower.contains("_etag") && chainLower.contains("mandatory")
      if (!sawExpectedFailure) {
        throw ex
      }
  }

  assert(sawExpectedFailure, "Expected failure for missing _etag in ItemDeleteIfNotModified")
  println("PASS: Scenario 2 (missing _etag fails as expected)")

  println("PASS: transactional ItemDeleteIfNotModified scenarios completed.")
} finally {
  spark.sql(s"DROP TABLE IF EXISTS cosmosCatalog.${cosmosDatabaseName}.${transactionalContainerName};")
}

