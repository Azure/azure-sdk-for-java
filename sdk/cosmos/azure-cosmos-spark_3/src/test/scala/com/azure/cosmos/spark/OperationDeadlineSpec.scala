// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import reactor.core.publisher.Mono

import java.time.Duration
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class OperationDeadlineSpec extends UnitSpec {
  "OperationDeadline" should "cancel a non-completing reactive operation when the deadline expires" in {
    val cancelled = new AtomicBoolean(false)
    val neverCompletingOperation = Mono.never[String]().doOnCancel(() => cancelled.set(true))
    val deadline = OperationDeadline(Duration.ofMillis(25), "Test metadata discovery")

    val exception = intercept[TimeoutException] {
      deadline.block(neverCompletingOperation)
    }

    exception.getMessage should include ("Test metadata discovery")
    exception.getMessage should include ("PT0.025S")
    cancelled.get shouldBe true
  }

  it should "reject a non-positive timeout" in {
    intercept[IllegalArgumentException] {
      OperationDeadline(Duration.ZERO, "Test metadata discovery")
    }
  }

  it should "preserve the interrupt status when sleep is interrupted" in {
    val deadline = OperationDeadline(Duration.ofSeconds(1), "Test metadata discovery")
    Thread.currentThread().interrupt()

    try {
      intercept[InterruptedException] {
        deadline.sleep(1)
      }
      Thread.currentThread().isInterrupted shouldBe true
    } finally {
      Thread.interrupted()
    }
  }

  it should "support timeout durations that cannot be represented in nanoseconds" in {
    val deadline = OperationDeadline(Duration.ofSeconds(Long.MaxValue), "Test metadata discovery")

    deadline.remainingDuration shouldEqual Duration.ofNanos(Long.MaxValue)
  }
}
