// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

class CosmosConfigSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals

  "Config Parser" should "parse account credentials" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.accountConsistency" -> "Strong"
    )

    val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

    endpointConfig.endpoint shouldEqual "https://localhsot:8081"
    endpointConfig.key shouldEqual "xyz"
    endpointConfig.consistency.get shouldEqual "Strong"
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

    "Read Config Parser" should "parse read configuration" in {
        val customQuery = "select * from c"
        val userConfig = Map(
            "spark.cosmos.read.inferSchemaSamplingSize" -> "50",
            "spark.cosmos.read.inferSchemaEnabled" -> "false",
            "spark.cosmos.read.inferSchemaQuery" -> customQuery
        )

        val config = CosmosSchemaInferenceConfig.parseCosmosReadConfig(userConfig)

        config.inferSchemaSamplingSize shouldEqual 50
        config.inferSchemaEnabled shouldBe false
        config.inferSchemaQuery shouldEqual Some(customQuery)
    }
  //scalastyle:on multiple.string.literals
}
