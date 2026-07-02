# Databricks notebook source
# MAGIC %md
# MAGIC # Spark Latest Version Change Feed Reader
# MAGIC
# MAGIC Reads the change feed in **Latest Version (Incremental)** mode using
# MAGIC `azure-cosmos-spark` connector and writes consumed events to the
# MAGIC `reconciliation` container with `source = "spark-lv"`.
# MAGIC
# MAGIC ## Prerequisites
# MAGIC - Databricks cluster with `azure-cosmos-spark_3-4_2-12` (or compatible) installed
# MAGIC - Cosmos DB account with `avad-test` and `reconciliation` containers
# MAGIC - Cluster env vars: `COSMOS_ENDPOINT`, `COSMOS_KEY`

# COMMAND ----------

# Configuration — reads from notebook widgets (set via job parameters or manually)
import os

try:
    cosmos_endpoint = dbutils.widgets.get("cosmos_endpoint")
except:
    cosmos_endpoint = os.environ.get("COSMOS_ENDPOINT", "")

try:
    cosmos_key = dbutils.widgets.get("cosmos_key")
except:
    cosmos_key = os.environ.get("COSMOS_KEY", "")

try:
    database = dbutils.widgets.get("database")
except:
    database = "graph_db"

try:
    run_id = dbutils.widgets.get("run_id")
except:
    run_id = os.environ.get("RUN_ID", "")

feed_container = "avad-test"
recon_container = "reconciliation"

assert cosmos_endpoint, "Set cosmos_endpoint widget or COSMOS_ENDPOINT env var"
assert cosmos_key, "Set cosmos_key widget or COSMOS_KEY env var"
assert run_id, "Set run_id widget or RUN_ID env var"

print(f"Endpoint: {cosmos_endpoint}")
print(f"Database: {database}")
print(f"Feed container: {feed_container}")
print(f"Run ID: {run_id}")

# COMMAND ----------

# Spark Cosmos config — read change feed in incremental (LV) mode
feed_cfg = {
    "spark.cosmos.accountEndpoint": cosmos_endpoint,
    "spark.cosmos.accountKey": cosmos_key,
    "spark.cosmos.database": database,
    "spark.cosmos.container": feed_container,
    "spark.cosmos.read.partitioning.strategy": "Default",
    "spark.cosmos.changeFeed.mode": "Incremental",
    "spark.cosmos.changeFeed.startFrom": "Beginning",
    "spark.cosmos.changeFeed.itemCountPerTriggerHint": "1000",
}

# Write config — reconciliation container
recon_cfg = {
    "spark.cosmos.accountEndpoint": cosmos_endpoint,
    "spark.cosmos.accountKey": cosmos_key,
    "spark.cosmos.database": database,
    "spark.cosmos.container": recon_container,
    "spark.cosmos.write.strategy": "ItemOverwrite",
    "spark.cosmos.write.bulk.enabled": "true",
}

# COMMAND ----------

from pyspark.sql.functions import col, lit, concat, current_timestamp, coalesce
from pyspark.sql.types import StringType, LongType, BooleanType

SOURCE = "spark-lv"

# Read change feed as streaming DataFrame
raw_df = (
    spark.readStream
    .format("cosmos.oltp.changeFeed")
    .options(**feed_cfg)
    .load()
)

# Transform to reconciliation schema
recon_df = (
    raw_df
    .select(
        concat(lit(SOURCE + "-"), col("eventId")).alias("id"),
        col("eventId").alias("correlationId"),
        lit(SOURCE).alias("source"),
        lit(run_id).alias("runId"),
        coalesce(col("seqNo"), lit(-1)).cast(LongType()).alias("seqNo"),
        coalesce(col("operationType"), lit("unknown")).alias("opType"),
        coalesce(col("tenantId"), lit("")).alias("partitionKey"),
        lit(-1).cast(LongType()).alias("lsn"),
        lit(False).cast(BooleanType()).alias("hasPreviousImage"),
        lit(-1).cast(LongType()).alias("crts"),
        current_timestamp().cast(StringType()).alias("timestamp"),
    )
    .filter(col("correlationId").isNotNull())
)

# COMMAND ----------

# Write to reconciliation container as a streaming job
query = (
    recon_df.writeStream
    .format("cosmos.oltp")
    .options(**recon_cfg)
    .option("checkpointLocation", f"/Workspace/avad-soak/checkpoints/spark-lv")
    .outputMode("append")
    .trigger(processingTime="10 seconds")
    .start()
)

print(f"Spark LV streaming query started: {query.id}")
print(f"Status: {query.status}")

# COMMAND ----------

# MAGIC %md
# MAGIC ### Monitor
# MAGIC Run this cell periodically to check progress.

# COMMAND ----------

# Check streaming query progress
if query.isActive:
    progress = query.lastProgress
    if progress:
        print(f"Batch: {progress['batchId']}")
        print(f"Input rows: {progress['numInputRows']}")
        print(f"Processing time: {progress['batchDuration']} ms")
        print(f"Sources: {progress['sources']}")
    else:
        print("No progress yet — waiting for first batch")
else:
    print(f"Query stopped. Exception: {query.exception()}")

# COMMAND ----------

# MAGIC %md
# MAGIC ### Stop
# MAGIC Run this cell to stop the streaming query gracefully.

# COMMAND ----------

# query.stop()
# print("Spark LV query stopped")
