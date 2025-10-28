// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.CosmosPredicates.{requireNotNull, requireNotNullOrEmpty}

private case class PartitionPlanningInfo
(
  feedRange: NormalizedRange,
  storageSizeInMB: Double,
  progressWeightFactor: Double,
  scaleFactor: Double,
  endLsn: Option[Long]
) {
  requireNotNull(feedRange, "feedRange")
  requireNotNull(storageSizeInMB, "storageSizeInMB")
  requireNotNull(scaleFactor, "scaleFactor")
}
