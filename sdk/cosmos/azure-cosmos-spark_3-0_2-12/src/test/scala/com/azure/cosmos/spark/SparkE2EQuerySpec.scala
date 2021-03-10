// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.implementation.{TestConfigurations, Utils}

class SparkE2EQuerySpec extends IntegrationSpec with Spark with CosmosClient with AutoCleanableCosmosContainer {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  // NOTE: due to some bug in the emulator, sub-range feed range doesn't work
  // "spark.cosmos.partitioning.strategy" -> "Restrictive" is added to the query tests
  // to ensure we don't do sub-range feed-range
  // once emulator fixed switch back to default partitioning.

  "spark query" can "use user provided schema" taggedAs RequiresCosmosEndpoint in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // assert that there is more than one range to ensure the test really is testing the parallelization of work
    container.getFeedRanges.block().size() should be > 1

    for (age <- 1 to 20) {
      for (state <- Array(true, false)) {
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", age)
        objectNode.put("isAlive", state)
        objectNode.put("id", UUID.randomUUID().toString)
        container.createItem(objectNode).block()
      }
    }

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.partitioning.strategy" -> "Restrictive"
    )

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
    val rowsArray = df.where("isAlive = 'true' and type = 'cat'").orderBy("age").collect()
    rowsArray should have size 20

    for (index <- 0 until rowsArray.length) {
      val row = rowsArray(index)
      row.getAs[String]("name") shouldEqual "Shrodigner's cat"
      row.getAs[String]("type") shouldEqual "cat"
      row.getAs[Integer]("age") shouldEqual index + 1
      row.getAs[Boolean]("isAlive") shouldEqual true
    }
  }

  "spark query" can "use schema inference with system properties" taggedAs RequiresCosmosEndpoint in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's dog")
      objectNode.put("type", "dog")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    val cfgWithInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "true",
      "spark.cosmos.read.inferSchemaIncludeSystemProperties" -> "true",
      "spark.cosmos.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.items").options(cfgWithInference).load()
    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'dog'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's dog"
    rowWithInference.getAs[String]("type") shouldEqual "dog"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe true
  }

  "spark query" can "use schema inference with no system properties" taggedAs RequiresCosmosEndpoint in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's dog")
      objectNode.put("type", "dog")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    val cfgWithInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "true",
      "spark.cosmos.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.items").options(cfgWithInference).load()
    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'dog'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's dog"
    rowWithInference.getAs[String]("type") shouldEqual "dog"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe false
  }

  "spark query" can "use schema inference with custom query" taggedAs RequiresCosmosEndpoint in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's duck")
      objectNode.put("type", "duck")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    val cfgWithInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "true",
      "spark.cosmos.read.inferSchemaQuery" -> "select TOP 1 c.isAlive, c.type, c.age from c",
      "spark.cosmos.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.items").options(cfgWithInference).load()
    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'duck'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.schema.fields should have size 3
    rowWithInference.getAs[String]("type") shouldEqual "duck"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true
  }

  "spark query" can "use schema inference with custom query an system properties" taggedAs RequiresCosmosEndpoint in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's duck")
      objectNode.put("type", "duck")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    val cfgWithInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "true",
      "spark.cosmos.read.inferSchemaQuery" -> "select TOP 1 * from c",
      "spark.cosmos.read.inferSchemaIncludeSystemProperties" -> "true",
      "spark.cosmos.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.items").options(cfgWithInference).load()
    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'duck'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("type") shouldEqual "duck"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe true
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
