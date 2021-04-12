// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, TestConfigurations}
import org.apache.spark.broadcast.Broadcast
import org.mockito.Mockito.{mock, verify}

class CosmosClientCacheITest extends IntegrationSpec with CosmosClient {
  //scalastyle:off multiple.string.literals

  private val cosmosEndpoint = TestConfigurations.HOST
  private val cosmosMasterKey = TestConfigurations.MASTER_KEY

  "CosmosClientCache" should "get cached object with same config" in {
    val userConfig = CosmosClientConfiguration(Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey
    ), useEventualConsistency = true)

    val client1 = CosmosClientCache(userConfig, None)
    val client2 = CosmosClientCache(userConfig, None)

    client2 should be theSameInstanceAs client1
    CosmosClientCache.purge(userConfig)
  }

  it should "return a new instance after purging" in {
    val userConfig = CosmosClientConfiguration(Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey
    ), useEventualConsistency = true)

    val client1 = CosmosClientCache(userConfig, None)
    CosmosClientCache.purge(userConfig)
    val client2 = CosmosClientCache(userConfig, None)

    client2 shouldNot be theSameInstanceAs client1
    CosmosClientCache.purge(userConfig)
  }

  it should "use state during initialization" in {
    val userConfig = CosmosClientConfiguration(Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey
    ), useEventualConsistency = true)

    val broadcast = mock(classOf[Broadcast[CosmosClientMetadataCachesSnapshot]])
    val client1 = CosmosClientCache(userConfig, Option(broadcast))
    verify(broadcast).value
    client1 shouldBe a[CosmosAsyncClient]
    CosmosClientCache.purge(userConfig)
  }
}
