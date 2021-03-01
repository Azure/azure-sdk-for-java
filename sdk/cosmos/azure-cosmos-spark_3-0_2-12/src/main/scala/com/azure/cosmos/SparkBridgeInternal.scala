// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos

import com.azure.cosmos.models.FeedRange

private[cosmos] object SparkBridgeInternal {
  def trySplitFeedRange
  (
    container: CosmosAsyncContainer,
    feedRange: String,
    targetedCountAfterSplit: Int
  ): Array[String] = {

    val list = container
      .trySplitFeedRange(FeedRange.fromString(feedRange), targetedCountAfterSplit)
      .block

    val array = new Array[String](list.size)
    for (i <- 0 until list.size) {
      array(i) = list.get(i).toString
    }
    array
  }
}
