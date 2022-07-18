// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.implementation._
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, ModelBridgeInternal}
import com.azure.cosmos.spark.ChangeFeedPartitionReader.LsnPropertyName
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.LsnAttributeName
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

private object ChangeFeedPartitionReader {
  val LsnPropertyName: String = LsnAttributeName
}

// per spark task there will be one CosmosPartitionReader.
// This provides iterator to read from the assigned spark partition
// For now we are creating only one spark partition per physical partition
private case class ChangeFeedPartitionReader
(
  partition: CosmosInputPartition,
  config: Map[String, String],
  readSchema: StructType,
  diagnosticsContext: DiagnosticsContext,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  diagnosticsConfig: DiagnosticsConfig
) extends PartitionReader[InternalRow] {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  requireNotNull(partition, "partition")
  assert(partition.continuationState.isDefined, "Argument 'partition.continuationState' must be defined here.")
  log.logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  log.logInfo(s"Reading from feed range ${partition.feedRange} of " +
    s"container ${containerTargetConfig.database}.${containerTargetConfig.container}")
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val clientCacheItem = CosmosClientCache(
    CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
    Some(cosmosClientStateHandle),
    s"ChangeFeedPartitionReader(partition $partition)")

  private val cosmosAsyncContainer =
    ThroughputControlHelper
      .getContainer(config, containerTargetConfig, clientCacheItem.client)
  SparkUtils.safeOpenConnectionInitCaches(cosmosAsyncContainer, log)

  private val cosmosSerializationConfig = CosmosSerializationConfig.parseSerializationConfig(config)
  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)

  private val changeFeedRequestOptions = {

    val startLsn =
      SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(this.partition.continuationState.get)
    log.logDebug(
      s"Request options for Range '${partition.feedRange.min}-${partition.feedRange.max}' LSN '$startLsn'")

    val options = CosmosChangeFeedRequestOptions
      .createForProcessingFromContinuation(this.partition.continuationState.get)
      .setMaxItemCount(readConfig.maxItemCount)

    ImplementationBridgeHelpers
      .CosmosChangeFeedRequestOptionsHelper
      .getCosmosChangeFeedRequestOptionsAccessor
      .setItemFactoryMethod(
        options,
        jsonNode => {
          val objectNode = cosmosRowConverter.ensureObjectNode(jsonNode)

          val row = cosmosRowConverter.fromObjectNodeToRow(readSchema,
            objectNode,
            readConfig.schemaConversionMode)

          // println("Change Feed Json Node is ", jsonNode.toPrettyString)

          ChangeFeedSparkRowItem(row, cosmosRowConverter.getChangeFeedLsn(objectNode))
        })
  }

  private val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(readSchema)

  private var operationContextAndListenerTuple: Option[OperationContextAndListenerTuple] = None

  initializeDiagnosticsIfConfigured()

  private def initializeDiagnosticsIfConfigured(): Unit = {
    if (diagnosticsConfig.mode.isDefined) {
      val taskContext = TaskContext.get
      assert(taskContext != null)

      val taskDiagnosticsContext = SparkTaskContext(
        diagnosticsContext.correlationActivityId,
        taskContext.stageId(),
        taskContext.partitionId(),
        taskContext.taskAttemptId(),
        s"${partition.feedRange} ${diagnosticsContext.details}")

      val listener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      operationContextAndListenerTuple =
        Some(new OperationContextAndListenerTuple(taskDiagnosticsContext, listener))

      ImplementationBridgeHelpers
        .CosmosChangeFeedRequestOptionsHelper
        .getCosmosChangeFeedRequestOptionsAccessor
        .setOperationContext(changeFeedRequestOptions, operationContextAndListenerTuple.get)
    }
  }

  private lazy val iterator: TransientIOErrorsRetryingIterator[ChangeFeedSparkRowItem] =
    new TransientIOErrorsRetryingIterator[ChangeFeedSparkRowItem](
      continuationToken => {
        if (!Strings.isNullOrWhiteSpace(continuationToken)) {
          ModelBridgeInternal.setChangeFeedRequestOptionsContinuation(continuationToken, changeFeedRequestOptions)
        } else {
          // scalastyle:off null
          ModelBridgeInternal.setChangeFeedRequestOptionsContinuation(null, changeFeedRequestOptions)
          // scalastyle:on null
        }
        cosmosAsyncContainer.queryChangeFeed(changeFeedRequestOptions, classOf[ChangeFeedSparkRowItem])
      },
      readConfig.maxItemCount,
      readConfig.prefetchBufferSize,
      operationContextAndListenerTuple
    )

  override def next(): Boolean = {
    this.iterator.hasNext && this.validateNextLsn
  }

  private[this] def validateNextLsn: Boolean = {
    this.partition.endLsn match {
      case None =>
        // In batch mode endLsn is cleared - we will always continue reading until the change feed is
        // completely drained so all partitions return 304
        true
      case Some(endLsn) =>
        // In streaming mode we only continue until we hit the endOffset's continuation Lsn
        val node = this.iterator.head()
        assert(node.lsn != null, "Change feed responses must have _lsn property.")
        assert(node.lsn != "", "Change feed responses must have non empty _lsn.")
        val nextLsn = SparkBridgeImplementationInternal.toLsn(node.lsn)

        nextLsn <= endLsn
    }
  }

  override def get(): InternalRow = {
    cosmosRowConverter.fromRowToInternalRow(this.iterator.next().row, rowSerializer)
  }

  override def close(): Unit = {
    this.iterator.close()
    RowSerializerPool.returnSerializerToPool(readSchema, rowSerializer)
    clientCacheItem.close()
  }
}
