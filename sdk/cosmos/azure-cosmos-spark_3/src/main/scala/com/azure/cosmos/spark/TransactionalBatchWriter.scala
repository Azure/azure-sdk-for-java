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
 * - An optional "operationType" column (String) can be provided to specify the operation ("create", "replace", "upsert", "delete", "read") for each row.
 *   If not provided, the default operation is "upsert".
 * 
 * Output DataFrame schema:
 * - id: String
 * - partitionKey: String
 * - operationType: String
 * - statusCode: Int
 * - isSuccessStatusCode: Boolean
 * - resultDocument: String (optional - for read operations)
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

  private[spark] def initializeAndBroadcastCosmosClientStatesForContainer(): (Broadcast[CosmosClientMetadataCachesSnapshots], Broadcast[String]) = {
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
        
        // Read container properties to get partition key definition
        val containerProperties: CosmosContainerProperties = 
          SparkBridgeInternal.getContainerPropertiesFromCollectionCache(container)
        val partitionKeyDefinition: PartitionKeyDefinition = 
          containerProperties.getPartitionKeyDefinition()
        
        // Warm up the client by attempting a read
        try {
          container.readItem(
            UUIDs.nonBlockingRandomUUID().toString,
            new PartitionKey(UUIDs.nonBlockingRandomUUID().toString),
            classOf[ObjectNode])
            .block()
        } catch {
          case _: CosmosException => None
        }

        val state = new CosmosClientMetadataCachesSnapshot()
        state.serialize(clientCacheItems(0).get.cosmosClient)

        var throughputControlState: Option[CosmosClientMetadataCachesSnapshot] = None
        if (clientCacheItems(1).isDefined) {
          throughputControlState = Some(new CosmosClientMetadataCachesSnapshot())
          throughputControlState.get.serialize(clientCacheItems(1).get.cosmosClient)
        }

        val metadataSnapshots = CosmosClientMetadataCachesSnapshots(state, throughputControlState)
        val metadataBroadcast = sparkSession.sparkContext.broadcast(metadataSnapshots)
        
        // Serialize PartitionKeyDefinition to JSON for broadcasting
        val partitionKeyDefJson = partitionKeyDefinition.toJson()
        val partitionKeyDefBroadcast = sparkSession.sparkContext.broadcast(partitionKeyDefJson)
        
        (metadataBroadcast, partitionKeyDefBroadcast)
      })
  }

  /**
   * Execute transactional batch operations from the input RDD.
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
    writeTransactionalBatchWithClientStates(inputRdd, operationExtraction, clientStates)
  }

  /**
   * Execute transactional batch operations with pre-initialized client states.
   * This version is used when the partition key definition is needed for extraction logic.
   * 
   * @param inputRdd RDD containing rows with batch operation data
   * @param operationExtraction Function to extract BatchOperation from each Row
   * @param clientStates Tuple of broadcasts (metadata caches, partition key definition)
   * @return DataFrame with execution results
   */
  def writeTransactionalBatchWithClientStates(
    inputRdd: RDD[Row],
    operationExtraction: Row => BatchOperation,
    clientStates: (Broadcast[CosmosClientMetadataCachesSnapshots], Broadcast[String])
  ): DataFrame = {
    val correlationActivityId = UUIDs.nonBlockingRandomUUID()
    val calledFrom = s"TransactionalBatchWriter.writeTransactionalBatch($correlationActivityId)"

    // Determine output schema
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
            clientStates._1,
            clientStates._2,
            DiagnosticsConfig.parseDiagnosticsConfig(effectiveUserConfig),
            sparkEnvironmentInfo,
            TaskContext.get,
            operations)

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
        StructField("resultDocument", StringType, nullable = true),
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
 */
private class TransactionalBatchPartitionExecutor(
  effectiveUserConfig: Map[String, String],
  outputSchema: StructType,
  diagnosticsContext: DiagnosticsContext,
  cosmosClientStates: Broadcast[CosmosClientMetadataCachesSnapshots],
  partitionKeyDefinitionBroadcast: Broadcast[String],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String,
  taskContext: TaskContext,
  operations: Iterator[BatchOperation]
) extends BasicLoggingTrait {

  private val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  // Deserialize PartitionKeyDefinition from JSON
  private val partitionKeyDefinition: PartitionKeyDefinition = 
    SparkModelBridgeInternal.createPartitionKeyDefinitionFromJson(partitionKeyDefinitionBroadcast.value)

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

  // Group operations by partition key
  // Use the full partition key values sequence as the key
  private val operationsByPartitionKey: mutable.Map[Seq[Any], mutable.ArrayBuffer[BatchOperation]] = {
    val grouped = mutable.Map[Seq[Any], mutable.ArrayBuffer[BatchOperation]]()
    operations.foreach { op =>
      val buffer = grouped.getOrElseUpdate(op.partitionKeyValues, mutable.ArrayBuffer[BatchOperation]())
      buffer += op
    }
    grouped
  }

  private val resultIterator: Iterator[Row] = executeAllBatches()

  def hasNext(): Boolean = resultIterator.hasNext
  def next(): Row = resultIterator.next()

  private def executeAllBatches(): Iterator[Row] = {
    val allResults = mutable.ArrayBuffer[Row]()
    try {
      operationsByPartitionKey.foreach { case (partitionKeyValues, ops) =>
        // Cosmos DB transactional batch limit: 100 operations per batch
        if (ops.size > 100) {
          val pkDescription = partitionKeyValues.mkString(", ")
          throw new IllegalArgumentException(
            s"Partition key [$pkDescription] has ${ops.size} operations, which exceeds the " +
            s"Cosmos DB transactional batch limit of 100 operations per partition key. " +
            s"Please reduce the number of operations for this partition key."
          )
        }
        val batchResults = executeBatchForPartitionKey(partitionKeyValues, ops.toSeq)
        allResults ++= batchResults
      }
    } finally {
      // Clean up clients
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
    }
    allResults.iterator
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

          case "read" =>
            batch.readItemOperation(op.id)

          case other =>
            throw new IllegalArgumentException(s"Unknown operation type: $other")
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
        
        val resultDocument = if (operation.operationType.toLowerCase == "read" && isSuccess) {
          Try {
            val item = result.getItem(classOf[ObjectNode])
            if (item != null) Some(item.toString) else None
          }.getOrElse(None)
        } else {
          None
        }

        val errorMessage = if (!isSuccess) {
          Some(s"Operation failed with status code ${statusCode}")
        } else {
          None
        }

        results += createResultRow(operation, statusCode, isSuccess, resultDocument, errorMessage)
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
    resultDocument: Option[String],
    errorMessage: Option[String]
  ): Row = {
    Row(
      operation.id,
      operation.partitionKey,
      operation.operationType,
      statusCode,
      isSuccessStatusCode,
      resultDocument.orNull,
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
      null,
      errorMessage
    )
  }
}
