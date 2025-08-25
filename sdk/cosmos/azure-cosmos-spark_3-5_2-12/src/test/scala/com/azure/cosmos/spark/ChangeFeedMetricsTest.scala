// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.changeFeedMetrics.ChangeFeedMetricsTracker

class ChangeFeedMetricsTest extends UnitSpec {

  "ChangeFeedMetricsTracker" should "track weighted changes per lsn" in {
    val testRange = NormalizedRange("0", "FF")
    val metricsTracker = new ChangeFeedMetricsTracker(1, testRange)
    metricsTracker.track(10000, 0)
    metricsTracker.getWeightedAvgChangesPerLsn shouldBe 10000
  }

  "ChangeFeedMetricsTracker" should "return none when no metrics tracked" in {
    val testRange = NormalizedRange("0", "FF")
    val metricsTracker = new ChangeFeedMetricsTracker(1, testRange)

    metricsTracker.getWeightedAvgChangesPerLsn shouldBe None
  }

  "ChangeFeedMetricsTracker" should "track limited metrics history" in {
    val testRange = NormalizedRange("0", "FF")
    val metricsTracker = new ChangeFeedMetricsTracker(1, testRange)

    metricsTracker.track(10000, 1)
    for (i <- 1 to 5) {
      metricsTracker.track(1, 2000)
    }

    metricsTracker.getWeightedAvgChangesPerLsn shouldBe 1.toDouble / 2000
  }
}