// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.lang.management.ManagementFactory

class CosmosClientConfigurationSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals

  "CosmosClientConfiguration" should "parse configuration" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz"
    )

    val forceEventual = false
    val configuration = CosmosClientConfiguration(userConfig, forceEventual)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.key shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe false
    configuration.useEventualConsistency shouldEqual forceEventual
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName}"
  }

  it should "apply applicationName if specified" in {
    val myApp = "myApp"
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.useGatewayMode" -> "true"
    )

    val forceEventual = true
    val configuration = CosmosClientConfiguration(userConfig, forceEventual)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.key shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe true
    configuration.useEventualConsistency shouldEqual forceEventual
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName} $myApp"
  }
}
