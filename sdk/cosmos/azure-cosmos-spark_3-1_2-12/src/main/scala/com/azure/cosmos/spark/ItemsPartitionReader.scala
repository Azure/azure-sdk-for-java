// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.spark.{OperationContextAndListenerTuple, OperationListener}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, ImplementationBridgeHelpers, SparkBridgeImplementationInternal}
import com.azure.cosmos.models.{CosmosParameterizedQuery, CosmosQueryRequestOptions}
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import com.azure.cosmos.util.CosmosPagedIterable
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

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
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  diagnosticsConfig: DiagnosticsConfig
)
  extends PartitionReader[InternalRow] {

  private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  log.logInfo(s"Reading from feed range $feedRange of " +
    s"container ${containerTargetConfig.database}.${containerTargetConfig.container}")
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val client = CosmosClientCache(
    CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
    Some(cosmosClientStateHandle))

  private val cosmosAsyncContainer = ThroughputControlHelper.getContainer(config, containerTargetConfig, client)
  cosmosAsyncContainer.openConnectionsAndInitCaches().block()

  private val queryOptions = new CosmosQueryRequestOptions()

  initializeDiagnosticsIfConfigured

  private def initializeDiagnosticsIfConfigured(): Unit = {
    if (diagnosticsConfig.mode.isDefined) {
      val taskContext = TaskContext.get
      assert(taskContext != null)

      val taskDiagnosticsContext = SparkTaskContext(diagnosticsContext.correlationActivityId,
        taskContext.stageId(),
        taskContext.partitionId(),
        taskContext.taskAttemptId(),
        feedRange.toString + " " + cosmosQuery.toSqlQuerySpec.getQueryText)

      val listener: OperationListener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      val operationContextAndListenerTuple = new OperationContextAndListenerTuple(taskDiagnosticsContext, listener)
      ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper
        .getCosmosQueryRequestOptionsAccessor.setOperationContext(queryOptions, operationContextAndListenerTuple)
    }
  }

  queryOptions.setFeedRange(SparkBridgeImplementationInternal.toFeedRange(feedRange))

  private lazy val iterator = new CosmosPagedIterable[ObjectNode](
    cosmosAsyncContainer.queryItems(cosmosQuery.toSqlQuerySpec, queryOptions, classOf[ObjectNode]),
    readConfig.maxItemCount
  ).iterator()

  private val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(readSchema)

  override def next(): Boolean = iterator.hasNext

  override def get(): InternalRow = {
    val objectNode = iterator.next()
    CosmosRowConverter.fromObjectNodeToInternalRow(
      readSchema,
      rowSerializer,
      objectNode,
      readConfig.schemaConversionMode)
  }

  override def close(): Unit = {
    RowSerializerPool.returnSerializerToPool(readSchema, rowSerializer)
  }
}
