// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosItemIdentity, CosmosParameterizedQuery}
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}
import org.apache.spark.sql.execution.datasources.v2.EmptyPartitionReader
import org.apache.spark.sql.types.StructType

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable.ListBuffer

private case class ItemsScanPartitionReaderFactory
(
    config: Map[String, String],
    readSchema: StructType,
    cosmosQuery: CosmosParameterizedQuery,
    diagnosticsOperationContext: DiagnosticsContext,
    cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
    diagnosticsConfig: DiagnosticsConfig,
    sparkEnvironmentInfo: String,
    readManyFilterRef: AtomicReference[Map[NormalizedRange, List[String]]]
) extends PartitionReaderFactory {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    val feedRange = partition.asInstanceOf[CosmosInputPartition].feedRange
    log.logInfo(s"Creating an ItemsPartitionReader to read from feed-range [$feedRange]")

    if (readManyFilterRef.get() == null) {
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
        val readManyFilter = readManyFilterRef.get().get(partition.asInstanceOf[CosmosInputPartition].feedRange)
        readManyFilter match {
            case Some(readManyFilterList) =>
                ItemsPartitionReaderWithReadMany(
                    config,
                    feedRange,
                    readSchema,
                    diagnosticsOperationContext,
                    cosmosClientStateHandles,
                    diagnosticsConfig,
                    sparkEnvironmentInfo,
                    readManyFilterList
                )
            case _ => new EmptyPartitionReader[InternalRow]
        }
    }
  }

  def broadcastPartitionFilterMap(partitionFilterMap: Map[NormalizedRange, ListBuffer[CosmosItemIdentity]]): Unit = {

  }
}
