// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
// scalastyle:off underscore.import
// scalastyle:on underscore.import


// TODO moderakh rely on the shared database/container for the tests to avoid creating many
// TODO moderakh we need to clean up databases after creation.
// TODO use facility from V4 SDk?
// TODO do proper clean up for spark session, client, etc
class SparkE2EWriteSpec extends IntegrationSpec with Spark with CosmosClient with AutoCleanableCosmosContainer {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  // TODO: moderakh should we tag tests at the test class level or test method level?
  "basic dataframe" can "write to cosmos" taggedAs (RequiresCosmosEndpoint) in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer
    )

    val newSpark = getSpark()

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import spark.implicits._
    val spark = newSpark
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

    val results = readAllItems().toArray

    results should have size 1
    results(0).get("number").asInt() shouldEqual 299792458
    results(0).get("word").asText() shouldEqual "speed of light"

    // TODO: moderakh develop the proper pattern for proper resource cleanup after test
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
