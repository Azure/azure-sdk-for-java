// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType
import org.assertj.core.api.Assertions.assertThat
import org.codehaus.jackson.map.ObjectMapper
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class SparkE2EQuerySpec extends IntegrationSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  it can "query cosmos and use user provided schema" taggedAs (RequiresCosmosEndpoint) in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val cosmosDatabase = "testDB"
    val cosmosContainer = UUID.randomUUID().toString

    val client = new CosmosClientBuilder()
      .endpoint(cosmosEndpoint)
      .key(cosmosMasterKey)
      .buildAsyncClient()

    client.createDatabaseIfNotExists(cosmosDatabase).block()
    client.getDatabase(cosmosDatabase).createContainerIfNotExists(cosmosContainer, "/id").block()

    val container = client.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }
    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer
    )

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import org.apache.spark.sql.types._
    // scalastyle:on underscore.import
    // scalastyle:on import.grouping

    val customSchema = StructType(Array(
      StructField("id", StringType),
      StructField("name", StringType),
      StructField("type", StringType),
      StructField("age", IntegerType),
      StructField("isAlive", BooleanType)
    ))

    val df = spark.read.schema(customSchema).format("cosmos.items").options(cfg).load()
    val rowsArray = df.where("isAlive = 'true'").collect()
    rowsArray should have size 1

    val row = rowsArray(0)
    row.getAs[String]("name") shouldEqual "Shrodigner's cat"
    row.getAs[String]("type") shouldEqual "cat"
    row.getAs[Integer]("age") shouldEqual 20
    row.getAs[Boolean]("isAlive") shouldEqual true

    client.close()
    spark.close()
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
