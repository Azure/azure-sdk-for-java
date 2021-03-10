// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.{ConsistencyLevel, CosmosAsyncClient, CosmosClientBuilder, ThrottlingRetryOptions}
import org.apache.spark.broadcast.Broadcast

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.ConcurrentModificationException
import scala.collection.concurrent.TrieMap

private[spark] object CosmosClientCache {
  private[this] val cache = new TrieMap[CosmosClientConfiguration, CosmosAsyncClient]

  def apply(cosmosClientConfiguration: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]]): CosmosAsyncClient = {
    cache.get(cosmosClientConfiguration) match {
      case Some(client) => client
      case None => syncCreate(cosmosClientConfiguration, cosmosClientStateHandle)
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

  private[this] def syncCreate(cosmosClientConfiguration: CosmosClientConfiguration,
                               cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]])
  : CosmosAsyncClient = synchronized {
    cache.get(cosmosClientConfiguration) match {
      case Some(client) => client
      case None =>
        var builder = new CosmosClientBuilder()
          .key(cosmosClientConfiguration.key)
          .endpoint(cosmosClientConfiguration.endpoint)
          .userAgentSuffix(cosmosClientConfiguration.applicationName)
            .throttlingRetryOptions(
                new ThrottlingRetryOptions()
                    .setMaxRetryAttemptsOnThrottledRequests(Int.MaxValue)
                    .setMaxRetryWaitTime(Duration.ofSeconds(Integer.MAX_VALUE/1000)))

        if (cosmosClientConfiguration.useEventualConsistency){
          builder = builder.consistencyLevel(ConsistencyLevel.EVENTUAL)
        }

        if (cosmosClientConfiguration.useGatewayMode){
          builder = builder.gatewayMode()
        }

        cosmosClientStateHandle match {
          case Some(handle) =>
            val metadataCache = handle.value
            SparkBridgeImplementationInternal.setMetadataCacheSnapshot(builder, metadataCache)
          case None => Unit
        }

        val client = builder.buildAsyncClient()

        cache.putIfAbsent(cosmosClientConfiguration, client) match {
          case None =>
            client
          case Some(_) =>
            throw new ConcurrentModificationException("Should not reach here because its synchronized")
        }
    }

  }
}
