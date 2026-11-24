// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.`export`.LogRecordExporter
import io.opentelemetry.sdk.logs.data.LogRecordData

import java.util
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

private[spark] class CosmosMaxCountPerIntervalLogExporter
(
  val delegate: LogRecordExporter,
  val thresholdSeverity: Severity,
  val maxLessThanThresholdSeverityLogCount: Int,
  val samplingIntervalInSeconds: Int
) extends LogRecordExporter with BasicLoggingTrait {

  require(maxLessThanThresholdSeverityLogCount >= 0, "Parameter 'maxLessThanThresholdSeverityLogCount' must be >= 0")
  require(samplingIntervalInSeconds > 0, "Parameter 'samplingIntervalInSeconds' must be at least 1.")

  private val thresholdSeverityNumber = thresholdSeverity.getSeverityNumber
  private val lessThanSeverityThresholdLogCountInInterval: AtomicInteger = new AtomicInteger(0)
  private val nextResetTimestamp: AtomicLong = new AtomicLong(
    System.currentTimeMillis() + samplingIntervalInSeconds * 1000)

  logInfo(s"Initialized CosmosMaxCountPerIntervalLogExporter <= $maxLessThanThresholdSeverityLogCount "
    + s"logs (with severity less than $thresholdSeverity)/$samplingIntervalInSeconds s")

  private[this] def shouldExport(logRecord: LogRecordData): Boolean = {
    if (logRecord.getSeverity.getSeverityNumber >= thresholdSeverityNumber) {
      true
    } else {
      val previousLogCount = lessThanSeverityThresholdLogCountInInterval.getAndIncrement
      if (previousLogCount <= maxLessThanThresholdSeverityLogCount) {
        true
      } else {
        val nowSnapshot = System.currentTimeMillis()
        val nextResetSnapshot = nextResetTimestamp.get()
        if (nowSnapshot > nextResetSnapshot) {
          nextResetTimestamp.set(nowSnapshot + samplingIntervalInSeconds * 1000)
          lessThanSeverityThresholdLogCountInInterval.set(0)

          true
        } else {
          if (previousLogCount == maxLessThanThresholdSeverityLogCount + 1) {
            logInfo(s"Already logged $maxLessThanThresholdSeverityLogCount log records with severity less than "
              + s"$thresholdSeverity - dropping those until sampling interval is reset at $nextResetSnapshot.")
          }

          false
        }
      }
    }
  }

  override def `export`(collection: util.Collection[LogRecordData]): CompletableResultCode = {
    val maintainedLogRecords = new util.ArrayList[LogRecordData](collection.size())
    collection.forEach(logRecord => {
      if (shouldExport(logRecord)) {
        maintainedLogRecords.add(logRecord)
      }
    })

    if (maintainedLogRecords.size() > 0) {
      delegate.`export`(maintainedLogRecords)
    } else {
      CompletableResultCode.ofSuccess()
    }
  }

  override def flush(): CompletableResultCode = delegate.flush()

  override def shutdown(): CompletableResultCode = delegate.shutdown()
}
