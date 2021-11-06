// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient

trait CosmosClientCacheItem extends AutoCloseable {
  def client: CosmosAsyncClient
}
