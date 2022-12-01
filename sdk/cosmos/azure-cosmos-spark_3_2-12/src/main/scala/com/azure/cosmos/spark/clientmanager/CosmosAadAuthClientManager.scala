// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.clientmanager

import com.azure.cosmos.implementation.ResourceType
import com.azure.cosmos.spark.cosmosclient.controlplane.CosmosControlPlaneClientConfiguration
import com.azure.cosmos.spark.cosmosclient.dataplane.CosmosDataPlaneClientConfiguration
import com.azure.cosmos.spark.{CosmosClientCache, CosmosClientCacheItem, CosmosReadConfig}

private class CosmosAadAuthClientManager
    extends CosmosClientManager {
    override def getReadClient(
                                  resourceType: ResourceType,
                                  config: Map[String, String],
                                  readConfig: CosmosReadConfig,
                                  calledFrom: String): CosmosClientCacheItem = {
        if (resourceType == ResourceType.StoredProcedure
                || resourceType == ResourceType.Trigger
                || resourceType == ResourceType.UserDefinedFunction) {
            CosmosClientCache(CosmosControlPlaneClientConfiguration(config), None, calledFrom)
        } else  {
            CosmosClientCache(CosmosDataPlaneClientConfiguration(config, readConfig.forceEventualConsistency), None, calledFrom)
        }
    }

    override def getCreateOrUpdateClient(
                                            resourceType: ResourceType,
                                            config: Map[String, String],
                                            readConfig: CosmosReadConfig,
                                            calledFrom: String): CosmosClientCacheItem = {
        if (resourceType == ResourceType.Database
            || resourceType == ResourceType.DocumentCollection
            || resourceType == ResourceType.Offer
            || resourceType == ResourceType.StoredProcedure
            || resourceType == ResourceType.Trigger
            || resourceType == ResourceType.UserDefinedFunction) {

            CosmosClientCache(CosmosControlPlaneClientConfiguration(config), None, calledFrom)
        } else {
            CosmosClientCache(CosmosDataPlaneClientConfiguration(config, readConfig.forceEventualConsistency), None, calledFrom)
        }
    }
}
