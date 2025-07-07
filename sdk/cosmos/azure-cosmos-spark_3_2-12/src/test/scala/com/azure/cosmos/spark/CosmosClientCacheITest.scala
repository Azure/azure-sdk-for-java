// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.management.AzureEnvironment
import com.azure.cosmos.{CosmosAsyncClient, ReadConsistencyStrategy}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, TestConfigurations}
import com.azure.cosmos.spark.catalog.CosmosCatalogCosmosSDKClient
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.mockito.Mockito.mock

class CosmosClientCacheITest
  extends IntegrationSpec
    with CosmosClient
    with CosmosContainer
    with Spark
    with BasicLoggingTrait {
  //scalastyle:off multiple.string.literals

  private val cosmosEndpoint = TestConfigurations.HOST
  private val cosmosMasterKey = TestConfigurations.MASTER_KEY
  private val userConfigTemplate = Map[String, String](
    "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.accountKey" -> cosmosMasterKey,
    "spark.cosmos.database" -> cosmosDatabase,
    "spark.cosmos.container" -> cosmosContainer
  )
  private val clientConfig = CosmosClientConfiguration(userConfigTemplate, readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL, sparkEnvironmentInfo = "")

  "CosmosClientCache" should "get cached object with same config" in {

    val userConfigs = Array[(String, CosmosClientConfiguration)](
      (
        "SimpleCtor",
        CosmosClientConfiguration(
        Map(
          "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
          "spark.cosmos.accountKey" -> cosmosMasterKey
        ),
          readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
        sparkEnvironmentInfo = "")
      ),
      (
        "StandardCtorWithoutPreferredRegions",
        CosmosClientConfiguration(
          cosmosEndpoint,
          "SampleDatabaseAccountName",
          CosmosMasterKeyAuthConfig(cosmosMasterKey),
          Some("SampleApplicationName"),
          "SampleApplicationName",
          useGatewayMode = true,
          enforceNativeTransport = false,
          proactiveConnectionInitialization = None,
          proactiveConnectionInitializationDurationInSeconds = 120,
          httpConnectionPoolSize = 1000,
          readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
          disableTcpConnectionEndpointRediscovery = false,
          preferredRegionsList = None,
          subscriptionId = None,
          tenantId = None,
          resourceGroupName = None,
          azureEnvironmentEndpoints = AzureEnvironment.AZURE.getEndpoints,
          sparkEnvironmentInfo = "",
          clientBuilderInterceptors = None,
          clientInterceptors = None,
          sampledDiagnosticsLoggerConfig = None,
          azureMonitorConfig = None
        )
      ),
      (
        "StandardCtorWithEmptyPreferredRegions",
        CosmosClientConfiguration(
          cosmosEndpoint,
          "SampleDatabaseAccountName",
          CosmosMasterKeyAuthConfig(cosmosMasterKey),
          Some("SampleApplicationName"),
          "SampleApplicationName",
          useGatewayMode = true,
          enforceNativeTransport = false,
          proactiveConnectionInitialization = None,
          proactiveConnectionInitializationDurationInSeconds = 120,
          httpConnectionPoolSize = 1000,
          readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
          disableTcpConnectionEndpointRediscovery = false,
          preferredRegionsList = Some(Array[String]()),
          subscriptionId = None,
          tenantId = None,
          resourceGroupName = None,
          azureEnvironmentEndpoints = AzureEnvironment.AZURE.getEndpoints,
          sparkEnvironmentInfo = "",
          clientBuilderInterceptors = None,
          clientInterceptors = None,
          sampledDiagnosticsLoggerConfig = None,
          azureMonitorConfig = None
        )
      ),
      (
        "StandardCtorWithOnePreferredRegion",
        CosmosClientConfiguration(
          cosmosEndpoint,
          "SampleDatabaseAccountName",
          CosmosMasterKeyAuthConfig(cosmosMasterKey),
          None,
          "SampleApplicationName",
          useGatewayMode = true,
          enforceNativeTransport = false,
          proactiveConnectionInitialization = None,
          proactiveConnectionInitializationDurationInSeconds = 120,
          httpConnectionPoolSize = 1000,
          readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
          disableTcpConnectionEndpointRediscovery = false,
          preferredRegionsList = Some(Array[String]("North Europe")),
          subscriptionId = None,
          tenantId = None,
          resourceGroupName = None,
          azureEnvironmentEndpoints = AzureEnvironment.AZURE.getEndpoints,
          sparkEnvironmentInfo = "",
          clientBuilderInterceptors = None,
          clientInterceptors = None,
          sampledDiagnosticsLoggerConfig = None,
          azureMonitorConfig = None
        )
      ),
      (
        "StandardCtorWithTwoPreferredRegions",
        CosmosClientConfiguration(
          cosmosEndpoint,
          "SampleDatabaseAccountName",
          CosmosMasterKeyAuthConfig(cosmosMasterKey),
          None,
          "SampleApplicationName",
          useGatewayMode = true,
          enforceNativeTransport = false,
          proactiveConnectionInitialization = None,
          proactiveConnectionInitializationDurationInSeconds = 120,
          httpConnectionPoolSize = 1000,
          readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
          disableTcpConnectionEndpointRediscovery = false,
          preferredRegionsList = Some(Array[String]("North Europe", "West Europe")),
          subscriptionId = None,
          tenantId = None,
          resourceGroupName = None,
          azureEnvironmentEndpoints = AzureEnvironment.AZURE.getEndpoints,
          sparkEnvironmentInfo = "",
          clientBuilderInterceptors = None,
          clientInterceptors = None,
          sampledDiagnosticsLoggerConfig = None,
          azureMonitorConfig = None
        )
      )
    )

    userConfigs.foreach(userConfigPair => {

      val testCaseName = userConfigPair._1
      val userConfig = userConfigPair._2
      val userConfigShallowCopy = CosmosClientConfiguration(
        userConfig.endpoint,
        userConfig.databaseAccountName,
        userConfig.authConfig,
        userConfig.customApplicationNameSuffix,
        userConfig.applicationName,
        userConfig.useGatewayMode,
        userConfig.enforceNativeTransport,
        userConfig.proactiveConnectionInitialization,
        userConfig.proactiveConnectionInitializationDurationInSeconds,
        userConfig.httpConnectionPoolSize,
        userConfig.readConsistencyStrategy,
        disableTcpConnectionEndpointRediscovery = false,
        userConfig.preferredRegionsList match {
          case Some(array) => Some(array.clone())
          case None => None
        },
        userConfig.subscriptionId,
        userConfig.tenantId,
        userConfig.resourceGroupName,
        userConfig.azureEnvironmentEndpoints,
        sparkEnvironmentInfo = "",
        clientBuilderInterceptors = None,
        clientInterceptors = None,
        sampledDiagnosticsLoggerConfig = None,
        azureMonitorConfig = None
      )

      logInfo(s"TestCase: {$testCaseName}")

      Loan(
       List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(userConfig, None, s"$testCaseName-CosmosClientCacheITest-01"))
       ))
        .to(clients => {
          Loan(
           List[Option[CosmosClientCacheItem]](
            Some(CosmosClientCache(userConfigShallowCopy, None, s"$testCaseName-CosmosClientCacheITest-02"))
           ))
           .to(clients2 => {
             clients2.head.get.cosmosClient should be theSameInstanceAs clients.head.get.cosmosClient
             clients2.head.get.sparkCatalogClient.isInstanceOf[CosmosCatalogCosmosSDKClient] should be
             clients.head.get.sparkCatalogClient.isInstanceOf[CosmosCatalogCosmosSDKClient] should be

               val ownerInfo = CosmosClientCache.ownerInformation(userConfig)
             logInfo(s"$testCaseName-OwnerInfo $ownerInfo")
             ownerInfo.contains(s"$testCaseName-CosmosClientCacheITest-01") shouldEqual true
             ownerInfo.contains(s"$testCaseName-CosmosClientCacheITest-02") shouldEqual true
             ownerInfo.contains(s"$testCaseName-CosmosClientCacheITest-03") shouldEqual false
             CosmosClientCache.purge(userConfig)
           })
        })
    })
  }

  it should "return a new instance after purging" in {
    val userConfig = CosmosClientConfiguration(Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey),
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      sparkEnvironmentInfo = "")

    Loan(
     List[Option[CosmosClientCacheItem]](
      Some(CosmosClientCache(userConfig, None, "CosmosClientCacheITest-03"))
     ))
     .to(clients => {
       CosmosClientCache.purge(userConfig)
       Loan(
        List[Option[CosmosClientCacheItem]](
         Some(CosmosClientCache(userConfig, None, "CosmosClientCacheITest-04"))
        ))
        .to(clients2 => {

         clients2.head.get shouldNot be theSameInstanceAs clients.head.get
          CosmosClientCache.purge(userConfig)
        })
     })
  }

  it should "use state during initialization" in {
    val userConfig = CosmosClientConfiguration(Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey
    ), readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL, sparkEnvironmentInfo = "")

    val cosmosClientCacheSnapshot = mock(classOf[CosmosClientMetadataCachesSnapshot])
    Loan(
     List[Option[CosmosClientCacheItem]](
      Some(CosmosClientCache(userConfig, Option(cosmosClientCacheSnapshot), "CosmosClientCacheITest-05"))
     ))
     .to(clients => {
       clients.head.get shouldBe a[CosmosClientCacheItem]
       clients.head.get.cosmosClient shouldBe a[CosmosAsyncClient]
       CosmosClientCache.purge(userConfig)
     })
  }

  it should "purge all Cosmos clients on SparkContext shutdown on driver" in {

    Loan(
     List[Option[CosmosClientCacheItem]](
      Some(CosmosClientCache.apply(clientConfig, None, "CreateDummyClient")),
      Some(CosmosClientCache.apply(clientConfig, None, "CreateDummyClient"))
     ))
     .to(_ => {
     })

    CosmosClientCache.isStillReferenced(clientConfig) shouldEqual true

    val ctx = spark.sparkContext
    logInfo(s"isOnDriver: ${CosmosPredicates.isOnSparkDriver()}")
    logInfo(s"Closing Spark context '${ctx.hashCode}' preemptively from unit test...")
    // closing the SparkContext on the driver should trigger
    // asynchronously purging Cosmos Client instances
    ctx.stop()

    logInfo(s"Immediately afters topping Spark context '${ctx.hashCode}' - " +
      s"still referenced: ${CosmosClientCache.isStillReferenced(clientConfig)}")

    CosmosClientCache.isStillReferenced(clientConfig) shouldEqual false

    resetSpark
  }

}
