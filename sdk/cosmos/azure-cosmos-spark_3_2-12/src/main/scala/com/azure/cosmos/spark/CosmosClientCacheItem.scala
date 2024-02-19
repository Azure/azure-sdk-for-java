// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.spark.catalog.CosmosCatalogClient

private[spark] trait CosmosClientCacheItem extends AutoCloseable {
  def cosmosClient: CosmosAsyncClient
  def sparkCatalogClient: CosmosCatalogClient
  def context: String
}
