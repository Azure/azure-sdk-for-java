// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, SparkBridgeInternal}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver, requireNotNull}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.read.streaming.{ReadAllAvailable, ReadLimit, ReadMaxFiles, ReadMaxRows}
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scala.publisher.SMono.PimpJMono

import java.time.Duration
import java.util
import java.util.concurrent.atomic.AtomicLong

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// TODO @fabianm remove
// scalastyle:off method.length
private object CosmosPartitionPlanner {
  def createInputPartitions
  (
     cosmosClientConfig: CosmosClientConfiguration,
     cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
     cosmosContainerConfig: CosmosContainerConfig,
     cosmosPartitioningConfig: CosmosPartitioningConfig,
     defaultMinimalPartitionCount: Int,
     defaultMaxPartitionSizeInMB: Int,
     readLimit: ReadLimit
  ): Array[CosmosInputPartition] = {
    assertOnSparkDriver()
    //scalastyle:off multiple.string.literals
    requireNotNull(cosmosClientConfig, "cosmosClientConfig")
    requireNotNull(cosmosContainerConfig, "cosmosContainerConfig")
    requireNotNull(cosmosPartitioningConfig, "cosmosPartitioningConfig")
    require(defaultMaxPartitionSizeInMB >= 64, "Argument 'defaultMaxPartitionSizeInMB' must at least be 64")
    require(defaultMinimalPartitionCount >= 1, "Argument 'defaultMinimalPartitionCount' must at least be 1")
    //scalastyle:on multiple.string.literals

    val partitionMetadata = getPartitionMetadata(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig
    )

    val client =
      CosmosClientCache.apply(cosmosClientConfig, cosmosClientStateHandle)
    val container = client
      .getDatabase(cosmosContainerConfig.database)
      .getContainer(cosmosContainerConfig.container)

    createInputPartitions(
      cosmosPartitioningConfig,
      container,
      partitionMetadata: Array[PartitionMetadata],
      defaultMinimalPartitionCount,
      defaultMaxPartitionSizeInMB,
      readLimit
    )
  }

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
          3 / defaultMaxPartitionSizeInMB.toDouble,
          defaultMinimalPartitionCount
        )
    }
  }
  // scalastyle:on method.length

  private[this] def applyRestrictiveStrategy
  (
    partitionPlanningInfo: Array[PartitionPlanningInfo]
  ): Array[CosmosInputPartition] = {
    partitionPlanningInfo.map(info =>
      CosmosInputPartition(
        SparkBridgeImplementationInternal.toFeedRange(info.feedRange),
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

  def getPartitionMetadata(
      cosmosClientConfig: CosmosClientConfiguration,
      cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
      cosmosContainerConfig: CosmosContainerConfig,
      maxStaleness: Option[Duration] = None
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
                f.toString,
                maxStaleness
            ))
          .collectSeq()
      })
      .block()
      .toArray
  }
}
