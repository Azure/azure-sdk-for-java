// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeInternal}
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, requireNotNull, requireNotNullOrEmpty}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scala.publisher.SMono.PimpJMono
import reactor.core.scheduler.Schedulers

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import scala.collection.concurrent.TrieMap

private class CosmosPartitionPlanner {

  private object ParameterNames {
    val CosmosClientConfig = "cosmosClientConfig"
    val CosmosContainerConfig = "cosmosContainerConfig"
    val DatabaseId = "databaseId"
    val ContainerId = "containerId"
    val FeedRange = "feedRange"
  }

  def getPartitionMetadata(cosmosClientConfig: CosmosClientConfiguration,
                           cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
                           cosmosContainerConfig: CosmosContainerConfig) : Array[PartitionMetadata] = {

    requireNotNull(cosmosClientConfig, ParameterNames.CosmosClientConfig)
    requireNotNull(cosmosContainerConfig, ParameterNames.CosmosContainerConfig)
    val client = CosmosClientCache.apply(cosmosClientConfig, cosmosClientStateHandle)

    val container = client
      .getDatabase(cosmosContainerConfig.database)
      .getContainer(cosmosContainerConfig.container)

    container
      .getFeedRanges
      .asScala
      .flatMap(feedRanges => {
        SFlux
          .fromArray(feedRanges.toArray())
          .flatMap(f => PartitionMetadataCache.apply(
            cosmosClientConfig,
            cosmosClientStateHandle,
            cosmosContainerConfig,
            f.toString
          ))
          .collectSeq()
      })
      .block()
      .toArray
  }

  // Intentionally keeping this private to the CosmosPartitionPlanner
  // The partition metadata here is used purely for an best effort
  // estimation of number of Spark partitions needed for a certain
  // physical partition - it is not guaranteeing functional correctness
  // because the cached metadata could be old or even be for a different
  // container after deletion and recreation of a container
  private object PartitionMetadataCache {
    private[this] val cache = new TrieMap[String, SMono[PartitionMetadata]]

    def apply(cosmosClientConfig: CosmosClientConfiguration,
              cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
              cosmosContainerConfig: CosmosContainerConfig,
              feedRange: String): SMono[PartitionMetadata] = {

      requireNotNull(cosmosClientConfig, ParameterNames.CosmosClientConfig)

      val key = PartitionMetadata.createKey(
        cosmosContainerConfig.database,
        cosmosContainerConfig.container,
        feedRange)

      cache.get(key) match {
        case Some(metadata) => metadata
        case None => create(
          cosmosClientConfig,
          cosmosClientStateHandle,
          cosmosContainerConfig,
          feedRange,
          key)
      }
    }

    def purge(cosmosContainerConfig: CosmosContainerConfig, feedRange: String): Unit = {
      val key = PartitionMetadata.createKey(
        cosmosContainerConfig.database,
        cosmosContainerConfig.container,
        feedRange)

      cache.get(key) match {
        case None => Unit
        case Some(_) =>
          cache.remove(key)
      }
    }

    private[this] def create(cosmosClientConfiguration: CosmosClientConfiguration,
                             cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
                             cosmosContainerConfig: CosmosContainerConfig,
                             feedRange: String,
                             key: String) : SMono[PartitionMetadata] = {
      cache.get(key) match {
        case Some(metadata) => metadata
        case None =>
          val client = CosmosClientCache.apply(cosmosClientConfiguration, cosmosClientStateHandle)
          val container = client
            .getDatabase(cosmosContainerConfig.database)
            .getContainer(cosmosContainerConfig.container)

          val options = CosmosChangeFeedRequestOptions.createForProcessingFromNow(FeedRange.fromString(feedRange))
          options.setMaxItemCount(1)
          options.setMaxPrefetchPageCount(1)

          val lastDocumentCount = new AtomicLong()
          val lastTotalDocumentSize = new AtomicLong()
          val lastContinuationToken = new AtomicReference[String]()
          val results = container.queryChangeFeed(options,classOf[ObjectNode])

          results.handle(r => {
            lastDocumentCount.set(r.getDocumentCountUsage)
            lastTotalDocumentSize.set(r.getDocumentUsage)
            val continuation = r.getContinuationToken
            if (continuation != null && !continuation.isBlank) {
              lastContinuationToken.set(continuation)
            }
          })

          val metadataObservable = results
            .collectList()
            .asScala
            .`then`(
              SMono.just(
                PartitionMetadata.create(
                  cosmosContainerConfig,
                  feedRange,
                  assertNotNull(lastDocumentCount.get, "lastDocumentCount"),
                  assertNotNull(lastTotalDocumentSize.get, "lastTotalDocumentSize"),
                  assertNotNullOrEmpty(lastContinuationToken.get, "continuationToken"))))
          metadataObservable.subscribeOn(Schedulers.boundedElastic())
          cache.putIfAbsent(key, metadataObservable) match {
            case None =>
              metadataObservable
            case Some(metadataObservableAddedConcurrently) =>
              metadataObservableAddedConcurrently
          }
      }
    }
  }

  private object PartitionMetadata {
    def createKey(
                   databaseId: String,
                   containerId: String,
                   feedRange: String) : String = s"$databaseId|$containerId|$feedRange"

    def create(cosmosContainerConfig: CosmosContainerConfig,
               feedRange: String,
               documentCount: Long,
               totalDocumentSize: Long,
               continuationToken: String): PartitionMetadata = {

      PartitionMetadata(
        cosmosContainerConfig.database,
        cosmosContainerConfig.container,
        feedRange,
        documentCount,
        totalDocumentSize,
        SparkBridgeInternal.extractLsnFromChangeFeedContinuation(continuationToken))
    }
  }

  private case class PartitionMetadata
  (
    databaseId: String,
    containerId: String,
    feedRange: String,
    documentCount: Long,
    totalDocumentSizeInKB: Long,
    latestLsn: Long
  ) {
    requireNotNullOrEmpty(databaseId, ParameterNames.DatabaseId)
    requireNotNullOrEmpty(containerId, ParameterNames.ContainerId)
    requireNotNullOrEmpty(feedRange, ParameterNames.FeedRange)
    requireNotNull(documentCount, "documentCount")
    requireNotNull(totalDocumentSizeInKB, "totalDocumentSizeInKB")
    requireNotNull(latestLsn, "latestLsn")
  }
}
