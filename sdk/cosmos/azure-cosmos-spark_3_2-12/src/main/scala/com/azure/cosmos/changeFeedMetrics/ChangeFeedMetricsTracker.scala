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
  private val DefaultMaxHistory: Int = CosmosConstants.ChangeFeedTrackerConfigs.changeFeedMetricsTrackerMaxHistory
  private val DefaultDecayFactor: Double = CosmosConstants.ChangeFeedTrackerConfigs.changeFeedMetricsTrackerDecayFactor

  def apply(partitionIndex: Long, feedRange: NormalizedRange): ChangeFeedMetricsTracker = {
    new ChangeFeedMetricsTracker(partitionIndex, feedRange)
  }

  def apply(partitionIndex: Long, feedRange: NormalizedRange, decayFactor: Double): ChangeFeedMetricsTracker = {
    new ChangeFeedMetricsTracker(partitionIndex, feedRange, DefaultMaxHistory, decayFactor)
  }
}

/**
 * Tracks metrics for Change Feed operations including partition indices and LSN gaps
 * with exponential weighting for recent metrics
 */
private[cosmos] class ChangeFeedMetricsTracker(
    private val partitionIndex: Long,
    private val feedRange: NormalizedRange,
    private val maxHistory: Int = ChangeFeedMetricsTracker.DefaultMaxHistory,
    private val decayFactor: Double = ChangeFeedMetricsTracker.DefaultDecayFactor) extends BasicLoggingTrait {

  private val changeFeedChangesPerLsnHistory = EvictingQueue.create[Double](maxHistory)
  private var currentChangesPerLsnOpt: Option[Double] = None

  /**
   * Track the normalized change feed changes per lsn
   *
   * @param lsnGap the lsn gap has been observed.
   * @param changesFetchedCnt the total fetched changes.
   */
  def track(lsnGap: Long, changesFetchedCnt: Long): Unit = {
    val effectiveChangesFetchedCnt = Math.max(1, changesFetchedCnt)
    val changesPerLsn = if (lsnGap == 0) effectiveChangesFetchedCnt.toDouble else effectiveChangesFetchedCnt.toDouble / lsnGap
    synchronized {
      changeFeedChangesPerLsnHistory.add(changesPerLsn)
      calculateWeightedChangesPerLsn()
    }
  }

  /**
   * Calculates weighted normalized changes per lsn where recent values have more impact
   * Uses exponential weighting: weight = decayFactor^(n-i-1) where:
   * n = number of measurements
   * i = index of measurement (0 being oldest)
   * @return Weighted average of LSN gaps
   */
  private def calculateWeightedChangesPerLsn(): Unit = {
    if (!changeFeedChangesPerLsnHistory.isEmpty) {
      val gaps = changeFeedChangesPerLsnHistory.asScala.toArray
      val n = gaps.length
      var weightedSum = 0.0
      var weightSum = 0.0

      for (i <- gaps.indices) {
        val weight = math.pow(decayFactor, n - i - 1)
        weightedSum += gaps(i) * weight
        weightSum += weight
      }

      currentChangesPerLsnOpt = Some(weightedSum / weightSum)
    }
  }

  /**
   * Gets current weighted changes per lsn
   * @return weighted changes per lsn
   */
  def getWeightedAvgChangesPerLsn: Option[Double] = {
    logDebug(s"getWeightedAvgChangesPerLsn for feedRangeIndex [$partitionIndex], feedRange [$feedRange] value [$currentChangesPerLsnOpt]")
    this.currentChangesPerLsnOpt
  }
}