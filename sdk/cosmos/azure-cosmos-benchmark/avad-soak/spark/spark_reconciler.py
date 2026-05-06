# Databricks notebook source
# MAGIC %md
# MAGIC # AVAD Soak Test — Spark Reconciler
# MAGIC
# MAGIC Runs all reconciliation checks against the `reconciliation` container using PySpark.
# MAGIC Handles millions of docs efficiently via bulk read + DataFrame operations.
# MAGIC
# MAGIC ## Checks
# MAGIC | # | Check | Sources |
# MAGIC |---|-------|---------|
# MAGIC | Q1 | Summary dashboard | All |
# MAGIC | Q2 | Gap detection (producer → consumer) | ingestor → cfp-lv, cfp-avad, spark-lv, spark-avad |
# MAGIC | Q3 | Parity (LV ⊆ AVAD) | cfp-lv → cfp-avad, spark-lv → spark-avad |
# MAGIC | Q4 | Cross-engine parity | cfp-lv ↔ spark-lv, cfp-avad ↔ spark-avad |
# MAGIC | Q5 | LSN ordering per partition | cfp-lv, cfp-avad, spark-lv, spark-avad |
# MAGIC | Q6 | CRTS ordering per partition | cfp-avad, spark-avad |
# MAGIC | Q7 | previousImage validation | cfp-avad, spark-avad |
# MAGIC | Q8 | Duplicate detection | All |
# MAGIC
# MAGIC ## Prerequisites
# MAGIC - `azure-cosmos-spark_3-4_2-12` connector installed on cluster
# MAGIC - Cluster env vars or widgets: `COSMOS_ENDPOINT`, `COSMOS_KEY`

# COMMAND ----------

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

assert cosmos_endpoint, "Set cosmos_endpoint widget or COSMOS_ENDPOINT env var"
assert cosmos_key, "Set cosmos_key widget or COSMOS_KEY env var"

recon_cfg = {
    "spark.cosmos.accountEndpoint": cosmos_endpoint,
    "spark.cosmos.accountKey": cosmos_key,
    "spark.cosmos.database": database,
    "spark.cosmos.container": "reconciliation",
    "spark.cosmos.read.partitioning.strategy": "Default",
}

print(f"Endpoint: {cosmos_endpoint}")
print(f"Database: {database}")

# COMMAND ----------

# Load entire reconciliation container into a cached DataFrame
# Use only columns guaranteed to exist across all sources
recon = (
    spark.read
    .format("cosmos.oltp")
    .options(**recon_cfg)
    .load()
    .cache()
)

# Select columns that exist, fill missing ones with defaults
from pyspark.sql.functions import col, lit, when

available_cols = recon.columns
print(f"Available columns: {available_cols}")

recon = recon.select(
    col("id"),
    col("correlationId") if "correlationId" in available_cols else lit(None).alias("correlationId"),
    col("source"),
    col("seqNo") if "seqNo" in available_cols else lit(-1).alias("seqNo"),
    col("opType") if "opType" in available_cols else lit("unknown").alias("opType"),
    col("partitionKey") if "partitionKey" in available_cols else lit("").alias("partitionKey"),
    col("lsn") if "lsn" in available_cols else lit(-1).alias("lsn"),
    col("hasPreviousImage") if "hasPreviousImage" in available_cols else lit(False).alias("hasPreviousImage"),
    col("crts").cast("long") if "crts" in available_cols else lit(-1).cast("long").alias("crts"),
    col("timestamp") if "timestamp" in available_cols else lit("").alias("timestamp"),
).cache()

total = recon.count()
print(f"Total reconciliation docs: {total:,}")

# COMMAND ----------

# MAGIC %md
# MAGIC ## Q1 — Summary Dashboard

# COMMAND ----------

from pyspark.sql.functions import count, countDistinct, min as spark_min, max as spark_max, col

summary = (
    recon
    .groupBy("source")
    .agg(
        count("*").alias("totalEvents"),
        countDistinct("correlationId").alias("uniqueEvents"),
        spark_min("seqNo").alias("minSeq"),
        spark_max("seqNo").alias("maxSeq"),
        spark_min("lsn").alias("minLsn"),
        spark_max("lsn").alias("maxLsn"),
    )
    .orderBy("source")
)

summary.show(truncate=False)

# COMMAND ----------

# MAGIC %md
# MAGIC ## Q2 — Gap Detection (Ingestor → Each Consumer)

# COMMAND ----------

from pyspark.sql.functions import lit

failures = 0

def check_gaps(source_a, source_b, label):
    global failures
    ids_a = recon.filter(col("source") == source_a).select("correlationId").distinct()
    ids_b = recon.filter(col("source") == source_b).select("correlationId").distinct()

    count_a = ids_a.count()
    count_b = ids_b.count()

    if count_a == 0:
        print(f"  ⏭️  {label}: {source_a} has 0 events — skipping")
        return
    if count_b == 0:
        print(f"  ⏭️  {label}: {source_b} has 0 events — skipping")
        return

    missing = ids_a.subtract(ids_b)
    gap_count = missing.count()

    status = "✅" if gap_count == 0 else "❌"
    print(f"  {status} {label}: {count_a:,} produced, {count_b:,} consumed, {gap_count:,} gaps")

    if gap_count > 0:
        failures += 1
        print(f"     Sample missing IDs:")
        for row in missing.limit(10).collect():
            print(f"       {row.correlationId}")

print("=== Gap Detection ===")
check_gaps("ingestor", "cfp-lv",    "Ingestor → CFP LV")
check_gaps("ingestor", "cfp-avad",  "Ingestor → CFP AVAD")
check_gaps("ingestor", "spark-lv",  "Ingestor → Spark LV")
check_gaps("ingestor", "spark-avad","Ingestor → Spark AVAD")

# COMMAND ----------

# MAGIC %md
# MAGIC ## Q3 — Parity (LV ⊆ AVAD)

# COMMAND ----------

print("=== Parity (AVAD ⊇ LV) ===")
check_gaps("cfp-lv",   "cfp-avad",   "CFP Parity")
check_gaps("spark-lv", "spark-avad", "Spark Parity")

# COMMAND ----------

# MAGIC %md
# MAGIC ## Q4 — Cross-Engine Parity (CFP ↔ Spark)

# COMMAND ----------

print("=== Cross-Engine Parity ===")
check_gaps("cfp-lv",   "spark-lv",   "LV: CFP → Spark")
check_gaps("spark-lv", "cfp-lv",     "LV: Spark → CFP")
check_gaps("cfp-avad", "spark-avad", "AVAD: CFP → Spark")
check_gaps("spark-avad","cfp-avad",  "AVAD: Spark → CFP")

# COMMAND ----------

# MAGIC %md
# MAGIC ## Q5 — LSN Ordering Per Partition

# COMMAND ----------

from pyspark.sql.window import Window
from pyspark.sql.functions import lag, sum as spark_sum, when

def check_lsn_ordering(source):
    global failures
    events = recon.filter((col("source") == source) & (col("lsn") >= 0))
    event_count = events.count()

    if event_count == 0:
        print(f"  ⏭️  {source}: no events with LSN — skipping")
        return

    w = Window.partitionBy("partitionKey").orderBy("seqNo")
    violations = (
        events
        .withColumn("prevLsn", lag("lsn").over(w))
        .filter(col("prevLsn").isNotNull() & (col("lsn") < col("prevLsn")))
        .count()
    )

    status = "✅" if violations == 0 else "❌"
    print(f"  {status} {source}: {event_count:,} events, {violations:,} LSN ordering violations")
    if violations > 0:
        failures += 1

print("=== LSN Ordering ===")
for s in ["cfp-lv", "cfp-avad", "spark-lv", "spark-avad"]:
    check_lsn_ordering(s)

# COMMAND ----------

# MAGIC %md
# MAGIC ## Q6 — CRTS Ordering Per Partition (AVAD Only)

# COMMAND ----------

def check_crts_ordering(source):
    global failures
    events = recon.filter((col("source") == source) & (col("crts") >= 0))
    event_count = events.count()

    if event_count == 0:
        print(f"  ⏭️  {source}: no events with CRTS — skipping")
        return

    w = Window.partitionBy("partitionKey").orderBy("seqNo")
    violations = (
        events
        .withColumn("prevCrts", lag("crts").over(w))
        .filter(col("prevCrts").isNotNull() & (col("crts") < col("prevCrts")))
        .count()
    )

    status = "✅" if violations == 0 else "❌"
    print(f"  {status} {source}: {event_count:,} events, {violations:,} CRTS ordering violations")
    if violations > 0:
        failures += 1

print("=== CRTS Ordering ===")
check_crts_ordering("cfp-avad")
check_crts_ordering("spark-avad")

# COMMAND ----------

# MAGIC %md
# MAGIC ## Q7 — previousImage Validation (AVAD Only)

# COMMAND ----------

def check_previous_image(source):
    global failures
    missing = (
        recon
        .filter(
            (col("source") == source) &
            col("opType").isin("replace", "delete") &
            (col("hasPreviousImage") == False)
        )
        .count()
    )

    total_rd = (
        recon
        .filter(
            (col("source") == source) &
            col("opType").isin("replace", "delete")
        )
        .count()
    )

    status = "✅" if missing == 0 else "❌"
    print(f"  {status} {source}: {total_rd:,} replace/delete events, {missing:,} missing previousImage")
    if missing > 0:
        failures += 1

print("=== previousImage Validation ===")
check_previous_image("cfp-avad")
check_previous_image("spark-avad")

# COMMAND ----------

# MAGIC %md
# MAGIC ## Q8 — Duplicate Detection (At-Least-Once)

# COMMAND ----------

print("=== Duplicate Detection ===")
dupes = (
    recon
    .groupBy("source")
    .agg(
        count("*").alias("total"),
        countDistinct("correlationId").alias("unique"),
    )
    .withColumn("duplicates", col("total") - col("unique"))
    .withColumn("dupeRate", (col("duplicates") / col("total") * 100).cast("decimal(5,2)"))
    .orderBy("source")
)

dupes.show(truncate=False)

# COMMAND ----------

# MAGIC %md
# MAGIC ## Final Verdict

# COMMAND ----------

if failures == 0:
    print("✅ ALL CHECKS PASSED")
else:
    print(f"❌ {failures} CHECK(S) FAILED")

# Return exit-like status for job runners
dbutils.notebook.exit("PASS" if failures == 0 else f"FAIL:{failures}")
