// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation

import com.azure.cosmos.{CosmosAsyncClient,  CosmosClientBuilder, DirectConnectionConfig, SparkBridgeInternal}
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientBuilderHelper
import com.azure.cosmos.implementation.changefeed.implementation.{ChangeFeedMode, ChangeFeedStartFromInternal, ChangeFeedState, ChangeFeedStateV1}
import com.azure.cosmos.implementation.query.CompositeContinuationToken
import com.azure.cosmos.implementation.routing.Range
import com.azure.cosmos.models.{FeedRange, PartitionKey, SparkModelBridgeInternal}
import com.azure.cosmos.spark.{ChangeFeedOffset, NormalizedRange}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

// scalastyle:off underscore.import
import com.azure.cosmos.implementation.feedranges._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[cosmos] object SparkBridgeImplementationInternal extends BasicLoggingTrait {
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

    ChangeFeedState.merge(states).toString
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

  private[cosmos] def rangeToNormalizedRange(rangeInput: Range[String]) = {
    val range = FeedRangeInternal.normalizeRange(rangeInput)
    assert(range != null, "Argument 'range' must not be null.")
    assert(range.isMinInclusive, "Argument 'range' must be minInclusive")
    assert(!range.isMaxInclusive, "Argument 'range' must be maxExclusive")

    NormalizedRange(range.getMin, range.getMax)
  }

  def toLsn(lsnToken: String): Long = {
    // the continuation from the backend is encoded as '"<LSN>"' where LSN is a long integer
    // removing the first and last characters - which are the quotes
    if (lsnToken != null) {
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

  private[cosmos] def toCosmosRange(range: NormalizedRange): Range[String] = {
    new Range[String](range.min, range.max, true, false)
  }

  def doRangesOverlap(left: NormalizedRange, right: NormalizedRange): Boolean = {
    Range.checkOverlapping(toCosmosRange(left), toCosmosRange(right))
  }

  private[cosmos] def toNormalizedRange(feedRange: FeedRange) = {
    val epk = feedRange.asInstanceOf[FeedRangeEpkImpl]
    rangeToNormalizedRange(epk.getRange)
  }

  private[cosmos] def partitionKeyValueToNormalizedRange
  (
    partitionKeyValue: Object,
    partitionKeyDefinitionJson: String
  ): NormalizedRange = {

    val feedRange = FeedRange
      .forLogicalPartition(new PartitionKey(partitionKeyValue))
      .asInstanceOf[FeedRangePartitionKeyImpl]

    val pkDefinition = SparkModelBridgeInternal.createPartitionKeyDefinitionFromJson(partitionKeyDefinitionJson)
    rangeToNormalizedRange(feedRange.getEffectiveRange(pkDefinition))
  }

  def setIoThreadCountPerCoreFactor
  (
    config: DirectConnectionConfig,
    ioThreadCountPerCoreFactor: Int
  ): DirectConnectionConfig = {

    ImplementationBridgeHelpers
      .DirectConnectionConfigHelper
      .getDirectConnectionConfigAccessor
      .setIoThreadCountPerCoreFactor(config, ioThreadCountPerCoreFactor)
  }

  def setIoThreadPriority
  (
    config: DirectConnectionConfig,
    ioThreadPriority: Int
  ): DirectConnectionConfig = {

    ImplementationBridgeHelpers
      .DirectConnectionConfigHelper
      .getDirectConnectionConfigAccessor
      .setIoThreadPriority(config, ioThreadPriority)
  }

  def setUserAgentWithSnapshotInsteadOfBeta(): Unit = {
    HttpConstants.Versions.useSnapshotInsteadOfBeta()
  }

  def createChangeFeedOffsetFromSpark2
  (
    client: CosmosAsyncClient,
    databaseResourceId: String,
    containerResourceId: String,
    tokens: Map[Int, Long]
  ): String = {

    val databaseName = client
      .getDatabase(databaseResourceId)
      .read()
      .block()
      .getProperties
      .getId

    val containerName = client
      .getDatabase(databaseResourceId)
      .getContainer(containerResourceId)
      .read()
      .block()
      .getProperties
      .getId

    val container = client
      .getDatabase(databaseName)
      .getContainer(containerName)

    val continuations = container
      .getFeedRanges
      .block()
      .asScala
      .map(feedRange => {
        val pkRangeFeedRange = feedRange.asInstanceOf[FeedRangePartitionKeyRangeImpl]
        val pkRangeId = pkRangeFeedRange.getPartitionKeyRangeId.toInt

        val lsn: Long = tokens.applyOrElse(pkRangeId, (_: Int) => {
          this.logInfo(
            s"No previous LSN available for PK RangeId $pkRangeId in container $containerName " +
            s"($containerResourceId) of database $databaseName ($databaseResourceId) - starting from beginning ")

          0L
        })

        new CompositeContinuationToken(
          "\"" + lsn + "\"",
          toCosmosRange(SparkBridgeInternal.getNormalizedEffectiveRange(container, pkRangeFeedRange)))
      }).toList

    val feedRangeContinuation: FeedRangeContinuation = FeedRangeContinuation
      .create(
        containerResourceId,
        FeedRangeEpkImpl.forFullRange,
        continuations.asJava
      )

    val changeFeedState: ChangeFeedState = new ChangeFeedStateV1(
      containerResourceId,
      FeedRangeEpkImpl.forFullRange,
      ChangeFeedMode.INCREMENTAL,
      ChangeFeedStartFromInternal.createFromLegacyContinuation(),
      feedRangeContinuation
    )

    new ChangeFeedOffset(
      changeFeedState.toString,
      Option.empty
    ).json()
  }
}
