// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{ServiceUnavailableException, Strings, Utils}
import com.azure.cosmos.models.{CosmosQueryRequestOptions, ModelBridgeInternal}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.CosmosPagedIterable
import com.fasterxml.jackson.databind.node.ObjectNode
import org.scalatest.Ignore

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

//scalastyle:off magic.number
//scalastyle:off null
//scalastyle:off multiple.string.literals
@Ignore
class TransientIOErrorsRetryingIteratorITest
  extends IntegrationSpec
  with Spark
  with CosmosClient
  with AutoCleanableCosmosContainer
  with BasicLoggingTrait {

  "transient failures" should "be retried" in {
    val invocationCount = new AtomicLong(0)
    val recordCount = new AtomicLong(0)
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // assert that there is more than one range to ensure the test really is testing the parallelization of work
    container.getFeedRanges.block().size() should be > 1

    for (age <- 1 to 20) {
      for (state <- Array(true, false)) {
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", age)
        objectNode.put("isAlive", state)
        objectNode.put("id", UUID.randomUUID().toString)
        container.createItem(objectNode).block()
      }
    }

    invocationCount.set(0)
    recordCount.set(0)
    val queryOptions = new CosmosQueryRequestOptions()
    val retryingIterator = new TransientIOErrorsRetryingIterator(
      continuationToken => {
        if (!Strings.isNullOrWhiteSpace(continuationToken)) {
          ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(
            queryOptions, continuationToken, 2)
        } else {
          // scalastyle:off null
          ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(
            queryOptions, null, 2)
          // scalastyle:on null
        }
        container
          .queryItems("SELECT * FROM c", queryOptions, classOf[ObjectNode])
          .handle(r => invocationCount.set(0))
      },
      2
    )
    retryingIterator.maxRetryIntervalInMs = 5
    retryingIterator.maxRetryCount = 10

    while (retryingIterator.executeWithRetry(
      "hasNext",
      () => simulateExecutionWithTransientErrors(
        invocationCount,
        () => retryingIterator.hasNext))) {

      retryingIterator.currentIterator.next
      recordCount.incrementAndGet()
    }

    recordCount.get shouldEqual 40
  }

  "without transient failures" should "be the baseline" in {
    val invocationCount = new AtomicLong(0)
    val recordCount = new AtomicLong(0)
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // assert that there is more than one range to ensure the test really is testing the parallelization of work
    container.getFeedRanges.block().size() should be > 1

    for (age <- 1 to 20) {
      for (state <- Array(true, false)) {
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", age)
        objectNode.put("isAlive", state)
        objectNode.put("id", UUID.randomUUID().toString)
        container.createItem(objectNode).block()
      }
    }

    val queryOptions = new CosmosQueryRequestOptions()
    val iterator = new CosmosPagedIterable[ObjectNode](
        container
          .queryItems("SELECT * FROM c", queryOptions, classOf[ObjectNode])
          .handle(r => invocationCount.set(0)),
      2).iterator()

    while (iterator.hasNext) {
      iterator.next
      recordCount.incrementAndGet()
    }

    recordCount.get shouldEqual 40
  }

  "non-transient failures" should "should be thrown after retries exceed" in {
    val invocationCount = new AtomicLong(0)
    val recordCount = new AtomicLong(0)
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // assert that there is more than one range to ensure the test really is testing the parallelization of work
    container.getFeedRanges.block().size() should be > 1

    for (age <- 1 to 20) {
      for (state <- Array(true, false)) {
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", age)
        objectNode.put("isAlive", state)
        objectNode.put("id", UUID.randomUUID().toString)
        container.createItem(objectNode).block()
      }
    }

    invocationCount.set(0)
    recordCount.set(0)
    val queryOptions = new CosmosQueryRequestOptions()
    val retryingIterator = new TransientIOErrorsRetryingIterator(
      continuationToken => {
        if (!Strings.isNullOrWhiteSpace(continuationToken)) {
          ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(
            queryOptions, continuationToken, 2)
        } else {
          // scalastyle:off null
          ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(
            queryOptions, null, 2)
          // scalastyle:on null
        }
        container
          .queryItems("SELECT * FROM c", queryOptions, classOf[ObjectNode])
          .handle(r => invocationCount.set(0))
      },
      2
    )
    retryingIterator.maxRetryIntervalInMs = 5
    retryingIterator.maxRetryCount = 10

    assertThrows[ServiceUnavailableException]({
      while (retryingIterator.executeWithRetry(
        "hasNext",
        () => simulateExecutionWithNonTransientErrors(
          invocationCount,
          recordCount,
          () => retryingIterator.hasNext))) {

        retryingIterator.currentIterator.next
        recordCount.incrementAndGet()
      }
    })
  }

  // first 2 invocation succeed (draining one page) - then we inject 3 transient errors
  // before subsequent request retrieving new page succeeds
  private def simulateExecutionWithTransientErrors[T](invocationCount: AtomicLong, func: () => T): T = {
    val invocationCountSnapshot = invocationCount.incrementAndGet()
    if (invocationCountSnapshot != 3) {
      func()
    } else {
      //scalastyle:off null
      throw new ServiceUnavailableException("Dummy 503", null, null)
      //scalastyle:on null
    }
  }

  // first 2 invocation succeed (draining one page) - then we inject 15 transient failures
  // trying to retrieve the next page - which exceeds the max allowed number
  // of retries (10) - so will be treated as non-transient and not further retried. Exception
  // bubbles up
  private def simulateExecutionWithNonTransientErrors[T](
                                                          invocationCount: AtomicLong,
                                                          recordCount: AtomicLong,
                                                          func: () => T): T = {
    val invocationCountSnapshot = invocationCount.incrementAndGet()
    if (recordCount.get < 2 || invocationCountSnapshot > 17) {
      func()
    } else {
      //scalastyle:off null
      throw new ServiceUnavailableException("Dummy 503", null, null)
      //scalastyle:on null
    }
  }
}
//scalastyle:on magic.number
//scalastyle:on null
//scalastyle:on multiple.string.literals
