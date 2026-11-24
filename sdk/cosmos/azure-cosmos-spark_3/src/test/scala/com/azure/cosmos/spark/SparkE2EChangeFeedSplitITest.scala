// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.{SparkBridgeImplementationInternal, TestConfigurations, Utils}
import com.azure.cosmos.models.{PartitionKey, PartitionKeyDefinition, ThroughputProperties}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.hadoop.fs.Path
import org.apache.spark.sql.{DataFrame, Row}

import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.{Callable, ExecutionException, Executors, ThreadFactory, TimeUnit, TimeoutException}
import scala.collection.mutable
import scala.jdk.CollectionConverters._

class SparkE2EChangeFeedSplitITest
 extends IntegrationSpec
  with Spark
  with CosmosClient
  with CosmosContainer
  with BasicLoggingTrait {

 //scalastyle:off multiple.string.literals
 //scalastyle:off magic.number
 private val MaxSplitWaitAttempts = 300
 private val MaxPartitionKeySearchAttempts = 100000
 private val TestTimeoutInMinutes = 5L

 "spark change feed query (incremental)" should "honor checkpoint location and read limit after partition split" in {
  val cosmosEndpoint = TestConfigurations.HOST
  val cosmosMasterKey = TestConfigurations.MASTER_KEY

  if (cosmosEndpoint.contains("localhost")) {
   logWarning("Skipping this test on emulator, because emulator doesn't allow splitting partitions")
  } else {
   val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
   val initialFeedRanges = container.getFeedRanges.block().asScala
    .map(feedRange => SparkBridgeImplementationInternal.toNormalizedRange(feedRange))
    .sortBy(_.min)
    .toSeq

   for (sequenceNumber <- 1 to 50) {
    val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
    objectNode.put("name", "Schrodinger's cat")
    objectNode.put("type", "cat")
    objectNode.put("age", 20)
    objectNode.put("sequenceNumber", sequenceNumber)
    objectNode.put("id", UUID.randomUUID().toString)
    container.createItem(objectNode).block()
   }

   val checkpointLocation = s"/tmp/checkpoints/${UUID.randomUUID().toString}"
   val cfg = Map(
    "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
    "spark.cosmos.accountKey" -> cosmosMasterKey,
    "spark.cosmos.database" -> cosmosDatabase,
    "spark.cosmos.container" -> cosmosContainer,
    "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "1",
    "spark.cosmos.read.inferSchema.enabled" -> "false",
    "spark.cosmos.changeFeed.startFrom" -> "Beginning",
    "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
    "spark.cosmos.changeFeed.batchCheckpointLocation" -> checkpointLocation,
    "spark.cosmos.metadata.feedRange.refreshIntervalInSeconds" -> "1800" // Disable FeedRange cache refresh to enforce that the cached feed ranges won't be aware of splits
   )

   val df1 = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
   val rowsArray1 = collectWithTimeout(df1, "initial bounded change feed batch")
   // technically possible that even with 50 documents randomly distributed across 3 partitions some
   // has no documents
   // rowsArray should have size df.rdd.getNumPartitions
   rowsArray1.length > 0 shouldEqual true
   rowsArray1.length <= df1.rdd.getNumPartitions shouldEqual true

   val firstBatchSequenceNumbers = getSequenceNumbers(rowsArray1)
   firstBatchSequenceNumbers.distinct.size shouldEqual firstBatchSequenceNumbers.size
   df1.rdd.getNumPartitions shouldEqual initialFeedRanges.size

   df1.schema.equals(
    ChangeFeedTable.defaultIncrementalChangeFeedSchemaForInferenceDisabled) shouldEqual true

   val hdfs = org.apache.hadoop.fs.FileSystem.get(spark.sparkContext.hadoopConfiguration)

   val startOffsetFolderLocation = Paths.get(checkpointLocation, "startOffset").toString
   val startOffsetFileLocation = Paths.get(startOffsetFolderLocation, "0").toString
   hdfs.exists(new Path(startOffsetFolderLocation)) shouldEqual true
   hdfs.exists(new Path(startOffsetFileLocation)) shouldEqual false

   val latestOffsetFolderLocation = Paths.get(checkpointLocation, "latestOffset").toString
   val latestOffsetFileLocation = Paths.get(latestOffsetFolderLocation, "0").toString
   hdfs.exists(new Path(latestOffsetFolderLocation)) shouldEqual true
   hdfs.exists(new Path(latestOffsetFileLocation)) shouldEqual true

   hdfs.copyToLocalFile(true, new Path(latestOffsetFileLocation), new Path(startOffsetFileLocation))
   hdfs.exists(new Path(latestOffsetFileLocation)) shouldEqual false

   val separateClient = new CosmosClientBuilder()
    .endpoint(cosmosEndpoint)
    .key(cosmosMasterKey)
    .buildClient()

   try {
    val splitContainer = separateClient
     .getDatabase(cosmosDatabase)
     .getContainer(cosmosContainer)

    val initialThroughput = splitContainer
     .readThroughput()
     .getProperties
     .getManualThroughput
    val newThroughputToForceSplits = (Math.ceil(initialThroughput.toDouble / 6000) * 2 * 10000).toInt
    val response = splitContainer
     .replaceThroughput(ThroughputProperties.createManualThroughput(newThroughputToForceSplits))

    response.getStatusCode shouldEqual 200

    val (splitParent, childRanges) = waitForSplit(splitContainer, initialFeedRanges)
    logInfo(s"Cached parent '$splitParent' split into '${childRanges.mkString(",")}'.")
    val partitionKeyDefinition = splitContainer.read().getProperties.getPartitionKeyDefinition

    for (sequenceNumber <- 51 to 100) {
     val id = getPartitionKeyValueInRange(childRanges.head, partitionKeyDefinition)
     val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
     objectNode.put("name", "Schrodinger's cat")
     objectNode.put("type", "cat")
     objectNode.put("age", 20)
     objectNode.put("sequenceNumber", sequenceNumber)
     objectNode.put("id", id)
     splitContainer.createItem(objectNode)
    }

    val cfgWithoutItemCountPerTriggerHint = cfg.filter(
     keyValuePair => !keyValuePair._1.equals("spark.cosmos.changeFeed.itemCountPerTriggerHint"))
    val expectedSecondBatchSequenceNumbers = (1 to 100).toSet -- firstBatchSequenceNumbers.toSet
    val secondBatchSequenceNumbers = mutable.ArrayBuffer.empty[Int]
    val postSplitDeadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(TestTimeoutInMinutes)
    var postSplitBatchNumber = 0

    while (secondBatchSequenceNumbers.toSet != expectedSecondBatchSequenceNumbers &&
     System.nanoTime() < postSplitDeadline) {

     postSplitBatchNumber += 1
     val remainingNanos = Math.max(1L, postSplitDeadline - System.nanoTime())
     val dataFrame = spark.read
      .format("cosmos.oltp.changeFeed")
      .options(cfgWithoutItemCountPerTriggerHint)
      .load()
     val rows = collectWithTimeout(
      dataFrame,
      s"post-split change feed batch '$postSplitBatchNumber'",
      remainingNanos)
     val batchSequenceNumbers = getSequenceNumbers(rows)
     batchSequenceNumbers.distinct.size shouldEqual batchSequenceNumbers.size
     secondBatchSequenceNumbers.toSet.intersect(batchSequenceNumbers.toSet) shouldBe empty
     batchSequenceNumbers.toSet.subsetOf(expectedSecondBatchSequenceNumbers) shouldEqual true
     secondBatchSequenceNumbers ++= batchSequenceNumbers

     if (secondBatchSequenceNumbers.toSet != expectedSecondBatchSequenceNumbers) {
      promoteLatestOffset(hdfs, latestOffsetFileLocation, startOffsetFileLocation)
     }
    }

    secondBatchSequenceNumbers.toSet shouldEqual expectedSecondBatchSequenceNumbers
   } finally {
    separateClient.close()
   }
  }
 }

 private def waitForSplit(
   splitContainer: com.azure.cosmos.CosmosContainer,
   initialFeedRanges: Seq[NormalizedRange]
 ): (NormalizedRange, Seq[NormalizedRange]) = {
  val deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(TestTimeoutInMinutes)
  var attempts = 0
  var currentRanges = Seq.empty[NormalizedRange]
  var splitResult = Option.empty[(NormalizedRange, Seq[NormalizedRange])]

  while (splitResult.isEmpty &&
    attempts < MaxSplitWaitAttempts &&
    System.nanoTime() < deadline) {

   attempts += 1
   currentRanges = splitContainer.getFeedRanges.asScala
    .map(SparkBridgeImplementationInternal.toNormalizedRange)
    .sortBy(_.min)
    .toSeq
   splitResult = initialFeedRanges
    .map(parent => parent -> currentRanges.filter(child => isStrictSubRange(parent, child)))
    .find(_._2.size >= 2)

   if (splitResult.isEmpty && attempts < MaxSplitWaitAttempts && System.nanoTime() < deadline) {
    logInfo("Offer replace still pending a split of one cached parent range - waiting for 1 second...")
    Thread.sleep(1000)
   }
  }

  splitResult.getOrElse(
   fail(s"No cached parent range split after '$attempts' attempts. " +
    s"Initial ranges: '${initialFeedRanges.mkString(",")}', current ranges: '${currentRanges.mkString(",")}'."))
 }

 private def collectWithTimeout(
   dataFrame: DataFrame,
   operation: String,
   timeoutNanos: Long = TimeUnit.MINUTES.toNanos(TestTimeoutInMinutes)
 ): Array[Row] = {
  val jobGroupId = s"spark-change-feed-split-${UUID.randomUUID()}"
  val collectExecutor = Executors.newSingleThreadExecutor(new ThreadFactory {
   override def newThread(runnable: Runnable): Thread = {
    val thread = new Thread(runnable, s"$jobGroupId-worker")
    thread.setDaemon(true)
    thread
   }
  })
  val collectFuture = collectExecutor.submit(new Callable[Array[Row]] {
   override def call(): Array[Row] = {
    spark.sparkContext.setJobGroup(jobGroupId, operation, interruptOnCancel = true)
    try {
     dataFrame.collect()
    } finally {
     spark.sparkContext.clearJobGroup()
    }
   }
  })
  var restoreInterrupt = false

  try {
   collectFuture.get(timeoutNanos, TimeUnit.NANOSECONDS)
  } catch {
   case _: TimeoutException =>
    spark.sparkContext.cancelJobGroup(jobGroupId)
    collectFuture.cancel(true)
    fail(s"Timed out while running '$operation'.")
   case error: InterruptedException =>
    spark.sparkContext.cancelJobGroup(jobGroupId)
    collectFuture.cancel(true)
    restoreInterrupt = true
    throw error
   case error: ExecutionException =>
    error.getCause match {
     case runtimeException: RuntimeException => throw runtimeException
     case fatalError: Error => throw fatalError
     case cause => throw new RuntimeException(cause)
    }
  } finally {
   collectExecutor.shutdownNow()
   try {
    if (!collectExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
     logWarning(s"Collection worker for '$operation' did not terminate within 30 seconds.")
    }
   } catch {
    case _: InterruptedException =>
     restoreInterrupt = true
   }
   if (restoreInterrupt) {
    Thread.currentThread().interrupt()
   }
  }
 }

 private def promoteLatestOffset(
   hdfs: org.apache.hadoop.fs.FileSystem,
   latestOffsetFileLocation: String,
   startOffsetFileLocation: String
 ): Unit = {
  val latestOffsetPath = new Path(latestOffsetFileLocation)
  val startOffsetPath = new Path(startOffsetFileLocation)
  hdfs.exists(latestOffsetPath) shouldEqual true
  if (hdfs.exists(startOffsetPath)) {
   hdfs.delete(startOffsetPath, false) shouldEqual true
  }
  hdfs.copyToLocalFile(true, latestOffsetPath, startOffsetPath)
  hdfs.exists(latestOffsetPath) shouldEqual false
  hdfs.exists(startOffsetPath) shouldEqual true
 }

 private def getSequenceNumbers(rows: Array[Row]): Seq[Int] = {
  val objectMapper = Utils.getSimpleObjectMapper
  rows.map(row =>
   objectMapper
    .readTree(row.getAs[String](CosmosTableSchemaInferrer.RawJsonBodyAttributeName))
    .get("sequenceNumber")
    .asInt())
 }

 private def isStrictSubRange(parent: NormalizedRange, child: NormalizedRange): Boolean = {
  child != parent &&
   child.min.compareTo(parent.min) >= 0 &&
   child.max.compareTo(parent.max) <= 0
 }

 private def getPartitionKeyValueInRange(
   targetRange: NormalizedRange,
   partitionKeyDefinition: PartitionKeyDefinition
 ): String = {
  var value = UUID.randomUUID().toString
  var effectiveRange = SparkBridgeImplementationInternal.partitionKeyToNormalizedRange(
   new PartitionKey(value),
   partitionKeyDefinition)
  var attempts = 1

  while (!isPartitionKeyInRange(effectiveRange, targetRange) &&
   attempts < MaxPartitionKeySearchAttempts) {

   attempts += 1
   value = UUID.randomUUID().toString
   effectiveRange = SparkBridgeImplementationInternal.partitionKeyToNormalizedRange(
   new PartitionKey(value),
   partitionKeyDefinition)
  }

  if (isPartitionKeyInRange(effectiveRange, targetRange)) {
   value
  } else {
   fail(s"Unable to find a partition key in range '$targetRange' after '$attempts' attempts.")
  }
 }

 private def isPartitionKeyInRange(
  effectiveRange: NormalizedRange,
  targetRange: NormalizedRange
 ): Boolean = {
  effectiveRange.min.compareTo(targetRange.min) >= 0 &&
   effectiveRange.min.compareTo(targetRange.max) < 0
 }

 //scalastyle:on magic.number
 //scalastyle:on multiple.string.literals
}
