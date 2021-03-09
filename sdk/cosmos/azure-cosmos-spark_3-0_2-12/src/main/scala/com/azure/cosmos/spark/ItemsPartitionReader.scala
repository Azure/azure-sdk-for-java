// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.models.{CosmosParameterizedQuery, CosmosQueryRequestOptions}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
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
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
)
// TODO: moderakh query need to change to SqlSpecQuery
// requires making a serializable wrapper on top of SqlQuerySpec
  extends PartitionReader[InternalRow] with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  logDebug(s"Reading from $feedRange")

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val client = CosmosClientCache(CosmosClientConfiguration(config, readConfig.forceEventualConsistency), Some(cosmosClientStateHandle))

  private val cosmosAsyncContainer = client
    .getDatabase(containerTargetConfig.database)
    .getContainer(containerTargetConfig.container)

  private lazy val iterator = cosmosAsyncContainer.queryItems(
    cosmosQuery.toSqlQuerySpec,
    new CosmosQueryRequestOptions(),
    classOf[ObjectNode],
    SparkBridgeImplementationInternal.toFeedRange(feedRange)).toIterable.iterator()

  override def next(): Boolean = iterator.hasNext

  override def get(): InternalRow = {
    val objectNode = iterator.next()
    CosmosRowConverter.fromObjectNodeToInternalRow(readSchema, objectNode)
  }

  override def close(): Unit = {
  }
}
