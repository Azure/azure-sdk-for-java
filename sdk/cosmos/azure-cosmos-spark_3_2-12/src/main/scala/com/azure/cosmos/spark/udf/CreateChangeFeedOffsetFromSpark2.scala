// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.spark.{CosmosAccountConfig, CosmosChangeFeedConfig, CosmosClientCache, CosmosClientConfiguration, CosmosConfig, CosmosContainerConfig, CosmosReadConfig, Loan}
import org.apache.spark.sql.api.java.UDF5

@SerialVersionUID(1L)
class CreateChangeFeedOffsetFromSpark2 extends UDF5[String, String, String, Map[String, String], Map[Int, Long], String] {
  override def call
  (
    changeFeedQueryName: String,
    databaseResourceId: String,
    containerResourceId: String,
    userProvidedConfig: Map[String, String],
    tokens: Map[Int, Long]
  ): String = {

    val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userProvidedConfig)

    val clientConfig = CosmosAccountConfig.parseCosmosAccountConfig(effectiveUserConfig)
    val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
    val cosmosClientConfig = CosmosClientConfiguration(
      effectiveUserConfig,
      useEventualConsistency = false)

    Loan(CosmosClientCache(
      cosmosClientConfig,
      None,
      s"UDF CreateChangeFeedOffsetFromSpark2"
    ))
      .to(cosmosClientCacheItem => {

        SparkBridgeImplementationInternal.createChangeFeedOffsetFromSpark2(
          cosmosClientCacheItem.client,
          changeFeedQueryName,
          databaseResourceId,
          containerResourceId,
          userProvidedConfig,
          tokens
        )
      })
  }
}
