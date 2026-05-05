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

# Configuration
cosmos_endpoint = dbutils.widgets.get("cosmos_endpoint") if "cosmos_endpoint" in [w.name for w in dbutils.widgets.getAll()] else spark.conf.get("spark.cosmos.endpoint", "")
cosmos_key = dbutils.widgets.get("cosmos_key") if "cosmos_key" in [w.name for w in dbutils.widgets.getAll()] else spark.conf.get("spark.cosmos.key", "")
database = dbutils.widgets.get("database") if "database" in [w.name for w in dbutils.widgets.getAll()] else "graph_db"
feed_container = "avad-test"
recon_container = "reconciliation"

if not cosmos_endpoint or not cosmos_key:
    import os
    cosmos_endpoint = os.environ.get("COSMOS_ENDPOINT", "")
    cosmos_key = os.environ.get("COSMOS_KEY", "")

assert cosmos_endpoint, "Set COSMOS_ENDPOINT"
assert cosmos_key, "Set COSMOS_KEY"

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

from pyspark.sql.functions import (
    col, lit, concat, current_timestamp, coalesce,
    when, get_json_object
)
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
# MAGIC In Full Fidelity mode, the Spark connector exposes:
# MAGIC - `_rawBody` — the full JSON of the change feed item
# MAGIC - `current` — the current document state (null for deletes)
# MAGIC - `previous` — the previous document state (present on replace/delete)
# MAGIC - `metadata` — change feed metadata including `operationType`, `lsn`, `crts`

# COMMAND ----------

# Transform to reconciliation schema
# For AVAD, we need to handle:
# 1. operationType from metadata (create/replace/delete)
# 2. previousImage check (must be present on replace/delete)
# 3. CRTS from metadata
# 4. For deletes, extract fields from previous image (current is tombstone)

recon_df = (
    raw_df
    .withColumn("_opType",
        coalesce(
            get_json_object(col("_rawBody"), "$.metadata.operationType"),
            lit("unknown")
        ).cast(StringType())
    )
    .withColumn("_lsnVal",
        coalesce(
            get_json_object(col("_rawBody"), "$.metadata.lsn").cast(LongType()),
            lit(-1)
        )
    )
    .withColumn("_crtsVal",
        coalesce(
            get_json_object(col("_rawBody"), "$.metadata.crts").cast(LongType()),
            lit(-1)
        )
    )
    .withColumn("_hasPrevious",
        get_json_object(col("_rawBody"), "$.previous").isNotNull()
    )
    # For deletes, use previous image fields; otherwise use current
    .withColumn("_eventId",
        when(col("_opType") == "delete",
             get_json_object(col("_rawBody"), "$.previous.eventId"))
        .otherwise(coalesce(col("eventId"), lit("")))
    )
    .withColumn("_seqNo",
        when(col("_opType") == "delete",
             get_json_object(col("_rawBody"), "$.previous.seqNo").cast(LongType()))
        .otherwise(coalesce(col("seqNo").cast(LongType()), lit(-1)))
    )
    .withColumn("_pk",
        when(col("_opType") == "delete",
             get_json_object(col("_rawBody"), "$.previous.tenantId"))
        .otherwise(coalesce(col("tenantId"), lit("")))
    )
    .filter(col("_eventId").isNotNull() & (col("_eventId") != ""))
    .select(
        concat(lit(SOURCE + "-"), col("_eventId")).alias("id"),
        col("_eventId").alias("correlationId"),
        lit(SOURCE).alias("source"),
        col("_seqNo").alias("seqNo"),
        col("_opType").alias("opType"),
        col("_pk").alias("partitionKey"),
        col("_lsnVal").alias("lsn"),
        col("_hasPrevious").cast(BooleanType()).alias("hasPreviousImage"),
        col("_crtsVal").alias("crts"),
        current_timestamp().cast(StringType()).alias("timestamp"),
    )
)

# COMMAND ----------

# Write to reconciliation container as a streaming job
query = (
    recon_df.writeStream
    .format("cosmos.oltp")
    .options(**recon_cfg)
    .option("checkpointLocation", f"/tmp/cosmos-avad-soak/spark-avad-checkpoint")
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
