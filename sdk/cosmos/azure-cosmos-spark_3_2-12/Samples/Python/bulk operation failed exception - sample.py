# Databricks notebook source
# configuration
cosmosEndpoint = "<cosmos-endpoint>"
cosmosMasterKey = "<cosmos-master-key>"
cosmosDatabaseName = "sampleDB"
cosmosContainerName = "sampleContainer"


cfg = {
    "spark.cosmos.accountEndpoint": cosmosEndpoint,
    "spark.cosmos.accountKey": cosmosMasterKey,
    "spark.cosmos.database": cosmosDatabaseName,
    "spark.cosmos.container": cosmosContainerName,
}

spark.conf.set("spark.sql.catalog.cosmosCatalog",
               "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(
    "spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(
    "spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)   

# COMMAND ----------

# create databse and container
# %sql
# CREATE DATABASE IF NOT EXISTS cosmosCatalog.sampleDB;

# CREATE TABLE IF NOT EXISTS cosmosCatalog.sampleDB.sampleContainer
# USING cosmos.oltp
# TBLPROPERTIES(partitionKeyPath = '/id', autoScaleMaxThroughput = '100000', indexingPolicy = 'OnlySystemProperties');

# COMMAND ----------

data = [
    {"id": "1", "city": "A", "temperature": 40},
    {"id": "2", "city": "B", "temperature": 55},
    {"id": "3", "city": "C", "temperature": 78}
]

# COMMAND ----------

cfg = { 
  "spark.cosmos.accountEndpoint" : cosmosEndpoint,
  "spark.cosmos.accountKey" : cosmosMasterKey,
  "spark.cosmos.database" : cosmosDatabaseName,
  "spark.cosmos.container" : cosmosContainerName,
  "spark.cosmos.write.strategy": "ItemOverwrite",
  "spark.cosmos.write.bulk.enabled": "true"
}


spark.createDataFrame(data)\
.write\
.format("com.azure.cosmos.spark.CosmosItemsDataSource")\
.options(**cfg)\
.mode("APPEND")\
.save()

# COMMAND ----------

# patch bulk example where one of the id is not present in the container, and throws a Bulk Operation Failed Exception with the itemId and PartitionKey

cfgBulk = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
           "spark.cosmos.accountKey": cosmosMasterKey,
           "spark.cosmos.database": cosmosDatabaseName,
           "spark.cosmos.container": cosmosContainerName,
           "spark.cosmos.write.strategy": "ItemPatch",
           "spark.cosmos.write.bulk.enabled": "true",
           "spark.cosmos.write.patch.columnConfigs": "[col(wind).op(add)]"
           }

data = [
    {"id": "1", "wind": "yes"},
    {"id": "2", "wind": "yes"},
    {"id": "4", "wind": "no" }
]
patchDf = spark.createDataFrame(data)

try :
    patchDf.write.format("com.azure.cosmos.spark.CosmosItemsDataSource").mode("Append").options(**cfgBulk).save()
except Exception as ex:
    print(ex)

# COMMAND ----------


