// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import org.scalatest.Assertion

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.{Base64, UUID}
import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable.ArrayBuffer

class CosmosPartitionPlannerSpec
  extends UnitSpec
    with CosmosClient
    with CosmosContainer
    with Spark {

  private[this] val rnd = scala.util.Random
  private[this] val cosmosEndpoint = TestConfigurations.HOST
  private[this] val cosmosMasterKey = TestConfigurations.MASTER_KEY
  private[this] val userConfigTemplate = Map[String, String](
    "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.accountKey" -> cosmosMasterKey,
    "spark.cosmos.database" -> cosmosDatabase,
    "spark.cosmos.container" -> cosmosContainer
  )
  private[this] val clientConfig = CosmosClientConfiguration(userConfigTemplate, useEventualConsistency = true)
  private[this] val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfigTemplate)
  private[this] var feedRanges = List("")

  lazy val cosmosBEPartitionCount = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer).getFeedRanges.block().size()

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  it should "provide 1 partition as long as storage size is <= 128 MB" taggedAs RequiresCosmosEndpoint in {
    evaluateStorageBasedStrategy(0, 1 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(1, 1 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(100 * 1024, 1 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(128 * 1000 + 1, 1 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(128 * 1024, 1 * cosmosBEPartitionCount)
  }

  it should "provide multiple partitions as soon as storage size is > 128 MB" taggedAs RequiresCosmosEndpoint in {
    evaluateStorageBasedStrategy(128 * 1024 + 1, 2 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(256 * 1024, 2 * cosmosBEPartitionCount)
  }

  it should "provide honor custom defaultMaxPartitionSize values" taggedAs RequiresCosmosEndpoint in {
    evaluateStorageBasedStrategy(128 * 1024, 1 * cosmosBEPartitionCount, defaultMaxPartitionSizeInMB = 1024)
    evaluateStorageBasedStrategy(256 * 1024, 1 * cosmosBEPartitionCount, defaultMaxPartitionSizeInMB = 1024)
    evaluateStorageBasedStrategy(2048 * 1024, 2 * cosmosBEPartitionCount, defaultMaxPartitionSizeInMB = 1024)
    evaluateStorageBasedStrategy(50 * 1024 * 1024, 50 * cosmosBEPartitionCount, defaultMaxPartitionSizeInMB = 1024)
  }

  it should "create one partition for every 128 MB (rounded up)" taggedAs RequiresCosmosEndpoint in {

    // testing the upper bound for 50 GB partition
    evaluateStorageBasedStrategy(50 * 1024 * 1024, 400 * cosmosBEPartitionCount)

    for (_ <- 1 to 100) {
      val docSizeInKB = rnd.nextInt(50 * 1024 * 1024)
      val expectedPartitionCount = (docSizeInKB + (128 * 1024) - 1)/(128 * 1024)
      evaluateStorageBasedStrategy(docSizeInKB, expectedPartitionCount * cosmosBEPartitionCount)
    }
  }

  it should "create exactly 3 times more partitions than with Default for Aggressive" in {

    // Min is still 1 (not 3) to avoid wasting compute resources where not necessary
    evaluateStrategy("Aggressive", 0, 1 * cosmosBEPartitionCount)

    // 3 Spark partitions for every 128 MB
    evaluateStrategy("Aggressive", 10 * 128 * 1024, 3 * 10 * cosmosBEPartitionCount)

    // change feed progress is honored
    evaluateStrategy(
      "Aggressive",
      10 * 128 * 1024,
      3 * 3 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(0.7)))

    for (_ <- 1 to 100) {
      val docSizeInKB = rnd.nextInt(50 * 1024 * 1024)
      val expectedPartitionCount = ((3 * docSizeInKB) + (128 * 1024) - 1)/(128 * 1024)
      evaluateStrategy("Aggressive", docSizeInKB, expectedPartitionCount * cosmosBEPartitionCount)
    }
  }

  it should "honor the relative progress of currentLsn/lastLsn" taggedAs RequiresCosmosEndpoint in {
    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      10 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(0.0)))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      1 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(1.0)))

    // always return at least 1 partition even when metadata is stale
    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      1 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(2.0)))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      1 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(0.9)))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      8 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(0.25)))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      6 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(0.499999)))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      5 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(0.5)))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      5 * cosmosBEPartitionCount,
      currentOffset = Some(createChangeFeedOffset(0.5000000001)))
  }

  it should "honor the min partition count to allow saturating all spark executors" taggedAs RequiresCosmosEndpoint in {
    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      4 * cosmosBEPartitionCount, // would usually be just 1 because progress > 100%
      currentOffset = Some(createChangeFeedOffset(2.0)),
      defaultMinimalPartitionCount = 4 * cosmosBEPartitionCount)
  }

  it should "honor the custom targeted partition count" taggedAs RequiresCosmosEndpoint in {

    // NOTE
    // targetPartitionCount is a best-effort while still maintaining
    // relative storage and change feed progress weight
    // So if you use TargetPartitionCount of 10
    // And you have two partitions one with 9 GB of data and second
    // with just 1 GB - you would still get 9 feed ranges for the first partition
    // basically it is a mechanism to target a certain number of partitions
    // MIN VALUE is 1 per physical partition - so effective partition count
    // will never be smaller as when using the restrictive strategy
    evaluateStrategy(
      "Custom",
      128 * 10 * 1024,
      23 * cosmosBEPartitionCount, // would usually be just 1 because progress > 100%
      currentOffset = Some(createChangeFeedOffset(2.0)),
      customPartitionCount = Some(23 * cosmosBEPartitionCount))

    // targetPartitionCount is ignore when Strategy is != Custom
    evaluateStrategy(
      "Default",
      128 * 10 * 1024,
      1 * cosmosBEPartitionCount, // would usually be just 1 because progress > 100%
      currentOffset = Some(createChangeFeedOffset(2.0)),
      customPartitionCount = Some(23 * cosmosBEPartitionCount))
  }

  it should "provide 1 Spark partition per physical Partition only " +
    "independent of storage size or change feed progress" taggedAs RequiresCosmosEndpoint in {

    evaluateRestrictiveStrategy(0, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(1, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(100 * 1024, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(128 * 1000 + 1, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(128 * 1024, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(50 * 1024 * 1024, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(
      50 * 1024 * 1024,
      1 * cosmosBEPartitionCount,
      Some(createChangeFeedOffset(2.0)))
    evaluateRestrictiveStrategy(
      50 * 1024 * 1024,
      1 * cosmosBEPartitionCount,
      Some(createChangeFeedOffset(0.9)))
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  private[this] def evaluateStorageBasedStrategy
  (
    docSizeInKB: Long,
    expectedPartitionCount: Int,
    currentOffset: Option[ChangeFeedOffset] = None,

    //scalastyle:off magic.number
    // 128 MB is the Spark default, can be changed via
    // SparkSession.sessionState.conf.filesMaxPartitionBytes
    defaultMaxPartitionSizeInMB: Int = 128,

    //scalastyle:off magic.number
    // maps to 1 + (2 * session.sparkContext.defaultParallelism)
    // should help saturating all executor nodes
    defaultMinimalPartitionCount: Int = cosmosBEPartitionCount
  ): Assertion = {
    this.evaluateStrategy(
      "Default",
      docSizeInKB,
      expectedPartitionCount,
      currentOffset,
      defaultMaxPartitionSizeInMB,
      defaultMinimalPartitionCount
    )
  }

  //scalastyle:off magic.number
  private[this] def evaluateStrategy
  (
    strategy: String,
    docSizeInKB: Long,
    expectedPartitionCount: Int,
    currentOffset: Option[ChangeFeedOffset] = None,

    // 128 MB is the Spark default maps to  SparkSession.sessionState.conf.filesMaxPartitionBytes
    defaultMaxPartitionSizeInMB: Int = 128,

    // maps to 1 + (2 * session.sparkContext.defaultParallelism) - should help saturating all executor nodes
    defaultMinimalPartitionCount: Int = cosmosBEPartitionCount,

    // NOTE targetPartitionCount is a best-effort while still maintaining relative storage and change feed
    // progress weight. So if you use TargetPartitionCount of 10 and you have two partitions one with 9 GB of data
    // and second with just 1 GB - you would still get 9 feed ranges for the first partition basically it is a
    // mechanism to target a certain number of partitions
    // MIN VALUE is 1 per physical partition - so effective partition count >= restrictive strategy
    customPartitionCount: Option[Int] = None
  ): Assertion = {
    this.reinitialize()
    val docCount = 50 *  rnd.nextInt(1000)
    val latestLsn = 100
    this.injectPartitionMetadata(docCount, docSizeInKB, latestLsn)
    val userConfig = collection.mutable.Map(this.userConfigTemplate.toSeq: _*)
    userConfig.put("spark.cosmos.partitioning.strategy", strategy)
    if (customPartitionCount.isDefined) {
      userConfig.put("spark.cosmos.partitioning.targetedCount", String.valueOf(customPartitionCount.get))
    }
    val partitions = CosmosPartitionPlanner.createInputPartitions(
      clientConfig,
      None,
      containerConfig,
      CosmosPartitioningConfig.parseCosmosPartitioningConfig(userConfig.toMap),
      currentOffset,
      defaultMinimalPartitionCount,
      defaultMaxPartitionSizeInMB
    )
    //scalastyle:on magic.number

    val alwaysThrow = false
    partitions.foreach {
      case _: ChangeFeedInputPartition => Unit
      case _ => assert(alwaysThrow, "Unexpected partition type")
    }
    partitions should have size expectedPartitionCount
  }

  private[this] def evaluateRestrictiveStrategy
  (
    docSizeInKB: Long,
    expectedPartitionCount: Int,
    currentOffset: Option[ChangeFeedOffset] = None,

    //scalastyle:off magic.number
    // 128 MB is the Spark default, can be changed via
    // SparkSession.sessionState.conf.filesMaxPartitionBytes
    defaultMaxPartitionSizeInMB: Int = 128
  ): Assertion = {
    this.evaluateStrategy(
      "RESTRICTIVE",
      docSizeInKB,
      expectedPartitionCount,
      currentOffset,
      defaultMaxPartitionSizeInMB)
  }

  private[this] def injectPartitionMetadata
  (
    documentCount: Long,
    documentSizeInKB: Long,
    latestLsn: Long
  ): Unit = {

    val nowEpochMs = Instant.now.toEpochMilli

    for (feedRange <- feedRanges) {
      PartitionMetadataCache.injectTestData(
        this.containerConfig,
        feedRange,
        PartitionMetadata(
          this.clientConfig,
          None,
          this.containerConfig,
          feedRange,
          documentCount,
          documentSizeInKB,
          latestLsn,
          new AtomicLong(nowEpochMs),
          new AtomicLong(nowEpochMs)))
    }
  }

  private[this] def reinitialize(): Unit = {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val ranges = ArrayBuffer[String]()
    container.getFeedRanges.block.forEach(fr => {
      PartitionMetadataCache.purge(containerConfig, fr.toString)
      ranges += fr.toString
    })

    ranges should have size cosmosBEPartitionCount
    this.feedRanges = ranges.toList

    PartitionMetadataCache.resetTestOverrides()
  }

  private[this] def createChangeFeedOffset(progress: Double) = {
    ChangeFeedOffset(createChangeFeedState((progress * 100).toLong))
  }

  private[this] def createChangeFeedState(latestLsn: Long) = {
    val collectionRid = UUID.randomUUID().toString

    val json = String.format(
      "{\"V\":1," +
        "\"Rid\":\"%s\"," +
        "\"Mode\":\"INCREMENTAL\"," +
        "\"StartFrom\":{\"Type\":\"BEGINNING\"}," +
        "\"Continuation\":%s}",
      collectionRid,
      String.format(
        "{\"V\":1," +
          "\"Rid\":\"%s\"," +
          "\"Continuation\":[" +
          "{\"token\":\"\\\"%s\\\"\",\"range\":{\"min\":\"\",\"max\":\"FF\"}}" +
          "]," +
          "\"PKRangeId\":\"0\"}",
        collectionRid,
        String.valueOf(latestLsn)))

    Base64.getUrlEncoder.encodeToString(json.getBytes(StandardCharsets.UTF_8))
  }
}
