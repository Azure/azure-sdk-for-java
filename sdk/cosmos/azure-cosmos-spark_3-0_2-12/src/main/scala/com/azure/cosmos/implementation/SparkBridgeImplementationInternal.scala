// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.routing.Range
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientBuilderHelper
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState
import com.azure.cosmos.implementation.feedranges.{FeedRangeEpkImpl, FeedRangeInternal}
import com.azure.cosmos.models.FeedRange

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[cosmos] object SparkBridgeImplementationInternal {
  def setMetadataCacheSnapshot(cosmosClientBuilder: CosmosClientBuilder,
                               metadataCache: CosmosClientMetadataCachesSnapshot): Unit = {

    val clientBuilderAccessor = CosmosClientBuilderHelper.getCosmosClientBuilderAccessor
    clientBuilderAccessor.setCosmosClientMetadataCachesSnapshot(cosmosClientBuilder, metadataCache)
  }

  def extractLsnFromChangeFeedContinuation(continuation: String) : Long = {
    val lsnToken = ChangeFeedState
      .fromString(continuation)
      .getContinuation
      .getCurrentContinuationToken
      .getToken

    convertToLsn(lsnToken)
  }

  def convertToLsn(lsnToken: String): Long = {
    // the continuation from the backend is encoded as '"<LSN>"' where LSN is a long integer
    // removing the first and last characters - which are the quotes
    lsnToken.substring(1, lsnToken.length - 1).toLong
  }

  def mergeChangeFeedContinuations(continuationTokens: Iterable[String]): String = {
    var count = 0
    val states = continuationTokens.map(s => {
      count+=1
      ChangeFeedState.fromString(s)
    }).toArray

    ChangeFeedState.merge(states).toJson()
  }

  def getOrderedFeedRangeToLsnTokens
  (
    stateJson: String,
    latestPartitionMetadata: Array[(String, Long)]
  ): Product2[Array[(Range[String], Long)], Array[(Range[String], Long)]] = {

    assert(!Strings.isNullOrWhiteSpace(stateJson), s"Argument 'stateJson' must not be null or empty.")
    val state = ChangeFeedState.fromString(stateJson)
    val orderedStartTokens = state
      .extractContinuationTokens() // already sorted
      .asScala
      .map(t => Tuple2(t.getRange, convertToLsn(t.getToken)))
      .toArray

    val orderedLatestTokens = latestPartitionMetadata
      .map(metadata => {
        val range = FeedRangeInternal
          .convert(FeedRange.fromString(metadata._1))
          .asInstanceOf[FeedRangeEpkImpl]
          .getRange
        Tuple2(FeedRangeInternal.normalizeRange(range), metadata._2)
      })
      .toArray
      .sortBy(rangeLsnPair => rangeLsnPair._1.getMin())

    (orderedStartTokens, orderedLatestTokens)
  }
}
