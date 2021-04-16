// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import org.apache.spark.sql.types.{BooleanType, IntegerType, StringType, StructField, StructType}

class SparkE2EChangeFeedITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
    with CosmosContainerWithRetention
    with CosmosLoggingTrait {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  "spark change feed query (incremental)" can "use default schema" in {
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

    val df = spark.read.format("cosmos.changeFeed").options(cfg).load()
    val rowsArray = df.collect()
    rowsArray should have size 2
    df.schema.equals(
      ChangeFeedTable.defaultIncrementalChangeFeedSchemaForInferenceDisabled) shouldEqual true

    val cfgExplicit = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "false",
      "spark.cosmos.changeFeed.mode" -> "Incremental"
    )

    val dfExplicit = spark.read.format("cosmos.changeFeed").options(cfgExplicit).load()
    val rowsArrayExplicit = dfExplicit.collect()
    rowsArrayExplicit should have size 2
    dfExplicit.schema.equals(
      ChangeFeedTable.defaultIncrementalChangeFeedSchemaForInferenceDisabled) shouldEqual true
  }

  "spark change feed query (incremental)" can "use user provided schema" in {
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

    val customSchema = StructType(Array(
      StructField("id", StringType),
      StructField("name", StringType),
      StructField("type", StringType),
      StructField("age", IntegerType),
      StructField("isAlive", BooleanType)
    ))

    val df = spark.read.schema(customSchema).format("cosmos.changeFeed").options(cfg).load()
    val rowsArray = df.collect()
    rowsArray should have size 2
    df.schema.equals(customSchema) shouldEqual true
  }

  "spark change feed query (full fidelity)" can "use default schema" in {
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
      "spark.cosmos.read.inferSchemaEnabled" -> "false",
      "spark.cosmos.changeFeed.mode" -> "FullFidelity",
      "spark.cosmos.changeFeed.startFrom" -> "NOW"
    )

    val df = spark.read.format("cosmos.changeFeed").options(cfg).load()
    val rowsArray = df.collect()
    rowsArray should have size 0
    df.schema.equals(
      ChangeFeedTable.defaultFullFidelityChangeFeedSchemaForInferenceDisabled) shouldEqual true
  }

  "spark change feed micro batch (incremental)" can "use default schema" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val sinkContainerName = cosmosClient
      .getDatabase(cosmosDatabase)
      .createContainer(s"sink-${UUID.randomUUID().toString}", "/id")
      .block
      .getProperties
      .getId

    val readCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "false",
      "spark.cosmos.read.startFrom" -> "Beginning",
      "spark.cosmos.partitioning.strategy" -> "Restrictive"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> sinkContainerName,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulkEnabled" -> "true"
    )

    val testId = UUID.randomUUID().toString.replace("-", "")

    for (i <- 1 to 5) {
      for (state <- Array(true, false)) {
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's fish")
        objectNode.put("type", "fish")
        objectNode.put("age", 20 + i)
        objectNode.put("isAlive", state)
        objectNode.put("id", UUID.randomUUID().toString)
        container.createItem(objectNode).block()
      }

      val changeFeedDF = spark
        .readStream
        .format("cosmos.changeFeed")
        .options(readCfg)
        .load()
      val microBatchQuery = changeFeedDF
        .writeStream
        .format("cosmos.items")
        .queryName(testId)
        .options(writeCfg)
        .option("checkpointLocation", s"/tmp/$testId/")
        .outputMode("append")
        .start()

      microBatchQuery.processAllAvailable()
      microBatchQuery.stop()
    }

    val validationCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> sinkContainerName,
      "spark.cosmos.read.inferSchemaEnabled" -> "false",
      "spark.cosmos.partitioning.strategy" -> "Restrictive"
    )

    val validationDF = spark
      .read
      .format("cosmos.items")
      .options(validationCfg)
      .load()

    val rowCount = validationDF.count()

    rowCount shouldEqual 10

    validationDF
      .show(truncate = false)
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
