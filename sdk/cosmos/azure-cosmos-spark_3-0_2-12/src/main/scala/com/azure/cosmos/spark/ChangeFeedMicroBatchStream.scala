// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal.extractContinuationTokensFromChangeFeedStateJson
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
  private var latestOffsetSnapshot: Option[ChangeFeedOffset] = None

  override def latestOffset(): Offset = {
    throw new UnsupportedOperationException(
      "latestOffset(Offset, ReadLimit) should be called instead of this method")
  }

  override def planInputPartitions(startOffset: Offset, endOffset: Offset): Array[InputPartition] = {
    assertNotNull(startOffset, "startOffset")
    assertNotNull(endOffset, "endOffset")
    assert(startOffset.isInstanceOf[ChangeFeedOffset], "Argument 'startOffset' is not a change feed offset.")
    assert(endOffset.isInstanceOf[ChangeFeedOffset], "Argument 'endOffset' is not a change feed offset.")

    val start = startOffset.asInstanceOf[ChangeFeedOffset]
    val end = endOffset.asInstanceOf[ChangeFeedOffset]

    assert(end.inputPartitions.isDefined, "Argument 'endOffset.inputPartitions' must not be null or empty.")

    val startJson = start.json()

    end
      .inputPartitions
      .get
      .map(partition => partition
        .withContinuationState(
          SparkBridgeImplementationInternal
            .extractChangeFeedStateForRange(startJson, partition.feedRange)))
  }

  override def createReaderFactory(): PartitionReaderFactory = ???

  override def latestOffset(startOffset: Offset, readLimit: ReadLimit): Offset = {
    this.getLatestOffset(startOffset.asInstanceOf[ChangeFeedOffset], readLimit, Duration.ZERO)
  }

  private def getLatestOffset(startOffset: ChangeFeedOffset, readLimit: ReadLimit, maxStaleness: Duration) = {
    assertOnSparkDriver()
    assertNotNull(startOffset, "startOffset")

    val latestPartitionMetadata = CosmosPartitionPlanner.getPartitionMetadata(
      this.clientConfiguration,
      Some(this.cosmosClientStateHandle),
      this.containerConfig,
      Some(maxStaleness)
    )

    val defaultMaxPartitionSizeInMB = (session.sessionState.conf.filesMaxPartitionBytes / (1024 * 1024)).toInt
    val defaultMinPartitionCount = 1 + (2 * session.sparkContext.defaultParallelism)

    val orderedMetadataWithStartLsn = this.getOrderedPartitionMetadataWithStartLsn(
      startOffset.changeFeedState,
      latestPartitionMetadata)

    val inputPartitions: Array[CosmosInputPartition] = CosmosPartitionPlanner.createInputPartitions(
      partitioningConfig,
      container,
      orderedMetadataWithStartLsn,
      defaultMinPartitionCount,
      defaultMaxPartitionSizeInMB,
      readLimit
    )

    val changeFeedStateJson = SparkBridgeImplementationInternal
      .createChangeFeedStateJson(
        startOffset.changeFeedState,
        orderedMetadataWithStartLsn.map(m => (m.feedRange, m.endLsn.getOrElse(m.latestLsn))))

    val offset = ChangeFeedOffset(changeFeedStateJson, Some(inputPartitions))
    this.latestOffsetSnapshot = Some(offset)
    offset
  }

  def getOrderedPartitionMetadataWithStartLsn
  (
    stateJson: String,
    latestPartitionMetadata: Array[PartitionMetadata]
  ): Array[PartitionMetadata] = {

    assert(!Strings.isNullOrWhiteSpace(stateJson), s"Argument 'stateJson' must not be null or empty.")
    val orderedStartTokens = extractContinuationTokensFromChangeFeedStateJson(stateJson)

    val orderedLatestTokens = latestPartitionMetadata
      .sortBy(metadata => metadata.feedRange.min)

    mergeStartAndLatestTokens(orderedStartTokens, orderedLatestTokens)
  }

  private[this] def mergeStartAndLatestTokens
  (
    startTokens: Array[(NormalizedRange, Long)],
    latestTokens: Array[PartitionMetadata]
  ): Array[PartitionMetadata] = {

    val orderedBoundaries = mutable.SortedSet[String]()
    startTokens.foreach(token => {
      orderedBoundaries += token._1.min
      orderedBoundaries += token._1.max
    })
    latestTokens.foreach(metadata => {
      orderedBoundaries += metadata.feedRange.min
      orderedBoundaries += metadata.feedRange.max
    })

    var startTokensIndex = 0
    var latestTokensIndex = 0
    orderedBoundaries
      .foldLeft(None: Option[NormalizedRange])((previous: Option[NormalizedRange], current) => {
        previous match {
          case Some(previousRange) =>
            Some(NormalizedRange(previousRange.max, current))
          case None => None
        }
      })
      .map(range => {
        while (!SparkBridgeImplementationInternal.doRangesOverlap(range, startTokens(startTokensIndex)._1)) {
          startTokensIndex += 1
          assert(
            startTokensIndex < startTokens.length,
            "No overlapping start token found for range '$range' ")
        }

        while (!SparkBridgeImplementationInternal.doRangesOverlap(range, latestTokens(latestTokensIndex).feedRange)) {
          latestTokensIndex += 1
          assert(
            latestTokensIndex < latestTokens.length,
            "No overlapping latest token found for range '$range' ")
        }

        val startLsn: Long = startTokens(startTokensIndex)._2
        latestTokens(latestTokensIndex).cloneForSubRange(range, startLsn)
      })
      .toArray
  }

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
    ChangeFeedOffset(offsetJson, None)
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
