// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.{ChangeFeedSparkRowItem, OperationCancelledException, SparkRowItem}
import com.azure.cosmos.models.{FeedResponse, ModelBridgeInternal}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.UtilBridgeInternal
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.Row
import reactor.core.publisher.Flux

import java.time.Duration
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

//scalastyle:off magic.number
//scalastyle:off multiple.string.literals
class TransientIOErrorsRetryingIteratorSpec extends UnitSpec with BasicLoggingTrait {

  private val injectedDelays = new AtomicLong(0)
  private val rnd = scala.util.Random
  private val pageSize = 2
  private val cosmosSerializationConfig = CosmosSerializationConfig(
    SerializationInclusionModes.Always,
    SerializationDateTimeConversionModes.Default
  )

  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)

  "TransientIOErrors" should "be retried without duplicates or missing records" in {

    val pageCount = 100
    val producerCount = 2
    val transientErrorCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingIterator(
      continuationToken =>generateMockedCosmosPagedFlux(
        continuationToken, pageCount, transientErrorCount, injectEmptyPages = false, injectedDelayOfFirstPage = None),
      pageSize,
      1,
      None,
      None
    )
    iterator.maxRetryIntervalInMs = 5

    iterator.count(_ => true) shouldEqual  (pageCount * pageSize * producerCount)

    transientErrorCount.get > 0 shouldEqual true
  }

  "TransientIOErrors" should "be retried without duplicates or missing records when empty pages exist" in {

    val pageCount = 100
    val producerCount = 2
    val transientErrorCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingIterator(
      continuationToken =>generateMockedCosmosPagedFlux(
        continuationToken, pageCount, transientErrorCount, injectEmptyPages = true, injectedDelayOfFirstPage = None),
      pageSize,
      1,
      None,
      None
    )
    iterator.maxRetryIntervalInMs = 5

    iterator.count(_ => true) shouldEqual ((pageCount - 10) * pageSize * producerCount)

    transientErrorCount.get > 0 shouldEqual true
  }

  "Timeouts retrieving pages" should "be retried" in {

    val pageCount = 100
    val producerCount = 2
    val transientErrorCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingIterator(
      continuationToken => generateMockedCosmosPagedFlux(
        continuationToken, pageCount, transientErrorCount, injectEmptyPages = false, injectedDelayOfFirstPage = Some(Duration.ofSeconds(70))),
      pageSize,
      1,
      None,
      None
    )
    iterator.maxRetryIntervalInMs = 5

    iterator.count(_ => true) shouldEqual (pageCount * pageSize * producerCount)

    transientErrorCount.get > 0 shouldEqual true
  }

  "CosmosPagedFluxFactory" should "only be called once per retry when transient errors happen" in {
    val pageCount = 30
    val producerCount = 2
    val factoryCallCount = new AtomicLong(0)
    val transientErrorCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingIterator(
      continuationToken => {
        factoryCallCount.incrementAndGet()
        generateMockedCosmosPagedFlux(
          continuationToken, pageCount, transientErrorCount, injectEmptyPages = false, injectedDelayOfFirstPage = None)
      },
      pageSize,
      1,
      None,
      None
    )
    iterator.maxRetryIntervalInMs = 5

    iterator.count(_ => true) shouldEqual (pageCount * pageSize * producerCount)

    transientErrorCount.get > 0 shouldEqual true

    // Each factory call should correspond to the initial call plus one per retry.
    // The factory call count should equal 1 (initial) + number of transient errors (retries).
    // The key assertion: no extra subscription beyond what is needed.
    factoryCallCount.get shouldEqual (1 + transientErrorCount.get)
  }

  "Iterator close" should "clear iterator references without creating extra subscriptions" in {
    val pageCount = 30
    val factoryCallCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingIterator(
      continuationToken => {
        factoryCallCount.incrementAndGet()
        generateMockedCosmosPagedFluxWithoutErrors(continuationToken, pageCount)
      },
      pageSize,
      1,
      None,
      None
    )

    // Read some records to initialize the iterator
    iterator.hasNext shouldEqual true
    iterator.next()

    factoryCallCount.get shouldEqual 1

    // Verify internal state is populated before close
    iterator.currentFeedResponseIterator.isDefined shouldEqual true

    // Close should clear references without calling the factory again
    iterator.close()

    iterator.currentFeedResponseIterator shouldEqual None
    iterator.currentItemIterator shouldEqual None

    // Factory should not have been called again by close
    factoryCallCount.get shouldEqual 1
  }

  "Iterator close" should "be safe to call multiple times" in {
    val factoryCallCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingIterator(
      continuationToken => {
        factoryCallCount.incrementAndGet()
        generateMockedCosmosPagedFluxWithoutErrors(continuationToken, 30)
      },
      pageSize,
      1,
      None,
      None
    )

    // Read some records
    iterator.hasNext shouldEqual true
    iterator.next()

    // Close multiple times - should not throw or create extra subscriptions
    iterator.close()
    iterator.close()
    iterator.close()

    factoryCallCount.get shouldEqual 1
  }

  "Bounded change feed reads" should
    "not complete when the feed ends before the planned end LSN" in {

    val endLsn = 20L
    val lastReturnedLsn = 15L

    val response = generateFeedResponse("ChangeFeed", 1, -1)
    ModelBridgeInternal.setFeedResponseContinuationToken(
      changeFeedContinuation(lastReturnedLsn),
      response
    )

    val iterator = new TransientIOErrorsRetryingIterator(
      _ => UtilBridgeInternal.createCosmosPagedFlux(
        _ => Flux.fromArray(Array(response))
      ),
      pageSize,
      1,
      None,
      Some(endLsn)
    )
    iterator.maxRetryCount = 0

    intercept[OperationCancelledException](iterator.hasNext)
  }

  "Bounded change feed reads" should
    "complete cleanly when the final continuation reaches the planned end LSN" in {

    // Validation matrix row #2: startLsn=10, endLsn=20, single-range continuation=20 -> complete.
    val endLsn = 20L
    val lastReturnedLsn = 20L

    val response = generateFeedResponse("ChangeFeed", 1, -1)
    ModelBridgeInternal.setFeedResponseContinuationToken(
      changeFeedContinuation(lastReturnedLsn),
      response
    )

    val iterator = new TransientIOErrorsRetryingIterator(
      _ => UtilBridgeInternal.createCosmosPagedFlux(
        _ => Flux.fromArray(Array(response))
      ),
      pageSize,
      1,
      None,
      Some(endLsn),
      Some(10L)
    )
    iterator.maxRetryCount = 0

    iterator.hasNext shouldEqual false
  }

  "Bounded change feed reads" should
    "complete cleanly when no page is consumed and startLsn already equals endLsn" in {

    // Validation matrix row #3: startLsn=20, endLsn=20, empty flux -> complete.
    val endLsn = 20L

    val iterator = new TransientIOErrorsRetryingIterator[SparkRowItem](
      _ => UtilBridgeInternal.createCosmosPagedFlux(_ => Flux.empty()),
      pageSize,
      1,
      None,
      Some(endLsn),
      Some(endLsn)
    )
    iterator.maxRetryCount = 0

    iterator.hasNext shouldEqual false
  }

  "Bounded change feed reads" should
    "throw when no page is consumed and startLsn is below endLsn" in {

    // Validation matrix row #4: startLsn=10, endLsn=20, empty flux -> throws.
    val endLsn = 20L

    val iterator = new TransientIOErrorsRetryingIterator[SparkRowItem](
      _ => UtilBridgeInternal.createCosmosPagedFlux(_ => Flux.empty()),
      pageSize,
      1,
      None,
      Some(endLsn),
      Some(10L)
    )
    iterator.maxRetryCount = 0

    intercept[OperationCancelledException](iterator.hasNext)
  }

  "Bounded change feed reads" should
    "throw when any range in a multi-range continuation lags behind the planned end LSN" in {

    // Validation matrix row #5: continuation [20, 18] (min=18) < endLsn=20 -> throws.
    val endLsn = 20L

    val response = generateFeedResponse("ChangeFeed", 1, -1)
    ModelBridgeInternal.setFeedResponseContinuationToken(
      multiRangeChangeFeedContinuation(Seq(20L, 18L)),
      response
    )

    val iterator = new TransientIOErrorsRetryingIterator(
      _ => UtilBridgeInternal.createCosmosPagedFlux(
        _ => Flux.fromArray(Array(response))
      ),
      pageSize,
      1,
      None,
      Some(endLsn),
      Some(10L)
    )
    iterator.maxRetryCount = 0

    intercept[OperationCancelledException](iterator.hasNext)
  }

  "Unbounded change feed reads" should
    "complete cleanly at EOF without any LSN progress validation" in {

    // Validation matrix row #6: endLsn=None (unbounded), empty flux -> no throw.
    // Guards against regression of validation kicking in for batch/unbounded mode.
    val iterator = new TransientIOErrorsRetryingIterator[SparkRowItem](
      _ => UtilBridgeInternal.createCosmosPagedFlux(_ => Flux.empty()),
      pageSize,
      1,
      None,
      None,
      None
    )
    iterator.maxRetryCount = 0

    iterator.hasNext shouldEqual false
  }

  "Bounded change feed reads" should
    "retry the bounded EOF failure up to maxRetryCount before propagating it" in {

    // Validation matrix row #7: under-run EOF is treated as a transient (408) failure
    // and re-subscribes the underlying flux factory until retries are exhausted.
    val endLsn = 20L
    val lastReturnedLsn = 15L
    val maxRetryCount = 2
    val factoryCallCount = new AtomicLong(0)

    val iterator = new TransientIOErrorsRetryingIterator(
      _ => {
        factoryCallCount.incrementAndGet()
        val response = generateFeedResponse("ChangeFeed", 1, -1)
        ModelBridgeInternal.setFeedResponseContinuationToken(
          changeFeedContinuation(lastReturnedLsn),
          response
        )
        UtilBridgeInternal.createCosmosPagedFlux(
          _ => Flux.fromArray(Array(response))
        )
      },
      pageSize,
      1,
      None,
      Some(endLsn),
      Some(10L)
    )
    iterator.maxRetryCount = maxRetryCount
    iterator.maxRetryIntervalInMs = 1

    intercept[OperationCancelledException](iterator.hasNext)

    // 1 initial attempt + maxRetryCount retries
    factoryCallCount.get shouldEqual (1 + maxRetryCount)
  }

  "Bounded change feed reads" should
    "still suppress rows above endLsn while passing the EOF progress check at the boundary" in {

    // Validation matrix row #8: page contains a row with _lsn > endLsn and the
    // final continuation reaches endLsn exactly. validateNextLsn must continue to
    // suppress the over-LSN row, and validateEofProgressOrThrow must accept the
    // boundary continuation without throwing.
    val endLsn = 20L
    val rowAboveEndLsn = ChangeFeedSparkRowItem(Row.empty, None, "25")

    val response: FeedResponse[ChangeFeedSparkRowItem] = ModelBridgeInternal
      .createFeedResponse(
        java.util.Collections.singletonList(rowAboveEndLsn),
        new ConcurrentHashMap[String, String]
      )
    ModelBridgeInternal.setFeedResponseContinuationToken(
      changeFeedContinuation(endLsn),
      response
    )

    val iterator = new TransientIOErrorsRetryingIterator[ChangeFeedSparkRowItem](
      _ => UtilBridgeInternal.createCosmosPagedFlux(
        _ => Flux.fromArray(Array(response))
      ),
      pageSize,
      1,
      None,
      Some(endLsn),
      Some(10L)
    )
    iterator.maxRetryCount = 0

    iterator.hasNext shouldEqual false
  }

  private val objectMapper = new ObjectMapper

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

  private def generateMockedCosmosPagedFlux
  (
    continuationToken: String,
    initialPageCount: Int,
    transientErrorCounter: AtomicLong,
    injectEmptyPages: Boolean,
    injectedDelayOfFirstPage: Option[Duration]
  ) = {

    require(initialPageCount > 20)

    val leftProducer = generateFeedResponseFlux(
      "Left",
      initialPageCount,
      0.2,
      Option.apply(continuationToken),
      transientErrorCounter,
      injectEmptyPages,
      injectedDelayOfFirstPage)
    val rightProducer = generateFeedResponseFlux(
      "Right",
      initialPageCount,
      0.1,
      Option.apply(continuationToken),
      transientErrorCounter,
      injectEmptyPages,
      injectedDelayOfFirstPage)
    val toBeMerged = Array(leftProducer, rightProducer).toIterable.asJava
    val mergedFlux = Flux.mergeSequential(toBeMerged , 1, 2)
    UtilBridgeInternal.createCosmosPagedFlux(_ => mergedFlux)
  }

  private def generateMockedCosmosPagedFluxWithoutErrors
  (
    continuationToken: String,
    initialPageCount: Int
  ) = {

    require(initialPageCount > 20)

    val leftProducer = generateFeedResponseFlux(
      "Left",
      initialPageCount,
      0.0,
      Option.apply(continuationToken),
      new AtomicLong(0),
      false,
      None)
    val rightProducer = generateFeedResponseFlux(
      "Right",
      initialPageCount,
      0.0,
      Option.apply(continuationToken),
      new AtomicLong(0),
      false,
      None)
    val toBeMerged = Array(leftProducer, rightProducer).toIterable.asJava
    val mergedFlux = Flux.mergeSequential(toBeMerged, 1, 2)
    UtilBridgeInternal.createCosmosPagedFlux(_ => mergedFlux)
  }

  private def generateFeedResponseFlux
  (
    prefix: String,
    pageCount: Int,
    errorThreshold: Double,
    requestContinuationToken: Option[String],
    transientErrorCounter: AtomicLong,
    injectEmptyPages: Boolean,
    injectedDelayOfFirstPage: Option[Duration]
  ): Flux[FeedResponse[SparkRowItem]] = {

    val responses = Array.range(1, pageCount + 1)
      .map(i => generateFeedResponse(
        prefix,
        i,
        // if injectEmptyPages == true
        // for the first 20 pages make every second page an empty page
        if (injectEmptyPages && i > 1 && i <= 20 && i % 2 == 0) {
          -1
        } else {
          1
        }))
      .filter(response => requestContinuationToken.isEmpty ||
        requestContinuationToken.get < response.getContinuationToken)

    var flux = Flux
      .fromArray(responses)

    if (injectedDelayOfFirstPage.isDefined && injectedDelays.incrementAndGet() < 3) {
      flux = flux.delaySequence(injectedDelayOfFirstPage.get)
    }

    flux
      .map(response => if (rnd.nextDouble() < errorThreshold) {
        transientErrorCounter.incrementAndGet()
        throw new DummyTransientCosmosException
      } else {
        response
      })
  }

  private def generateFeedResponseItems
  (
    prefix: String,
    pageSequenceNumber: Int,
    documentStartIndex: Int
  ): Array[SparkRowItem] = {

    if (documentStartIndex < 0) {
      Array.empty[SparkRowItem]
    } else {
      val id1 = f"$prefix%s_Page$pageSequenceNumber%05d_$documentStartIndex%05d"
      val id2 = f"$prefix%s_Page$pageSequenceNumber%05d_${documentStartIndex + 1}%05d"

      Array[SparkRowItem](
        getDocumentDefinition(id1),
        getDocumentDefinition(id2)
      )
    }
  }
  private def generateFeedResponse
  (
    prefix: String,
    pageSequenceNumber: Int,
    documentStartIndex: Int
  ): FeedResponse[SparkRowItem] = {

    val continuationToken = f"$prefix%s_Page$pageSequenceNumber%05d_ContinuationToken"
    try {
      val r = ModelBridgeInternal
        .createFeedResponse(
          generateFeedResponseItems(prefix, pageSequenceNumber, documentStartIndex)
            .toList.asJava,
            new ConcurrentHashMap[String, String]
        )
      ModelBridgeInternal.setFeedResponseContinuationToken(continuationToken, r)
      r
    } catch {
      case e: JsonProcessingException =>
        e.printStackTrace()
        null
    }
  }

  private def changeFeedContinuation(lsn: Long): String = {
    val state =
      s"""{
         |  "V": 1,
         |  "Rid": "testContainer",
         |  "Mode": "INCREMENTAL",
         |  "StartFrom": {
         |    "Type": "BEGINNING"
         |  },
         |  "Continuation": {
         |    "V": 1,
         |    "Rid": "testContainer",
         |    "Continuation": [
         |      {
         |        "token": "$lsn",
         |        "range": {
         |          "min": "",
         |          "max": "FF"
         |        }
         |      }
         |    ],
         |    "Range": {
         |      "min": "",
         |      "max": "FF"
         |    }
         |  }
         |}""".stripMargin

    Base64.getEncoder.encodeToString(state.getBytes("UTF-8"))
  }

  private def multiRangeChangeFeedContinuation(lsns: Seq[Long]): String = {
    // Splits the [""..."FF"] range into evenly-sized adjacent sub-ranges, one per supplied LSN.
    // The boundaries are arbitrary hex strings; the iterator only inspects the per-range tokens.
    require(lsns.nonEmpty, "lsns must contain at least one value")
    val boundaries: Seq[String] = "" +:
      (1 until lsns.size).map(i => f"${(0xFF * i) / lsns.size}%02X") :+
      "FF"

    val ranges = lsns.zipWithIndex.map { case (lsn, i) =>
      s"""{
         |  "token": "$lsn",
         |  "range": {
         |    "min": "${boundaries(i)}",
         |    "max": "${boundaries(i + 1)}"
         |  }
         |}""".stripMargin
    }.mkString(",\n")

    val state =
      s"""{
         |  "V": 1,
         |  "Rid": "testContainer",
         |  "Mode": "INCREMENTAL",
         |  "StartFrom": {
         |    "Type": "BEGINNING"
         |  },
         |  "Continuation": {
         |    "V": 1,
         |    "Rid": "testContainer",
         |    "Continuation": [
         |      $ranges
         |    ],
         |    "Range": {
         |      "min": "",
         |      "max": "FF"
         |    }
         |  }
         |}""".stripMargin

    Base64.getEncoder.encodeToString(state.getBytes("UTF-8"))
  }

  private class DummyTransientCosmosException
    extends CosmosException(500, "Dummy Internal Server Error")

  private class DummyNonTransientCosmosException
    extends CosmosException(404, "Dummy Not Found Error")
}
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals
