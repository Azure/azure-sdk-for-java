// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.changeFeedMetrics

import com.azure.cosmos.implementation.guava25.collect.EvictingQueue
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.{CosmosConstants, NormalizedRange}

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[cosmos] object ChangeFeedMetricsTracker {
  private val MetricsHistory: Int = CosmosConstants.ChangeFeedMetricsConfigs.MetricsHistory
  private val MetricsHistoryDecayFactor: Double = CosmosConstants.ChangeFeedMetricsConfigs.MetricsHistoryDecayFactor

  def apply(
            partitionIndex: Long,
            feedRange: NormalizedRange,
            metricsHistory: Int,
            metricsHistoryDecayFactor: Double): ChangeFeedMetricsTracker = {
   new ChangeFeedMetricsTracker(partitionIndex, feedRange, metricsHistory, metricsHistoryDecayFactor)
  }

  def apply(partitionIndex: Long, feedRange: NormalizedRange): ChangeFeedMetricsTracker = {
    new ChangeFeedMetricsTracker(partitionIndex, feedRange, MetricsHistory, MetricsHistoryDecayFactor)
  }
}

/**
 * Tracks metrics for Change Feed operations including partition indices and LSN range
 * with exponential weighting for recent metrics
 */
private[cosmos] class ChangeFeedMetricsTracker(
    private val partitionIndex: Long,
    private val feedRange: NormalizedRange,
    private val maxHistory: Int = ChangeFeedMetricsTracker.MetricsHistory,
    private val decayFactor: Double = ChangeFeedMetricsTracker.MetricsHistoryDecayFactor) extends BasicLoggingTrait {

  private val changeFeedItemsPerLsnHistory = EvictingQueue.create[Double](maxHistory)
  private var currentChangeFeedItemsPerLsnOpt: Option[Double] = None

  /**
   * Track the normalized change feed items per lsn
   *
   * @param lsnRange the lsn range of the fetched items.
   * @param fetchedItemCnt the total fetched item cnt within a micro-batch for the feed range.
   */
  def track(lsnRange: Long, fetchedItemCnt: Long): Unit = {
    val effectiveChangesFetchedCnt = Math.max(1, fetchedItemCnt)
    val changesPerLsn = if (lsnRange == 0) effectiveChangesFetchedCnt.toDouble else effectiveChangesFetchedCnt.toDouble / lsnRange
    synchronized {
      changeFeedItemsPerLsnHistory.add(changesPerLsn)
      calculateWeightedItemsPerLsn()
    }
  }

  /**
   * Calculates weighted normalized items per lsn where recent values have more impact
   * Uses exponential weighting: weight = decayFactor^(n-i-1) where:
   * n = number of measurements
   * i = index of measurement (0 being oldest)
   * @return Weighted changes per lsn
   */
  private def calculateWeightedItemsPerLsn(): Unit = {
    if (!changeFeedItemsPerLsnHistory.isEmpty) {
      val histories = changeFeedItemsPerLsnHistory.asScala.toArray
      val n = histories.length
      var weightedSum = 0.0
      var weightSum = 0.0

      for (i <- histories.indices) {
        val weight = math.pow(decayFactor, n - i - 1)
        weightedSum += histories(i) * weight
        weightSum += weight
      }

      currentChangeFeedItemsPerLsnOpt = Some(weightedSum / weightSum)
    }
  }

  /**
   * Gets current weighted items per lsn
   * @return weighted items per lsn
   */
  def getWeightedChangeFeedItemsPerLsn: Option[Double] = {
    logDebug(s"getWeightedChangeFeedItemsPerLsn for feedRangeIndex [$partitionIndex], " +
     s"feedRange [$feedRange] value [$currentChangeFeedItemsPerLsnOpt]")
    this.currentChangeFeedItemsPerLsnOpt
  }
}