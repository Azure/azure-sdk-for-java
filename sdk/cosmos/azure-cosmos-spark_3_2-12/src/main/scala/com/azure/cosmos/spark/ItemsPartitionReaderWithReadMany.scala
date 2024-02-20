// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, SparkRowItem}
import com.azure.cosmos.models.{CosmosItemIdentity, CosmosReadManyRequestOptions, ModelBridgeInternal, PartitionKey, PartitionKeyDefinition}
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
  readManyFilters: Iterator[CosmosItemIdentity]
)
  extends PartitionReader[InternalRow] {

  private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  private val readManyOptions = new CosmosReadManyRequestOptions()

  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  ThroughputControlHelper.populateThroughputControlGroupName(readManyOptions, readConfig.throughputControlConfig)

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

      ImplementationBridgeHelpers.CosmosQueryRequestOptionsBaseHelper
        .getCosmosQueryRequestOptionsBaseAccessor
        .setOperationContext(readManyOptions, ctxAndListener)

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
    s"readManyFilter: [feedRange: $feedRange], " +
    s"Context: ${operationContext.toString} ${getThreadInfo}")

  log.logTrace(s"container ${containerTargetConfig.database}.${containerTargetConfig.container} - " +
    s"readManyFilterDetails: [feedRange: $feedRange." +
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
    .CosmosQueryRequestOptionsBaseHelper
    .getCosmosQueryRequestOptionsBaseAccessor
    .setItemFactoryMethod(
      readManyOptions,
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

            SparkRowItem(row, getPartitionKeyForFeedDiagnostics(partitionKey))
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

            SparkRowItem(row, getPartitionKeyForFeedDiagnostics(partitionKey))
          }
        }
      })

  private lazy val iterator = new TransientIOErrorsRetryingReadManyIterator[SparkRowItem](
    cosmosAsyncContainer,
    readManyFilters,
    readManyOptions,
    readConfig.maxItemCount,
    operationContextAndListenerTuple,
    classOf[SparkRowItem])

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

  override def next(): Boolean = iterator.hasNext

  override def get(): InternalRow = {
    cosmosRowConverter.fromRowToInternalRow(iterator.next().row, rowSerializer)
  }

  def getCurrentRow(): Row = iterator.next().row

  override def close(): Unit = {
    this.iterator.close()
    RowSerializerPool.returnSerializerToPool(readSchema, rowSerializer)
    clientCacheItem.close()
    if (throughputControlClientCacheItemOpt.isDefined) {
      throughputControlClientCacheItemOpt.get.close()
    }
  }
}
