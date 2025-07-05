// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.ReadConsistencyStrategy
import com.azure.cosmos.implementation.Configs
import org.apache.logging.log4j.Level

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
      "spark.cosmos.diagnostics.sampling.maxCount" -> "9",
      "spark.cosmos.diagnostics.sampling.intervalInSeconds" -> "4",
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

  it should "apply azureMonitor settings" in {
    val myApp = "myApp"
    val userConfig_Invalid = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.diagnostics.azureMonitor.enabled" -> "true",
    )

    val readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL
    val sparkEnvironmentInfo = s"sparkenv-${UUID.randomUUID()}"

    val originalAppInsightsConnectionStringProperty = Option(
      System.getProperty(Configs.APPLICATIONINSIGHTS_CONNECTION_STRING))
    val originalAppInsightsConnectionStringVariable = Option(
      System.getenv(Configs.APPLICATIONINSIGHTS_CONNECTION_STRING_VARIABLE))

    if (originalAppInsightsConnectionStringVariable.isDefined) {
      cancel(
        s"This test is only applicable when no env variable "
        + s"'${Configs.APPLICATIONINSIGHTS_CONNECTION_STRING_VARIABLE}' is defined.")
    }

    try {
      try {
        CosmosClientConfiguration(userConfig_Invalid, readConsistencyStrategy, sparkEnvironmentInfo)
        fail("Should never have reached here")
      } catch {
        case _: IllegalArgumentException =>
      }
    } finally {
      if (originalAppInsightsConnectionStringProperty.isDefined) {
        System.setProperty(
          Configs.APPLICATIONINSIGHTS_CONNECTION_STRING,
          originalAppInsightsConnectionStringProperty.get)
      } else {
        System.clearProperty(Configs.APPLICATIONINSIGHTS_CONNECTION_STRING)
      }
    }

    val userConfig_Minimal = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.diagnostics.azureMonitor.enabled" -> "true",
      "spark.cosmos.diagnostics.azureMonitor.connectionString" -> "MyAzMonitorTestConnectionString",
    )

    var configuration = CosmosClientConfiguration(userConfig_Minimal, readConsistencyStrategy, sparkEnvironmentInfo)

    configuration.endpoint shouldEqual userConfig_Minimal("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig_Minimal("spark.cosmos.accountKey")
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp"
    configuration.azureMonitorOpenTelemetryEnabled shouldEqual Some(true)
    configuration.azureMonitorConnectionString shouldEqual Some("MyAzMonitorTestConnectionString")
    configuration.azureMonitorAuthEnabled shouldEqual Some(false)
    configuration.azureMonitorAuthConfig shouldEqual None
    configuration.azureMonitorLiveMetricsEnabled shouldEqual Some(true)
    configuration.azureMonitorSamplingRate shouldEqual Some(0.05f)
    configuration.azureMonitorSamplingRateMaxCount shouldEqual Some(100000)
    configuration.azureMonitorSamplingRateIntervalInSeconds shouldEqual Some(60)

    val userConfig_WithAuth_Invalid = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.diagnostics.azureMonitor.enabled" -> "true",
      "spark.cosmos.diagnostics.azureMonitor.connectionString" -> "MyAzMonitorTestConnectionString",
      "spark.cosmos.diagnostics.azureMonitor.auth.enabled" -> "true"
    )

    try {
      CosmosClientConfiguration(userConfig_WithAuth_Invalid, readConsistencyStrategy, sparkEnvironmentInfo)
      fail("Should never have reached here")
    } catch {
      case e: AssertionError => e.getMessage shouldEqual "assertion failed: Parameter 'spark.cosmos.account.tenantId' is missing."
    }

    val userConfig_WithAuth_ManagedIdentity = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.diagnostics.azureMonitor.enabled" -> "true",
      "spark.cosmos.diagnostics.azureMonitor.connectionString" -> "MyAzMonitorTestConnectionString",
      "spark.cosmos.diagnostics.azureMonitor.auth.enabled" -> "true",
      // Implicit "spark.cosmos.diagnostics.azureMonitor.auth.type" -> "ManagedIdentity",
      "spark.cosmos.account.tenantId" -> "SomeTenantId",
    )

    configuration = CosmosClientConfiguration(userConfig_WithAuth_ManagedIdentity, readConsistencyStrategy, sparkEnvironmentInfo)

    configuration.endpoint shouldEqual userConfig_WithAuth_ManagedIdentity("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig_WithAuth_ManagedIdentity("spark.cosmos.accountKey")
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp"
    configuration.azureMonitorOpenTelemetryEnabled shouldEqual Some(true)
    configuration.azureMonitorConnectionString shouldEqual Some("MyAzMonitorTestConnectionString")
    configuration.azureMonitorAuthEnabled shouldEqual Some(true)
    configuration.azureMonitorLiveMetricsEnabled shouldEqual Some(true)
    configuration.azureMonitorSamplingRate shouldEqual Some(0.05f)
    configuration.azureMonitorSamplingRateMaxCount shouldEqual Some(100000)
    configuration.azureMonitorSamplingRateIntervalInSeconds shouldEqual Some(60)
    configuration.azureMonitorAuthConfig.isDefined shouldEqual true
    val miAuthCfg = configuration.azureMonitorAuthConfig.get.asInstanceOf[CosmosManagedIdentityAuthConfig]
    miAuthCfg.tenantId shouldEqual "SomeTenantId"

    val userConfig_All = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.diagnostics.azureMonitor.enabled" -> "true",
      "spark.cosmos.diagnostics.azureMonitor.connectionString" -> "MyAzMonitorTestConnectionString",
      "spark.cosmos.diagnostics.azureMonitor.auth.enabled" -> "true",
      "spark.cosmos.diagnostics.azureMonitor.auth.type" -> "servicePRINcipal",
      "spark.cosmos.account.tenantId" -> "SomeTenantId",
      "spark.cosmos.auth.aad.clientId" -> "SomeClientId",
      "spark.cosmos.auth.aad.clientSecret" -> "SomeClientSecret",
      "spark.cosmos.diagnostics.azureMonitor.liveMetrics.enabled" -> "false",
      "spark.cosmos.diagnostics.azureMonitor.sampling.rate" -> "0.5",
      "spark.cosmos.diagnostics.azureMonitor.sampling.maxCount" -> "10",
      "spark.cosmos.diagnostics.azureMonitor.sampling.intervalInSeconds" -> "1",
      "spark.cosmos.diagnostics.azureMonitor.metrics.intervalInSeconds" -> "7",
      "spark.cosmos.diagnostics.azureMonitor.log.level" -> "Debug",
      "spark.cosmos.diagnostics.azureMonitor.log.sampling.maxCount" -> "33",
      "spark.cosmos.diagnostics.azureMonitor.log.sampling.intervalInSeconds" -> "3"
    )
    configuration = CosmosClientConfiguration(userConfig_All, readConsistencyStrategy, sparkEnvironmentInfo)

    configuration.endpoint shouldEqual userConfig_All("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig_All("spark.cosmos.accountKey")
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp"
    configuration.azureMonitorOpenTelemetryEnabled shouldEqual Some(true)
    configuration.azureMonitorConnectionString shouldEqual Some("MyAzMonitorTestConnectionString")
    configuration.azureMonitorLiveMetricsEnabled shouldEqual Some(false)
    configuration.azureMonitorSamplingRate shouldEqual Some(0.5f)
    configuration.azureMonitorSamplingRateMaxCount shouldEqual Some(10)
    configuration.azureMonitorSamplingRateIntervalInSeconds shouldEqual Some(1)
    configuration.azureMonitorAuthConfig.isDefined shouldEqual true
    configuration.azureMonitorAuthEnabled shouldEqual Some(true)
    configuration.azureMonitorAuthConfig.isDefined shouldEqual true
    val spnAuthCfg = configuration.azureMonitorAuthConfig.get.asInstanceOf[CosmosServicePrincipalAuthConfig]
    spnAuthCfg.tenantId shouldEqual "SomeTenantId"
    spnAuthCfg.clientId shouldEqual "SomeClientId"
    spnAuthCfg.clientSecret shouldEqual Some("SomeClientSecret")
    configuration.azureMonitorMetricCollectionIntervalInSeconds shouldEqual Some(7)
    configuration.azureMonitorLogLevel shouldEqual Some(Level.DEBUG)
    configuration.azureMonitorLogSamplingMaxCount shouldEqual Some(33)
    configuration.azureMonitorLogSamplingIntervalInSeconds shouldEqual Some(3)
  }

  it should "ignore sampling and diagnostics thresholds unless diagnostics mode is sampled" in {
    val myApp = "myApp"
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      "spark.cosmos.diagnostics" -> "simple",
      "spark.cosmos.diagnostics.sampling.maxCount" -> "9",
      "spark.cosmos.diagnostics.sampling.intervalInSeconds" -> "4",
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

  it should "ignore azure monitor settings unless enabled" in {
    val myApp = "myApp"
    val readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL
    val sparkEnvironmentInfo = s"sparkenv-${UUID.randomUUID()}"

    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://localhost:8081",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> myApp,
      // Implicit "spark.cosmos.diagnostics.azureMonitor.enabled" -> "false",
      "spark.cosmos.diagnostics.azureMonitor.connectionString" -> "MyAzMonitorTestConnectionString",
      "spark.cosmos.diagnostics.azureMonitor.auth.enabled" -> "true",
      "spark.cosmos.diagnostics.azureMonitor.auth.type" -> "servicePRINcipal",
      "spark.cosmos.account.tenantId" -> "SomeTenantId",
      "spark.cosmos.auth.aad.clientId" -> "SomeClientId",
      "spark.cosmos.auth.aad.clientSecret" -> "SomeClientSecret",
      "spark.cosmos.diagnostics.azureMonitor.liveMetrics.enabled" -> "false",
      "spark.cosmos.diagnostics.azureMonitor.sampling.rate" -> "0.5",
      "spark.cosmos.diagnostics.azureMonitor.sampling.maxCount" -> "10",
      "spark.cosmos.diagnostics.azureMonitor.sampling.intervalInSeconds" -> "1"
    )

    val configuration = CosmosClientConfiguration(userConfig, readConsistencyStrategy, sparkEnvironmentInfo)

    configuration.endpoint shouldEqual userConfig("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig("spark.cosmos.accountKey")
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp"
    configuration.azureMonitorOpenTelemetryEnabled shouldEqual None
    configuration.azureMonitorConnectionString shouldEqual None
    configuration.azureMonitorAuthEnabled shouldEqual None
    configuration.azureMonitorAuthConfig shouldEqual None
    configuration.azureMonitorLiveMetricsEnabled shouldEqual None
    configuration.azureMonitorSamplingRate shouldEqual None
    configuration.azureMonitorSamplingRateMaxCount shouldEqual None
    configuration.azureMonitorSamplingRateIntervalInSeconds shouldEqual None
  }
}
