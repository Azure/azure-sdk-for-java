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
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName}"
  }

  "CosmosClientConfiguration" should "use different cache key for client telemetry enabled/disabled" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.clientTelemetry.enabled" -> "true"
    )

    val forceEventual = false
    val configuration = CosmosClientConfiguration(userConfig, forceEventual)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.key shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe false
    configuration.useEventualConsistency shouldEqual forceEventual
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName}"
    configuration.enableClientTelemetry shouldEqual true
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.clientTelemetryEndpoint shouldEqual None

    val userConfig2 = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.clientTelemetry.enabled" -> "false",
      "spark.cosmos.clientTelemetry.endpoint" -> "SomeEndpoint01"
    )

    val configuration2 = CosmosClientConfiguration(userConfig2, forceEventual)

    configuration2.endpoint shouldEqual userConfig2("spark.cosmos.accountEndpoint")
    configuration2.key shouldEqual userConfig2("spark.cosmos.accountKey")
    configuration2.useGatewayMode shouldBe false
    configuration2.useEventualConsistency shouldEqual forceEventual
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration2.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName}"
    configuration2.enableClientTelemetry shouldEqual false
    configuration2.clientTelemetryEndpoint shouldEqual Some("SomeEndpoint01")

    val userConfig3 = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.clientTelemetry.enabled" -> "true",
      "spark.cosmos.clientTelemetry.endpoint" -> "SomeEndpoint03"
    )

    val configuration3 = CosmosClientConfiguration(userConfig3, forceEventual)

    configuration3.endpoint shouldEqual userConfig3("spark.cosmos.accountEndpoint")
    configuration3.key shouldEqual userConfig3("spark.cosmos.accountKey")
    configuration3.useGatewayMode shouldBe false
    configuration3.useEventualConsistency shouldEqual forceEventual
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration3.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName}"
    configuration3.enableClientTelemetry shouldEqual true
    configuration3.clientTelemetryEndpoint shouldEqual Some("SomeEndpoint03")

    configuration.equals(configuration2).shouldEqual(false)
    configuration.equals(configuration3).shouldEqual(false)
    configuration2.equals(configuration3).shouldEqual(false)
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
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName} $myApp"
  }

  it should "allow disabling endpoint rediscovery" in {
    val myApp = "myApp"
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhsot:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.disableTcpConnectionEndpointRediscovery" -> "true",
    )

    val forceEventual = false
    val configuration = CosmosClientConfiguration(userConfig, forceEventual)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.key shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe false
    configuration.useEventualConsistency shouldEqual forceEventual
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual true
  }
}
