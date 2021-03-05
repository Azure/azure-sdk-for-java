// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.routing.{PartitionKeyInternalHelper, Range}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal, Strings}
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver}
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.connector.read.streaming.{MicroBatchStream, Offset, ReadLimit, SupportsAdmissionControl}
import org.apache.spark.sql.types.StructType
import reactor.core.scala.publisher.SFlux
import reactor.core.scala.publisher.SMono.PimpJMono

import java.time.Duration
import java.util.UUID
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}
import scala.collection.mutable

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// scala style rule flaky - even complaining on partial log messages
// scalastyle:off multiple.string.literals
private class ChangeFeedMicroBatchStream
(
  val session: SparkSession,
  val schema: StructType,
  val config: Map[String, String],
  val cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  val checkpointLocation: String
) extends MicroBatchStream
  with SupportsAdmissionControl
  with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val clientConfiguration = CosmosClientConfiguration.apply(config, readConfig.forceEventualConsistency)
  private val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val partitioningConfig = CosmosPartitioningConfig.parseCosmosPartitioningConfig(config)
  private val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(config)
  private val client = CosmosClientCache.apply(clientConfiguration, Some(cosmosClientStateHandle))
  private val container = client.getDatabase(containerConfig.database).getContainer(containerConfig.container)
  private val streamId = UUID.randomUUID().toString

  override def latestOffset(): Offset = {
    throw new UnsupportedOperationException(
      "latestOffset(Offset, ReadLimit) should be called instead of this method")
  }

  override def planInputPartitions(startOffset: Offset, endOffset: Offset): Array[InputPartition] = ???

  override def createReaderFactory(): PartitionReaderFactory = ???

  override def latestOffset(startOffset: Offset, readLimit: ReadLimit): Offset = {
    this.getLatestOffset(startOffset.asInstanceOf[ChangeFeedOffset], readLimit, Duration.ZERO)
  }

  private def getLatestOffset(startOffset: ChangeFeedOffset, readLimit: ReadLimit, maxStaleness: Duration) = {
    assertNotNull(startOffset, "startOffset")

    val latestPartitionMetadata = CosmosPartitionPlanner.getPartitionMetadata(
      this.clientConfiguration,
      Some(this.cosmosClientStateHandle),
      this.containerConfig,
      Some(maxStaleness)
    )
      .map(metadata => (metadata.feedRange, metadata.latestLsn))


    val feedRangeToLsnTokens = SparkBridgeImplementationInternal
      .getOrderedFeedRangeToLsnTokens(startOffset.changeFeedState, latestPartitionMetadata)

    val feedRangeToStartTokens = feedRangeToLsnTokens._1
    val feedRangeToLatestTokens = feedRangeToLsnTokens._1




  }

  private def mergeStartAndLatestTokens
  (
    startTokens: Array[(Range[String], Long)],
    latestTokens: Array[(Range[String], Long)]
  ): Array[(Range[String], Long, Long)] = {

    val boundaries = mutable.SortedSet[String]()
    startTokens.foreach(token => {
      boundaries += token._1.getMin()
      boundaries += token._1.getMax()
    })
    latestTokens.foreach(token => {
      boundaries += token._1.getMin()
      boundaries += token._1.getMax()
    })

    vfgjgfjgj

    val merged = List[(Range[String], Long, Long)]
    var leftIndex = 0
    var rightIndex = 0
    var min: String = PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey
    var max: String = PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey
    while (true) {

    }
  }

                                      )

  private def parseLsnFromNode(lsnNode: JsonNode) = {
      if (lsnNode != null && lsnNode.isTextual) {
        Some(lsnNode.textValue())
      } else {
        None
      }
  }

  override def initialOffset(): Offset = {
    assertOnSparkDriver()

    val metadataLog = new ChangeFeedInitialOffsetWriter(
        assertNotNull(session, "session"),
        assertNotNullOrEmpty(checkpointLocation, "checkpointLocation"))
    val offsetJson = metadataLog.get(0).getOrElse {
      val lastContinuationTokens: ConcurrentMap[FeedRange, String] = new ConcurrentHashMap[FeedRange, String]()

      container
        .getFeedRanges
        .asScala
        .flatMapMany(feedRanges => SFlux.fromIterable(feedRanges.asScala))
        .map(feedRange => {
          val requestOptions = changeFeedConfig.toRequestOptions(feedRange)
          requestOptions.setMaxItemCount(1)
          requestOptions.setMaxPrefetchPageCount(1)
          requestOptions.setQuotaInfoEnabled(true)

          container
            .queryChangeFeed(requestOptions, classOf[ObjectNode])
            .handle(r => {
              val items = r.getElements.asScala
              val lsnFromItems = items
                .collectFirst({ case item: ObjectNode if item != null => parseLsnFromNode(item.get("_lsn"))})
                .flatten

              val continuation = lsnFromItems.getOrElse(r.getContinuationToken)
              if (!Strings.isNullOrWhiteSpace(continuation)) {
                lastContinuationTokens.put(feedRange, continuation)
              }
            })
            .collectList()
            .asScala
        })
        .asJava()
        .collectList()

      val offsetJson = SparkBridgeImplementationInternal
        .mergeChangeFeedContinuations(lastContinuationTokens.values().asScala)

      logDebug(s"MicroBatch stream $streamId: Calculated initial offset '$offsetJson'.")
      metadataLog.add(0, offsetJson)
      offsetJson
    }

    logInfo(s"MicroBatch stream $streamId: Initial offset '$offsetJson'.")
    ChangeFeedOffset(offsetJson)
  }

  override def deserializeOffset(s: String): Offset = {
    logDebug(s"MicroBatch stream $streamId: Deserialized offset '$s'.")
    ChangeFeedOffset.fromJson(s)
  }

  override def commit(offset: Offset): Unit = {
    logInfo(s"MicroBatch stream $streamId: Committed offset '${offset.json()}'.")
  }

  override def stop(): Unit = {
    logInfo(s"MicroBatch stream $streamId: stopped.")
  }
}
// scalastyle:on multiple.string.literals