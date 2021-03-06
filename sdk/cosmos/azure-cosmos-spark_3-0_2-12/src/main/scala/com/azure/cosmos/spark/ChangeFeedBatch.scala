// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

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

    val defaultMaxPartitionSizeInMB = (session.sessionState.conf.filesMaxPartitionBytes / (1024 * 1024)).toInt
    val defaultMinPartitionCount = 1 + (2 * session.sparkContext.defaultParallelism)

    CosmosPartitionPlanner.createInputPartitions(
      clientConfiguration,
      Some(cosmosClientStateHandle),
      containerConfig,
      partitioningConfig,
      defaultMinPartitionCount,
      defaultMaxPartitionSizeInMB,
      changeFeedConfig.toReadLimit
    ).map(_.asInstanceOf[InputPartition])
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    ChangeFeedScanPartitionReaderFactory(config, schema, cosmosClientStateHandle)
  }
}
