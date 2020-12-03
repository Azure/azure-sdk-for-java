// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.TestConfigurations
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.SparkSession
import org.assertj.core.api.Assertions.assertThat
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import


// TODO moderakh we need to clean up databases after creation.
// TODO use facility from V4 SDk?
// TODO do proper clean up for spark session, client, etc

class SparkE2EWriteSpec extends IntegrationSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  // TODO: moderakh should we tag tests at the test class level or test method level?
  "basic dataframe" can "write to cosmos" taggedAs (RequiresCosmosEndpoint) in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val cosmosDatabase = "testDB"
    val cosmosContainer = UUID.randomUUID().toString

    val client = new CosmosClientBuilder()
      .endpoint(cosmosEndpoint)
      .key(cosmosMasterKey)
      .buildAsyncClient()

    client.createDatabaseIfNotExists(cosmosDatabase).block()
    client.getDatabase(cosmosDatabase).createContainerIfNotExists(cosmosContainer, "/id").block()

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer
    )

    // TODO: moderakh do we need to recreate spark for each test or should we use a common instance?
    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import spark.implicits._
    // scalastyle:on underscore.import
    // scalastyle:on import.grouping

    val df = Seq(
      (299792458, "speed of light")
    ).toDF("number", "word")
    df.printSchema()

    df.write.format("cosmos.items").mode("append").options(cfg).save()

    // verify data is written

    // TODO: moderakh note unless if we use an account with strong consistency there is no guarantee
    // that the write by spark is visible by the client query
    // wait for a second to allow replication is completed.
    Thread.sleep(1000)

    val results = client.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
      .queryItems("SELECT * FROM r", classOf[ObjectNode])
      .toIterable
      .asScala
      .toArray

    assertThat(results).hasSize(1)
    assertThat(results(0).get("number").asInt()).isEqualTo(299792458)
    assertThat(results(0).get("word").asText()).isEqualTo("speed of light")

    // TODO: moderakh develop the proper pattern for proper resource cleanup after test
    client.close()
    spark.close()
  }

  // TODO: moderakh should we tag tests at the test class level or test method level?
  "basic test" should  "fail" taggedAs (RequiresCosmosEndpoint) in {
    assertThat(1).isEqualTo(2)
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
