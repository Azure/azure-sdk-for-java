// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import org.apache.spark.sql.types.{StringType, StructField}

class CosmosTableSchemaInferrerITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
    with AutoCleanableCosmosContainer {

  "it" should "add _itemIdentity in the schema if readMany filtering is enabled" in {
    for (runTimeFilteringEnabled <- Array(true, false)) {
      for (readManyFilteringEnabled <- Array(true, false)) {
        for (inferSchemaEnabled <- Array(true, false)) {
          val config = Map(
            "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
            "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> cosmosContainer,
            "spark.cosmos.read.inferSchema.enabled" -> inferSchemaEnabled.toString,
            "spark.cosmos.applicationName" -> "CosmosTableSchemaInferrerITest",
            "spark.cosmos.read.runtimeFiltering.enabled" -> runTimeFilteringEnabled.toString,
            "spark.cosmos.read.readManyFiltering.enabled" -> readManyFilteringEnabled.toString
          )

          val schema = Loan(
            List[Option[CosmosClientCacheItem]](
              Some(CosmosClientCache(
                CosmosClientConfiguration(config, true, ""),
                None,
                "CosmosTableSchemaInferrerITest.inferSchema"
              ))
            )).to(cosmosClientCacheItems => {
                CosmosTableSchemaInferrer.inferSchema(
                  cosmosClientCacheItems(0).get,
                  Option.empty[CosmosClientCacheItem],
                  config,
                  ItemsTable.defaultSchemaForInferenceDisabled)
          })

          if (readManyFilteringEnabled) {
            schema.fields should contain (StructField("_itemIdentity", StringType, true))
          } else {
            schema.fields should not contain (StructField("_itemIdentity", StringType, true))
          }
        }
      }
    }
  }
}
