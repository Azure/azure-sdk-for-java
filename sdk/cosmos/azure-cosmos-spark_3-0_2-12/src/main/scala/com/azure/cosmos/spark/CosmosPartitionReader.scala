// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeInternal}
import com.azure.cosmos.models.{CosmosParametrizedQuery, CosmosQueryRequestOptions}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

// per spark task there will be one CosmosPartitionReader.
// This provides iterator to read from the assigned spark partition
// For now we are creating only one spark partition
case class CosmosPartitionReader(config: Map[String, String],
                                 readSchema: StructType,
                                 cosmosQuery: CosmosParametrizedQuery,
                                 cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot])
// TODO: moderakh query need to change to SqlSpecQuery
// requires making a serializable wrapper on top of SqlQuerySpec

  extends PartitionReader[InternalRow] with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)

  val client = CosmosClientCache(CosmosClientConfiguration(config), Some(cosmosClientStateHandle))

  val cosmosAsyncContainer = client
    .getDatabase(containerTargetConfig.database)
    .getContainer(containerTargetConfig.container)

  lazy val iterator = cosmosAsyncContainer.queryItems(
    cosmosQuery.toSqlQuerySpec(),
    new CosmosQueryRequestOptions(),
    classOf[ObjectNode]).toIterable.iterator()

  override def next(): Boolean = iterator.hasNext

  override def get(): InternalRow = {
    val objectNode = iterator.next()
    CosmosRowConverter.fromObjectNodeToInternalRow(readSchema, objectNode)
  }

  override def close(): Unit = {
    // TODO moderakh manage the lifetime of the cosmos clients
  }
}
