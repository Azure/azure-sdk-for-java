// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.implementation.{SparkBridgeImplementationInternal, TestConfigurations, Utils}
import com.azure.cosmos.models.{CosmosContainerProperties, CosmosItemIdentity, CosmosItemRequestOptions, PartitionKey, PartitionKeyDefinition, PartitionKeyDefinitionVersion, PartitionKind, ThroughputProperties}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.udf.{GetCosmosItemIdentityValue, GetFeedRangeForPartitionKeyValue}
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.types.StringType

import java.sql.Timestamp
import java.util
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
import org.apache.spark.sql.types._
// scalastyle:on underscore.import

abstract class SparkE2EQueryITestBase
  extends IntegrationSpec
    with SparkWithJustDropwizardAndNoSlf4jMetrics
    with CosmosClient
    with AutoCleanableCosmosContainersWithPkAsPartitionKey
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
  "spark items DataSource version" can "be determined" in {
    CosmosItemsDataSource.version shouldEqual CosmosConstants.currentVersion
  }

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
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.diagnostics" -> "feed_details"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.where("nestedObject.prop2 = '6'").collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id

    assertMetrics(meterRegistry, "cosmos.client.op.latency", expectedToFind = true)

    // Gateway requests are not happening always - but they can happen
    //assertMetrics(meterRegistry, "cosmos.client.req.gw", expectedToFind = true)

    assertMetrics(meterRegistry, "cosmos.client.req.rntbd", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.rntbd", expectedToFind = true)

    // address resolution requests can but don't have to happen - they are optional
    // assertMetrics(meterRegistry, "cosmos.client.rntbd.addressResolution", expectedToFind = true)
  }

  "cosmos client" can "be retrieved from cache" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
    )
    var clientCacheItem = com.azure.cosmos.spark.udf.CosmosAsyncClientCache
      .getCosmosClientFromCache(cfg)
    var clientFromCache = clientCacheItem
      .getClient
      .asInstanceOf[CosmosAsyncClient]
    var dbResponse = clientFromCache.getDatabase(cosmosDatabase).read().block()

    dbResponse.getProperties.getId shouldEqual cosmosDatabase

    clientCacheItem = com.azure.cosmos.spark.udf.CosmosAsyncClientCache
      .getCosmosClientFuncFromCache(cfg)()
    clientFromCache = clientCacheItem
      .getClient
      .asInstanceOf[CosmosAsyncClient]
    dbResponse = clientFromCache.getDatabase(cosmosDatabase).read().block()

    dbResponse.getProperties.getId shouldEqual cosmosDatabase

    val idOfTestDoc = insertDummyValue()
    val itemIdentities = new util.ArrayList[CosmosItemIdentity]()
    itemIdentities.add(new CosmosItemIdentity(new PartitionKey(idOfTestDoc), idOfTestDoc))
    val response = clientFromCache
      .getDatabase(cosmosDatabase)
      .getContainer(cosmosContainer)
      .readMany(itemIdentities, classOf[ObjectNode])
      .block()

    val customObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
    customObjectMapper.registerModule(new JavaTimeModule())
    customObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    customObjectMapper.registerModule(new DefaultScalaModule())
    customObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    customObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

    val returnValues = response.getResults.asScala.map(shadedObjectNode => {
      val json: String = shadedObjectNode.toString();
      customObjectMapper.readValue(json, classOf[TestScalaDoc]);
    })

    returnValues.size shouldEqual 1

    clientCacheItem.close()
  }

  private def insertDummyValue() : String = {
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

    id
  }

  "spark query" can "support StringStartsWith" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

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

    val logTestCaseIdentifier = UUID.randomUUID().toString
    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.read.maxItemCount" -> "2",
      "spark.cosmos.read.customQuery" ->
        s"SELECT * FROM c WHERE c.isAlive = true and c.type = 'cat' and c.id <> '$logTestCaseIdentifier'",
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
    val rowsArray = df
      .orderBy("age")
      .collect()
    rowsArray should have size 20

    for (index <- rowsArray.indices) {
      val row = rowsArray(index)
      row.getAs[String]("name") shouldEqual "Shrodigner's cat"
      row.getAs[String]("type") shouldEqual "cat"
      row.getAs[Integer]("age") shouldEqual index + 1
      row.getAs[Boolean]("isAlive") shouldEqual true
    }

    // validate from diagnostics that all responses had at most 2 records (instead of the default of up to 100)
    val logger = SimpleFileDiagnosticsProvider.getOrCreateSingletonLoggerInstance(ItemsPartitionReader.getClass)
    val messages = logger.getMessages()
    SimpleFileDiagnosticsProvider.reset()
    messages should not be null
    messages.size should not be 0
    for ((msg, _) <- messages) {
      if (msg.contains(logTestCaseIdentifier)) {
        val itemCountPos = msg.indexOf("itemCount:")
        if (itemCountPos > 0) {
          val startPos = itemCountPos + "itemCount:".length
          val itemCount = msg.substring(startPos, msg.indexOf(",", startPos)).toInt
          if (itemCount > 3) {
            this.logInfo(s"Wrong log message: $msg")
          }
          itemCount should be <= 2
        }
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

    for (index <- rowsArray.indices) {
      val row = rowsArray(index)
      row.getAs[String]("name") shouldEqual "Shrodigner's cat"
      row.getAs[String]("type") shouldEqual "cat"
      row.getAs[Integer]("age") shouldEqual index + 1
      row.getAs[Boolean]("isAlive") shouldEqual true
    }
  }

  "spark read many" can "use user provided schema" in {
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

    val customSchema = StructType(Array(
      StructField("id", StringType),
      StructField("name", StringType),
      StructField("type", StringType),
      StructField("age", IntegerType),
      StructField("isAlive", BooleanType)
    ))

    val df = spark.read.schema(customSchema).format("cosmos.oltp").options(cfg).load()
    val filteredDf = df
      .where("isAlive = 'true' and type = 'cat'").orderBy("age")
    var rowsArray = filteredDf.collect()
    rowsArray should have size 20

    for (index <- rowsArray.indices) {
      val row = rowsArray(index)
      row.getAs[String]("name") shouldEqual "Shrodigner's cat"
      row.getAs[String]("type") shouldEqual "cat"
      row.getAs[Integer]("age") shouldEqual index + 1
      row.getAs[Boolean]("isAlive") shouldEqual true
    }

    val readManyDf = CosmosItemsDataSource.readMany(filteredDf, cfg.asJava, customSchema).orderBy("age")
    rowsArray = readManyDf.collect()
    rowsArray should have size 20

    for (index <- rowsArray.indices) {
      val row = rowsArray(index)
      row.getAs[String]("name") shouldEqual "Shrodigner's cat"
      row.getAs[String]("type") shouldEqual "cat"
      row.getAs[Integer]("age") shouldEqual index + 1
      row.getAs[Boolean]("isAlive") shouldEqual true
    }
  }

  "spark read many" can "use default schema" in {
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
      "spark.cosmos.read.inferSchema.enabled" -> "false"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val filteredDf = df.limit(10)
    filteredDf.schema shouldEqual ItemsTable.defaultSchemaForInferenceDisabled
    var rowsArray = filteredDf.collect()
    rowsArray should have size 10

    val readManyDf = CosmosItemsDataSource.readMany(filteredDf, cfg.asJava)
    readManyDf.schema shouldEqual ItemsTable.defaultSchemaForInferenceDisabled
    rowsArray = readManyDf.collect()
    rowsArray should have size 10
  }

  "spark read many" can "use schema inference with system properties and timestamp" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // assert that there is more than one range to ensure the test really is testing the parallelization of work
    container.getFeedRanges.block().size() should be > 1

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
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.read.inferSchema.includeSystemProperties" -> "true",
    )

    // Not passing schema, letting inference work
    val dfWithInference = spark.read.format("cosmos.oltp").options(cfgWithInference).load()
    var rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'dog'").collect()
    rowsArrayWithInference should have size 1

    var rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's dog"
    rowWithInference.getAs[String]("type") shouldEqual "dog"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    var fieldNames = rowWithInference.schema.fields.map(field => field.name)
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

    val filteredDf = dfWithInference
      .where("isAlive = 'true' and type = 'dog'").orderBy("age")

    val readManyDf = CosmosItemsDataSource.readMany(filteredDf, cfgWithInference.asJava).orderBy("age")
    rowsArrayWithInference = readManyDf.collect()
    rowsArrayWithInference should have size 1

    rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's dog"
    rowWithInference.getAs[String]("type") shouldEqual "dog"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    fieldNames = rowWithInference.schema.fields.map(field => field.name)
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

  "spark query" can "use schema inference with varying schema across partitions" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    for (index <- 1 to 20) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's frog")
      objectNode.put("type", "frog")
      objectNode.put("age", 20)
      objectNode.put(s"Property${index.toString}", index.toString)
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
    val rowsArrayWithInference = dfWithInference.where("type = 'frog'").collect()
    rowsArrayWithInference should have size 20

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's frog"
    rowWithInference.getAs[String]("type") shouldEqual "frog"
    rowWithInference.getAs[Integer]("age") shouldEqual 20

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe false
    for (index <- 1 to 20) {
      logInfo(s"Property${index.toString}")
      fieldNames.contains(s"Property${index.toString}") shouldBe true
    }
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

    Thread.sleep(2000)

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
        logInfo(inner.toString)
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

    Thread.sleep(2000)

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

    for (index <- rowsArray.indices) {
      val row = rowsArray(index)
      val ts = row.getAs[Timestamp]("_ts")
      val id = row.getAs[String]("id")

      ts.getTime > 0 shouldBe true

      val itemResponse = container.readItem(id, new PartitionKey(id), classOf[ObjectNode]).block()

      val documentTs = itemResponse.getItem.get("_ts").asLong

      ts.getTime shouldBe documentTs
    }
  }

  "spark query" can "return proper Cosmos specific query plan on explain" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

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

  "spark query" can "for single logical partition can be configured to use only one Spark partition" in  {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // assert that there is more than one range to ensure the test really is testing the parallelization of work
    container.getFeedRanges.block().size() should be > 1
    var lastId = ""
    for (age <- 1 to 20) {
      for (state <- Array(true, false)) {
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", age)
        objectNode.put("isAlive", state)
        lastId = UUID.randomUUID().toString
        objectNode.put("id", lastId)
        container.createItem(objectNode).block()
      }
    }

    var cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
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

    var df = spark.read.schema(customSchema).format("cosmos.oltp").options(cfg).load()
    var rowsArray = df.where(s"id = '$lastId'").collect()
    rowsArray should have size 1
    rowsArray(0).getAs[String]("id") shouldEqual lastId
    df.rdd.getNumPartitions shouldEqual container.getFeedRanges.block().size()

    spark.udf.register("GetFeedRangeForPartitionKey", new GetFeedRangeForPartitionKeyValue(), StringType)
    val pkDefinition = "{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"
    val dummyDf = spark.sql(s"SELECT GetFeedRangeForPartitionKey('$pkDefinition', '$lastId')")

    val feedRange = dummyDf
      .collect()(0)
      .getAs[String](0)

    logInfo(s"FeedRange from UDF: $feedRange")

    cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.partitioning.feedRangeFilter" -> feedRange
    )

    df = spark.read.schema(customSchema).format("cosmos.oltp").options(cfg).load()
    rowsArray = df.where(s"id = '$lastId'").collect()
    rowsArray should have size 1
    rowsArray(0).getAs[String]("id") shouldEqual lastId
    df.rdd.getNumPartitions shouldEqual 1
  }

  "spark query" can "log single correlation activity id for all queries across multiple Spark partitions" in  {
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

    val logTestCaseIdentifier = UUID.randomUUID().toString
    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.read.maxItemCount" -> "2",
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.read.customQuery" ->
        s"SELECT * FROM c WHERE c.isAlive = true and c.type = 'cat' and c.id <> '$logTestCaseIdentifier'",
      "spark.cosmos.diagnostics" -> SimpleFileDiagnosticsProvider.getClass.getName.replace("$", "")
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

    SimpleFileDiagnosticsProvider.reset()

    val df = spark.read.schema(customSchema).format("cosmos.oltp").options(cfg).load()
    val rowsArray = df
      .orderBy("age")
      .collect()
    rowsArray should have size 20

    for (index <- rowsArray.indices) {
      val row = rowsArray(index)
      row.getAs[String]("name") shouldEqual "Shrodigner's cat"
      row.getAs[String]("type") shouldEqual "cat"
      row.getAs[Integer]("age") shouldEqual index + 1
      row.getAs[Boolean]("isAlive") shouldEqual true
    }

    // validate from diagnostics that all responses had at most 2 records (instead of the default of up to 100)
    val logger = SimpleFileDiagnosticsProvider.getOrCreateSingletonLoggerInstance(ItemsPartitionReader.getClass)
    val messages = logger.getMessages()
    SimpleFileDiagnosticsProvider.reset()
    messages should not be null
    messages.size should not be 0

    val correlationActivityIds = mutable.HashSet.empty[String]
    for ((msg, _) <- messages) {
      if (msg.contains(logTestCaseIdentifier)) {
        var startPos = 0
        var continueLoop = true
        while (continueLoop && startPos < msg.length) {
          val nextActivityIdPos = msg.indexOf("correlationActivityId", startPos)

          if (nextActivityIdPos < 0) {
            continueLoop = false
          } else {
            val currentStartPos = nextActivityIdPos + "correlationActivityId".length + 1
            val currentEndPos = math.min(currentStartPos + 36, msg.length - 1)
            val correlationActivityId = msg.substring(currentStartPos, currentEndPos)
            correlationActivityIds += correlationActivityId

            startPos = currentEndPos + 1
          }
        }
      }
    }

    assert(
      correlationActivityIds.size == 1,
      "Logs should only contain one correlationActivityId - " + messages.mkString("\r\n"))
    correlationActivityIds.size shouldEqual 1
  }

  "spark query" can "execute query with VALUE function" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

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

    val id2 = UUID.randomUUID().toString

    val rawItem2 = s"""
                     | {
                     |   "id" : "$id2",
                     |   "nestedObject" : {
                     |     "prop1" : 5,
                     |     "prop2" : "7"
                     |   }
                     | }
                     |""".stripMargin

    val objectNode2 = objectMapper.readValue(rawItem2, classOf[ObjectNode])

    container.createItem(objectNode2).block()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.read.customQuery" -> "SELECT VALUE c.nestedObject.prop1 FROM c"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.collect()
    rowsArray should have size 2

    val item = rowsArray(0)
    item.getAs[Int]("_value") shouldEqual 5

    val item2 = rowsArray(1)
    item2.getAs[Int]("_value") shouldEqual 5
  }

  "spark query" can "execute query with VALUE function ad ORDER BY" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

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

    val id2 = UUID.randomUUID().toString

    val rawItem2 = s"""
                      | {
                      |   "id" : "$id2",
                      |   "nestedObject" : {
                      |     "prop1" : 8,
                      |     "prop2" : "7"
                      |   }
                      | }
                      |""".stripMargin

    val objectNode2 = objectMapper.readValue(rawItem2, classOf[ObjectNode])

    container.createItem(objectNode2).block()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.read.customQuery" -> "SELECT VALUE c.nestedObject.prop1 FROM c ORDER BY c.nestedObject.prop2"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()

    // sorting on the Spark level is is done here to simplify result validation. The custom
    // query is ordered by prop2 - but this ordering is happening scoped to each physical partition
    // because in Spark we run the custom query isolated (as non-cross-partition query) against an EPK
    // So to simplify the content validation the spark-level ordering (which is global - across
    // results from all cosmos partitions) is added
    val rowsArray = df.orderBy("_value").collect()
    rowsArray should have size 2

    val item = rowsArray(0)
    item.getAs[Int]("_value") shouldEqual 5

    val item2 = rowsArray(1)
    item2.getAs[Int]("_value") shouldEqual 8
  }

  "spark query" can "read invalid json with duplicate properties if setting is overridden" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString

    val rawItem = s"""
                     | {
                     |   "id" : "$id",
                     |   "prop1" : 5,
                     |   "prop1" : 7,
                     |   "nestedObject" : {
                     |     "prop2" : "6"
                     |   }
                     | }
                     |""".stripMargin

    val blob = rawItem.getBytes("UTF-8")

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val requestOptions = new CosmosItemRequestOptions()
    container.createItem(blob, new PartitionKey(id), requestOptions).block()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.read.allowInvalidJsonWithDuplicateJsonProperties" -> "true"
    )

    try {
      val df = spark.read.format("cosmos.oltp").options(cfg).load()
      val rowsArray = df.where("nestedObject.prop2 = '6'").collect()
      rowsArray should have size 1

      val item = rowsArray(0)
      item.getAs[String]("id") shouldEqual id
      item.getAs[Int]("prop1") shouldEqual 7
    } finally {
      try {
        container.deleteItem(id, new PartitionKey(id)).block()
      } finally {
        SparkBridgeImplementationInternal.configureSimpleObjectMapper(false)
      }
    }
  }

  "spark query" can "read item by using readMany" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString

    val rawItem =
      s"""
         | {
         |   "id" : "$id",
         |   "prop1" : 5,
         |   "nestedObject" : {
         |     "prop2" : "6"
         |   }
         | }
         |""".stripMargin

    val blob = rawItem.getBytes("UTF-8")

    val joinContainerName = UUID.randomUUID().toString
    val joinContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(joinContainerName)
    try {
      cosmosClient
        .getDatabase(cosmosDatabase)
        .createContainerIfNotExists(joinContainerName, "/id", ThroughputProperties.createManualThroughput(400))
        .block()

      val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
      val requestOptions = new CosmosItemRequestOptions()
      container.createItem(blob, new PartitionKey(id), requestOptions).block()
      joinContainer.createItem(blob, new PartitionKey(id), requestOptions).block()

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
        "spark.cosmos.read.readManyFiltering.enabled" -> "true"
      )

      val joinCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> joinContainerName,
        "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
      )

      val joinDf = spark.read.format("cosmos.oltp").options(joinCfg).load().select("id")
      val valuesForFilter = joinDf.collect().map(_.getString(0))

      val df = spark.read.format("cosmos.oltp").options(cfg).load()
      val rowsArray = df.where(df("id") isin (valuesForFilter: _*)).collect()
      rowsArray should have size 1

      val item = rowsArray(0)
      item.getAs[String]("id") shouldEqual id
      item.getAs[Int]("prop1") shouldEqual 5

    } finally {
      joinContainer.delete().block()
    }
  }

  "spark query" can "read item by using readMany with subpartitions" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val joinContainerName = s"join-${UUID.randomUUID().toString}"
    val joinContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(joinContainerName)
    val containerWithSubPartitionsName = s"subpartition-${UUID.randomUUID().toString}"
    val containerWithSubPartitions = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerWithSubPartitionsName)

    try {
      val subpartitionKeyDefinition = getDefaultPartitionKeyDefinitionWithSubpartitions
      createContainerIfNotExists(joinContainerName, subpartitionKeyDefinition)
      createContainerIfNotExists(containerWithSubPartitionsName, subpartitionKeyDefinition)

      val createdItemIds = ListBuffer[String]()
      // create two items in both containers
      for (i <- 0 to 2) {
        val id = UUID.randomUUID().toString
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", 20)
        objectNode.put("index", i.toString)
        objectNode.put("id", id)
        objectNode.put("tenantId", id)
        objectNode.put("userId", "userId1")
        objectNode.put("sessionId", "sessionId1")
        containerWithSubPartitions.createItem(objectNode).block()
        joinContainer.createItem(objectNode).block()
        createdItemIds += id
      }

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> containerWithSubPartitionsName,
        "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
        "spark.cosmos.read.readManyFiltering.enabled" -> "true"
      )

      val joinCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> joinContainerName,
        "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
      )

      spark.udf.register("GetCosmosItemIdentityValue", new GetCosmosItemIdentityValue(), StringType)

      val joinDf =
        spark
          .read
          .format("cosmos.oltp")
          .options(joinCfg)
          .load()
          .withColumn("_itemIdentity", expr("GetCosmosItemIdentityValue(id, array(tenantId, userId, sessionId))"))
          .select("_itemIdentity")

      val valuesForFilter = joinDf.collect().map(_.getString(0))

      val df = spark.read.format("cosmos.oltp").options(cfg).load()
      val rowsArrays = df.where(df("_itemIdentity") isin (valuesForFilter: _*)).collect()
      rowsArrays should have size createdItemIds.size

      val itemIds = rowsArrays.map(rowArray => rowArray.getAs[String]("id"))
      itemIds should contain allElementsOf createdItemIds

    } finally {
      joinContainer.delete().block()
      containerWithSubPartitions.delete().block()
    }
  }

  "spark query" should "always populate the readMany filtering property if readMany filtering enabled" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val id = UUID.randomUUID().toString
    val pk = UUID.randomUUID().toString
    val rawItem =
      s"""
         | {
         |   "id" : "${id}",
         |   "pk" : "${pk}",
         |   "prop1" : 5,
         |   "nestedObject" : {
         |     "prop2" : "6"
         |   }
         | }
         |""".stripMargin

    val blob = rawItem.getBytes("UTF-8")

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val requestOptions = new CosmosItemRequestOptions()
    container.createItem(blob, new PartitionKey(pk), requestOptions).block()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.read.readManyFiltering.enabled" -> "true"
    )

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    val rowsArray = df.collect()
    rowsArray should have size 1

    val item = rowsArray(0)
    item.getAs[String]("id") shouldEqual id
    item.getAs[Int]("prop1") shouldEqual 5
    item.getAs[String]("_itemIdentity") shouldEqual CosmosItemIdentityHelper.getCosmosItemIdentityValueString(id, List[String](pk))
  }

  private def getDefaultPartitionKeyDefinitionWithSubpartitions: PartitionKeyDefinition = {
    val partitionKeyPaths = new util.ArrayList[String]
    partitionKeyPaths.add("/tenantId")
    partitionKeyPaths.add("/userId")
    partitionKeyPaths.add("/sessionId")
    val subpartitionKeyDefinition = new PartitionKeyDefinition
    subpartitionKeyDefinition.setPaths(partitionKeyPaths)
    subpartitionKeyDefinition.setKind(PartitionKind.MULTI_HASH)
    subpartitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2)

    subpartitionKeyDefinition
  }

  private def createContainerIfNotExists(containerName: String, partitionKeyDefinition: PartitionKeyDefinition): Unit = {
    val containerProperties = new CosmosContainerProperties(containerName, partitionKeyDefinition)
    cosmosClient
      .getDatabase(cosmosDatabase)
      .createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(Defaults.DefaultContainerThroughput))
      .block()
  }


  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  //scalastyle:on file.size.limit
  //scalastyle:on null
}
