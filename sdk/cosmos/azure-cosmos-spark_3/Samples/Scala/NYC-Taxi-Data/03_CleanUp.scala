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
// MAGIC 
// MAGIC Configure the Catalog API to be used

// COMMAND ----------

spark.conf.set("spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set("spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)

// COMMAND ----------

// MAGIC %md
// MAGIC **Cleanup - deleting the Cosmos DB container and database again (to reduce cost) - skip this step if you want to keep them**

// COMMAND ----------

// MAGIC %sql
// MAGIC DROP TABLE IF EXISTS cosmosCatalog.SampleDatabase.GreenTaxiRecords;
// MAGIC 
// MAGIC DROP TABLE IF EXISTS cosmosCatalog.SampleDatabase.GreenTaxiRecordsCFSink;
// MAGIC 
// MAGIC DROP TABLE IF EXISTS cosmosCatalog.SampleDatabase.ThroughputControl;
// MAGIC 
// MAGIC DROP DATABASE IF EXISTS cosmosCatalog.SampleDatabase CASCADE;
