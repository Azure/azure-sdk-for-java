// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, CosmosEndToEndOperationLatencyPolicyConfigBuilder, CosmosItemSerializerNoExceptionWrapping, SparkBridgeInternal}
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, ObjectNodeMap, SparkRowItem, Utils}
import com.azure.cosmos.BridgeInternal
import com.azure.cosmos.models.{CosmosReadManyRequestOptions, ModelBridgeInternal, PartitionKey, PartitionKeyDefinition, SqlQuerySpec}
import com.azure.cosmos.spark.BulkWriter.getThreadInfo
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.IdAttributeName
import com.azure.cosmos.spark.diagnostics.{DetailedFeedDiagnosticsProvider, DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

import java.util
import java.util.concurrent.atomic.AtomicBoolean

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] case class ItemsPartitionReaderWithReadManyByPartitionKey
(
  config: Map[String, String],
  feedRange: NormalizedRange,
  readSchema: StructType,
  diagnosticsContext: DiagnosticsContext,
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String,
  taskContext: TaskContext,
  readManyPartitionKeys: Iterator[PartitionKey]
)
  extends PartitionReader[InternalRow] {

  private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  private val readManyOptions = new CosmosReadManyRequestOptions()
  private val readManyOptionsImpl = ImplementationBridgeHelpers
    .CosmosReadManyRequestOptionsHelper
    .getCosmosReadManyRequestOptionsAccessor
    .getImpl(readManyOptions)

  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  ThroughputControlHelper.populateThroughputControlGroupName(readManyOptionsImpl, readConfig.throughputControlConfig)

  private val operationContext = {
    assert(taskContext != null)

    SparkTaskContext(diagnosticsContext.correlationActivityId,
      taskContext.stageId(),
      taskContext.partitionId(),
      taskContext.taskAttemptId(),
      feedRange.toString)
  }

  private val operationContextAndListenerTuple: Option[OperationContextAndListenerTuple] = {
    if (diagnosticsConfig.mode.isDefined) {
      val listener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      val ctxAndListener = new OperationContextAndListenerTuple(operationContext, listener)

      readManyOptionsImpl
        .setOperationContextAndListenerTuple(ctxAndListener)

      Some(ctxAndListener)
    } else {
      None
    }
  }

  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}, Context: ${operationContext.toString} $getThreadInfo")

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)

  log.logInfo(s"Using ReadManyByPartitionKey from feed range $feedRange of " +
    s"container ${containerTargetConfig.database}.${containerTargetConfig.container} - " +
    s"correlationActivityId ${diagnosticsContext.correlationActivityId}, " +
    s"Context: ${operationContext.toString} $getThreadInfo")

  private val clientCacheItem = CosmosClientCache(
    CosmosClientConfiguration(config, readConfig.readConsistencyStrategy, sparkEnvironmentInfo),
    Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
    s"ItemsPartitionReaderWithReadManyByPartitionKey($feedRange, ${containerTargetConfig.database}.${containerTargetConfig.container})"
  )

  private val throughputControlClientCacheItemOpt =
    ThroughputControlHelper.getThroughputControlClientCacheItem(
      config,
      clientCacheItem.context,
      Some(cosmosClientStateHandles),
      sparkEnvironmentInfo)

  private val cosmosAsyncContainer =
    ThroughputControlHelper.getContainer(
      config,
      containerTargetConfig,
      clientCacheItem,
      throughputControlClientCacheItemOpt)

  private val partitionKeyDefinition: PartitionKeyDefinition = {
    TransientErrorsRetryPolicy.executeWithRetry(() => {
      SparkBridgeInternal
        .getContainerPropertiesFromCollectionCache(cosmosAsyncContainer).getPartitionKeyDefinition
    })
  }

  private val cosmosSerializationConfig = CosmosSerializationConfig.parseSerializationConfig(config)
  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)

  readManyOptionsImpl
    .setCustomItemSerializer(
      new CosmosItemSerializerNoExceptionWrapping {
        override def serialize[T](item: T): util.Map[String, AnyRef] = {
          throw new UnsupportedOperationException(
            s"Serialization is not supported by the custom item serializer in " +
              s"ItemsPartitionReaderWithReadManyByPartitionKey; this serializer is intended " +
              s"for deserializing read-many responses into SparkRowItem only. " +
              s"Unexpected item type: ${if (item == null) "null" else item.getClass.getName}"
          )
        }

        override def deserialize[T](jsonNodeMap: util.Map[String, AnyRef], classType: Class[T]): T = {
          if (jsonNodeMap == null) {
            throw new IllegalStateException("The 'jsonNodeMap' should never be null here.")
          }

          if (classType != classOf[SparkRowItem]) {
            throw new IllegalStateException("The 'classType' must be 'classOf[SparkRowItem])' here.")
          }

          val objectNode: ObjectNode = jsonNodeMap match {
            case map: ObjectNodeMap =>
              map.getObjectNode
            case _ =>
              Utils.getSimpleObjectMapper.convertValue(jsonNodeMap, classOf[ObjectNode])
          }

          val partitionKey = PartitionKeyHelper.getPartitionKeyPath(objectNode, partitionKeyDefinition)

          val row = cosmosRowConverter.fromObjectNodeToRow(readSchema,
            objectNode,
            readConfig.schemaConversionMode)

          SparkRowItem(row, getPartitionKeyForFeedDiagnostics(partitionKey)).asInstanceOf[T]
        }
      }
    )

  // Collect all PK values upfront - readManyByPartitionKey needs the full list to
  // group by physical partition (the SDK batches internally per physical partition).
  // Deduplicate using the canonical PartitionKeyInternal JSON representation so that
  // equivalent PKs built from different runtime types (Int vs Long vs Double) are
  // collapsed, and distinct PKs that happen to toString() identically are not.
  private lazy val pkList = {
    val seen = new java.util.LinkedHashMap[String, PartitionKey]()
    readManyPartitionKeys.foreach(pk => {
      val key = BridgeInternal.getPartitionKeyInternal(pk).toJson
      seen.putIfAbsent(key, pk)
    })
    new java.util.ArrayList[PartitionKey](seen.values())
  }

  private val endToEndTimeoutPolicy =
    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(
      java.time.Duration.ofSeconds(CosmosConstants.readOperationEndToEndTimeoutInSeconds))
      .enable(true)
      .build

  readManyOptionsImpl.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndTimeoutPolicy)

  private trait CloseableSparkRowItemIterator {
    def hasNext: Boolean
    def next(): SparkRowItem
    def close(): Unit
  }

  private object EmptySparkRowItemIterator extends CloseableSparkRowItemIterator {
    override def hasNext: Boolean = false

    override def next(): SparkRowItem = {
      throw new java.util.NoSuchElementException("No items available for empty partition-key list.")
    }

    override def close(): Unit = {}
  }

  // Pass the full PK list to the SDK (which batches per physical partition internally).
  // On transient I/O failures the retry iterator tracks pages already emitted upstream
  // and skips them on replay; if a failure occurs mid-page (after items from that page have been
  // emitted) the task fails rather than risking row duplication.
  private val isClosed = new AtomicBoolean(false)
  private var iteratorOpt: Option[CloseableSparkRowItemIterator] = None

  private def getOrCreateIterator: CloseableSparkRowItemIterator = iteratorOpt match {
    case Some(existing) => existing
    case None =>
      val created =
        if (pkList.isEmpty) {
          EmptySparkRowItemIterator
        } else {
          new CloseableSparkRowItemIterator {
            private val delegate = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
              cosmosAsyncContainer,
              pkList,
              readConfig.customQuery.map(_.toSqlQuerySpec),
              readManyOptions,
              readConfig.maxItemCount,
              readConfig.prefetchBufferSize,
              operationContextAndListenerTuple,
              classOf[SparkRowItem]
            )

            override def hasNext: Boolean = delegate.hasNext

            override def next(): SparkRowItem = delegate.next()

            override def close(): Unit = delegate.close()
          }
        }

      iteratorOpt = Some(created)
      created
  }

  private val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(readSchema)

  private def shouldLogDetailedFeedDiagnostics(): Boolean = {
    diagnosticsConfig.mode.isDefined &&
      diagnosticsConfig.mode.get.equalsIgnoreCase(classOf[DetailedFeedDiagnosticsProvider].getName)
  }

  private def getPartitionKeyForFeedDiagnostics(pkValue: PartitionKey): Option[PartitionKey] = {
    if (shouldLogDetailedFeedDiagnostics()) {
      Some(pkValue)
    } else {
      None
    }
  }

  override def next(): Boolean = getOrCreateIterator.hasNext

  override def get(): InternalRow = {
    cosmosRowConverter.fromRowToInternalRow(getOrCreateIterator.next().row, rowSerializer)
  }

  def getCurrentRow(): Row = getOrCreateIterator.next().row

  override def close(): Unit = {
    if (isClosed.compareAndSet(false, true)) {
      iteratorOpt.foreach(_.close())
      iteratorOpt = None
      RowSerializerPool.returnSerializerToPool(readSchema, rowSerializer)
      clientCacheItem.close()
      if (throughputControlClientCacheItemOpt.isDefined) {
        throughputControlClientCacheItemOpt.get.close()
      }
    }
  }
}