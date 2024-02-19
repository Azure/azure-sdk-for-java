// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.spark.{CosmosClientCache, CosmosClientConfiguration, CosmosConfig}
import org.apache.spark.sql.SparkSession

object CosmosAsyncClientCache {
  def getCosmosClientFromCache(userProvidedConfig: Map[String, String]): CosmosAsyncClientCacheItem = {
    val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userProvidedConfig)
    val cosmosClientConfig = CosmosClientConfiguration(
      effectiveUserConfig,
      useEventualConsistency = false,
      CosmosClientConfiguration.getSparkEnvironmentInfo(SparkSession.getActiveSession))

    new CosmosAsyncClientCacheItem(
      CosmosClientCache(cosmosClientConfig, None, "CosmosAsyncClientCache.getCosmosClientFromCache"))
  }
}
