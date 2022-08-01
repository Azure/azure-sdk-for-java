// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.UUID

class SparkE2EGatewayQueryITest
extends IntegrationSpec
    with SparkWithDropwizardAndSlf4jMetrics
    with CosmosGatewayClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait
    with MetricAssertions {

  val objectMapper = new ObjectMapper()

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  //scalastyle:off file.size.limit
  //scalastyle:off null

  // NOTE: due to some bug in the emulator, sub-range feed range doesn't work
  // "spark.cosmos.read.partitioning.strategy" -> "Restrictive" is added to the query tests
  // to ensure we don't do sub-range feed-range
  // once emulator fixed switch back to default partitioning.
  "spark query" can "basic nested query" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    CosmosClientMetrics.meterRegistry.isDefined shouldEqual true
    val meterRegistry = CosmosClientMetrics.meterRegistry.get

    val id = UUID.randomUUID().toString

    val rawItem = s"""
                     | {
                     |   "id" : "$id",
                     |   "nestedObject" : {
                     |     "prop1" : 5,
                     |     "prop2" : "6"
                     |   }
                     | }
                     |""".stripMargin

    val objectNode = objectMapper.readValue(rawItem, classOf[ObjectNode])

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    container.createItem(objectNode).block()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.useGatewayMode" -> "true",
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where("nestedObject.prop2 = '6'").collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id

    assertMetrics(meterRegistry, "cosmos.client.op.latency", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.req.gw", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.req.rntbd", expectedToFind = false)
    assertMetrics(meterRegistry, "cosmos.client.rntbd", expectedToFind = false)
    assertMetrics(meterRegistry, "cosmos.client.rntbd.addressResolution", expectedToFind = false)
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  //scalastyle:on file.size.limit
  //scalastyle:on null
}
