// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.{CosmosItemResponse, PartitionKey}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.sql.Timestamp
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

class SparkE2EQueryITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait {

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

    val id = UUID.randomUUID().toString

    val rawItem = s"""
      | {
      |   "id" : "${id}",
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
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where("nestedObject.prop2 = '6'").collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
  }

  private def insertDummyValue() : Unit = {
    val id = UUID.randomUUID().toString

    val rawItem = s"""
                     | {
                     |   "id" : "${id}",
                     |   "nestedObject" : {
                     |     "prop1" : 5,
                     |     "prop2" : "6"
                     |   }
                     | }
                     |""".stripMargin

    val objectNode = objectMapper.readValue(rawItem, classOf[ObjectNode])

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    container.createItem(objectNode).block()
  }

  "spark query" can "support StringStartsWith" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString

    val rawItem = s"""
                     | {
                     |   "id" : "${id}",
                     |   "nestedObject" : {
                     |     "prop1" : 5,
                     |     "prop2" : "6"
                     |   }
                     | }
                     |""".stripMargin

    val objectNode = objectMapper.readValue(rawItem, classOf[ObjectNode])

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    container.createItem(objectNode).block()

    // insert a dummy value
    insertDummyValue()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.maxItemCount" -> "1",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.filter(df.col("id").startsWith(id.substring(0, id.length / 2))).collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
  }

  "spark query" can "support StringEndswith" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString

    val rawItem = s"""
                     | {
                     |   "id" : "${id}",
                     |   "nestedObject" : {
                     |     "prop1" : 5,
                     |     "prop2" : "6"
                     |   }
                     | }
                     |""".stripMargin

    val objectNode = objectMapper.readValue(rawItem, classOf[ObjectNode])

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    container.createItem(objectNode).block()

    // insert a dummy value
    insertDummyValue()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.maxItemCount" -> "5",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.filter(df.col("id").endsWith(id.substring(id.length / 2, id.length))).collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
  }

  "spark query" can "support StringContains" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString

    val rawItem = s"""
                     | {
                     |   "id" : "${id}",
                     |   "nestedObject" : {
                     |     "prop1" : 5,
                     |     "prop2" : "6"
                     |   }
                     | }
                     |""".stripMargin

    val objectNode = objectMapper.readValue(rawItem, classOf[ObjectNode])

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    container.createItem(objectNode).block()

    // insert a dummy value
    insertDummyValue()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.filter(df.col("id").contains(id.substring(2, id.length - 2))).collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
  }

  "spark query" can "override maxItemCount" in  {
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
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.read.maxItemCount" -> "2",
      "spark.cosmos.diagnostics" -> SimpleFileDiagnosticsProvider.getClass.getName.replace("$", "")
    )

    SimpleFileDiagnosticsProvider.reset()

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

    val df = spark.read.schema(customSchema).format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where("isAlive = 'true' and type = 'cat'").orderBy("age").collect()
    rowsArray should have size 20

    for (index <- 0 until rowsArray.length) {
      val row = rowsArray(index)
      row.getAs[String]("name") shouldEqual "Shrodigner's cat"
      row.getAs[String]("type") shouldEqual "cat"
      row.getAs[Integer]("age") shouldEqual index + 1
      row.getAs[Boolean]("isAlive") shouldEqual true
    }

    // validate from diagnostics that all responses had at most 2 records (instead of the default of up to 100)
    val logger = SimpleFileDiagnosticsProvider.getOrCreateSingletonLoggerInstance(ItemsPartitionReader.getClass)
    val messages = logger.getMessages()
    logger.reset()
    messages should not be null
    messages.size should not be 0
    for ((msg, throwable) <- messages) {
      val itemCountPos = msg.indexOf("itemCount:")
      if (itemCountPos > 0) {
        val startPos = itemCountPos + "itemCount:".length
        val itemCount = msg.substring(startPos, msg.indexOf(",", startPos)).toInt
        itemCount should be <= 2
      }
    }
  }

  "spark query" can "use user provided schema" in  {
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
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
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

    val df = spark.read.schema(customSchema).format("cosmos.oltp").options(cfg).load()
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

  "spark query" can "use schema inference with system properties and timestamp" in {
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
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.inferSchema.includeSystemProperties" -> "true",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
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

    rowWithInference.schema(CosmosTableSchemaInferrer.SelfAttributeName).nullable shouldBe false
    rowWithInference.schema(CosmosTableSchemaInferrer.TimestampAttributeName).nullable shouldBe false
    rowWithInference.schema(CosmosTableSchemaInferrer.ResourceIdAttributeName).nullable shouldBe false
    rowWithInference.schema(CosmosTableSchemaInferrer.ETagAttributeName).nullable shouldBe false
    rowWithInference.schema(CosmosTableSchemaInferrer.AttachmentsAttributeName).nullable shouldBe false
  }

  "spark query" can "use schema inference with just timestamp" in {
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
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.inferSchema.includeTimestamp" -> "true",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'dog'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's dog"
    rowWithInference.getAs[String]("type") shouldEqual "dog"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe false
  }

  "spark query" can "use schema inference with no system properties or timestamp" in {
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
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
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

  "spark query" can "use schema inference with custom query" in {
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
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.inferSchema.query" -> "select TOP 1 c.isAlive, c.type, c.age from c",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'duck'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.schema.fields should have size 3
    rowWithInference.getAs[String]("type") shouldEqual "duck"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true
  }

  "spark query" can "use schema inference with custom query and system properties" in {
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
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.inferSchema.query" -> "select TOP 1 c.type, c.age, c.isAlive, c._ts, c.id from c",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'duck'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("type") shouldEqual "duck"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.IdAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe false

    rowWithInference.schema(CosmosTableSchemaInferrer.TimestampAttributeName).nullable shouldBe false
    rowWithInference.schema(CosmosTableSchemaInferrer.IdAttributeName).nullable shouldBe false
    rowWithInference.schema("type").nullable shouldBe true
    rowWithInference.schema("age").nullable shouldBe true
    rowWithInference.schema("isAlive").nullable shouldBe true
  }

  "spark query" can "when forceNullableProperties is false and rows have different schema" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val samplingSize = 100
    val expectedResults = samplingSize * 2
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // Inserting documents with slightly different schema
    for( _ <- 1 to expectedResults) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      val arr = objectNode.putArray("object_array")
      val nested = Utils.getSimpleObjectMapper.createObjectNode()
      nested.put("A", "test")
      nested.put("B", "test")
      arr.add(nested)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    for( _ <- 1 to samplingSize) {
      val objectNode2 = Utils.getSimpleObjectMapper.createObjectNode()
      val arr = objectNode2.putArray("object_array")
      val nested = Utils.getSimpleObjectMapper.createObjectNode()
      nested.put("A", "test")
      arr.add(nested)
      objectNode2.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode2).block()
    }

    val cfgWithInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.inferSchema.forceNullableProperties" -> "false",
      "spark.cosmos.read.inferSchema.samplingSize" -> samplingSize.toString,
      "spark.cosmos.read.inferSchema.query" -> "SELECT * FROM c ORDER BY c._ts",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
    try {
      dfWithInference.collect()
      fail("Should have thrown an exception")
    }
    catch {
      case inner: Exception =>
        inner.toString.contains("The 1th field 'B' of input row cannot be null") shouldBe true
    }
  }

  "spark query" can "use custom sampling size" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val samplingSize = 100
    val expectedResults = samplingSize * 2
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // Inserting documents with slightly different schema
    for( _ <- 1 to expectedResults) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("legs", 4)
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "animal")
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    for( _ <- 1 to samplingSize) {
      val objectNode2 = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode2.put("wheels", 4)
      objectNode2.put("name", "Shrodigner's car")
      objectNode2.put("type", "car")
      objectNode2.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode2).block()
    }

    val cfgWithInference = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.inferSchema.samplingSize" -> samplingSize.toString,
      "spark.cosmos.read.inferSchema.query" -> "SELECT * FROM c ORDER BY c._ts",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
    val rows = dfWithInference.where("type = 'animal'").collect()
    rows should have size expectedResults

    // Schema inference should not have picked up the cars even though the query was *
    val fieldNames = rows(0).schema.fields.map(field => field.name)
    fieldNames.contains("legs") shouldBe true
    fieldNames.contains("wheels") shouldBe false
  }

  "spark query" can "get _ts as Timestamp" in  {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    for (_ <- 1 to 10) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      val id = UUID.randomUUID().toString
      objectNode.put("id", id)
      container.createItem(objectNode).block()
    }

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import org.apache.spark.sql.types._
    // scalastyle:on underscore.import
    // scalastyle:on import.grouping

    val customSchema = StructType(Array(
      StructField("_ts", TimestampType),
      StructField("id", StringType)
    ))

    val df = spark.read.schema(customSchema).format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.collect()

    for (index <- 0 until rowsArray.length) {
      val row = rowsArray(index)
      val ts = row.getAs[Timestamp]("_ts")
      val id = row.getAs[String]("id")

      ts.getTime() > 0 shouldBe true

      val itemResponse = container.readItem(id, new PartitionKey(id), classOf[ObjectNode]).block()

      val documentTs = itemResponse.getItem.get("_ts").asLong

      ts.getTime() shouldBe documentTs
    }
  }

  "spark query" can "return proper Cosmos specific query plan on explain" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString

    val rawItem = s"""
                     | {
                     |   "id" : "${id}",
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
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.forceNullableProperties" -> "false",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where("nestedObject.prop2 = '6'").collect()
    rowsArray should have size 1

    var output = new java.io.ByteArrayOutputStream()
    Console.withOut(output) {
      df.explain()
    }
    var queryPlan = output.toString.replaceAll("#\\d+", "#x")
    logInfo(s"Query Plan: $queryPlan")
    queryPlan.contains("Cosmos Query: SELECT * FROM r") shouldEqual true

    output = new java.io.ByteArrayOutputStream()
    Console.withOut(output) {
      df.where("nestedObject.prop2 = '6'").explain()
    }
    queryPlan = output.toString.replaceAll("#\\d+", "#x")
    logInfo(s"Query Plan: $queryPlan")
    val expected = s"Cosmos Query: SELECT * FROM r WHERE r['nestedObject']['prop2']=" +
      s"@param0${System.getProperty("line.separator")} > param: @param0 = 6"
    queryPlan.contains(expected) shouldEqual true

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
  }

  "spark query" should "use Custom Query also for inference" in {
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
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.customQuery" ->
        "SELECT c.type, c.age, c.isAlive FROM c where c.type = 'duck' and c.isAlive = true",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
    val rowsArrayWithInference = dfWithInference.collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("type") shouldEqual "duck"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe false
  }

  "spark query" can "return proper Cosmos specific query plan on explain with nullable properties" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString

    val rawItem = s"""
                     | {
                     |   "id" : "${id}",
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
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.forceNullableProperties" -> "true",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where("nestedObject.prop2 = '6'").collect()
    rowsArray should have size 1

    var output = new java.io.ByteArrayOutputStream()
    Console.withOut(output) {
      df.explain()
    }
    var queryPlan = output.toString.replaceAll("#\\d+", "#x")
    logInfo(s"Query Plan: $queryPlan")
    queryPlan.contains("Cosmos Query: SELECT * FROM r") shouldEqual true

    output = new java.io.ByteArrayOutputStream()
    Console.withOut(output) {
      df.where("nestedObject.prop2 = '6'").explain()
    }
    queryPlan = output.toString.replaceAll("#\\d+", "#x")
    logInfo(s"Query Plan: $queryPlan")
    val expected = s"Cosmos Query: SELECT * FROM r WHERE (NOT(IS_NULL(r['nestedObject'])) AND IS_DEFINED(r['nestedObject'])) " +
      s"AND r['nestedObject']['prop2']=" +
      s"@param0${System.getProperty("line.separator")} > param: @param0 = 6"
    queryPlan.contains(expected) shouldEqual true

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  //scalastyle:on file.size.limit
  //scalastyle:on null
}
