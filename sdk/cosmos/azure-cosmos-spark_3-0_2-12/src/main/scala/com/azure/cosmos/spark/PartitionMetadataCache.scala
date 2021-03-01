// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, Strings}
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver, requireNotNull}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import reactor.core.scala.publisher.SMono
import reactor.core.scala.publisher.SMono.PimpJMono
import reactor.core.scheduler.Schedulers

import java.time.Instant
import java.util.{Timer, TimerTask}
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import scala.collection.concurrent.TrieMap

// The partition metadata here is used purely for a best effort
// estimation of number of Spark partitions needed for a certain
// physical partition - it is not guaranteeing functional correctness
// because the cached metadata could be old or even be for a different
// container after deletion and recreation of a container
private object PartitionMetadataCache extends CosmosLoggingTrait {
  private[this] val Nothing = 0
  private[this] val cache = new TrieMap[String, PartitionMetadata]
  // TODO @fabianm reevaluate usage of test hooks over reflection and/or making the fields vars
  // so that they can simply be changed under test
  private[this] var cacheTestOverride: Option[TrieMap[String, PartitionMetadata]] = None

  // purpose of the time is to update partition metadata
  // additional throughput when more RUs are getting provisioned
  private[this] val timerName = "partition-metadata-refresh-timer"
  private[this] val timerOverrideName = "partition-metadata-refresh-timerOverride"
  // TODO @fabianm consider switching to ScheduledThreadExecutor or ExecutorService
  // see https://stackoverflow.com/questions/409932/java-timer-vs-executorservice
  private[this] val timer: Timer = new Timer(timerName, true)
  private[this] var testTimerOverride: Option[Timer] = None
  private[this] val refreshIntervalInMsDefault : Long = 60 * 1000 // refresh cache every minute after initialization
  private[this] var refreshIntervalInMsOverride: Option[Long] = None
  private[this] def refreshIntervalInMs : Long= refreshIntervalInMsOverride.getOrElse(refreshIntervalInMsDefault)

  // update cached items which haven't been retrieved in the last refreshPeriod only if they
  // have been last updated longer than 15 minutes ago
  // any cached item which has been retrieved within the last refresh period will
  // automatically kept being updated
  private[this] val staleCachedItemRefreshPeriodInMsDefault : Long = 15 * 60 * 1000
  private[this] var staleCachedItemRefreshPeriodInMsOverride : Option[Long] = None
  private[this] def staleCachedItemRefreshPeriodInMs: Long =
    staleCachedItemRefreshPeriodInMsOverride.getOrElse(staleCachedItemRefreshPeriodInMsDefault)

  // purged cached items if they haven't been retrieved within 2 hours
  private[this] val cachedItemTtlInMsDefault : Long = 2 * 60 * 60 * 1000
  private[this] var cachedItemTtlInMsOverride : Option[Long] = None
  private[this] def cachedItemTtlInMs: Long = cachedItemTtlInMsOverride.getOrElse(cachedItemTtlInMsDefault)

  this.startRefreshTimer()

  // NOTE
  // This method can only be used from the Spark driver
  // The reason for this restriction is that the IO operations necessary
  // to retrieve the metadata for the partitioning should only ever need
  // to happen on the driver and not the executors.
  // This also helps reducing the RU consumption of the Cosmos DB call to get the metadata
  def apply(cosmosClientConfig: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
            cosmosContainerConfig: CosmosContainerConfig,
            feedRange: String): SMono[PartitionMetadata] = {

    assertOnSparkDriver()
    requireNotNull(cosmosClientConfig, "cosmosClientConfig")

    val key = PartitionMetadata.createKey(
      cosmosContainerConfig.database,
      cosmosContainerConfig.container,
      feedRange)

    val getKey = (key: String) =>  cache.get(key) match {
      case Some(metadata) =>
        metadata.lastRetrieved.set(Instant.now.toEpochMilli)
        SMono.just(metadata)
      case None => this.create(
        cosmosClientConfig,
        cosmosClientStateHandle,
        cosmosContainerConfig,
        feedRange,
        key)
    }

    cacheTestOverride match {
      case Some(testCache) => {
        testCache.get(key) match {
          case Some(testMetadata) => {
            testMetadata.lastRetrieved.set(Instant.now.toEpochMilli)
            SMono.just(testMetadata)
          }
          case None  => getKey(key)
        }
      }
      case None => getKey(key)
    }
  }

  def purge(cosmosContainerConfig: CosmosContainerConfig, feedRange: String): Boolean = {
    assertOnSparkDriver()
    val key = PartitionMetadata.createKey(
      cosmosContainerConfig.database,
      cosmosContainerConfig.container,
      feedRange)

    cache.get(key) match {
      case None => false
      case Some(_) =>
        cache.remove(key).isDefined
    }
  }

  def injectTestData(cosmosContainerConfig: CosmosContainerConfig,
                     feedRange: String,
                     partitionMetadata: PartitionMetadata): Unit = {

    val key = PartitionMetadata.createKey(cosmosContainerConfig.database, cosmosContainerConfig.container, feedRange)
    val effectiveTestCache = this.cacheTestOverride match {
      case None => {
        val newCache = new TrieMap[String, PartitionMetadata]()
        this.cacheTestOverride = Some(newCache)
        newCache
      }
      case Some(existingCache) => existingCache
    }

    effectiveTestCache.put(key, partitionMetadata)
  }

  def resetTestOverrides(): Unit = {
    val timerOverrideSnapshot = this.testTimerOverride
    timerOverrideSnapshot match {
      case Some(testTimer) =>
        testTimer.cancel()
        this.testTimerOverride = None
      case None => Unit
    }

    this.cacheTestOverride match {
      case Some(cacheOverrideSnapshot) => {
        this.cacheTestOverride = None
        cacheOverrideSnapshot.clear()
      }
      case None => Unit
    }
    this.refreshIntervalInMsOverride = None
    this.cachedItemTtlInMsOverride = None
    this.staleCachedItemRefreshPeriodInMsOverride = None
  }

  def applyTestOverrides
  (
      newRefreshIntervalInMsOverride: Option[Long],
      newStaleCachedItemRefreshPeriodInMsOverride: Option[Long],
      newCachedItemTtlInMsOverride: Option[Long]
  ): Unit = {
    this.refreshIntervalInMsOverride = newRefreshIntervalInMsOverride
    this.cachedItemTtlInMsOverride = newCachedItemTtlInMsOverride
    this.staleCachedItemRefreshPeriodInMsOverride = newStaleCachedItemRefreshPeriodInMsOverride
    this.testTimerOverride = Some(new Timer(timerOverrideName, true))
    this.startRefreshTimer()
  }

  private[this] def create
  (
    cosmosClientConfiguration: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    feedRange: String,
    key: String
  ): SMono[PartitionMetadata] = {

    assertOnSparkDriver()
    cache.get(key) match {
      case Some(metadata) =>
        metadata.lastRetrieved.set(Instant.now.toEpochMilli)
        SMono.just(metadata)
      case None =>
        val metadataObservable = readPartitionMetadata(
          cosmosClientConfiguration: CosmosClientConfiguration,
          cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
          cosmosContainerConfig: CosmosContainerConfig,
          feedRange: String
        )

        metadataObservable
          .map(metadata => {
            cache.putIfAbsent(key, metadata) match {
              case None =>
                metadata
              case Some(metadataAddedConcurrently) =>
                metadataAddedConcurrently.lastRetrieved.set(Instant.now.toEpochMilli)

                metadataAddedConcurrently
            }
          })
          .subscribeOn(Schedulers.boundedElastic())
    }
  }

  private def readPartitionMetadata
  (
    cosmosClientConfiguration: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    feedRange: String
  ): SMono[PartitionMetadata] = {
    val client = CosmosClientCache.apply(cosmosClientConfiguration, cosmosClientStateHandle)
    val container = client
      .getDatabase(cosmosContainerConfig.database)
      .getContainer(cosmosContainerConfig.container)

    val options = CosmosChangeFeedRequestOptions.createForProcessingFromNow(FeedRange.fromString(feedRange))
    options.setMaxItemCount(1)
    options.setMaxPrefetchPageCount(1)
    options.setQuotaInfoEnabled(true)

    val lastDocumentCount = new AtomicLong()
    val lastTotalDocumentSize = new AtomicLong()
    val lastContinuationToken = new AtomicReference[String]()

    container
      .queryChangeFeed(options, classOf[ObjectNode])
      .handle(r => {
        lastDocumentCount.set(r.getDocumentCountUsage)
        lastTotalDocumentSize.set(r.getDocumentUsage)
        val continuation = r.getContinuationToken
        if (!Strings.isNullOrWhiteSpace(continuation)) {
          lastContinuationToken.set(continuation)
        }
      })
      .collectList()
      .asScala
      .map(_ => {
        PartitionMetadata(
          cosmosClientConfiguration,
          cosmosClientStateHandle,
          cosmosContainerConfig,
          feedRange,
          assertNotNull(lastDocumentCount.get, "lastDocumentCount"),
          assertNotNull(lastTotalDocumentSize.get, "lastTotalDocumentSize"),
          assertNotNullOrEmpty(lastContinuationToken.get, "continuationToken")
        )
      })
  }

  private def startRefreshTimer() : Unit = {
    logInfo(s"$timerName: scheduling timer - delay: $refreshIntervalInMs ms, period: $refreshIntervalInMs ms")
    // TODO @fabianm consider switching to ScheduledThreadExecutor or ExecutorService
    // see https://stackoverflow.com/questions/409932/java-timer-vs-executorservice
    testTimerOverride.getOrElse(timer).schedule(
      new TimerTask { def run(): Unit = onRunRefreshTimer() },
      refreshIntervalInMs,
      refreshIntervalInMs)
  }

  private def onRunRefreshTimer() : Unit = {
    logTrace(s"--> $timerName: onRunRefreshTimer")
    val snapshot = cache.readOnlySnapshot()
    val updateObservables = snapshot.map(metadataSnapshot => updateIfNecessary(metadataSnapshot._2))
    SMono
      .zipDelayError(updateObservables, _ => 0)
      .onErrorResume(t => {
        logWarning("An error happened when updating partition metadata", t)
        SMono.just(Nothing)
      })
      .block()
    logTrace(s"<-- $timerName: onRunRefreshTimer")
  }

  private def updateIfNecessary(metadataSnapshot: PartitionMetadata):SMono[Int] = {
    val nowEpochMs = Instant.now.toEpochMilli
    val hotThreshold = nowEpochMs - refreshIntervalInMs
    val staleThreshold = nowEpochMs - staleCachedItemRefreshPeriodInMs
    val ttlThreshold = nowEpochMs - cachedItemTtlInMs

    val lastRetrievedSnapshot = metadataSnapshot.lastRetrieved.get()
    if (lastRetrievedSnapshot < ttlThreshold) {
      this.purge(metadataSnapshot.cosmosContainerConfig, metadataSnapshot.feedRange)
      SMono.just(Nothing)
    } else if (lastRetrievedSnapshot < staleThreshold || lastRetrievedSnapshot > hotThreshold) {
      readPartitionMetadata(
        metadataSnapshot.cosmosClientConfig,
        metadataSnapshot.cosmosClientStateHandle,
        metadataSnapshot.cosmosContainerConfig,
        metadataSnapshot.feedRange
      ).map(metadata => {
        val key = PartitionMetadata.createKey(
          metadataSnapshot.cosmosContainerConfig.database,
          metadataSnapshot.cosmosContainerConfig.container,
          metadataSnapshot.feedRange
        )
        if (cache.replace(key, metadataSnapshot, metadata)) {
          logTrace(s"Updated partition metadata '$key'")
        } else {
          logWarning(s"Ignored retrieved metadata due to concurrent update of partition metadata '$key'")
        }

        Nothing
      })
    } else {
      SMono.just(Nothing)
    }
  }
}
