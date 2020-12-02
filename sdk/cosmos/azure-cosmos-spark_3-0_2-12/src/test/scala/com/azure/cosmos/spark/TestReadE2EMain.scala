// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.{ConsistencyLevel, CosmosClientBuilder}
import org.apache.spark.sql.SparkSession

/** sample test for query */
object TestReadE2EMain {
  def main(args: Array[String]) {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val cosmosDatabase = "testDB"
    val cosmosContainer = "testContainer"

//    val client = new CosmosClientBuilder()
//      .endpoint(cosmosEndpoint)
//      .key(cosmosMasterKey)
//      .consistencyLevel(ConsistencyLevel.EVENTUAL)
//      .buildAsyncClient()
//
//    client.createDatabaseIfNotExists(cosmosDatabase).block()
//    client.getDatabase(cosmosDatabase).createContainerIfNotExists(cosmosContainer, "/id").block()
//    client.close()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer
    )

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    val df = spark.read.format("cosmos.items").options(cfg).load()
    df.where("number = 1").show()

    spark.close()
  }
}
