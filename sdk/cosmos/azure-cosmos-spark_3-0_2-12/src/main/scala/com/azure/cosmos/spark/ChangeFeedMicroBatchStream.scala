// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.spark.ChangeFeedStartFromModes.{Beginning, Now, PointInTime}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty, assertOnSparkDriver}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.connector.read.streaming.{MicroBatchStream, Offset, ReadLimit, SupportsAdmissionControl}
import org.apache.spark.sql.types.StructType

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

  override def latestOffset(): Offset = {
    throw new UnsupportedOperationException(
      "latestOffset(Offset, ReadLimit) should be called instead of this method")
  }

  override def planInputPartitions(startOffset: Offset, endOffset: Offset): Array[InputPartition] = ???

  override def createReaderFactory(): PartitionReaderFactory = ???

  override def latestOffset(startOffset: Offset, readLimit: ReadLimit): Offset = ???

  override def initialOffset(): Offset = {
    assertOnSparkDriver()
    val metadataLog =
      new ChangeFeedInitialOffsetWriter(
        assertNotNull(session, "session"),
        assertNotNullOrEmpty(checkpointLocation, "checkpointLocation"))
    metadataLog.get(0).getOrElse {
      this.changeFeedConfig.startFrom match {
        case Beginning => ChangeFeedOffset("")
        case Now => ChangeFeedOffset("")
        case PointInTime => ChangeFeedOffset("")
      }
    }
  }

  override def deserializeOffset(s: String): Offset = {
    ChangeFeedOffset.fromJson(s);
  }

  override def commit(offset: Offset): Unit = ???

  override def stop(): Unit = ???
}
