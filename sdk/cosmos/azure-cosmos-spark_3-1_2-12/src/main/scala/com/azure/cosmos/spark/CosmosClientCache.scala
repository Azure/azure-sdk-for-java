// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.batch.FlushBuffersItemOperation
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, CosmosDaemonThreadFactory, SparkBridgeImplementationInternal}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{ConsistencyLevel, CosmosAsyncClient, CosmosClientBuilder, CosmosItemOperation, ThrottlingRetryOptions}
import org.apache.spark.broadcast.Broadcast
import reactor.core.publisher.FluxSink

import java.time.{Duration, Instant}
import java.util.ConcurrentModificationException
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object CosmosClientCache extends BasicLoggingTrait {
  private[this] val unusedClientTtlInMs = 5 * 60 * 1000
  private[this] val cleanupIntervalInSeconds = 60
  private[this] val cache = new TrieMap[CosmosClientConfiguration, CosmosClientCacheMetadata]
  private[this] val executorService:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
    new CosmosDaemonThreadFactory("CosmosClientCache"));

  this.executorService.scheduleWithFixedDelay(
    () => this.onCleanup,
    this.cleanupIntervalInSeconds,
    this.cleanupIntervalInSeconds,
    TimeUnit.SECONDS);

  def apply(cosmosClientConfiguration: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]]): CosmosAsyncClient = {
    cache.get(cosmosClientConfiguration) match {
      case Some(clientCacheMetadata) => {
        clientCacheMetadata.lastRetrieved.set(Instant.now.toEpochMilli)
        clientCacheMetadata.client
      }
      case None => syncCreate(cosmosClientConfiguration, cosmosClientStateHandle)
    }
  }

  def purge(cosmosClientConfiguration: CosmosClientConfiguration): Unit = {
    cache.get(cosmosClientConfiguration) match {
      case None => Unit
      case Some(existingClientCacheMetadata) =>
        cache.remove(cosmosClientConfiguration) match {
          case None => Unit
          case Some(_) => existingClientCacheMetadata.client.close()
        }
    }
  }

  // scalastyle:off method.length
  private[this] def syncCreate(cosmosClientConfiguration: CosmosClientConfiguration,
                               cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]])
  : CosmosAsyncClient = synchronized {
    cache.get(cosmosClientConfiguration) match {
      case Some(clientCacheMetadata) => {
        clientCacheMetadata.lastRetrieved.set(Instant.now.toEpochMilli)
        clientCacheMetadata.client
      }
      case None =>
        var builder = new CosmosClientBuilder()
          .key(cosmosClientConfiguration.key)
          .endpoint(cosmosClientConfiguration.endpoint)
          .userAgentSuffix(cosmosClientConfiguration.applicationName)
            .throttlingRetryOptions(
                new ThrottlingRetryOptions()
                    .setMaxRetryAttemptsOnThrottledRequests(Int.MaxValue)
                    .setMaxRetryWaitTime(Duration.ofSeconds((Integer.MAX_VALUE/1000) - 1)))

        if (cosmosClientConfiguration.useEventualConsistency){
          builder = builder.consistencyLevel(ConsistencyLevel.EVENTUAL)
        }

        if (cosmosClientConfiguration.useGatewayMode){
          builder = builder.gatewayMode()
        }

        if (cosmosClientConfiguration.preferredRegionsList.isDefined) {
          builder.preferredRegions(cosmosClientConfiguration.preferredRegionsList.get.toList.asJava)
        }

        cosmosClientStateHandle match {
          case Some(handle) =>
            val metadataCache = handle.value
            SparkBridgeImplementationInternal.setMetadataCacheSnapshot(builder, metadataCache)
          case None => Unit
        }

        val client = builder.buildAsyncClient()
        val epochNowInMs = Instant.now.toEpochMilli
        val newClientCacheEntry = CosmosClientCacheMetadata(
          client,
          new AtomicLong(epochNowInMs),
          new AtomicLong(epochNowInMs))

        cache.putIfAbsent(cosmosClientConfiguration, newClientCacheEntry) match {
          case None =>
            client
          case Some(_) =>
            throw new ConcurrentModificationException("Should not reach here because its synchronized")
        }
    }
  }
  // scalastyle:on method.length

  private[this] def onCleanup(): Unit = {
    try {
      logInfo(s"-->onCleanup (${cache.size} clients)")
      val snapshot = cache.foreach((pair) => {
        val clientConfig = pair._1
        val clientMetadata = pair._2

        if (clientMetadata.lastRetrieved.get() < Instant.now.toEpochMilli - unusedClientTtlInMs) {
          // Only remove the client from the cache - don't purge or close!!! (because the client retrieved 15 minutes
          // ago might still be used
          logInfo(s"Removing client due to inactivity from the cache - ${clientConfig.endpoint}, " +
            s"${clientConfig.applicationName}, ${clientConfig.preferredRegionsList}, ${clientConfig.useGatewayMode}, " +
            s"${clientConfig.useEventualConsistency}")
          cache.remove(clientConfig)
        }
      })
    }
    catch {
      case t: Throwable =>
        logError("Callback invocation 'onCleanup' failed.", t)
    }
  }

  private[this] case class CosmosClientCacheMetadata
  (
    client: CosmosAsyncClient,
    lastRetrieved: AtomicLong,
    created: AtomicLong
  )
}
