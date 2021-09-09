// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.ThroughputProperties
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.StreamingQueryListener
import org.apache.spark.sql.streaming.StreamingQueryListener.{QueryProgressEvent, QueryStartedEvent, QueryTerminatedEvent}

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

class SparkE2EStructuredStreamingITest
  extends IntegrationSpec
    with CosmosClient
    with CosmosContainer//WithRetention
    with BasicLoggingTrait {

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  "spark change feed micro batch (incremental)" can "be used to copy data to another container" in {
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

    Thread.sleep(5000)

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
    for (i <- 100 until 110) {
      this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(5000)

    val secondChangeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    // new query reusing the same query name - so continuing where the first one left off
    val secondMicroBatchQuery = secondChangeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(20000)
    secondMicroBatchQuery.stop()

    sourceCount = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after second execution: $sourceCount")
    targetCount = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after second execution: $targetCount")

    sourceCount shouldEqual 110L
    sourceCount shouldEqual targetCount

    targetContainer.delete()
  }

  "spark change feed micro batch (incremental)" can "be used to copy data to another container with limit" in {
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

    Thread.sleep(5000)

    val changeFeedCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "10"
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
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(40000)
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
    for (i <- 100 until 110) {
      this.ingestTestDocument(sourceContainer, i)
    }

    Thread.sleep(5000)

    val secondChangeFeedDF = spark
      .readStream
      .format("cosmos.oltp.changeFeed")
      .options(changeFeedCfg)
      .load()

    // new query reusing the same query name - so continuing where the first one left off
    val secondMicroBatchQuery = secondChangeFeedDF
      .writeStream
      .format("cosmos.oltp")
      .queryName(testId)
      .options(writeCfg)
      .outputMode("append")
      .start()

    Thread.sleep(20000)
    secondMicroBatchQuery.stop()

    sourceCount = getRecordCountOfContainer(sourceContainer)
    logInfo(s"RecordCount in source container after second execution: $sourceCount")
    targetCount = getRecordCountOfContainer(targetContainer)
    logInfo(s"RecordCount in target container after second execution: $targetCount")

    sourceCount shouldEqual 110L
    sourceCount shouldEqual targetCount

    targetContainer.delete()
  }

  private[this] def ingestTestDocument
  (
      container: CosmosAsyncContainer,
      sequenceNumber: Int
  ): Unit = {
    val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
    objectNode.put("name", "Shrodigner's cat")
    objectNode.put("type", "cat")
    objectNode.put("age", 20)
    objectNode.put("sequenceNumber", sequenceNumber)
    objectNode.put("id", UUID.randomUUID().toString)
    container.createItem(objectNode).block()
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
