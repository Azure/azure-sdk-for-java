package com.azure.cosmos.spark

import com.azure.core.util.Context
import com.azure.cosmos.{CosmosDiagnosticsContext, CosmosDiagnosticsHandler}
import com.azure.cosmos.implementation.CosmosDaemonThreadFactory
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger

class CosmosSamplingDiagnosticsLogger(val maxLogCount: Int, val samplingIntervalInSeconds: Int)  extends CosmosDiagnosticsHandler with BasicLoggingTrait {
  require(maxLogCount > 0, "Argument 'maxLogCount must be a positive integer.")

  private val logCountInSamplingInterval: AtomicInteger = new AtomicInteger(0)
  logInfo(s"MaxLogCount: $maxLogCount, samplingIntervalInSeconds: $samplingIntervalInSeconds")

  private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
    new CosmosDaemonThreadFactory("CosmosSamplingDiagnosticsLogger"))

  executor.scheduleAtFixedRate(
    () => {
      val snapshot = this.logCountInSamplingInterval.getAndSet(0)
      if (snapshot != 0) {
        logDebug(s"Resetting number of logs ($snapshot->0)...")
      }
    },
    samplingIntervalInSeconds,
    samplingIntervalInSeconds,
    TimeUnit.SECONDS
  )

  /**
   * Decides whether to log diagnostics for an operation and emits the logs when needed
   *
   * @param diagnosticsContext the Cosmos DB diagnostic context with metadata for the operation
   * @param traceContext       the Azure trace context
   */
  override def handleDiagnostics(diagnosticsContext: CosmosDiagnosticsContext, traceContext: Context): Unit = {
    require(Option.apply(diagnosticsContext).isDefined, "Argument 'diagnosticsContext' must not be null.")

    if (shouldLog(diagnosticsContext)) {
      val previousLogCount = this.logCountInSamplingInterval.getAndIncrement
      if (previousLogCount <= this.maxLogCount) {
        logInfo(s"Account: ${diagnosticsContext.getAccountName} -> DB: ${diagnosticsContext.getDatabaseName}, Col:${diagnosticsContext.getContainerName}, StatusCode: ${diagnosticsContext.getStatusCode}${diagnosticsContext.getSubStatusCode} Diagnostics: ${diagnosticsContext.toJson}")
      } else if (previousLogCount == this.maxLogCount + 1) {
        logInfo(s"Already logged $maxLogCount diagnostics - stopping until sampling interval is reset.")
      }
    }
  }

  /**
   * Decides whether to log diagnostics for an operation
   *
   * @param diagnosticsContext the diagnostics context
   * @return a flag indicating whether to log the operation or not
   */
  protected def shouldLog(diagnosticsContext: CosmosDiagnosticsContext): Boolean = {
    if (!diagnosticsContext.isCompleted) return false
    diagnosticsContext.isFailure || diagnosticsContext.isThresholdViolated || isDebugLogEnabled || isTraceLogEnabled
  }

  /**
   * Logs the operation. This method can be overridden for example to emit logs to a different target than log4j
   *
   * @param ctx the diagnostics context
   */
  protected def log(ctx: CosmosDiagnosticsContext): Unit = {
    if (ctx.isFailure) {
      logError(s"Account: ${ctx.getAccountName} -> DB: ${ctx.getDatabaseName}, Col:${ctx.getContainerName}, StatusCode: ${ctx.getStatusCode}:${ctx.getSubStatusCode} Diagnostics: ${ctx.toJson}")
    } else if (ctx.isThresholdViolated) {
      logInfo(s"Account: ${ctx.getAccountName} -> DB: ${ctx.getDatabaseName}, Col:${ctx.getContainerName}, StatusCode: ${ctx.getStatusCode}:${ctx.getSubStatusCode} Diagnostics: ${ctx.toJson}")
    } else if (isTraceLogEnabled) {
      logTrace(s"Account: ${ctx.getAccountName} -> DB: ${ctx.getDatabaseName}, Col:${ctx.getContainerName}, StatusCode: ${ctx.getStatusCode}:${ctx.getSubStatusCode} Diagnostics: ${ctx.toJson}")
    } else if (isDebugLogEnabled) {
      logDebug(s"Account: ${ctx.getAccountName} -> DB: ${ctx.getDatabaseName}, Col:${ctx.getContainerName}, StatusCode: ${ctx.getStatusCode}:${ctx.getSubStatusCode}, Latency: ${ctx.getDuration}, Request charge: ${ctx.getTotalRequestCharge}")
    }
  }
}
