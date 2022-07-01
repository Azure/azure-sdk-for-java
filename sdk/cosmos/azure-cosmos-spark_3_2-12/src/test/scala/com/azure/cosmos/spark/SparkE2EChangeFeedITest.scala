// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import org.apache.spark.sql.types.{BooleanType, IntegerType, StringType, StructField, StructType}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.udf.GetFeedRangeForPartitionKeyValue
import org.apache.hadoop.fs.Path

import java.nio.file.Paths

class SparkE2EChangeFeedITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
    with CosmosContainerWithRetention
    with BasicLoggingTrait {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  "spark change feed DataSource version" can "be determined" in {
    CosmosChangeFeedDataSource.version shouldEqual CosmosConstants.currentVersion
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
      "spark.cosmos.read.maxItemCount" -> "2",
      "spark.cosmos.read.inferSchema.enabled" -> "false"
    )

    val df = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
    val rowsArray = df.collect()
    rowsArray should have size 2
    df.schema.equals(
      ChangeFeedTable.defaultIncrementalChangeFeedSchemaForInferenceDisabled) shouldEqual true

    val cfgExplicit = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.read.maxItemCount" -> "1",
      "spark.cosmos.changeFeed.mode" -> "Incremental"
    )

    val dfExplicit = spark.read.format("cosmos.oltp.changeFeed").options(cfgExplicit).load()
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
      "spark.cosmos.read.maxItemCount" -> "1",
      "spark.cosmos.read.inferSchema.enabled" -> "false"
    )

    val customSchema = StructType(Array(
      StructField("id", StringType),
      StructField("name", StringType),
      StructField("type", StringType),
      StructField("age", IntegerType),
      StructField("isAlive", BooleanType)
    ))

    val df = spark.read.schema(customSchema).format("cosmos.oltp.changeFeed").options(cfg).load()
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
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.changeFeed.mode" -> "FullFidelity",
      "spark.cosmos.read.maxItemCount" -> "1",
      "spark.cosmos.changeFeed.startFrom" -> "NOW"
    )

    val df = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
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
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.read.maxItemCount" -> "200000",
      "spark.cosmos.changeFeed.startFrom" -> "Beginning",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> sinkContainerName,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "true"
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
        .format("cosmos.oltp.changeFeed")
        .options(readCfg)
        .load()
      val microBatchQuery = changeFeedDF
        .writeStream
        .format("cosmos.oltp")
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
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val validationDF = spark
      .read
      .format("cosmos.oltp")
      .options(validationCfg)
      .load()

    val rowCount = validationDF.count()

    rowCount shouldEqual 10

    validationDF
      .show(truncate = false)
  }

  "spark change feed query (incremental)" can "filter feed ranges" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    var lastId: String = ""
    for (i <- 0 to 100) {
      lastId = UUID.randomUUID().toString
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("index", i.toString)
      objectNode.put("id", lastId)
      container.createItem(objectNode).block()
    }

    spark.udf.register("GetFeedRangeForPartitionKey", new GetFeedRangeForPartitionKeyValue(), StringType)
    val pkDefinition = "{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"
    val dummyDf = spark.sql(s"SELECT GetFeedRangeForPartitionKey('$pkDefinition', '$lastId')")

    val feedRange = dummyDf
      .collect()(0)
      .getAs[String](0)

    logInfo(s"FeedRange from UDF: $feedRange")

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.maxItemCount" -> "2",
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.partitioning.feedRangeFilter" -> feedRange
    )

    val df = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
    df.rdd.getNumPartitions shouldEqual 1
    val rowsArray = df.collect()
    rowsArray should have size 1
    df.schema.equals(
      ChangeFeedTable.defaultIncrementalChangeFeedSchemaForInferenceDisabled) shouldEqual true
  }

  "spark change feed query (incremental)" should "honor checkpoint location and read limit" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

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

    val checkpointLocation = "/checkpoints/" + UUID.randomUUID().toString
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
    rowsArray1.size <= df1.rdd.getNumPartitions shouldEqual true

    val initialCount = rowsArray1.size

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

    val cfgWithoutItemCountPerTriggerHint = cfg.filter(keyValuePair => !keyValuePair._1.equals("spark.cosmos.changeFeed.itemCountPerTriggerHint"))
    val df2 = spark.read.format("cosmos.oltp.changeFeed").options(cfgWithoutItemCountPerTriggerHint).load()
    val rowsArray2 = df2.collect()
    rowsArray2 should have size 50 - initialCount
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
