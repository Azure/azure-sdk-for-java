// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.SparkBridgeInternal
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver, requireNotNull}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.read.InputPartition
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scala.publisher.SMono.PimpJMono

import java.util

private object CosmosPartitionPlanner {
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

    //scalastyle:off multiple.string.literals
    requireNotNull(cosmosClientConfig, "cosmosClientConfig")
    requireNotNull(cosmosContainerConfig, "cosmosContainerConfig")
    requireNotNull(cosmosPartitioningConfig, "cosmosPartitioningConfig")
    //scalastyle:on multiple.string.literals

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
    assertNotNullOrEmpty(planningInfo, "planningInfo")

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

    assertNotNull(cosmosClientConfig, "cosmosClientConfig")
    assertNotNull(cosmosContainerConfig, "cosmosContainerConfig")

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

    assertNotNull(cosmosClientConfig, "cosmosClientConfig")
    assertNotNull(cosmosContainerConfig, "cosmosContainerConfig")
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
}
