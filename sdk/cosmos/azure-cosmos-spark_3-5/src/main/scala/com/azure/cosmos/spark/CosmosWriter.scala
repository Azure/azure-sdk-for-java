// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosDiagnosticsContext
import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.metric.CustomTaskMetric
import org.apache.spark.sql.connector.write.WriterCommitMessage
import org.apache.spark.sql.execution.metric.CustomMetrics
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
  private val totalRequestCharge = new AtomicLong(0)

  private val  recordsWrittenMetric = new CustomTaskMetric {
    override def name(): String = CosmosConstants.MetricNames.RecordsWritten
    override def value(): Long = recordsWritten.get()
  }

  private val bytesWrittenMetric = new CustomTaskMetric {
    override def name(): String = CosmosConstants.MetricNames.BytesWritten

    override def value(): Long = bytesWritten.get()
  }

  private val totalRequestChargeMetric = new CustomTaskMetric {
    override def name(): String = CosmosConstants.MetricNames.TotalRequestCharge

    // Internally we capture RU/s up to 2 fractional digits to have more precise rounding
    override def value(): Long = totalRequestCharge.get() / 100L
  }

  private val metrics = Array(recordsWrittenMetric, bytesWrittenMetric, totalRequestChargeMetric)

  override def currentMetricsValues(): Array[CustomTaskMetric] = {
    metrics
  }

  override def getOutputMetricsPublisher(): OutputMetricsPublisherTrait = this

  override def trackWriteOperation(recordCount: Long, diagnostics: Option[CosmosDiagnosticsContext]): Unit = {
    if (recordCount > 0) {
      recordsWritten.addAndGet(recordCount)
    }

    diagnostics match {
      case Some(ctx) =>
        // Capturing RU/s with 2 fractional digits internally
        totalRequestCharge.addAndGet((ctx.getTotalRequestCharge * 100L).toLong)
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

  override def commit(): WriterCommitMessage = {
    val commitMessage = super.commit()

    // TODO @fabianm - this is a workaround - it shouldn't be necessary to do this here
    // Unfortunately WriteToDataSourceV2Exec.scala is not updating custom metrics after the
    // call to commit - meaning DataSources which asynchronously write data and flush in commit
    // won't get accurate metrics because updates between the last call to write and flushing the
    // writes are lost. See https://issues.apache.org/jira/browse/SPARK-45759
    // Once above issue is addressed (probably in Spark 3.4.1 or 3.5 - this needs to be changed
    //
    // NOTE: This also means that the RU/s metrics cannot be updated in commit - so the
    // RU/s metric at the end of a task will be slightly outdated/behind
    CustomMetrics.updateMetrics(
      currentMetricsValues(),
      SparkInternalsBridge.getInternalCustomTaskMetricsAsSQLMetric(CosmosConstants.MetricNames.KnownCustomMetricNames))

    commitMessage
  }
}
