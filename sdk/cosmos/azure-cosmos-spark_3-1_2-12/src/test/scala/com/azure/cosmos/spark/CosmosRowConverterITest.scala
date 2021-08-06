// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import org.apache.spark

class CosmosRowConverterITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  "row converter" can "save raw json array" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val spark = this.getSpark()

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import spark.implicits._
    // scalastyle:on underscore.import
    // scalastyle:on import.grouping

    val jsonStr = """{ "id":"abc", "array": ["abc","def"]}"""
    val testStream = spark.read.json(Seq(jsonStr).toDS)

    testStream
      .write
      .format("cosmos.oltp")
      .options(cfg)
      .mode("Append")
      .save()

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val itemResponse = container.readItem("abc", new PartitionKey("abc"), classOf[ObjectNode]).block()

    val arrayNode = itemResponse.getItem.get("array")
    arrayNode.isArray shouldBe true
    arrayNode.asInstanceOf[ArrayNode] should have size 2
    arrayNode.asInstanceOf[ArrayNode].get(0).asText shouldEqual "abc"
    arrayNode.asInstanceOf[ArrayNode].get(1).asText shouldEqual "def"
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
