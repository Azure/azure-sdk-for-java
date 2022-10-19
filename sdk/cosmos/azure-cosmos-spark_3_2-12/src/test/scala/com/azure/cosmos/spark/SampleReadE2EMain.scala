// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.SparkSession

/** sample test for query */
object SampleReadE2EMain {
  def main(args: Array[String]) {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val cosmosDatabase = "testDB"
    val cosmosContainer = "testContainer"
    val dummy: Option[CosmosItemsDataSource] = None

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
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "true"
    )

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    val df = spark.read.format("cosmos.oltp").options(cfg).load()
    df.show(numRows = 10)

    // With raw json as inference
    val cfgForRaw = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false"
    )

    val dfForRaw = spark.read.format("cosmos.oltp").options(cfgForRaw).load()
    dfForRaw.show(numRows = 10)
    val rawJson = dfForRaw.first.getAs[String](CosmosTableSchemaInferrer.RawJsonBodyAttributeName)
    val mapper = new ObjectMapper();
    mapper.readTree(rawJson);

    spark.close()
  }
}
