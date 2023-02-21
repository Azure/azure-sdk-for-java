// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.SparkBridgeInternal
import com.azure.cosmos.implementation.{SparkBridgeImplementationInternal, Strings}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty}
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

import java.nio.file.Paths
import java.time.Duration
import java.util.UUID

private class ChangeFeedBatch
(
  session: SparkSession,
  schema: StructType,
  config: Map[String, String],
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
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

    val calledFrom = s"ChangeFeedBatch.planInputPartitions(batchId $batchId)"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache.apply(
          clientConfiguration,
          Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
          calledFrom
        )),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          config,
          calledFrom,
          Some(cosmosClientStateHandles)))
    ).to(cacheItems => {
      val container =
        ThroughputControlHelper.getContainer(
          config,
          containerConfig,
          cacheItems(0).get,
          cacheItems(1))

      val hasBatchCheckpointLocation = changeFeedConfig.batchCheckpointLocation.isDefined &&
        !Strings.isNullOrWhiteSpace(changeFeedConfig.batchCheckpointLocation.get)

      // This maps the StartFrom settings to concrete LSNs
      val initialOffsetJson = if(hasBatchCheckpointLocation) {
        val startOffsetLocation = Paths.get(changeFeedConfig.batchCheckpointLocation.get, "startOffset").toString
        val metadataLog = new ChangeFeedInitialOffsetWriter(
          assertNotNull(session, "session"),
          assertNotNullOrEmpty(startOffsetLocation, "startOffset checkpointLocation"))

        if (metadataLog.get(0).isDefined) {
          val offsetJson = metadataLog.get(0).get

          log.logDebug(s"Start offset retrieved from file location '$startOffsetLocation' for batchId: $batchId " +
            s"-> offset: '$offsetJson'.")

          val changeFeedStateBase64 = ChangeFeedOffset.fromJson(offsetJson).changeFeedState
          val expectedContainerResourceId = container.read().block().getProperties.getResourceId
          val offsetIsValid = SparkBridgeImplementationInternal.validateCollectionRidOfChangeFeedState(
            changeFeedStateBase64,
            expectedContainerResourceId,
            changeFeedConfig.ignoreOffsetWhenInvalid
          )

          if (offsetIsValid) {
            changeFeedStateBase64
          } else {
            SparkBridgeInternal.clearCollectionCache(
              container,
              SparkBridgeImplementationInternal.extractCollectionRid(changeFeedStateBase64))

            val newOffsetJson = CosmosPartitionPlanner.createInitialOffset(
              container, changeFeedConfig, partitioningConfig, None)
            log.logWarning(s"Invalid Start offset '$offsetJson' retrieved from file location " +
              s"'$startOffsetLocation' for batchId: $batchId -> New offset retrieved from " +
              s"service: '$newOffsetJson'.")

            SparkBridgeImplementationInternal.validateCollectionRidOfChangeFeedState(
              newOffsetJson,
              expectedContainerResourceId,
              false
            )

            newOffsetJson
          }
        } else {
          val newOffsetJson = CosmosPartitionPlanner.createInitialOffset(container, changeFeedConfig, partitioningConfig, None)
          log.logDebug(s"No Start offset retrieved from file location '$startOffsetLocation' for batchId: $batchId " +
            s"-> offset retrieved from service: '$newOffsetJson'.")

          newOffsetJson
        }
      } else {
        val newOffsetJson = CosmosPartitionPlanner.createInitialOffset(container, changeFeedConfig, partitioningConfig, None)
        log.logDebug(s"No offset file location provided for batchId: $batchId " +
          s"-> offset retrieved from service: '$newOffsetJson'.")

        newOffsetJson
      }

      // Calculates the Input partitions based on start Lsn and latest Lsn
      val latestOffset = CosmosPartitionPlanner.getLatestOffset(
        config,
        ChangeFeedOffset(initialOffsetJson, None),
        changeFeedConfig.toReadLimit,
        // ok to use from cache because endLsn is ignored in batch mode
        Duration.ofMillis(PartitionMetadataCache.refreshIntervalInMsDefault),
        clientConfiguration,
        this.cosmosClientStateHandles,
        containerConfig,
        partitioningConfig,
        this.defaultParallelism,
        container
      )

      if(hasBatchCheckpointLocation) {
        val latestOffsetLocation = Paths.get(changeFeedConfig.batchCheckpointLocation.get, "latestOffset").toString
        val metadataLog = new ChangeFeedInitialOffsetWriter(
          assertNotNull(session, "session"),
          assertNotNullOrEmpty(latestOffsetLocation, "latestOffset checkpointLocation"))

        val latestOffsetJson = latestOffset.json()
        log.logDebug(s"Latest offset for batchId: $batchId -> $latestOffsetJson")
        if (!metadataLog.add(0, latestOffsetJson)) {
          val existingLatestOffset = metadataLog.get(0).get

          val msg = s"Cannot update latest offset at location '$latestOffsetLocation' for batchId: $batchId " +
            s"-> existing latestOffset: '$existingLatestOffset' failed to persist " +
            s"new latestOffset: '$latestOffsetJson'."

          if (existingLatestOffset != latestOffsetJson) {
            log.logError(msg)

            throw new IllegalStateException(msg)
          } else {
            log.logDebug(msg)
          }
        } else {
          log.logDebug(s"Successfully updated latest offset at location '$latestOffsetLocation' for batchId: $batchId " +
            s"-> existing latestOffset: 'n/a', new latestOffset: '$latestOffsetJson'.")
        }
      }

      // Latest offset above has the EndLsn specified based on the point-in-time latest offset
      // For batch mode instead we need to reset it so that the change feed will get fully drained
      val inputPartitions = latestOffset
        .inputPartitions
        .get
        .map(partition => partition
          .withContinuationState(
            SparkBridgeImplementationInternal
              .extractChangeFeedStateForRange(initialOffsetJson, partition.feedRange),
            clearEndLsn = !hasBatchCheckpointLocation))

      log.logInfo(s"<-- planInputPartitions $batchId (creating ${inputPartitions.length} partitions)")
      inputPartitions
    })
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    ChangeFeedScanPartitionReaderFactory(
      config,
      schema,
      DiagnosticsContext(correlationActivityId, "Batch"),
      cosmosClientStateHandles,
      diagnosticsConfig)
  }
}
