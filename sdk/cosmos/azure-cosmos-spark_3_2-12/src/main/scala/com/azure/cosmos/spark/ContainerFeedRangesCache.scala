// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.{CosmosAsyncContainer, SparkBridgeInternal}
import reactor.core.scala.publisher.SMono
import reactor.core.scala.publisher.SMono.PimpJMono

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.DurationLong
import scala.util.Random
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object ContainerFeedRangesCache {
  private val rnd = Random
  private val RANDOM_IN_MS = 5000
  private val DEFAULT_FEED_RANGE_REFRESH_INTERVAL_IN_SECONDS = 120L
  private val cache = new TrieMap[String, CachedFeedRanges]

  def getFeedRanges
  (
   container: CosmosAsyncContainer,
   feedRangeRefreshIntervalInSecondsOpt: Option[Long],
  ): SMono[List[FeedRange]] = {

    val key = SparkBridgeInternal.getCacheKeyForContainer(container)
    val cacheExpirationThreshold = Instant.now.minus(
      feedRangeRefreshIntervalInSecondsOpt.getOrElse(DEFAULT_FEED_RANGE_REFRESH_INTERVAL_IN_SECONDS),
      ChronoUnit.SECONDS)

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
   // Introduce a small randomized delay to stagger executor refreshFeedRanges
   // and reduce the risk of a metadata RU surge when multiple Spark executors start simultaneously.
    val randomDelay = rnd.nextInt(RANDOM_IN_MS).toLong
    SMono.delay(randomDelay.millis)
     .flatMap(_ => {
       TransientErrorsRetryPolicy.executeWithRetry(() =>
         container
          .getFeedRanges
          .map[List[FeedRange]](javaList => {
            val scalaList = javaList.asScala.toList
            cache.put(key, CachedFeedRanges(scalaList, Instant.now))
            scalaList
          })
          .asScala)
     })
  }

  private case class CachedFeedRanges(feedRanges: List[FeedRange], retrievedAt: Instant)
}
