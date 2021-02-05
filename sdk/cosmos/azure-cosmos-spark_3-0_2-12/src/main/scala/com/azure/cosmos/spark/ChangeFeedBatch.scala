// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.FeedRange
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

private class ChangeFeedBatch
(
  schema: StructType,
  config: Map[String, String],
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
) extends Batch
  with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  override def planInputPartitions(): Array[InputPartition] = {
    // TODO: moderakh use get feed range?
    // for now we are returning one partition hence only one spark task will be created.
    Array(FeedRangeInputPartition(FeedRange.forFullRange.toString))
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    ChangeFeedScanPartitionReaderFactory(config, schema, cosmosClientStateHandle)
  }
}
