// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.changeFeedMetrics.ChangeFeedMetricsTracker

class ChangeFeedMetricsTrackerSpec extends UnitSpec {

 it should "track weighted changes per lsn" in {
  val metricsTracker = ChangeFeedMetricsTracker(1, NormalizedRange("0", "FF"), 5, 0.5)
  // Single tracking
  metricsTracker.track(100, 200) // 2 changes per LSN
  metricsTracker.getWeightedChangeFeedItemsPerLsn.get shouldBe 2.0

  // Multiple tracking
  metricsTracker.track(50, 150) // 3 changes per LSN
  // With default decay factor 0.5, weighted avg should be (2.0 * 0.5 + 3.0 * 1.0)/(0.5 + 1.0) = 2.67
  metricsTracker.getWeightedChangeFeedItemsPerLsn.get shouldBe 2.67 +- 0.01
 }

 it should "return none when no metrics tracked" in {
  val testRange = NormalizedRange("0", "FF")
  val metricsTracker = ChangeFeedMetricsTracker(1, testRange)

  metricsTracker.getWeightedChangeFeedItemsPerLsn shouldBe None
 }

 it should "handle zero LSN gap correctly" in {
  val testRange = NormalizedRange("0", "FF")
  val metricsTracker = ChangeFeedMetricsTracker(1, testRange, 5, 0.5)

  metricsTracker.track(0, 100) // Should treat as 100 changes
  metricsTracker.getWeightedChangeFeedItemsPerLsn.get shouldBe 100.0

  metricsTracker.track(0, 0) // Should treat as 1 change
  metricsTracker.getWeightedChangeFeedItemsPerLsn.get shouldBe (100.0 * 0.5 + 1.0)/(0.5 + 1.0) +- 0.01
 }


 it should "respect maxHistory limit" in {
  val maxHistory = 2
  val tracker = ChangeFeedMetricsTracker(0L, NormalizedRange("", "FF"), maxHistory, 0.5)

  tracker.track(10, 10) // 1.0 changes per LSN
  tracker.track(10, 20) // 2.0 changes per LSN
  tracker.track(10, 30) // 3.0 changes per LSN - should evict first entry

  // Should only consider last 2 entries: (2.0 * 0.5 + 3.0 * 1.0)/(0.5 + 1.0)
  tracker.getWeightedChangeFeedItemsPerLsn.get shouldBe 2.67 +- 0.01
 }

 it should "handle minimum change count of 1" in {
  val testRange = NormalizedRange("0", "FF")
  val metricsTracker = ChangeFeedMetricsTracker(1, testRange, 5, 0.5)

  // Small values should be normalized to minimum of 1 change
  metricsTracker.track(1000, 0)
  metricsTracker.getWeightedChangeFeedItemsPerLsn.get shouldBe 0.001 // 1/1000
 }
}
