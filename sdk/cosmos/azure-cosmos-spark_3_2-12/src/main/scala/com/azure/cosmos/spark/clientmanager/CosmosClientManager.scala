// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.clientmanager

import com.azure.cosmos.implementation.ResourceType
import com.azure.cosmos.spark.{CosmosClientCacheItem, CosmosReadConfig}

trait CosmosClientManager {
   def getReadClient(
                        resourceType: ResourceType,
                        config: Map[String, String],
                        readConfig: CosmosReadConfig,
                        calledFrom: String): CosmosClientCacheItem

   def getCreateOrUpdateClient(resourceType: ResourceType,
                               config: Map[String, String],
                               readConfig: CosmosReadConfig,
                               calledFrom: String): CosmosClientCacheItem
}
