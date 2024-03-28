// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.{CosmosClientCache, CosmosClientCacheItem, CosmosClientConfiguration}

import java.util.UUID

final class CosmosAsyncClientCacheItem
(
  private[spark] val effectiveUserConfig: Map[String, String],
  private[spark] val useEventualConsistency: Boolean,
  private[spark] val sparkEnvironmentInfo: String
) extends AutoCloseable with BasicLoggingTrait {
  private val id = UUID.randomUUID().toString
  logInfo(s"CosmosAsyncClientCacheItem '$id' created.")
  private lazy val cosmosClientConfig = {
    logDebug(s"CosmosAsyncClientCacheItem '$id' - creating  cosmosClientConfig")

    CosmosClientConfiguration(
      effectiveUserConfig,
      useEventualConsistency = useEventualConsistency,
      sparkEnvironmentInfo)
  }
  private lazy val cacheItem: CosmosClientCacheItem = {
    logDebug(s"CosmosAsyncClientCacheItem '$id' - creating  cacheItem")

    CosmosClientCache(
      cosmosClientConfig,
      None,
      s"CosmosAsyncClientCacheItem($id)")
  }
  def getClient: Object = cacheItem.cosmosClient
  override def close(): Unit = {
    cacheItem.close()
    logInfo(
      s"CosmosAsyncClientCacheItem '$id' - closed cacheItem '${cacheItem.context}' - "
        +s"references remaining on client: ${cacheItem.getRefCount}")

  }
}
