// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, SparkBridgeInternal}
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import reactor.core.scala.publisher.SMono
import reactor.core.scala.publisher.SMono.PimpJMono

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.collection.concurrent.TrieMap
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object ContainerFeedRangesCache extends BasicLoggingTrait{
  private val DefaultFeedRangeRefreshIntervalInMinutes = CosmosConstants.ContainerFeedRangeConfigs.FeedRangeRefreshIntervalInMinutes

  private val cache = new TrieMap[String, CachedFeedRanges]
  logInfo(s"Default container feed range refresh interval $DefaultFeedRangeRefreshIntervalInMinutes")

  def getFeedRanges
  (
    container: CosmosAsyncContainer,
    feedRangeRefreshIntervalInMinutesOpt: Option[Long],
  ): SMono[List[FeedRange]] = {

    val key = SparkBridgeInternal.getCacheKeyForContainer(container)
    val effectiveFeedRangeRefreshInterval = feedRangeRefreshIntervalInMinutesOpt.getOrElse(DefaultFeedRangeRefreshIntervalInMinutes)

    val cacheExpirationThreshold = Instant.now.minus(
      effectiveFeedRangeRefreshInterval,
      ChronoUnit.MINUTES)

    cache.get(key) match {
      case Some(cached) =>
        if (cached
          .retrievedAt
          .compareTo(cacheExpirationThreshold) >= 0) {

          SMono.just(cached.feedRanges)
        } else {
          refreshFeedRanges(key, container)
        }
      case None => refreshFeedRanges(key, container)
    }
  }

  private[this] def refreshFeedRanges(key: String, container: CosmosAsyncContainer): SMono[List[FeedRange]] = {

    TransientErrorsRetryPolicy.executeWithRetry(() =>
      container
        .getFeedRanges
        .map[List[FeedRange]](javaList => {
          val scalaList = javaList.asScala.toList
          cache.put(key, CachedFeedRanges(scalaList, Instant.now))
          scalaList
        })
        .asScala)
  }

  private case class CachedFeedRanges(feedRanges: List[FeedRange], retrievedAt: Instant)
}
