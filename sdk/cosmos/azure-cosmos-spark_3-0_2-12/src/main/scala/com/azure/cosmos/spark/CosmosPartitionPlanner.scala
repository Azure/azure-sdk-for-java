// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.SparkBridgeInternal
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, requireNotNull, requireNotNullOrEmpty}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scala.publisher.SMono.PimpJMono
import reactor.core.scheduler.Schedulers

import java.util
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import scala.collection.concurrent.TrieMap

private class CosmosPartitionPlanner {

  private object ParameterNames {
    val CosmosClientConfig = "cosmosClientConfig"
    val CosmosContainerConfig = "cosmosContainerConfig"
    val CosmosPartitioningConfig = "cosmosPartitioningConfig"
    val DatabaseId = "databaseId"
    val ContainerId = "containerId"
    val FeedRange = "feedRange"
  }

  def createInputPartitions
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    cosmosPartitioningConfig: CosmosPartitioningConfig,
    changeFeedOffset: Option[ChangeFeedOffset]
  ) : Array[ChangeFeedInputPartition] = {

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
        1
      )
      case PartitioningStrategies.Aggressive =>  applyStorageAlignedStrategy(
        cosmosClientConfig,
        cosmosClientStateHandle,
        cosmosContainerConfig,
        changeFeedOffset,
        3
      )
    }
  }

  private[this] def applyRestrictiveStrategy
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig
  ) = {
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
    splitCountMultiplier: Double
  ) = {
    requireNotNullOrEmpty(planningInfo, "planningInfo")

    val totalScaleFactor = planningInfo.map(pi => pi.scaleFactor).sum
    val inputPartitions =
      new util.ArrayList[ChangeFeedInputPartition]((totalScaleFactor * (splitCountMultiplier + 1)).toInt)

    val client = CosmosClientCache.apply(cosmosClientConfig, cosmosClientStateHandle)

    val container = client
      .getDatabase(cosmosContainerConfig.database)
      .getContainer(cosmosContainerConfig.container)

    planningInfo.foreach(info => {
      val numberOfSparkPartitions = math.min(
        Int.MaxValue,
        math.min(1, (info.scaleFactor * splitCountMultiplier).round)).toInt
      SparkBridgeInternal
        .trySplitFeedRange(container, info.feedRange, numberOfSparkPartitions)
        .foreach(feedRange => inputPartitions.add(ChangeFeedInputPartition(feedRange)))
    })

    val returnValue = new Array[ChangeFeedInputPartition](inputPartitions.size())
    inputPartitions.toArray(returnValue)
    returnValue
  }

  private[this] def applyStorageAlignedStrategy
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    changeFeedOffset: Option[ChangeFeedOffset],
    weightFactor: Double
  ) = {
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
      weightFactor
    )
  }

  private[this] def applyCustomStrategy
  (
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    changeFeedOffset: Option[ChangeFeedOffset],
    targetPartitionCount: Int
  ) = {
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
      customPartitioningFactor
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
      val storageSizeInGB: Double = m.totalDocumentSizeInKB / (1024 * 1024)
      val progressWeightFactor: Double = getChangeFeedProgressFactor(changeFeedOffset, storageSizeInGB, m.latestLsn)

      // Round up scale factor
      val scaleFactor = if (storageSizeInGB == 0) {
        1
      } else {
        progressWeightFactor * storageSizeInGB.toDouble
      }

      val planningInfo = PartitionPlanningInfo(
        m.feedRange,
        storageSizeInGB,
        progressWeightFactor,
        scaleFactor
      )

      partitionPlanningInfo(index) = planningInfo
      index += 1
    })

    partitionPlanningInfo
  }

  private[this] def getChangeFeedProgressFactor(changeFeedOffset: Option[ChangeFeedOffset],
                                                storageSizeInGB: Double,
                                                latestLsn: Long) : Double = {
    changeFeedOffset match {
      case None => 1
      case Some(offset) =>
        val lsnFromOffset = SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(offset.changeFeedState)

        if (lsnFromOffset <= 0 || storageSizeInGB == 0) {
          // No progress has been made so far - use one Spark partition per GB
          1
        } else if (latestLsn <= lsnFromOffset) {
          // If progress has caught up with estimation already make sure we only use one Spark partition
          // for the physical partition in Cosmos
          1 / storageSizeInGB
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
  private object PartitionMetadataCache {
    private[this] val cache = new TrieMap[String, SMono[PartitionMetadata]]

    def apply(cosmosClientConfig: CosmosClientConfiguration,
              cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
              cosmosContainerConfig: CosmosContainerConfig,
              feedRange: String): SMono[PartitionMetadata] = {

      requireNotNull(cosmosClientConfig, ParameterNames.CosmosClientConfig)

      val key = PartitionMetadata.createKey(
        cosmosContainerConfig.database,
        cosmosContainerConfig.container,
        feedRange)

      cache.get(key) match {
        case Some(metadata) => metadata
        case None => create(
          cosmosClientConfig,
          cosmosClientStateHandle,
          cosmosContainerConfig,
          feedRange,
          key)
      }
    }

    def purge(cosmosContainerConfig: CosmosContainerConfig, feedRange: String): Unit = {
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

    private[this] def create(cosmosClientConfiguration: CosmosClientConfiguration,
                             cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
                             cosmosContainerConfig: CosmosContainerConfig,
                             feedRange: String,
                             key: String) : SMono[PartitionMetadata] = {
      cache.get(key) match {
        case Some(metadata) => metadata
        case None =>
          val client = CosmosClientCache.apply(cosmosClientConfiguration, cosmosClientStateHandle)
          val container = client
            .getDatabase(cosmosContainerConfig.database)
            .getContainer(cosmosContainerConfig.container)

          val options = CosmosChangeFeedRequestOptions.createForProcessingFromNow(FeedRange.fromString(feedRange))
          options.setMaxItemCount(1)
          options.setMaxPrefetchPageCount(1)

          val lastDocumentCount = new AtomicLong()
          val lastTotalDocumentSize = new AtomicLong()
          val lastContinuationToken = new AtomicReference[String]()
          val results = container.queryChangeFeed(options,classOf[ObjectNode])

          results.handle(r => {
            lastDocumentCount.set(r.getDocumentCountUsage)
            lastTotalDocumentSize.set(r.getDocumentUsage)
            val continuation = r.getContinuationToken
            if (continuation != null && !continuation.isBlank) {
              lastContinuationToken.set(continuation)
            }
          })

          val metadataObservable = results
            .collectList()
            .asScala
            .`then`(
              SMono.just(
                PartitionMetadata.create(
                  cosmosContainerConfig,
                  feedRange,
                  assertNotNull(lastDocumentCount.get, "lastDocumentCount"),
                  assertNotNull(lastTotalDocumentSize.get, "lastTotalDocumentSize"),
                  assertNotNullOrEmpty(lastContinuationToken.get, "continuationToken"))))
          metadataObservable.subscribeOn(Schedulers.boundedElastic())
          cache.putIfAbsent(key, metadataObservable) match {
            case None =>
              metadataObservable
            case Some(metadataObservableAddedConcurrently) =>
              metadataObservableAddedConcurrently
          }
      }
    }
  }

  private object PartitionMetadata {
    def createKey(
                   databaseId: String,
                   containerId: String,
                   feedRange: String) : String = s"$databaseId|$containerId|$feedRange"

    def create(cosmosContainerConfig: CosmosContainerConfig,
               feedRange: String,
               documentCount: Long,
               totalDocumentSize: Long,
               continuationToken: String): PartitionMetadata = {

      PartitionMetadata(
        cosmosContainerConfig.database,
        cosmosContainerConfig.container,
        feedRange,
        documentCount,
        totalDocumentSize,
        SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(continuationToken))
    }
  }

  private case class PartitionMetadata
  (
    databaseId: String,
    containerId: String,
    feedRange: String,
    documentCount: Long,
    totalDocumentSizeInKB: Long,
    latestLsn: Long
  ) {
    requireNotNullOrEmpty(databaseId, ParameterNames.DatabaseId)
    requireNotNullOrEmpty(containerId, ParameterNames.ContainerId)
    requireNotNullOrEmpty(feedRange, ParameterNames.FeedRange)
    requireNotNull(documentCount, "documentCount")
    requireNotNull(totalDocumentSizeInKB, "totalDocumentSizeInKB")
    requireNotNull(latestLsn, "latestLsn")
  }

  private case class PartitionPlanningInfo
  (
    feedRange: String,
    storageSizeInGB: Double,
    progressWeightFactor: Double,
    scaleFactor: Double
  ) {
    requireNotNullOrEmpty(feedRange, ParameterNames.FeedRange)
    requireNotNull(storageSizeInGB, "storageSizeInGB")
    requireNotNull(scaleFactor, "scaleFactor")
  }
}
