// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{SparkBridgeImplementationInternal, TestConfigurations}

import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicLong

class PartitionMetadataCacheITest
  extends UnitSpec
  with CosmosClient
  with CosmosContainer
  with Spark {

  private[this] val cosmosEndpoint = TestConfigurations.HOST
  private[this] val cosmosMasterKey = TestConfigurations.MASTER_KEY
  private[this] val userConfig = Map(
    "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.accountKey" -> cosmosMasterKey,
    "spark.cosmos.database" -> cosmosDatabase,
    "spark.cosmos.container" -> cosmosContainer
  )
  private[this] val clientConfig = CosmosClientConfiguration(userConfig, useEventualConsistency = true)
  private[this] val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)
  private[this] var feedRange: NormalizedRange = NormalizedRange("", "FF")

  //scalastyle:off multiple.string.literals
  it should "create the partition metadata for the first physical partition" in {
    this.reinitialize()
    val startEpochMs = Instant.now.toEpochMilli

    val newItem = PartitionMetadataCache(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      feedRange).block()
    newItem.feedRange shouldEqual feedRange
    newItem.lastRetrieved.get should be >= startEpochMs
    newItem.lastUpdated.get should be >= startEpochMs
  }

  //scalastyle:off multiple.string.literals
  it should "update lastRetrievedAt timestamp every time" in {
    this.reinitialize()
    val startEpochMs = Instant.now.toEpochMilli

    val newItem = PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    newItem.feedRange shouldEqual feedRange
    newItem.lastUpdated.get should be >= startEpochMs
    newItem.lastRetrieved.get should be >=  startEpochMs

    val initialLastRetrieved = newItem.lastRetrieved.get
    val initialLastUpdated = newItem.lastUpdated.get

    //scalastyle:off magic.number
    Thread.sleep(10)
    //scalastyle:on magic.number

    val nextRetrievedItem =
      PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    nextRetrievedItem.feedRange shouldEqual feedRange
    nextRetrievedItem.lastUpdated.get shouldEqual initialLastUpdated
    nextRetrievedItem.lastRetrieved.get should be > initialLastRetrieved
  }

  //scalastyle:off multiple.string.literals
  it should "retrieve new item after purge" in {
    this.reinitialize()

    val newItem = PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    val initialLastUpdated = newItem.lastUpdated.get
    PartitionMetadataCache.purge(containerConfig, feedRange) shouldEqual true

    //scalastyle:off magic.number
    Thread.sleep(10)
    //scalastyle:on magic.number

    val nextRetrievedItem =
      PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    nextRetrievedItem.lastUpdated.get should be > initialLastUpdated
  }

  //scalastyle:off multiple.string.literals
  it should "delete cached item after TTL elapsed" in {
    this.reinitialize()

    val newItem = PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    val initialLastUpdated = newItem.lastUpdated.get

    //scalastyle:off magic.number
    PartitionMetadataCache.applyTestOverrides(
      newRefreshIntervalInMsOverride = Some(10),
      newStaleCachedItemRefreshPeriodInMsOverride = Some(10 * 1000),
      newCachedItemTtlInMsOverride = Some(50))

    Thread.sleep(2000)
    //scalastyle:on magic.number

    PartitionMetadataCache.purge(containerConfig, feedRange) shouldEqual false
    this.reinitialize()
  }

  //scalastyle:off multiple.string.literals
  it should "automatically update the cached item after staleness threshold elapsed" in {
    this.reinitialize()

    val newItem = PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    val initialLastUpdated = newItem.lastUpdated.get

    //scalastyle:off magic.number
    PartitionMetadataCache.applyTestOverrides(
      newRefreshIntervalInMsOverride = Some(10),
      newStaleCachedItemRefreshPeriodInMsOverride = Some(50),
      newCachedItemTtlInMsOverride = Some(10 * 1000))

    Thread.sleep(500)
    //scalastyle:on magic.number

    val candidate =
      PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    candidate.lastUpdated.get should be > initialLastUpdated

    PartitionMetadataCache.purge(containerConfig, feedRange) shouldEqual true
    this.reinitialize()
  }

  //scalastyle:off multiple.string.literals
  it should "honor maxStaleness" in {
    this.reinitialize()

    val newItem =
      PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    var initialLastUpdated = newItem.lastUpdated.get

    //scalastyle:off magic.number
    Thread.sleep(500)

    var candidate: PartitionMetadata = PartitionMetadataCache(
      Map[String, String](), clientConfig, None, containerConfig, feedRange, Some(Duration.ofMillis(400))).block()

    candidate.lastUpdated.get should be > initialLastUpdated
    initialLastUpdated = candidate.lastUpdated.get
    Thread.sleep(1)
    //scalastyle:on magic.number
    candidate = PartitionMetadataCache(
      Map[String, String](), clientConfig, None, containerConfig, feedRange, Some(Duration.ZERO)).block()
    candidate.lastUpdated.get should be > initialLastUpdated

    PartitionMetadataCache.purge(containerConfig, feedRange) shouldEqual true
    this.reinitialize()
  }

  it should "honor test data overrides" in {
    this.reinitialize()
    val startEpochMs = Instant.now.toEpochMilli

    val rnd = scala.util.Random
    val docCount = rnd.nextInt()
    val docSize = rnd.nextInt()
    val lastLsn = rnd.nextInt()

    val testMetadata = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      feedRange,
      docCount,
      docSize,
      lastLsn,
      0,
      None,
      new AtomicLong(startEpochMs),
      new AtomicLong(startEpochMs))

    PartitionMetadataCache.injectTestData(containerConfig, feedRange, testMetadata)

    //scalastyle:off magic.number
    Thread.sleep(10)
    //scalastyle:on magic.number

    val newItem = PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    newItem.feedRange shouldEqual feedRange
    newItem.lastRetrieved.get should be >= startEpochMs
    newItem.lastUpdated.get shouldEqual startEpochMs
    newItem should be theSameInstanceAs testMetadata

    val reinitializedEpochMs = Instant.now.toEpochMilli
    this.reinitialize()

    val realItem = PartitionMetadataCache(Map[String, String](), clientConfig, None, containerConfig, feedRange).block()
    realItem.lastUpdated.get should be >= reinitializedEpochMs
    realItem should not (be theSameInstanceAs testMetadata)
  }

  private[this] def reinitialize() = {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    val cosmosFeedRange = container.getFeedRanges.block.get(0)
    this.feedRange = SparkBridgeImplementationInternal.toNormalizedRange(cosmosFeedRange)
    PartitionMetadataCache.purge(containerConfig, feedRange)
    PartitionMetadataCache.resetTestOverrides()
  }
  //scalastyle:on multiple.string.literals
}
