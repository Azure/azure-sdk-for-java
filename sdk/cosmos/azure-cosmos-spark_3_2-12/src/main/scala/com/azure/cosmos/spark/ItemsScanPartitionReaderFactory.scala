// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.CosmosParameterizedQuery
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

private case class ItemsScanPartitionReaderFactory
(
  config: Map[String, String],
  readSchema: StructType,
  cosmosQuery: CosmosParameterizedQuery,
  diagnosticsOperationContext: DiagnosticsContext,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  diagnosticsConfig: DiagnosticsConfig
) extends PartitionReaderFactory {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    val feedRange = partition.asInstanceOf[CosmosInputPartition].feedRange
    log.logInfo(s"Creating an ItemsPartitionReader to read from feed-range [$feedRange]")

    ItemsPartitionReader(config,
      feedRange,
      readSchema,
      cosmosQuery,
      diagnosticsOperationContext,
      cosmosClientStateHandle,
      diagnosticsConfig
    )
  }
}
