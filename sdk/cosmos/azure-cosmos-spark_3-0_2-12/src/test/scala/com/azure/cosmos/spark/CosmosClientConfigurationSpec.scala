// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.ConsistencyLevel

class CosmosClientConfigurationSpec extends UnitSpec {
    //scalastyle:off multiple.string.literals

    "CosmosClientConfiguration" should "parse configuration" in {
        val userConfig = Map(
            "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
            "spark.cosmos.accountKey" -> "xyz",
            "spark.cosmos.accountConsistency" -> "Strong"
        )

        val configuration = CosmosClientConfiguration(userConfig)

        configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
        configuration.key shouldEqual userConfig("spark.cosmos.accountKey")
        configuration.consistencyLevel shouldEqual ConsistencyLevel.STRONG
    }

    it should "use default for consistency" in {
        val userConfig = Map(
            "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
            "spark.cosmos.accountKey" -> "xyz"
        )

        val configuration = CosmosClientConfiguration(userConfig)

        configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
        configuration.key shouldEqual userConfig("spark.cosmos.accountKey")
        configuration.consistencyLevel shouldEqual ConsistencyLevel.EVENTUAL
    }

    it should "use default for consistency if wrong value" in {
        val userConfig = Map(
            "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
            "spark.cosmos.accountKey" -> "xyz",
            "spark.cosmos.accountConsistency" -> "NotAConsistency"
        )

        val configuration = CosmosClientConfiguration(userConfig)

        configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
        configuration.key shouldEqual userConfig("spark.cosmos.accountKey")
        configuration.consistencyLevel shouldEqual ConsistencyLevel.EVENTUAL
    }
}
