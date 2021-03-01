// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.azure.cosmos.spark.CosmosPredicates.requireNotNullOrEmpty
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

// per spark task there will be one CosmosPartitionReader.
// This provides iterator to read from the assigned spark partition
// For now we are creating only one spark partition per physical partition
private case class ChangeFeedPartitionReader
(
  feedRange: String,
  config: Map[String, String],
  readSchema: StructType,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
) extends PartitionReader[InternalRow] with CosmosLoggingTrait {

  requireNotNullOrEmpty(feedRange, "feedRange")
  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(config)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val client = CosmosClientCache(CosmosClientConfiguration(config, readConfig.forceEventualConsistency), Some(cosmosClientStateHandle))

  private val cosmosAsyncContainer = client
    .getDatabase(containerTargetConfig.database)
    .getContainer(containerTargetConfig.container)

  // TODO fabianm this needs to be initialized based on InputPartition and startFrom configuration
  private val changeFeedRequestOptions = this.changeFeedConfig.changeFeedMode match {
    case ChangeFeedModes.Incremental =>
      CosmosChangeFeedRequestOptions.createForProcessingFromBeginning (FeedRange.fromString(feedRange))
    case ChangeFeedModes.FullFidelity =>
      CosmosChangeFeedRequestOptions
        .createForProcessingFromNow(FeedRange.fromString(feedRange))
        .fullFidelity()
  }

  private lazy val iterator = cosmosAsyncContainer
    .queryChangeFeed(changeFeedRequestOptions, classOf[ObjectNode])
    .toIterable.iterator()

  override def next(): Boolean = this.iterator.hasNext

  override def get(): InternalRow = {
    val objectNode = this.iterator.next()
    // TODO fabianm - for custom schema we need a ChangeFeedRowConverter (knowing how to create new
    //  ObjectNode from raw json and then deserialize that one into custom schema)
    CosmosRowConverter.fromObjectNodeToInternalRow(readSchema, objectNode)
  }

  override def close(): Unit = {
    // TODO moderakh manage the lifetime of the cosmos clients
  }
}
