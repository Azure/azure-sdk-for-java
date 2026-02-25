// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

class PartitionPlanningInfoSpec extends UnitSpec {
  private[this] val rnd = scala.util.Random
  private[this] val feedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
  private[this] val storageSizeInMB = rnd.nextDouble()
  private[this] val progressWeightFactor = rnd.nextDouble()
  private[this] val scaleFactor = rnd.nextDouble()

  it should "create instance with valid parameters via apply" in {
    val viaCtor = PartitionPlanningInfo(feedRange, storageSizeInMB, progressWeightFactor, scaleFactor, None)

    viaCtor.feedRange shouldEqual feedRange
    viaCtor.storageSizeInMB shouldEqual storageSizeInMB
    viaCtor.progressWeightFactor shouldEqual progressWeightFactor
    viaCtor.scaleFactor shouldEqual scaleFactor
  }

  //scalastyle:off null
  it should "throw due to missing feedRange" in {
    assertThrows[IllegalArgumentException](
      PartitionPlanningInfo(null, storageSizeInMB, progressWeightFactor, scaleFactor, None)
    )
  }
  //scalastyle:on null
}
