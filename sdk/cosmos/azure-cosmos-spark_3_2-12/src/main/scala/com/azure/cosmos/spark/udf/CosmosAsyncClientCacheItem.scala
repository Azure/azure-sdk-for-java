// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.spark.CosmosClientCacheItem

import java.lang.AutoCloseable

final class CosmosAsyncClientCacheItem(private[spark] val cacheItem: CosmosClientCacheItem) extends AutoCloseable {
  def getClient: CosmosAsyncClient = cacheItem.cosmosClient
  override def close(): Unit = cacheItem.close()
}
