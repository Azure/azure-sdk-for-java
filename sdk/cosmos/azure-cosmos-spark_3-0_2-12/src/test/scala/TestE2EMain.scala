/*
 * Copyright (c) Microsoft Corporation.  All rights reserved.
 */

package com.azure.cosmos.spark

import com.azure.cosmos.{ConsistencyLevel, CosmosClientBuilder}
import com.azure.cosmos.implementation.TestConfigurations
import org.apache.spark.sql.SparkSession
object TestE2EMain {

  def main(args: Array[String]) {

    println("Starting spark")

    val destCfg = Map("spark.cosmos.accountEndpoint" -> "https://test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "XYZ==",
      "spark.cosmos.database" -> "testDB",
      "spark.cosmos.container" -> "testContainer",
    )

    val client = new CosmosClientBuilder()
      .key(TestConfigurations.MASTER_KEY)
      .endpoint(TestConfigurations.HOST)
      .consistencyLevel(ConsistencyLevel.EVENTUAL)
      .buildAsyncClient();

    client.createDatabaseIfNotExists("testDB").block()
    client.getDatabase("testDB").createContainerIfNotExists("testContainer", "/id").block();
    client.close()

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    import spark.implicits._

    val df = Seq(
      (8, "bat"),
      (64, "mouse"),
      (-27, "horse")
    ).toDF("number", "word")

    df.printSchema()

    df.write.format("cosmos.items").mode("append").options {
      destCfg
    }.save()

    df.show()

    spark.close()
  }
}
