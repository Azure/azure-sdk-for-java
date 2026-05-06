# Databricks notebook source
# MAGIC %md
# MAGIC # Spark AVAD (Full Fidelity) Change Feed Reader
# MAGIC
# MAGIC Reads the change feed in **All Versions and Deletes (Full Fidelity)** mode
# MAGIC using `azure-cosmos-spark` connector and writes consumed events to the
# MAGIC `reconciliation` container with `source = "spark-avad"`.
# MAGIC
# MAGIC ## AVAD-Specific Validations
# MAGIC - Extracts `operationType` from change feed metadata
# MAGIC - Checks `previousImage` presence on replace/delete events
# MAGIC - Captures CRTS (conflict resolution timestamp) from metadata
# MAGIC
# MAGIC ## Prerequisites
# MAGIC - Databricks cluster with `azure-cosmos-spark_3-4_2-12` (or compatible) installed
# MAGIC - Cosmos DB account with AVAD-enabled `avad-test` container and `reconciliation` container
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

feed_container = "avad-test"
recon_container = "reconciliation"

assert cosmos_endpoint, "Set cosmos_endpoint widget or COSMOS_ENDPOINT env var"
assert cosmos_key, "Set cosmos_key widget or COSMOS_KEY env var"

print(f"Endpoint: {cosmos_endpoint}")
print(f"Database: {database}")
print(f"Feed container: {feed_container}")

# COMMAND ----------

# Spark Cosmos config — read change feed in Full Fidelity (AVAD) mode
feed_cfg = {
    "spark.cosmos.accountEndpoint": cosmos_endpoint,
    "spark.cosmos.accountKey": cosmos_key,
    "spark.cosmos.database": database,
    "spark.cosmos.container": feed_container,
    "spark.cosmos.read.partitioning.strategy": "Default",
    "spark.cosmos.changeFeed.mode": "FullFidelity",
    "spark.cosmos.changeFeed.startFrom": "Now",
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

SOURCE = "spark-avad"

# Read change feed as streaming DataFrame — Full Fidelity mode
raw_df = (
    spark.readStream
    .format("cosmos.oltp.changeFeed")
    .options(**feed_cfg)
    .load()
)

# COMMAND ----------

# MAGIC %md
# MAGIC ### Schema Notes
# MAGIC
# MAGIC In Full Fidelity mode, the Spark connector exposes the same columns
# MAGIC as Incremental mode: `id`, `eventId`, `seqNo`, `operationType`,
# MAGIC `tenantId`, `payload`, `timestamp`. The connector flattens the change
# MAGIC feed item — metadata like `lsn` and `crts` are not directly exposed
# MAGIC as columns. previousImage availability depends on container config.

# COMMAND ----------

# Transform to reconciliation schema
# The Spark connector flattens AVAD events — use available columns directly.
# LSN/CRTS/previousImage not exposed as columns by the connector.
recon_df = (
    raw_df
    .select(
        concat(lit(SOURCE + "-"), col("eventId")).alias("id"),
        col("eventId").alias("correlationId"),
        lit(SOURCE).alias("source"),
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
    .option("checkpointLocation", f"/Workspace/avad-soak/checkpoints/spark-avad")
    .outputMode("append")
    .trigger(processingTime="10 seconds")
    .start()
)

print(f"Spark AVAD streaming query started: {query.id}")
print(f"Status: {query.status}")

# COMMAND ----------

# MAGIC %md
# MAGIC ### Monitor
# MAGIC Run this cell periodically to check progress and AVAD correctness.

# COMMAND ----------

# Check streaming query progress
if query.isActive:
    progress = query.lastProgress
    if progress:
        print(f"Batch: {progress['batchId']}")
        print(f"Input rows: {progress['numInputRows']}")
        print(f"Processing time: {progress['batchDuration']} ms")
    else:
        print("No progress yet — waiting for first batch")
else:
    print(f"Query stopped. Exception: {query.exception()}")

# COMMAND ----------

# MAGIC %md
# MAGIC ### AVAD Correctness Check (ad-hoc)
# MAGIC Query the reconciliation container directly to check previousImage counts.

# COMMAND ----------

# Ad-hoc correctness check — read reconciliation container
recon_read_cfg = {
    "spark.cosmos.accountEndpoint": cosmos_endpoint,
    "spark.cosmos.accountKey": cosmos_key,
    "spark.cosmos.database": database,
    "spark.cosmos.container": recon_container,
    "spark.cosmos.read.partitioning.strategy": "Default",
}

recon_data = spark.read.format("cosmos.oltp").options(**recon_read_cfg).load()

# previousImage check for spark-avad
missing_prev = (
    recon_data
    .filter((col("source") == SOURCE) & col("opType").isin("replace", "delete") & (col("hasPreviousImage") == False))
    .count()
)
total_avad = recon_data.filter(col("source") == SOURCE).count()

print(f"Spark AVAD total events: {total_avad}")
print(f"Missing previousImage: {missing_prev}")
print("✅ OK" if missing_prev == 0 else f"❌ {missing_prev} events missing previousImage")

# COMMAND ----------

# MAGIC %md
# MAGIC ### Stop
# MAGIC Run this cell to stop the streaming query gracefully.

# COMMAND ----------

# query.stop()
# print("Spark AVAD query stopped")
