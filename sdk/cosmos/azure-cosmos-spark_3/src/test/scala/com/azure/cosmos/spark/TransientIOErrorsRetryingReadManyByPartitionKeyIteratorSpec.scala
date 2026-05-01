// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.SparkRowItem
import com.azure.cosmos.models.{FeedResponse, ModelBridgeInternal}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.UtilBridgeInternal
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import reactor.core.publisher.Flux

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

//scalastyle:off magic.number
//scalastyle:off multiple.string.literals
class TransientIOErrorsRetryingReadManyByPartitionKeyIteratorSpec extends UnitSpec with BasicLoggingTrait {

  private val rnd = scala.util.Random
  private val pageSize = 2
  private val cosmosSerializationConfig = CosmosSerializationConfig(
    SerializationInclusionModes.Always,
    SerializationDateTimeConversionModes.Default
  )

  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)
  private val objectMapper = new ObjectMapper

  "TransientIOErrors" should "be retried without duplicates or missing records" in {

    val pageCount = 30
    val transientErrorCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
      continuationToken => generateMockedCosmosPagedFlux(
        continuationToken, pageCount, 0.2, transientErrorCount, injectEmptyPages = false),
      pageSize,
      1,
      None,
      classOf[SparkRowItem]
    )
    iterator.maxRetryIntervalInMs = 5

    val items = drainAll(iterator)
    items.size shouldEqual (pageCount * pageSize)

    transientErrorCount.get > 0 shouldEqual true
    assertNoDuplicates(items)
  }

  "TransientIOErrors" should "be retried without duplicates when empty pages exist" in {

    val pageCount = 30
    val transientErrorCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
      continuationToken => generateMockedCosmosPagedFlux(
        continuationToken, pageCount, 0.2, transientErrorCount, injectEmptyPages = true),
      pageSize,
      1,
      None,
      classOf[SparkRowItem]
    )
    iterator.maxRetryIntervalInMs = 5

    // Pages 2,4,6,8,10,12,14,16,18,20 are empty (10 empty pages out of first 20)
    val items = drainAll(iterator)
    items.size shouldEqual ((pageCount - 10) * pageSize)

    transientErrorCount.get > 0 shouldEqual true
    assertNoDuplicates(items)
  }

  "Continuation token replay" should "not re-yield already consumed items after transient error" in {

    // Inject a transient error at a deterministic point (after page 3) so we can assert
    // that pages 1-3's items are not duplicated after the retry.
    val pageCount = 10
    val errorInjectedAfterPage = 3
    val errorInjected = new AtomicLong(0)
    val factoryCallCount = new AtomicLong(0)

    val iterator = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
      continuationToken => {
        factoryCallCount.incrementAndGet()
        generateMockedCosmosPagedFluxWithDeterministicError(
          continuationToken, pageCount, errorInjectedAfterPage, errorInjected)
      },
      pageSize,
      1,
      None,
      classOf[SparkRowItem]
    )
    iterator.maxRetryIntervalInMs = 5

    val items = drainAll(iterator)
    items.size shouldEqual (pageCount * pageSize)

    errorInjected.get shouldEqual 1
    factoryCallCount.get shouldEqual 2 // initial + 1 retry
    assertNoDuplicates(items)
  }

  "Continuation token" should "be passed to factory on retry" in {

    val pageCount = 10
    val errorInjectedAfterPage = 5
    val capturedContinuationTokens = new java.util.concurrent.CopyOnWriteArrayList[String]()
    val errorInjected = new AtomicLong(0)

    val iterator = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
      continuationToken => {
        capturedContinuationTokens.add(continuationToken)
        generateMockedCosmosPagedFluxWithDeterministicError(
          continuationToken, pageCount, errorInjectedAfterPage, errorInjected)
      },
      pageSize,
      1,
      None,
      classOf[SparkRowItem]
    )
    iterator.maxRetryIntervalInMs = 5

    drainAll(iterator)

    errorInjected.get shouldEqual 1

    // First call should have null continuation token (start from beginning)
    capturedContinuationTokens.get(0) shouldEqual null

    // Retry call should have non-null continuation token
    // (resume from last committed page before the error)
    capturedContinuationTokens.size shouldEqual 2
    capturedContinuationTokens.get(1) should not be null
  }

  "Non-transient errors" should "not be retried and propagate immediately" in {

    val iterator = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
      _ => {
        val flux = Flux.error[FeedResponse[SparkRowItem]](new DummyNonTransientCosmosException)
        UtilBridgeInternal.createCosmosPagedFlux(_ => flux)
      },
      pageSize,
      1,
      None,
      classOf[SparkRowItem]
    )
    iterator.maxRetryIntervalInMs = 5

    val thrown = the[CosmosException] thrownBy {
      iterator.hasNext
    }
    thrown.getStatusCode shouldEqual 404
  }

  "Iterator close" should "clear internal state without extra factory calls" in {

    val factoryCallCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
      continuationToken => {
        factoryCallCount.incrementAndGet()
        generateMockedCosmosPagedFlux(
          continuationToken, 30, 0.0, new AtomicLong(0), injectEmptyPages = false)
      },
      pageSize,
      1,
      None,
      classOf[SparkRowItem]
    )

    iterator.hasNext shouldEqual true
    iterator.next()

    factoryCallCount.get shouldEqual 1

    iterator.currentFeedResponseIterator.isDefined shouldEqual true

    iterator.close()

    iterator.currentFeedResponseIterator shouldEqual None
    iterator.currentItemIterator shouldEqual None

    factoryCallCount.get shouldEqual 1
  }

  "Iterator close" should "be safe to call multiple times" in {

    val iterator = new TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem](
      continuationToken => generateMockedCosmosPagedFlux(
        continuationToken, 30, 0.0, new AtomicLong(0), injectEmptyPages = false),
      pageSize,
      1,
      None,
      classOf[SparkRowItem]
    )

    iterator.hasNext shouldEqual true
    iterator.next()

    iterator.close()
    iterator.close()
    iterator.close()
    // No exception should be thrown
  }

  // --- helpers ---

  private def drainAll(iterator: TransientIOErrorsRetryingReadManyByPartitionKeyIterator[SparkRowItem]): List[SparkRowItem] = {
    val buffer = scala.collection.mutable.ListBuffer[SparkRowItem]()
    while (iterator.hasNext) {
      buffer += iterator.next()
    }
    buffer.toList
  }

  private def assertNoDuplicates(items: List[SparkRowItem]): Unit = {
    val ids = items.map(item => item.row.getString(0)) // "id" field
    ids.size shouldEqual ids.distinct.size
  }

  @throws[JsonProcessingException]
  private def getDocumentDefinition(documentId: String) = {
    val json = s"""{"id":"$documentId"}"""
    val node = objectMapper.readValue(json, classOf[ObjectNode])
    SparkRowItem(
      cosmosRowConverter.fromObjectNodeToRow(
        ItemsTable.defaultSchemaForInferenceDisabled,
        node,
        SchemaConversionModes.Strict
      ),
      None)
  }

  private def generateMockedCosmosPagedFlux(
    continuationToken: String,
    pageCount: Int,
    errorRate: Double,
    transientErrorCounter: AtomicLong,
    injectEmptyPages: Boolean
  ) = {

    require(pageCount > 20)

    val flux = generateFeedResponseFlux(
      "Batch", pageCount, errorRate,
      Option(continuationToken), transientErrorCounter, injectEmptyPages)

    UtilBridgeInternal.createCosmosPagedFlux(_ => flux)
  }

  private def generateMockedCosmosPagedFluxWithDeterministicError(
    continuationToken: String,
    pageCount: Int,
    errorAfterPage: Int,
    errorInjected: AtomicLong
  ) = {

    val flux = generateFeedResponseFluxWithDeterministicError(
      "Batch", pageCount, errorAfterPage,
      Option(continuationToken), errorInjected)

    UtilBridgeInternal.createCosmosPagedFlux(_ => flux)
  }

  private def generateFeedResponseFlux(
    prefix: String,
    pageCount: Int,
    errorRate: Double,
    requestContinuationToken: Option[String],
    transientErrorCounter: AtomicLong,
    injectEmptyPages: Boolean
  ): Flux[FeedResponse[SparkRowItem]] = {

    val responses = Array.range(1, pageCount + 1)
      .map(i => generateFeedResponse(
        prefix, i,
        if (injectEmptyPages && i > 1 && i <= 20 && i % 2 == 0) -1 else 1))
      .filter(response => requestContinuationToken.isEmpty ||
        requestContinuationToken.get == null ||
        requestContinuationToken.get < response.getContinuationToken)

    Flux
      .fromArray(responses)
      .map(response => if (errorRate > 0 && rnd.nextDouble() < errorRate) {
        transientErrorCounter.incrementAndGet()
        throw new DummyTransientCosmosException
      } else {
        response
      })
  }

  private def generateFeedResponseFluxWithDeterministicError(
    prefix: String,
    pageCount: Int,
    errorAfterPage: Int,
    requestContinuationToken: Option[String],
    errorInjected: AtomicLong
  ): Flux[FeedResponse[SparkRowItem]] = {

    val responses = Array.range(1, pageCount + 1)
      .map(i => generateFeedResponse(prefix, i, 1))
      .filter(response => requestContinuationToken.isEmpty ||
        requestContinuationToken.get == null ||
        requestContinuationToken.get < response.getContinuationToken)

    Flux
      .fromArray(responses)
      .map(response => {
        // Extract page sequence number from continuation token to inject error deterministically
        val token = response.getContinuationToken
        val pageNum = token.split("_Page")(1).split("_")(0).toInt
        if (pageNum == errorAfterPage + 1 && errorInjected.compareAndSet(0, 1)) {
          throw new DummyTransientCosmosException
        } else {
          response
        }
      })
  }

  private def generateFeedResponse(
    prefix: String,
    pageSequenceNumber: Int,
    documentStartIndex: Int
  ): FeedResponse[SparkRowItem] = {

    val continuationToken = f"$prefix%s_Page$pageSequenceNumber%05d_ContinuationToken"
    val items = if (documentStartIndex < 0) {
      Array.empty[SparkRowItem]
    } else {
      val id1 = f"$prefix%s_Page$pageSequenceNumber%05d_$documentStartIndex%05d"
      val id2 = f"$prefix%s_Page$pageSequenceNumber%05d_${documentStartIndex + 1}%05d"
      Array[SparkRowItem](getDocumentDefinition(id1), getDocumentDefinition(id2))
    }

    val r = ModelBridgeInternal.createFeedResponse(
      items.toList.asJava,
      new ConcurrentHashMap[String, String]
    )
    ModelBridgeInternal.setFeedResponseContinuationToken(continuationToken, r)
    r
  }

  private class DummyTransientCosmosException
    extends CosmosException(500, "Dummy Internal Server Error")

  private class DummyNonTransientCosmosException
    extends CosmosException(404, "Dummy Not Found Error")
}
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals
