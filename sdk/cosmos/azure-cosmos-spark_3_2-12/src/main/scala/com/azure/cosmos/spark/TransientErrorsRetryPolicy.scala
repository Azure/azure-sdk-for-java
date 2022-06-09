// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

import java.util.concurrent.atomic.AtomicLong
import scala.util.Random
import scala.util.control.Breaks

private[spark] object TransientErrorsRetryPolicy extends BasicLoggingTrait {
  private val rnd = Random

  def executeWithRetry[T]
  (
    func: () => T,
    initialMaxRetryIntervalInMs: Int = CosmosConstants.initialMaxRetryIntervalForTransientFailuresInMs,
    maxRetryIntervalInMs: Int = CosmosConstants.maxRetryIntervalForTransientFailuresInMs,
    maxRetryCount: Int = Int.MaxValue
  ): T = {
    val loop = new Breaks()
    val retryCount = new AtomicLong(0)
    var returnValue: Option[T] = None

    loop.breakable {
      var currentMaxRetryIntervalInMs = Math.min(initialMaxRetryIntervalInMs, maxRetryIntervalInMs)
      while (true) {
        val retryIntervalInMs = rnd.nextInt(currentMaxRetryIntervalInMs)

        try {
          returnValue = Some(func())
          loop.break
        }
        catch {
          case cosmosException: CosmosException =>
            if (Exceptions.canBeTransientFailure(cosmosException.getStatusCode, cosmosException.getSubStatusCode)) {
              val retryCountSnapshot = retryCount.incrementAndGet()
              if (retryCountSnapshot > maxRetryCount) {
                logError(
                  s"Too many transient failure retry attempts ($retryCountSnapshot) in " +
                    s"TransientIORetryPolicy.executeWithRetry",
                  cosmosException)
                throw cosmosException
              } else {
                logWarning(
                  s"Transient failure handled in TransientIORetryPolicy.executeWithRetry -" +
                    s" will be retried (attempt#$retryCountSnapshot) in ${retryIntervalInMs}ms",
                  cosmosException)
              }
            } else {
              throw cosmosException
            }
          case other: Throwable => throw other
        }

        Thread.sleep(retryIntervalInMs)
        currentMaxRetryIntervalInMs = Math.min(2 * currentMaxRetryIntervalInMs, maxRetryIntervalInMs)
      }
    }

    returnValue.get
  }
}
