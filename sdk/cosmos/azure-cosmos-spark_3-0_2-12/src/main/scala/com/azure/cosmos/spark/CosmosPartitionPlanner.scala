// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.SparkBridgeInternal
import com.azure.cosmos.implementation.{
  CosmosClientMetadataCachesSnapshot,
  SparkBridgeImplementationInternal
}
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.CosmosPredicates.{
  assertNotNull,
  assertNotNullOrEmpty,
  assertOnSparkDriver,
  requireNotNull
}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.read.InputPartition
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scala.publisher.SMono.PimpJMono

import java.util

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private object CosmosPartitionPlanner {
  def createInputPartitions(
      cosmosClientConfig: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig,
      cosmosPartitioningConfig: CosmosPartitioningConfig,
      changeFeedOffset: Option[ChangeFeedOffset],
      defaultMinimalPartitionCount: Int,
      defaultMaxPartitionSizeInMB: Int
  ): Array[InputPartition] = {
    assertOnSparkDriver()
    //scalastyle:off multiple.string.literals
    requireNotNull(cosmosClientConfig, "cosmosClientConfig")
    requireNotNull(cosmosContainerConfig, "cosmosContainerConfig")
    requireNotNull(cosmosPartitioningConfig, "cosmosPartitioningConfig")
    require(defaultMaxPartitionSizeInMB >= 64, "Argument 'defaultMaxPartitionSizeInMB' must at least be 64")
    require(defaultMinimalPartitionCount >= 1, "Argument 'defaultMinimalPartitionCount' must at least be 1")
    //scalastyle:on multiple.string.literals

    cosmosPartitioningConfig.partitioningStrategy match {
      case PartitioningStrategies.Restrictive =>
        applyRestrictiveStrategy(
          cosmosClientConfig,
          cosmosClientStateHandle,
          cosmosContainerConfig
        )
      case PartitioningStrategies.Custom =>
        applyCustomStrategy(cosmosClientConfig,
                            cosmosClientStateHandle,
                            cosmosContainerConfig,
                            changeFeedOffset,
                            cosmosPartitioningConfig.targetedPartitionCount.get)
      case PartitioningStrategies.Default =>
        applyStorageAlignedStrategy(
          cosmosClientConfig,
          cosmosClientStateHandle,
          cosmosContainerConfig,
          changeFeedOffset,
          1 / defaultMaxPartitionSizeInMB.toDouble,
          defaultMinimalPartitionCount
        )
      case PartitioningStrategies.Aggressive =>
        applyStorageAlignedStrategy(
          cosmosClientConfig,
          cosmosClientStateHandle,
          cosmosContainerConfig,
          changeFeedOffset,
          3 / defaultMaxPartitionSizeInMB.toDouble,
          defaultMinimalPartitionCount
        )
    }
  }

  private[this] def applyRestrictiveStrategy(
      cosmosClientConfig: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig
  ): Array[InputPartition] = {
    val feedRangesList = this
      .getFeedRanges(cosmosClientConfig,
                     cosmosClientStateHandle,
                     cosmosContainerConfig)
      .block()

    feedRangesList.asScala.map(feedRange => ChangeFeedInputPartition(feedRange.toString)).toArray
  }

  private[this] def applyStorageAlignedStrategy(
      cosmosClientConfig: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig,
      planningInfo: Array[PartitionPlanningInfo],
      splitCountMultiplier: Double,
      defaultMinPartitionCount: Int
  ): Array[InputPartition] = {
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
      new util.ArrayList[ChangeFeedInputPartition](
        (2 * totalScaleFactor * effectiveSplitCountMultiplier).toInt)
    val client =
      CosmosClientCache.apply(cosmosClientConfig, cosmosClientStateHandle)
    val container = client
      .getDatabase(cosmosContainerConfig.database)
      .getContainer(cosmosContainerConfig.container)

    planningInfo.foreach(info => {
      val numberOfSparkPartitions = math.min(
        Int.MaxValue,
        math.max(
          1,
          math.ceil(info.scaleFactor * effectiveSplitCountMultiplier).toInt))
      SparkBridgeInternal
        .trySplitFeedRange(container, info.feedRange, numberOfSparkPartitions)
        .foreach(feedRange =>
          inputPartitions.add(ChangeFeedInputPartition(feedRange)))
    })

    inputPartitions.asScala.toArray
  }

  private[this] def applyStorageAlignedStrategy(
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

  private[this] def applyCustomStrategy(
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

    val customPartitioningFactor = planningInfo
      .map(pi => pi.scaleFactor)
      .sum / targetPartitionCount
    applyStorageAlignedStrategy(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig,
      planningInfo,
      customPartitioningFactor,
      targetPartitionCount
    )
  }

  private[this] def getPartitionPlanningInfo(
      cosmosClientConfig: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig,
      changeFeedOffset: Option[ChangeFeedOffset]
  ): Array[PartitionPlanningInfo] = {

    assertNotNull(cosmosClientConfig, "cosmosClientConfig")
    assertNotNull(cosmosContainerConfig, "cosmosContainerConfig")

    val partitionMetadata = getPartitionMetadata(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig
    )

    assertNotNullOrEmpty(partitionMetadata, "partitionMetadata")

    val partitionPlanningInfo =
      new Array[PartitionPlanningInfo](partitionMetadata.length)
    var index = 0
    partitionMetadata.foreach(m => {
      val storageSizeInMB: Double = m.totalDocumentSizeInKB / 1024.toDouble
      val progressWeightFactor: Double =
        getChangeFeedProgressFactor(changeFeedOffset,
                                    storageSizeInMB,
                                    m.latestLsn)

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

  private[this] def getChangeFeedProgressFactor(
      changeFeedOffset: Option[ChangeFeedOffset],
      storageSizeInMB: Double,
      latestLsn: Long): Double = {
    changeFeedOffset match {
      case None => 1
      case Some(offset) =>
        val lsnFromOffset = SparkBridgeImplementationInternal
          .extractLsnFromChangeFeedContinuation(offset.changeFeedState)

        if (lsnFromOffset <= 0 || storageSizeInMB == 0) {
          // No progress has been made so far - use one Spark partition per GB
          1
        } else if (latestLsn <= lsnFromOffset) {
          // If progress has caught up with estimation already make sure we only use one Spark partition
          // for the physical partition in Cosmos
          1 / storageSizeInMB.toDouble
        } else {
          // Use weight factor based on progress. This estimate assumes equal distribution of storage
          // size per LSN - which is a "good enough" simplification
          (latestLsn - lsnFromOffset) / latestLsn.toDouble
        }
    }
  }

  private[this] def getFeedRanges(
      cosmosClientConfig: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig
  ): SMono[util.List[FeedRange]] = {

    assertNotNull(cosmosClientConfig, "cosmosClientConfig")
    assertNotNull(cosmosContainerConfig, "cosmosContainerConfig")
    val client =
      CosmosClientCache.apply(cosmosClientConfig, cosmosClientStateHandle)

    val container = client
      .getDatabase(cosmosContainerConfig.database)
      .getContainer(cosmosContainerConfig.container)

    container.getFeedRanges.asScala
  }

  private[this] def getPartitionMetadata(
      cosmosClientConfig: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig
  ): Array[PartitionMetadata] = {

    this
      .getFeedRanges(cosmosClientConfig,
                     cosmosClientStateHandle,
                     cosmosContainerConfig)
      .flatMap(feedRanges => {
        SFlux
          .fromArray(feedRanges.toArray())
          .flatMap(
            f =>
              PartitionMetadataCache.apply(
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
