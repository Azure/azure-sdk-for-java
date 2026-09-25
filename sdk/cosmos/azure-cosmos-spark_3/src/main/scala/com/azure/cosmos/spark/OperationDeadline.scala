// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import reactor.core.{Exceptions => ReactorExceptions}
import reactor.core.publisher.Mono

import java.time.Duration
import java.util.concurrent.TimeoutException

private[spark] final class OperationDeadline private(
    timeout: Duration,
    operationName: String,
    deadlineInNanos: Long) {

  def remainingDuration: Duration = {
    if (deadlineInNanos == Long.MaxValue) {
      Duration.ofNanos(Long.MaxValue)
    } else {
      val remainingNanos = deadlineInNanos - System.nanoTime()
      if (remainingNanos <= 0) {
        throw timeoutException()
      }

      Duration.ofNanos(remainingNanos)
    }
  }

  def block[T](mono: Mono[T]): T = {
    try {
      mono.timeout(remainingDuration).block()
    } catch {
      case error: RuntimeException if ReactorExceptions.unwrap(error).isInstanceOf[TimeoutException] =>
        throw timeoutException(error)
    }
  }

  def sleep(durationInMillis: Int): Unit = {
    val remaining = remainingDuration
    val requested = Duration.ofMillis(durationInMillis.toLong)
    val effective = if (requested.compareTo(remaining) < 0) requested else remaining

    val millis = effective.toMillis
    val additionalNanos = (effective.minusMillis(millis).toNanos).toInt
    try {
      Thread.sleep(millis, additionalNanos)
    } catch {
      case interrupted: InterruptedException =>
        Thread.currentThread().interrupt()
        throw interrupted
    }

    if (requested.compareTo(remaining) >= 0) {
      remainingDuration
    }
  }

  private def timeoutException(cause: Throwable = null): TimeoutException = {
    val exception = new TimeoutException(
      s"$operationName did not complete within $timeout.")
    if (cause != null) {
      exception.initCause(cause)
    }
    exception
  }
}

private[spark] object OperationDeadline {
  def apply(timeout: Duration, operationName: String): OperationDeadline = {
    require(timeout != null && !timeout.isZero && !timeout.isNegative, "timeout must be positive")

    val timeoutNanos = try {
      timeout.toNanos
    } catch {
      case _: ArithmeticException => Long.MaxValue
    }
    val deadline = if (timeoutNanos == Long.MaxValue) {
      Long.MaxValue
    } else {
      try {
        Math.addExact(System.nanoTime(), timeoutNanos)
      } catch {
        case _: ArithmeticException => Long.MaxValue
      }
    }
    new OperationDeadline(timeout, operationName, deadline)
  }
}
