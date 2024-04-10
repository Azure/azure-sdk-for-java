// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.SparkBridgeInternal
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.udf.{CreateChangeFeedOffsetFromSpark2, CreateSpark2ContinuationsFromChangeFeedOffset, GetFeedRangeForPartitionKeyValue}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.functions
import org.apache.spark.sql.types._

import java.io.{BufferedReader, InputStreamReader}
import java.nio.file.Paths
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class SparkE2EChangeFeedITest
  extends IntegrationSpec
    with SparkWithDropwizardAndSlf4jMetrics
    with CosmosClient
    with CosmosContainerWithRetention
    with BasicLoggingTrait
    with MetricAssertions {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  "spark change feed DataSource version" can "be determined" in {
    CosmosChangeFeedDataSource.version shouldEqual CosmosConstants.currentVersion
  }

  "spark change feed query (incremental)" can "handle container recreate with batch checkpoint location (ignoring invalid offset)" in {
    runContainerRecreationScenarioWithBatchFileLocation(true)
  }

  "spark change feed query (incremental)" can "handle container recreate with batch checkpoint location (failing on invalid offset)" in {
    runContainerRecreationScenarioWithBatchFileLocation(false)
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
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.diagnostics" -> "feed_details"
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

  "spark change feed query (LatestVersion)" can "use default schema" in {
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
      "spark.cosmos.changeFeed.mode" -> "LatestVersion"
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

  "spark change feed query (all versions and deletes)" can "use default schema" in {
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
      "spark.cosmos.changeFeed.mode" -> "AllVersionsAndDeletes",
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

    CosmosClientMetrics.meterRegistry.isDefined shouldEqual true
    val meterRegistry = CosmosClientMetrics.meterRegistry.get

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

    assertMetrics(meterRegistry, "cosmos.client.op.latency", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.system.avgCpuLoad", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.req.gw", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.req.rntbd", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.rntbd", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.rntbd.addressResolution", expectedToFind = true)
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
    assert(!hdfs.exists(new Path(latestOffsetFileLocation)))

    val cfgWithoutItemCountPerTriggerHint = cfg.filter(keyValuePair => !keyValuePair._1.equals("spark.cosmos.changeFeed.itemCountPerTriggerHint"))
    val df2 = spark.read.format("cosmos.oltp.changeFeed").options(cfgWithoutItemCountPerTriggerHint).load()
    val rowsArray2 = df2.collect()
    rowsArray2 should have size 50 - initialCount
  }

  "spark change feed query (full fidelity)" should "honor checkpoint location" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val checkpointLocation = s"/tmp/checkpoints/${UUID.randomUUID().toString}"
    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.changeFeed.mode" -> "FullFidelity",
      "spark.cosmos.changeFeed.startFrom" -> "NOW",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.changeFeed.batchCheckpointLocation" -> checkpointLocation
    )

    val df1 = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
    val rowsArray1 = df1.collect()
    rowsArray1.length == 0 shouldEqual true

    df1.schema.equals(
      ChangeFeedTable.defaultFullFidelityChangeFeedSchemaForInferenceDisabled) shouldEqual true

    val hdfs = org.apache.hadoop.fs.FileSystem.get(spark.sparkContext.hadoopConfiguration)

    val startOffsetFolderLocation = Paths.get(checkpointLocation, "startOffset").toString
    val startOffsetFileLocation = Paths.get(startOffsetFolderLocation, "0").toString
    hdfs.exists(new Path(startOffsetFolderLocation)) shouldEqual true
    hdfs.exists(new Path(startOffsetFileLocation)) shouldEqual false

    val latestOffsetFolderLocation = Paths.get(checkpointLocation, "latestOffset").toString
    val latestOffsetFileLocation = Paths.get(latestOffsetFolderLocation, "0").toString
    hdfs.exists(new Path(latestOffsetFolderLocation)) shouldEqual true
    hdfs.exists(new Path(latestOffsetFileLocation)) shouldEqual true

    //  TODO - check for the offset structure to make sure it looks like the new lease format.

    hdfs.copyToLocalFile(true, new Path(latestOffsetFileLocation), new Path(startOffsetFileLocation))
    assert(!hdfs.exists(new Path(latestOffsetFileLocation)))

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    val createdObjectIds = new ArrayBuffer[String]()
    val replacedObjectIds = new ArrayBuffer[String]()
    val deletedObjectIds = new ArrayBuffer[String]()
    for (sequenceNumber <- 1 to 5) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("sequenceNumber", sequenceNumber)
      val id = UUID.randomUUID().toString
      objectNode.put("id", id)
      createdObjectIds += id
      if (sequenceNumber % 2 == 0) {
        replacedObjectIds += id
      }
      if (sequenceNumber % 3 == 0) {
        deletedObjectIds += id
      }
      container.createItem(objectNode).block()
    }

    for (id <- replacedObjectIds) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "dog")
      objectNode.put("age", 25)
      objectNode.put("id", id)
      container.replaceItem(objectNode, id, new PartitionKey(id)).block()
    }

    for (id <- deletedObjectIds) {
      container.deleteItem(id, new PartitionKey(id)).block()
    }

    // wait for the log store to get these changes
    Thread.sleep(2000)

    val df2 = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
    val groupedFrame = df2.groupBy(CosmosTableSchemaInferrer.OperationTypeAttributeName)
      .agg(functions.collect_list("id").as("ids"))

    val collectedFrame = groupedFrame.collect()
    collectedFrame.foreach(row => {
      val wrappedArray = row.get(1).asInstanceOf[mutable.WrappedArray[String]]
      val array = wrappedArray.array
      row.get(0) match {
        case "create" =>
          validateArraysUnordered(createdObjectIds, array)
        case "replace" =>
          validateArraysUnordered(replacedObjectIds, array)
        case "delete" =>
          validateArraysUnordered(deletedObjectIds, array)
      }
    })
  }

  "spark change feed query (incremental)" can "proceed with simulated Spark2 Checkpoint" in {
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

    // convert ChangeFeedOffset to simulated Spark 2.4 continuation
    val fileContent = this.readFileContentAsString(hdfs, latestOffsetFileLocation)
    val indexOfNewLine = fileContent.indexOf("\n")
    fileContent.substring(0, indexOfNewLine) shouldEqual "v1"
    val offsetJson = fileContent.substring(indexOfNewLine + 1)
    val changeFeedStateEncoded = ChangeFeedOffset.fromJson(offsetJson).changeFeedState
    val changeFeedState = ChangeFeedState.fromString(changeFeedStateEncoded)
    val databaseResourceIdAndTokenMap = calculateTokenMap(changeFeedState, cfg)
    // calling UDF to get migrated offset
    val migratedOffset = new CreateChangeFeedOffsetFromSpark2()
      .call(
        databaseResourceIdAndTokenMap._1,
        changeFeedState.getContainerRid,
        cfg,
        databaseResourceIdAndTokenMap._2
      )

    val tokenMapAfterBackAndForthConversion: Map[Int, Long] = new CreateSpark2ContinuationsFromChangeFeedOffset()
      .call(
        cfg,
        migratedOffset
      )

    tokenMapAfterBackAndForthConversion.size shouldBe databaseResourceIdAndTokenMap._2.size
    databaseResourceIdAndTokenMap
      ._2
      .foreach(pkRangeLsnPair => {
        tokenMapAfterBackAndForthConversion.get(pkRangeLsnPair._1).isDefined shouldEqual true
        tokenMapAfterBackAndForthConversion.get(pkRangeLsnPair._1).get shouldEqual pkRangeLsnPair._2
      })

    if (hdfs.exists(new Path(startOffsetFileLocation))) {
      hdfs.copyToLocalFile(true, new Path(startOffsetFileLocation), new Path(startOffsetFileLocation + ".bak"))
    }

    hdfs.copyToLocalFile(true, new Path(latestOffsetFileLocation), new Path(latestOffsetFileLocation + ".bak"))
    assert(!hdfs.exists(new Path(latestOffsetFileLocation)))

    val outputStream = hdfs.create(new Path(startOffsetFileLocation), true)
    outputStream.writeBytes(migratedOffset)
    outputStream.flush()
    outputStream.close()
    hdfs.delete(new Path(latestOffsetFolderLocation), true)

    val cfgWithoutItemCountPerTriggerHint = cfg.filter(keyValuePair => !keyValuePair._1.equals("spark.cosmos.changeFeed.itemCountPerTriggerHint"))
    val df2 = spark.read.format("cosmos.oltp.changeFeed").options(cfgWithoutItemCountPerTriggerHint).load()
    val rowsArray2 = df2.collect()
    rowsArray2 should have size 50 - initialCount
  }

  "spark change feed query (incremental)" should "honor checkpoint location even when triggering multiple planInputPartitions calls" in {
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
    assert(!hdfs.exists(new Path(latestOffsetFileLocation)))

    val cfgWithoutItemCountPerTriggerHint = cfg.filter(keyValuePair => !keyValuePair._1.equals("spark.cosmos.changeFeed.itemCountPerTriggerHint"))
    val df2 = spark.read.format("cosmos.oltp.changeFeed").options(cfgWithoutItemCountPerTriggerHint).load()
    println(df2.queryExecution.logical)
    assert(!hdfs.exists(new Path(latestOffsetFileLocation)))
    println(df2.queryExecution.optimizedPlan)
    assert(!hdfs.exists(new Path(latestOffsetFileLocation)))
    println(df2.queryExecution.sparkPlan)
    // physical plan will trigger calling planInputPartitions (so, latestOffset file gets created)
    assert(hdfs.exists(new Path(latestOffsetFileLocation)))

    for (sequenceNumber <- 51 to 60) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("sequenceNumber", sequenceNumber)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    // Ensure that even after some new changes happen, planInputPartitions can be called again
    println(df2.queryExecution.sparkPlan)
    assert(hdfs.exists(new Path(latestOffsetFileLocation)))

    val rowsArray2 = df2.collect()

    // What is important is that at this point not all changes have been consumed yet
    // the 10 additional changes after initial planInputPartitions invocation are not consumed
    // yet because the latestOffset file was created before making those changes
    rowsArray2 should have size 50 - initialCount

    hdfs.exists(new Path(startOffsetFolderLocation)) shouldEqual true
    hdfs.exists(new Path(startOffsetFileLocation)) shouldEqual true
    val startOffsetFileBackupLocation = Paths.get(startOffsetFolderLocation, "backup").toString
    hdfs.copyToLocalFile(true, new Path(startOffsetFileLocation), new Path(startOffsetFileBackupLocation))
    hdfs.exists(new Path(startOffsetFileLocation)) shouldEqual false

    var remainingFromLastBatchOfTen = 10;
    while(remainingFromLastBatchOfTen > 0) {
      hdfs.copyToLocalFile(true, new Path(startOffsetFileBackupLocation), new Path(startOffsetFileLocation))
      hdfs.delete(new Path(latestOffsetFileLocation), true)

      val df3 = spark.read.format("cosmos.oltp.changeFeed").options(cfgWithoutItemCountPerTriggerHint).load()
      val rowsArray3 = df3.collect()
      remainingFromLastBatchOfTen -= rowsArray3.length


      if (remainingFromLastBatchOfTen != 0) {
        logWarning(s"Still waiting for $remainingFromLastBatchOfTen changes to be processed. Waiting 500ms before retry...")
        Thread.sleep(500)
      }
    }
  }

  private def validateArraysUnordered(inputArrayBuffer : ArrayBuffer[String], outputArray: Array[String]) : Unit = {
    assert(inputArrayBuffer.length == outputArray.length)
    val set: mutable.HashSet[String] = new mutable.HashSet[String]()
    for (element <- inputArrayBuffer) {
      set.add(element)
    }
    for (element <- outputArray) {
      assert(set.contains(element))
    }
  }

  private[this] def readFileContentAsString(fileSystem: FileSystem, fileName: String): String = {
    val reader: BufferedReader = new BufferedReader(
      new InputStreamReader(fileSystem.open(new Path(fileName))))

    val fileContent: StringBuilder = new StringBuilder()
    var line: String = reader.readLine()
    while (line != null) {
      if (fileContent.nonEmpty) {
        fileContent.append("\n")
      }

      fileContent.append(line)
      line = reader.readLine()
    }

    reader.close()

    fileContent.toString
  }

  private def calculateTokenMap
  (
    changeFeedState: ChangeFeedState, cfg: Map[String, String]
  ): (String, Map[Int, Long]) = {

    val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, cfg)
    val cosmosClientConfig = CosmosClientConfiguration(
      effectiveUserConfig,
      useEventualConsistency = false,
      sparkEnvironmentInfo = "")

    val tokenMap = scala.collection.mutable.Map[Int, Long]()
    var databaseResourceId = "n/a"

    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          cosmosClientConfig,
          None,
          s"E2ETest calculateTokenMap"))
      ))
      .to(cosmosClientCacheItems => {

        databaseResourceId = cosmosClientCacheItems.head.get
          .cosmosClient
          .getDatabase(cosmosDatabase)
          .read()
          .block()
          .getProperties
          .getResourceId

        val container = cosmosClientCacheItems.head.get
          .cosmosClient
          .getDatabase(cosmosDatabase)
          .getContainer(cosmosContainer)

        val pkRanges = SparkBridgeInternal
          .getPartitionKeyRanges(container)

        pkRanges
          .foreach(pkRange => {
            val pkRangeId: Int = pkRange.getId.toInt
            val effectiveRange = pkRange.toRange

            val filteredCompositeContinuations = changeFeedState
              .getContinuation
              .getCurrentContinuationTokens
              .filter(compositeContinuationToken => effectiveRange.equals(compositeContinuationToken.getRange))

            filteredCompositeContinuations should have size 1

            val quotedToken = filteredCompositeContinuations.head.getToken
            val lsn: Long =  Math.max(0, quotedToken.substring(1, quotedToken.length - 1).toLong)

            tokenMap += (pkRangeId -> lsn)
          })
      })

    (databaseResourceId, tokenMap.toMap)
  }

  private def runContainerRecreationScenarioWithBatchFileLocation(ignoreOffsetWhenInvalid: Boolean): Unit = {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    var container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    for (sequenceNumber <- 1 to 50) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("sequenceNumber", sequenceNumber)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    // clearing metadata cache to avoid using cached endLSN (which would result)
    // in not getting all records form initial change feed batch (eventually it would work)
    PartitionMetadataCache.clearCache()

    val checkpointLocation = s"/tmp/checkpoints/${UUID.randomUUID().toString}"
    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.changeFeed.startFrom" -> "Beginning",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.changeFeed.batchCheckpointLocation" -> checkpointLocation,
      "spark.cosmos.changeFeed.batchCheckpointLocation.ignoreWhenInvalid" -> ignoreOffsetWhenInvalid.toString
    )

    val df1 = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
    val rowsArray1 = df1.collect()
    rowsArray1.length shouldEqual 50

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

    if (hdfs.exists(new Path(startOffsetFileLocation))) {
      val startOffsetJson = readFileContentAsString(hdfs, startOffsetFileLocation)
      logInfo(s"StartOffset: $startOffsetJson")
      hdfs.copyToLocalFile(true, new Path(startOffsetFileLocation), new Path(startOffsetFileLocation + ".bak"))
    } else {
      logInfo(s"StartOffset: n/a")
    }
    hdfs.exists(new Path(startOffsetFileLocation)) shouldEqual false


    hdfs.exists(new Path(latestOffsetFileLocation)) shouldEqual true
    val latestOffsetJson = readFileContentAsString(hdfs, latestOffsetFileLocation)
    logInfo(s"LatestOffset: $latestOffsetJson")
    hdfs.copyToLocalFile(true, new Path(latestOffsetFileLocation), new Path(startOffsetFileLocation))
    hdfs.exists(new Path(latestOffsetFileLocation)) shouldEqual false
    hdfs.exists(new Path(startOffsetFileLocation)) shouldEqual true
    hdfs.delete(new Path(latestOffsetFolderLocation), true)

    logInfo("Copied LatestOffset -> StartOffset")

    val deleteResponse = container.delete().block()
    deleteResponse.getStatusCode shouldEqual 204

    this.createContainerCore()
    logInfo("Recreated container")

    container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    for (sequenceNumber <- 1 to 20) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("sequenceNumber", sequenceNumber)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    // clearing metadata cache to avoid using cached endLSN (which would result)
    // in not getting all records form initial change feed batch (eventually it would work)
    PartitionMetadataCache.clearCache()

    if (ignoreOffsetWhenInvalid) {
      val df2 = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
      val rowsArray2 = df2.collect()
      rowsArray2 should have size 20
    } else {
      try {
        val df2 = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
        df2.collect()

        fail("Should have thrown an IllegalStateException")
      } catch {
        case _: IllegalStateException => logInfo("Got expected IllegalStateException")
        case t: Exception => fail(s"Unexpected exception $t")
      }
    }
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
