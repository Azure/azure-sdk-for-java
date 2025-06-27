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

  it should "apply sampled diagnostics settings" in {
    val myApp = "myApp"
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.diagnostics" -> "saMpled",
      "spark.cosmos.diagnostics.samplings.maxCount" -> "9",
      "spark.cosmos.diagnostics.samplings.intervalInSeconds" -> "4",
      "spark.cosmos.diagnostics.thresholds.requestCharge" -> "978",
      "spark.cosmos.diagnostics.thresholds.latency.pointOperationInMs" -> "300",
      "spark.cosmos.diagnostics.thresholds.latency.nonPointOperationInMs" -> "400",
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL
    val sparkEnvironmentInfo = s"sparkenv-${UUID.randomUUID()}"
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp"
    configuration.samplingRateMaxCount shouldEqual Some(9)
    configuration.samplingRateIntervalInSeconds shouldEqual Some(4)
    configuration.thresholdsRequestCharge shouldEqual Some(978)
    configuration.thresholdsPointOperationLatencyInMs shouldEqual Some(300)
    configuration.thresholdsNonPointOperationLatencyInMs shouldEqual Some(400)
  }

  it should "ignore sampling and diagnostics thresholds unless diagnostics mode is sampled" in {
    val myApp = "myApp"
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.diagnostics" -> "simple",
      "spark.cosmos.diagnostics.samplings.maxCount" -> "9",
      "spark.cosmos.diagnostics.samplings.intervalInSeconds" -> "4",
      "spark.cosmos.diagnostics.thresholds.requestCharge" -> "978",
      "spark.cosmos.diagnostics.thresholds.latency.pointOperationInMs" -> "300",
      "spark.cosmos.diagnostics.thresholds.latency.nonPointOperationInMs" -> "400",
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL
    val sparkEnvironmentInfo = s"sparkenv-${UUID.randomUUID()}"
    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp"
    configuration.samplingRateMaxCount shouldEqual None
    configuration.samplingRateIntervalInSeconds shouldEqual None
    configuration.thresholdsRequestCharge shouldEqual None
    configuration.thresholdsPointOperationLatencyInMs shouldEqual None
    configuration.thresholdsNonPointOperationLatencyInMs shouldEqual None
  }

}
