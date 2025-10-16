// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.management.AzureEnvironment
import com.azure.cosmos.changeFeedMetrics.ChangeFeedMetricsTracker
import com.azure.cosmos.{ReadConsistencyStrategy, spark}
import org.apache.spark.sql.connector.read.streaming.ReadLimit

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class CosmosPartitionPlannerSpec extends UnitSpec {
  private[this] val rnd = scala.util.Random

  it should "calculateEndLsn without readLimit" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      enforceNativeTransport = false,
      proactiveConnectionInitialization = None,
      proactiveConnectionInitializationDurationInSeconds = 120,
      httpConnectionPoolSize = 1000,
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      disableTcpConnectionEndpointRediscovery = false,
      preferredRegionsList = Option.empty,
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

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = None
    val latestLsn = 2150
    val startLsn = 2057
    val docCount = 200174
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata1 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      latestLsn,
      startLsn,
      None,
      createdAt,
      lastRetrievedAt)

    val metadata2 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      latestLsn,
      startLsn,
      None,
      createdAt,
      lastRetrievedAt)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata1, metadata2),
      ReadLimit.allAvailable(),
      isChangeFeed = true
    )

    calculate(0).endLsn.get shouldBe latestLsn
  }

  it should "calculateEndLsn should have latestLsn >= startLsn when latestLsn==0 (no continuation)" in {

    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      enforceNativeTransport = false,
      proactiveConnectionInitialization = None,
      proactiveConnectionInitializationDurationInSeconds = 120,
      httpConnectionPoolSize = 1000,
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      disableTcpConnectionEndpointRediscovery = false,
      preferredRegionsList = Option.empty,
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

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = None
    val latestLsn = 0
    val startLsn = 2057
    val docCount = 200174
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata1 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      latestLsn,
      startLsn,
      None,
      createdAt,
      lastRetrievedAt)

    val metadata2 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      latestLsn,
      startLsn,
      None,
      createdAt,
      lastRetrievedAt)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata1, metadata2),
      ReadLimit.allAvailable(),
      isChangeFeed = true
    )

    calculate(0).endLsn.get shouldBe startLsn
  }

  it should "calculateEndLsn should return startLsn when lastLsn < startLsn (possible with replication lag)" in {

    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      enforceNativeTransport = false,
      proactiveConnectionInitialization = None,
      proactiveConnectionInitializationDurationInSeconds = 120,
      httpConnectionPoolSize = 1000,
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      disableTcpConnectionEndpointRediscovery = false,
      preferredRegionsList = Option.empty,
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

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = None
    val latestLsn = 2056
    val startLsn = 2057
    val docCount = 200174
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata1 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      latestLsn,
      startLsn,
      None,
      createdAt,
      lastRetrievedAt)

    val metadata2 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      latestLsn,
      startLsn,
      None,
      createdAt,
      lastRetrievedAt)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata1, metadata2),
      ReadLimit.allAvailable(),
      isChangeFeed = true
    )

    calculate(0).endLsn.get shouldBe startLsn
  }

  it should "calculateEndLsn with readLimit should honor estimated lag" in {

    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      enforceNativeTransport = false,
      proactiveConnectionInitialization = None,
      proactiveConnectionInitializationDurationInSeconds = 120,
      httpConnectionPoolSize = 1000,
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      disableTcpConnectionEndpointRediscovery = false,
      preferredRegionsList = Option.empty,
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

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val maxRows = 10
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata1 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 2150,
      docSizeInKB,
      firstLsn = None,
      latestLsn = 2150,
      startLsn = 2050,
      None,
      createdAt,
      lastRetrievedAt)

    val metadata2 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 2150,
      docSizeInKB,
      firstLsn = Some(0),
      latestLsn = 2150,
      startLsn = 1750,
      None,
      createdAt,
      lastRetrievedAt)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata1, metadata2),
      ReadLimit.maxRows(maxRows),
      isChangeFeed = true
    )

    calculate(0).endLsn.get shouldEqual 2052 // proceeds 2 LSNs
    calculate(1).endLsn.get shouldEqual 1758 // proceeds 8 LSNs
  }

  it should "calculateEndLsn with readLimit should proceed at least 1 LSN when there is any lag" in {

    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      enforceNativeTransport = false,
      proactiveConnectionInitialization = None,
      proactiveConnectionInitializationDurationInSeconds = 120,
      httpConnectionPoolSize = 1000,
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      disableTcpConnectionEndpointRediscovery = false,
      preferredRegionsList = Option.empty,
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

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val maxRows = 10
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata1 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 215000,
      docSizeInKB,
      firstLsn = Some(0),
      latestLsn = 2150,
      startLsn = 2050,
      None,
      createdAt,
      lastRetrievedAt)

    val metadata2 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 215000,
      docSizeInKB,
      firstLsn = None,
      latestLsn = 2150,
      startLsn = 1750,
      None,
      createdAt,
      lastRetrievedAt)

    val metadata3 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 215000,
      docSizeInKB,
      firstLsn = Some(0),
      latestLsn = 2150,
      startLsn = 2150,
      None,
      createdAt,
      lastRetrievedAt)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata1, metadata2, metadata3),
      ReadLimit.maxRows(maxRows),
      isChangeFeed = true
    )

    calculate(0).endLsn.get shouldEqual 2051 // proceeds at least 1 LSN
    calculate(1).endLsn.get shouldEqual 1751 // proceeds at least 1 LSN
    calculate(2).endLsn.get shouldEqual 2150 // no lag, no progress
  }

  it should "calculateEndLsn with readLimit should exceed weightedGap if totalWeighted gap < maxReadLimit" in {

    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      enforceNativeTransport = false,
      proactiveConnectionInitialization = None,
      proactiveConnectionInitializationDurationInSeconds = 120,
      httpConnectionPoolSize = 1000,
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      disableTcpConnectionEndpointRediscovery = false,
      preferredRegionsList = Option.empty,
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

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val maxRows = 10
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata1 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 215,
      docSizeInKB,
      firstLsn = Some(0),
      latestLsn = 2150,
      startLsn = 2100,
      None,
      createdAt,
      lastRetrievedAt)

    val metadata2 = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 215,
      docSizeInKB,
      firstLsn = Some(0),
      latestLsn = 2150,
      startLsn = 2100,
      None,
      createdAt,
      lastRetrievedAt)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata1, metadata2),
      ReadLimit.maxRows(maxRows),
      isChangeFeed = true
    )

    calculate(0).endLsn.get shouldEqual 2150
    calculate(1).endLsn.get shouldEqual 2150
  }

  it should "calculateEndLsn should distribute rate based on metrics with readLimit" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      enforceNativeTransport = false,
      proactiveConnectionInitialization = None,
      proactiveConnectionInitializationDurationInSeconds = 120,
      httpConnectionPoolSize = 1000,
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      disableTcpConnectionEndpointRediscovery = false,
      preferredRegionsList = Option.empty,
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

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val maxRows = 10
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 2150,
      docSizeInKB,
      firstLsn = Some(0),
      latestLsn = 2150,
      startLsn = 2050,
      None,
      createdAt,
      lastRetrievedAt)

    val metricsMap = new ConcurrentHashMap[NormalizedRange, ChangeFeedMetricsTracker]()
    val metricsTracker = ChangeFeedMetricsTracker(0L, normalizedRange)
    // Simulate metrics showing 2 changes per LSN on average
    metricsTracker.track(10, 20)
    metricsMap.put(normalizedRange, metricsTracker)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata),
      ReadLimit.maxRows(maxRows),
      isChangeFeed = true,
      Some(metricsMap)
    )

    // With 2 changes per LSN average from metrics and maxRows=10, should allow 5 LSN progress
    calculate(0).endLsn.get shouldEqual 2055
  }

  it should "calculateEndLsn should handle when no progress is made even with metrics" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      enforceNativeTransport = false,
      proactiveConnectionInitialization = None,
      proactiveConnectionInitializationDurationInSeconds = 120,
      httpConnectionPoolSize = 1000,
      readConsistencyStrategy = ReadConsistencyStrategy.EVENTUAL,
      disableTcpConnectionEndpointRediscovery = false,
      preferredRegionsList = Option.empty,
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

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val maxRows = 10
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      documentCount = 2150,
      docSizeInKB,
      firstLsn = Some(0),
      latestLsn = 2050, // Latest LSN same as start LSN
      startLsn = 2050,
      None,
      createdAt,
      lastRetrievedAt)

    val metricsMap = new ConcurrentHashMap[NormalizedRange, ChangeFeedMetricsTracker]()
    val metricsTracker = ChangeFeedMetricsTracker(0L, normalizedRange)
    metricsTracker.track(2050, 100)
    metricsMap.put(normalizedRange, metricsTracker)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata),
      ReadLimit.maxRows(maxRows),
      isChangeFeed = true,
      Some(metricsMap)
    )

    // Should stay at start LSN since no progress can be made
    calculate(0).endLsn.get shouldEqual 2050
  }
}
