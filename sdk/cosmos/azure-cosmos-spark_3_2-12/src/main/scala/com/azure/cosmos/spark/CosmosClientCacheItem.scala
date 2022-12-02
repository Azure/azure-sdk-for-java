// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.cosmosclient.{CosmosClientProvider, CosmosSparkCatalogClient}

trait CosmosClientCacheItem extends AutoCloseable {
  def clientProvider: CosmosClientProvider
  def sparkCatalogClient: CosmosSparkCatalogClient
  def context: String
}
