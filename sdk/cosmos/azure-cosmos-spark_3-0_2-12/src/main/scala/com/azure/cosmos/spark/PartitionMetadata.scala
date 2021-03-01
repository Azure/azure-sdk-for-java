// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{
  CosmosClientMetadataCachesSnapshot,
  SparkBridgeImplementationInternal
}
import com.azure.cosmos.spark.CosmosPredicates.{
  requireNotNull,
  requireNotNullOrEmpty
}
import org.apache.spark.broadcast.Broadcast

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

private object PartitionMetadata {
  def createKey(databaseId: String,
                containerId: String,
                feedRange: String): String =
    s"$databaseId/$containerId/$feedRange"

  def apply(cosmosClientConfig: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
            cosmosContainerConfig: CosmosContainerConfig,
            feedRange: String,
            documentCount: Long,
            totalDocumentSizeInKB: Long,
            continuationToken: String): PartitionMetadata = {

    val nowEpochMs = Instant.now().toEpochMilli

    PartitionMetadata(
      cosmosClientConfig,
      cosmosClientStateHandle,
      cosmosContainerConfig,
      feedRange,
      documentCount,
      totalDocumentSizeInKB,
      SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(
        continuationToken),
      new AtomicLong(nowEpochMs),
      new AtomicLong(nowEpochMs)
    )
  }
}

private case class PartitionMetadata(
    cosmosClientConfig: CosmosClientConfiguration,
    cosmosClientStateHandle: Option[Broadcast[CosmosClientMetadataCachesSnapshot]],
    cosmosContainerConfig: CosmosContainerConfig,
    feedRange: String,
    documentCount: Long,
    totalDocumentSizeInKB: Long,
    latestLsn: Long,
    lastRetrieved: AtomicLong,
    lastUpdated: AtomicLong
) {
  requireNotNullOrEmpty(feedRange, "feedRange")
  requireNotNull(cosmosClientConfig, "cosmosClientConfig")
  requireNotNull(cosmosContainerConfig, "cosmosContainerConfig")
  requireNotNull(lastRetrieved, "lastRetrieved")
  requireNotNull(lastUpdated, "lastUpdated")
}
