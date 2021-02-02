// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.implementation.{TestConfigurations, Utils}

class SparkE2EChangeFeedSpec extends IntegrationSpec with Spark with CosmosClient with CosmosContainer {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  "spark change feed query" can "use default schema" taggedAs RequiresCosmosEndpoint in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }
    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "false"
    )

    val df = spark.read.format("cosmos.changeFeed.items").options(cfg).load()
    val rowsArray = df.collect()
    rowsArray should have size 2
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
