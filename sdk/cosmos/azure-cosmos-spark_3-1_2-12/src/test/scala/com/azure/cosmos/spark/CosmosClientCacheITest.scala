// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, TestConfigurations}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.broadcast.Broadcast
import org.mockito.Mockito.{mock, verify}

class CosmosClientCacheITest extends IntegrationSpec with CosmosClient with BasicLoggingTrait {
  //scalastyle:off multiple.string.literals

  private val cosmosEndpoint = TestConfigurations.HOST
  private val cosmosMasterKey = TestConfigurations.MASTER_KEY

  "CosmosClientCache" should "get cached object with same config" in {
    val userConfig = CosmosClientConfiguration(Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey
    ), useEventualConsistency = true)

    Loan(CosmosClientCache(userConfig, None, "CosmosClientCacheITest-01"))
      .to(client1 => {
        Loan(CosmosClientCache(userConfig, None, "CosmosClientCacheITest-02"))
          .to(client2 => {
              client2.client should be theSameInstanceAs client1.client

            val ownerInfo = CosmosClientCache.ownerInformation(userConfig)
            logInfo(s"OwnerInfo $ownerInfo")
            ownerInfo.contains("CosmosClientCacheITest-01") shouldEqual true
            ownerInfo.contains("CosmosClientCacheITest-02") shouldEqual true
            ownerInfo.contains("CosmosClientCacheITest-03") shouldEqual false
            CosmosClientCache.purge(userConfig)
          })
      })
  }

  it should "return a new instance after purging" in {
    val userConfig = CosmosClientConfiguration(Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey
    ), useEventualConsistency = true)

    Loan(CosmosClientCache(userConfig, None, "CosmosClientCacheITest-03"))
      .to(client1 => {
        CosmosClientCache.purge(userConfig)
        Loan(CosmosClientCache(userConfig, None, "CosmosClientCacheITest-04"))
          .to(client2 => {

            client2 shouldNot be theSameInstanceAs client1
            CosmosClientCache.purge(userConfig)
          })
      })
  }

  it should "use state during initialization" in {
    val userConfig = CosmosClientConfiguration(Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey
    ), useEventualConsistency = true)

    val broadcast = mock(classOf[Broadcast[CosmosClientMetadataCachesSnapshot]])
    Loan(CosmosClientCache(userConfig, Option(broadcast), "CosmosClientCacheITest-05"))
      .to(client1 => {
        verify(broadcast).value
        client1 shouldBe a[CosmosClientCacheItem]
        client1.client shouldBe a[CosmosAsyncClient]
        CosmosClientCache.purge(userConfig)
      })
  }
}
