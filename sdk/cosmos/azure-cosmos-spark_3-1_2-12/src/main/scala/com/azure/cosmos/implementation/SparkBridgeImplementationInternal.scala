// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientBuilderHelper
import com.azure.cosmos.implementation.changefeed.implementation.{ChangeFeedState, ChangeFeedStateV1}
import com.azure.cosmos.implementation.feedranges.{FeedRangeContinuation, FeedRangeEpkImpl, FeedRangeInternal}
import com.azure.cosmos.implementation.query.CompositeContinuationToken
import com.azure.cosmos.implementation.routing.Range
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.NormalizedRange

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[cosmos] object SparkBridgeImplementationInternal {
  def setMetadataCacheSnapshot(cosmosClientBuilder: CosmosClientBuilder,
                               metadataCache: CosmosClientMetadataCachesSnapshot): Unit = {

    val clientBuilderAccessor = CosmosClientBuilderHelper.getCosmosClientBuilderAccessor
    clientBuilderAccessor.setCosmosClientMetadataCachesSnapshot(cosmosClientBuilder, metadataCache)
  }

  def changeFeedContinuationToJson(continuation: String): String = {
    ChangeFeedState
      .fromString(continuation)
      .toJson()
  }

  def extractLsnFromChangeFeedContinuation(continuation: String): Long = {
    val lsnToken = ChangeFeedState
      .fromString(continuation)
      .getContinuation
      .getCurrentContinuationToken
      .getToken

    toLsn(lsnToken)
  }

  def overrideLsnInChangeFeedContinuation(continuation: String, newContinuationToken: String): String = {
    val state = ChangeFeedState
      .fromString(continuation)
    val continuationToken = state.getContinuation.getCurrentContinuationToken
    continuationToken.setToken(newContinuationToken)
    state.toString
  }

  def mergeChangeFeedContinuations(continuationTokens: Iterable[String]): String = {
    val states = continuationTokens.map(s => {
      ChangeFeedState.fromString(s)
    }).toArray

    ChangeFeedState.merge(states).toString()
  }

  def createChangeFeedStateJson
  (
    startOffsetContinuationState: String,
    feedRangeToLsn: Array[(NormalizedRange, Long)]
  ): String = {

    val continuationTokens = feedRangeToLsn
      .map(rangeToLsn => new CompositeContinuationToken(
        toContinuationToken(rangeToLsn._2),
        toCosmosRange(rangeToLsn._1)))
    val startState = ChangeFeedState.fromString(startOffsetContinuationState)
    new ChangeFeedStateV1(
      startState.getContainerRid,
      startState.getFeedRange,
      startState.getMode,
      startState.getStartFromSettings,
      FeedRangeContinuation.create(
        startState.getContainerRid,
        startState.getFeedRange,
        continuationTokens.toList.asJava
      )
    ).toString
  }

  def toContinuationToken(lsn: Long): String = {
    raw""""${String.valueOf(lsn)}""""
  }

  def extractContinuationTokensFromChangeFeedStateJson(stateJsonBase64: String): Array[(NormalizedRange, Long)] = {
    assert(!Strings.isNullOrWhiteSpace(stateJsonBase64), s"Argument 'stateJsonBase64' must not be null or empty.")
    val state = ChangeFeedState.fromString(stateJsonBase64)
    state
      .extractContinuationTokens() // already sorted
      .asScala
      .map(t => Tuple2(rangeToNormalizedRange(t.getRange), toLsn(t.getToken)))
      .toArray
  }

  private[this] def rangeToNormalizedRange(rangeInput: Range[String]) = {
    val range = FeedRangeInternal.normalizeRange(rangeInput)
    assert(range != null, "Argument 'range' must not be null.")
    assert(range.isMinInclusive, "Argument 'range' must be minInclusive")
    assert(!range.isMaxInclusive, "Argument 'range' must be maxExclusive")

    NormalizedRange(range.getMin, range.getMax)
  }

  def toLsn(lsnToken: String): Long = {
    // the continuation from the backend is encoded as '"<LSN>"' where LSN is a long integer
    // removing the first and last characters - which are the quotes
    if (lsnToken != null && lsnToken.length > 2) {
      if (lsnToken.startsWith("\"")) {
        lsnToken.substring(1, lsnToken.length - 1).toLong
      } else {
        lsnToken.toLong
      }
    } else {
      0
    }
  }

  def extractChangeFeedStateForRange
  (
    stateJsonBase64: String,
    feedRange: NormalizedRange
  ): String = {
    assert(!Strings.isNullOrWhiteSpace(stateJsonBase64), s"Argument 'stateJsonBase64' must not be null or empty.")
    ChangeFeedState
      .fromString(stateJsonBase64)
      .extractForEffectiveRange(toCosmosRange(feedRange))
      .toString
  }

  def toFeedRange(range: NormalizedRange): FeedRange = {
    new FeedRangeEpkImpl(toCosmosRange(range))
  }

  private[this] def toCosmosRange(range: NormalizedRange): Range[String] = {
    new Range[String](range.min, range.max, true, false)
  }

  def doRangesOverlap(left: NormalizedRange, right: NormalizedRange): Boolean = {
    Range.checkOverlapping(toCosmosRange(left), toCosmosRange(right))
  }

  private[cosmos] def toNormalizedRange(feedRange: FeedRange) = {
    val epk = feedRange.asInstanceOf[FeedRangeEpkImpl]
    rangeToNormalizedRange(epk.getRange)
  }
}
