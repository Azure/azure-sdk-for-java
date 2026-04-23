// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, CosmosEndToEndOperationLatencyPolicyConfigBuilder, CosmosItemSerializer, CosmosItemSerializerNoExceptionWrapping, SparkBridgeInternal}
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, ObjectNodeMap, SparkRowItem, Utils}
import com.azure.cosmos.models.{CosmosReadManyByPartitionKeysRequestOptions, ModelBridgeInternal, PartitionKey, PartitionKeyDefinition, SqlQuerySpec}
import com.azure.cosmos.spark.BulkWriter.getThreadInfo
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.IdAttributeName
import com.azure.cosmos.spark.diagnostics.{DetailedFeedDiagnosticsProvider, DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import com.azure.cosmos.util.CosmosPagedFlux
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

  private val readManyOptions = new CosmosReadManyByPartitionKeysRequestOptions()
  private val readManyOptionsImpl = ImplementationBridgeHelpers
    .CosmosReadManyByPartitionKeysRequestOptionsHelper
    .getCosmosReadManyByPartitionKeysRequestOptionsAccessor
    .getImpl(readManyOptions)

  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  ThroughputControlHelper.populateThroughputControlGroupName(readManyOptionsImpl, readConfig.throughputControlConfig)
  readManyOptions.setMaxConcurrentBatchPrefetch(readConfig.readManyByPkMaxConcurrentBatchPrefetch)

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
        // The base class (CosmosItemSerializerNoExceptionWrapping) sets canSerialize = false,
        // which prevents the SDK from calling serialize() — so no override is needed here.
        // Only deserialization (the hot path) is customized below.

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

  // Collect all PK values upfront - the SDK now owns normalization and deduplication,
  // so Spark preserves the caller's input as-is and relies on the SDK's set-based semantics.
  // Callers should still dedupe their DataFrame input when practical to avoid extra work.
  //
  // NOTE on memory footprint: every PartitionKey from this iterator is materialized into a
  // single ArrayList here, and the SDK in turn keeps the normalized set, the EPK->PK map,
  // and the per-batch BatchDescriptor lists alive for the lifetime of the reader. For a
  // single Spark partition with O(N) input rows this is O(N) memory on the executor; if
  // upstream Spark partitioning sends millions of distinct PKs to one task this can become
  // a noticeable allocation. Repartition upstream when N is very large.
  private val PK_COUNT_LARGE_INPUT_WARN_THRESHOLD = 200000
  private lazy val pkList = {
    val values = new java.util.ArrayList[PartitionKey]()
    readManyPartitionKeys.foreach(values.add)
    if (values.size() > PK_COUNT_LARGE_INPUT_WARN_THRESHOLD) {
      log.logWarning(
        s"ItemsPartitionReaderWithReadManyByPartitionKey received ${values.size()} partition " +
          s"keys for a single Spark partition (feedRange=$feedRange). Large PK lists materialize " +
          s"the full set in memory plus the SDK's normalized batch metadata; consider increasing " +
          s"upstream Spark parallelism so each task processes <= " +
          s"$PK_COUNT_LARGE_INPUT_WARN_THRESHOLD distinct partition keys.")
    }
    values
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
  // On transient I/O failures the retry iterator re-creates the underlying flux from the
  // continuation token of the last fully-committed page, matching the pattern used by
  // TransientIOErrorsRetryingIterator for queries and change feed.
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
            private val customQueryOpt = readConfig.customQuery.map(_.toSqlQuerySpec)

            // Factory that creates a CosmosPagedFlux from an optional continuation token.
            // On the first call continuationToken is null (start from scratch); on retry
            // it is the continuation token from the last fully-drained page.
            // A fresh CosmosReadManyByPartitionKeysRequestOptions instance is created per
            // call to avoid mutating the shared readManyOptions object, which would be
            // fragile if the SDK ever stopped cloning options internally.
            private val fluxFactory: String => CosmosPagedFlux[SparkRowItem] = { (continuationToken: String) =>
              val perCallOptions = new CosmosReadManyByPartitionKeysRequestOptions()
              perCallOptions.setMaxConcurrentBatchPrefetch(readConfig.readManyByPkMaxConcurrentBatchPrefetch)
              perCallOptions.setContinuationToken(continuationToken)
              val perCallOptionsImpl = ImplementationBridgeHelpers
                .CosmosReadManyByPartitionKeysRequestOptionsHelper
                .getCosmosReadManyByPartitionKeysRequestOptionsAccessor
                .getImpl(perCallOptions)
              ThroughputControlHelper.populateThroughputControlGroupName(perCallOptionsImpl, readConfig.throughputControlConfig)
              perCallOptionsImpl.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndTimeoutPolicy)
              if (operationContextAndListenerTuple.isDefined) {
                perCallOptionsImpl.setOperationContextAndListenerTuple(operationContextAndListenerTuple.get)
              }
              perCallOptionsImpl
                .setCustomItemSerializer(readManyOptionsImpl.getCustomItemSerializer)
              customQueryOpt match {
                case Some(query) =>
                  cosmosAsyncContainer.readManyByPartitionKeys(pkList, query, perCallOptions, classOf[SparkRowItem])
                case None =>
                  cosmosAsyncContainer.readManyByPartitionKeys(pkList, perCallOptions, classOf[SparkRowItem])
              }
            }

            private val delegate = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
              fluxFactory,
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

  private var currentRow: Option[Row] = None

  override def next(): Boolean = {
    val hasMore = getOrCreateIterator.hasNext
    if (hasMore) {
      currentRow = Some(getOrCreateIterator.next().row)
    } else {
      currentRow = None
    }
    hasMore
  }

  override def get(): InternalRow = {
    cosmosRowConverter.fromRowToInternalRow(
      currentRow.getOrElse(throw new NoSuchElementException("No current row - next() must be called first")),
      rowSerializer)
  }

  def getCurrentRow(): Row = {
    currentRow.getOrElse(throw new NoSuchElementException("No current row - next() must be called first"))
  }

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
