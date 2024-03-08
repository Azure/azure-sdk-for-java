// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosDiagnosticsContext
import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.WriterCommitMessage
import org.apache.spark.sql.types.StructType

import java.util.concurrent.atomic.AtomicLong

private class CosmosWriter(
                            userConfig: Map[String, String],
                            cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
                            diagnosticsConfig: DiagnosticsConfig,
                            inputSchema: StructType,
                            partitionId: Int,
                            taskId: Long,
                            epochId: Option[Long],
                            sparkEnvironmentInfo: String)
  extends CosmosWriterBase(
    userConfig,
    cosmosClientStateHandles,
    diagnosticsConfig,
    inputSchema,
    partitionId,
    taskId,
    epochId,
    sparkEnvironmentInfo
  ) with OutputMetricsPublisherTrait {

  private val recordsWritten = new AtomicLong(0)
  private val bytesWritten = new AtomicLong(0)
  private val count: AtomicLong = new AtomicLong(0)
  override def getOutputMetricsPublisher(): OutputMetricsPublisherTrait = this

  override def trackWriteOperation(recordCount: Long, diagnostics: Option[CosmosDiagnosticsContext]): Unit = {
    if (recordCount > 0) {
      recordsWritten.addAndGet(recordCount)
    }

    diagnostics match {
      case Some(ctx) =>
        bytesWritten.addAndGet(
          if (ImplementationBridgeHelpers
            .CosmosDiagnosticsContextHelper
            .getCosmosDiagnosticsContextAccessor
            .getOperationType(ctx)
            .isReadOnlyOperation) {

            ctx.getMaxRequestPayloadSizeInBytes + ctx.getMaxResponsePayloadSizeInBytes
          } else {
            ctx.getMaxRequestPayloadSizeInBytes
          }
        )
      case None =>
    }
  }

  override def write(internalRow: InternalRow): Unit = {
    super.write(internalRow)

    if (count.incrementAndGet() % SparkInternalsBridge.NUM_ROWS_PER_UPDATE == 0) {
      SparkInternalsBridge.updateInternalTaskMetrics(recordsWritten.get, bytesWritten.get)
    }
  }

  override def commit(): WriterCommitMessage = {
    val commitMessage = super.commit()

    // In Spark 3.1 there is no concept of custom metrics yet, updating bytesWritten and recordsWritten
    // needs to be done manually in the DataSource (using internal TaskMetrics API) - so, this is a pretty
    // ugly workaround - but given that Spark 3.1 is close to end-of-life already the risk of the
    // behavior changing within Spark 3.1 is low enough to make it an acceptable workaround
    SparkInternalsBridge.updateInternalTaskMetrics(recordsWritten.get, bytesWritten.get)

    commitMessage
  }
}
