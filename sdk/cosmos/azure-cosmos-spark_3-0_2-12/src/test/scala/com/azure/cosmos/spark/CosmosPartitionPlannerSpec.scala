// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import org.scalatest.Assertion

import java.time.Instant
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
  private[this] var feedRange = ""
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
   it should "provide 1 partition as long as storage size is <= 128 MB" in {
     evaluateStorageBasedStrategy(0, 1)
     evaluateStorageBasedStrategy(1, 1)
     evaluateStorageBasedStrategy(100 * 1024, 1)
     evaluateStorageBasedStrategy(128 * 1000 + 1, 1)
     evaluateStorageBasedStrategy(128 * 1024, 1)
   }

  it should "provide multiple partitions as soon as storage size is > 128 MB" in {
    evaluateStorageBasedStrategy(128 * 1024 + 1, 2)
    evaluateStorageBasedStrategy(256 * 1024, 2)
  }

  it should "create one partition for every 128 MB (rounded up)" in {

    // testing teh upper bound for 50 GB partition
    evaluateStorageBasedStrategy(50 * 1024 * 1024, 400)

    for (i <- 1 to 100) {
      val docSizeInKB = rnd.nextInt(50 * 1024 * 1024)
      val expectedPartitionCount = (docSizeInKB + (128 * 1024) - 1)/(128 * 1024)
      evaluateStorageBasedStrategy(docSizeInKB, expectedPartitionCount)
    }
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals

  private[this] def evaluateStorageBasedStrategy
  (
    docSizeInKB: Long,
    expectedPartitionCount: Int,

    //scalastyle:off magic.number
    // 128 MB is the Spark default, can be changed via
    // SparkSession.sessionState.conf.filesMaxPartitionBytes
    defaultMaxPartitionSizeInMB: Int = 128
  ): Assertion = {

    this.reinitialize()

    val docCount = 50 *  rnd.nextInt(1000)
    val latestLsn = rnd.nextInt()

    this.injectPartitionMetadata(docCount, docSizeInKB, latestLsn)
    val userConfig = collection.mutable.Map(this.userConfigTemplate.toSeq: _*)
    userConfig.put("spark.cosmos.partitioning.strategy", "Default")
    val partitions = CosmosPartitionPlanner.createInputPartitions(
      clientConfig,
      None,
      containerConfig,
      CosmosPartitioningConfig.parseCosmosPartitioningConfig(userConfig.toMap),
      None,
      defaultMinimalPartitionCount = 1,
      defaultMaxPartitionSizeInMB
    )
    //scalastyle:on magic.number

    partitions.foreach(p => p match {
      case partition: ChangeFeedInputPartition => Unit
      case _ => assert(false, "Unexpected partition type")
    })
    partitions should have size expectedPartitionCount
  }

  private[this] def injectPartitionMetadata
  (
    documentCount: Long,
    documentSizeInKB: Long,
    latestLsn: Long
  ) = {

    val nowEpochMs = Instant.now.toEpochMilli

    PartitionMetadataCache.injectTestData(
      this.containerConfig,
      this.feedRange,
      PartitionMetadata(
        this.clientConfig,
        None,
        this.containerConfig,
        this.feedRange,
        documentCount,
        documentSizeInKB,
        latestLsn,
        new AtomicLong(nowEpochMs),
        new AtomicLong(nowEpochMs)))
  }

  private[this] def reinitialize() = {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val ranges = ArrayBuffer[String]()
    container.getFeedRanges.block.forEach(fr => {
      PartitionMetadataCache.purge(containerConfig, fr.toString)
      ranges += fr.toString
    })

    ranges should have size 1
    this.feedRange = ranges(0)

    PartitionMetadataCache.resetTestOverrides()
  }
}
