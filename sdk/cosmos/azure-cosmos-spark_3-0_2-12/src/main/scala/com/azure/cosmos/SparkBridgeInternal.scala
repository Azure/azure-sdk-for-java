// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.implementation.feedranges.{FeedRangeEpkImpl, FeedRangeInternal}
import com.azure.cosmos.implementation.routing.Range
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.NormalizedRange
import reactor.core.publisher.Mono
import reactor.core.scala.publisher.SMono
import reactor.core.scala.publisher.SMono.PimpJMono

private[cosmos] object SparkBridgeInternal {
  def trySplitFeedRange
  (
    container: CosmosAsyncContainer,
    feedRange: NormalizedRange,
    targetedCountAfterSplit: Int
  ): Array[NormalizedRange] = {

    val list = container
      .trySplitFeedRange(new FeedRangeEpkImpl(toCosmosRange(feedRange)), targetedCountAfterSplit)
      .block

    val array = new Array[NormalizedRange](list.size)
    for (i <- 0 until list.size) {
      array(i) = SparkBridgeImplementationInternal.toNormalizedRange(list.get(i))
    }
    array
  }

  def toCosmosRange(range: NormalizedRange): Range[String] = {
    new Range[String](range.min, range.max, true, false)
  }
}
