// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

import java.time.Duration
import java.util.UUID

private class ChangeFeedBatch
(
  session: SparkSession,
  schema: StructType,
  config: Map[String, String],
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  diagnosticsConfig: DiagnosticsConfig
) extends Batch {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  private val correlationActivityId = UUID.randomUUID()
  private val batchId = correlationActivityId.toString
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")
  private val defaultParallelism = session.sparkContext.defaultParallelism

  override def planInputPartitions(): Array[InputPartition] = {

    log.logInfo(s"--> planInputPartitions $batchId")
    val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
    val clientConfiguration = CosmosClientConfiguration.apply(config, readConfig.forceEventualConsistency)
    val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
    val partitioningConfig = CosmosPartitioningConfig.parseCosmosPartitioningConfig(config)
    val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(config)

    Loan(
      CosmosClientCache.apply(
        clientConfiguration,
        Some(cosmosClientStateHandle),
        s"ChangeFeedBatch.planInputPartitions(batchId $batchId)"
      )).to(cacheItem => {
      val container = ThroughputControlHelper.getContainer(config, containerConfig, cacheItem.client)

      // This maps the StartFrom settings to concrete LSNs
      val initialOffsetJson = CosmosPartitionPlanner.createInitialOffset(
        container, changeFeedConfig, partitioningConfig, None)

      // Calculates the Input partitions based on start Lsn and latest Lsn
      val latestOffset = CosmosPartitionPlanner.getLatestOffset(
        config,
        ChangeFeedOffset(initialOffsetJson, None),
        changeFeedConfig.toReadLimit,
        // ok to use from cache because endLsn is ignored in batch mode
        Duration.ofMillis(PartitionMetadataCache.refreshIntervalInMsDefault),
        clientConfiguration,
        this.cosmosClientStateHandle,
        containerConfig,
        partitioningConfig,
        this.defaultParallelism,
        container
      )

      // Latest offset above has the EndLsn specified based on the point-in-time latest offset
      // For batch mode instead we need to reset it so that the change feed will get fully drained
      val inputPartitions = latestOffset
        .inputPartitions
        .get
        .map(partition => partition
          .withContinuationState(
            SparkBridgeImplementationInternal
              .extractChangeFeedStateForRange(initialOffsetJson, partition.feedRange),
            clearEndLsn = true))

      log.logInfo(s"<-- planInputPartitions $batchId (creating ${inputPartitions.length} partitions)")
      inputPartitions
    })
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    ChangeFeedScanPartitionReaderFactory(
      config,
      schema,
      DiagnosticsContext(correlationActivityId, "Batch"),
      cosmosClientStateHandle,
      diagnosticsConfig)
  }
}
