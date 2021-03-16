// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import org.apache.spark.broadcast.Broadcast

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

private object PartitionMetadata {
  def createKey(databaseId: String,
                containerId: String,
                feedRange: NormalizedRange): String =
    s"$databaseId/$containerId/${feedRange.min}-${feedRange.max}"

  // scalastyle:off parameter.number
  def apply(userConfig: Map[String, String],
            cosmosClientConfig: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
            cosmosContainerConfig: CosmosContainerConfig,
            feedRange: NormalizedRange,
            documentCount: Long,
            totalDocumentSizeInKB: Long,
            continuationToken: String,
            startLsn: Long = 0,
            endLsn: Option[Long] = None): PartitionMetadata = {
    // scalastyle:on parameter.number

    val nowEpochMs = Instant.now().toEpochMilli

    val latestLsn = SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(
      continuationToken)

    PartitionMetadata(
      userConfig,
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig,
      feedRange,
      documentCount,
      totalDocumentSizeInKB,
      latestLsn,
      startLsn,
      endLsn,
      new AtomicLong(nowEpochMs),
      new AtomicLong(nowEpochMs)
    )
  }
}

private[cosmos] case class PartitionMetadata
(
  userConfig: Map[String, String],
  cosmosClientConfig: CosmosClientConfiguration,
  cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
  cosmosContainerConfig: CosmosContainerConfig,
  feedRange: NormalizedRange,
  documentCount: Long,
  totalDocumentSizeInKB: Long,
  latestLsn: Long,
  startLsn: Long,
  endLsn: Option[Long],
  lastRetrieved: AtomicLong,
  lastUpdated: AtomicLong
) {

  requireNotNull(feedRange, "feedRange")
  requireNotNull(cosmosClientConfig, "cosmosClientConfig")
  requireNotNull(cosmosContainerConfig, "cosmosContainerConfig")
  requireNotNull(startLsn, "startLsn")
  requireNotNull(lastRetrieved, "lastRetrieved")
  requireNotNull(lastUpdated, "lastUpdated")

  def cloneForSubRange(subRange: NormalizedRange, startLsn: Long): PartitionMetadata = {
    new PartitionMetadata(
      this.userConfig,
      this.cosmosClientConfig,
      this.cosmosClientStateHandle,
      this.cosmosContainerConfig,
      subRange,
      this.documentCount,
      this.totalDocumentSizeInKB,
      this.latestLsn,
      startLsn,
      this.endLsn,
      new AtomicLong(this.lastRetrieved.get),
      new AtomicLong(this.lastUpdated.get)
    )
  }

  def withEndLsn(explicitEndLsn: Long): PartitionMetadata = {
    new PartitionMetadata(
      this.userConfig,
      this.cosmosClientConfig,
      this.cosmosClientStateHandle,
      this.cosmosContainerConfig,
      this.feedRange,
      this.documentCount,
      this.totalDocumentSizeInKB,
      this.latestLsn,
      startLsn,
      Some(explicitEndLsn),
      new AtomicLong(this.lastRetrieved.get),
      new AtomicLong(this.lastUpdated.get)
    )
  }

  def getWeightedLsnGap: Long = {
    val progressFactor = math.max(this.latestLsn - this.startLsn, 0)
    val averageItemsPerLsn = if (this.documentCount == 0) {
      1d
    } else {
      this.latestLsn / this.documentCount.toDouble
    }

    (progressFactor * averageItemsPerLsn).toLong
  }
}
