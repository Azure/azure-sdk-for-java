// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.UUID

/**
 * End-to-end integration tests for the custom headers (workload-id) feature in the Spark connector.
 *
 * These tests verify that the `spark.cosmos.customHeaders` configuration option correctly flows
 * through the Spark connector pipeline into CosmosClientBuilder.customHeaders(), ensuring that
 * custom HTTP headers (such as x-ms-cosmos-workload-id) are applied to all Cosmos DB operations
 * initiated via Spark DataFrames (reads and writes).
 *
 * Requires the Cosmos DB Emulator running
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

  // Verifies that a Spark DataFrame read operation succeeds when spark.cosmos.customHeaders
  // is configured with a workload-id header. The header should be passed through to the
  // CosmosAsyncClient via CosmosClientBuilder.customHeaders() without affecting read behavior.
  "spark query with customHeaders" can "read items with workload-id header" in {
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
      "spark.cosmos.customHeaders" -> """{"x-ms-cosmos-workload-id": "15"}""",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where(s"id = '$id'").collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
  }

  // Verifies that a Spark DataFrame write operation succeeds when spark.cosmos.customHeaders
  // is configured with a workload-id header. The item is written via Spark and then verified
  // via a direct SDK read to confirm the write was persisted correctly.
  "spark write with customHeaders" can "write items with workload-id header" in {
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
      "spark.cosmos.customHeaders" -> """{"x-ms-cosmos-workload-id": "20"}""",
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

  // Regression test: verifies that Spark read operations continue to work correctly when
  // spark.cosmos.customHeaders is NOT specified. Ensures that the feature addition does not
  // break existing behavior for clients that do not use custom headers.
  "spark operations without customHeaders" can "still succeed" in {
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
