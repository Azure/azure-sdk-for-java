// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.spark.{CosmosClientCache, CosmosClientConfiguration, CosmosConfig}
import org.apache.spark.sql.SparkSession

/**
 * Helper API to retrieve the CosmosAsyncClient instance used by the connector internally
 */
object CosmosAsyncClientCache {
  /**
   * This API can be used by Spark apps to retrieve the same CosmosAsyncClient instance that
   * the Cosmos DB Spark connector is usng internally as well. It can help avoiding the need
   * to instantiate one additional CosmosAsyncClient in the app for the same configuration
   * the Spark connector internally also uses.
   * NOTE: The returned client is from the shaded package - so `azure_cosmos_spark.com.azure.CosmosAsyncClient`
   * @param userProvidedConfig the configuration dictionary also used in Spark APIs to authenticate
   * @return the shaded CosmosAsyncClient that is also used internally by the Spark connector
   */
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
