// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.spark.{CosmosClientCache, CosmosClientCacheItem, CosmosClientConfiguration, CosmosConfig, CosmosContainerConfig, CosmosReadConfig, Loan}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.api.java.UDF2

@SerialVersionUID(1L)
class GetFeedRangesForContainer extends UDF2[Map[String, String], Option[Int], Array[String]] {
  override def call
  (
    userProvidedConfig: Map[String, String],
    targetedCount: Option[Int]
  ): Array[String] = {

    val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userProvidedConfig)
    var feedRanges = List[String]()
    val cosmosContainerConfig: CosmosContainerConfig =
      CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
    val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
    val cosmosClientConfig = CosmosClientConfiguration(
      effectiveUserConfig,
      readConsistencyStrategy = readConfig.readConsistencyStrategy,
      CosmosClientConfiguration.getSparkEnvironmentInfo(SparkSession.getActiveSession))
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          cosmosClientConfig,
          None,
          s"UDF GetFeedRangesForContainer"
        ))
      ))
      .to(cosmosClientCacheItems => {

        if (targetedCount.isEmpty) {
          feedRanges = SparkBridgeImplementationInternal.getFeedRangesForContainer(
            cosmosClientCacheItems.head.get.cosmosClient,
            cosmosContainerConfig.container,
            cosmosContainerConfig.database
          )
        } else {
          feedRanges = SparkBridgeImplementationInternal.trySplitFeedRanges(
            cosmosClientCacheItems.head.get.cosmosClient,
            cosmosContainerConfig.container,
            cosmosContainerConfig.database,
            targetedCount.get)
        }
      })
    feedRanges.toArray

  }
}
