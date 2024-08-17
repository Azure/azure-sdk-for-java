// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosItemSerializerNoExceptionWrapping, SparkBridgeInternal}
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, ObjectNodeMap, SparkRowItem, Utils}
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

import java.util

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

  log.logInfo(s"Using ReadMany from feed range $feedRange of " +
    s"container ${containerTargetConfig.database}.${containerTargetConfig.container} - " +
    s"correlationActivityId ${diagnosticsContext.correlationActivityId}, " +
    s"readManyFilter: [feedRange: $feedRange], " +
    s"Context: ${operationContext.toString} $getThreadInfo")

  log.logTrace(s"container ${containerTargetConfig.database}.${containerTargetConfig.container} - " +
    s"readManyFilterDetails: [feedRange: $feedRange." +
    s"Context: ${operationContext.toString} $getThreadInfo"
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

  private val partitionKeyDefinition: PartitionKeyDefinition = {
    TransientErrorsRetryPolicy.executeWithRetry(() => {
      SparkBridgeInternal
        .getContainerPropertiesFromCollectionCache(cosmosAsyncContainer).getPartitionKeyDefinition
    })
  }

  private val cosmosSerializationConfig = CosmosSerializationConfig.parseSerializationConfig(config)
  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)
  private val effectiveReadManyFilteringConfig =
    CosmosReadManyFilteringConfig
      .getEffectiveReadManyFilteringConfig(
        readConfig.readManyFilteringConfig,
        partitionKeyDefinition)

  readManyOptionsImpl
    .setCustomItemSerializer(
      new CosmosItemSerializerNoExceptionWrapping {
        /**
         * Used to serialize a POJO into a json tree
         *
         * @param item the POJO to be serialized
         * @return the json tree that will be used as payload in Cosmos DB items
         * @param <  T> The type of the POJO
         */
        override def serialize[T](item: T): util.Map[String, AnyRef] = ???

        /**
         * Used to deserialize the json tree stored in the Cosmos DB item as a POJO
         *
         * @param jsonNodeMap the json tree from the Cosmos DB item
         * @param classType   The type of the POJO
         * @return The deserialized POJO
         * @param < T> The type of the POJO
         */
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

          val idValue = objectNode.get(IdAttributeName).asText()
          val partitionKey = PartitionKeyHelper.getPartitionKeyPath(objectNode, partitionKeyDefinition)

          effectiveReadManyFilteringConfig.readManyFilterProperty match {
            case CosmosConstants.Properties.Id => {
              // id is also the partition key, there is no need to dynamically populate it
              val row = cosmosRowConverter.fromObjectNodeToRow(readSchema,
                objectNode,
                readConfig.schemaConversionMode)

              SparkRowItem(row, getPartitionKeyForFeedDiagnostics(partitionKey)).asInstanceOf[T]
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

              SparkRowItem(row, getPartitionKeyForFeedDiagnostics(partitionKey)).asInstanceOf[T]
            }
          }
        }
      }
    )

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
