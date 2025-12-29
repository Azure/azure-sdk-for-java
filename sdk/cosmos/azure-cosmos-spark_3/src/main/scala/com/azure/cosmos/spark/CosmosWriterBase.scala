// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.SparkBridgeInternal
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.{DataWriter, WriterCommitMessage}
import org.apache.spark.sql.types.StructType

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

private abstract class CosmosWriterBase(
                            userConfig: Map[String, String],
                            cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
                            diagnosticsConfig: DiagnosticsConfig,
                            inputSchema: StructType,
                            partitionId: Int,
                            taskId: Long,
                            epochId: Option[Long],
                            sparkEnvironmentInfo: String) extends DataWriter[InternalRow] {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  log.logTrace(s"Instantiated ${this.getClass.getSimpleName} - ($partitionId, $taskId, $epochId)")
  private val cosmosTargetContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)
  private val cosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, inputSchema)
  private val cosmosReadConfig = CosmosReadConfig.parseCosmosReadConfig(userConfig)
  private val cosmosSerializationConfig = CosmosSerializationConfig.parseSerializationConfig(userConfig)
  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)

  private val cacheItemReleasedCount = new AtomicInteger(0)

  private val clientCacheItem = CosmosClientCache(
    CosmosClientConfiguration(userConfig, cosmosReadConfig.readConsistencyStrategy, sparkEnvironmentInfo),
    Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
    s"CosmosWriter($partitionId, $taskId, $epochId)"
  )

  private val throughputControlClientCacheItemOpt =
    ThroughputControlHelper.getThroughputControlClientCacheItem(
      userConfig,
      clientCacheItem.context,
      Some(cosmosClientStateHandles),
      sparkEnvironmentInfo)

  private val container =
    ThroughputControlHelper.getContainer(
      userConfig,
      cosmosTargetContainerConfig,
      clientCacheItem,
      throughputControlClientCacheItemOpt)

  private val containerDefinition = SparkBridgeInternal
    .getContainerPropertiesFromCollectionCache(container)
  private val partitionKeyDefinition = containerDefinition.getPartitionKeyDefinition
  private val commitAttempt = new AtomicInteger(1)

  private val writer: AtomicReference[AsyncItemWriter] = new AtomicReference(
    if (cosmosWriteConfig.bulkEnabled) {
      new BulkWriter(
        container,
        cosmosTargetContainerConfig,
        partitionKeyDefinition,
        cosmosWriteConfig,
        diagnosticsConfig,
        getOutputMetricsPublisher(),
        commitAttempt.getAndIncrement())
    } else {
      new PointWriter(
        container,
        partitionKeyDefinition,
        cosmosWriteConfig,
        diagnosticsConfig,
        TaskContext.get(),
        getOutputMetricsPublisher())
    })

  override def write(internalRow: InternalRow): Unit = {
    val objectNode = cosmosRowConverter.fromInternalRowToObjectNode(internalRow, inputSchema)

    require(objectNode.has(CosmosConstants.Properties.Id) &&
      objectNode.get(CosmosConstants.Properties.Id).isTextual,
      s"${CosmosConstants.Properties.Id} is a mandatory field. " +
        s"But it is missing or it is not a string. Json: ${SparkUtils.objectNodeToJson(objectNode)}")

    if (cosmosWriteConfig.itemWriteStrategy == ItemWriteStrategy.ItemDeleteIfNotModified) {
      require(objectNode.has(CosmosConstants.Properties.ETag) &&
        objectNode.get(CosmosConstants.Properties.ETag).isTextual,
        s"${CosmosConstants.Properties.ETag} is a mandatory field for write strategy ItemDeleteIfNotModified. " +
          s"But it is missing or it is not a string. Json: ${SparkUtils.objectNodeToJson(objectNode)}")
    }

    val partitionKeyValue = PartitionKeyHelper.getPartitionKeyPath(objectNode, partitionKeyDefinition)
    writer.get.scheduleWrite(partitionKeyValue, objectNode)
  }

  override def commit(): WriterCommitMessage = {
    log.logInfo("commit invoked!!!")
    flushAndCloseWriterWithRetries("committing")
    new WriterCommitMessage {}
  }

  override def abort(): Unit = {
    log.logInfo("abort invoked!!!")
    try {
      writer.get.abort(true)
    } finally {
      closeClients()
    }
  }

  override def close(): Unit = {
    log.logInfo("close invoked!!!")
    try {
      flushAndCloseWriterWithRetries("closing")
    } finally {
      closeClients()
    }
  }

  private def flushAndCloseWriterWithRetries(operationName: String) = {
    try {
      writer.get.flushAndClose()
    } catch {
      case bulkWriterStaleError: BulkWriterNoProgressException =>
        bulkWriterStaleError.activeBulkWriteOperations match {
          case Some(remainingWriteOperations) =>
            log.logWarning(s"Error indicating stuck writer when $operationName write job. Retry will be attempted for "
              + s"the outstanding ${remainingWriteOperations.size} write operations.", bulkWriterStaleError)

            val bulkWriterForRetry =
              new BulkWriter(
                container,
                cosmosTargetContainerConfig,
                partitionKeyDefinition,
                cosmosWriteConfig,
                diagnosticsConfig,
                getOutputMetricsPublisher(),
                commitAttempt.getAndIncrement())
            val oldBulkWriter = writer.getAndSet(bulkWriterForRetry)

            cosmosWriteConfig.retryCommitInterceptor match {
              case Some(onRetryCommitInterceptor) =>
                log.logInfo("Invoking custom on-retry-commit interceptor...")
                onRetryCommitInterceptor.beforeRetryCommit()
              case None =>
            }

            for (operation <- remainingWriteOperations) {
              bulkWriterForRetry.scheduleWrite(operation.getPartitionKeyValue, operation.getItem[ObjectNode])
            }
            oldBulkWriter.abort(false)
            bulkWriterForRetry.flushAndClose()
          // None means not just write operations but also read-many are outstanding we can't retry
          case None =>
            log.logError(s"Error indicating stuck writer when $operationName write job. No retry possible because "
              + "of outstanding read-many operations.", bulkWriterStaleError)

            throw bulkWriterStaleError
        }
      case e: Throwable =>
        log.logError(s"Unexpected error when $operationName write job.", e)
        throw e
    }
  }

  private def closeClients() = {
    if (cacheItemReleasedCount.incrementAndGet() == 1) {
      clientCacheItem.close()
      if (throughputControlClientCacheItemOpt.isDefined) {
        throughputControlClientCacheItemOpt.get.close()
      }
    }
  }

  def getOutputMetricsPublisher(): OutputMetricsPublisherTrait
}
