// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.{PartitionKey, ThroughputProperties}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{StreamingQueryListener, Trigger}
import org.apache.spark.sql.streaming.StreamingQueryListener.{QueryProgressEvent, QueryStartedEvent, QueryTerminatedEvent}

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.udf.GetFeedRangeForPartitionKeyValue
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.types.StringType
import org.scalatest.Retries
import org.scalatest.tagobjects.Retryable

import java.util.regex.Pattern

class SparkE2EStructuredStreamingITest
  extends IntegrationSpec
    with CosmosClient
    with CosmosContainerWithRetention
    with BasicLoggingTrait
    with Retries {

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  override def withFixture(test: NoArgTest) = {
    if (isRetryable(test))
      withRetry { super.withFixture(test) }
    else
      super.withFixture(test)
  }

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  "spark change feed micro batch (incremental)" can
    "be used with ItemCountPerTriggerHint on container with some empty partitions" in {

    val processedRecordCount = new AtomicLong()
    var spark = this.createSparkSession(processedRecordCount)
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val testId = UUID.randomUUID().toString
    val targetContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
      "target_" + testId,
      "/id",
      ThroughputProperties.createManualThroughput(18000)).block()
    val targetContainer = cosmosClient
      .getDatabase(cosmosDatabase)
      .getContainer(targetContainerResponse.getProperties.getId)

    // Only ingest exactly 1 record, so at least one partition is empty
    this.ingestTestDocument(sourceContainer, 1)

    val changeFeedCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "10000"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> targetContainer.getId,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "true",
      "checkpointLocation" -> ("/tmp/" + testId + "/")
    )

    val changeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    val microBatchQuery = changeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("500 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(20000)
    microBatchQuery.stop()

    var sourceCount: Long = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after first execution: $sourceCount")
    var targetCount: Long = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after first execution: $targetCount")

    processedRecordCount.get() shouldEqual 1L
    sourceCount shouldEqual 1L
    sourceCount shouldEqual targetCount

    // Initially ingest 100 records
    for (i <- 1 until 21) {
      this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(2100)

    val secondChangeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    // new query reusing the same query name - so continuing where the first one left off
    val secondMicroBatchQuery = secondChangeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("500 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(15500)
    secondMicroBatchQuery.stop()

    sourceCount = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after second execution: $sourceCount")
    targetCount = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after second execution: $targetCount")

    sourceCount shouldEqual 21L
    targetCount shouldEqual sourceCount

    targetContainer.delete().block()
  }

  "spark change feed micro batch (incremental)" can
    "be used to copy data to another container" taggedAs(Retryable) in {

    val processedRecordCount = new AtomicLong()
    var spark = this.createSparkSession(processedRecordCount)
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val testId = UUID.randomUUID().toString
    val targetContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
      "target_" + testId,
      "/id",
      ThroughputProperties.createManualThroughput(18000)).block()
    val targetContainer = cosmosClient
      .getDatabase(cosmosDatabase)
      .getContainer(targetContainerResponse.getProperties.getId)

    // Initially ingest 100 records
    for (i <- 0 until 20) {
      this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(2100)

    val changeFeedCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> targetContainer.getId,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "true",
      "checkpointLocation" -> ("/tmp/" + testId + "/")
    )

    val changeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    val microBatchQuery = changeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("500 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(20000)

    microBatchQuery.lastProgress should not be null
    microBatchQuery.lastProgress.sources should not be null
    microBatchQuery.lastProgress.sources should not be null
    microBatchQuery.lastProgress.sources(0).endOffset should not be null
    getPartitionCountInOffset(microBatchQuery.lastProgress.sources(0).endOffset) >= 1 shouldEqual true

    microBatchQuery.stop()

    var sourceCount: Long = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after first execution: $sourceCount")
    var targetCount: Long = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after first execution: $targetCount")

    processedRecordCount.get() shouldEqual 20L
    sourceCount shouldEqual 20L
    targetCount shouldEqual sourceCount

    // close and recreate spark session to validate
    // that it is possible to recover the previous query
    // from the commit log
    spark.close()

    processedRecordCount.set(0L)
    spark = createSparkSession(processedRecordCount)

    // Ingest ten more records
    var lastId: String = ""
    for (i <- 20 until 30) {
      lastId = this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(2100)

    val secondChangeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    // new query reusing the same query name - so continuing where the first one left off
    val secondMicroBatchQuery = secondChangeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("500 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(15500)
    secondMicroBatchQuery.stop()

    sourceCount = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after second execution: $sourceCount")
    targetCount = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after second execution: $targetCount")

    sourceCount shouldEqual 30L
    targetCount shouldEqual sourceCount

    val sourceDocumentResponse = sourceContainer
      .readItem[ObjectNode](lastId, new PartitionKey(lastId), classOf[ObjectNode])
      .block()
    sourceDocumentResponse.getStatusCode shouldEqual 200

    val sourceDocument = sourceDocumentResponse.getItem
    sourceDocument should not be null

    sourceDocument.get("age").asInt() shouldEqual 20
    sourceDocument.get("type").asText() shouldEqual "cat"

    val targetDocumentResponse = targetContainer
      .readItem[ObjectNode](lastId, new PartitionKey(lastId), classOf[ObjectNode])
      .block()
    targetDocumentResponse.getStatusCode shouldEqual 200

    val targetDocument = targetDocumentResponse.getItem
    targetDocument should not be null

    sourceDocument.get("age").asInt() shouldEqual targetDocument.get("age").asInt()
    sourceDocument.get("type").asText() shouldEqual targetDocument.get("type").asText()
    sourceDocument.get("sequenceNumber").asInt() shouldEqual targetDocument.get("sequenceNumber").asInt()

    targetContainer.delete().block()
  }

  "spark change feed micro batch (incremental)" can
    "be filtered on FeedRange" taggedAs(Retryable) in {

    val processedRecordCount = new AtomicLong()
    var spark = this.createSparkSession(processedRecordCount)
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val testId = UUID.randomUUID().toString
    val targetContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
      "target_" + testId,
      "/id",
      ThroughputProperties.createManualThroughput(18000)).block()
    val targetContainer = cosmosClient
      .getDatabase(cosmosDatabase)
      .getContainer(targetContainerResponse.getProperties.getId)

    // Initially ingest 200 records
    var lastId = ""
    for (i <- 0 until 200) {
      lastId = this.ingestTestDocument(sourceContainer, i)
    }

    spark.udf.register("GetFeedRangeForPartitionKey", new GetFeedRangeForPartitionKeyValue(), StringType)
    val pkDefinition = "{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"
    val dummyDf = spark.sql(s"SELECT GetFeedRangeForPartitionKey('$pkDefinition', '$lastId')")

    val feedRange = dummyDf
      .collect()(0)
      .getAs[String](0)

    logInfo(s"FeedRange from UDF: $feedRange")

    Thread.sleep(2100)

    val queryCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.partitioning.feedRangeFilter" -> feedRange,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val sourceCount: Long = spark
      .read
      .format("cosmos.oltp")
      .options(queryCfg)
      .load()
      .count()

    val changeFeedCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.partitioning.feedRangeFilter" -> feedRange,
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> targetContainer.getId,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "true",
      "checkpointLocation" -> ("/tmp/" + testId + "/")
    )

    val changeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    val microBatchQuery = changeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("500 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(20000)

    microBatchQuery.lastProgress should not be null
    microBatchQuery.lastProgress.sources should not be null
    microBatchQuery.lastProgress.sources should not be null
    microBatchQuery.lastProgress.sources(0).endOffset should not be null
    getPartitionCountInOffset(microBatchQuery.lastProgress.sources(0).endOffset) shouldEqual 1
    microBatchQuery.stop()

    logInfo(s"RecordCount in source container after first execution: $sourceCount")
    val targetCount: Long = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after first execution: $targetCount")

    sourceCount > 0 shouldEqual true
    processedRecordCount.get() shouldEqual sourceCount
    targetCount shouldEqual sourceCount

    targetContainer.delete().block()
  }

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  "spark change feed micro batch (incremental)" can
    "be used to copy data to another container capturing origin TS and etag" taggedAs(Retryable) in {

    val processedRecordCount = new AtomicLong()
    var spark = this.createSparkSession(processedRecordCount)
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val testId = UUID.randomUUID().toString
    val targetContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
      "target_" + testId,
      "/id",
      ThroughputProperties.createManualThroughput(18000)).block()
    val targetContainer = cosmosClient
      .getDatabase(cosmosDatabase)
      .getContainer(targetContainerResponse.getProperties.getId)

    // Initially ingest 100 records
    for (i <- 0 until 20) {
      this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(2100)

    val changeFeedCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> targetContainer.getId,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "true",
      "checkpointLocation" -> ("/tmp/" + testId + "/")
    )

    val changeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    val microBatchQuery = changeFeedDF
      // Dataframe with _originRawBody column will use it as raw json - but keep _ts as _origin_ts
      // and _etag as _origin_etag
      .withColumnRenamed(
        CosmosTableSchemaInferrer.RawJsonBodyAttributeName,
        CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName)
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("500 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(20000)
    microBatchQuery.stop()

    var sourceCount: Long = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after first execution: $sourceCount")
    var targetCount: Long = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after first execution: $targetCount")

    processedRecordCount.get() shouldEqual 20L
    sourceCount shouldEqual 20L
    targetCount shouldEqual sourceCount

    // close and recreate spark session to validate
    // that it is possible to recover the previous query
    // from the commit log
    spark.close()

    processedRecordCount.set(0L)
    spark = createSparkSession(processedRecordCount)

    // Ingest ten more records
    var lastId: String = ""
    for (i <- 20 until 30) {
      lastId = this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(2100)

    val secondChangeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    // new query reusing the same query name - so continuing where the first one left off
    val secondMicroBatchQuery = secondChangeFeedDF
      // Dataframe with _originRawBody column will use it as raw json - but keep _ts as _origin_ts
      // and _etag as _origin_etag
      .withColumnRenamed(
        CosmosTableSchemaInferrer.RawJsonBodyAttributeName,
        CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName)
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("500 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(15500)
    secondMicroBatchQuery.stop()

    sourceCount = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after second execution: $sourceCount")
    targetCount = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after second execution: $targetCount")

    sourceCount shouldEqual 30L
    targetCount shouldEqual sourceCount

    val sourceDocumentResponse = sourceContainer
      .readItem[ObjectNode](lastId, new PartitionKey(lastId), classOf[ObjectNode])
      .block()
    sourceDocumentResponse.getStatusCode shouldEqual 200

    val sourceDocument = sourceDocumentResponse.getItem
    sourceDocument should not be null

    sourceDocument.get("age").asInt() shouldEqual 20
    sourceDocument.get("type").asText() shouldEqual "cat"

    val targetDocumentResponse = targetContainer
      .readItem[ObjectNode](lastId, new PartitionKey(lastId), classOf[ObjectNode])
      .block()
    targetDocumentResponse.getStatusCode shouldEqual 200

    val targetDocument = targetDocumentResponse.getItem
    targetDocument should not be null

    sourceDocument.get("age").asInt() shouldEqual targetDocument.get("age").asInt()
    sourceDocument.get("type").asText() shouldEqual targetDocument.get("type").asText()
    sourceDocument.get("sequenceNumber").asInt() shouldEqual targetDocument.get("sequenceNumber").asInt()
    sourceDocument.get("_ts").asLong() shouldEqual targetDocument.get("_origin_ts").asLong()
    sourceDocument.get("_etag").asText() shouldEqual targetDocument.get("_origin_etag").asText()

    targetContainer.delete().block()
  }

  "spark change feed micro batch (incremental)" can
    "be used to copy data to another container with limit" taggedAs(Retryable) in {

    val processedRecordCount = new AtomicLong()
    var spark = this.createSparkSession(processedRecordCount)
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val testId = UUID.randomUUID().toString
    val targetContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
      "target_" + testId,
      "/id",
      ThroughputProperties.createManualThroughput(18000)).block()
    val targetContainer = cosmosClient
      .getDatabase(cosmosDatabase)
      .getContainer(targetContainerResponse.getProperties.getId)

    // Initially ingest 100 records
    for (i <- 0 until 100) {
      this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(2100)

    val changeFeedCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "500"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> targetContainer.getId,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "false",
      "checkpointLocation" -> ("/tmp/" + testId + "/")
    )

    val changeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    val microBatchQuery = changeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("100 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(25500)
    microBatchQuery.stop()

    var sourceCount: Long = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after first execution: $sourceCount")
    var targetCount: Long = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after first execution: $targetCount")

    processedRecordCount.get() shouldEqual 100L
    sourceCount shouldEqual 100L
    sourceCount shouldEqual targetCount

    // close and recreate spark session to validate
    // that it is possible to recover the previous query
    // from the commit log
    spark.close()

    processedRecordCount.set(0L)
    spark = createSparkSession(processedRecordCount)

    // Ingest ten more records
    for (i <- 100 until 105) {
      this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(2100)

    val secondChangeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    // new query reusing the same query name - so continuing where the first one left off
    val secondMicroBatchQuery = secondChangeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .trigger(Trigger.ProcessingTime("100 milliseconds"))
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(25500)
    secondMicroBatchQuery.stop()

    sourceCount = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after second execution: $sourceCount")
    targetCount = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after second execution: $targetCount")

    sourceCount shouldEqual 105L
    sourceCount shouldEqual targetCount

    targetContainer.delete().block()
  }

  private[this] def ingestTestDocument
  (
    container: CosmosAsyncContainer,
    sequenceNumber: Int
  ): String = {
    val id = UUID.randomUUID().toString
    val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
    objectNode.put("name", "Shrodigner's cat")
    objectNode.put("type", "cat")
    objectNode.put("age", 20)
    objectNode.put("sequenceNumber", sequenceNumber)
    objectNode.put("id", id)
    container.createItem(objectNode).block()

    id
  }

  private[this] def createSparkSession(processedRecordCount: AtomicLong) = {
    val spark = SparkSession.builder()
      .appName("spark connector sample for recovering structure streaming query")
      .master("local")
      .getOrCreate()

    spark.streams.addListener(new StreamingQueryListener() {
      override def onQueryStarted(queryStarted: QueryStartedEvent): Unit = {}
      override def onQueryTerminated(queryTerminated: QueryTerminatedEvent): Unit = {}
      override def onQueryProgress(queryProgress: QueryProgressEvent): Unit = {
        processedRecordCount.addAndGet(queryProgress.progress.sink.numOutputRows)
      }
    })

    spark
  }

  private[this] def getPartitionCountInOffset(text: String): Long = {
    text should not be null

    val pattern = Pattern.compile("(\"range\")")
    val matcher = pattern.matcher(text)
    var count = 0L
    while (matcher.find) { count += 1 }

    count
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
