// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
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
class TransientIOErrorsRetryingIteratorSpec extends UnitSpec with BasicLoggingTrait {

  val rnd = scala.util.Random

  "TransientIOErrors" should "be retried without duplicates or missing records" in {

    val transientErrorCount = new AtomicLong(0)
    val iterator = new TransientIOErrorsRetryingIterator(
      continuationToken =>generateMockedCosmosPagedFlux(continuationToken, transientErrorCount),
      2
    )
    iterator.maxRetryIntervalInMs = 5

    iterator.count(doc => true) shouldEqual 400

    transientErrorCount.get > 0 shouldEqual true
  }

  private val objectMapper = new ObjectMapper

  @throws[JsonProcessingException]
  private def getDocumentDefinition(documentId: String, pkId: String) = {
    val json = s"""{"id":"$documentId", "mypk": "$pkId"}"""
    objectMapper.readValue(json, classOf[ObjectNode])
  }

  private def generateMockedCosmosPagedFlux
  (
    continuationToken: String,
    transientErrorCounter: AtomicLong
  ) = {

    val leftProducer = generateFeedResponseFlux(
      "Left", 100, 0.2, Option.apply(continuationToken), transientErrorCounter)
    val rightProducer = generateFeedResponseFlux(
      "Right", 100, 0.1, Option.apply(continuationToken), transientErrorCounter)
    val toBeMerged = Array(leftProducer, rightProducer).toIterable.asJava
    val mergedFlux = Flux.mergeSequential(toBeMerged , 1, 2)
    UtilBridgeInternal.createCosmosPagedFlux(options => mergedFlux)
  }

  private def generateFeedResponseFlux
  (
    prefix: String,
    pageCount: Int,
    errorThreshold: Double,
    requestContinuationToken: Option[String],
    transientErrorCounter: AtomicLong
  ): Flux[FeedResponse[ObjectNode]] = {

    val responses = Array.range(1, pageCount + 1)
      .map(i => generateFeedResponse(prefix, i, 1))
      .filter(response => requestContinuationToken.isEmpty || requestContinuationToken.get < response.getContinuationToken())

    Flux
      .fromArray(responses)
      .map(response => if (rnd.nextDouble() < errorThreshold) {
        transientErrorCounter.incrementAndGet()
        throw new DummyTransientCosmosException
      } else {
        response
      })
  }

  private def generateFeedResponse
  (
    prefix: String,
    pageSequenceNumber: Int,
    documentStartIndex: Int
  ): FeedResponse[ObjectNode] = {

    val id1 = f"$prefix%s_Page$pageSequenceNumber%05d_$documentStartIndex%05d"
    val id2 = f"$prefix%s_Page$pageSequenceNumber%05d_${documentStartIndex + 1}%05d"
    val continuationToken = f"$prefix%s_Page$pageSequenceNumber%05d_ContinuationToken"
    try {
      val r = ModelBridgeInternal
        .createFeedResponse(
            Array[ObjectNode](
              getDocumentDefinition(id1, id1),
              getDocumentDefinition(id2, id2)
            ).toList.asJava,
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

  private class DummyTransientCosmosException
    extends CosmosException(500, "Dummy Internal Server Error")

  private class DummyNonTransientCosmosException
    extends CosmosException(404, "Dummy Not Found Error")
}
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals
