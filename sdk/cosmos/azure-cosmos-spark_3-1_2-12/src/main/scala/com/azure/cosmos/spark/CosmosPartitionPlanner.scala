// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal.extractContinuationTokensFromChangeFeedStateJson
import com.azure.cosmos.{CosmosAsyncContainer, SparkBridgeInternal}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal, Strings}
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver, requireNotNull}
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.LsnAttributeName
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.streaming.{ReadAllAvailable, ReadLimit, ReadMaxFiles, ReadMaxRows}
import reactor.core.scala.publisher.SFlux
import reactor.core.scala.publisher.SMono.PimpJMono

import java.time.Duration
import java.util
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}
import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private object CosmosPartitionPlanner extends BasicLoggingTrait {
  val DefaultPartitionSizeInMB: Int = 5 * 1024 // 10 GB

  def createInputPartitions
  (
    cosmosPartitioningConfig: CosmosPartitioningConfig,
    container: CosmosAsyncContainer,
    partitionMetadata: Array[PartitionMetadata],
    defaultMinimalPartitionCount: Int,
    defaultMaxPartitionSizeInMB: Int,
    readLimit: ReadLimit
  ): Array[CosmosInputPartition] = {
    assertOnSparkDriver()
    //scalastyle:off multiple.string.literals
    requireNotNull(cosmosPartitioningConfig, "cosmosPartitioningConfig")
    require(defaultMaxPartitionSizeInMB >= 64, "Argument 'defaultMaxPartitionSizeInMB' must at least be 64")
    require(defaultMinimalPartitionCount >= 1, "Argument 'defaultMinimalPartitionCount' must at least be 1")
    //scalastyle:on multiple.string.literals

    val planningInfo = this.getPartitionPlanningInfo(partitionMetadata, readLimit)

    cosmosPartitioningConfig.partitioningStrategy match {
      case PartitioningStrategies.Restrictive =>
        applyRestrictiveStrategy(planningInfo)
      case PartitioningStrategies.Custom =>
        applyCustomStrategy(
          container,
          planningInfo,
          cosmosPartitioningConfig.targetedPartitionCount.get)
      case PartitioningStrategies.Default =>
        applyStorageAlignedStrategy(
          container,
          planningInfo,
          1 / defaultMaxPartitionSizeInMB.toDouble,
          defaultMinimalPartitionCount
        )
      case PartitioningStrategies.Aggressive =>
        applyStorageAlignedStrategy(
          container,
          planningInfo,
          5 / defaultMaxPartitionSizeInMB.toDouble,
          defaultMinimalPartitionCount
        )
    }
  }

  private[this] def getContinuationTokenLsnOfFirstItem(items: Iterable[ObjectNode]): Option[String] = {
    items
      .collectFirst({
        case item: ObjectNode if item != null =>
          val lsnNode = item.get(LsnAttributeName)
          if (lsnNode != null && lsnNode.isNumber) {
            // when grabbing the LSN from the item we need to use the item's LSN -1
            // to ensure we would retrieve this item again
            Some(
              SparkBridgeImplementationInternal.toContinuationToken(lsnNode.asLong() - 1))
          } else {
            None
          }
      })
      .flatten
  }

  // scalastyle:off method.length
  def createInitialOffset
  (
    container: CosmosAsyncContainer,
    changeFeedConfig: CosmosChangeFeedConfig,
    streamId: Option[String]
  ): String = {

    assertOnSparkDriver()
    val lastContinuationTokens: ConcurrentMap[FeedRange, String] = new ConcurrentHashMap[FeedRange, String]()
    container
      .getFeedRanges
      .asScala
      .flatMapMany(feedRanges => SFlux.fromIterable(feedRanges.asScala))
      .flatMap(feedRange => {
        val requestOptions = changeFeedConfig.toRequestOptions(feedRange)
        requestOptions.setMaxItemCount(1)
        requestOptions.setMaxPrefetchPageCount(1)
        requestOptions.setQuotaInfoEnabled(true)

        container
          .queryChangeFeed(requestOptions, classOf[ObjectNode])
          .handle(r => {
            val lsnFromItems = getContinuationTokenLsnOfFirstItem(r.getElements.asScala)
            val continuation = if (lsnFromItems.isDefined) {
              SparkBridgeImplementationInternal
                .overrideLsnInChangeFeedContinuation(r.getContinuationToken,lsnFromItems.get)
            } else {
              r.getContinuationToken
            }

            if (!Strings.isNullOrWhiteSpace(continuation)) {
              if (lastContinuationTokens.putIfAbsent(feedRange, continuation) == null && isDebugLogEnabled) {
                val stateJson = SparkBridgeImplementationInternal.changeFeedContinuationToJson(continuation)
                val range = SparkBridgeImplementationInternal.toNormalizedRange(feedRange)
                logDebug(s"FeedRange '${range.min}-${range.max}': Set effective continuation '$stateJson")
              }
            }
          })
          .take(1)
          .collectList()
          .asScala
      })
      .asJava()
      .collectList()
      .block()

    val offsetJsonBase64 = SparkBridgeImplementationInternal
      .mergeChangeFeedContinuations(lastContinuationTokens.values().asScala)

    if (isDebugLogEnabled) {
      val offsetJson = SparkBridgeImplementationInternal.changeFeedContinuationToJson(offsetJsonBase64)
      // scala style rule flaky - even complaining on partial log messages
      // scalastyle:off multiple.string.literals
      logDebug(s"Initial offset of stream ${streamId.getOrElse("null")}: '$offsetJson'.")
      // scalastyle:on multiple.string.literals
    }
    offsetJsonBase64
  }
  // scalastyle:on method.length

  // scalastyle:off method.length
  // scalastyle:off parameter.number
  // Based on a start offset, calculate which is the next end offset
  def getLatestOffset
  (
    userConfig: Map[String, String],
    startOffset: ChangeFeedOffset,
    readLimit: ReadLimit,
    maxStaleness: Duration,
    clientConfiguration: CosmosClientConfiguration,
    cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
    containerConfig: CosmosContainerConfig,
    partitioningConfig: CosmosPartitioningConfig,
    defaultParallelism: Int,
    container: CosmosAsyncContainer
  ): ChangeFeedOffset = {
    assertOnSparkDriver()
    assertNotNull(startOffset, "startOffset")

    val latestPartitionMetadata = CosmosPartitionPlanner.getPartitionMetadata(
      userConfig,
      clientConfiguration,
      Some(cosmosClientStateHandle),
      containerConfig,
      Some(maxStaleness)
    )

    val defaultMaxPartitionSizeInMB = DefaultPartitionSizeInMB
    val defaultMinPartitionCount = 1 + (2 * defaultParallelism)
    val orderedMetadataWithStartLsn = this.getOrderedPartitionMetadataWithStartLsn(
      startOffset.changeFeedState,
      latestPartitionMetadata)

    val inputPartitions: Array[CosmosInputPartition] = CosmosPartitionPlanner.createInputPartitions(
      partitioningConfig,
      container,
      orderedMetadataWithStartLsn,
      defaultMinPartitionCount,
      defaultMaxPartitionSizeInMB,
      readLimit
    )

    // If the end lsn has been populated by customization on createInputPartitions or present in the metadata cache
    val orderedFeedRangeWithEndLsn =
      orderedMetadataWithStartLsn.map(m =>
        (m.feedRange,
          inputPartitions.find(ip => ip.feedRange == m.feedRange).get.endLsn.getOrElse(m.endLsn.getOrElse(m.latestLsn))))

    if (isDebugLogEnabled) {
      val endOffsetDebug = new StringBuilder("EndOffSet using EndLsn: ")
      for (range <- orderedFeedRangeWithEndLsn) {
        endOffsetDebug += s"${range._1.min}-${range._1.max}: ${range._2},"
      }

      logDebug(endOffsetDebug.toString)
    }

    val changeFeedStateJson = SparkBridgeImplementationInternal
      .createChangeFeedStateJson(
        startOffset.changeFeedState,
        orderedFeedRangeWithEndLsn.map(m => (m._1, m._2)))

    ChangeFeedOffset(changeFeedStateJson, Some(inputPartitions))
  }
  // scalastyle:on method.length
  // scalastyle:on parameter.number

  private[this] def getOrderedPartitionMetadataWithStartLsn
  (
    stateJson: String,
    latestPartitionMetadata: Array[PartitionMetadata]
  ): Array[PartitionMetadata] = {

    assert(!Strings.isNullOrWhiteSpace(stateJson), s"Argument 'stateJson' must not be null or empty.")
    val orderedStartTokens = extractContinuationTokensFromChangeFeedStateJson(stateJson)

    val orderedLatestTokens = latestPartitionMetadata
      .sortBy(metadata => metadata.feedRange.min)

    mergeStartAndLatestTokens(orderedStartTokens, orderedLatestTokens)
  }

  // scalastyle:off method.length
  private[this] def mergeStartAndLatestTokens
  (
    startTokens: Array[(NormalizedRange, Long)],
    latestTokens: Array[PartitionMetadata]
  ): Array[PartitionMetadata] = {

    val orderedBoundaries = mutable.SortedSet[String]()
    startTokens.foreach(token => {
      orderedBoundaries += token._1.min
      orderedBoundaries += token._1.max
    })
    latestTokens.foreach(metadata => {
      orderedBoundaries += metadata.feedRange.min
      orderedBoundaries += metadata.feedRange.max
    })

    var startTokensIndex = 0
    var latestTokensIndex = 0
    var orderedRangesIndex = 0
    val orderedRanges: Array[NormalizedRange] = new Array[NormalizedRange](orderedBoundaries.size - 1)

    orderedBoundaries
      .tail
      .foldLeft(None: Option[NormalizedRange])((previous: Option[NormalizedRange], current: String) => {
        val range = previous match {
          case Some(previousRange) =>
            NormalizedRange(previousRange.max, current)
          case None =>
            NormalizedRange(orderedBoundaries.head, current)
        }

        orderedRanges(orderedRangesIndex) = range
        orderedRangesIndex += 1
        Some(range)
      })

    orderedRanges
      .map(range => {
        while (!SparkBridgeImplementationInternal.doRangesOverlap(range, startTokens(startTokensIndex)._1)) {
          startTokensIndex += 1
          if (startTokensIndex >= startTokens.length) {
            throw new IllegalStateException(s"No overlapping start token found for range '$range'.")
          }
        }

        while (!SparkBridgeImplementationInternal.doRangesOverlap(range, latestTokens(latestTokensIndex).feedRange)) {
          latestTokensIndex += 1
          if (latestTokensIndex >= latestTokens.length) {
            throw new IllegalStateException(s"No overlapping latest token found for range '$range'.")
          }
        }

        val startLsn: Long = startTokens(startTokensIndex)._2
        latestTokens(latestTokensIndex).cloneForSubRange(range, startLsn)
      })
  }
  // scalastyle:on method.length

  private[this] def applyRestrictiveStrategy
  (
    partitionPlanningInfo: Array[PartitionPlanningInfo]
  ): Array[CosmosInputPartition] = {
    partitionPlanningInfo.map(info =>
      CosmosInputPartition(
        info.feedRange,
        info.endLsn))
  }

  private[this] def applyStorageAlignedStrategy(
      container: CosmosAsyncContainer,
      planningInfo: Array[PartitionPlanningInfo],
      splitCountMultiplier: Double,
      defaultMinPartitionCount: Int
  ): Array[CosmosInputPartition] = {
    assertNotNullOrEmpty(planningInfo, "planningInfo")

    val totalScaleFactor = planningInfo.map(pi => pi.scaleFactor).sum

    val effectiveSplitCountMultiplier =
      if (splitCountMultiplier == 0 || totalScaleFactor == 0) {
        1
      } else {
        splitCountMultiplier * math.max(
          1,
          defaultMinPartitionCount / (splitCountMultiplier * totalScaleFactor))
      }

    val inputPartitions =
      new util.ArrayList[CosmosInputPartition](
        (2 * totalScaleFactor * effectiveSplitCountMultiplier).toInt)

    planningInfo.foreach(info => {
      val numberOfSparkPartitions = math.min(
        Int.MaxValue,
        math.max(
          1,
          math.ceil(info.scaleFactor * effectiveSplitCountMultiplier).toInt))
      SparkBridgeInternal
        .trySplitFeedRange(container, info.feedRange, numberOfSparkPartitions)
        .foreach(feedRange =>
          inputPartitions.add(CosmosInputPartition(feedRange, info.endLsn)))
    })

    inputPartitions.asScala.toArray
  }

  private[this] def applyCustomStrategy(
      container: CosmosAsyncContainer,
      planningInfo: Array[PartitionPlanningInfo],
      targetPartitionCount: Int
  ): Array[CosmosInputPartition] = {
    val customPartitioningFactor = planningInfo
      .map(pi => pi.scaleFactor)
      .sum / targetPartitionCount
    applyStorageAlignedStrategy(
      container,
      planningInfo,
      customPartitioningFactor,
      targetPartitionCount
    )
  }

  def getPartitionPlanningInfo
  (
    partitionMetadata: Array[PartitionMetadata],
    readLimit: ReadLimit
  ): Array[PartitionPlanningInfo] = {

    assertOnSparkDriver()
    assertNotNullOrEmpty(partitionMetadata, "partitionMetadata")

    val partitionPlanningInfo =
      new Array[PartitionPlanningInfo](partitionMetadata.length)
    var index = 0

    calculateEndLsn(partitionMetadata, readLimit)
      .foreach(m => {
        val storageSizeInMB: Double = m.totalDocumentSizeInKB / 1024.toDouble
        val progressWeightFactor: Double = getChangeFeedProgressFactor(storageSizeInMB, m)

        val scaleFactor = if (storageSizeInMB == 0) {
          1
        } else {
          progressWeightFactor * storageSizeInMB.toDouble
        }

        val planningInfo = PartitionPlanningInfo(
          m.feedRange,
          storageSizeInMB,
          progressWeightFactor,
          scaleFactor,
          m.endLsn
        )

        partitionPlanningInfo(index) = planningInfo
        index += 1
      })

    partitionPlanningInfo
  }

  private[this] def calculateEndLsn
  (
    metadata: Array[PartitionMetadata],
    readLimit: ReadLimit
  ): Array[PartitionMetadata] = {

    val totalWeightedLsnGap = new AtomicLong(0)
    metadata.foreach(m => {
      totalWeightedLsnGap.addAndGet(m.getWeightedLsnGap)
    })

    metadata
      // Update endLsn - which depends on read limit
      .map(metadata => {
        val endLsn = readLimit match {
          case _: ReadAllAvailable => metadata.latestLsn
          case _: ReadMaxRows =>
            val gap = math.max(0, metadata.latestLsn - metadata.startLsn)
            val weightFactor = metadata.getWeightedLsnGap.toDouble / totalWeightedLsnGap.get
            val allowedRate = (weightFactor * gap).toLong
            if (isDebugLogEnabled) {
              val calculateDebugLine = s"calculateEndLsn - gap $gap weightFactor $weightFactor " +
                s"documentCount ${metadata.documentCount} latestLsn ${metadata.latestLsn} " +
                s"startLsn ${metadata.startLsn} allowedRate $allowedRate weightedGap ${metadata.getWeightedLsnGap}"
              logDebug(calculateDebugLine)
            }
            // if isDebugLogEnabled
            math.min(metadata.latestLsn, metadata.startLsn + allowedRate)
          case _: ReadMaxFiles => throw new IllegalStateException("ReadLimitMaxFiles not supported by this source.")
        }

        metadata.withEndLsn(endLsn)
      })
  }

  private[this] def getChangeFeedProgressFactor(
      storageSizeInMB: Double,
      metadata: PartitionMetadata): Double = {

    val effectiveEndLsn = metadata.endLsn.getOrElse(metadata.latestLsn)
    if (metadata.startLsn <= 0 || storageSizeInMB == 0) {
      // No progress has been made so far - use one Spark partition per GB
      1
    } else if (effectiveEndLsn <= metadata.startLsn) {
      // If progress has caught up with estimation already make sure we only use one Spark partition
      // for the physical partition in Cosmos
      1 / storageSizeInMB.toDouble
    } else {
      // Use weight factor based on progress. This estimate assumes equal distribution of storage
      // size per LSN - which is a "good enough" simplification
      (effectiveEndLsn - metadata.startLsn) / metadata.latestLsn.toDouble
    }
  }

  private[this] def getFeedRanges
  (
    userConfig: Map[String, String],
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig
  ) = {

  assertNotNull(cosmosClientConfig, "cosmosClientConfig")
    assertNotNull(cosmosContainerConfig, "cosmosContainerConfig")
    val client =
      CosmosClientCache.apply(cosmosClientConfig, cosmosClientStateHandle)

    val container = ThroughputControlHelper.getContainer(userConfig, cosmosContainerConfig, client)
    container.openConnectionsAndInitCaches().block()

    container
      .getFeedRanges
      .asScala
      .map(feedRanges => feedRanges
        .asScala
        .map(feedRange => SparkBridgeImplementationInternal.toNormalizedRange(feedRange))
        .toArray)
  }

  def getPartitionMetadata(
      userConfig: Map[String, String],
      cosmosClientConfig: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig,
      maxStaleness: Option[Duration] = None
  ): Array[PartitionMetadata] = {

    assertOnSparkDriver()
    this
      .getFeedRanges(
        userConfig,
        cosmosClientConfig,
        cosmosClientStateHandle,
        cosmosContainerConfig)
      .flatMap(feedRanges => {
        SFlux
          .fromArray(feedRanges)
          .flatMap(
            normalizedRange =>
              PartitionMetadataCache.apply(
                userConfig,
                cosmosClientConfig,
                cosmosClientStateHandle,
                cosmosContainerConfig,
                normalizedRange,
                maxStaleness
            ))
          .collectSeq()
      })
      .block()
      .toArray
  }
}
