// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.connector.read.streaming.ReadLimit

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

class CosmosPartitionPlannerSpec extends UnitSpec {
  private[this] val rnd = scala.util.Random

  it should "calculateEndLsn without readLimit" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
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
      ReadLimit.allAvailable()
    )

    calculate(0).endLsn.get shouldBe latestLsn
  }

  it should "calculateEndLsn should have latestLsn >= startLsn when latestLsn==0 (no continuation)" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
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
      ReadLimit.allAvailable()
    )

    calculate(0).endLsn.get shouldBe startLsn
  }

  it should "calculateEndLsn should return startLsn when lastLsn < startLsn (possible with replication lag)" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
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
      ReadLimit.allAvailable()
    )

    calculate(0).endLsn.get shouldBe startLsn
  }

  it should "calculateEndLsn with readLimit should honor estimated lag" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
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
      ReadLimit.maxRows(maxRows)
    )

    calculate(0).endLsn.get shouldEqual 2052 // proceeds 2 LSNs
    calculate(1).endLsn.get shouldEqual 1758 // proceeds 8 LSNs
  }

  it should "calculateEndLsn with readLimit should proceed at least 1 LSN when there is any lag" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
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
      ReadLimit.maxRows(maxRows)
    )

    calculate(0).endLsn.get shouldEqual 2051 // proceeds at least 1 LSN
    calculate(1).endLsn.get shouldEqual 1751 // proceeds at least 1 LSN
    calculate(2).endLsn.get shouldEqual 2150 // no lag, no progress
  }

  it should "calculateEndLsn with readLimit should exceed weightedGap if totalWeighted gap < maxReadLimit" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      enableClientTelemetry = false,
      disableTcpConnectionEndpointRediscovery = false,
      clientTelemetryEndpoint = None,
      preferredRegionsList = Option.empty)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
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
      ReadLimit.maxRows(maxRows)
    )

    calculate(0).endLsn.get shouldEqual 2150
    calculate(1).endLsn.get shouldEqual 2150
  }
}
