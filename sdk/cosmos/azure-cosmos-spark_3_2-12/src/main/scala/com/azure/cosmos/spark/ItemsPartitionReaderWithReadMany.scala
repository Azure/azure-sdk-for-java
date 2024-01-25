// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, SparkRowItem}
import com.azure.cosmos.models.{CosmosQueryRequestOptions, ModelBridgeInternal, PartitionKeyDefinition}
import com.azure.cosmos.spark.BulkWriter.getThreadInfo
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.IdAttributeName
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

private[spark] case class ItemsPartitionReaderWithReadMany
(
  config: Map[String, String],
  feedRange: NormalizedRange,
  readSchema: StructType,
  diagnosticsContext: DiagnosticsContext,
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String,
  taskContext: TaskContext,
  readManyFilters: List[String]
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

      ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper
        .getCosmosQueryRequestOptionsAccessor
        .setOperationContext(queryOptions, ctxAndListener)

      Some(ctxAndListener)
    } else {
      None
    }
  }

  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}, Context: ${operationContext.toString} ${getThreadInfo}")

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)

  log.logInfo(s"Using ReadMany from feed range $feedRange of " +
    s"container ${containerTargetConfig.database}.${containerTargetConfig.container} - " +
    s"correlationActivityId ${diagnosticsContext.correlationActivityId}, " +
    s"readManyFilter: [ feedRange: $feedRange. total size ${readManyFilters.size}], " +
    s"Context: ${operationContext.toString} ${getThreadInfo}")

  log.logTrace(s"container ${containerTargetConfig.database}.${containerTargetConfig.container} - " +
    s"readManyFilterDetails: [feedRange: $feedRange. Details: ${readManyFilters.reduce((left, right) => s"$left;$right")}]" +
    s"Context: ${operationContext.toString} ${getThreadInfo}"
  )

  private val clientCacheItem = CosmosClientCache(
    CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
    Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
    s"ItemsPartitionReader($feedRange, ${containerTargetConfig.database}.${containerTargetConfig.container})"
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
  SparkUtils.safeOpenConnectionInitCaches(cosmosAsyncContainer, log)

  private val partitionKeyDefinition: PartitionKeyDefinition = {
    TransientErrorsRetryPolicy.executeWithRetry(() => {
      cosmosAsyncContainer.read().block().getProperties.getPartitionKeyDefinition
    })
  }

  private val cosmosSerializationConfig = CosmosSerializationConfig.parseSerializationConfig(config)
  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)
  private val effectiveReadManyFilteringConfig =
    CosmosReadManyFilteringConfig
      .getEffectiveReadManyFilteringConfig(
        readConfig.readManyFilteringConfig,
        partitionKeyDefinition)

  ImplementationBridgeHelpers
    .CosmosQueryRequestOptionsHelper
    .getCosmosQueryRequestOptionsAccessor
    .setItemFactoryMethod(
      queryOptions,
      jsonNode => {
        val objectNode = cosmosRowConverter.ensureObjectNode(jsonNode)
        val idValue = objectNode.get(IdAttributeName).asText()
        val partitionKey = PartitionKeyHelper.getPartitionKeyPath(objectNode, partitionKeyDefinition)

        this.effectiveReadManyFilteringConfig.readManyFilterProperty match {
          case CosmosConstants.Properties.Id => {
            // id is also the partition key, there is no need to dynamically populate it
            val row = cosmosRowConverter.fromObjectNodeToRow(readSchema,
              objectNode,
              readConfig.schemaConversionMode)

            SparkRowItem(row, Some(partitionKey))
          }
          case _ => {
            // id is not the partitionKey, dynamically computed the readMany filtering property
            val computedColumnsMap = Map(
              readConfig.readManyFilteringConfig.readManyFilterProperty ->
                ((_: ObjectNode) => {
                  CosmosItemIdentityHelper.getCosmosItemIdentityValueString(
                    idValue,
                    ModelBridgeInternal.getPartitionKeyInternal(partitionKey).toObjectArray.toList)
                })
            )

            val row = cosmosRowConverter.fromObjectNodeToRowWithComputedColumns(readSchema,
              objectNode,
              readConfig.schemaConversionMode,
              computedColumnsMap)

            SparkRowItem(row, Some(partitionKey))
          }
        }
      })

  private lazy val iterator = new TransientIOErrorsRetryingReadManyIterator[SparkRowItem](
    cosmosAsyncContainer,
    readManyFilters.map(CosmosItemIdentityHelper.tryParseCosmosItemIdentity(_).get),
    queryOptions,
    readConfig.maxItemCount,
    operationContextAndListenerTuple,
    classOf[SparkRowItem])

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
