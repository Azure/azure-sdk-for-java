// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient
import org.apache.spark.TaskContext

trait CosmosClientCacheItem extends AutoCloseable {
  def client: CosmosAsyncClient
  def context: String
}
