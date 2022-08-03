// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.{TestConfigurations, Utils}

import java.util.UUID

class SparkE2EThroughputControlITest extends IntegrationSpec with Spark with CosmosClient with AutoCleanableCosmosContainer {

  "spark throughput control" should "limit throughput usage" in {

    val throughputControlDatabaseId = "testThroughputControlDB"
    val throughputControlContainerId = "testThroughputControlContainer"

    cosmosClient.createDatabaseIfNotExists(throughputControlDatabaseId).block()
    val throughputControlDatabase = cosmosClient.getDatabase(throughputControlDatabaseId)
    throughputControlDatabase.createContainerIfNotExists(throughputControlContainerId, "/groupId").block()

    val cfg = Map("spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
      "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.throughputControl.enabled" -> "true",
      "spark.cosmos.throughputControl.name" -> "sparkTest",
      "spark.cosmos.throughputControl.targetThroughput" -> "6",
      "spark.cosmos.throughputControl.globalControl.database" -> throughputControlDatabaseId,
      "spark.cosmos.throughputControl.globalControl.container" -> throughputControlContainerId,
      "spark.cosmos.throughputControl.globalControl.renewIntervalInMS" -> "5000",
      "spark.cosmos.throughputControl.globalControl.expireIntervalInMS" -> "20000"
    )

    val newSpark = getSpark

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import spark.implicits._
    val spark = newSpark
    // scalastyle:on underscore.import
    // scalastyle:on import.grouping

    val df = Seq(
      ("Quark", "Quark", "Red", 1.0 / 2)
    ).toDF("particle name", "id", "color", "spin")

    df.write.format("cosmos.oltp").mode("Append").options(cfg).save()
    spark.read.format("cosmos.oltp").options(cfg).load()
  }

  "spark throughput control" should "limit throughput usage after updating targetThroughput" in {

    val throughputControlDatabaseId = "testThroughputControlDB"
    val throughputControlContainerId = "testThroughputControlContainer"

    cosmosClient.createDatabaseIfNotExists(throughputControlDatabaseId).block()
    val throughputControlDatabase = cosmosClient.getDatabase(throughputControlDatabaseId)
    throughputControlDatabase.createContainerIfNotExists(throughputControlContainerId, "/groupId").block()

    val cfg = Map("spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
      "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.throughputControl.enabled" -> "true",
      "spark.cosmos.throughputControl.name" -> "sparkTest",
      "spark.cosmos.throughputControl.targetThroughputThreshold" -> "0.9",
      "spark.cosmos.throughputControl.globalControl.database" -> throughputControlDatabaseId,
      "spark.cosmos.throughputControl.globalControl.container" -> throughputControlContainerId,
    )

    val newSpark = getSpark

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import spark.implicits._
    val spark = newSpark
    // scalastyle:on underscore.import
    // scalastyle:on import.grouping

    val df = Seq(
      ("Quark", "Quark", "Red", 1.0 / 2)
    ).toDF("particle name", "id", "color", "spin")

    df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

    spark
      .read
      .format("cosmos.oltp")
      .options(cfg + ("spark.cosmos.throughputControl.targetThroughputThreshold" -> "0.8"))
      .load()
  }

  "spark throughput control" should "be able to use a different account config" in {

    val throughputControlDatabaseId = "testThroughputControlDB"
    val throughputControlContainerId = "testThroughputControlContainer"

    val throughputControlClient =
      new CosmosClientBuilder()
       .endpoint(TestConfigurations.THROUGHPUT_CONTROL_ACCOUNT_HOST)
       .key(TestConfigurations.THROUGHPUT_CONTROL_MASTER_KEY)
       .buildAsyncClient()

    throughputControlClient.createDatabaseIfNotExists(throughputControlDatabaseId).block()

    val throughputControlDatabase = throughputControlClient.getDatabase(throughputControlDatabaseId)
    throughputControlDatabase.createContainerIfNotExists(throughputControlContainerId, "/groupId").block()

    try {
      // create few items ahead of time
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

      val cfg = Map("spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
        "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.read.inferSchema.enabled" -> "true",
        "spark.cosmos.read.maxItemCount" -> "1",
        "spark.cosmos.throughputControl.enabled" -> "true",
        "spark.cosmos.throughputControl.accountEndpoint" -> TestConfigurations.THROUGHPUT_CONTROL_ACCOUNT_HOST,
        "spark.cosmos.throughputControl.accountKey" -> TestConfigurations.THROUGHPUT_CONTROL_MASTER_KEY,
        "spark.cosmos.throughputControl.name" -> "sparkTest",
        "spark.cosmos.throughputControl.targetThroughput" -> "6",
        "spark.cosmos.throughputControl.globalControl.database" -> throughputControlDatabaseId,
        "spark.cosmos.throughputControl.globalControl.container" -> throughputControlContainerId,
        "spark.cosmos.throughputControl.globalControl.renewIntervalInMS" -> "5000",
        "spark.cosmos.throughputControl.globalControl.expireIntervalInMS" -> "20000"
      )

      val newSpark = getSpark

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val df = spark.read.format("cosmos.oltp.changeFeed").options(cfg).load()
      val rowsArray = df.collect()
      rowsArray should have size 50
    } finally {
      if (throughputControlClient != null) {
        throughputControlDatabase.delete().block()
        throughputControlClient.close()
      }
    }
  }
}
