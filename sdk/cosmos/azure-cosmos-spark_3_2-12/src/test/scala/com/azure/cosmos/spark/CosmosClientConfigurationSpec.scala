// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.ReadConsistencyStrategy

import java.lang.management.ManagementFactory
import java.util.UUID

class CosmosClientConfigurationSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals

  "CosmosClientConfiguration" should "parse configuration" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz"
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo = "")

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe false
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}"
  }

  "CosmosClientConfiguration" should "process http connection pool size configuration" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.useGatewayMode" -> "true",
      "spark.cosmos.http.connectionPoolSize" -> "1111"
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo = "")

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe true
    configuration.httpConnectionPoolSize shouldBe 1111
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}"
  }

  "CosmosClientConfiguration" should "process Spark environment info" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz"
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT
    val sparkEnvironmentInfo = s"sparkenv-${UUID.randomUUID()}"
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe false
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}"
  }

  "CosmosClientConfiguration" should "use different cache key for client telemetry enabled/disabled" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.clientTelemetry.enabled" -> "true"
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo = "")

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe false
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}"
    configuration.enableClientTelemetry shouldEqual true
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.clientTelemetryEndpoint shouldEqual None

    val userConfig2 = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.clientTelemetry.enabled" -> "false",
      "spark.cosmos.clientTelemetry.endpoint" -> "SomeEndpoint01"
    )

    val configuration2 = CosmosClientConfiguration(userConfig2, readConsistencyStrategy, sparkEnvironmentInfo = "")

    configuration2.endpoint shouldEqual userConfig2("spark.cosmos.accountEndpoint")
    configuration2.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig2("spark.cosmos.accountKey")
    configuration2.useGatewayMode shouldBe false
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration2.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}"
    configuration2.enableClientTelemetry shouldEqual false
    configuration2.clientTelemetryEndpoint shouldEqual Some("SomeEndpoint01")

    val userConfig3 = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.clientTelemetry.enabled" -> "true",
      "spark.cosmos.clientTelemetry.endpoint" -> "SomeEndpoint03"
    )

    val configuration3 = CosmosClientConfiguration(userConfig3, readConsistencyStrategy, sparkEnvironmentInfo = "")

    configuration3.endpoint shouldEqual userConfig3("spark.cosmos.accountEndpoint")
    configuration3.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig3("spark.cosmos.accountKey")
    configuration3.useGatewayMode shouldBe false
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration3.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}"
    configuration3.enableClientTelemetry shouldEqual true
    configuration3.clientTelemetryEndpoint shouldEqual Some("SomeEndpoint03")

    configuration.equals(configuration2).shouldEqual(false)
    configuration.equals(configuration3).shouldEqual(false)
    configuration2.equals(configuration3).shouldEqual(false)
  }

  it should "apply applicationName if specified" in {
    val myApp = "myApp"
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.useGatewayMode" -> "true"
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo = "")

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe true
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}|$myApp"
  }

  it should "apply applicationName and spark environment info if specified" in {
    val myApp = "myApp"
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.useGatewayMode" -> "true"
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL
    val sparkEnvironmentInfo = s"sparkenv-${UUID.randomUUID()}"
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe true
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual false
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp"
  }

  it should "allow disabling endpoint rediscovery" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.disableTcpConnectionEndpointRediscovery" -> "true",
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo = "")

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.useGatewayMode shouldBe false
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.disableTcpConnectionEndpointRediscovery shouldEqual true
  }
}
