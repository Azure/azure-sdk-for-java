// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.management.AzureEnvironment
import com.azure.cosmos.spark

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import java.util.{Base64, UUID}

class PartitionMetadataSpec extends UnitSpec {
  private[this] val rnd = scala.util.Random

  //scalastyle:off multiple.string.literals
  private[this] val clientCfg = CosmosClientConfiguration(
    UUID.randomUUID().toString,
    UUID.randomUUID().toString,
    CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
    None,
    UUID.randomUUID().toString,
    useGatewayMode = false,
    useEventualConsistency = true,
    enableClientTelemetry = false,
    disableTcpConnectionEndpointRediscovery = false,
    clientTelemetryEndpoint = None,
    preferredRegionsList = Option.empty,
    subscriptionId = None,
    tenantId = None,
    resourceGroupName = None,
    azureEnvironment = AzureEnvironment.AZURE)

  private[this] val contCfg = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
  private[this] val lLsn = rnd.nextInt(10000000) + 10
  private[this] val fLsn = Some(lLsn - 10L)
  private[this] val fr = NormalizedRange("", UUID.randomUUID().toString)
  private[this] val dc = rnd.nextInt()
  private[this] val ds = rnd.nextInt()

  private[this] val nowEpochMs = Instant.now.toEpochMilli
  private[this] val cAt = new AtomicLong(nowEpochMs)
  private[this] val lrAt = new AtomicLong(nowEpochMs)

  it should "calculate the correct cache key" in {
    val databaseName = UUID.randomUUID().toString
    val collectionName = UUID.randomUUID().toString
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val key = PartitionMetadata.createKey(databaseName, collectionName, normalizedRange)
    key shouldEqual s"$databaseName/$collectionName/${normalizedRange.min}-${normalizedRange.max}"
  }

  it should "create instance with valid parameters via apply in incremental mode" in {

    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val latestLsn = rnd.nextInt(10000000) + 1
    val firstLsn = Some(latestLsn - 10L)

    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docCount = rnd.nextInt()
    val docSizeInKB = rnd.nextInt()

    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val viaCtor = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      latestLsn,
      0,
      None,
      createdAt,
      lastRetrievedAt)

    val viaApply = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      createChangeFeedState(latestLsn, "INCREMENTAL"))

    viaCtor.cosmosClientConfig should be theSameInstanceAs viaApply.cosmosClientConfig
    viaCtor.cosmosClientConfig should be theSameInstanceAs clientConfig
    viaCtor.cosmosContainerConfig should be theSameInstanceAs viaApply.cosmosContainerConfig
    viaCtor.cosmosContainerConfig should be theSameInstanceAs containerConfig
    viaCtor.feedRange shouldEqual viaApply.feedRange
    viaCtor.feedRange shouldEqual normalizedRange
    viaCtor.documentCount shouldEqual viaApply.documentCount
    viaCtor.documentCount shouldEqual docCount
    viaCtor.totalDocumentSizeInKB shouldEqual viaApply.totalDocumentSizeInKB
    viaCtor.totalDocumentSizeInKB shouldEqual docSizeInKB
    viaCtor.latestLsn shouldEqual viaApply.latestLsn
    viaCtor.latestLsn shouldEqual latestLsn
    viaCtor.firstLsn shouldEqual viaApply.firstLsn
    viaCtor.firstLsn.get shouldEqual latestLsn - 10
    viaCtor.lastUpdated.get should be >= nowEpochMs
    viaCtor.lastUpdated.get shouldEqual viaCtor.lastRetrieved.get
    viaApply.lastUpdated.get should be >= nowEpochMs
    viaApply.lastUpdated.get shouldEqual viaApply.lastRetrieved.get
  }

  it should "create instance with valid parameters via apply in full fidelity mode" in {

    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val latestLsn = rnd.nextInt(10000000) + 1
    val firstLsn = Some(latestLsn - 10L)

    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docCount = rnd.nextInt()
    val docSizeInKB = rnd.nextInt()

    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val viaCtor = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      latestLsn,
      0,
      None,
      createdAt,
      lastRetrievedAt)

    val viaApply = PartitionMetadata(
      Map[String, String](),
      clientConfig,
      None,
      containerConfig,
      normalizedRange,
      docCount,
      docSizeInKB,
      firstLsn,
      createChangeFeedState(latestLsn, "FULL_FIDELITY"))

    viaCtor.cosmosClientConfig should be theSameInstanceAs viaApply.cosmosClientConfig
    viaCtor.cosmosClientConfig should be theSameInstanceAs clientConfig
    viaCtor.cosmosContainerConfig should be theSameInstanceAs viaApply.cosmosContainerConfig
    viaCtor.cosmosContainerConfig should be theSameInstanceAs containerConfig
    viaCtor.feedRange shouldEqual viaApply.feedRange
    viaCtor.feedRange shouldEqual normalizedRange
    viaCtor.documentCount shouldEqual viaApply.documentCount
    viaCtor.documentCount shouldEqual docCount
    viaCtor.totalDocumentSizeInKB shouldEqual viaApply.totalDocumentSizeInKB
    viaCtor.totalDocumentSizeInKB shouldEqual docSizeInKB
    viaCtor.latestLsn shouldEqual viaApply.latestLsn
    viaCtor.latestLsn shouldEqual latestLsn
    viaCtor.firstLsn shouldEqual viaApply.firstLsn
    viaCtor.firstLsn.get shouldEqual latestLsn - 10
    viaCtor.lastUpdated.get should be >= nowEpochMs
    viaCtor.lastUpdated.get shouldEqual viaCtor.lastRetrieved.get
    viaApply.lastUpdated.get should be >= nowEpochMs
    viaApply.lastUpdated.get shouldEqual viaApply.lastRetrieved.get
  }

  it should "withEndLsn honors the new end LSN" in {

    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val latestLsn = rnd.nextInt(10000000) + 10
    val firstLsn = Some(latestLsn - 10L)
    val startLsn = rnd.nextInt()
    val endLsn = rnd.nextInt()
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docCount = rnd.nextInt()
    val docSizeInKB = rnd.nextInt()

    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val original = PartitionMetadata(
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
      Some(endLsn),
      createdAt,
      lastRetrievedAt)

    original.latestLsn shouldEqual latestLsn
    original.firstLsn shouldEqual firstLsn
    original.startLsn shouldEqual startLsn
    original.endLsn shouldEqual Some(endLsn)

    val newEndLsn = rnd.nextInt()
    val withNewEndLsn = original.withEndLsn(newEndLsn)
    withNewEndLsn.latestLsn shouldEqual latestLsn
    withNewEndLsn.firstLsn shouldEqual firstLsn
    withNewEndLsn.startLsn shouldEqual startLsn
    withNewEndLsn.endLsn shouldEqual Some(newEndLsn)
  }

  it should "clone the meta data for a new sub range" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val latestLsn = rnd.nextInt(10000000) + 10
    val firstLsn = Some(latestLsn - 10L)
    val startLsn = rnd.nextInt()
    val endLsn = rnd.nextInt()
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docCount = rnd.nextInt()
    val docSizeInKB = rnd.nextInt()

    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val original = PartitionMetadata(
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
      Some(endLsn),
      createdAt,
      lastRetrievedAt)

    val newRange = NormalizedRange("AA", "AB")
    val newStartLsn = rnd.nextInt()

    val cloned = original.cloneForSubRange(newRange, newStartLsn)
    cloned.feedRange shouldEqual newRange
    cloned.startLsn shouldEqual newStartLsn
  }

  it should "calculate weighted gap when document count per LSN is > 1" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = Some(10L)
    val latestLsn = 2160
    val startLsn = 2067
    val docCount = 200174
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata = PartitionMetadata(
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

    val gap = metadata.getWeightedLsnGap
    gap shouldBe (docCount.toDouble / (latestLsn - firstLsn.get) * (latestLsn - startLsn)).toLong
  }

  it should "weighted gap should be at least 1" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = Some(10L)
    val latestLsn = 2160
    val startLsn = 2159
    val docCount = 200
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata = PartitionMetadata(
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

    val gap = metadata.getWeightedLsnGap
    gap shouldBe 1
  }

  it should "calculate weighted gap when document count per LSN is < 1" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = Some(10L)
    val latestLsn = 2160
    val startLsn = 2067
    val docCount = 500
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata = PartitionMetadata(
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

    val gap = metadata.getWeightedLsnGap
    gap shouldBe (docCount.toDouble / (latestLsn - firstLsn.get) * (latestLsn - startLsn)).toLong
  }

  it should "calculate weighted gap when latestLsn==startLsn" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = Some(10L)
    val latestLsn = 2160
    val startLsn = 2160
    val docCount = 5000
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata = PartitionMetadata(
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

    val gap = metadata.getWeightedLsnGap
    gap shouldBe 0
  }

  it should "calculate avg. document count per LSN correctly when there are no documents" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = Some(10L)
    val latestLsn = 2160
    val startLsn = 2067
    val docCount = 0
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val metadata = PartitionMetadata(
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

    val gap = metadata.getAvgItemsPerLsn
    gap shouldBe 1d
  }

  it should "calculate avg. document count per LSN correctly" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = Some(10L)
    val latestLsn = 2160
    val startLsn = 2060
    var docCount = 21500
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    var metadata = PartitionMetadata(
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

    metadata.getAvgItemsPerLsn shouldBe 10d

    docCount = 215
    metadata = PartitionMetadata(
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

    metadata.getAvgItemsPerLsn shouldBe 0.1d
  }

  it should "calculate avg. document count per LSN correctly when firstLsn was empty" in {
    val clientConfig = spark.CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      CosmosMasterKeyAuthConfig(UUID.randomUUID().toString),
      None,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty,
      subscriptionId = None,
      tenantId = None,
      resourceGroupName = None,
      azureEnvironment = AzureEnvironment.AZURE)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val firstLsn = None
    val latestLsn = 2160
    val startLsn = 2067
    var docCount = 21600
    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    var metadata = PartitionMetadata(
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

    metadata.getAvgItemsPerLsn shouldBe 10d

    docCount = 216
    metadata = PartitionMetadata(
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

    metadata.getAvgItemsPerLsn shouldBe 1d
  }

  //scalastyle:off null
  it should "throw due to missing clientConfig" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(Map[String, String](), null, None, contCfg, fr, dc, ds, fLsn, lLsn, 0, None, lrAt, cAt))
  }

  it should "throw due to missing containerConfig" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(Map[String, String](), clientCfg, None, null, fr, dc, ds, fLsn, lLsn, 0, None, lrAt, cAt))
  }

  it should "throw due to missing feedRange" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(Map[String, String](), clientCfg, None, contCfg, null, dc, ds, fLsn, lLsn, 0, None, lrAt, cAt))
  }

  it should "throw due to missing lastRetrievedAt" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(Map[String, String](), clientCfg, None, contCfg, fr, dc, ds, fLsn, lLsn, 0, None, null, cAt))
  }

  it should "throw due to missing lastUpdatedAt" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(Map[String, String](), clientCfg, None, contCfg, fr, dc, ds, fLsn, lLsn, 0, None, lrAt, null))
  }
  //scalastyle:on null
  //scalastyle:on multiple.string.literals

  private[this] def createChangeFeedState(latestLsn: Long, mode: String) = {
    val collectionRid = UUID.randomUUID().toString

    val json = String.format(
      "{\"V\":1," +
        "\"Rid\":\"%s\"," +
        "\"Mode\":\"%s\"," +
        "\"StartFrom\":{\"Type\":\"BEGINNING\"}," +
        "\"Continuation\":%s}",
      collectionRid,
      mode,
      String.format(
        "{\"V\":1," +
          "\"Rid\":\"%s\"," +
          "\"Continuation\":[" +
          "{\"token\":\"\\\"%s\\\"\",\"range\":{\"min\":\"\",\"max\":\"FF\"}}" +
          "]," +
          "\"PKRangeId\":\"0\"}",
        collectionRid,
        String.valueOf(latestLsn)))

    Base64.getUrlEncoder.encodeToString(json.getBytes(StandardCharsets.UTF_8))
  }
}
