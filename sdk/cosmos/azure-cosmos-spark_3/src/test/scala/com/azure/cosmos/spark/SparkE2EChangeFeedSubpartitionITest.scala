// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.udf.{GetFeedRangeForHierarchicalPartitionKeyValues}
import org.apache.spark.sql.types._

import java.util.UUID

class SparkE2EChangeFeedSubpartitionITest
  extends IntegrationSpec
    with SparkWithMetrics
    with CosmosClient
    with AutoCleanableCosmosContainerWithSubpartitions
    with BasicLoggingTrait
    with MetricAssertions {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

    "spark change feed query (incremental)" can "filter feed ranges with first level of hierarchical partition key" in {
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
            objectNode.put("tenantId", lastId)
            objectNode.put("userId", "userId1")
            objectNode.put("sessionId", "sessionId1")
            container.createItem(objectNode).block()
        }

        spark.udf.register("GetFeedRangeForPartitionKey", new GetFeedRangeForHierarchicalPartitionKeyValues(), StringType)
        val pkDefinition = "{\"paths\":[\"/tenantId\",\"/userId\",\"/sessionId\"],\"kind\":\"MultiHash\"}"
        val pkValues = "[\"" + lastId + "\"]"
        val dummyDf = spark.sql(s"SELECT GetFeedRangeForPartitionKey('$pkDefinition', '$pkValues')")

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

    "spark change feed query (incremental)" can "filter feed ranges with first and second level of hierarchical partition key" in {
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
            objectNode.put("tenantId", lastId)
            objectNode.put("userId", "userId1")
            objectNode.put("sessionId", "sessionId1")
            container.createItem(objectNode).block()
        }

        spark.udf.register("GetFeedRangeForPartitionKey", new GetFeedRangeForHierarchicalPartitionKeyValues(), StringType)
        val pkDefinition = "{\"paths\":[\"/tenantId\",\"/userId\",\"/sessionId\"],\"kind\":\"MultiHash\"}"
        val pkValues = "[\"" + lastId + "\", \"userId1\"]"
        val dummyDf = spark.sql(s"SELECT GetFeedRangeForPartitionKey('$pkDefinition', '$pkValues')")

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

    "spark change feed query (incremental)" can "filter feed ranges with all levels of hierarchical partition key" in {
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
            objectNode.put("tenantId", lastId)
            objectNode.put("userId", "userId1")
            objectNode.put("sessionId", "sessionId1")
            container.createItem(objectNode).block()
        }

        spark.udf.register("GetFeedRangeForPartitionKey", new GetFeedRangeForHierarchicalPartitionKeyValues(), StringType)
        val pkDefinition = "{\"paths\":[\"/tenantId\",\"/userId\",\"/sessionId\"],\"kind\":\"MultiHash\"}"
        val pkValues = "[\"" + lastId + "\", \"userId1\", \"sessionId1\"]"
        val dummyDf = spark.sql(s"SELECT GetFeedRangeForPartitionKey('$pkDefinition', '$pkValues')")

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

    //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
