// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation

import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientBuilderHelper
import com.azure.cosmos.implementation.changefeed.common.{ChangeFeedMode, ChangeFeedStartFromInternal, ChangeFeedState, ChangeFeedStateV1}
import com.azure.cosmos.implementation.guava25.base.MoreObjects.firstNonNull
import com.azure.cosmos.implementation.guava25.base.Strings.emptyToNull
import com.azure.cosmos.implementation.query.CompositeContinuationToken
import com.azure.cosmos.implementation.routing.Range
import com.azure.cosmos.models.{FeedRange, PartitionKey, PartitionKeyBuilder, PartitionKeyDefinition, SparkModelBridgeInternal}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.{ChangeFeedOffset, CosmosConstants, NormalizedRange}
import com.azure.cosmos.{CosmosAsyncClient, CosmosClientBuilder, DirectConnectionConfig, SparkBridgeInternal}
import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.mutable

// scalastyle:off underscore.import
import com.azure.cosmos.implementation.feedranges._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[cosmos] object SparkBridgeImplementationInternal extends BasicLoggingTrait {
  private val SPARK_MAX_CONNECTIONS_PER_ENDPOINT_PROPERTY = "COSMOS.SPARK_MAX_CONNECTIONS_PER_ENDPOINT"
  private val SPARK_MAX_CONNECTIONS_PER_ENDPOINT_VARIABLE = "COSMOS_SPARK_MAX_CONNECTIONS_PER_ENDPOINT"
  private val DEFAULT_SPARK_MAX_CONNECTIONS_PER_ENDPOINT: Int = DirectConnectionConfig.getDefaultConfig.getMaxConnectionsPerEndpoint

  private val SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE_PROPERTY = "COSMOS.SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE"
  private val SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE_VARIABLE = "COSMOS_SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE"
  private val DEFAULT_SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE: Int = CosmosConstants.defaultIoThreadCountFactorPerCore

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

  def extractCollectionRid(continuation: String): String = {
    val state = ChangeFeedState.fromString(continuation)
    state.getContainerRid
  }

  def validateCollectionRidOfChangeFeedState
  (
    continuation: String,
    expectedCollectionRid: String,
    ignoreOffsetWhenInvalid: Boolean
  ): Boolean = {
    val extractedRid = extractCollectionRid(continuation)
    val isOffsetValid = extractedRid.equalsIgnoreCase(expectedCollectionRid)
    if (!isOffsetValid) {
      val message = s"The provided change feed continuation state is for a different container. Offset's " +
        s"container: ${extractedRid}, Current container: $expectedCollectionRid, " +
        s"Continuation: $continuation"

      if (!ignoreOffsetWhenInvalid) {
        throw new IllegalStateException(message)
      }

      logWarning(message)
    }

    isOffsetValid
  }

  def validateCollectionRidOfChangeFeedStates
  (
    continuationLeft: String,
    continuationRight: String,
  ): Boolean = {
    val extractedRidLeft = extractCollectionRid(continuationLeft)
    val extractedRidRight = extractCollectionRid(continuationRight)
    extractedRidLeft.equalsIgnoreCase(extractedRidRight)
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
    val pkDefinition = SparkModelBridgeInternal.createPartitionKeyDefinitionFromJson(partitionKeyDefinitionJson)
    partitionKeyToNormalizedRange(new PartitionKey(partitionKeyValue), pkDefinition)
  }

  private[cosmos] def partitionKeyToNormalizedRange(
                                                    partitionKey: PartitionKey,
                                                    partitionKeyDefinitionJson: PartitionKeyDefinition): NormalizedRange = {
      val feedRange = FeedRange.forLogicalPartition(partitionKey).asInstanceOf[FeedRangePartitionKeyImpl]
      val effectiveRange = feedRange.getEffectiveRange(partitionKeyDefinitionJson)
      rangeToNormalizedRange(effectiveRange)
  }

  private[cosmos] def hierarchicalPartitionKeyValuesToNormalizedRange
  (
      partitionKeyValueJsonArray: Object,
      partitionKeyDefinitionJson: String
  ): NormalizedRange = {

      val partitionKey = new PartitionKeyBuilder()
      val objectMapper = new ObjectMapper()
      val json = partitionKeyValueJsonArray.toString
      try {
          val partitionKeyValues = objectMapper.readValue(json, classOf[Array[String]])
          for (value <- partitionKeyValues) {
              partitionKey.add(value.trim)
          }
          partitionKey.build()
      } catch {
          case e: Exception =>
              logInfo("Invalid partition key paths: " + json, e)
      }

      val feedRange = FeedRange
          .forLogicalPartition(partitionKey.build())
          .asInstanceOf[FeedRangePartitionKeyImpl]

      val pkDefinition = SparkModelBridgeInternal.createPartitionKeyDefinitionFromJson(partitionKeyDefinitionJson)
      val effectiveRange = feedRange.getEffectiveRange(pkDefinition)
      rangeToNormalizedRange(effectiveRange)
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

    val pkRanges = SparkBridgeInternal
      .getPartitionKeyRanges(container)

    val pkRangesByPkRangeId = mutable.Map[Int, PartitionKeyRange]()
    val pkRangesByParentPkRangeId = mutable.Map[Int, List[PartitionKeyRange]]()

    pkRanges
      .foreach(pkRange => {
        pkRangesByPkRangeId.put(pkRange.getId.toInt, pkRange)
        if (pkRange.getParents != null && pkRange.getParents.size > 0) {
          pkRange
            .getParents
            .forEach(parentId => {
              if (pkRangesByParentPkRangeId.contains(parentId.toInt)) {
                val existingChildren = pkRangesByParentPkRangeId.get(parentId.toInt).get
                val newChildren = existingChildren :+ pkRange
                pkRangesByParentPkRangeId.put(parentId.toInt, newChildren)
              } else {
                pkRangesByParentPkRangeId.put(parentId.toInt, List(pkRange))
              }
            })
        }
      })

    val continuations = tokens
      .map(token => {
        val pkRangeId = token._1

        val lsn: Long = token._2

        val range: Range[String] = if (pkRangesByPkRangeId.contains(pkRangeId)) {
          pkRangesByPkRangeId.get(pkRangeId).get.toRange
        } else if (pkRangesByParentPkRangeId.contains(pkRangeId)) {
          val normalizedChildRanges = pkRangesByParentPkRangeId
            .get(pkRangeId)
            .get
            .map(childPkRange => rangeToNormalizedRange(childPkRange.toRange))
            .sorted

          toCosmosRange(new NormalizedRange(
                      normalizedChildRanges.head.min,
                      normalizedChildRanges.last.max))
        } else {
          throw new IllegalStateException(
            s"Can't resolve PKRangeId $pkRangeId - it is possible that this partition has been split multiple times. " +
              "In this case the change feed continuation can not be migrated - a new change feed needs to be " +
              "started instead."
          )
        }

        new CompositeContinuationToken(
          "\"" + lsn + "\"",
          range)
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

    s"v1\n" +
    new ChangeFeedOffset(
      changeFeedState.toString,
      None
    ).json()
  }

  private def getMaxConnectionsPerEndpointOverride: Int = {
    val maxConnectionsPerEndpointText = System.getProperty(
      SPARK_MAX_CONNECTIONS_PER_ENDPOINT_PROPERTY,
      firstNonNull(
        emptyToNull(System.getenv.get(SPARK_MAX_CONNECTIONS_PER_ENDPOINT_VARIABLE)),
        String.valueOf(DEFAULT_SPARK_MAX_CONNECTIONS_PER_ENDPOINT)))

    try {
      maxConnectionsPerEndpointText.toInt
    }
    catch {
      case e: Exception =>
        logError(s"Parsing spark max connections per endpoint failed. Using the default $DEFAULT_SPARK_MAX_CONNECTIONS_PER_ENDPOINT.", e)
        DEFAULT_SPARK_MAX_CONNECTIONS_PER_ENDPOINT
    }
  }

  def getIoThreadCountPerCoreOverride: Int = {
    val ioThreadCountPerCoreText = System.getProperty(
      SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE_PROPERTY,
      firstNonNull(
        emptyToNull(System.getenv.get(SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE_VARIABLE)),
        String.valueOf(DEFAULT_SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE)))

    try {
      ioThreadCountPerCoreText.toInt
    }
    catch {
      case e: Exception =>
        logError(s"Parsing spark I/O thread-count per core failed. Using the default $DEFAULT_SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE.", e)
        DEFAULT_SPARK_IO_THREAD_COUNT_FACTOR_PER_CORE
    }
  }


  def configureSimpleObjectMapper(allowDuplicateProperties: Boolean) : Unit = {
    Utils.configureSimpleObjectMapper(allowDuplicateProperties)
  }

  def overrideDefaultTcpOptionsForSparkUsage(): Unit = {
    val overrideJson = "{\"timeoutDetectionEnabled\": true, \"timeoutDetectionDisableCPUThreshold\": 75.0," +
      "\"timeoutDetectionTimeLimit\": \"PT90S\", \"timeoutDetectionHighFrequencyThreshold\": 10," +
      "\"timeoutDetectionHighFrequencyTimeLimit\": \"PT30S\", \"timeoutDetectionOnWriteThreshold\": 10," +
      "\"timeoutDetectionOnWriteTimeLimit\": \"PT90s\", \"tcpNetworkRequestTimeout\": \"PT7S\", " +
      "\"connectTimeout\": \"PT10S\", \"maxChannelsPerEndpoint\": \"" +
      s"$getMaxConnectionsPerEndpointOverride" +
      "\"}"

    if (System.getProperty("reactor.netty.tcp.sslHandshakeTimeout") == null) {
      System.setProperty("reactor.netty.tcp.sslHandshakeTimeout", "20000")
    }

    if (System.getProperty(Configs.HTTP_MAX_REQUEST_TIMEOUT) == null) {
      System.setProperty(
        Configs.HTTP_MAX_REQUEST_TIMEOUT,
        "70")
    }

    if (System.getProperty(Configs.HTTP_DEFAULT_CONNECTION_POOL_SIZE) == null) {
      System.setProperty(
        Configs.HTTP_DEFAULT_CONNECTION_POOL_SIZE,
        "25000")
    }

    if (System.getProperty("azure.cosmos.directTcp.defaultOptions") == null) {
      System.setProperty("azure.cosmos.directTcp.defaultOptions", overrideJson)
    }
  }
}
