// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.spark.CosmosClientCacheItem

final class CosmosAsyncClientCacheItem(private[spark] val cacheItem: CosmosClientCacheItem) extends AutoCloseable {
  def getClient: Object = cacheItem.cosmosClient
  override def close(): Unit = cacheItem.close()
}
