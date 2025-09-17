// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.ReadConsistencyStrategy
import com.azure.cosmos.implementation.Configs

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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}".replace("@", " ")
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}".replace("@", " ")
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}".replace("@", " ")
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|${ManagementFactory.getRuntimeMXBean.getName}|$myApp".replace("@", " ")
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp".replace("@", " ")
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp".replace("@", " ")
    configuration.sampledDiagnosticsLoggerConfig.isDefined shouldEqual true
    var sampledDiagnosticsLoggerCfg = configuration.sampledDiagnosticsLoggerConfig.get
    sampledDiagnosticsLoggerCfg.samplingRateMaxCount shouldEqual 9
    sampledDiagnosticsLoggerCfg.samplingRateIntervalInSeconds shouldEqual 4
    sampledDiagnosticsLoggerCfg.thresholdsRequestCharge shouldEqual 978
    sampledDiagnosticsLoggerCfg.thresholdsPointOperationLatencyInMs shouldEqual 300
    sampledDiagnosticsLoggerCfg.thresholdsNonPointOperationLatencyInMs shouldEqual 400
  }

  it should "apply azureMonitor settings" in {
    val myApp = "myApp"
    AzureMonitorConfig.resetForUsageInTest()
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

    AzureMonitorConfig.resetForUsageInTest()
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp".replace("@", " ")
    configuration.azureMonitorConfig.isDefined shouldEqual true
    var azMonCfg = configuration.azureMonitorConfig.get
    azMonCfg.enabled shouldEqual true
    azMonCfg.connectionString shouldEqual "MyAzMonitorTestConnectionString"
    azMonCfg.authEnabled shouldEqual false
    azMonCfg.authConfig shouldEqual None
    azMonCfg.liveMetricsEnabled shouldEqual true
    azMonCfg.samplingRate shouldEqual 0.05f
    azMonCfg.samplingRateMaxCount shouldEqual 10000
    azMonCfg.samplingRateIntervalInSeconds shouldEqual 60

    AzureMonitorConfig.resetForUsageInTest()
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

    AzureMonitorConfig.resetForUsageInTest()
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp".replace("@", " ")
    configuration.azureMonitorConfig.isDefined shouldEqual true
    azMonCfg = configuration.azureMonitorConfig.get
    azMonCfg.enabled shouldEqual true
    azMonCfg.connectionString shouldEqual "MyAzMonitorTestConnectionString"
    azMonCfg.authEnabled shouldEqual true
    azMonCfg.liveMetricsEnabled shouldEqual true
    azMonCfg.samplingRate shouldEqual 0.05f
    azMonCfg.samplingRateMaxCount shouldEqual 10000
    azMonCfg.samplingRateIntervalInSeconds shouldEqual 60
    azMonCfg.authConfig.isDefined shouldEqual true
    val miAuthCfg = azMonCfg.authConfig.get.asInstanceOf[CosmosManagedIdentityAuthConfig]
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
    try {
      configuration = CosmosClientConfiguration(userConfig_All, readConsistencyStrategy, sparkEnvironmentInfo)
      fail("Should have thrown an exception")
    } catch {
      case e: IllegalStateException => e.getMessage.contains("spark.cosmos.diagnostics.azureMonitor.*") shouldEqual true
    }

    AzureMonitorConfig.resetForUsageInTest()

    configuration = CosmosClientConfiguration(userConfig_All, readConsistencyStrategy, sparkEnvironmentInfo)
    configuration.endpoint shouldEqual userConfig_All("spark.cosmos.accountEndpoint")
    configuration.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual userConfig_All("spark.cosmos.accountKey")
    configuration.readConsistencyStrategy shouldEqual readConsistencyStrategy
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp".replace("@", " ")
    azMonCfg = configuration.azureMonitorConfig.get
    azMonCfg.enabled shouldEqual true
    azMonCfg.connectionString shouldEqual "MyAzMonitorTestConnectionString"
    azMonCfg.liveMetricsEnabled shouldEqual false
    azMonCfg.samplingRate shouldEqual 0.5f
    azMonCfg.samplingRateMaxCount shouldEqual 10
    azMonCfg.samplingRateIntervalInSeconds shouldEqual 1
    azMonCfg.authConfig.isDefined shouldEqual true
    azMonCfg.authEnabled shouldEqual true
    val spnAuthCfg = azMonCfg.authConfig.get.asInstanceOf[CosmosServicePrincipalAuthConfig]
    spnAuthCfg.tenantId shouldEqual "SomeTenantId"
    spnAuthCfg.clientId shouldEqual "SomeClientId"
    spnAuthCfg.clientSecret shouldEqual Some("SomeClientSecret")
    azMonCfg.metricCollectionIntervalInSeconds shouldEqual 7
  }

  it should "ignore sampling and diagnostics thresholds unless diagnostics mode is sampled" in {
    val myApp = "myApp"
    AzureMonitorConfig.resetForUsageInTest()
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp".replace("@", " ")
    configuration.sampledDiagnosticsLoggerConfig shouldEqual None
  }

  it should "ignore azure monitor settings unless enabled" in {
    val myApp = "myApp"
    AzureMonitorConfig.resetForUsageInTest()
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
    configuration.applicationName shouldEqual s"${CosmosConstants.userAgentSuffix}|$sparkEnvironmentInfo|${ManagementFactory.getRuntimeMXBean.getName}|$myApp".replace("@", " ")
    configuration.azureMonitorConfig shouldEqual None
  }
}
