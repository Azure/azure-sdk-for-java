// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.SparkRowItem
import com.azure.cosmos.implementation.accesshelpers.FeedResponseHelper
import com.azure.cosmos.models.{FeedResponse, ModelBridgeInternal}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.UtilBridgeInternal
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import reactor.core.publisher.Flux
import reactor.util.concurrent.Queues

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

//scalastyle:off magic.number
//scalastyle:off multiple.string.literals
class TransientIOErrorsRetryingIteratorSpec extends UnitSpec with BasicLoggingTrait {

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
        continuationToken, pageCount, transientErrorCount, injectEmptyPages = false),
      pageSize,
      1,
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
        continuationToken, pageCount, transientErrorCount, injectEmptyPages = true),
      pageSize,
      1,
      None
    )
    iterator.maxRetryIntervalInMs = 5

    iterator.count(_ => true) shouldEqual ((pageCount - 10) * pageSize * producerCount)

    transientErrorCount.get > 0 shouldEqual true
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
        SchemaConversionModes.Strict))
  }

  private def generateMockedCosmosPagedFlux
  (
    continuationToken: String,
    initialPageCount: Int,
    transientErrorCounter: AtomicLong,
    injectEmptyPages: Boolean
  ) = {

    require(initialPageCount > 20)

    val leftProducer = generateFeedResponseFlux(
      "Left",
      initialPageCount,
      0.2,
      Option.apply(continuationToken),
      transientErrorCounter,
      injectEmptyPages)
    val rightProducer = generateFeedResponseFlux(
      "Right",
      initialPageCount,
      0.1,
      Option.apply(continuationToken),
      transientErrorCounter,
      injectEmptyPages)
    val toBeMerged = Array(leftProducer, rightProducer).toIterable.asJava
    val mergedFlux = Flux.mergeSequential(toBeMerged , 1, 2)
    UtilBridgeInternal.createCosmosPagedFlux(_ => mergedFlux)
  }

  private def generateFeedResponseFlux
  (
    prefix: String,
    pageCount: Int,
    errorThreshold: Double,
    requestContinuationToken: Option[String],
    transientErrorCounter: AtomicLong,
    injectEmptyPages: Boolean
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

    Flux
      .fromArray(responses)
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
      val r = FeedResponseHelper
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

  private class DummyTransientCosmosException
    extends CosmosException(500, "Dummy Internal Server Error")

  private class DummyNonTransientCosmosException
    extends CosmosException(404, "Dummy Not Found Error")
}
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals
