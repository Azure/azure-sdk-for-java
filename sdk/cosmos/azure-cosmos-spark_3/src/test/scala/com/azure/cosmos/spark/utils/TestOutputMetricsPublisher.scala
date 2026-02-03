// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.utils

import com.azure.cosmos.CosmosDiagnosticsContext
import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.spark.OutputMetricsPublisherTrait

import java.util.concurrent.atomic.AtomicLong

private[spark] class TestOutputMetricsPublisher extends OutputMetricsPublisherTrait {
  private val recordsWritten = new AtomicLong(0)
  private val bytesWritten = new AtomicLong(0)
  private val totalRequestCharge = new AtomicLong()


  override def trackWriteOperation(recordCount: Long, diagnostics: Option[CosmosDiagnosticsContext]): Unit = {
    if (recordCount > 0) {
      recordsWritten.addAndGet(recordCount)
    }

    diagnostics match {
      case Some(ctx) => {
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
      }
      case None =>
    }
  }

  def getRecordsWrittenSnapshot(): Long = recordsWritten.get()

  def getBytesWrittenSnapshot(): Long = bytesWritten.get()

  def getTotalRequestChargeSnapshot(): Long = totalRequestCharge.get() / 100
}