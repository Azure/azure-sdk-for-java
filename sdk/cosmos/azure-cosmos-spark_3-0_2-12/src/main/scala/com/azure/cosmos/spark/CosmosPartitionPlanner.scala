// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.ChangeFeedStartFromModes.Value

private class CosmosPartitionPlanner {

finsih me


  private case class PartitionMetadata
  (
    feedRange: String,
    documentCount: String,
    documentsSizeInKB: Long,
    latestLsn: Long
  )

}
