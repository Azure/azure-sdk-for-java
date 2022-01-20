// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{ServiceUnavailableException, Strings, Utils}
import com.azure.cosmos.models.{CosmosQueryRequestOptions, ModelBridgeInternal}
import com.azure.cosmos.spark.TransientIOErrorsRetryingIteratorITest.maxRetryCountPerIOOperation
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.CosmosPagedIterable
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

//scalastyle:off magic.number
//scalastyle:off null
//scalastyle:off multiple.string.literals
class TransientIOErrorsRetryingIteratorITest
  extends IntegrationSpec
  with Spark
  with CosmosClient
  with AutoCleanableCosmosContainer
  with BasicLoggingTrait {

  "transient failures" should "be retried" in {
    val lastIdOfPage = new AtomicReference[String]("")
    val lastIdRetrieved = new AtomicReference[String]("")
    val recordCount = new AtomicLong(0)
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

    // assert that there is more than one range to ensure the test really is testing the parallelization of work
    container.getFeedRanges.block().size() should be > 1

    for (age <- 1 to 20) {
      for (state <- Array(true, false)) {
        val id = UUID.randomUUID().toString
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", age)
        objectNode.put("isAlive", state)
        objectNode.put("id", id)
        container.createItem(objectNode).block()
        logInfo(s"ID of test doc: $id")
      }
    }

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
          .handle(r => {
            val lastId = if (r.getResults.size() > 0) {
              r.getResults.get(r.getResults.size() - 1).get("id").asText()
            } else {
              ""
            }
            logInfo(s"Last ID of page: $lastId")
            lastIdOfPage.set(lastId)
          })
      },
      2
    )
    retryingIterator.maxRetryIntervalInMs = 5
    retryingIterator.maxRetryCount = maxRetryCountPerIOOperation
    val idsWithRetries = new ConcurrentHashMap[String, Long]()

    while (retryingIterator.executeWithRetry(
      "hasNext",
      () => simulateExecutionWithTransientErrors(
        lastIdOfPage,
        lastIdRetrieved,
        idsWithRetries,
        () => retryingIterator.hasNext))) {

      val node = retryingIterator.currentIterator.next
      val idRetrieved = node.get("id").asText()
      logInfo(s"Last ID retrieved: $idRetrieved")
      lastIdRetrieved.set(idRetrieved)
      recordCount.incrementAndGet()
    }

    recordCount.get shouldEqual 40
  }

  "without transient failures" should "be the baseline" in {
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
          .queryItems("SELECT * FROM c", queryOptions, classOf[ObjectNode]),
      2).iterator()

    while (iterator.hasNext) {
      iterator.next
      recordCount.incrementAndGet()
    }

    recordCount.get shouldEqual 40
  }

  "non-transient failures" should "should be thrown after retries exceed" in {
    val lastIdOfPage = new AtomicReference[String]("")
    val lastIdRetrieved = new AtomicReference[String]("")
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
          .handle(r => {
            if (r.getResults.size() > 0) {
              lastIdOfPage.set(r.getResults.get(r.getResults.size() - 1).get("id").asText())
            } else {
              lastIdOfPage.set("")
            }
          })
      },
      2
    )
    retryingIterator.maxRetryIntervalInMs = 5
    retryingIterator.maxRetryCount = maxRetryCountPerIOOperation
    val idsWithRetries = new ConcurrentHashMap[String, Long]()

    assertThrows[ServiceUnavailableException]({
    while (retryingIterator.executeWithRetry(
      "hasNext",
      () => simulateExecutionWithNonTransientErrors(
        lastIdOfPage,
        lastIdRetrieved,
        idsWithRetries,
        () => retryingIterator.hasNext))) {

      val node = retryingIterator.currentIterator.next
      lastIdRetrieved.set(node.get("id").asText())
      recordCount.incrementAndGet()
    }
    })
  }

  // first 2 invocation succeed (draining one page) - then we inject 3 transient errors
  // before subsequent request retrieving new page succeeds
  private def simulateExecutionWithTransientErrors[T]
  (
    lastIdOfPage: AtomicReference[String],
    lastIdRetrieved: AtomicReference[String],
    idsWithRetries: ConcurrentHashMap[String, Long],
    func: () => T): T = {

    val idSnapshot = lastIdRetrieved.get

    // transient I/O errors can only happen in reality between
    // pages - and the retry logic depends on this assertion
    // so the test here will only ever inject an error after retrieving the
    // last document of one page (and before retrieving teh next one)
    if (!idSnapshot.equals("") &&
      idSnapshot.equals(lastIdOfPage.get()) &&
        idsWithRetries.computeIfAbsent(idSnapshot, id => 0) < maxRetryCountPerIOOperation &&
        idsWithRetries.computeIfPresent(
          idSnapshot, (id, currentRetryCount) => currentRetryCount + 1) < maxRetryCountPerIOOperation) {

      //scalastyle:off null
      throw new ServiceUnavailableException("Dummy 503", null, null)
      //scalastyle:on null
    } else {
      func()
    }
  }

  private def simulateExecutionWithNonTransientErrors[T]
  (
    lastIdOfPage: AtomicReference[String],
    lastIdRetrieved: AtomicReference[String],
    idsWithRetries: ConcurrentHashMap[String, Long],
    func: () => T): T = {

    val idSnapshot = lastIdRetrieved.get

    // transient I/O errors can only happen in reality between
    // pages - and the retry logic depends on this assertion
    // so the test here will only ever inject an error after retrieving the
    // last document of one page (and before retrieving teh next one)
    if (
      idsWithRetries.computeIfAbsent(idSnapshot, id => 0) <= maxRetryCountPerIOOperation * 100 &&
      idsWithRetries.computeIfPresent(
        idSnapshot, (id, currentRetryCount) => currentRetryCount + 1) <= maxRetryCountPerIOOperation * 100) {

      //scalastyle:off null
      throw new ServiceUnavailableException("Dummy 503", null, null)
      //scalastyle:on null
    } else {
      func()
    }
  }
}

object TransientIOErrorsRetryingIteratorITest {
  val maxRetryCountPerIOOperation = 1
}
//scalastyle:on magic.number
//scalastyle:on null
//scalastyle:on multiple.string.literals
