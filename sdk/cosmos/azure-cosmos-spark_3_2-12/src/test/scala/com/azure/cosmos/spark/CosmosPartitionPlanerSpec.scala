// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.connector.read.streaming.ReadLimit

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

class CosmosPartitionPlanerSpec extends UnitSpec {
  private[this] val rnd = scala.util.Random

  it should "calculateEndLsn without readLimit" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      Option.empty)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
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

  it should "calculateEndLsn with readLimit" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true,
      Option.empty)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val normalizedRange = NormalizedRange(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val docSizeInKB = rnd.nextInt()
    val latestLsn = 2150
    val startLsn = 2057
    val docCount = 200174
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
      docCount,
      docSizeInKB,
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
      latestLsn,
      startLsn,
      None,
      createdAt,
      lastRetrievedAt)

    val calculate = CosmosPartitionPlanner.calculateEndLsn(
      Array[PartitionMetadata](metadata1, metadata2),
      ReadLimit.maxRows(maxRows)
    )

    calculate(0).endLsn.get should not be latestLsn
  }
}
