// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

class CosmosConfigSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals

  "Config Parser" should "parse account credentials" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz"
    )

    val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

    endpointConfig.endpoint shouldEqual "https://localhsot:8081"
    endpointConfig.key shouldEqual "xyz"
  }

  it should "validate account endpoint" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "invalidUrl",
      "spark.cosmos.accountKey" -> "xyz"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("invalid URL")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "invalid configuration for spark.cosmos.accountEndpoint:invalidUrl." +
          " Config description: Cosmos DB Account Endpoint Uri"
    }
  }

  it should "complain if mandatory config is missing" in {
    val userConfig = Map(
      "spark.cosmos.accountKey" -> "xyz"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("missing URL")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "mandatory option spark.cosmos.accountEndpoint is missing." +
          " Config description: Cosmos DB Account Endpoint Uri"
    }
  }
  //scalastyle:on multiple.string.literals
}
