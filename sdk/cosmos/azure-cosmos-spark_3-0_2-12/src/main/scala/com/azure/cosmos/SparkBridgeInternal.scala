// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos

import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl
import com.azure.cosmos.implementation.routing.Range
import com.azure.cosmos.spark.NormalizedRange

private[cosmos] object SparkBridgeInternal {
  def trySplitFeedRange
  (
    container: CosmosAsyncContainer,
    feedRange: NormalizedRange,
    targetedCountAfterSplit: Int
  ): Array[String] = {

    val list = container
      .trySplitFeedRange(new FeedRangeEpkImpl(toCosmosRange(feedRange)), targetedCountAfterSplit)
      .block

    val array = new Array[String](list.size)
    for (i <- 0 until list.size) {
      array(i) = list.get(i).toString
    }
    array
  }

  private[this] def toCosmosRange(range: NormalizedRange): Range[String] = {
    new Range[String](range.min, range.max, true, false)
  }
}
