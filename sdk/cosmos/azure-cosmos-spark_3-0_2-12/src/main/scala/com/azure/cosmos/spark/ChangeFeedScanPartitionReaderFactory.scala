// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

private case class ChangeFeedScanPartitionReaderFactory
(
  config: Map[String, String],
  readSchema: StructType,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
) extends PartitionReaderFactory with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    val changeFeedPartition = partition.asInstanceOf[CosmosInputPartition]
    ChangeFeedPartitionReader(changeFeedPartition, config, readSchema, cosmosClientStateHandle)
  }
}
