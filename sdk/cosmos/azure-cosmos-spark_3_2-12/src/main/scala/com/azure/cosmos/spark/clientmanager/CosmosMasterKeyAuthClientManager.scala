// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.clientmanager
import com.azure.cosmos.implementation.ResourceType
import com.azure.cosmos.spark.cosmosclient.CosmosClientConfiguration
import com.azure.cosmos.spark.{CosmosClientCache, CosmosClientCacheItem, CosmosReadConfig}

class CosmosMasterKeyAuthClientManager
    extends CosmosClientManager {
    override def getReadClient(
                                  resourceType: ResourceType,
                                  config: Map[String, String],
                                  readConfig: CosmosReadConfig,
                                  calledFrom: String): CosmosClientCacheItem = {

        CosmosClientCache(CosmosClientConfiguration(config, readConfig.forceEventualConsistency), None, calledFrom)
    }

    override def getCreateOrUpdateClient(
                                            resourceType: ResourceType,
                                            config: Map[String, String],
                                            readConfig: CosmosReadConfig,
                                            calledFrom: String): CosmosClientCacheItem = {
        CosmosClientCache(CosmosClientConfiguration(config, readConfig.forceEventualConsistency), None, calledFrom)
    }
}
