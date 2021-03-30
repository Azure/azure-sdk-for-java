// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer

import java.util.UUID
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.ThroughputProperties
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.StreamingQueryListener
import org.apache.spark.sql.streaming.StreamingQueryListener.{QueryProgressEvent, QueryStartedEvent, QueryTerminatedEvent}
import org.apache.spark.sql.types.{BooleanType, IntegerType, StringType, StructField, StructType}

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

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
    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "false"
    )

    val testId = UUID.randomUUID().toString.replace("-", "")
    val changeFeedDF = spark
      .readStream
      .format("cosmos.changeFeed")
      .options(cfg)
      .load()
    val microBatchQuery = changeFeedDF
      .writeStream
      .format("memory")
      .queryName(testId)
      .option("checkpointLocation", s"/tmp/$testId/")
      .outputMode("append")
      .start()

    // Ingest test data while micro batch query is running already
    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }
    microBatchQuery.processAllAvailable()

    spark
      .table(testId)
      .show(truncate = false)

    val rowCount = spark.table(testId).count()

    rowCount shouldEqual 2
  }

  "spark change feed micro batch (incremental)" can "be used to copy data to another container" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    val testId = UUID.randomUUID().toString.replace("-", "")
    val targetContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
      "target_" + testId,
      "/id",
      ThroughputProperties.createManualThroughput(18000)).block()
    val targetContainer = cosmosClient
      .getDatabase(cosmosDatabase)
      .getContainer(targetContainerResponse.getProperties.getId)

    // Initially ingest 100 records
    for (i <- 0 until 100) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("sequenceNumber", i)
      objectNode.put("id", UUID.randomUUID().toString)
      sourceContainer.createItem(objectNode).block()
    }

    Thread.sleep(1000)

    val changeFeedCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchemaEnabled" -> "false"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> targetContainer.getId,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulkEnabled" -> "true",
      "checkpointLocation" -> ("/tmp/" + testId + "/")
    )

    val processedRecordCount = new AtomicLong()
    spark.streams.addListener(new StreamingQueryListener() {
      override def onQueryStarted(queryStarted: QueryStartedEvent): Unit = {}
      override def onQueryTerminated(queryTerminated: QueryTerminatedEvent): Unit = {}
      override def onQueryProgress(queryProgress: QueryProgressEvent): Unit = {
        processedRecordCount.addAndGet(queryProgress.progress.sink.numOutputRows)
      }
    })

    val changeFeedDF = spark
      .readStream
      .format("cosmos.changeFeed")
      .options(changeFeedCfg)
      .load()

    var microBatchQuery = changeFeedDF
      .writeStream
      .format("cosmos.items")
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    microBatchQuery.processAllAvailable()

    Thread.sleep(1000)

    var sourceCount: Long = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after first execution: $sourceCount")
    var targetCount: Long = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after first execution: $targetCount")

    processedRecordCount.get() shouldEqual 100L
    sourceCount shouldEqual 100L
    sourceCount shouldEqual targetCount

    spark.close()

    spark = SparkSession.builder()
      .appName("spark connector sample for recovering structure streaming query")
      .master("local")
      .getOrCreate()

    processedRecordCount.set(0L)
    spark.streams.addListener(new StreamingQueryListener() {
      override def onQueryStarted(queryStarted: QueryStartedEvent): Unit = {}
      override def onQueryTerminated(queryTerminated: QueryTerminatedEvent): Unit = {}
      override def onQueryProgress(queryProgress: QueryProgressEvent): Unit = {
        processedRecordCount.addAndGet(queryProgress.progress.sink.numOutputRows)
      }
    })

    // Ingest ten more records
    for (i <- 100 until 110) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("sequenceNumber", i)
      objectNode.put("id", UUID.randomUUID().toString)
      sourceContainer.createItem(objectNode).block()
    }

    Thread.sleep(1000)

    val secondChangeFeedDF = spark
      .readStream
      .format("cosmos.changeFeed")
      .options(changeFeedCfg)
      .load()

    // new query reusing the same query name - so continuing where the first one left off
    val secondMicroBatchQuery = secondChangeFeedDF
      .writeStream
      .format("cosmos.items")
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    secondMicroBatchQuery.processAllAvailable()

    Thread.sleep(1000)

    sourceCount = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after second execution: $sourceCount")
    targetCount = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after second execution: $targetCount")

    processedRecordCount.get() shouldEqual 10L
    sourceCount shouldEqual 110L
    sourceCount shouldEqual targetCount

    targetContainer.delete()
  }

  private[this] def getRecordCountOfContainer(container: CosmosAsyncContainer): Long = {
    val countValueList = container
      .queryItems("SELECT VALUE COUNT(1) FROM c", classOf[Long])
      .collectList()
      .block

    countValueList should have size 1

    countValueList.get(0)
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
