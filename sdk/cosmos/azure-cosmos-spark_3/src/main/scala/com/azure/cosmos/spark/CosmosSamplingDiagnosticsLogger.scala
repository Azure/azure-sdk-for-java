package com.azure.cosmos.spark

import com.azure.core.util.Context
import com.azure.cosmos.{CosmosDiagnosticsContext, CosmosDiagnosticsHandler}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

private[spark] class CosmosSamplingDiagnosticsLogger(val maxLogCount: Int, val samplingIntervalInSeconds: Int)  extends CosmosDiagnosticsHandler with BasicLoggingTrait {
  require(maxLogCount > 0, "Argument 'maxLogCount' must be a positive integer.")

  private val logCountInSamplingInterval: AtomicInteger = new AtomicInteger(0)
  private val nextReset: AtomicLong = new AtomicLong(System.currentTimeMillis() + samplingIntervalInSeconds * 1000)
  logInfo(s"Initialized - MaxLogCount: $maxLogCount, samplingIntervalInSeconds: $samplingIntervalInSeconds")

  /**
   * Decides whether to log diagnostics for an operation and emits the logs when needed
   *
   * @param diagnosticsContext the Cosmos DB diagnostic context with metadata for the operation
   * @param traceContext       the Azure trace context
   */
  override def handleDiagnostics(diagnosticsContext: CosmosDiagnosticsContext, traceContext: Context): Unit = {
    require(Option.apply(diagnosticsContext).isDefined, "Argument 'diagnosticsContext' must not be null.")

    if (shouldLog(diagnosticsContext)) {
      log(diagnosticsContext)
    }
  }

  /**
   * Decides whether to log diagnostics for an operation
   *
   * @param diagnosticsContext the diagnostics context
   * @return a flag indicating whether to log the operation or not
   */
  private def shouldLog(diagnosticsContext: CosmosDiagnosticsContext): Boolean = {
    if (!diagnosticsContext.isCompleted) {
      return false
    }

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
    } else {
      var shouldLog = false
      if (ctx.isThresholdViolated) {
        val previousLogCount = logCountInSamplingInterval.getAndIncrement
        if (previousLogCount <= maxLogCount) {
          shouldLog = true
        } else {
          val nowSnapshot = System.currentTimeMillis()
          val nextResetSnapshot = nextReset.get()
          if (nowSnapshot > nextResetSnapshot) {
            nextReset.set(nowSnapshot + samplingIntervalInSeconds * 1000)
            logCountInSamplingInterval.set(0)
            shouldLog = true
          } else if (previousLogCount == maxLogCount + 1) {
            logInfo(s"Already logged $maxLogCount diagnostics - stopping until sampling interval is reset at $nextResetSnapshot.")
          }
        }
      }

      if (shouldLog) {
        logInfo(s"Account: ${ctx.getAccountName} -> DB: ${ctx.getDatabaseName}, Col:${ctx.getContainerName}, StatusCode: ${ctx.getStatusCode}:${ctx.getSubStatusCode} Diagnostics: ${ctx.toJson}")
      } else if (isTraceLogEnabled) {
        logTrace(s"Account: ${ctx.getAccountName} -> DB: ${ctx.getDatabaseName}, Col:${ctx.getContainerName}, StatusCode: ${ctx.getStatusCode}:${ctx.getSubStatusCode} Diagnostics: ${ctx.toJson}")
      } else if (isDebugLogEnabled) {
        logDebug(s"Account: ${ctx.getAccountName} -> DB: ${ctx.getDatabaseName}, Col:${ctx.getContainerName}, StatusCode: ${ctx.getStatusCode}:${ctx.getSubStatusCode}, Latency: ${ctx.getDuration}, Request charge: ${ctx.getTotalRequestCharge}")
      }
    }
  }
}
