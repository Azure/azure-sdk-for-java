// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

//scalastyle:off magic.number
//scalastyle:off multiple.string.literals
class TransientErrorsRetryPolicySpec extends UnitSpec with BasicLoggingTrait {
  "TransientErrorsRetryPolicy" should "execute successfully when errorCount < maxRetryCount" in {

    val expected = UUID.randomUUID().toString
    val attempt = new AtomicLong(0)
    val testFunc: () => String = () => {
      if (attempt.incrementAndGet() < 5) {
        throw new DummyTransientCosmosException()
      }

      expected
    }

    val returnValue = TransientErrorsRetryPolicy
      .executeWithRetry(testFunc)

    returnValue shouldEqual expected
  }

  "TransientErrorsRetryPolicy" should "throw any non transient errors" in {

    val expected = UUID.randomUUID().toString
    val attempt = new AtomicLong(0)
    val testFunc: () => String = () => {
      if (attempt.incrementAndGet() < 5) {
        throw new DummyNonTransientCosmosException()
      }

      expected
    }

    var thrownException: Option[CosmosException] = None
    try {
      TransientErrorsRetryPolicy
        .executeWithRetry(testFunc)

      fail("Expected exception not thrown")
    } catch {
      case e: CosmosException => thrownException = Some(e)
    }

    thrownException should not be empty
    thrownException.get.getStatusCode shouldEqual new DummyNonTransientCosmosException().getStatusCode
  }

  "TransientErrorsRetryPolicy" should "throw an exceptions when errorCount > maxRetryCount" in {

    val expected = UUID.randomUUID().toString
    val attempt = new AtomicLong(0)
    val testFunc: () => String = () => {
      if (attempt.incrementAndGet() < 5) {
        throw new DummyTransientCosmosException()
      }

      expected
    }

    var thrownException: Option[CosmosException] = None
    try {
      TransientErrorsRetryPolicy
        .executeWithRetry(testFunc, maxRetryCount = 3)

      fail("Expected exception not thrown")
    } catch {
      case e: CosmosException => thrownException = Some(e)
    }

    thrownException should not be empty
    thrownException.get.getStatusCode shouldEqual new DummyTransientCosmosException().getStatusCode
  }

  private class DummyTransientCosmosException
    extends CosmosException(500, "Dummy Internal Server Error")

  private class DummyNonTransientCosmosException
    extends CosmosException(404, "Dummy Not Found Error")
}
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals
