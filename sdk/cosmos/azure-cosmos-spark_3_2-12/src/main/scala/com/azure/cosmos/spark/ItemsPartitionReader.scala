// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.spark.{OperationContextAndListenerTuple, OperationListener}
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, SparkBridgeImplementationInternal, SparkRowItem, Strings}
import com.azure.cosmos.models.{CosmosParameterizedQuery, CosmosQueryRequestOptions, DedicatedGatewayRequestOptions, ModelBridgeInternal, PartitionKeyDefinition}
import com.azure.cosmos.spark.BulkWriter.getThreadInfo
import com.azure.cosmos.spark.diagnostics.{DetailedFeedDiagnosticsProvider, DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

import java.time.Duration

// per spark task there will be one CosmosPartitionReader.
// This provides iterator to read from the assigned spark partition
// For now we are creating only one spark partition
private case class ItemsPartitionReader
(
  config: Map[String, String],
  feedRange: NormalizedRange,
  readSchema: StructType,
  cosmosQuery: CosmosParameterizedQuery,
  diagnosticsContext: DiagnosticsContext,
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig
)
  extends PartitionReader[InternalRow] {

  private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  private val queryOptions = ImplementationBridgeHelpers
    .CosmosQueryRequestOptionsHelper
    .getCosmosQueryRequestOptionsAccessor
    .disallowQueryPlanRetrieval(new CosmosQueryRequestOptions())

  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  ThroughputControlHelper.populateThroughputControlGroupName(queryOptions, readConfig.throughputControlConfig)

  private val operationContext = {
    val taskContext = TaskContext.get
    assert(taskContext != null)

    SparkTaskContext(diagnosticsContext.correlationActivityId,
      taskContext.stageId(),
      taskContext.partitionId(),
      taskContext.taskAttemptId(),
      feedRange.toString + " " + cosmosQuery.toString)
  }

  private var operationContextAndListenerTuple: Option[OperationContextAndListenerTuple] = {
    if (diagnosticsConfig.mode.isDefined) {
      val listener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      val ctxAndListener = new OperationContextAndListenerTuple(operationContext, listener)

      ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper
        .getCosmosQueryRequestOptionsAccessor
        .setOperationContext(queryOptions, ctxAndListener)

      Some(ctxAndListener)
    } else {
      None
    }
  }

  log.logInfo(s"Instantiated ${this.getClass.getSimpleName}, Context: ${operationContext.toString} ${getThreadInfo}")

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  log.logInfo(s"Reading from feed range $feedRange of " +
    s"container ${containerTargetConfig.database}.${containerTargetConfig.container} - " +
    s"correlationActivityId ${diagnosticsContext.correlationActivityId}, " +
    s"query: ${cosmosQuery.toString}, Context: ${operationContext.toString} ${getThreadInfo}")

  private val clientCacheItem = CosmosClientCache(
    CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
    Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
    s"ItemsPartitionReader($feedRange, ${containerTargetConfig.database}.${containerTargetConfig.container})"
  )

  private val throughputControlClientCacheItemOpt =
    ThroughputControlHelper.getThroughputControlClientCacheItem(
      config,
      clientCacheItem.context,
      Some(cosmosClientStateHandles))

  private val cosmosAsyncContainer =
    ThroughputControlHelper.getContainer(
      config,
      containerTargetConfig,
      clientCacheItem,
      throughputControlClientCacheItemOpt)
  SparkUtils.safeOpenConnectionInitCaches(cosmosAsyncContainer, log)

  private val partitionKeyDefinition: Option[PartitionKeyDefinition] =
    if (diagnosticsConfig.mode.isDefined &&
      diagnosticsConfig.mode.get.equalsIgnoreCase(classOf[DetailedFeedDiagnosticsProvider].getName)) {

      Option.apply(cosmosAsyncContainer.read().block().getProperties.getPartitionKeyDefinition)
    } else {
      None
    }

  private val cosmosSerializationConfig = CosmosSerializationConfig.parseSerializationConfig(config)
  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)

  private def initializeOperationContext(): SparkTaskContext = {
    val taskContext = TaskContext.get

    if (taskContext != null) {
      val taskDiagnosticsContext = SparkTaskContext(diagnosticsContext.correlationActivityId,
        taskContext.stageId(),
        taskContext.partitionId(),
        taskContext.taskAttemptId(),
        feedRange.toString + " " + cosmosQuery.toString)

      val listener: OperationListener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      val operationContextAndListenerTuple = new OperationContextAndListenerTuple(taskDiagnosticsContext, listener)
      ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper
        .getCosmosQueryRequestOptionsAccessor
        .setOperationContext(queryOptions, operationContextAndListenerTuple)

      taskDiagnosticsContext
    } else{
      SparkTaskContext(diagnosticsContext.correlationActivityId,
        -1,
        -1,
        -1,
        "")
    }
  }

  queryOptions.setFeedRange(SparkBridgeImplementationInternal.toFeedRange(feedRange))

  ImplementationBridgeHelpers
    .CosmosQueryRequestOptionsHelper
    .getCosmosQueryRequestOptionsAccessor
    .setItemFactoryMethod(
      queryOptions,
      jsonNode => {
        val objectNode = cosmosRowConverter.ensureObjectNode(jsonNode)
        val row = cosmosRowConverter.fromObjectNodeToRow(readSchema,
          objectNode,
          readConfig.schemaConversionMode)

        val pkValue = partitionKeyDefinition match {
          case Some(pkDef) => Some(PartitionKeyHelper.getPartitionKeyPath(objectNode, pkDef))
          case None => None
        }

        SparkRowItem(row, pkValue)
      })

  private lazy val iterator = new TransientIOErrorsRetryingIterator(
    continuationToken => {

      if (!Strings.isNullOrWhiteSpace(continuationToken)) {
        ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(
          queryOptions, continuationToken, readConfig.maxItemCount)
      } else {
        // scalastyle:off null
        ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(
          queryOptions, null, readConfig.maxItemCount)
        // scalastyle:on null
      }

      queryOptions.setMaxBufferedItemCount(
        math.min(
          readConfig.maxItemCount * readConfig.prefetchBufferSize.toLong, // converting to long to avoid overflow when
                                                                          // multiplying to ints
          java.lang.Integer.MAX_VALUE
        ).toInt
      )

      queryOptions.setDedicatedGatewayRequestOptions(readConfig.dedicatedGatewayRequestOptions)

      ImplementationBridgeHelpers
        .CosmosQueryRequestOptionsHelper
        .getCosmosQueryRequestOptionsAccessor
        .setCorrelationActivityId(
          queryOptions,
          diagnosticsContext.correlationActivityId)

      cosmosAsyncContainer.queryItems(cosmosQuery.toSqlQuerySpec, queryOptions, classOf[SparkRowItem])
    },
    readConfig.maxItemCount,
    readConfig.prefetchBufferSize,
    operationContextAndListenerTuple
  )

  private val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(readSchema)

  override def next(): Boolean = iterator.hasNext

  override def get(): InternalRow = {
    cosmosRowConverter.fromRowToInternalRow(iterator.next().row, rowSerializer)
  }

  override def close(): Unit = {
    this.iterator.close()
    RowSerializerPool.returnSerializerToPool(readSchema, rowSerializer)
    clientCacheItem.close()
    if (throughputControlClientCacheItemOpt.isDefined) {
      throughputControlClientCacheItemOpt.get.close()
    }
  }
}
