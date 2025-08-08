// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.changeFeedMetrics

import com.azure.cosmos.implementation.guava25.collect.EvictingQueue
import com.azure.cosmos.spark.NormalizedRange

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[cosmos] object ChangeFeedMetricsTracker {
  // Default values for tracker configuration
  private val DefaultMaxHistory: Int = 5
  private val DefaultDecayFactor: Double = 0.85

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
    private val decayFactor: Double = ChangeFeedMetricsTracker.DefaultDecayFactor
) {
  private val changeFeedChangesPerLsnHistory = EvictingQueue.create[Double](maxHistory)

  /**
   * Track the normalized change feed changes per lsn
   * @param changesPerLsn The latest changes per lsn metric.
   */
  def track(lsnGap: Long, changesFetchedCnt: Long): Unit = {
    val changesPerLsn = changesFetchedCnt.toDouble / lsnGap
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
  private def calculateWeightedChangesPerLsn(): Option[Double] = {
    synchronized {
      if (changeFeedChangesPerLsnHistory.isEmpty) {
        None
      } else {
        val gaps = changeFeedChangesPerLsnHistory.asScala.toArray
        val n = gaps.length
        var weightedSum = 0.0
        var weightSum = 0.0
        
        for (i <- gaps.indices) {
          val weight = math.pow(decayFactor, n - i - 1)
          weightedSum += gaps(i) * weight
          weightSum += weight
        }
        
        Some(weightedSum / weightSum)
      }
    }
  }

  /**
   * Gets current weighted changes per lsn
   * @return Current weighted LSN gap
   */
  def getWeightedAvgChangesPerLsn: Option[Double] = {
    synchronized {
      calculateWeightedChangesPerLsn()
    }
  }
}