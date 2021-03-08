// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.connector.read.streaming.{MicroBatchStream, Offset, ReadLimit, SupportsAdmissionControl}
import org.apache.spark.sql.types.StructType

import java.time.Duration
import java.util.UUID

// scala style rule flaky - even complaining on partial log messages
// scalastyle:off multiple.string.literals
private class ChangeFeedMicroBatchStream
(
  val session: SparkSession,
  val schema: StructType,
  val config: Map[String, String],
  val cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  val checkpointLocation: String
) extends MicroBatchStream
  with SupportsAdmissionControl
  with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val clientConfiguration = CosmosClientConfiguration.apply(config, readConfig.forceEventualConsistency)
  private val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val partitioningConfig = CosmosPartitioningConfig.parseCosmosPartitioningConfig(config)
  private val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(config)
  private val client = CosmosClientCache.apply(clientConfiguration, Some(cosmosClientStateHandle))
  private val container = client.getDatabase(containerConfig.database).getContainer(containerConfig.container)
  private val streamId = UUID.randomUUID().toString
  private var latestOffsetSnapshot: Option[ChangeFeedOffset] = None

  override def latestOffset(): Offset = {
    throw new UnsupportedOperationException(
      "latestOffset(Offset, ReadLimit) should be called instead of this method")
  }

  override def planInputPartitions(startOffset: Offset, endOffset: Offset): Array[InputPartition] = {
    assertNotNull(startOffset, "startOffset")
    assertNotNull(endOffset, "endOffset")
    assert(startOffset.isInstanceOf[ChangeFeedOffset], "Argument 'startOffset' is not a change feed offset.")
    assert(endOffset.isInstanceOf[ChangeFeedOffset], "Argument 'endOffset' is not a change feed offset.")

    val start = startOffset.asInstanceOf[ChangeFeedOffset]
    val end = endOffset.asInstanceOf[ChangeFeedOffset]

    assert(end.inputPartitions.isDefined, "Argument 'endOffset.inputPartitions' must not be null or empty.")

    val startJson = start.json()

    end
      .inputPartitions
      .get
      .map(partition => partition
        .withContinuationState(
          SparkBridgeImplementationInternal
            .extractChangeFeedStateForRange(startJson, partition.feedRange)))
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    ChangeFeedScanPartitionReaderFactory(config, schema, cosmosClientStateHandle)
  }

  override def latestOffset(startOffset: Offset, readLimit: ReadLimit): Offset = {
    val offset = CosmosPartitionPlanner.getLatestOffset(
      startOffset.asInstanceOf[ChangeFeedOffset],
      readLimit,
      Duration.ZERO,
      this.clientConfiguration,
      this.cosmosClientStateHandle,
      this.containerConfig,
      this.partitioningConfig,
      this.session
    )
    this.latestOffsetSnapshot = Some(offset)
    offset
  }

  override def initialOffset(): Offset = {
    assertOnSparkDriver()

    val metadataLog = new ChangeFeedInitialOffsetWriter(
        assertNotNull(session, "session"),
        assertNotNullOrEmpty(checkpointLocation, "checkpointLocation"))
    val offsetJson = metadataLog.get(0).getOrElse {
      val newOffsetJson = CosmosPartitionPlanner.createInitialOffset(container, changeFeedConfig, Some(streamId))
      metadataLog.add(0, newOffsetJson)
      newOffsetJson
    }

    logInfo(s"MicroBatch stream $streamId: Initial offset '$offsetJson'.")
    ChangeFeedOffset(offsetJson, None)
  }

  override def deserializeOffset(s: String): Offset = {
    logDebug(s"MicroBatch stream $streamId: Deserialized offset '$s'.")
    ChangeFeedOffset.fromJson(s)
  }

  override def commit(offset: Offset): Unit = {
    logInfo(s"MicroBatch stream $streamId: Committed offset '${offset.json()}'.")
  }

  override def stop(): Unit = {
    logInfo(s"MicroBatch stream $streamId: stopped.")
  }
}
// scalastyle:on multiple.string.literals
