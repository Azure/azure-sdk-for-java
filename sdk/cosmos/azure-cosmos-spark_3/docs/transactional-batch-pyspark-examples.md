# Azure Cosmos DB Spark Connector - Transactional Batch Operations (PySpark)

This guide provides PySpark examples for using transactional batch operations with the Azure Cosmos DB Spark 3 connector.

## Overview

Transactional batch operations enable atomic multi-document operations within a single partition, ensuring all operations succeed or fail together (ACID guarantees).

## Key Features

- **Atomic Operations**: All operations in a batch succeed or fail together (ACID guarantees)
- **Multiple Operation Types**: Supports create, replace, upsert, delete, and read operations
- **100-Operation Limit**: Enforces Cosmos DB's limit of 100 operations per partition key (throws error if exceeded)
- **Flexible Schema**: Optional `operationType` column (defaults to "upsert" if not provided)
- **Partition Key Validation**: Ensures all operations within a batch target the same partition
- **Detailed Results**: Returns per-operation status codes, success flags, and error messages
- **Resource Management**: Proper cleanup of Cosmos clients and throughput control resources

## Setup

```python
from pyspark.sql import SparkSession
from pyspark.sql.types import StructType, StructField, StringType, IntegerType
from pyspark.sql.functions import col

# Initialize Spark session
spark = SparkSession.builder \
    .appName("CosmosDB Transactional Batch") \
    .config("spark.jars.packages", "com.azure.cosmos.spark:azure-cosmos-spark_3-3_2-12:4.41.0") \
    .getOrCreate()

# Cosmos DB configuration
cosmos_config = {
    "spark.cosmos.accountEndpoint": "https://your-account.documents.azure.com:443/",
    "spark.cosmos.accountKey": "your-account-key",
    "spark.cosmos.database": "your-database",
    "spark.cosmos.container": "your-container"
}
```

## Example 1: Basic Upsert Operations (No Operation Type Specified)

When you don't specify an `operationType` column, all operations default to "upsert":

```python
from pyspark.sql.types import StructType, StructField, StringType, IntegerType

# Define schema WITHOUT operationType column
schema = StructType([
    StructField("id", StringType(), False),
    StructField("pk", StringType(), False),
    StructField("name", StringType(), False),
    StructField("age", IntegerType(), False)
])

# Create DataFrame with flat fields - no operationType column
items = [
    ("item1", "partition1", "Alice", 30),
    ("item2", "partition1", "Bob", 25),
    ("item3", "partition1", "Charlie", 35)
]

df = spark.createDataFrame(items, schema)

# All operations will be upserts (default behavior)
# Access the Scala API through JVM
result_jdf = spark._jvm.com.azure.cosmos.spark.CosmosItemsDataSource.writeTransactionalBatch(
    df._jdf,
    spark._jvm.scala.collection.JavaConverters.mapAsJavaMap(cosmos_config)
)

# Convert Java DataFrame back to PySpark DataFrame
from pyspark.sql import DataFrame
results = DataFrame(result_jdf, spark)

results.show()
```

**Output:**
```
+------+------------+-------------+----------+------------------+--------------+------------+
|    id|partitionKey|operationType|statusCode|isSuccessStatusCode|resultDocument|errorMessage|
+------+------------+-------------+----------+------------------+--------------+------------+
| item1|  partition1|       upsert|       201|              true|          null|        null|
| item2|  partition1|       upsert|       201|              true|          null|        null|
| item3|  partition1|       upsert|       201|              true|          null|        null|
+------+------------+-------------+----------+------------------+--------------+------------+
```

## Example 2: Mixed Operations with Operation Type Column

Specify different operations per row using the `operationType` column:

```python
from pyspark.sql.types import StructType, StructField, StringType, IntegerType

# Define schema WITH operationType column
schema = StructType([
    StructField("id", StringType(), False),
    StructField("pk", StringType(), False),
    StructField("operationType", StringType(), False),
    StructField("name", StringType(), True),
    StructField("age", IntegerType(), True)
])

# Create DataFrame with operationType column specifying different operations
mixed_operations = [
    ("new1", "pk1", "create", "Alice", 30),
    ("existing1", "pk1", "replace", "Bob", 26),
    ("any1", "pk1", "upsert", "Charlie", 35),
    ("old1", "pk1", "delete", None, None),
    ("check1", "pk1", "read", None, None)
]

df = spark.createDataFrame(mixed_operations, schema)

# Execute transactional batch
result_jdf = spark._jvm.com.azure.cosmos.spark.CosmosItemsDataSource.writeTransactionalBatch(
    df._jdf,
    spark._jvm.scala.collection.JavaConverters.mapAsJavaMap(cosmos_config)
)

results = DataFrame(result_jdf, spark)

# Check results
results.show()

# Filter for failures
print("Failed operations:")
results.filter(col("isSuccessStatusCode") == False).show()
```

**Example Output:**
```
+----------+------------+-------------+----------+------------------+--------------------+------------+
|        id|partitionKey|operationType|statusCode|isSuccessStatusCode|      resultDocument|errorMessage|
+----------+------------+-------------+----------+------------------+--------------------+------------+
|      new1|         pk1|       create|       201|              true|                null|        null|
| existing1|         pk1|      replace|       200|              true|                null|        null|
|      any1|         pk1|       upsert|       200|              true|                null|        null|
|      old1|         pk1|       delete|       204|              true|                null|        null|
|    check1|         pk1|         read|       200|              true|{"id":"check1","p...|        null|
+----------+------------+-------------+----------+------------------+--------------------+------------+
```

## Example 3: Error Handling - Exceeding 100 Operations Per Partition

The connector enforces Cosmos DB's limit of 100 operations per partition key:

```python
from pyspark.sql.types import StructType, StructField, StringType, IntegerType

# Define schema
schema = StructType([
    StructField("id", StringType(), False),
    StructField("pk", StringType(), False),
    StructField("operationType", StringType(), False),
    StructField("name", StringType(), True),
    StructField("age", IntegerType(), True)
])

# Create 101 items for the SAME partition - this will error
large_batch = [(f"item{i}", "partition1", "upsert", f"User{i}", 20 + (i % 50)) 
               for i in range(1, 102)]

df = spark.createDataFrame(large_batch, schema)

# This will throw an IllegalArgumentException
try:
    result_jdf = spark._jvm.com.azure.cosmos.spark.CosmosItemsDataSource.writeTransactionalBatch(
        df._jdf,
        spark._jvm.scala.collection.JavaConverters.mapAsJavaMap(cosmos_config)
    )
    results = DataFrame(result_jdf, spark)
    results.show()
except Exception as e:
    print(f"Error: {e}")
    # Error: Partition key 'partition1' has 101 operations, which exceeds the 
    # Cosmos DB transactional batch limit of 100 operations per partition key.
```

## Example 4: Multiple Partitions (Each Under 100 Operations)

You can process multiple partitions in a single DataFrame, as long as each partition has ≤100 operations:

```python
from pyspark.sql.types import StructType, StructField, StringType, IntegerType

# Define schema
schema = StructType([
    StructField("id", StringType(), False),
    StructField("pk", StringType(), False),
    StructField("operationType", StringType(), False),
    StructField("name", StringType(), True),
    StructField("age", IntegerType(), True)
])

# Create data for multiple partitions, each with ≤100 operations
multi_partition_data = []

# Partition 1: 50 operations
for i in range(1, 51):
    multi_partition_data.append((f"p1_item{i}", "partition1", "upsert", f"User{i}", 20 + i % 50))

# Partition 2: 75 operations
for i in range(1, 76):
    multi_partition_data.append((f"p2_item{i}", "partition2", "upsert", f"User{i}", 25 + i % 50))

# Partition 3: 100 operations (at the limit)
for i in range(1, 101):
    multi_partition_data.append((f"p3_item{i}", "partition3", "upsert", f"User{i}", 30 + i % 50))

df = spark.createDataFrame(multi_partition_data, schema)

# This will succeed - each partition has ≤100 operations
result_jdf = spark._jvm.com.azure.cosmos.spark.CosmosItemsDataSource.writeTransactionalBatch(
    df._jdf,
    spark._jvm.scala.collection.JavaConverters.mapAsJavaMap(cosmos_config)
)

results = DataFrame(result_jdf, spark)

# Total operations across all partitions
print(f"Total operations: {results.count()}")

# Successful operations
print(f"Successful: {results.filter(col('isSuccessStatusCode')).count()}")

# Check results per partition
results.groupBy("partitionKey").count().show()
```

**Output:**
```
Total operations: 225
Successful: 225

+------------+-----+
|partitionKey|count|
+------------+-----+
|  partition1|   50|
|  partition2|   75|
|  partition3|  100|
+------------+-----+
```

## Example 5: Error Handling and Result Analysis

```python
from pyspark.sql.types import StructType, StructField, StringType, IntegerType
from pyspark.sql.functions import col, count, when

# Define schema
schema = StructType([
    StructField("id", StringType(), False),
    StructField("pk", StringType(), False),
    StructField("operationType", StringType(), False),
    StructField("name", StringType(), True),
    StructField("age", IntegerType(), True)
])

# Create operations that might have errors
operations = [
    ("item1", "pk1", "create", "Alice", 30),
    ("item2", "pk1", "create", "Bob", 25),      # Might conflict if item1 succeeds
    ("item3", "pk1", "replace", "Charlie", 35),  # Might fail if doesn't exist
    ("item4", "pk1", "delete", None, None),      # Might fail if doesn't exist
    ("item5", "pk1", "read", None, None)         # Might fail if doesn't exist
]

df = spark.createDataFrame(operations, schema)

# Execute transactional batch
result_jdf = spark._jvm.com.azure.cosmos.spark.CosmosItemsDataSource.writeTransactionalBatch(
    df._jdf,
    spark._jvm.scala.collection.JavaConverters.mapAsJavaMap(cosmos_config)
)

results = DataFrame(result_jdf, spark)

# Analyze results
print("All Results:")
results.show(truncate=False)

print("\nFailed Operations:")
failed = results.filter(col("isSuccessStatusCode") == False)
failed.select("id", "operationType", "statusCode", "errorMessage").show(truncate=False)

print("\nSuccess/Failure Summary:")
results.groupBy("operationType").agg(
    count(when(col("isSuccessStatusCode") == True, 1)).alias("successful"),
    count(when(col("isSuccessStatusCode") == False, 1)).alias("failed")
).show()

print("\nStatus Code Distribution:")
results.groupBy("statusCode").count().orderBy("statusCode").show()
```

## Input DataFrame Schema Requirements

Your DataFrame should have flat columns representing document properties:

| Column | Type | Required | Description |
|--------|------|----------|-------------|
| id | String | Yes | Document identifier |
| pk (or partition key path) | String | Yes | Partition key value |
| operationType | String | No | Operation: "create", "replace", "upsert", "delete", "read" (default: "upsert") |
| ...additional columns... | Any | No | Document properties (converted to JSON) |

**Note:** The `operationType` column is metadata and not included in the stored document.

## Output DataFrame Schema

| Column | Type | Description |
|--------|------|-------------|
| id | String | Document ID |
| partitionKey | String | Partition key value used |
| operationType | String | Operation that was performed |
| statusCode | Int | HTTP status code (200-299 for success) |
| isSuccessStatusCode | Boolean | Whether the operation succeeded |
| resultDocument | String | Document content (populated for "read" operations) |
| errorMessage | String | Error details (populated on failure) |

## Constraints and Limits

- **Same Partition Key**: All operations in a batch must target the same partition key value
- **100 Operation Limit**: Cosmos DB limits batches to 100 operations per partition key
  - The connector **throws an error** if you exceed this limit
  - Solution: Reduce operations per partition or split into multiple batches
- **Atomicity**: All operations succeed or fail together within each batch
- **2MB Size Limit**: Total batch payload cannot exceed 2MB

## Common Status Codes

| Status Code | Meaning | Common Scenarios |
|-------------|---------|------------------|
| 200 | OK | Successful replace, read, or upsert (existing item) |
| 201 | Created | Successful create or upsert (new item) |
| 204 | No Content | Successful delete |
| 400 | Bad Request | Invalid document structure or operation |
| 404 | Not Found | Document doesn't exist (replace/delete/read) |
| 409 | Conflict | Document already exists (create operation) |
| 412 | Precondition Failed | ETag mismatch (if using optimistic concurrency) |
| 413 | Request Too Large | Batch payload exceeds 2MB |

## Helper Function for Easier Usage

```python
def execute_transactional_batch(spark, df, cosmos_config):
    """
    Helper function to execute transactional batch operations.
    
    Args:
        spark: SparkSession
        df: PySpark DataFrame with batch operations
        cosmos_config: Dictionary with Cosmos DB configuration
        
    Returns:
        PySpark DataFrame with results
    """
    # Convert Python dict to Java Map
    java_config = spark._jvm.scala.collection.JavaConverters.mapAsJavaMap(cosmos_config)
    
    # Execute batch through Scala API
    result_jdf = spark._jvm.com.azure.cosmos.spark.CosmosItemsDataSource.writeTransactionalBatch(
        df._jdf,
        java_config
    )
    
    # Convert back to PySpark DataFrame
    from pyspark.sql import DataFrame
    return DataFrame(result_jdf, spark)

# Usage
df = spark.createDataFrame([
    ("id1", "pk1", "upsert", "Alice", 30),
    ("id2", "pk1", "create", "Bob", 25)
], ["id", "pk", "operationType", "name", "age"])

results = execute_transactional_batch(spark, df, cosmos_config)
results.show()
```

## Best Practices

1. **Validate Partition Keys**: Ensure all operations in a DataFrame targeting the same logical batch have the same partition key
2. **Handle Errors**: Always check `isSuccessStatusCode` and `errorMessage` in results
3. **Respect Limits**: Keep operations per partition ≤100
4. **Test Atomicity**: Verify that failures roll back all operations in a batch
5. **Monitor Performance**: Use Cosmos DB diagnostics to track RU consumption
6. **Use Appropriate Operations**:
   - `create`: When you know the item doesn't exist (faster, but fails if exists)
   - `upsert`: When you're unsure (creates or updates)
   - `replace`: When you need to update existing items (fails if doesn't exist)
   - `delete`: To remove items
   - `read`: To retrieve items within a transactional context

## Troubleshooting

### Error: "Partition key 'X' has Y operations, which exceeds..."

**Cause**: More than 100 operations for a single partition key.

**Solution**: Split your data into smaller batches per partition:

```python
# Group by partition key and limit to 100 operations per partition
from pyspark.sql.window import Window
from pyspark.sql.functions import row_number

# Add row number per partition
window = Window.partitionBy("pk").orderBy("id")
df_with_row = df.withColumn("row_num", row_number().over(window))

# Filter to first 100 per partition
df_limited = df_with_row.filter(col("row_num") <= 100).drop("row_num")

# Execute batch
results = execute_transactional_batch(spark, df_limited, cosmos_config)
```

### Error: "All operations in a transactional batch must have the same partition key"

**Cause**: Operations within a batch have mismatched partition keys (internal validation error).

**Solution**: This is an internal consistency check. Ensure your data is properly grouped by partition before processing.

### Error: Java/Scala Interop Issues

**Cause**: Incorrect conversion between Python and Java types.

**Solution**: Use the helper function provided above, or ensure proper conversion:

```python
# Correct way to convert Python dict to Java Map
java_config = spark._jvm.scala.collection.JavaConverters.mapAsJavaMap(cosmos_config)

# Correct way to access Java DataFrame
df._jdf  # Not df.jdf or df._java_df
```

## Additional Resources

- [Cosmos DB Transactional Batch Documentation](https://docs.microsoft.com/azure/cosmos-db/transactional-batch)
- [Cosmos DB Spark Connector Documentation](https://aka.ms/azure-cosmos-spark-3-quickstart)
- [Configuration Parameter Reference](https://aka.ms/azure-cosmos-spark-3-config)
