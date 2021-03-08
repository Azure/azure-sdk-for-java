// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

import java.time.Duration

private class ChangeFeedBatch
(
  session: SparkSession,
  schema: StructType,
  config: Map[String, String],
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
) extends Batch
  with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  override def planInputPartitions(): Array[InputPartition] = {

    val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
    val clientConfiguration = CosmosClientConfiguration.apply(config, readConfig.forceEventualConsistency)
    val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
    val partitioningConfig = CosmosPartitioningConfig.parseCosmosPartitioningConfig(config)
    val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(config)

    val client =
      CosmosClientCache.apply(clientConfiguration, Some(cosmosClientStateHandle))
    val container = client
      .getDatabase(containerConfig.database)
      .getContainer(containerConfig.container)

    // This maps the StartFrom settings to concrete LSNs
    val initialOffsetJson = CosmosPartitionPlanner.createInitialOffset(container, changeFeedConfig, None)

    // Calculates the Input partitions based on start Lsn and latest Lsn
    val latestOffset = CosmosPartitionPlanner.getLatestOffset(
      ChangeFeedOffset(initialOffsetJson, None),
      changeFeedConfig.toReadLimit,
      Duration.ZERO,
      clientConfiguration,
      this.cosmosClientStateHandle,
      containerConfig,
      partitioningConfig,
      this.session
    )

    // Latest offset above has the EndLsn specified based on the point-in-time latest offset
    // For batch mode instead we need to reset it so that the change feed will get fully drained
    latestOffset
      .inputPartitions
      .get
      .map(p => p.clearEndLsn())
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    ChangeFeedScanPartitionReaderFactory(config, schema, cosmosClientStateHandle)
  }
}
