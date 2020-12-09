// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.assertj.core.api.Assertions.assertThat

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import


class SparkE2EConfigResolutionSpec extends IntegrationSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  it can "merge user config with spark config for write" taggedAs (RequiresCosmosEndpoint) in {
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

    val cfg = Map("spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer
    )

    val sparkConfig = new SparkConf()
    sparkConfig.set("spark.cosmos.accountEndpoint", cosmosEndpoint)
    sparkConfig.set("spark.cosmos.accountKey", cosmosMasterKey)

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .config(sparkConfig)
      .getOrCreate()

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import spark.implicits._
    // scalastyle:on underscore.import
    // scalastyle:on import.grouping

    val df = Seq(
      (26, "Iron atomic number")
    ).toDF("number", "word")
    df.printSchema()

    df.write.format("cosmos.items").mode("append").options(cfg).save()

    // verify data is written

    // that the write by spark is visible by the client query
    // wait for a second to allow replication is completed.
    Thread.sleep(1000)

    val results = client.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
      .queryItems("SELECT * FROM r", classOf[ObjectNode])
      .toIterable
      .asScala
      .toArray

    assertThat(results).hasSize(1)
    assertThat(results(0).get("number").asInt()).isEqualTo(26)
    assertThat(results(0).get("word").asText()).isEqualTo("Iron atomic number")

    client.close()
    spark.close()
  }

  it can "merge user config with spark config for query" taggedAs (RequiresCosmosEndpoint) in {
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
    val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
    objectNode.put("word", "Cobalt atomic number")
    objectNode.put("number", 27)
    objectNode.put("id", UUID.randomUUID().toString)
    container.createItem(objectNode).block()
    val cfg = Map("spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer
    )

    val sparkConfig = new SparkConf()
    sparkConfig.set("spark.cosmos.accountEndpoint", cosmosEndpoint)
    sparkConfig.set("spark.cosmos.accountKey", cosmosMasterKey)

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .config(sparkConfig)
      .getOrCreate()

    val df = spark.read.format("cosmos.items").options(cfg).load()
    val rowsArray = df.where("number = '27'").collect()
    rowsArray should have size 1

    val row = rowsArray(0)
    row.getAs[String]("word") shouldEqual "Cobalt atomic number"
    row.getAs[Double]("number") shouldEqual 27

    client.close()
    spark.close()
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
