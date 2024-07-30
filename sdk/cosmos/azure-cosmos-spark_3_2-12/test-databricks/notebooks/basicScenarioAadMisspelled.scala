// Databricks notebook source
// val cosmosEndpoint = "<inserted by environment>"
// val subscriptionId = "<inserted by environment>"
// val tenantId = "<inserted by environment>"
// val resourceGroupName = "<inserted by environment>"
// val clientId = "<inserted by environment>"
// val clientSecret = "<inserted by environment>"

println("SCENARIO: basicScenarioAadMisspelled")

val authType = "ServicePrinciple"
val cosmosEndpoint = dbutils.widgets.get("cosmosEndpoint")
val subscriptionId = dbutils.widgets.get("subscriptionId")
val tenantId = dbutils.widgets.get("tenantId")
val resourceGroupName = dbutils.widgets.get("resourceGroupName")
val clientId = dbutils.widgets.get("clientId")
val clientSecret = dbutils.widgets.get("clientSecret")
val cosmosContainerName = dbutils.widgets.get("cosmosContainerName")
val cosmosDatabaseName = dbutils.widgets.get("cosmosDatabaseName")

val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.auth.type" -> authType,
    "spark.cosmos.account.subscriptionId" -> subscriptionId,
    "spark.cosmos.account.tenantId" -> tenantId,
    "spark.cosmos.account.resourceGroupName" -> resourceGroupName,
    "spark.cosmos.auth.aad.clientId" -> clientId,
    "spark.cosmos.auth.aad.clientSecret" -> clientSecret,
    "spark.cosmos.database" -> cosmosDatabaseName,
    "spark.cosmos.container" -> cosmosContainerName
)

val cfgWithAutoSchemaInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.auth.type" -> authType,
    "spark.cosmos.account.subscriptionId" -> subscriptionId,
    "spark.cosmos.account.tenantId" -> tenantId,
    "spark.cosmos.account.resourceGroupName" -> resourceGroupName,
    "spark.cosmos.auth.aad.clientId" -> clientId,
    "spark.cosmos.auth.aad.clientSecret" -> clientSecret,
    "spark.cosmos.database" -> cosmosDatabaseName,
    "spark.cosmos.container" -> cosmosContainerName,
    "spark.cosmos.read.inferSchema.enabled" -> "true"
)

// COMMAND ----------

// create Cosmos Database and Cosmos Container using Catalog APIs
spark.conf.set(s"spark.sql.catalog.cosmosCatalogSpnOld", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(s"spark.sql.catalog.cosmosCatalogSpnOld.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogSpnOld.spark.cosmos.auth.type", authType)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogSpnOld.spark.cosmos.account.subscriptionId", subscriptionId)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogSpnOld.spark.cosmos.account.tenantId", tenantId)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogSpnOld.spark.cosmos.account.resourceGroupName", resourceGroupName)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogSpnOld.spark.cosmos.auth.aad.clientId", clientId)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogSpnOld.spark.cosmos.auth.aad.clientSecret", clientSecret)

// create a cosmos database
spark.sql(s"CREATE DATABASE IF NOT EXISTS cosmosCatalogSpnOld.${cosmosDatabaseName};")

// create a cosmos container
spark.sql(s"CREATE TABLE IF NOT EXISTS cosmosCatalogSpnOld.${cosmosDatabaseName}.${cosmosContainerName} using cosmos.oltp " +
    s"TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '400')")

// update the throughput
spark.sql(s"ALTER TABLE cosmosCatalogSpnOld.${cosmosDatabaseName}.${cosmosContainerName} " +
    s"SET TBLPROPERTIES('manualThroughput' = '1100')")

// COMMAND ----------

// ingestion
spark.createDataFrame(Seq(("cat-alive", "Schrodinger cat", 2, true), ("cat-dead", "Schrodinger cat", 2, false)))
    .toDF("id","name","age","isAlive")
    .write
    .format("cosmos.oltp")
    .options(cfg)
    .mode("APPEND")
    .save()

// COMMAND ----------

// Show the schema of the table and data without auto schema inference
val df = spark.read.format("cosmos.oltp").options(cfg).load()
df.printSchema()

df.show()

// COMMAND ----------

// Show the schema of the table and data with auto schema inference
val df = spark.read.format("cosmos.oltp").options(cfgWithAutoSchemaInference).load()
df.printSchema()

df.show()

// COMMAND ----------

import org.apache.spark.sql.functions.col

// Query to find the live cat and increment age of the alive cat
df.filter(col("isAlive") === true)
    .withColumn("age", col("age") + 1)
    .show()

// COMMAND ----------

// cleanup
spark.sql(s"DROP TABLE cosmosCatalogSpnOld.${cosmosDatabaseName}.${cosmosContainerName};")
