// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.assertj.core.api.Assertions.assertThat

class CosmosConfigSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals

  "account endpoint" should "be parsed" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz"
    )

    val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

    assertThat(endpointConfig.endpoint).isEqualTo( "https://localhsot:8081")
    assertThat(endpointConfig.key).isEqualTo( "xyz")
  }

  "account endpoint" should "be validated" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "invalidUrl",
      "spark.cosmos.accountKey" -> "xyz"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("invalid URL")
    } catch {
      case e: Exception => assertThat(e.getMessage).isEqualTo(
        "invalid configuration for spark.cosmos.accountEndpoint:invalidUrl." +
          " Config description: Cosmos DB Account Endpoint Uri")
    }
  }

  "account endpoint" should "mandatory config" in {
    val userConfig = Map(
      "spark.cosmos.accountKey" -> "xyz"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("missing URL")
    } catch {
      case e: Exception => assertThat(e.getMessage).isEqualTo(
        "mandatory option spark.cosmos.accountEndpoint is missing." +
          " Config description: Cosmos DB Account Endpoint Uri")
    }
  }
  //scalastyle:on multiple.string.literals
}
