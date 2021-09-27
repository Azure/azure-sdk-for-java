// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver}
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.streaming.{MicroBatchStream, Offset, ReadLimit, SupportsAdmissionControl}
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

import java.time.Duration
import java.util.UUID

// scala style rule flaky - even complaining on partial log messages
// scalastyle:off multiple.string.literals
private class ChangeFeedMicroBatchStream
(
  val session: SparkSession,
  val schema: StructType,
  val config: Map[String, String],
  val cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  val checkpointLocation: String,
  diagnosticsConfig: DiagnosticsConfig
) extends MicroBatchStream
  with SupportsAdmissionControl {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  private val streamId = UUID.randomUUID().toString
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}.$streamId")

  private val defaultParallelism = session.sparkContext.defaultParallelism
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val clientConfiguration = CosmosClientConfiguration.apply(config, readConfig.forceEventualConsistency)
  private val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val partitioningConfig = CosmosPartitioningConfig.parseCosmosPartitioningConfig(config)
  private val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(config)
  private val client = CosmosClientCache(clientConfiguration, Some(cosmosClientStateHandle))
  private val container = ThroughputControlHelper.getContainer(config, containerConfig, client)

  SparkUtils.safeOpenConnectionInitCaches(container, log)

  private var latestOffsetSnapshot: Option[ChangeFeedOffset] = None

  override def latestOffset(): Offset = {
    // For Spark data streams implementing SupportsAdmissionControl trait
    // latestOffset(Offset, ReadLimit) is called instead
    throw new UnsupportedOperationException(
      "latestOffset(Offset, ReadLimit) should be called instead of this method")
  }

  /**
   * Returns a list of `InputPartition` given the start and end offsets. Each
   * `InputPartition` represents a data split that can be processed by one Spark task. The
   * number of input partitions returned here is the same as the number of RDD partitions this scan
   * outputs.
   * <p>
   * If the `Scan` supports filter push down, this stream is likely configured with a filter
   * and is responsible for creating splits for that filter, which is not a full scan.
   * </p>
   * <p>
   * This method will be called multiple times, to launch one Spark job for each micro-batch in this
   * data stream.
   * </p>
   */
  override def planInputPartitions(startOffset: Offset, endOffset: Offset): Array[InputPartition] = {
    assertNotNull(startOffset, "startOffset")
    assertNotNull(endOffset, "endOffset")
    assert(startOffset.isInstanceOf[ChangeFeedOffset], "Argument 'startOffset' is not a change feed offset.")
    assert(endOffset.isInstanceOf[ChangeFeedOffset], "Argument 'endOffset' is not a change feed offset.")

    log.logInfo(s"--> planInputPartitions.$streamId, startOffset: ${startOffset.json()} - endOffset: ${endOffset.json()}")
    val start = startOffset.asInstanceOf[ChangeFeedOffset]
    val end = endOffset.asInstanceOf[ChangeFeedOffset]

    val startChangeFeedState = new String(java.util.Base64.getUrlDecoder.decode(start.changeFeedState))
    log.logInfo(s"Start-ChangeFeedState.$streamId: $startChangeFeedState")

    val endChangeFeedState = new String(java.util.Base64.getUrlDecoder.decode(end.changeFeedState))
    log.logInfo(s"End-ChangeFeedState.$streamId: $endChangeFeedState")

    assert(end.inputPartitions.isDefined, "Argument 'endOffset.inputPartitions' must not be null or empty.")

    end
      .inputPartitions
      .get
      .map(partition => partition
        .withContinuationState(
          SparkBridgeImplementationInternal
            .extractChangeFeedStateForRange(start.changeFeedState, partition.feedRange),
          clearEndLsn = false))
  }

  /**
   * Returns a factory to create a `PartitionReader` for each `InputPartition`.
   */
  override def createReaderFactory(): PartitionReaderFactory = {
    log.logInfo(s"--> createReaderFactory.$streamId")
    ChangeFeedScanPartitionReaderFactory(config, schema, cosmosClientStateHandle, diagnosticsConfig)
  }

  /**
   * Returns the most recent offset available given a read limit. The start offset can be used
   * to figure out how much new data should be read given the limit. Users should implement this
   * method instead of latestOffset for a MicroBatchStream or getOffset for Source.
   *
   * When this method is called on a `Source`, the source can return `null` if there is no
   * data to process. In addition, for the very first micro-batch, the `startOffset` will be
   * null as well.
   *
   * When this method is called on a MicroBatchStream, the `startOffset` will be `initialOffset`
   * for the very first micro-batch. The source can return `null` if there is no data to process.
   */
  // This method is doing all the heavy lifting - after calculating the latest offset
  // all information necessary to plan partitions is available - so we plan partitions here and
  // serialize them in the end offset returned to avoid any IO calls for the actual partitioning
  override def latestOffset(startOffset: Offset, readLimit: ReadLimit): Offset = {

    log.logInfo(s"--> latestOffset.$streamId")

    val startChangeFeedOffset = startOffset.asInstanceOf[ChangeFeedOffset]
    val offset = CosmosPartitionPlanner.getLatestOffset(
      config,
      startChangeFeedOffset,
      readLimit,
      Duration.ZERO,
      this.clientConfiguration,
      this.cosmosClientStateHandle,
      this.containerConfig,
      this.partitioningConfig,
      this.defaultParallelism,
      this.container
    )

    if (offset.changeFeedState != startChangeFeedOffset.changeFeedState) {
      log.logInfo(s"<-- latestOffset.$streamId - new offset ${offset.json()}")
      this.latestOffsetSnapshot = Some(offset)
      offset
    } else {
      log.logInfo(s"<-- latestOffset.$streamId - Finished returning null")

      this.latestOffsetSnapshot = None

      // scalastyle:off null
      // null means no more data to process
      // null is used here because the DataSource V2 API is defined in Java
      null
      // scalastyle:on null
    }
  }

  /**
   * Returns the initial offset for a streaming query to start reading from. Note that the
   * streaming data source should not assume that it will start reading from its initial offset:
   * if Spark is restarting an existing query, it will restart from the check-pointed offset rather
   * than the initial one.
   */
  // Mapping start form settings to the initial offset/LSNs
  override def initialOffset(): Offset = {
    assertOnSparkDriver()

    val metadataLog = new ChangeFeedInitialOffsetWriter(
        assertNotNull(session, "session"),
        assertNotNullOrEmpty(checkpointLocation, "checkpointLocation"))
    val offsetJson = metadataLog.get(0).getOrElse {
      val newOffsetJson = CosmosPartitionPlanner.createInitialOffset(container, changeFeedConfig, Some(streamId))
      metadataLog.add(0, newOffsetJson)
      newOffsetJson
    }

    log.logInfo(s"MicroBatch stream $streamId: Initial offset '$offsetJson'.")
    ChangeFeedOffset(offsetJson, None)
  }

  /**
   * Returns the read limits potentially passed to the data source through options when creating
   * the data source.
   */
  override def getDefaultReadLimit: ReadLimit = {
    this.changeFeedConfig.toReadLimit
  }

  /**
   * Returns the most recent offset available.
   *
   * The source can return `null`, if there is no data to process or the source does not support
   * to this method.
   */
  // TODO @fabianm mark this override when switching to Spark 3.2.0
  def reportLatestOffset(): Offset = {
    this.latestOffsetSnapshot.orNull
  }

  /**
   * Deserialize a JSON string into an Offset of the implementation-defined offset type.
   *
   * @throws IllegalArgumentException if the JSON does not encode a valid offset for this reader
   */
  override def deserializeOffset(s: String): Offset = {
    log.logDebug(s"MicroBatch stream $streamId: Deserialized offset '$s'.")
    ChangeFeedOffset.fromJson(s)
  }

  /**
   * Informs the source that Spark has completed processing all data for offsets less than or
   * equal to `end` and will only request offsets greater than `end` in the future.
   */
  override def commit(offset: Offset): Unit = {
    log.logInfo(s"MicroBatch stream $streamId: Committed offset '${offset.json()}'.")
  }

  /**
   * Stop this source and free any resources it has allocated.
   */
  override def stop(): Unit = {
    log.logInfo(s"MicroBatch stream $streamId: stopped.")
  }
}
// scalastyle:on multiple.string.literals
