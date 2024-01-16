// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.models.CosmosParameterizedQuery
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}
import org.apache.spark.sql.execution.datasources.v2.EmptyPartitionReader
import org.apache.spark.sql.types.StructType

import java.util.concurrent.atomic.AtomicReference

private case class ItemsScanPartitionReaderFactory
(
  config: Map[String, String],
  containerConfig: CosmosContainerConfig,
  readSchema: StructType,
  cosmosQuery: CosmosParameterizedQuery,
  diagnosticsOperationContext: DiagnosticsContext,
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String,
  readManyFiltersMapRef: AtomicReference[Map[NormalizedRange, String]]
) extends PartitionReaderFactory {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    val feedRange = partition.asInstanceOf[CosmosInputPartition].feedRange

    if (readManyFiltersMapRef.get() == null) {
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
    } else {
      val readManyFilters =
        readManyFiltersMapRef
          .get()
          .filter(readManyFilterEntry => {
            SparkBridgeImplementationInternal.doRangesOverlap(
              readManyFilterEntry._1,
              feedRange
            )
          })
          .values
          .toList

      if (readManyFilters.nonEmpty) {
        log.logInfo(s"Creating an ItemsPartitionReaderWithReadMany to read from feed-range [$feedRange] ${containerConfig.container}")
        ItemsPartitionReaderWithReadMany(
          config,
          feedRange,
          readSchema,
          diagnosticsOperationContext,
          cosmosClientStateHandles,
          diagnosticsConfig,
          sparkEnvironmentInfo,
          readManyFilters)
      } else {
        log.logInfo(s"Creating an EmptyPartitionReader to read from feed-range [$feedRange] ${containerConfig.container} " +
          s"as there is no matching item identity")
        new EmptyPartitionReader[InternalRow]
      }
    }
  }
}
