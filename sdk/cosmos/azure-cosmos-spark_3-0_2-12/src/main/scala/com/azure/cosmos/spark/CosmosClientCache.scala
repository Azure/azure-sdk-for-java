// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncClient, CosmosClientBuilder}
import com.azure.cosmos.implementation.SparkBridgeInternal

import scala.collection.concurrent.TrieMap

private[spark] object CosmosClientCache {
    private val cache = new TrieMap[CosmosClientConfiguration, CosmosAsyncClient]

    def apply(cosmosClientConfiguration: CosmosClientConfiguration): CosmosAsyncClient = {
        cache.get(cosmosClientConfiguration) match {
            case Some(client) => client
            case None =>
                val builder = new CosmosClientBuilder()
                    .key(cosmosClientConfiguration.key)
                    .endpoint(cosmosClientConfiguration.endpoint)
                    .consistencyLevel(cosmosClientConfiguration.consistencyLevel)

                val client = builder.buildAsyncClient()

                cache.putIfAbsent(cosmosClientConfiguration, client) match {
                    case None =>
                        client
                    case Some(existingCosmosClient) =>
                        // Reuse the pre-existing one, means a concurrent thread put it first
                        client.close()
                        existingCosmosClient
                }
        }
    }

    def purge(cosmosClientConfiguration: CosmosClientConfiguration): Unit = {
        cache.get(cosmosClientConfiguration) match {
            case None => Unit
            case Some(existingClient) =>
                cache.remove(cosmosClientConfiguration) match {
                    case None => Unit
                    case Some(_) => existingClient.close()
                }
        }
    }
}
