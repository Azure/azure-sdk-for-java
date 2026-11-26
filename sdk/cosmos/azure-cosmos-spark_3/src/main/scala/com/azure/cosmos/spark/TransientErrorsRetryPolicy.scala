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

  //scalastyle:off method.length
  def executeWithRetry[T]
  (
    func: () => T,
    initialMaxRetryIntervalInMs: Int = CosmosConstants.initialMaxRetryIntervalForTransientFailuresInMs,
    maxRetryIntervalInMs: Int = CosmosConstants.maxRetryIntervalForTransientFailuresInMs,
    maxRetryCount: Int = Int.MaxValue,
    statusResetFuncBetweenRetry: Option[() => Unit] = None,
    operationDeadline: Option[OperationDeadline] = None
  ): T = {
    val loop = new Breaks()
    val retryCount = new AtomicLong(0)
    var returnValue: Option[T] = None

    loop.breakable {
      var currentMaxRetryIntervalInMs = Math.min(initialMaxRetryIntervalInMs, maxRetryIntervalInMs)
      while (true) {
        val retryIntervalInMs = rnd.nextInt(currentMaxRetryIntervalInMs)

        try {
          operationDeadline.foreach(_.remainingDuration)
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

        if (statusResetFuncBetweenRetry.isDefined) {
          statusResetFuncBetweenRetry.get.apply()
        }

        operationDeadline match {
          case Some(deadline) => deadline.sleep(retryIntervalInMs)
          case None =>
            try {
              Thread.sleep(retryIntervalInMs)
            } catch {
              case interrupted: InterruptedException =>
                Thread.currentThread().interrupt()
                throw interrupted
            }
        }
        currentMaxRetryIntervalInMs = Math.min(2 * currentMaxRetryIntervalInMs, maxRetryIntervalInMs)
      }
    }

    returnValue.get
  }
}
