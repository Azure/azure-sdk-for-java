// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.CosmosParameterizedQuery
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

private case class ItemsScanPartitionReaderFactory
(
  config: Map[String, String],
  readSchema: StructType,
  cosmosQuery: CosmosParameterizedQuery,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
) extends PartitionReaderFactory with CosmosLoggingTrait {

  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    ItemsPartitionReader(config,
      readSchema,
      cosmosQuery,
      cosmosClientStateHandle)
  }
}
