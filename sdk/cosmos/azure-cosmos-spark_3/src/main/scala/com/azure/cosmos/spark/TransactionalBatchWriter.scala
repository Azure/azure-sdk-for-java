// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosBatch, CosmosBatchResponse, CosmosContainerProperties, PartitionKey, PartitionKeyDefinition, SparkModelBridgeInternal}
import com.azure.cosmos.spark.CosmosPredicates.assertOnSparkDriver
import com.azure.cosmos.spark.diagnostics.{BasicLoggingTrait, DiagnosticsContext}
import com.azure.cosmos.{CosmosAsyncContainer, CosmosException, SparkBridgeInternal}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, UUIDs, Utils}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

/**
 * TransactionalBatchWriter executes transactional batch operations against Cosmos DB.
 * Operations are grouped by partition key and executed atomically per partition.
 * Follows the same pattern as CosmosReadManyReader for consistency.
 * 
 * Input DataFrame schema:
 * - Flat columns corresponding to the properties of the Cosmos DB item to be operated on.
 * - The partition key column name must match the container's partition key path (e.g., "pk" if the path is "/pk").
 * - The "id" column (String) is required.
 * - An optional "operationType" column (String) can be provided to specify the operation ("create", "replace", "upsert", "delete") for each row.
 *   If not provided, the default operation is "upsert".
 * 
 * Output DataFrame schema:
 * - id: String
 * - partitionKey: String
 * - operationType: String
 * - statusCode: Int
 * - isSuccessStatusCode: Boolean
 * - errorMessage: String (optional)
 */
private[spark] class TransactionalBatchWriter(
  val outputSchema: StructType,
  val userConfig: Map[String, String]
) extends BasicLoggingTrait with Serializable {

  val effectiveUserConfig: Map[String, String] = CosmosConfig.getEffectiveConfig(
    databaseName = None,
    containerName = None,
    userConfig)

  val clientConfig: CosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(effectiveUserConfig)
  val readConfig: CosmosReadConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  val cosmosContainerConfig: CosmosContainerConfig =
    CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)

  val tableName: String = s"com.azure.cosmos.spark.items.${clientConfig.accountName}." +
    s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}"

  private lazy val sparkSession = {
    assertOnSparkDriver()
    SparkSession.active
  }

  val sparkEnvironmentInfo: String = CosmosClientConfiguration.getSparkEnvironmentInfo(Some(sparkSession))
  logTrace(s"Instantiated ${this.getClass.getSimpleName} for $tableName")

  /**
   * Get partition key column names from the container.
   * Maps partition key paths (e.g., "/pk") to DataFrame column names (e.g., "pk").
   * This is safe to call on the driver as it only reads container metadata.
   * 
   * @return Sequence of column names that comprise the partition key
   */
  def getPartitionKeyColumnNames(): Seq[String] = {
    val calledFrom = s"TransactionalBatchWriter($tableName).getPartitionKeyColumnNames"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(
          CosmosClientCache(
            CosmosClientConfiguration(
              effectiveUserConfig,
              readConsistencyStrategy = readConfig.readConsistencyStrategy,
              sparkEnvironmentInfo),
            None,
            calledFrom)),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          effectiveUserConfig,
          calledFrom,
          None,
          sparkEnvironmentInfo)
      ))
      .to(clientCacheItems => {
        val container =
          ThroughputControlHelper.getContainer(
            effectiveUserConfig,
            cosmosContainerConfig,
            clientCacheItems(0).get,
            clientCacheItems(1))

        val containerProperties = container.read().block().getProperties
        val partitionKeyDef = containerProperties.getPartitionKeyDefinition
        val partitionKeyPaths = partitionKeyDef.getPaths.asScala.toSeq

        // Map partition key paths (e.g., "/pk") to column names (e.g., "pk")
        partitionKeyPaths.map { path =>
          // Remove leading slash and any nested path separators
          path.stripPrefix("/").split("/").head
        }
      })
  }

  private[spark] def initializeAndBroadcastCosmosClientStatesForContainer(): Broadcast[CosmosClientMetadataCachesSnapshots] = {
    val calledFrom = s"TransactionalBatchWriter($tableName).initializeAndBroadcastCosmosClientStateForContainer"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(
          CosmosClientCache(
            CosmosClientConfiguration(
              effectiveUserConfig,
              readConsistencyStrategy = readConfig.readConsistencyStrategy,
              sparkEnvironmentInfo),
            None,
            calledFrom)),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          effectiveUserConfig,
          calledFrom,
          None,
          sparkEnvironmentInfo)
      ))
      .to(clientCacheItems => {
        val container =
          ThroughputControlHelper.getContainer(
            effectiveUserConfig,
            cosmosContainerConfig,
            clientCacheItems(0).get,
            clientCacheItems(1))

        val state = new CosmosClientMetadataCachesSnapshot()
        state.serialize(clientCacheItems(0).get.cosmosClient)

        var throughputControlState: Option[CosmosClientMetadataCachesSnapshot] = None
        if (clientCacheItems(1).isDefined) {
          throughputControlState = Some(new CosmosClientMetadataCachesSnapshot())
          throughputControlState.get.serialize(clientCacheItems(1).get.cosmosClient)
        }

        val metadataSnapshots = CosmosClientMetadataCachesSnapshots(state, throughputControlState)
        sparkSession.sparkContext.broadcast(metadataSnapshots)
      })
  }

  /**
   * Execute transactional batch operations from the input RDD with pre-extracted operations.
   * Operations are grouped by partition key and executed atomically.
   * This follows the same pattern as CosmosReadManyReader.readMany().
   * 
   * @param inputRdd RDD containing rows with batch operation data
   * @param operationExtraction Function to extract BatchOperation from each Row
   * @return DataFrame with execution results
   */
  def writeTransactionalBatch(
    inputRdd: RDD[Row],
    operationExtraction: Row => BatchOperation
  ): DataFrame = {
    val clientStates = initializeAndBroadcastCosmosClientStatesForContainer()
    writeTransactionalBatchWithPreExtractedOperations(inputRdd, operationExtraction, clientStates)
  }

  /**
   * Execute transactional batch operations from raw Rows.
   * Partition key definition is fetched on each executor (like ChangeFeedPartitionReader).
   * This eliminates the need for JSON serialization and broadcasting of PartitionKeyDefinition.
   * 
   * @param inputRdd RDD containing raw rows
   * @param clientStates Broadcast of metadata caches
   * @return DataFrame with execution results
   */
  def writeTransactionalBatchWithRowExtraction(
    inputRdd: RDD[Row],
    clientStates: Broadcast[CosmosClientMetadataCachesSnapshots]
  ): DataFrame = {
    val correlationActivityId = UUIDs.nonBlockingRandomUUID()
    val calledFrom = s"TransactionalBatchWriter.writeTransactionalBatchWithRowExtraction($correlationActivityId)"

    val outputSchema = getOutputSchema()

    sparkSession.sqlContext.createDataFrame(
      inputRdd.mapPartitionsWithIndex(
        (partitionIndex: Int, rowsIterator: Iterator[Row]) => {
          logInfo(s"Executing transactional batch operations for Activity $correlationActivityId " +
            s"on input partition [$partitionIndex] ${tableName}")

          val executor = new TransactionalBatchPartitionExecutor(
            effectiveUserConfig,
            outputSchema,
            DiagnosticsContext(correlationActivityId, partitionIndex.toString),
            clientStates,
            diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(effectiveUserConfig),
            sparkEnvironmentInfo,
            TaskContext.get,
            rowsIterator = Some(rowsIterator),
            operationsIterator = None)

          new Iterator[Row] {
            override def hasNext: Boolean = executor.hasNext()
            override def next(): Row = executor.next()
          }
        },
        preservesPartitioning = true
      ),
      outputSchema)
  }

  /**
   * Execute transactional batch operations with pre-extracted BatchOperations.
   * This version is used when custom extraction logic is provided.
   * 
   * @param inputRdd RDD containing rows with batch operation data
   * @param operationExtraction Function to extract BatchOperation from each Row
   * @param clientStates Broadcast of metadata caches
   * @return DataFrame with execution results
   */
  def writeTransactionalBatchWithPreExtractedOperations(
    inputRdd: RDD[Row],
    operationExtraction: Row => BatchOperation,
    clientStates: Broadcast[CosmosClientMetadataCachesSnapshots]
  ): DataFrame = {
    val correlationActivityId = UUIDs.nonBlockingRandomUUID()
    val calledFrom = s"TransactionalBatchWriter.writeTransactionalBatchWithPreExtractedOperations($correlationActivityId)"

    val outputSchema = getOutputSchema()

    sparkSession.sqlContext.createDataFrame(
      inputRdd.mapPartitionsWithIndex(
        (partitionIndex: Int, batchOperationsIterator: Iterator[Row]) => {
          val operations: Iterator[BatchOperation] = batchOperationsIterator
            .map(row => operationExtraction.apply(row))

          logInfo(s"Executing transactional batch operations for Activity $correlationActivityId " +
            s"on input partition [$partitionIndex] ${tableName}")

          val executor = new TransactionalBatchPartitionExecutor(
            effectiveUserConfig,
            outputSchema,
            DiagnosticsContext(correlationActivityId, partitionIndex.toString),
            clientStates,
            diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(effectiveUserConfig),
            sparkEnvironmentInfo,
            TaskContext.get,
            rowsIterator = None,
            operationsIterator = Some(operations))

          new Iterator[Row] {
            override def hasNext: Boolean = executor.hasNext()
            override def next(): Row = executor.next()
          }
        },
        preservesPartitioning = true
      ),
      outputSchema)
  }

  private def getOutputSchema(): StructType = {
    if (outputSchema != null) {
      outputSchema
    } else {
      StructType(Seq(
        StructField("id", StringType, nullable = false),
        StructField("partitionKey", StringType, nullable = false),
        StructField("operationType", StringType, nullable = false),
        StructField("statusCode", IntegerType, nullable = false),
        StructField("isSuccessStatusCode", BooleanType, nullable = false),
        StructField("errorMessage", StringType, nullable = true)
      ))
    }
  }
}

/**
 * Represents a batch operation to be executed
 */
private case class BatchOperation(
  id: String,
  partitionKeyValues: Seq[Any],
  operationType: String,
  documentNode: ObjectNode
) {
  // Backwards compatibility helper: get partition key as string
  def partitionKey: String = partitionKeyValues.mkString(",")
}

/**
 * Executes transactional batch operations for a single partition.
 * Groups operations by partition key and executes them atomically.
 * Supports two modes:
 * 1. Row extraction mode: Fetches PartitionKeyDefinition from container and extracts BatchOperations from Rows
 * 2. Pre-extracted mode: Uses pre-extracted BatchOperations (for custom extraction logic)
 */
private class TransactionalBatchPartitionExecutor(
  effectiveUserConfig: Map[String, String],
  outputSchema: StructType,
  diagnosticsContext: DiagnosticsContext,
  cosmosClientStates: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String,
  taskContext: TaskContext,
  rowsIterator: Option[Iterator[Row]],
  operationsIterator: Option[Iterator[BatchOperation]]
) extends BasicLoggingTrait {

  private val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)

  private val clientCacheItem = CosmosClientCache(
    CosmosClientConfiguration(
      effectiveUserConfig,
      readConsistencyStrategy = readConfig.readConsistencyStrategy,
      sparkEnvironmentInfo),
    Some(cosmosClientStates.value.cosmosClientMetadataCaches),
    s"TransactionalBatchPartitionExecutor(${diagnosticsContext.correlationActivityId})"
  )

  private val throughputControlClientCacheItemOpt =
    ThroughputControlHelper.getThroughputControlClientCacheItem(
      effectiveUserConfig,
      clientCacheItem.context,
      Some(cosmosClientStates),
      sparkEnvironmentInfo)

  private val container: CosmosAsyncContainer =
    ThroughputControlHelper.getContainer(
      effectiveUserConfig,
      cosmosContainerConfig,
      clientCacheItem,
      throughputControlClientCacheItemOpt)

  // Fetch PartitionKeyDefinition from container (like ChangeFeedPartitionReader)
  // Only needed when processing raw rows
  private val partitionKeyDefinition: Option[PartitionKeyDefinition] = 
    if (rowsIterator.isDefined) {
      Some(SparkBridgeInternal
        .getContainerPropertiesFromCollectionCache(container)
        .getPartitionKeyDefinition)
    } else {
      None
    }

  // Extract operations from rows or use pre-extracted operations
  private val operations: Iterator[BatchOperation] = {
    if (rowsIterator.isDefined) {
      // Extract from rows using partition key definition
      val pkDef = partitionKeyDefinition.get
      rowsIterator.get.map(row => extractBatchOperationFromRow(row, pkDef))
    } else {
      // Use pre-extracted operations
      operationsIterator.get
    }
  }

  // Process operations in streaming fashion - no Map-based grouping!
  // Thanks to repartitioning + sorting in CosmosItemsDataSource:
  // 1. All same partition key operations arrive consecutively (sorted)
  // 2. All same partition key operations are in the same Spark partition (repartitioned)
  // This allows streaming processing with a simple buffer for the current partition key
  private val resultIterator: Iterator[Row] = executeAllBatchesStreaming()

  def hasNext(): Boolean = resultIterator.hasNext
  def next(): Row = resultIterator.next()

  /**
   * Extract BatchOperation from Row using PartitionKeyDefinition.
   * This replicates the logic from CosmosItemsDataSource.createBatchOperationExtraction.
   */
  private def extractBatchOperationFromRow(row: Row, pkDef: PartitionKeyDefinition): BatchOperation = {
    // Get partition key paths from definition
    val partitionKeyPaths: Seq[String] = pkDef.getPaths.asScala.toSeq

    // Get id field
    val idFieldIndex = row.schema.fieldIndex("id")
    val id = row.getString(idFieldIndex)

    // Get operationType (default to upsert)
    val operationType = if (row.schema.fieldNames.contains("operationType")) {
      val opTypeIndex = row.schema.fieldIndex("operationType")
      if (row.isNullAt(opTypeIndex)) "upsert" else row.getString(opTypeIndex)
    } else {
      "upsert"
    }

    // Convert row to ObjectNode
    val documentNode = CosmosRowConverter.get(CosmosSerializationConfig.parseSerializationConfig(effectiveUserConfig))
      .fromRowToObjectNode(row)

    // Extract partition key values based on partition key definition
    val partitionKeyValues: Seq[Any] = partitionKeyPaths.map { path =>
      val fieldName = if (path.startsWith("/")) path.substring(1) else path
      val valueNode = documentNode.get(fieldName)

      if (Option(valueNode).isEmpty || valueNode.isNull) {
        throw new IllegalArgumentException(
          s"Partition key field '$fieldName' is missing or null in row with id '$id'. " +
            s"All partition key fields must be present and non-null for transactional batch operations.")
      }

      // Extract value based on type
      if (valueNode.isTextual) {
        valueNode.asText()
      } else if (valueNode.isNumber) {
        if (valueNode.isIntegralNumber) {
          if (valueNode.canConvertToLong) valueNode.asLong()
          else valueNode.asInt()
        } else {
          valueNode.asDouble()
        }
      } else if (valueNode.isBoolean) {
        valueNode.asBoolean()
      } else {
        throw new IllegalArgumentException(
          s"Unsupported partition key value type for field '$fieldName': ${valueNode.getNodeType}")
      }
    }

    BatchOperation(id, partitionKeyValues, operationType, documentNode)
  }

  /**
   * Execute batches in streaming fashion without materializing all operations in memory.
   * Leverages the fact that operations are sorted by partition key (thanks to repartitioning).
   * Maintains a buffer for the current partition key and flushes when partition key changes.
   */
  private def executeAllBatchesStreaming(): Iterator[Row] = {
    // Track partition key transitions for validation/debugging (disabled by default)
    val logTransitions = effectiveUserConfig.getOrElse(
      "spark.cosmos.write.transactionalBatch.logPartitionKeyTransitions", "false").toBoolean
    var partitionKeyTransitionCount = 0
    
    new Iterator[Row] {
      private var currentPartitionKeyOpt: Option[Seq[Any]] = None
      private var currentBuffer: mutable.ArrayBuffer[BatchOperation] = mutable.ArrayBuffer[BatchOperation]()
      private var pendingResults: Iterator[Row] = Iterator.empty
      private var exhausted = false
      private var clientsClosed = false

      override def hasNext: Boolean = {
        // If we have pending results, return true
        if (pendingResults.hasNext) {
          return true
        }

        // If operations iterator is exhausted and no pending buffer, cleanup and return false
        if (!operations.hasNext && currentBuffer.isEmpty) {
          if (!clientsClosed) {
            closeClients()
          }
          return false
        }

        // Process next operation or flush buffer
        while (operations.hasNext || currentBuffer.nonEmpty) {
          if (operations.hasNext) {
            val op = operations.next()

            // Check if partition key changed (or first operation)
            if (currentPartitionKeyOpt.isEmpty || currentPartitionKeyOpt.get != op.partitionKeyValues) {
              // Track transition
              if (currentPartitionKeyOpt.isDefined) {
                partitionKeyTransitionCount += 1
                if (logTransitions) {
                  logInfo(s"Partition key transition #$partitionKeyTransitionCount: ${currentPartitionKeyOpt.get.mkString(",")} -> ${op.partitionKeyValues.mkString(",")}")
                }
              } else if (logTransitions) {
                logInfo(s"First partition key: ${op.partitionKeyValues.mkString(",")}")
              }
              
              // Flush current buffer if not empty
              if (currentBuffer.nonEmpty) {
                pendingResults = flushCurrentBuffer()
                if (pendingResults.hasNext) {
                  // Start new buffer with this operation
                  currentPartitionKeyOpt = Some(op.partitionKeyValues)
                  currentBuffer = mutable.ArrayBuffer[BatchOperation](op)
                  return true
                }
              }

              // Start new buffer
              currentPartitionKeyOpt = Some(op.partitionKeyValues)
              currentBuffer = mutable.ArrayBuffer[BatchOperation](op)
            } else {
              // Same partition key - add to buffer
              currentBuffer += op
            }
          } else {
            // No more operations - flush final buffer
            if (currentBuffer.nonEmpty) {
              pendingResults = flushCurrentBuffer()
              if (!clientsClosed) {
                closeClients()
              }
              return pendingResults.hasNext
            }
          }
        }

        // All done
        if (!clientsClosed) {
          closeClients()
        }
        false
      }

      override def next(): Row = {
        if (!hasNext) {
          throw new NoSuchElementException("No more results")
        }
        pendingResults.next()
      }

      private def flushCurrentBuffer(): Iterator[Row] = {
        val partitionKeyValues = currentPartitionKeyOpt.get
        val ops = currentBuffer.toSeq

        // Validate batch size
        if (ops.size > 100) {
          val pkDescription = partitionKeyValues.mkString(", ")
          throw new IllegalArgumentException(
            s"Partition key [$pkDescription] has ${ops.size} operations, which exceeds the " +
            s"Cosmos DB transactional batch limit of 100 operations per partition key. " +
            s"Please reduce the number of operations for this partition key."
          )
        }

        // Execute batch and return results
        val results = executeBatchForPartitionKey(partitionKeyValues, ops)
        currentBuffer.clear()
        results.iterator
      }

      private def closeClients(): Unit = {
        if (logTransitions) {
          logInfo(s"Partition key transition summary: $partitionKeyTransitionCount transitions")
        }
        try {
          clientCacheItem.close()
        } catch {
          case e: Exception =>
            logError(s"Error closing main client cache item: ${e.getMessage}", e)
        }
        if (throughputControlClientCacheItemOpt.isDefined) {
          try {
            throughputControlClientCacheItemOpt.get.close()
          } catch {
            case e: Exception =>
              logError(s"Error closing throughput control client cache item: ${e.getMessage}", e)
          }
        }
        clientsClosed = true
      }
    }
  }

  private def executeBatchForPartitionKey(
    partitionKeyValues: Seq[Any],
    operations: Seq[BatchOperation]
  ): Seq[Row] = {
    // Validate that all operations have the same partition key
    val mismatchedOps = operations.filter(_.partitionKeyValues != partitionKeyValues)
    if (mismatchedOps.nonEmpty) {
      val mismatchedKeys = mismatchedOps.map(_.partitionKeyValues.mkString("[", ", ", "]")).distinct.mkString(", ")
      val expectedKey = partitionKeyValues.mkString("[", ", ", "]")
      throw new IllegalArgumentException(
        s"All operations in a transactional batch must have the same partition key. " +
        s"Expected: $expectedKey, but found: $mismatchedKeys. " +
        s"Number of mismatched operations: ${mismatchedOps.size}."
      )
    }
    
    Try {
      // Create PartitionKey using PartitionKeyBuilder
      // This correctly handles hierarchical partition keys of any level (1-3)
      val builder = new com.azure.cosmos.models.PartitionKeyBuilder()
      partitionKeyValues.foreach { value =>
        // PartitionKeyBuilder.add() has overloads for String, Double, and Boolean
        // Cast the value to the appropriate type
        value match {
          case s: String => builder.add(s)
          case d: Double => builder.add(d)
          case f: Float => builder.add(f.toDouble)
          case i: Int => builder.add(i.toDouble)
          case l: Long => builder.add(l.toDouble)
          case b: Boolean => builder.add(b)
          case other => builder.add(other.toString) // Fallback to string representation
        }
      }
      val partitionKey: PartitionKey = builder.build()
      
      val pkDescription = partitionKeyValues.mkString("[", ", ", "]")
      logTrace(s"Creating batch with partition key: $pkDescription (${partitionKeyValues.size} levels)")
      val batch = CosmosBatch.createCosmosBatch(partitionKey)

      // Add operations to batch
      operations.foreach { op =>
        op.operationType.toLowerCase match {
          case "create" =>
            logTrace(s"Adding create operation for id=${op.documentNode.get("id").asText()}, pk=$pkDescription")
            logTrace(s"Document JSON: ${op.documentNode.toString}")
            batch.createItemOperation(op.documentNode)

          case "replace" =>
            batch.replaceItemOperation(op.id, op.documentNode)

          case "upsert" =>
            batch.upsertItemOperation(op.documentNode)

          case "delete" =>
            batch.deleteItemOperation(op.id)

          case other =>
            throw new IllegalArgumentException(s"Unsupported operation type: $other. Supported types are: create, replace, upsert, delete")
        }
      }

      // Execute batch
      val batchResponse: CosmosBatchResponse = container.executeCosmosBatch(batch).block()
      
      logTrace(s"Batch response status: ${batchResponse.getStatusCode}, isSuccessStatusCode: ${batchResponse.isSuccessStatusCode}")
      logTrace(s"Batch response error message: ${batchResponse.getErrorMessage}")
      logTrace(s"Batch response diagnostics: ${batchResponse.getDiagnostics}")
      
      // Check individual operation results
      if (batchResponse.getResults != null) {
        batchResponse.getResults.asScala.zipWithIndex.foreach { case (result, idx) =>
          logTrace(s"Operation $idx: statusCode=${result.getStatusCode}, subStatusCode=${result.getSubStatusCode}")
        }
      }

      // Process results
      processResponse(batchResponse, operations)
    } match {
      case Success(results) => results
      case Failure(exception: CosmosException) =>
        val pkDescription = partitionKeyValues.mkString("[", ", ", "]")
        logError(s"Cosmos batch execution failed for partition key $pkDescription: ${exception.getMessage}", exception)
        operations.map { op =>
          createErrorRow(op, exception.getStatusCode, exception.getMessage)
        }
      case Failure(exception) =>
        val pkDescription = partitionKeyValues.mkString("[", ", ", "]")
        logError(s"Batch execution failed for partition key $pkDescription: ${exception.getMessage}", exception)
        operations.map { op =>
          createErrorRow(op, 500, exception.getMessage)
        }
    }
  }

  private def processResponse(
    response: CosmosBatchResponse,
    operations: Seq[BatchOperation]
  ): Seq[Row] = {
    val results = mutable.ArrayBuffer[Row]()
    
    // If the overall batch failed, all operations failed
    if (!response.isSuccessStatusCode) {
      logWarning(s"Batch failed with status ${response.getStatusCode}: ${response.getErrorMessage}")
      operations.foreach { op =>
        results += createErrorRow(op, response.getStatusCode, 
          Option(response.getErrorMessage).getOrElse("Batch operation failed"))
      }
      return results.toSeq
    }
    
    val batchResults = response.getResults.asScala

    for (i <- operations.indices) {
      val operation = operations(i)
      val result = if (i < batchResults.size) batchResults(i) else null

      if (result != null) {
        val statusCode = result.getStatusCode
        val isSuccess = statusCode >= 200 && statusCode < 300

        val errorMessage = if (!isSuccess) {
          val subStatusCode = result.getSubStatusCode
          if (subStatusCode != 0) {
            Some(s"Operation failed with status code ${statusCode}, sub-status code ${subStatusCode}")
          } else {
            Some(s"Operation failed with status code ${statusCode}")
          }
        } else {
          None
        }

        results += createResultRow(operation, statusCode, isSuccess, errorMessage)
      } else {
        results += createErrorRow(operation, 500, "No result returned for operation")
      }
    }

    results.toSeq
  }

  private def createResultRow(
    operation: BatchOperation,
    statusCode: Int,
    isSuccessStatusCode: Boolean,
    errorMessage: Option[String]
  ): Row = {
    Row(
      operation.id,
      operation.partitionKey,
      operation.operationType,
      statusCode,
      isSuccessStatusCode,
      errorMessage.orNull
    )
  }

  private def createErrorRow(operation: BatchOperation, statusCode: Int, errorMessage: String): Row = {
    Row(
      operation.id,
      operation.partitionKey,
      operation.operationType,
      statusCode,
      false,
      errorMessage
    )
  }
}
