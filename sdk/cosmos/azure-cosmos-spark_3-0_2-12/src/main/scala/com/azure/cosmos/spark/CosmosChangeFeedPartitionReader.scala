// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeInternal}
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

// per spark task there will be one CosmosPartitionReader.
// This provides iterator to read from the assigned spark partition
// For now we are creating only one spark partition per physical partition
private case class CosmosChangeFeedPartitionReader
(
  config: Map[String, String],
  readSchema: StructType,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
) extends PartitionReader[InternalRow] with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(config)
  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(config)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val client = CosmosClientCache(CosmosClientConfiguration(config, readConfig.forceEventualConsistency), Some(cosmosClientStateHandle))

  private val cosmosAsyncContainer = client
    .getDatabase(containerTargetConfig.database)
    .getContainer(containerTargetConfig.container)

  // TODO fabianm this needs to be initialized based on InputPartition and startFrom configuration
  private val changeFeedRequestOptions = this.changeFeedConfig.changeFeedMode match {
    case ChangeFeedModes.incremental =>
      CosmosChangeFeedRequestOptions.createForProcessingFromBeginning (FeedRange.forFullRange)
    case ChangeFeedModes.fullFidelity =>
      CosmosChangeFeedRequestOptions
        .createForProcessingFromNow(FeedRange.forFullRange)
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
