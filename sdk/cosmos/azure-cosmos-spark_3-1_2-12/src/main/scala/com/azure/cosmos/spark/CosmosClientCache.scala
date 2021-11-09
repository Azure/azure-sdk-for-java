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
import java.util.concurrent.{ConcurrentHashMap, Executors, ScheduledExecutorService, TimeUnit}
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals
private[spark] object CosmosClientCache extends BasicLoggingTrait {
  // removing clients from the cache after 15 minutes
  // The clients won't be disposed - so any still running task can still keep using it
  // but it helps to allow the GC to clean-up the resources if no running task is using the client anymore
  private[this] val unusedClientTtlInMs = 15 * 60 * 1000
  private[this] val cleanupIntervalInSeconds = 1 * 60
  private[this] val cache = new TrieMap[ClientConfigurationWrapper, CosmosClientCacheMetadata]
  private[this] val toBeClosedWhenNotActiveAnymore =  new ConcurrentHashMap[CosmosClientCacheMetadata, java.lang.Boolean]
  private[this] val executorService:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
    new CosmosDaemonThreadFactory("CosmosClientCache"))

  this.executorService.scheduleWithFixedDelay(
    () => this.onCleanup(),
    this.cleanupIntervalInSeconds,
    this.cleanupIntervalInSeconds,
    TimeUnit.SECONDS)

  def apply(cosmosClientConfiguration: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
            calledFrom: String): CosmosClientCacheItem = {

    val ownerInfo = OwnerInfo(calledFrom)

    cache.get(ClientConfigurationWrapper(cosmosClientConfiguration)) match {
      case Some(clientCacheMetadata) => clientCacheMetadata.createCacheItemForReuse(ownerInfo)
      case None => syncCreate(cosmosClientConfiguration, cosmosClientStateHandle, ownerInfo)
    }
  }

  def ownerInformation(cosmosClientConfiguration: CosmosClientConfiguration): String = {
    cache.get(ClientConfigurationWrapper(cosmosClientConfiguration)) match {
      case None => ""
      case Some(existingClientCacheMetadata) => existingClientCacheMetadata
        .owners
        .keys
        .mkString(", ")
    }
  }

  def purge(cosmosClientConfiguration: CosmosClientConfiguration): Unit = {
    purgeImpl(ClientConfigurationWrapper(cosmosClientConfiguration))
  }

  private[this]def purgeImpl(clientConfigWrapper: ClientConfigurationWrapper): Unit = {
    cache.get(clientConfigWrapper) match {
      case None => Unit
      case Some(existingClientCacheMetadata) =>
        cache.remove(clientConfigWrapper) match {
          case None => Unit
          case Some(_) =>
            // there is a race condition here - technically between the check in onCleanup
            // when the client wasn't retrieved for certain period of time
            // and it wasn't actively used anymore someone could have
            // retrieved it form the cache before we remove it here
            // so if it is actively used now we need to keep a reference and close it
            // when it isn't used anymore
            if (existingClientCacheMetadata.refCount.get() == 0) {
              existingClientCacheMetadata.client.close()
            } else {
              toBeClosedWhenNotActiveAnymore.put(existingClientCacheMetadata, true)
            }
        }
    }
  }

  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  private[this] def syncCreate(cosmosClientConfiguration: CosmosClientConfiguration,
                               cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
                               ownerInfo: OwnerInfo)
  : CosmosClientCacheItem = synchronized {
    val clientConfigWrapper = ClientConfigurationWrapper(cosmosClientConfiguration)
    cache.get(clientConfigWrapper) match {
      case Some(clientCacheMetadata) => clientCacheMetadata.createCacheItemForReuse(ownerInfo)
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
        // but the main purpose of broadcasting the cache state is to avoid peeks in metadata
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
        val owners = new TrieMap[OwnerInfo, Option[Boolean]]
        owners.put(ownerInfo, None)

        val newClientCacheEntry = CosmosClientCacheMetadata(
          client,
          new AtomicLong(epochNowInMs),
          new AtomicLong(epochNowInMs),
          new AtomicLong(epochNowInMs),
          new AtomicLong(1),
          owners
        )

        cache.putIfAbsent(clientConfigWrapper, newClientCacheEntry) match {
          case None => new CacheItemImpl(client, newClientCacheEntry, ownerInfo)
          case Some(_) =>
            throw new ConcurrentModificationException("Should not reach here because its synchronized")
        }
    }
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

  private[this] def onCleanup(): Unit = {
    try {
      logDebug(s"-->onCleanup (${cache.size} clients)")
      val snapshot = cache.readOnlySnapshot()
      val staleClientsInUse = ArrayBuffer[OwnerInfo]()
      snapshot.foreach(pair => {
        val clientConfig = pair._1
        val clientMetadata = pair._2

        if (clientMetadata.lastRetrieved.get() < Instant.now.toEpochMilli - unusedClientTtlInMs) {
          if (clientMetadata.refCount.get() == 0) {
            if (clientMetadata.lastModified.get() < Instant.now.toEpochMilli - (cleanupIntervalInSeconds * 1000)) {
              logDebug(s"Removing client due to inactivity from the cache - ${clientConfig.endpoint}, " +
                s"${clientConfig.applicationName}, ${clientConfig.preferredRegionsList}, ${clientConfig.useGatewayMode}, " +
                s"${clientConfig.useEventualConsistency}")
              purgeImpl(clientConfig)
            } else {
              logDebug("Client has not been retrieved from the cache recently and no spark task has been using " +
                s"it for < $cleanupIntervalInSeconds seconds. Waiting one more clean-up cycle before closing it, in " +
                s"case newly scheduled spark tasks need it - Created: ${clientMetadata.created}, " +
                s"LastModified ${clientMetadata.lastModified}, RefCount: ${clientMetadata.refCount}, " +
                s"Owning Spark tasks: [${clientMetadata.owners.keys.mkString(", ")}]")
            }
          } else {
            logDebug(s"Client has not been retrieved from the cache recently - Created: ${clientMetadata.created}, " +
              s"LastModified ${clientMetadata.lastModified}, RefCount: ${clientMetadata.refCount}, " +
              s"Owning Spark tasks: [${clientMetadata.owners.keys.mkString(", ")}]")
          }
        }
      })

      val deleteCandidates = ArrayBuffer[CosmosClientCacheMetadata]()
      toBeClosedWhenNotActiveAnymore
        .keys()
        .asScala
        .foreach(m => if (m.refCount.get == 0) {
          deleteCandidates += m
        })

      deleteCandidates.foreach(c => if (toBeClosedWhenNotActiveAnymore.remove(c) != null) {
        // refCount is never going to increase once in this list
        // so it is save to close the client
        c.client.close()
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
    lastModified: AtomicLong,
    created: AtomicLong,
    refCount: AtomicLong,
    owners: TrieMap[OwnerInfo, Option[Boolean]]
  ) {
    def createCacheItemForReuse(ownerInfo: OwnerInfo) : CacheItemImpl = {
      val nowInEpochMilli = Instant.now.toEpochMilli
      lastRetrieved.set(nowInEpochMilli)
      lastModified.set(nowInEpochMilli)
      refCount.incrementAndGet()
      owners.putIfAbsent(ownerInfo, None)

      new CacheItemImpl(client, this, ownerInfo)
    }
  }

  private[this] case class OwnerInfo(
                                      calledFrom: String,
                                      partitionId: Int,
                                      stageId: Int,
                                      taskAttemptId: Long,
                                      attemptNumber: Int,
                                      stageAttemptNumber: Int
                                    )

  private[this] object OwnerInfo {
    def apply(calledFrom: String): OwnerInfo = {
      Option[TaskContext](TaskContext.get) match {
        case Some(ctx) => OwnerInfo(calledFrom, ctx.partitionId(), ctx.stageId(), ctx.taskAttemptId(), ctx.attemptNumber(), ctx.stageAttemptNumber())
        case None => OwnerInfo(calledFrom, -1, -1, -1, -1, -1)
      }
    }
  }

  private[spark] case class ClientConfigurationWrapper (
                                                        endpoint: String,
                                                        key: String,
                                                        applicationName: String,
                                                        useGatewayMode: Boolean,
                                                        useEventualConsistency: Boolean,
                                                        preferredRegionsList: String)

  private[this] object ClientConfigurationWrapper {
    def apply(clientConfig: CosmosClientConfiguration): ClientConfigurationWrapper = {
      ClientConfigurationWrapper(
        clientConfig.endpoint,
        clientConfig.key,
        clientConfig.applicationName,
        clientConfig.useGatewayMode,
        clientConfig.useEventualConsistency,
        clientConfig.preferredRegionsList match {
          case Some(regionListArray) => s"[${regionListArray.mkString(", ")}]"
          case None => ""
        }
      )
    }
  }

  private[this] class CacheItemImpl
  (
    val cosmosClient: CosmosAsyncClient,
    val ref: CosmosClientCacheMetadata,
    val ownerInfo: OwnerInfo
  ) extends CosmosClientCacheItem with BasicLoggingTrait {

    override def client: CosmosAsyncClient = this.cosmosClient

    override def context: String = this.ownerInfo.toString

    override def close(): Unit = {
      val remainingActiveClients = ref.refCount.decrementAndGet()
      if (remainingActiveClients < 0) {
        logError(s"Cached cosmos client has been released to the Cache more often than acquired.")
      }

      ref.owners.remove(ownerInfo)
      ref.lastModified.set(Instant.now.toEpochMilli)

      logDebug("Returned client to the pool = remaining active clients - Count: " +
        s"$remainingActiveClients, Spark contexts: ${ref.owners.keys.mkString(", ")}")
    }
  }
}
// scalastyle:on multiple.string.literals
