// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.guava25.collect.{Iterators, PeekingIterator}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions
import com.azure.cosmos.spark.ChangeFeedPartitionReader.LsnPropertyName
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

private object ChangeFeedPartitionReader {
  val LsnPropertyName: String = "_lsn"
}

// per spark task there will be one CosmosPartitionReader.
// This provides iterator to read from the assigned spark partition
// For now we are creating only one spark partition per physical partition
private case class ChangeFeedPartitionReader
(
  partition: CosmosInputPartition,
  config: Map[String, String],
  readSchema: StructType,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
) extends PartitionReader[InternalRow] with CosmosLoggingTrait {

  requireNotNull(partition, "partition")
  assert(partition.continuationState.isDefined, "Argument 'partition.continuationState' must be defined here.")
  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val client = CosmosClientCache(
    CosmosClientConfiguration(config, readConfig.forceEventualConsistency), Some(cosmosClientStateHandle))

  private val cosmosAsyncContainer = client
    .getDatabase(containerTargetConfig.database)
    .getContainer(containerTargetConfig.container)

  private val changeFeedRequestOptions =
    CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(this.partition.continuationState.get)

  private lazy val iterator: PeekingIterator[ObjectNode] =  Iterators.peekingIterator(cosmosAsyncContainer
    .queryChangeFeed(changeFeedRequestOptions, classOf[ObjectNode])
    .toIterable.iterator())

  override def next(): Boolean = {
    this.iterator.hasNext && this.validateNextLsn
  }

  private[this] def validateNextLsn: Boolean = {
    if (this.partition.endLsn.isEmpty) {
      true
    } else {
      val node = this.iterator.peek()
      assert(node.get(LsnPropertyName) != null, "Change feed responses must have _lsn property.")
      assert(node.get(LsnPropertyName).asText("") != "", "Change feed responses must have non empty _lsn.")
      val nextLsn = SparkBridgeImplementationInternal.toLsn(node.get(LsnPropertyName).asText())
      nextLsn < this.partition.endLsn
    }
  }

  override def get(): InternalRow = {
    val objectNode = this.iterator.next()
    // TODO fabianm - for custom schema we need a ChangeFeedRowConverter (knowing how to create new
    //  ObjectNode from raw json and then deserialize that one into custom schema)
    CosmosRowConverter.fromObjectNodeToInternalRow(readSchema, objectNode)
  }

  override def close(): Unit = {
  }
}
