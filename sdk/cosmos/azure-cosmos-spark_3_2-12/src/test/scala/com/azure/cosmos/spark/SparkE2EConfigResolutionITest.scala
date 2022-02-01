// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

class SparkE2EConfigResolutionITest extends IntegrationSpec with CosmosClient with AutoCleanableCosmosContainer {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  "config resolution" can "merge user config with spark config for write" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

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
      (26, "Iron atomic number", UUID.randomUUID().toString)
    ).toDF("number", "word", "id")
    df.printSchema()

    val options = Map(
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer
    )
    df.write.format("cosmos.oltp").mode("append").options(options).save()

    // verify data is written

    // that the write by spark is visible by the client query
    // wait for a second to allow replication is completed.
    Thread.sleep(1000)

    val results = readAllItems().toArray

    results should have size 1
    results(0).get("number").asInt() shouldEqual 26
    results(0).get("word").asText() shouldEqual "Iron atomic number"

    spark.close()
  }

  it can "merge user config with spark config for query" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY


    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
    objectNode.put("word", "Cobalt atomic number")
    objectNode.put("number", 27)
    objectNode.put("id", UUID.randomUUID().toString)
    container.createItem(objectNode).block()

    val sparkConfig = new SparkConf()
    sparkConfig.set("spark.cosmos.accountEndpoint", cosmosEndpoint)
    sparkConfig.set("spark.cosmos.accountKey", cosmosMasterKey)

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .config(sparkConfig)
      .getOrCreate()

    val options = Map(
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "true"
    )
    val df = spark.read.format("cosmos.oltp").options(options).load()

    val rowsArray = df.where("number = '27'").collect()
    rowsArray should have size 1

    val row = rowsArray(0)
    row.getAs[String]("word") shouldEqual "Cobalt atomic number"
    row.getAs[Double]("number") shouldEqual 27

    spark.close()
  }

  it should "validate config names with 'spark.cosmos.' prefix" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.someTypo" -> "xyz"
    )

    try {
      CosmosConfig.getEffectiveConfig(None, None, userConfig)
      fail("Should throw on invalid config names")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "The config property 'spark.cosmos.someTypo' is invalid. No config setting with this name exists."
    }
  }

  it should "not validate config names without 'spark.cosmos.' prefix" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmoss.someTypo" -> "xyz"
    )

    CosmosConfig.getEffectiveConfig(None, None, userConfig)
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
