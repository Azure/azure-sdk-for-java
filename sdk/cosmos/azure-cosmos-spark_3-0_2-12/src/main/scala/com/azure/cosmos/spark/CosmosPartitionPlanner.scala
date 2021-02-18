// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.SparkBridgeInternal
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal, Strings}
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver, requireNotNull, requireNotNullOrEmpty}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.read.InputPartition
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scala.publisher.SMono.PimpJMono
import reactor.core.scheduler.Schedulers

import java.time.Instant
import java.util
import java.util.{Timer, TimerTask}
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import scala.collection.concurrent.TrieMap

private object CosmosPartitionPlanner {

  private object ParameterNames {
    val CosmosClientConfig = "cosmosClientConfig"
    val CosmosContainerConfig = "cosmosContainerConfig"
    val CosmosPartitioningConfig = "cosmosPartitioningConfig"
    val FeedRange = "feedRange"
  }

  def createInputPartitions
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    cosmosPartitioningConfig: CosmosPartitioningConfig,
    changeFeedOffset: Option[ChangeFeedOffset],
    defaultMinimalPartitionCount : Int,
    defaultMaxPartitionSizeInMB: Int
  ) : Array[InputPartition] = {

    assertOnSparkDriver()
    requireNotNull(cosmosClientConfig, ParameterNames.CosmosClientConfig)
    requireNotNull(cosmosContainerConfig, ParameterNames.CosmosContainerConfig)
    requireNotNull(cosmosPartitioningConfig, ParameterNames.CosmosPartitioningConfig)

    cosmosPartitioningConfig.partitioningStrategy match {
      case PartitioningStrategies.Restrictive => applyRestrictiveStrategy(
        cosmosClientConfig,
        cosmosClientStateHandle,
        cosmosContainerConfig
      )
      case PartitioningStrategies.Custom => applyCustomStrategy(
        cosmosClientConfig,
        cosmosClientStateHandle,
        cosmosContainerConfig,
        changeFeedOffset,
        cosmosPartitioningConfig.targetedPartitionCount.get)
      case PartitioningStrategies.Default =>  applyStorageAlignedStrategy(
        cosmosClientConfig,
        cosmosClientStateHandle,
        cosmosContainerConfig,
        changeFeedOffset,
        1 / defaultMaxPartitionSizeInMB,
        defaultMinimalPartitionCount
      )
      case PartitioningStrategies.Aggressive =>  applyStorageAlignedStrategy(
        cosmosClientConfig,
        cosmosClientStateHandle,
        cosmosContainerConfig,
        changeFeedOffset,
        3 / defaultMaxPartitionSizeInMB,
        defaultMinimalPartitionCount
      )
    }
  }

  private[this] def applyRestrictiveStrategy
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig
  ): Array[InputPartition] = {
    val feedRangesList = this
      .getFeedRanges(cosmosClientConfig, cosmosClientStateHandle, cosmosContainerConfig)
      .block()

    val feedRanges = new Array[FeedRange](feedRangesList.size())
    feedRangesList.toArray(feedRanges)

    feedRanges
      .map(feedRange => ChangeFeedInputPartition(feedRange.toString))
  }

  private[this] def applyStorageAlignedStrategy
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    planningInfo: Array[PartitionPlanningInfo],
    splitCountMultiplier: Double,
    defaultMinPartitionCount: Int
  ): Array[InputPartition] = {
    requireNotNullOrEmpty(planningInfo, "planningInfo")

    val totalScaleFactor = planningInfo.map(pi => pi.scaleFactor).sum
    val effectiveSplitCountMultiplier = splitCountMultiplier * math.min(
      1,
      defaultMinPartitionCount / (splitCountMultiplier * totalScaleFactor))
    val inputPartitions =
      new util.ArrayList[ChangeFeedInputPartition]((2 * totalScaleFactor * effectiveSplitCountMultiplier).toInt)
    val client = CosmosClientCache.apply(cosmosClientConfig, cosmosClientStateHandle)
    val container = client
      .getDatabase(cosmosContainerConfig.database)
      .getContainer(cosmosContainerConfig.container)

    planningInfo.foreach(info => {
      val numberOfSparkPartitions = math.min(
        Int.MaxValue,
        math.min(1, (info.scaleFactor * effectiveSplitCountMultiplier).round)).toInt
      SparkBridgeInternal
        .trySplitFeedRange(container, info.feedRange, numberOfSparkPartitions)
        .foreach(feedRange => inputPartitions.add(ChangeFeedInputPartition(feedRange)))
    })

    val returnValue = new Array[InputPartition](inputPartitions.size())
    inputPartitions.toArray(returnValue)
    returnValue
  }

  private[this] def applyStorageAlignedStrategy
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    changeFeedOffset: Option[ChangeFeedOffset],
    weightFactor: Double,
    defaultMinPartitionCount: Int
  ): Array[InputPartition] = {
    val planningInfo = this.getPartitionPlanningInfo(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig,
      changeFeedOffset
    )

    applyStorageAlignedStrategy(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig,
      planningInfo,
      weightFactor,
      defaultMinPartitionCount
    )
  }

  private[this] def applyCustomStrategy
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    changeFeedOffset: Option[ChangeFeedOffset],
    targetPartitionCount: Int
  ): Array[InputPartition] = {
    val planningInfo = this.getPartitionPlanningInfo(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig,
      changeFeedOffset
    )

    val customPartitioningFactor = planningInfo.map(pi => pi.scaleFactor).sum/targetPartitionCount
    applyStorageAlignedStrategy(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig,
      planningInfo,
      customPartitioningFactor,
      targetPartitionCount
    )
  }

  private[this] def getPartitionPlanningInfo
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    changeFeedOffset: Option[ChangeFeedOffset]
  ) : Array[PartitionPlanningInfo] = {

    requireNotNull(cosmosClientConfig, ParameterNames.CosmosClientConfig)
    requireNotNull(cosmosContainerConfig, ParameterNames.CosmosContainerConfig)

    val partitionMetadata = getPartitionMetadata(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig
    )

    assertNotNullOrEmpty(partitionMetadata, "partitionMetadata")

    val partitionPlanningInfo = new Array[PartitionPlanningInfo](partitionMetadata.length)
    var index = 0
    partitionMetadata.foreach(m => {
      // rounded up to the next size
      val storageSizeInMB: Double = m.totalDocumentSizeInKB / 1024
      val progressWeightFactor: Double = getChangeFeedProgressFactor(changeFeedOffset, storageSizeInMB, m.latestLsn)

      // Round up scale factor
      val scaleFactor = if (storageSizeInMB == 0) {
        1
      } else {
        progressWeightFactor * storageSizeInMB.toDouble
      }

      val planningInfo = PartitionPlanningInfo(
        m.feedRange,
        storageSizeInMB,
        progressWeightFactor,
        scaleFactor
      )

      partitionPlanningInfo(index) = planningInfo
      index += 1
    })

    partitionPlanningInfo
  }

  private[this] def getChangeFeedProgressFactor(changeFeedOffset: Option[ChangeFeedOffset],
                                                storageSizeInMB: Double,
                                                latestLsn: Long) : Double = {
    changeFeedOffset match {
      case None => 1
      case Some(offset) =>
        val lsnFromOffset = SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(offset.changeFeedState)

        if (lsnFromOffset <= 0 || storageSizeInMB == 0) {
          // No progress has been made so far - use one Spark partition per GB
          1
        } else if (latestLsn <= lsnFromOffset) {
          // If progress has caught up with estimation already make sure we only use one Spark partition
          // for the physical partition in Cosmos
          1 / storageSizeInMB
        } else {
          // Use weight factor based on progress. This estimate assumes equal distribution of storage
          // size per LSN - which is a "good enough" simplification
          (latestLsn - lsnFromOffset) / latestLsn
        }
    }
  }

  private[this] def getFeedRanges
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig
  ): SMono[util.List[FeedRange]] = {

    requireNotNull(cosmosClientConfig, ParameterNames.CosmosClientConfig)
    requireNotNull(cosmosContainerConfig, ParameterNames.CosmosContainerConfig)
    val client = CosmosClientCache.apply(cosmosClientConfig, cosmosClientStateHandle)

    val container = client
      .getDatabase(cosmosContainerConfig.database)
      .getContainer(cosmosContainerConfig.container)

    container
      .getFeedRanges
      .asScala
  }

  private[this] def getPartitionMetadata
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig
  ) : Array[PartitionMetadata] = {

    this.getFeedRanges(cosmosClientConfig, cosmosClientStateHandle, cosmosContainerConfig)
      .flatMap(feedRanges => {
        SFlux
          .fromArray(feedRanges.toArray())
          .flatMap(f => PartitionMetadataCache.apply(
            cosmosClientConfig,
            cosmosClientStateHandle,
            cosmosContainerConfig,
            f.toString
          ))
          .collectSeq()
      })
      .block()
      .toArray
  }

  // Intentionally keeping this private to the CosmosPartitionPlanner
  // The partition metadata here is used purely for an best effort
  // estimation of number of Spark partitions needed for a certain
  // physical partition - it is not guaranteeing functional correctness
  // because the cached metadata could be old or even be for a different
  // container after deletion and recreation of a container
  private object PartitionMetadataCache extends CosmosLoggingTrait {
    private[this] val Nothing = 0
    private[this] val cache = new TrieMap[String, PartitionMetadata]

    // purpose of the time is to update partition metadata
    // additional throughput when more RUs are getting provisioned
    private val timerName = "partition-metadata-refresh-timer"
    private val timer: Timer = new Timer(timerName, true)
    private val refreshIntervalInMs : Long = 1 * 1000 // refresh cache every minute after initialization

    // update cached items which haven't been retrieved in the last refreshPeriod only if they
    // have been last updated longer than 15 minutes ago
    // any cached item which has been retrieved within the last refresh period will
    // automatically kept being updated
    private val staleCachedItemRefreshPeriodInMs : Long = 15 * 60 * 1000

    // purged cached items if they haven't been retrieved within 2 hours
    private val cachedItemTtlInMs : Long = 2 * 60 * 60 * 1000

    this.startRefreshTimer()

    def apply(cosmosClientConfig: CosmosClientConfiguration,
              cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
              cosmosContainerConfig: CosmosContainerConfig,
              feedRange: String): SMono[PartitionMetadata] = {

      assertOnSparkDriver()
      requireNotNull(cosmosClientConfig, ParameterNames.CosmosClientConfig)

      val key = PartitionMetadata.createKey(
        cosmosContainerConfig.database,
        cosmosContainerConfig.container,
        feedRange)

      cache.get(key) match {
        case Some(metadata) =>
          metadata.lastRetrieved.set(Instant.now.toEpochMilli)
          SMono.just(metadata)
        case None => this.create(
          cosmosClientConfig,
          cosmosClientStateHandle,
          cosmosContainerConfig,
          feedRange,
          key)
      }
    }

    def purge(cosmosContainerConfig: CosmosContainerConfig, feedRange: String): Unit = {
      assertOnSparkDriver()
      val key = PartitionMetadata.createKey(
        cosmosContainerConfig.database,
        cosmosContainerConfig.container,
        feedRange)

      cache.get(key) match {
        case None => Unit
        case Some(_) =>
          cache.remove(key)
      }
    }

    private[this] def create
    (
       cosmosClientConfiguration: CosmosClientConfiguration,
       cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
       cosmosContainerConfig: CosmosContainerConfig,
       feedRange: String,
       key: String
    ): SMono[PartitionMetadata] = {

      assertOnSparkDriver()
      cache.get(key) match {
        case Some(metadata) =>
          metadata.lastRetrieved.set(Instant.now.toEpochMilli)
          SMono.just(metadata)
        case None =>
          val metadataObservable = readPartitionMetadata(
            cosmosClientConfiguration: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
            cosmosContainerConfig: CosmosContainerConfig,
            feedRange: String
          )

          metadataObservable
            .map(metadata => {
              cache.putIfAbsent(key, metadata) match {
                case None =>
                  metadata
                case Some(metadataAddedConcurrently) =>
                  metadataAddedConcurrently.lastRetrieved.set(Instant.now.toEpochMilli)

                  metadataAddedConcurrently
              }
            })
            .subscribeOn(Schedulers.boundedElastic())
      }
    }

    private def readPartitionMetadata
    (
      cosmosClientConfiguration: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig,
      feedRange: String
    ): SMono[PartitionMetadata] = {
      val client = CosmosClientCache.apply(cosmosClientConfiguration, cosmosClientStateHandle)
      val container = client
        .getDatabase(cosmosContainerConfig.database)
        .getContainer(cosmosContainerConfig.container)

      val options = CosmosChangeFeedRequestOptions.createForProcessingFromNow(FeedRange.fromString(feedRange))
      options.setMaxItemCount(1)
      options.setMaxPrefetchPageCount(1)
      options.setQuotaInfoEnabled(true)

      val lastDocumentCount = new AtomicLong()
      val lastTotalDocumentSize = new AtomicLong()
      val lastContinuationToken = new AtomicReference[String]()

      container
        .queryChangeFeed(options, classOf[ObjectNode])
        .handle(r => {
          lastDocumentCount.set(r.getDocumentCountUsage)
          lastTotalDocumentSize.set(r.getDocumentUsage)
          val continuation = r.getContinuationToken
          if (!Strings.isNullOrWhiteSpace(continuation)) {
            lastContinuationToken.set(continuation)
          }
        })
        .collectList()
        .asScala
        .map(_ => {
          PartitionMetadata.create(
            cosmosClientConfiguration,
            cosmosClientStateHandle,
            cosmosContainerConfig,
            feedRange,
            assertNotNull(lastDocumentCount.get, "lastDocumentCount"),
            assertNotNull(lastTotalDocumentSize.get, "lastTotalDocumentSize"),
            assertNotNullOrEmpty(lastContinuationToken.get, "continuationToken")
          )
        })
    }

    private def startRefreshTimer() : Unit = {
      logInfo(s"$timerName: scheduling timer - delay: $refreshIntervalInMs ms, period: $refreshIntervalInMs ms")
      timer.schedule(
        new TimerTask { def run(): Unit = onRunRefreshTimer() },
        refreshIntervalInMs,
        refreshIntervalInMs)
    }

    private def onRunRefreshTimer() : Unit = {
      logTrace(s"--> $timerName: onRunRefreshTimer")
      val snapshot = cache.readOnlySnapshot()
      val updateObservables = snapshot.map(metadataSnapshot => updateIfNecessary(metadataSnapshot._2))
      SMono
        .zipDelayError(updateObservables, _ => 0)
        .onErrorResume(t => {
          logWarning("An error happened when updating partition metadata", t)
          SMono.just(Nothing)
        })
        .block()
      logTrace(s"<-- $timerName: onRunRefreshTimer")
    }

    private def updateIfNecessary(metadataSnapshot: PartitionMetadata):SMono[Int] = {
      val nowEpochMs = Instant.now.toEpochMilli
      val hotThreshold = nowEpochMs - refreshIntervalInMs
      val staleThreshold = nowEpochMs - staleCachedItemRefreshPeriodInMs
      val ttlThreshold = nowEpochMs - cachedItemTtlInMs

      val lastRetrievedSnapshot = metadataSnapshot.lastRetrieved.get()
      if (lastRetrievedSnapshot < ttlThreshold) {
        this.purge(metadataSnapshot.cosmosContainerConfig, metadataSnapshot.feedRange)
        SMono.just(Nothing)
      } else if (lastRetrievedSnapshot < staleThreshold || lastRetrievedSnapshot > hotThreshold) {
        readPartitionMetadata(
          metadataSnapshot.cosmosClientConfig,
          metadataSnapshot.cosmosClientStateHandle,
          metadataSnapshot.cosmosContainerConfig,
          metadataSnapshot.feedRange
        ).map(metadata => {
          val key = PartitionMetadata.createKey(
            metadataSnapshot.cosmosContainerConfig.database,
            metadataSnapshot.cosmosContainerConfig.container,
            metadataSnapshot.feedRange
          )
          if (cache.replace(key, metadataSnapshot, metadata)) {
            logTrace(s"Updated partition metadata '$key'")
          } else {
            logWarning(s"Ignored retrieved metadata due to concurrent update of partition metadata '$key'")
          }

          Nothing
        })
      } else {
        SMono.just(Nothing)
      }
    }
  }

  private object PartitionMetadata {
    def createKey(
                   databaseId: String,
                   containerId: String,
                   feedRange: String) : String = s"$databaseId|$containerId|$feedRange"

    def create(cosmosClientConfig: CosmosClientConfiguration,
               cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
               cosmosContainerConfig: CosmosContainerConfig,
               feedRange: String,
               documentCount: Long,
               totalDocumentSize: Long,
               continuationToken: String): PartitionMetadata = {

      val nowEpochMs = Instant.now().toEpochMilli

      PartitionMetadata(
        cosmosClientConfig,
        cosmosClientStateHandle,
        cosmosContainerConfig,
        feedRange,
        documentCount,
        totalDocumentSize,
        SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(continuationToken),
        new AtomicLong(nowEpochMs),
        new AtomicLong(nowEpochMs))
    }
  }

  private case class PartitionMetadata
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    feedRange: String,
    documentCount: Long,
    totalDocumentSizeInKB: Long,
    latestLsn: Long,
    lastRetrieved: AtomicLong,
    lastUpdated: AtomicLong
  ) {
    requireNotNullOrEmpty(feedRange, ParameterNames.FeedRange)
    requireNotNull(cosmosClientConfig, ParameterNames.CosmosClientConfig)
    requireNotNull(cosmosContainerConfig, ParameterNames.CosmosContainerConfig)
    requireNotNull(documentCount, "documentCount")
    requireNotNull(totalDocumentSizeInKB, "totalDocumentSizeInKB")
    requireNotNull(latestLsn, "latestLsn")
  }

  private case class PartitionPlanningInfo
  (
    feedRange: String,
    storageSizeInMB: Double,
    progressWeightFactor: Double,
    scaleFactor: Double
  ) {
    requireNotNullOrEmpty(feedRange, ParameterNames.FeedRange)
    requireNotNull(storageSizeInMB, "storageSizeInMB")
    requireNotNull(scaleFactor, "scaleFactor")
  }
}
