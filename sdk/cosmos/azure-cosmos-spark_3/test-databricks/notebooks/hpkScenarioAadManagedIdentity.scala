// Databricks notebook source
println("SCENARIO: hpkScenarioAadManagedIdentity")

val authType = "ManagedIdentity"
val cosmosEndpoint = "https://benchmark-cosmos-lx0.documents.azure.com:443/"
val subscriptionId = "b31b6408-0fb5-4688-9a3c-33ffb3983297"
val tenantId = "b5a53dd4-9da5-494f-adab-d04a5754fc6f"
val resourceGroupName = "lx-cosmos-benchmark"
val cosmosDatabaseName = "hpk-mi-testdb"
val cosmosContainerName = "hpk-mi-testcontainer"
val cosmosHpkContainerName = cosmosContainerName + "-hpk"

val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.auth.type" -> authType,
    "spark.cosmos.account.subscriptionId" -> subscriptionId,
    "spark.cosmos.account.tenantId" -> tenantId,
    "spark.cosmos.account.resourceGroupName" -> resourceGroupName,
    "spark.cosmos.database" -> cosmosDatabaseName,
    "spark.cosmos.container" -> cosmosContainerName,
    "spark.cosmos.enforceNativeTransport" -> "true",
    "spark.cosmos.read.consistencyStrategy" -> "LatestCommitted",
)

val cfgWithAutoSchemaInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.auth.type" -> authType,
    "spark.cosmos.account.subscriptionId" -> subscriptionId,
    "spark.cosmos.account.tenantId" -> tenantId,
    "spark.cosmos.account.resourceGroupName" -> resourceGroupName,
    "spark.cosmos.database" -> cosmosDatabaseName,
    "spark.cosmos.container" -> cosmosContainerName,
    "spark.cosmos.read.inferSchema.enabled" -> "true",
    "spark.cosmos.enforceNativeTransport" -> "true",
    "spark.cosmos.read.consistencyStrategy" -> "LatestCommitted",
)

val cfgHpk = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.auth.type" -> authType,
    "spark.cosmos.account.subscriptionId" -> subscriptionId,
    "spark.cosmos.account.tenantId" -> tenantId,
    "spark.cosmos.account.resourceGroupName" -> resourceGroupName,
    "spark.cosmos.database" -> cosmosDatabaseName,
    "spark.cosmos.container" -> cosmosHpkContainerName,
    "spark.cosmos.enforceNativeTransport" -> "true",
    "spark.cosmos.read.consistencyStrategy" -> "LatestCommitted",
)

val cfgHpkWithAutoSchemaInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.auth.type" -> authType,
    "spark.cosmos.account.subscriptionId" -> subscriptionId,
    "spark.cosmos.account.tenantId" -> tenantId,
    "spark.cosmos.account.resourceGroupName" -> resourceGroupName,
    "spark.cosmos.database" -> cosmosDatabaseName,
    "spark.cosmos.container" -> cosmosHpkContainerName,
    "spark.cosmos.read.inferSchema.enabled" -> "true",
    "spark.cosmos.enforceNativeTransport" -> "true",
    "spark.cosmos.read.consistencyStrategy" -> "LatestCommitted",
)

// COMMAND ----------

// Section 1: Setup Catalog and create database + standard container
spark.conf.set(s"spark.sql.catalog.cosmosCatalogMI", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(s"spark.sql.catalog.cosmosCatalogMI.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogMI.spark.cosmos.auth.type", authType)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogMI.spark.cosmos.account.subscriptionId", subscriptionId)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogMI.spark.cosmos.account.tenantId", tenantId)
spark.conf.set(s"spark.sql.catalog.cosmosCatalogMI.spark.cosmos.account.resourceGroupName", resourceGroupName)

// create database
spark.sql(s"CREATE DATABASE IF NOT EXISTS cosmosCatalogMI.${cosmosDatabaseName};")

// create standard container (single partition key on /id)
spark.sql(s"CREATE TABLE IF NOT EXISTS cosmosCatalogMI.${cosmosDatabaseName}.${cosmosContainerName} using cosmos.oltp " +
    s"TBLPROPERTIES(partitionKeyPath = '/id', manualThroughput = '400')")

// COMMAND ----------

// Section 2: Ingest data into standard container
spark.createDataFrame(Seq(("cat-alive", "Schrodinger cat", 2, true), ("cat-dead", "Schrodinger cat", 2, false)))
    .toDF("id","name","age","isAlive")
    .write
    .format("cosmos.oltp")
    .options(cfg)
    .mode("APPEND")
    .save()

// COMMAND ----------

// Section 3: Read and query standard container
val df = spark.read.format("cosmos.oltp").options(cfgWithAutoSchemaInference).load()
df.printSchema()
df.show()

// COMMAND ----------

import org.apache.spark.sql.functions.col

// Query to find the live cat and increment age
df.filter(col("isAlive") === true)
    .withColumn("age", col("age") + 1)
    .show()

// COMMAND ----------

// Section 4: Create container with Hierarchical Partition Key (HPK)
// HPK uses multiple partition key paths: /tenantId, /userId, /sessionId
spark.sql(s"CREATE TABLE IF NOT EXISTS cosmosCatalogMI.${cosmosDatabaseName}.${cosmosHpkContainerName} using cosmos.oltp " +
    s"TBLPROPERTIES(partitionKeyPath = '/tenantId,/userId,/sessionId', manualThroughput = '400')")

// COMMAND ----------

// Section 5: Ingest data into HPK container
spark.createDataFrame(Seq(
    ("1", "tenant-A", "user-1", "session-100", "login", "2024-01-01"),
    ("2", "tenant-A", "user-1", "session-101", "query", "2024-01-02"),
    ("3", "tenant-A", "user-2", "session-200", "login", "2024-01-01"),
    ("4", "tenant-B", "user-3", "session-300", "login", "2024-01-03"),
    ("5", "tenant-B", "user-3", "session-300", "logout", "2024-01-03")
))
    .toDF("id", "tenantId", "userId", "sessionId", "action", "timestamp")
    .write
    .format("cosmos.oltp")
    .options(cfgHpk)
    .mode("APPEND")
    .save()

// COMMAND ----------

// Section 6: Read and query HPK container
val dfHpk = spark.read.format("cosmos.oltp").options(cfgHpkWithAutoSchemaInference).load()
dfHpk.printSchema()
dfHpk.show()

// COMMAND ----------

import org.apache.spark.sql.functions.col

// Query: filter by first level of hierarchical partition key (tenantId)
dfHpk.filter(col("tenantId") === "tenant-A").show()

// Query: filter by first and second level (tenantId + userId)
dfHpk.filter(col("tenantId") === "tenant-A" && col("userId") === "user-1").show()

// Query: filter by all three levels (tenantId + userId + sessionId)
dfHpk.filter(col("tenantId") === "tenant-B" && col("userId") === "user-3" && col("sessionId") === "session-300").show()

// COMMAND ----------

// Section 7: Update throughput on HPK container
spark.sql(s"ALTER TABLE cosmosCatalogMI.${cosmosDatabaseName}.${cosmosHpkContainerName} " +
    s"SET TBLPROPERTIES('manualThroughput' = '1100')")

// COMMAND ----------

// Section 8: Cleanup
spark.sql(s"DROP TABLE cosmosCatalogMI.${cosmosDatabaseName}.${cosmosContainerName};")
spark.sql(s"DROP TABLE cosmosCatalogMI.${cosmosDatabaseName}.${cosmosHpkContainerName};")
