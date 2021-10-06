// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal, TestConfigurations, Utils}
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.azure.cosmos.spark.CosmosPartitionPlanner.{createInputPartitions, getPartitionMetadata}
import com.azure.cosmos.util.CosmosPagedFlux
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.read.streaming.ReadLimit
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.Assertion

import java.time.{Duration, Instant}
import java.util
import java.util.UUID
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.asScalaBufferConverter

class CosmosPartitionPlannerITest
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
  private[this] var feedRanges = List(NormalizedRange("", "FF"))

  lazy val cosmosBEPartitionCount: Int = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer).getFeedRanges.block().size()

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  it should "provide 1 partition as long as storage size is <= 128 MB" in {
    evaluateStorageBasedStrategy(0, 1 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(1, 1 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(100 * 1024, 1 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(128 * 1000 + 1, 1 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(128 * 1024, 1 * cosmosBEPartitionCount)
  }

  it should "provide multiple partitions as soon as storage size is > 128 MB" in {
    evaluateStorageBasedStrategy(128 * 1024 + 1, 2 * cosmosBEPartitionCount)
    evaluateStorageBasedStrategy(256 * 1024, 2 * cosmosBEPartitionCount)
  }

  it should "provide honor custom defaultMaxPartitionSize values" in {
    evaluateStorageBasedStrategy(128 * 1024, 1 * cosmosBEPartitionCount, defaultMaxPartitionSizeInMB = 1024)
    evaluateStorageBasedStrategy(256 * 1024, 1 * cosmosBEPartitionCount, defaultMaxPartitionSizeInMB = 1024)
    evaluateStorageBasedStrategy(2048 * 1024, 2 * cosmosBEPartitionCount, defaultMaxPartitionSizeInMB = 1024)
    evaluateStorageBasedStrategy(50 * 1024 * 1024, 50 * cosmosBEPartitionCount, defaultMaxPartitionSizeInMB = 1024)
  }

  it should "create one partition for every 128 MB (rounded up)" in {

    // testing the upper bound for 50 GB partition
    evaluateStorageBasedStrategy(50 * 1024 * 1024, 400 * cosmosBEPartitionCount)

    for (_ <- 1 to 100) {
      val docSizeInKB = rnd.nextInt(50 * 1024 * 1024)
      val expectedPartitionCount = (docSizeInKB + (128 * 1024) - 1)/(128 * 1024)
      evaluateStorageBasedStrategy(docSizeInKB, expectedPartitionCount * cosmosBEPartitionCount)
    }
  }

  it should "create exactly 5 times more partitions than with Default for Aggressive" in {

    // Min is still 1 (not 3) to avoid wasting compute resources where not necessary
    evaluateStrategy("Aggressive", 0, 1 * cosmosBEPartitionCount)

    // 5 Spark partitions for every 128 MB
    evaluateStrategy("Aggressive", 10 * 128 * 1024, 5 * 10 * cosmosBEPartitionCount)

    // change feed progress is honored
    evaluateStrategy(
      "Aggressive",
      10 * 128 * 1024,
      5 * 3 * cosmosBEPartitionCount,
      Some(70))

    for (_ <- 1 to 100) {
      val docSizeInKB = rnd.nextInt(50 * 1024 * 1024)
      val expectedPartitionCount = ((5 * docSizeInKB) + (128 * 1024) - 1)/(128 * 1024)
      evaluateStrategy("Aggressive", docSizeInKB, expectedPartitionCount * cosmosBEPartitionCount)
    }
  }

  it should "honor the relative progress of currentLsn/lastLsn" in {
    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      10 * cosmosBEPartitionCount,
      None)

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      1 * cosmosBEPartitionCount,
      Some(100))

    // always return at least 1 partition even when metadata is stale
    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      1 * cosmosBEPartitionCount,
      Some(200))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      1 * cosmosBEPartitionCount,
      Some(90))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      8 * cosmosBEPartitionCount,
      Some(25))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      6 * cosmosBEPartitionCount,
      Some(49))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      5 * cosmosBEPartitionCount,
      Some(50))

    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      5 * cosmosBEPartitionCount,
      Some(51))
  }

  it should "honor the min partition count to allow saturating all spark executors" in {
    evaluateStorageBasedStrategy(
      128 * 10 * 1024,
      4 * cosmosBEPartitionCount, // would usually be just 1 because progress > 100%
      Some(200),
      defaultMinimalPartitionCount = 4 * cosmosBEPartitionCount)
  }

  it should "honor the custom targeted partition count" in {

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
      Some(200),
      customPartitionCount = Some(23 * cosmosBEPartitionCount))

    // targetPartitionCount is ignore when Strategy is != Custom
    evaluateStrategy(
      "Default",
      128 * 10 * 1024,
      1 * cosmosBEPartitionCount, // would usually be just 1 because progress > 100%
      Some(200),
      customPartitionCount = Some(23 * cosmosBEPartitionCount))
  }

  it should "provide 1 Spark partition per physical Partition only " +
    "independent of storage size or change feed progress" in {

    evaluateRestrictiveStrategy(0, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(1, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(100 * 1024, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(128 * 1000 + 1, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(128 * 1024, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(50 * 1024 * 1024, 1 * cosmosBEPartitionCount)
    evaluateRestrictiveStrategy(
      50 * 1024 * 1024,
      1 * cosmosBEPartitionCount,
      Some(200))
    evaluateRestrictiveStrategy(
      50 * 1024 * 1024,
      1 * cosmosBEPartitionCount,
      Some(90))
  }

  "createInitialOffset" should "only ever retrieve one page of changes per feedRange" in {

    val container = this.cosmosClient
      .getDatabase(containerConfig.database)
      .getContainer(containerConfig.container)

    for (i <- 0 until 10) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("index", i.toString)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(userConfigTemplate)
    val testId = UUID.randomUUID().toString

    val pagesRetrievedCounterMap = new util.HashMap[FeedRange, AtomicInteger]
    for (feedRange <- container.getFeedRanges.block().asScala) {
      pagesRetrievedCounterMap.put(feedRange, new AtomicInteger())
    }

    val initialFeedRangesCount = pagesRetrievedCounterMap.size()

    // mocking container to count number of pages retrieved
    val mockContainer = Mockito.spy(container)
    Mockito.doAnswer(new Answer[CosmosPagedFlux[ObjectNode]]() {
      override def answer(invocationOnMock: InvocationOnMock): CosmosPagedFlux[ObjectNode] = {
        val requestOptions: CosmosChangeFeedRequestOptions =
          invocationOnMock.getArgument(0, classOf[CosmosChangeFeedRequestOptions])
        val pageFlux = container.queryChangeFeed(requestOptions, classOf[ObjectNode])
        pageFlux.handle(feedResponse => {
          pagesRetrievedCounterMap.get(requestOptions.getFeedRange).getAndIncrement()
        })
      }
    }).when(mockContainer).queryChangeFeed(
      ArgumentMatchers.any(classOf[CosmosChangeFeedRequestOptions]),
      ArgumentMatchers.any()
    )

    val initialOffset = CosmosPartitionPlanner.createInitialOffset(mockContainer, changeFeedConfig, streamId = Some(testId))

    //scalastyle:off null
    initialOffset should not equal null
    //scalastyle:on null

    pagesRetrievedCounterMap should have size initialFeedRangesCount
    for (feedRange <- container.getFeedRanges.block().asScala) {
      pagesRetrievedCounterMap.get(feedRange).get() shouldEqual 1
    }
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  private[this] def evaluateStorageBasedStrategy
  (
    docSizeInKB: Long,
    expectedPartitionCount: Int,
    startLsn: Option[Long] = None,

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
      startLsn,
      defaultMaxPartitionSizeInMB,
      defaultMinimalPartitionCount
    )
  }

  private[this] def createPartitions
  (
    startLsn: Option[Long],
    userConfig: Map[String, String],
    defaultMaxPartitionSizeInMB: Int,
    defaultMinimalPartitionCount: Int
  ) = {
    val rawPartitionMetadata = getPartitionMetadata(
      userConfig,
      clientConfig,
      None,
      containerConfig
    )

    val partitionMetadata = if (startLsn.isDefined) {
      rawPartitionMetadata
        .map(metadata => metadata.cloneForSubRange(metadata.feedRange, startLsn.get))
    } else {
      rawPartitionMetadata
    }

    val client = CosmosClientCache.apply(clientConfig, None)
    val container = client
      .getDatabase(containerConfig.database)
      .getContainer(containerConfig.container)

    createInputPartitions(
      CosmosPartitioningConfig.parseCosmosPartitioningConfig(userConfig),
      container,
      partitionMetadata: Array[PartitionMetadata],
      defaultMinimalPartitionCount,
      defaultMaxPartitionSizeInMB,
      ReadLimit.allAvailable()
    )
  }

  //scalastyle:off magic.number
  private[this] def evaluateStrategy
  (
    strategy: String,
    docSizeInKB: Long,
    expectedPartitionCount: Int,
    startLsn: Option[Long] = None,

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
    userConfig.put("spark.cosmos.read.partitioning.strategy", strategy)
    if (customPartitionCount.isDefined) {
      userConfig.put("spark.cosmos.partitioning.targetedCount", String.valueOf(customPartitionCount.get))
    }
    val partitions =
      createPartitions(startLsn, userConfig.toMap, defaultMaxPartitionSizeInMB, defaultMinimalPartitionCount)
    //scalastyle:on magic.number

    val alwaysThrow = false
    partitions.foreach {
      case _: CosmosInputPartition => Unit
      case _ => assert(alwaysThrow, "Unexpected partition type")
    }
    partitions should have size expectedPartitionCount
  }

  private[this] def evaluateRestrictiveStrategy
  (
    docSizeInKB: Long,
    expectedPartitionCount: Int,
    currentOffset: Option[Long] = None,

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
          Map[String, String](),
          this.clientConfig,
          None,
          this.containerConfig,
          feedRange,
          documentCount,
          documentSizeInKB,
          latestLsn,
          0,
          None,
          new AtomicLong(nowEpochMs),
          new AtomicLong(nowEpochMs)))
    }
  }

  private[this] def reinitialize(): Unit = {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val ranges = ArrayBuffer[NormalizedRange]()
    container.getFeedRanges.block.forEach(fr => {
      val normalizedRange = SparkBridgeImplementationInternal.toNormalizedRange(fr)
      PartitionMetadataCache.purge(containerConfig, normalizedRange)
      ranges += normalizedRange
    })

    ranges should have size cosmosBEPartitionCount
    this.feedRanges = ranges.toList

    PartitionMetadataCache.resetTestOverrides()
  }
}
