// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.spark.{CosmosClientCache, CosmosClientCacheItem, CosmosClientConfiguration, CosmosConfig, Loan}
import org.apache.spark.sql.api.java.UDF4

@SerialVersionUID(1L)
class CreateChangeFeedOffsetFromSpark2 extends UDF4[String, String, Map[String, String], Map[Int, Long], String] {
  override def call
  (
    databaseResourceId: String,
    containerResourceId: String,
    userProvidedConfig: Map[String, String],
    tokens: Map[Int, Long]
  ): String = {

    val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userProvidedConfig)
    val cosmosClientConfig = CosmosClientConfiguration(
      effectiveUserConfig,
      useEventualConsistency = false)

    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          cosmosClientConfig,
          None,
          s"UDF CreateChangeFeedOffsetFromSpark2"
        ))
      ))
      .to(cosmosClientCacheItems => {

        SparkBridgeImplementationInternal.createChangeFeedOffsetFromSpark2(
          cosmosClientCacheItems(0).get.client,
          databaseResourceId,
          containerResourceId,
          tokens
        )
      })
  }
}
