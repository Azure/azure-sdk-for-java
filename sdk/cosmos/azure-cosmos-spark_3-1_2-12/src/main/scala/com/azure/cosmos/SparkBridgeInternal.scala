// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.implementation.SparkBridgeImplementationInternal.rangeToNormalizedRange
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl
import com.azure.cosmos.implementation.routing.Range
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.NormalizedRange

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

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

    list.asScala.map(e => SparkBridgeImplementationInternal.toNormalizedRange(e)).toArray
  }

  private[this] def toCosmosRange(range: NormalizedRange): Range[String] = {
    new Range[String](range.min, range.max, true, false)
  }

  private[cosmos] def getCacheKeyForContainer(container: CosmosAsyncContainer): String = {
    val database = container.getDatabase
    s"${database.getClient.getServiceEndpoint}|${database.getId}|${container.getId}"
  }

  private[cosmos] def getNormalizedEffectiveRange
  (
    container: CosmosAsyncContainer,
    feedRange: FeedRange
  ) : NormalizedRange = {

    SparkBridgeImplementationInternal
      .rangeToNormalizedRange(
        container.getNormalizedEffectiveRange(feedRange).block)
  }
}
