// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.changeFeedMetrics.ChangeFeedMetricsTracker
import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.broadcast.Broadcast

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private object PartitionMetadata {
  def createKey(databaseId: String,
                containerId: String,
                feedRange: NormalizedRange): String =
    s"$databaseId/$containerId/${feedRange.min}-${feedRange.max}"

  // scalastyle:off parameter.number
  def apply(userConfig: Map[String, String],
            cosmosClientConfig: CosmosClientConfiguration,
            cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
            cosmosContainerConfig: CosmosContainerConfig,
            feedRange: NormalizedRange,
            documentCount: Long,
            totalDocumentSizeInKB: Long,
            firstLsn: Option[Long],
            latestLsn: Long,
            startLsn: Long,
            endLsn: Option[Long],
            lastRetrieved: AtomicLong,
            lastUpdated: AtomicLong): PartitionMetadata = {
    new PartitionMetadata(
      userConfig,
      cosmosClientConfig,
      cosmosClientStateHandles,
      cosmosContainerConfig,
      feedRange,
      documentCount,
      totalDocumentSizeInKB,
      firstLsn,
      latestLsn,
      startLsn,
      endLsn,
      lastRetrieved,
      lastUpdated,
      None)
  }

  def apply(userConfig: Map[String, String],
            cosmosClientConfig: CosmosClientConfiguration,
            cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
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
      cosmosClientStateHandles,
      cosmosContainerConfig,
      feedRange,
      documentCount,
      totalDocumentSizeInKB,
      firstLsn,
      latestLsn,
      startLsn,
      endLsn,
      new AtomicLong(nowEpochMs),
      new AtomicLong(nowEpochMs),
      Some(fromNowContinuationToken)
    )
  }
}

private[cosmos] case class PartitionMetadata
(
  userConfig: Map[String, String],
  cosmosClientConfig: CosmosClientConfiguration,
  cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
  cosmosContainerConfig: CosmosContainerConfig,
  feedRange: NormalizedRange,
  documentCount: Long,
  totalDocumentSizeInKB: Long,
  firstLsn: Option[Long],
  latestLsn: Long,
  startLsn: Long,
  endLsn: Option[Long],
  lastRetrieved: AtomicLong,
  lastUpdated: AtomicLong,
  fromNowContinuationState: Option[String]
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
      this.cosmosClientStateHandles,
      this.cosmosContainerConfig,
      subRange,
      this.documentCount,
      this.totalDocumentSizeInKB,
      this.firstLsn,
      this.latestLsn,
      startLsn,
      this.endLsn,
      new AtomicLong(this.lastRetrieved.get),
      new AtomicLong(this.lastUpdated.get),
      this.getFromNowContinuationStateForRange(subRange)
    )
  }

  def withEndLsn(explicitEndLsn: Long): PartitionMetadata = {
    new PartitionMetadata(
      this.userConfig,
      this.cosmosClientConfig,
      this.cosmosClientStateHandles,
      this.cosmosContainerConfig,
      this.feedRange,
      this.documentCount,
      this.totalDocumentSizeInKB,
      this.firstLsn,
      this.latestLsn,
      startLsn,
      Some(explicitEndLsn),
      new AtomicLong(this.lastRetrieved.get),
      new AtomicLong(this.lastUpdated.get),
      this.fromNowContinuationState
    )
  }

  def splitByLatestLsn(): Seq[PartitionMetadata] = {
    this.fromNowContinuationState match {
      case None => Seq(this)
      case Some(stateJson) =>
        val parsedState = SparkBridgeImplementationInternal.parseChangeFeedState(stateJson)
        val latestLsnsByRange =
          SparkBridgeImplementationInternal.extractContinuationTokensFromChangeFeedState(parsedState)

        if (latestLsnsByRange.length <= 1) {
          Seq(this)
        } else {
          logInfo(
            s"FromNow continuation for range '${this.feedRange}' resolved to " +
              s"${latestLsnsByRange.length} effective ranges. Planning each range independently.")

          val effectiveRangesAndLsns = latestLsnsByRange
            .flatMap(rangeAndLsn =>
              this.intersect(this.feedRange, rangeAndLsn._1)
                .map(effectiveRange => effectiveRange -> rangeAndLsn._2))
            .toSeq
          if (effectiveRangesAndLsns.length != latestLsnsByRange.length) {
            throw new IllegalStateException(
              s"FromNow continuation for range '${this.feedRange}' contained non-overlapping effective ranges.")
          }
          val effectiveStates = SparkBridgeImplementationInternal.extractChangeFeedStateForRanges(
            parsedState,
            effectiveRangesAndLsns.map(_._1))

          require(
            effectiveStates.length == effectiveRangesAndLsns.length,
            "Expected one continuation state for every effective range.")

          effectiveRangesAndLsns
            .zip(effectiveStates)
            .map { case ((effectiveRange, effectiveLatestLsn), effectiveState) =>
              this.withFeedRangeAndLatestLsn(
                effectiveRange,
                effectiveLatestLsn,
                effectiveState)
            }
        }
    }
  }

  def getWeightedLsnGap(
                        isChangeFeed: Boolean,
                        partitionMetricsMap: Option[ConcurrentHashMap[NormalizedRange, ChangeFeedMetricsTracker]] = None): Long = {
    val progressFactor = math.max(this.getAndValidateLatestLsn - this.startLsn, 0)
    if (progressFactor == 0) {
      0
    } else {
      val effectiveItemsPerLsn = this.getAvgItemsPerLsn(isChangeFeed, partitionMetricsMap)

      val weightedGap: Double = progressFactor * effectiveItemsPerLsn
      // Any double less than 1 gets rounded to 0 when toLong is invoked
      weightedGap.toLong.max(1)
    }
  }

  def getAvgItemsPerLsn(isChangeFeed: Boolean,
                        partitionMetricsMap: Option[ConcurrentHashMap[NormalizedRange, ChangeFeedMetricsTracker]] = None): Double = {
    var itemsPerLsnFromMetricsOpt: Option[Double] = None
    if (isChangeFeed) {
      partitionMetricsMap match {
        case Some(metricsMap) =>
          if (metricsMap.containsKey(this.feedRange)) {
            itemsPerLsnFromMetricsOpt = partitionMetricsMap.get.get(this.feedRange).getWeightedChangeFeedItemsPerLsn
          }
        case None =>
      }
    }

    itemsPerLsnFromMetricsOpt match {
      case Some(itemsPerLsnFromMetrics) => itemsPerLsnFromMetrics
      case None => getDefaultAvgItemsPerLsn
    }
  }

  def getDefaultAvgItemsPerLsn: Double = {
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

  private def withFeedRangeAndLatestLsn(
    effectiveRange: NormalizedRange,
    effectiveLatestLsn: Long,
    effectiveFromNowContinuationState: String
  ): PartitionMetadata = {
    new PartitionMetadata(
      this.userConfig,
      this.cosmosClientConfig,
      this.cosmosClientStateHandles,
      this.cosmosContainerConfig,
      effectiveRange,
      0,
      0,
      None,
      effectiveLatestLsn,
      this.startLsn,
      this.endLsn,
      new AtomicLong(this.lastRetrieved.get),
      new AtomicLong(this.lastUpdated.get),
      Some(effectiveFromNowContinuationState)
    )
  }

  private def getFromNowContinuationStateForRange(
    effectiveRange: NormalizedRange
  ): Option[String] = {
    this.fromNowContinuationState.map(state => {
      if (effectiveRange == this.feedRange) {
        state
      } else {
        SparkBridgeImplementationInternal.extractChangeFeedStateForRange(
          SparkBridgeImplementationInternal.parseChangeFeedState(state),
          effectiveRange)
      }
    })
  }

  private def intersect(
    left: NormalizedRange,
    right: NormalizedRange
  ): Option[NormalizedRange] = {
    val min = if (left.min.compareTo(right.min) >= 0) left.min else right.min
    val max = if (left.max.compareTo(right.max) <= 0) left.max else right.max
    if (min.compareTo(max) < 0) Some(NormalizedRange(min, max)) else None
  }
}
