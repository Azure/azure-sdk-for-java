// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.guava25.collect.{Iterators, PeekingIterator}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions
import com.azure.cosmos.spark.ChangeFeedPartitionReader.LsnPropertyName
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.LsnAttributeName
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import com.fasterxml.jackson.databind.node.ObjectNode
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
  private val client = CosmosClientCache(
    CosmosClientConfiguration(config, readConfig.forceEventualConsistency), Some(cosmosClientStateHandle))

  private val cosmosAsyncContainer = ThroughputControlHelper.getContainer(config, containerTargetConfig, client)
  cosmosAsyncContainer.openConnectionsAndInitCaches().block()

  private val changeFeedRequestOptions = {

    val startLsn =
      SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(this.partition.continuationState.get)
    log.logDebug(s"Request options for Range '${partition.feedRange.min}-${partition.feedRange.max}' LSN '$startLsn'")

    CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(this.partition.continuationState.get)
  }

  private val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(readSchema)

  private lazy val iterator: PeekingIterator[ObjectNode] =  Iterators.peekingIterator(
    cosmosAsyncContainer
      .queryChangeFeed(changeFeedRequestOptions, classOf[ObjectNode])
      .toIterable
      .iterator()
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
        val node = this.iterator.peek()
        assert(node.get(LsnPropertyName) != null, "Change feed responses must have _lsn property.")
        assert(node.get(LsnPropertyName).asText("") != "", "Change feed responses must have non empty _lsn.")
        val nextLsn = SparkBridgeImplementationInternal.toLsn(node.get(LsnPropertyName).asText())
        nextLsn <= endLsn
    }
  }

  override def get(): InternalRow = {
    val objectNode = this.iterator.next()
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
