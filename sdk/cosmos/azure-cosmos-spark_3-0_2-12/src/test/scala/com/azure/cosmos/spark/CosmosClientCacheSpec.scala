// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations


class CosmosClientCacheSpec extends UnitSpec {
    private val cosmosEndpoint = TestConfigurations.HOST
    private val cosmosMasterKey = TestConfigurations.MASTER_KEY


    "CosmosClientCache" should "get cached object with same config" in {
        val userConfig = CosmosClientConfiguration(Map(
            "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
            "spark.cosmos.accountKey" -> cosmosMasterKey,
            "spark.cosmos.accountConsistency" -> "Strong"
        ))

        val client1 = CosmosClientCache(userConfig)
        val client2 = CosmosClientCache(userConfig)

        client2 should be theSameInstanceAs client1
    }

    it should "return a new instance after purging" in {
        val userConfig = CosmosClientConfiguration(Map(
            "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
            "spark.cosmos.accountKey" -> cosmosMasterKey,
            "spark.cosmos.accountConsistency" -> "Strong"
        ))

        val client1 = CosmosClientCache(userConfig)
        CosmosClientCache.purge(userConfig)
        val client2 = CosmosClientCache(userConfig)

        client2 shouldNot be theSameInstanceAs client1
    }
}
