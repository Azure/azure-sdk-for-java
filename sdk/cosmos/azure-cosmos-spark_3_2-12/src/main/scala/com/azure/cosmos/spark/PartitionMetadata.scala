// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
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
            firstLsn: Option[Long],
            fromNowContinuationToken: String,
            startLsn: Long = 0,
            endLsn: Option[Long] = None): PartitionMetadata = {
    // scalastyle:on parameter.number

    val nowEpochMs = Instant.now().toEpochMilli

    val latestLsn = SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(
      fromNowContinuationToken)

    PartitionMetadata(
      userConfig,
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig,
      feedRange,
      documentCount,
      totalDocumentSizeInKB,
      firstLsn,
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
  firstLsn: Option[Long],
  latestLsn: Long,
  startLsn: Long,
  endLsn: Option[Long],
  lastRetrieved: AtomicLong,
  lastUpdated: AtomicLong
)  extends BasicLoggingTrait {

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
      this.firstLsn,
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
      this.firstLsn,
      this.latestLsn,
      startLsn,
      Some(explicitEndLsn),
      new AtomicLong(this.lastRetrieved.get),
      new AtomicLong(this.lastUpdated.get)
    )
  }

  def getWeightedLsnGap: Long = {
    val progressFactor = math.max(this.getAndValidateLatestLsn - this.startLsn, 0)
    if (progressFactor == 0) {
      0
    } else {
      val averageItemsPerLsn = getAvgItemsPerLsn

      val weightedGap: Double = progressFactor * averageItemsPerLsn
      // Any double less than 1 gets rounded to 0 when toLong is invoked
      weightedGap.toLong.max(1)
    }
  }

  def getAvgItemsPerLsn: Double = {
    if (this.firstLsn.isEmpty) {
      math.max(1d, this.documentCount.toDouble / this.getAndValidateLatestLsn)
    } else if (this.documentCount == 0 || (this.getAndValidateLatestLsn - this.firstLsn.get) <= 0) {
      1d
    } else {
      this.documentCount.toDouble / (this.getAndValidateLatestLsn- this.firstLsn.get)
    }
  }

  def getAndValidateLatestLsn(): Long = {
    if (this.latestLsn == 0) {
      // latestLsn == 0 but startLsn > 0 means there was an issue where change feed continuation
      // was null - endLsn created here will be used as the startLsn for the next micro batch iteration
      // so it should never be smaller than startLsn
      this.startLsn
    } else {
      if (this.latestLsn < this.startLsn) {
        logInfo(s"Received LatestLSN '${this.latestLsn}' for range '${this.feedRange}' is smaller than the " +
          s"StartLSN from last offset '${this.startLsn}'. This can happen when there is a lagging replica with " +
          s"eventual consistency - and is not a problem when it happens temporarily - the next attempt to drain the " +
          s"change feed will hit other replica or replica has caught up in the meantime. So eventually all " +
          s"events will be processed.")

        this.startLsn
      } else {
        this.latestLsn
      }
    }
  }
}
