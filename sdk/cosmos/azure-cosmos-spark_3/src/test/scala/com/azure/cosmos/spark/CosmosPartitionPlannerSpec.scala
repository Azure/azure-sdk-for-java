// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.management.AzureEnvironment
import com.azure.cosmos.changeFeedMetrics.ChangeFeedMetricsTracker
import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.{ReadConsistencyStrategy, spark}
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.connector.read.streaming.ReadLimit

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.Base64

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class CosmosPartitionPlannerSpec extends UnitSpec {
  private[this] val rnd = scala.util.Random
  private[this] val plannerClientConfig = CosmosClientConfiguration(
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
    azureMonitorConfig = None,
    additionalHeaders = None)
  private[this] val plannerContainerConfig =
    CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString, None)

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
      azureMonitorConfig = None,
      additionalHeaders = None
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
      azureMonitorConfig = None,
      additionalHeaders = None
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
      azureMonitorConfig = None,
      additionalHeaders = None
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
      azureMonitorConfig = None,
      additionalHeaders = None
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
      azureMonitorConfig = None,
      additionalHeaders = None
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
      azureMonitorConfig = None,
      additionalHeaders = None
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
      azureMonitorConfig = None,
      additionalHeaders = None
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
      azureMonitorConfig = None,
      additionalHeaders = None
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

  it should "expand composite latest metadata only for change feed planning" in {
    val ranges = Seq(
      NormalizedRange("", "AA"),
      NormalizedRange("AA", "BB"),
      NormalizedRange("BB", "FF"))
    val state = createChangeFeedState(Seq(
      ranges(0) -> 180L,
      ranges(1) -> 160L,
      ranges(2) -> 130L))
    val metadata = createMetadata(
      NormalizedRange("", "FF"),
      latestLsn = 180L,
      startLsn = 100L,
      fromNowContinuationState = Some(state),
      documentCount = 42L,
      totalDocumentSizeInKB = 84L)

    val itemQueryMetadata = CosmosPartitionPlanner.expandPartitionMetadataByLatestLsn(
      Seq(metadata),
      isChangeFeed = false)
    itemQueryMetadata should contain only (metadata)
    itemQueryMetadata.head should be theSameInstanceAs metadata
    itemQueryMetadata.head.documentCount shouldEqual 42L
    itemQueryMetadata.head.totalDocumentSizeInKB shouldEqual 84L

    val changeFeedMetadata = CosmosPartitionPlanner.expandPartitionMetadataByLatestLsn(
      Seq(metadata),
      isChangeFeed = true)
    changeFeedMetadata.map(m => m.feedRange -> m.latestLsn) should contain theSameElementsInOrderAs
      Seq(ranges(0) -> 180L, ranges(1) -> 160L, ranges(2) -> 130L)
    changeFeedMetadata.map(_.documentCount).sum shouldEqual 0L
    changeFeedMetadata.map(_.totalDocumentSizeInKB).sum shouldEqual 0L
    changeFeedMetadata.foreach(_.firstLsn shouldBe empty)
  }

  it should "exclude out-of-scope split children before allocating ReadMaxRows" in {
    val includedRange = NormalizedRange("", "AA")
    val excludedRange = NormalizedRange("AA", "FF")
    val state = createChangeFeedState(Seq(
      includedRange -> 180L,
      excludedRange -> 1000L))
    val metadata = createMetadata(
      NormalizedRange("", "FF"),
      latestLsn = 180L,
      startLsn = 100L,
      fromNowContinuationState = Some(state))
    val expandedMetadata = CosmosPartitionPlanner.expandPartitionMetadataByLatestLsn(
      Seq(metadata),
      isChangeFeed = true)

    val filteredMetadata = CosmosPartitionPlanner.filterPartitionMetadataByFeedRanges(
      expandedMetadata,
      Some(Array(includedRange)))
    filteredMetadata.map(_.feedRange) should contain only includedRange

    val planned = CosmosPartitionPlanner.calculateEndLsn(
      filteredMetadata.toArray,
      ReadLimit.maxRows(10),
      isChangeFeed = true)
    planned.map(m => m.feedRange -> m.endLsn.get) should contain only (includedRange -> 110L)
  }

  it should "reconcile split metadata and preserve v1 offsets with ReadAllAvailable" in {
    val parentRange = NormalizedRange("", "FF")
    val childRanges = Seq(NormalizedRange("", "AA"), NormalizedRange("AA", "FF"))
    val startState = createChangeFeedState(Seq(parentRange -> 100L))
    val latestState = createChangeFeedState(Seq(childRanges(0) -> 180L, childRanges(1) -> 130L))
    val latestParentMetadata = createMetadata(
      parentRange,
      latestLsn = 180L,
      startLsn = 0L,
      fromNowContinuationState = Some(latestState))
    val latestChildMetadata = CosmosPartitionPlanner.expandPartitionMetadataByLatestLsn(
      Seq(latestParentMetadata),
      isChangeFeed = true)

    val reconciled = CosmosPartitionPlanner.getOrderedPartitionMetadataWithStartLsn(
      startState,
      latestChildMetadata.toArray)
    reconciled.map(m => (m.feedRange, m.startLsn, m.latestLsn)) should contain theSameElementsInOrderAs
      Seq((childRanges(0), 100L, 180L), (childRanges(1), 100L, 130L))

    val planned = CosmosPartitionPlanner.calculateEndLsn(
      reconciled,
      ReadLimit.allAvailable(),
      isChangeFeed = true)
    planned.map(m => m.feedRange -> m.endLsn.get) should contain theSameElementsInOrderAs
      Seq(childRanges(0) -> 180L, childRanges(1) -> 130L)

    val inputPartitions = planned.map(m => CosmosInputPartition(m.feedRange, m.endLsn))
    val latestOffset = CosmosPartitionPlanner.createLatestOffset(
      ChangeFeedOffset(startState, None),
      inputPartitions)
    val persistedOffset = new ObjectMapper().readTree(latestOffset.json())
    persistedOffset.fieldNames().asScala.toSet shouldEqual Set("id", "state", "partitions")
    persistedOffset.get("id").asText() shouldEqual ChangeFeedOffset.V1Identifier
    SparkBridgeImplementationInternal
      .extractContinuationTokensFromChangeFeedStateJson(latestOffset.changeFeedState) should
      contain theSameElementsInOrderAs Seq(childRanges(0) -> 180L, childRanges(1) -> 130L)

    val roundTrippedOffset = ChangeFeedOffset.fromJson(latestOffset.json())
    roundTrippedOffset.inputPartitions.get.map(p => p.feedRange -> p.endLsn.get) should
      contain theSameElementsInOrderAs Seq(childRanges(0) -> 180L, childRanges(1) -> 130L)
  }

  it should "cap divergent child ranges independently with ReadMaxRows" in {
    val parentRange = NormalizedRange("", "FF")
    val childRanges = Seq(NormalizedRange("", "AA"), NormalizedRange("AA", "FF"))
    val startState = createChangeFeedState(Seq(parentRange -> 100L))
    val latestState = createChangeFeedState(Seq(childRanges(0) -> 180L, childRanges(1) -> 130L))
    val latestParentMetadata = createMetadata(
      parentRange,
      latestLsn = 180L,
      startLsn = 0L,
      fromNowContinuationState = Some(latestState))
    val latestChildMetadata = CosmosPartitionPlanner.expandPartitionMetadataByLatestLsn(
      Seq(latestParentMetadata),
      isChangeFeed = true)
    val reconciled = CosmosPartitionPlanner.getOrderedPartitionMetadataWithStartLsn(
      startState,
      latestChildMetadata.toArray)

    val planned = CosmosPartitionPlanner.calculateEndLsn(
      reconciled,
      ReadLimit.maxRows(20),
      isChangeFeed = true)

    planned.map(m => m.feedRange -> m.endLsn.get) should contain theSameElementsInOrderAs
      Seq(childRanges(0) -> 114L, childRanges(1) -> 105L)
    planned.foreach(metadata => {
      metadata.endLsn.get should be > metadata.startLsn
      metadata.endLsn.get should be <= metadata.latestLsn
    })
  }

  it should "reconcile child checkpoints with merged latest metadata" in {
    val parentRange = NormalizedRange("", "FF")
    val childRanges = Seq(NormalizedRange("", "AA"), NormalizedRange("AA", "FF"))
    val startState = createChangeFeedState(Seq(childRanges(0) -> 150L, childRanges(1) -> 100L))
    val mergedMetadata = createMetadata(parentRange, latestLsn = 200L, startLsn = 0L)

    val reconciled = CosmosPartitionPlanner.getOrderedPartitionMetadataWithStartLsn(
      startState,
      Array(mergedMetadata))
    reconciled.map(m => (m.feedRange, m.startLsn, m.latestLsn)) should contain theSameElementsInOrderAs
      Seq((childRanges(0), 150L, 200L), (childRanges(1), 100L, 200L))

    val planned = CosmosPartitionPlanner.calculateEndLsn(
      reconciled,
      ReadLimit.allAvailable(),
      isChangeFeed = true)
    planned.map(m => m.feedRange -> m.endLsn.get) should contain theSameElementsInOrderAs
      Seq(childRanges(0) -> 200L, childRanges(1) -> 200L)
  }

  private[this] def createMetadata(
    feedRange: NormalizedRange,
    latestLsn: Long,
    startLsn: Long,
    fromNowContinuationState: Option[String] = None,
    documentCount: Long = 0L,
    totalDocumentSizeInKB: Long = 0L
  ): PartitionMetadata = {
    val nowEpochMs = Instant.now.toEpochMilli
    new PartitionMetadata(
      Map.empty,
      plannerClientConfig,
      None,
      plannerContainerConfig,
      feedRange,
      documentCount,
      totalDocumentSizeInKB,
      Some(0L),
      latestLsn,
      startLsn,
      None,
      new AtomicLong(nowEpochMs),
      new AtomicLong(nowEpochMs),
      fromNowContinuationState)
  }

  private[this] def createChangeFeedState(
    latestLsns: Seq[(NormalizedRange, Long)]
  ): String = {
    val collectionRid = UUID.randomUUID().toString
    val continuationTokens = latestLsns
      .map(rangeAndLsn =>
        s"""{"token":"\\"${rangeAndLsn._2}\\"","range":""" +
          s"""{"min":"${rangeAndLsn._1.min}","max":"${rangeAndLsn._1.max}"}}""")
      .mkString(",")
    val json = String.format(
      "{\"V\":1," +
        "\"Rid\":\"%s\"," +
        "\"Mode\":\"INCREMENTAL\"," +
        "\"StartFrom\":{\"Type\":\"BEGINNING\"}," +
        "\"Continuation\":%s}",
      collectionRid,
      String.format(
        "{\"V\":1," +
          "\"Rid\":\"%s\"," +
          "\"Continuation\":[%s]," +
          "\"Range\":{\"min\":\"\",\"max\":\"FF\"}}",
        collectionRid,
        continuationTokens))

    Base64.getUrlEncoder.encodeToString(json.getBytes(StandardCharsets.UTF_8))
  }
}
