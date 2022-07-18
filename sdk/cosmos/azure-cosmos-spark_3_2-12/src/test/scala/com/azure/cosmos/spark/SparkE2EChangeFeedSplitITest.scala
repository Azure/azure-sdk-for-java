// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.ThroughputProperties
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.hadoop.fs.Path

import java.nio.file.Paths
import java.util.UUID

class SparkE2EChangeFeedSplitITest
 extends IntegrationSpec
  with Spark
  with CosmosClient
  with CosmosContainer
  with BasicLoggingTrait {

 //scalastyle:off multiple.string.literals
 //scalastyle:off magic.number

 "spark change feed query (incremental)" should "honor checkpoint location and read limit after partition split" in {
  val cosmosEndpoint = TestConfigurations.HOST
  val cosmosMasterKey = TestConfigurations.MASTER_KEY

  if (cosmosEndpoint.contains("localhost")) {
   logWarning("Skipping this test on emulator, because emulator doesn't allow splitting partitions")
  } else {
   val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

   for (sequenceNumber <- 1 to 50) {
    val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
    objectNode.put("name", "Shrodigner's cat")
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
    "spark.cosmos.changeFeed.batchCheckpointLocation" -> checkpointLocation
   )

   val df1 = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
   val rowsArray1 = df1.collect()
   // technically possible that even with 50 documents randomly distributed across 3 partitions some
   // has no documents
   // rowsArray should have size df.rdd.getNumPartitions
   rowsArray1.length > 0 shouldEqual true
   rowsArray1.length <= df1.rdd.getNumPartitions shouldEqual true

   val initialCount = rowsArray1.length

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

   val separateClient = new CosmosClientBuilder()
    .endpoint(cosmosEndpoint)
    .key(cosmosMasterKey)
    .buildClient();

   val initialThroughput = separateClient
    .getDatabase(cosmosDatabase)
    .getContainer(cosmosContainer)
    .readThroughput()
    .getProperties
    .getManualThroughput

   val newThroughputToForceSplits = (Math.ceil(initialThroughput.toDouble / 6000) * 2 * 10000).toInt;

   val response = separateClient
    .getDatabase(cosmosDatabase)
    .getContainer(cosmosContainer)
    .replaceThroughput(ThroughputProperties.createManualThroughput(newThroughputToForceSplits))

   response.getStatusCode shouldEqual 200

   // Disable FeedRange cache refresh to enforce that the cached feed ranges won't be aware of splits
   ContainerFeedRangesCache.overrideFeedRangeRefreshInterval(Int.MaxValue)

   try {
    val initialPartitionCount = df1.rdd.getNumPartitions
    var currentPartitionCount = separateClient
     .getDatabase(cosmosDatabase)
     .getContainer(cosmosContainer)
     .getFeedRanges()
     .size()

    while (currentPartitionCount < initialPartitionCount * 2) {
     logInfo(s"Offer replace still pending current Partition count '$currentPartitionCount' - " +
      s"target Partition count '${initialPartitionCount * 2}'- waiting for 1 second...")
     Thread.sleep(1000)

     currentPartitionCount = separateClient
      .getDatabase(cosmosDatabase)
      .getContainer(cosmosContainer)
      .getFeedRanges()
      .size()
    }

    val cfgWithoutItemCountPerTriggerHint = cfg.filter(keyValuePair => !keyValuePair._1.equals("spark.cosmos.changeFeed.itemCountPerTriggerHint"))
    val df2 = spark.read.format("cosmos.oltp.changeFeed").options(cfgWithoutItemCountPerTriggerHint).load()
    val rowsArray2 = df2.collect()
    rowsArray2 should have size 50 - initialCount
   } finally {
    // Resetting FeedRange cache refresh to avoid unintended side-effects for other tests
    ContainerFeedRangesCache.resetFeedRangeRefreshInterval()
   }
  }
 }

 //scalastyle:on magic.number
 //scalastyle:on multiple.string.literals
}