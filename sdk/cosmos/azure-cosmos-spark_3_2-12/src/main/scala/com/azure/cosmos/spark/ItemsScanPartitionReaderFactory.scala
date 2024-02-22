// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParameterizedQuery
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper, SparkTaskContext}
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

private case class ItemsScanPartitionReaderFactory
(
  config: Map[String, String],
  containerConfig: CosmosContainerConfig,
  readSchema: StructType,
  cosmosQuery: CosmosParameterizedQuery,
  diagnosticsOperationContext: DiagnosticsContext,
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String
) extends PartitionReaderFactory {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    val feedRange = partition.asInstanceOf[CosmosInputPartition].feedRange
    val readManyFiltersOpt = partition.asInstanceOf[CosmosInputPartition].readManyFilterOpt

    readManyFiltersOpt match {
      case Some(readManyFilters) => {
        log.logInfo(s"Creating an ItemsPartitionReaderWithReadMany to read from feed-range [$feedRange] ${containerConfig.container}")
        ItemsPartitionReaderWithReadMany(
          config,
          feedRange,
          readSchema,
          diagnosticsOperationContext,
          cosmosClientStateHandles,
          diagnosticsConfig,
          sparkEnvironmentInfo,
          TaskContext.get(),
          readManyFilters.iterator.map(idText => CosmosItemIdentityHelper.tryParseCosmosItemIdentity(idText).get))
      }
      case _ => {
        log.logInfo(s"Creating an ItemsPartitionReader to read from feed-range [$feedRange] ${containerConfig.container}")
        ItemsPartitionReader(config,
          feedRange,
          readSchema,
          cosmosQuery,
          diagnosticsOperationContext,
          cosmosClientStateHandles,
          diagnosticsConfig,
          sparkEnvironmentInfo
        )
      }
    }
  }
}
