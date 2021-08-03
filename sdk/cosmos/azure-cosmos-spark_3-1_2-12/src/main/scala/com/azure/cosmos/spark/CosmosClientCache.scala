// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, CosmosDaemonThreadFactory, SparkBridgeImplementationInternal}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{ConsistencyLevel, CosmosAsyncClient, CosmosClientBuilder, ThrottlingRetryOptions}
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast

import java.time.{Duration, Instant}
import java.util.ConcurrentModificationException
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object CosmosClientCache extends BasicLoggingTrait {
  // removing clients form the cache after 15 minutes
  // The clients won't be disposed - so any still running task can still keep using it
  // but it helps to allow the GC to clean-up the resources if no running task is using the client anymore
  private[this] val unusedClientTtlInMs = 15 * 60 * 1000
  private[this] val cleanupIntervalInSeconds = 60
  private[this] val cache = new TrieMap[CosmosClientConfiguration, CosmosClientCacheMetadata]
  private[this] val executorService:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
    new CosmosDaemonThreadFactory("CosmosClientCache"))

  this.executorService.scheduleWithFixedDelay(
    () => this.onCleanup(),
    this.cleanupIntervalInSeconds,
    this.cleanupIntervalInSeconds,
    TimeUnit.SECONDS)

  def apply(cosmosClientConfiguration: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]]): CosmosAsyncClient = {

    cache.get(cosmosClientConfiguration) match {
      case Some(clientCacheMetadata) =>
        clientCacheMetadata.lastRetrieved.set(Instant.now.toEpochMilli)
        clientCacheMetadata.client
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
  // scalastyle:off cyclomatic.complexity
  private[this] def syncCreate(cosmosClientConfiguration: CosmosClientConfiguration,
                               cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]])
  : CosmosAsyncClient = synchronized {
    cache.get(cosmosClientConfiguration) match {
      case Some(clientCacheMetadata) =>
        clientCacheMetadata.lastRetrieved.set(Instant.now.toEpochMilli)
        clientCacheMetadata.client
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

        // We saw incidents where even when Spark restarted Executors we haven't been able
        // to recover - most likely due to stale cache state being broadcast
        // Ideally the SDK would always be able to recover from stale cache state
        // but the main purpose of broadcasting teh cache state is to avoid peeks in metadata
        // RU usage when multiple workers/executors are all started at the same time
        // Skipping the broadcast cache state for retries should be safe - because not all executors
        // will be restarted at the same time - and it adds an additional layer of safety.
        val isTaskRetryAttempt: Boolean = TaskContext.get() != null && TaskContext.get().attemptNumber() > 0

        val effectiveClientStateHandle = if (cosmosClientStateHandle.isDefined && !isTaskRetryAttempt) {
          Some(cosmosClientStateHandle.get)
        } else {

          if (cosmosClientStateHandle.isDefined && isTaskRetryAttempt) {
            logInfo(s"Ignoring broadcast client state handle because Task is getting retried. " +
              s"Attempt Count: ${TaskContext.get().attemptNumber()}")
          }

          None
        }

        effectiveClientStateHandle match {
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
  // scalastyle:on cyclomatic.complexity

  private[this] def onCleanup(): Unit = {
    try {
      logInfo(s"-->onCleanup (${cache.size} clients)")
      val snapshot = cache.readOnlySnapshot()
      snapshot.foreach(pair => {
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
