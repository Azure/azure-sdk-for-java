// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.{Base64, UUID}
import java.util.concurrent.atomic.AtomicLong

class PartitionMetadataSpec extends UnitSpec {
  private[this] val rnd = scala.util.Random

  //scalastyle:off multiple.string.literals
  it should "calculate the correct cache key" in {
    val databaseName = UUID.randomUUID().toString
    val collectionName = UUID.randomUUID().toString
    val feedRange = UUID.randomUUID().toString
    val key = PartitionMetadata.createKey(databaseName, collectionName, feedRange)
    key shouldEqual s"$databaseName|$collectionName|$feedRange"
  }

  it should "create instance with valid parameters via apply" in {

    val clientConfig = CosmosClientConfiguration(
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      UUID.randomUUID().toString,
      useGatewayMode = false,
      useEventualConsistency = true)

    val containerConfig = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
    val latestLsn = rnd.nextInt()
    val feedRange = UUID.randomUUID().toString
    val docCount = rnd.nextInt()
    val docSizeInKB = rnd.nextInt()

    val nowEpochMs = Instant.now.toEpochMilli
    val createdAt = new AtomicLong(nowEpochMs)
    val lastRetrievedAt = new AtomicLong(nowEpochMs)

    val viaCtor = PartitionMetadata(
      clientConfig,
      None,
      containerConfig,
      feedRange,
      docCount,
      docSizeInKB,
      latestLsn,
      createdAt,
      lastRetrievedAt)

    val viaApply = PartitionMetadata(
      clientConfig,
      None,
      containerConfig,
      feedRange,
      docCount,
      docSizeInKB,
      createChangeFeedState(latestLsn))

    viaCtor.cosmosClientConfig should be theSameInstanceAs viaApply.cosmosClientConfig
    viaCtor.cosmosClientConfig should be theSameInstanceAs clientConfig
    viaCtor.cosmosContainerConfig should be theSameInstanceAs viaApply.cosmosContainerConfig
    viaCtor.cosmosContainerConfig should be theSameInstanceAs containerConfig
    viaCtor.feedRange shouldEqual viaApply.feedRange
    viaCtor.feedRange shouldEqual feedRange
    viaCtor.documentCount shouldEqual viaApply.documentCount
    viaCtor.documentCount shouldEqual docCount
    viaCtor.totalDocumentSizeInKB shouldEqual viaApply.totalDocumentSizeInKB
    viaCtor.totalDocumentSizeInKB shouldEqual docSizeInKB
    viaCtor.latestLsn shouldEqual viaApply.latestLsn
    viaCtor.latestLsn shouldEqual latestLsn
    viaCtor.lastUpdated.get should be >= nowEpochMs
    viaCtor.lastUpdated.get shouldEqual viaCtor.lastRetrieved.get
    viaApply.lastUpdated.get should be >= nowEpochMs
    viaApply.lastUpdated.get shouldEqual viaApply.lastRetrieved.get
  }

  private[this] val clientCfg = CosmosClientConfiguration(
    UUID.randomUUID().toString,
    UUID.randomUUID().toString,
    UUID.randomUUID().toString,
    useGatewayMode = false,
    useEventualConsistency = true)

  private[this] val contCfg = CosmosContainerConfig(UUID.randomUUID().toString, UUID.randomUUID().toString)
  private[this] val lLsn = rnd.nextInt()
  private[this] val fr = UUID.randomUUID().toString
  private[this] val dc = rnd.nextInt()
  private[this] val ds = rnd.nextInt()

  private[this] val nowEpochMs = Instant.now.toEpochMilli
  private[this] val cAt = new AtomicLong(nowEpochMs)
  private[this] val lrAt = new AtomicLong(nowEpochMs)

  //scalastyle:off null
  it should "throw due to missing clientConfig" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(null, None, contCfg, fr, dc, ds, lLsn, lrAt, cAt))
  }

  it should "throw due to missing containerConfig" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(clientCfg, None, null, fr, dc, ds, lLsn, lrAt, cAt))
  }

  it should "throw due to missing feedRange" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(clientCfg, None, contCfg, null, dc, ds, lLsn, lrAt, cAt))
  }

  it should "throw due to empty feedRange" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(clientCfg, None, contCfg, "", dc, ds, lLsn, lrAt, cAt))
  }

  it should "throw due to missing lastRetrievedAt" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(clientCfg, None, contCfg, fr, dc, ds, lLsn, null, cAt))
  }

  it should "throw due to missing lastUpdatedAt" in {
    assertThrows[IllegalArgumentException](
      PartitionMetadata(clientCfg, None, contCfg, fr, dc, ds, lLsn, lrAt, null))
  }
  //scalastyle:on null
  //scalastyle:on multiple.string.literals

  private[this] def createChangeFeedState(latestLsn: Long) = {
    val collectionRid = UUID.randomUUID().toString

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
          "\"Continuation\":[" +
          "{\"token\":\"\\\"%s\\\"\",\"range\":{\"min\":\"\",\"max\":\"FF\"}}" +
          "]," +
          "\"PKRangeId\":\"0\"}",
        collectionRid,
        String.valueOf(latestLsn)))

    Base64.getUrlEncoder.encodeToString(json.getBytes(StandardCharsets.UTF_8))
  }
}
