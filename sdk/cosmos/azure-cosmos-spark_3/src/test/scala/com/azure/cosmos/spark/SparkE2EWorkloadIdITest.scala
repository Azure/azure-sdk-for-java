// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.UUID

/**
 * Integration tests (smoke tests) for the additional headers (workload-id) feature in the Spark connector.
 * These are smoke tests — they verify that Spark DataFrame read and write operations succeed
 * (no errors, correct data) when the `spark.cosmos.additionalHeaders` configuration is set.
 * They do NOT assert that the workload-id header is actually present on the wire request.
 * Wire-level header propagation is verified by:
 *   - Java SDK unit tests: RxGatewayStoreModelTest, GatewayAddressCacheTest
 *   - Java SDK integration tests: WorkloadIdE2ETests (interceptor-based wire assertions)
 *
 * Test cases:
 *   1. Read with workload-id header — Spark read succeeds, correct item returned
 *   2. Write with workload-id header — Spark write succeeds, item verified via SDK read-back
 *   3. No additionalHeaders (regression) — operations succeed without the config set
 */
class SparkE2EWorkloadIdITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait {

  val objectMapper = new ObjectMapper()

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  //scalastyle:off null

  // Integration smoke test #1: Spark read with workload-id header.
  // Creates an item via SDK, then reads it back via Spark DataFrame with
  // spark.cosmos.additionalHeaders set to {"x-ms-cosmos-workload-id": "15"}.
  // Verifies the read succeeds and returns the correct item.
  // This proves the header flows through the Spark config pipeline without causing errors.
  "spark query with additionalHeaders" can "read items with workload-id header" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString
    val rawItem =
      s"""
         | {
         |   "id" : "$id",
         |   "name" : "testItem"
         | }
         |""".stripMargin

    val objectNode = objectMapper.readValue(rawItem, classOf[ObjectNode])

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    container.createItem(objectNode).block()

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.additionalHeaders" -> """{"x-ms-cosmos-workload-id": "15"}""",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where(s"id = '$id'").collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
  }

  // Integration smoke test #2: Spark write with workload-id header.
  // Writes an item via Spark DataFrame with spark.cosmos.additionalHeaders set to
  // {"x-ms-cosmos-workload-id": "20"}, then reads it back via SDK to confirm
  // write was persisted correctly.
  // This proves the header flows through the Spark write pipeline without causing errors.
  "spark write with additionalHeaders" can "write items with workload-id header" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString
    val rawItem =
      s"""
         | {
         |   "id" : "$id",
         |   "name" : "testWriteItem"
         | }
         |""".stripMargin

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.additionalHeaders" -> """{"x-ms-cosmos-workload-id": "20"}""",
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "false",
      "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
    )

    val spark_session = spark
    import spark_session.implicits._
    val df = spark.read.json(Seq(rawItem).toDS())

    df.write.format("cosmos.oltp").options(cfg).mode("Append").save()

    // Verify the item was written by reading it back via the SDK directly
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val readItem = container.readItem(id, new com.azure.cosmos.models.PartitionKey(id), classOf[ObjectNode]).block()
    readItem.getItem.get("id").textValue() shouldEqual id
    readItem.getItem.get("name").textValue() shouldEqual "testWriteItem"
  }

  // Integration smoke test #3: Regression test — no additionalHeaders configured.
  // Verifies that Spark read operations continue to work correctly when
  // spark.cosmos.additionalHeaders is NOT specified in the config map.
  // This ensures the feature addition does not break existing Spark jobs
  // that don't use additional headers.
  "spark operations without additionalHeaders" can "still succeed" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString
    val rawItem =
      s"""
         | {
         |   "id" : "$id",
         |   "name" : "noHeadersItem"
         | }
         |""".stripMargin

    val objectNode = objectMapper.readValue(rawItem, classOf[ObjectNode])
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    container.createItem(objectNode).block()

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where(s"id = '$id'").collect()
    rowsArray should have size 1
    rowsArray(0).getAs[String]("id") shouldEqual id
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  //scalastyle:on null
}
