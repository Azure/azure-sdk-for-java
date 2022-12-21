// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{SparkBridgeImplementationInternal, Strings}
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver, requireNotNull}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import reactor.core.publisher.{Flux, Mono}
import reactor.core.scala.publisher.SMono
import reactor.core.scala.publisher.SMono.PimpJMono
import reactor.core.scheduler.Schedulers

import java.time.{Duration, Instant}
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import java.util.{Timer, TimerTask}
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// The partition metadata here is used purely for a best effort
// estimation of number of Spark partitions needed for a certain
// physical partition - it is not guaranteeing functional correctness
// because the cached metadata could be old or even be for a different
// container after deletion and recreation of a container
private object PartitionMetadataCache extends BasicLoggingTrait {
  private[this] val Nothing = 0
  private[this] val cache = new TrieMap[String, PartitionMetadata]
  // purpose of the time is to update partition metadata
  // additional throughput when more RUs are getting provisioned
  private[this] val timerName = "partition-metadata-refresh-timer"
  private[this] val timerOverrideName = "partition-metadata-refresh-timerOverride"
  // TODO @fabianm consider switching to ScheduledThreadExecutor or ExecutorService
  // see https://stackoverflow.com/questions/409932/java-timer-vs-executorservice
  private[this] val timer: Timer = new Timer(timerName, true)

  private[spark] val refreshIntervalInMsDefault: Long = 5 * 1000 // refresh cache every 5 seconds after initialization
  // while retrieved within this interval the last time the data will be updated every
  // refresh cycle

  private[this] val hotThresholdIntervalInMs: Long = 60 * 1000 // refresh cache every minute after initialization

  // update cached items which haven't been retrieved in the last refreshPeriod only if they
  // have been last updated longer than 15 minutes ago
  // any cached item which has been retrieved within the last refresh period will
  // automatically kept being updated
    private[this] val staleCachedItemRefreshPeriodInMsDefault: Long = 15 * 60 * 1000

  // purged cached items if they haven't been retrieved within 2 hours
  private[this] val cachedItemTtlInMsDefault: Long = 2 * 60 * 60 * 1000
  // TODO @fabianm reevaluate usage of test hooks over reflection and/or making the fields vars
  // so that they can simply be changed under test
  private[this] var cacheTestOverride: Option[TrieMap[String, PartitionMetadata]] = None
  private[this] var testTimerOverride: Option[Timer] = None
  private[this] var refreshIntervalInMsOverride: Option[Long] = None
  private[this] var staleCachedItemRefreshPeriodInMsOverride: Option[Long] = None
  private[this] var cachedItemTtlInMsOverride: Option[Long] = None

  def cachedCount(): Int = cache.readOnlySnapshot().size

  // NOTE
  // This method can only be used from the Spark driver
  // The reason for this restriction is that the IO operations necessary
  // to retrieve the metadata for the partitioning should only ever need
  // to happen on the driver and not the executors.
  // This also helps reducing the RU consumption of the Cosmos DB call to get the metadata
  def apply(userConfig: Map[String, String],
            cosmosClientConfig: CosmosClientConfiguration,
            cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
            cosmosContainerConfig: CosmosContainerConfig,
            feedRange: NormalizedRange,
            maxStaleness: Option[Duration] = None): SMono[PartitionMetadata] = {

    requireNotNull(cosmosClientConfig, "cosmosClientConfig")

    val key = PartitionMetadata.createKey(
      cosmosContainerConfig.database,
      cosmosContainerConfig.container,
      feedRange)

    cacheTestOverride match {
      case Some(testCache) =>
        testCache.get(key) match {
          case Some(testMetadata) =>
            testMetadata.lastRetrieved.set(Instant.now.toEpochMilli)
            SMono.just(testMetadata)
          case None => this.getOrCreate(
            key,
            userConfig,
            cosmosClientConfig,
            cosmosClientStateHandles,
            cosmosContainerConfig,
            feedRange,
            maxStaleness)
        }
      case None => this.getOrCreate(
        key, userConfig, cosmosClientConfig, cosmosClientStateHandles, cosmosContainerConfig, feedRange, maxStaleness)
    }
  }

  def clearCache(): Unit = {
    this.cache.clear()
  }

  private[this] def getOrCreate
  (
    key: String,
    userConfig: Map[String, String],
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
    cosmosContainerConfig: CosmosContainerConfig,
    feedRange: NormalizedRange,
    maxStaleness: Option[Duration] = None
  ) : SMono[PartitionMetadata] = {

    val nowEpochMs = Instant.now.toEpochMilli
    cache.get(key) match {
      case Some(metadata) =>
        if (maxStaleness.isEmpty ||
          nowEpochMs - metadata.lastUpdated.get() <= maxStaleness.get.toMillis) {

          metadata.lastRetrieved.set(nowEpochMs)
          SMono.just(metadata)
        } else {
          readPartitionMetadata(
            metadata.userConfig,
            metadata.cosmosClientConfig,
            metadata.cosmosClientStateHandles,
            metadata.cosmosContainerConfig,
            metadata.feedRange,
            metadata.firstLsn,
            tolerateNotFound = true
          )
            .flatMap(updatedMetadata => {
              val key = PartitionMetadata.createKey(
                metadata.cosmosContainerConfig.database,
                metadata.cosmosContainerConfig.container,
                metadata.feedRange
              )
              val partitionMetadata: SMono[PartitionMetadata] = if (updatedMetadata.isDefined) {
                updatedMetadata.get.lastRetrieved.set(metadata.lastRetrieved.get())
                if (cache.replace(key, metadata, updatedMetadata.get)) {
                  logTrace(s"Updated partition metadata '$key'")
                  updatedMetadata.get.lastRetrieved.set(nowEpochMs)
                  SMono.just(updatedMetadata.get)
                } else {
                  logDebug(s"Ignored retrieved metadata due to concurrent update of partition metadata '$key'")
                  metadata.lastRetrieved.set(nowEpochMs)
                  SMono.just(metadata)
                }
              } else {
                logDebug(s"Removing partition metadata '$key' because container doesn't exist anymore")
                this.purge(metadata.cosmosContainerConfig, metadata.feedRange)

                this.create(
                  userConfig,
                  cosmosClientConfig,
                  cosmosClientStateHandles,
                  cosmosContainerConfig,
                  feedRange,
                  key)
              }

              partitionMetadata
            })
        }
      case None => this.create(
        userConfig,
        cosmosClientConfig,
        cosmosClientStateHandles,
        cosmosContainerConfig,
        feedRange,
        key)
    }
  }

  private[this] def create
  (
    userConfig: Map[String, String],
    cosmosClientConfiguration: CosmosClientConfiguration,
    cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
    cosmosContainerConfig: CosmosContainerConfig,
    feedRange: NormalizedRange,
    key: String
  ): SMono[PartitionMetadata] = {

    readPartitionMetadata(
      userConfig,
      cosmosClientConfiguration,
      cosmosClientStateHandles,
      cosmosContainerConfig,
      feedRange,
      None,
      tolerateNotFound = false
    )
      .map(metadata => {
        cache.put(key, metadata.get)
        metadata.get
      })
      .subscribeOn(Schedulers.boundedElastic())
  }

  this.startRefreshTimer()

  private def readPartitionMetadata
  (
    userConfig: Map[String, String],
    cosmosClientConfiguration: CosmosClientConfiguration,
    cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
    cosmosContainerConfig: CosmosContainerConfig,
    feedRange: NormalizedRange,
    firstLsn: Option[Long],
    tolerateNotFound: Boolean
  ): SMono[Option[PartitionMetadata]] = {

    TransientErrorsRetryPolicy.executeWithRetry(() =>
      readPartitionMetadataImpl(
        userConfig,
        cosmosClientConfiguration,
        cosmosClientStateHandles,
        cosmosContainerConfig,
        feedRange,
        firstLsn,
        tolerateNotFound))
  }

  //scalastyle:off method.length
  private def readPartitionMetadataImpl
  (
    userConfig: Map[String, String],
    cosmosClientConfiguration: CosmosClientConfiguration,
    cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
    cosmosContainerConfig: CosmosContainerConfig,
    feedRange: NormalizedRange,
    previouslyCalculatedFirstLsn: Option[Long],
    tolerateNotFound: Boolean
  ): SMono[Option[PartitionMetadata]] = {

    val cosmosClientMetadataCache =
      cosmosClientStateHandles match {
        case None => None
        case Some(_) => Some(cosmosClientStateHandles.get.value.cosmosClientMetadataCaches)
      }

    val calledFrom = s"PartitionMetadataCache.readPartitionMetadata(${cosmosContainerConfig.database}.${cosmosContainerConfig.container}, $feedRange)"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(
          CosmosClientCache(
            cosmosClientConfiguration,
            cosmosClientMetadataCache,
            calledFrom)),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          userConfig, calledFrom, cosmosClientStateHandles)
      ))
      .to(clientCacheItems => {
        val container =
          ThroughputControlHelper.getContainer(
            userConfig,
            cosmosContainerConfig,
            clientCacheItems(0).get,
            clientCacheItems(1))

        val optionsFromNow = CosmosChangeFeedRequestOptions.createForProcessingFromNow(
          SparkBridgeImplementationInternal.toFeedRange(feedRange))
        optionsFromNow.setMaxItemCount(1)
        optionsFromNow.setMaxPrefetchPageCount(1)
        optionsFromNow.setQuotaInfoEnabled(true)

        val lastDocumentCount = new AtomicLong()
        val lastTotalDocumentSize = new AtomicLong()
        val lastContinuationToken = new AtomicReference[String]()
        val firstLsn = new AtomicLong(-1)

        val fromNowFlux: Flux[Int] = container
          .queryChangeFeed(optionsFromNow, classOf[ObjectNode])
          .byPage
          .take(1)
          .map(r => {
            lastDocumentCount.set(r.getDocumentCountUsage)
            lastTotalDocumentSize.set(r.getDocumentUsage)
            val continuation = r.getContinuationToken
            logDebug(s"PartitionMetadataCache.readPartitionMetadata-fromNow(" +
              s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}, $feedRange, $continuation)")
            if (!Strings.isNullOrWhiteSpace(continuation)) {
              lastContinuationToken.set(continuation)
            }
            Nothing
          })

        val fromNowMono: Mono[Int] = fromNowFlux.single()

        val fromBeginningMono: Mono[Int] = previouslyCalculatedFirstLsn match {
          case Some(providedFirstLsn) =>
            firstLsn.set(providedFirstLsn)
            Mono.just(0)
          case None =>
            val optionsFromBeginning = CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(
              SparkBridgeImplementationInternal.toFeedRange(feedRange))
            optionsFromBeginning.setMaxItemCount(1)
            optionsFromBeginning.setMaxPrefetchPageCount(1)

            val fromBeginningFlux: Flux[Int] = container
              .queryChangeFeed(optionsFromBeginning, classOf[ObjectNode])
              .byPage()
              .take(1)
              .map(
                r => {
                  logDebug(s"PartitionMetadataCache.readPartitionMetadata-fromBeginning(" +
                    s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}, $feedRange, " +
                    s"${r.getRequestCharge}, ${r.getContinuationToken}, ${r.getResults.size})")
                  if (r.getResults.size() > 0) {
                    CosmosPartitionPlanner.getLsnOfFirstItem(r.getResults.asScala) match {
                      case Some(lsn) =>
                        if (firstLsn.get < 0 || lsn < firstLsn.get) { firstLsn.set(lsn) }
                        logDebug(s"LSN: $lsn, FirstLsn: $firstLsn.get")
                      case None =>
                    }
                  }

                  Nothing
                })

            fromBeginningFlux.single()
        }

        Mono.zip(fromNowMono, fromBeginningMono)
          .asScala
          .map(_ => {
            Some(PartitionMetadata(
              userConfig,
              cosmosClientConfiguration,
              cosmosClientStateHandles,
              cosmosContainerConfig,
              feedRange,
              assertNotNull(lastDocumentCount.get, "lastDocumentCount"),
              assertNotNull(lastTotalDocumentSize.get, "lastTotalDocumentSize"),
              if (firstLsn.get >= 0) { Some(firstLsn.get)} else { None },
              assertNotNullOrEmpty(lastContinuationToken.get, "fromNow continuationToken"),
              startLsn = math.max(firstLsn.get(), 0),
              endLsn = None
            ))
          })
          .onErrorResume((throwable: Throwable) => {
            if (tolerateNotFound && Exceptions.isNotFoundException(throwable)) {
              SMono.just(None)
            } else {
              SMono.error(throwable)
            }
          })
    })
  }
  //scalastyle:on method.length

  def injectTestData(cosmosContainerConfig: CosmosContainerConfig,
                     feedRange: NormalizedRange,
                     partitionMetadata: PartitionMetadata): Unit = {

    val key = PartitionMetadata.createKey(
      cosmosContainerConfig.database,
      cosmosContainerConfig.container,
      feedRange)
    val effectiveTestCache = this.cacheTestOverride match {
      case None =>
        val newCache = new TrieMap[String, PartitionMetadata]()
        this.cacheTestOverride = Some(newCache)
        newCache
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
      case None =>
    }

    this.cacheTestOverride match {
      case Some(cacheOverrideSnapshot) =>
        this.cacheTestOverride = None
        cacheOverrideSnapshot.clear()
      case None =>
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

  private def startRefreshTimer(): Unit = {
    logInfo(s"$timerName: scheduling timer - delay: $refreshIntervalInMs ms, period: $refreshIntervalInMs ms")
    // TODO @fabianm consider switching to ScheduledThreadExecutor or ExecutorService
    // see https://stackoverflow.com/questions/409932/java-timer-vs-executorservice
    testTimerOverride.getOrElse(timer).schedule(
      new TimerTask {
        def run(): Unit = onRunRefreshTimer()
      },
      refreshIntervalInMs,
      refreshIntervalInMs)
  }

  private[this] def refreshIntervalInMs: Long = refreshIntervalInMsOverride.getOrElse(refreshIntervalInMsDefault)

  private def onRunRefreshTimer(): Unit = {
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

  private def updateIfNecessary
  (
    metadataSnapshot: PartitionMetadata
  ): SMono[Int] = {
    val nowEpochMs = Instant.now.toEpochMilli
    val hotThreshold = nowEpochMs - hotThresholdIntervalInMs
    val staleThreshold = nowEpochMs - staleCachedItemRefreshPeriodInMs
    val ttlThreshold = nowEpochMs - cachedItemTtlInMs

    val lastRetrievedSnapshot = metadataSnapshot.lastRetrieved.get()
    if (lastRetrievedSnapshot < ttlThreshold) {
      this.purge(metadataSnapshot.cosmosContainerConfig, metadataSnapshot.feedRange)
      SMono.just(Nothing)
    } else if (lastRetrievedSnapshot < staleThreshold || lastRetrievedSnapshot > hotThreshold) {
      readPartitionMetadata(
        metadataSnapshot.userConfig,
        metadataSnapshot.cosmosClientConfig,
        metadataSnapshot.cosmosClientStateHandles,
        metadataSnapshot.cosmosContainerConfig,
        metadataSnapshot.feedRange,
        metadataSnapshot.firstLsn,
        tolerateNotFound = true
      )
        .map(metadata => {
          val key = PartitionMetadata.createKey(
            metadataSnapshot.cosmosContainerConfig.database,
            metadataSnapshot.cosmosContainerConfig.container,
            metadataSnapshot.feedRange
          )
          if (metadata.isDefined) {
            metadata.get.lastRetrieved.set(metadataSnapshot.lastRetrieved.get())
            if (cache.replace(key, metadataSnapshot, metadata.get)) {
              logTrace(s"Updated partition metadata '$key'")
            } else {
              logDebug(s"Ignored retrieved metadata due to concurrent update of partition metadata '$key'")
            }
          } else {
            logDebug(s"Removing partition metadata '$key' because container doesn't exist anymore")
            this.purge(metadataSnapshot.cosmosContainerConfig, metadataSnapshot.feedRange)
          }

          Nothing
        })
    } else {
      SMono.just(Nothing)
    }
  }

  private[this] def staleCachedItemRefreshPeriodInMs: Long =
    staleCachedItemRefreshPeriodInMsOverride.getOrElse(staleCachedItemRefreshPeriodInMsDefault)

  private[this] def cachedItemTtlInMs: Long = cachedItemTtlInMsOverride.getOrElse(cachedItemTtlInMsDefault)

  def purge(cosmosContainerConfig: CosmosContainerConfig, feedRange: NormalizedRange): Boolean = {
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

  def purge(cosmosContainerConfig: CosmosContainerConfig): Unit = {
    assertOnSparkDriver()
    cache.readOnlySnapshot().foreach(keyValuePair =>
      if (keyValuePair._2.cosmosContainerConfig == cosmosContainerConfig) {
        cache.get(keyValuePair._1) match {
          case None => false
          case Some(_) =>
            cache.remove(keyValuePair._1).isDefined
        }
      }
    )
  }
}
