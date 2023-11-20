// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.{DataWriter, WriterCommitMessage}
import org.apache.spark.sql.types.StructType

import java.util.concurrent.atomic.AtomicInteger

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
  private val cosmosSerializationConfig = CosmosSerializationConfig.parseSerializationConfig(userConfig)
  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)

  private val cacheItemReleasedCount = new AtomicInteger(0)

  private val clientCacheItem = CosmosClientCache(
    CosmosClientConfiguration(userConfig, useEventualConsistency = true, sparkEnvironmentInfo),
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
  SparkUtils.safeOpenConnectionInitCaches(container, log)

  private val containerDefinition = container.read().block().getProperties
  private val partitionKeyDefinition = containerDefinition.getPartitionKeyDefinition

  private val writer = if (cosmosWriteConfig.bulkEnabled) {
    new BulkWriter(
      container,
      partitionKeyDefinition,
      cosmosWriteConfig,
      diagnosticsConfig,
      getOutputMetricsPublisher())
  } else {
    new PointWriter(
      container,
      partitionKeyDefinition,
      cosmosWriteConfig,
      diagnosticsConfig,
      TaskContext.get(),
      getOutputMetricsPublisher())
  }

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
    writer.scheduleWrite(partitionKeyValue, objectNode)
  }

  override def commit(): WriterCommitMessage = {
    log.logInfo("commit invoked!!!")
    writer.flushAndClose()

    new WriterCommitMessage {}
  }

  override def abort(): Unit = {
    log.logInfo("abort invoked!!!")
    writer.abort()
    if (cacheItemReleasedCount.incrementAndGet() == 1) {
      clientCacheItem.close()
    }
  }

  override def close(): Unit = {
    log.logInfo("close invoked!!!")
    writer.flushAndClose()
    if (cacheItemReleasedCount.incrementAndGet() == 1) {
      clientCacheItem.close()
      if (throughputControlClientCacheItemOpt.isDefined) {
        throughputControlClientCacheItemOpt.get.close()
      }
    }
  }

  def getOutputMetricsPublisher(): OutputMetricsPublisherTrait
}
