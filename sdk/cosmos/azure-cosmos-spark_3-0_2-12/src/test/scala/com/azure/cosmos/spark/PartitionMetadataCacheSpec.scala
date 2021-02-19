// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations

import java.time.Instant

class PartitionMetadataCacheSpec
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
    "spark.cosmos.container" -> cosmosContainer,
  )
  private[this] val clientConfig = CosmosClientConfiguration(userConfig, useEventualConsistency = true)
  private[this] val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)

  //scalastyle:off multiple.string.literals
  it should "create the partition metadata for the first physical partition" taggedAs RequiresCosmosEndpoint in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val feedRange = container.getFeedRanges.block.get(0).toString
    val startEpochMs = Instant.now.toEpochMilli

    val newItem = PartitionMetadataCache(clientConfig, None, containerConfig, feedRange).block()
    newItem.feedRange shouldEqual feedRange
    newItem.lastRetrieved.get should be >= startEpochMs
    newItem.lastUpdated.get should be >= startEpochMs

    val initialLastRetrieved = newItem.lastRetrieved.get
    val initialLastUpdated = newItem.lastUpdated.get

    //scalastyle:off magic.number
    Thread.sleep(10)
    //scalastyle:on magic.number

    val nextRetrievedItem = PartitionMetadataCache(clientConfig, None, containerConfig, feedRange).block()
    nextRetrievedItem.feedRange shouldEqual feedRange
    nextRetrievedItem.lastUpdated.get shouldEqual initialLastUpdated
    nextRetrievedItem.lastRetrieved.get should be > initialLastRetrieved
  }
  //scalastyle:on multiple.string.literals
}
