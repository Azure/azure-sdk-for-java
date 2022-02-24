// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations

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

    val newSpark = getSpark()

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
}
