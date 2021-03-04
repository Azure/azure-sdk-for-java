// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.{CosmosParameterizedQuery, FeedRange}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory, Scan}
import org.apache.spark.sql.types.StructType

private case class ItemsScan(session: SparkSession,
                             schema: StructType,
                             config: Map[String, String],
                             cosmosQuery: CosmosParameterizedQuery,
                             cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot])
  extends Scan
    with Batch
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  /**
    * Returns the actual schema of this data source scan, which may be different from the physical
    * schema of the underlying storage, as column pruning or other optimizations may happen.
    */
  override def readSchema(): StructType = {
    schema
  }

  override def planInputPartitions(): Array[InputPartition] = {
    val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
    val clientConfiguration = CosmosClientConfiguration.apply(config, readConfig.forceEventualConsistency)
    val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
    val partitioningConfig = CosmosPartitioningConfig.parseCosmosPartitioningConfig(config)
    val defaultMaxPartitionSizeInMB = (session.sessionState.conf.filesMaxPartitionBytes / (1024 * 1024)).toInt

    val defaultMinPartitionCount = 1 + (2 * session.sparkContext.defaultParallelism)

    CosmosPartitionPlanner.createInputPartitions(
      clientConfiguration,
      Some(cosmosClientStateHandle),
      containerConfig,
      partitioningConfig,
      None, // In batch mode always start a new query - without previous continuation state
      defaultMinPartitionCount,
      defaultMaxPartitionSizeInMB
    )
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    ItemsScanPartitionReaderFactory(config, schema, cosmosQuery, cosmosClientStateHandle)
  }

  override def toBatch: Batch = {
    // TODO: moderakh should we refactor Bath to a new class? of should it be here?
    this
  }
}
