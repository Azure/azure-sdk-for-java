// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosItemSerializerNoExceptionWrapping}
import com.azure.cosmos.implementation.{HttpConstants, ObjectNodeMap, ServiceUnavailableException, SparkRowItem, Strings, Utils}
import com.azure.cosmos.models.{CosmosQueryRequestOptions, ModelBridgeInternal}
import com.azure.cosmos.spark.TransientIOErrorsRetryingIteratorITest.maxRetryCountPerIOOperation
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.CosmosPagedIterable
import com.fasterxml.jackson.databind.node.ObjectNode
import reactor.util.concurrent.Queues

import java.util
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
        objectNode.put("name", "Schrodinger's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", age)
        objectNode.put("isAlive", state)
        objectNode.put("id", id)
        container.createItem(objectNode).block()
        logInfo(s"ID of test doc: $id")
      }
    }

    val cosmosSerializationConfig = CosmosSerializationConfig(
        SerializationInclusionModes.Always,
        SerializationDateTimeConversionModes.Default
      )

    val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)
    val queryOptions = new CosmosQueryRequestOptions()
      .setCustomItemSerializer(
        new CosmosItemSerializerNoExceptionWrapping {
          override def serialize[T](item: T): util.Map[String, AnyRef] = ???

          override def deserialize[T](jsonNodeMap: util.Map[String, AnyRef], classType: Class[T]): T = {
            if (jsonNodeMap == null) {
              throw new IllegalStateException("The 'jsonNodeMap' should never be null here.")
            }

            if (classType != classOf[SparkRowItem]) {
              throw new IllegalStateException("The 'classType' must be 'classOf[SparkRowItem])' here.")
            }

            val objectNode: ObjectNode = jsonNodeMap match {
              case map: ObjectNodeMap =>
                map.getObjectNode
              case _ =>
                Utils.getSimpleObjectMapper.convertValue(jsonNodeMap, classOf[ObjectNode])
            }

            val row = cosmosRowConverter.fromObjectNodeToRow(
              ItemsTable.defaultSchemaForInferenceDisabled,
              objectNode,
              SchemaConversionModes.Strict)

            SparkRowItem(row, None).asInstanceOf[T]
          }
        }
      )

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
          .queryItems("SELECT * FROM c", queryOptions, classOf[SparkRowItem])
          .handle(r => {
            val lastId = if (r.getResults.size() > 0) {
              val row = r.getResults.get(r.getResults.size() - 1).row
              row.get(row.fieldIndex("id")).asInstanceOf[String]
            } else {
              ""
            }
            logInfo(s"Last ID of page: $lastId")
            lastIdOfPage.set(lastId)
          })
      },
      2,
      Queues.XS_BUFFER_SIZE,
      None
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

      val node = retryingIterator.next()
      val row = node.row
      val idRetrieved = row.get(row.fieldIndex("id")).asInstanceOf[String]
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
        objectNode.put("name", "Schrodinger's cat")
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
      2,
      1
    ).iterator()

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
        objectNode.put("name", "Schrodinger's cat")
        objectNode.put("type", "cat")
        objectNode.put("age", age)
        objectNode.put("isAlive", state)
        objectNode.put("id", UUID.randomUUID().toString)
        container.createItem(objectNode).block()
      }
    }

    val cosmosSerializationConfig = CosmosSerializationConfig(
      SerializationInclusionModes.Always,
      SerializationDateTimeConversionModes.Default
    )
    val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)
    val queryOptions = new CosmosQueryRequestOptions()
      .setCustomItemSerializer(
        new CosmosItemSerializerNoExceptionWrapping {
          override def serialize[T](item: T): util.Map[String, AnyRef] = ???

          override def deserialize[T](jsonNodeMap: util.Map[String, AnyRef], classType: Class[T]): T = {
            if (jsonNodeMap == null) {
              throw new IllegalStateException("The 'jsonNodeMap' should never be null here.")
            }

            if (classType != classOf[SparkRowItem]) {
              throw new IllegalStateException("The 'classType' must be 'classOf[SparkRowItem])' here.")
            }

            val objectNode: ObjectNode = jsonNodeMap match {
              case map: ObjectNodeMap =>
                map.getObjectNode
              case _ =>
                Utils.getSimpleObjectMapper.convertValue(jsonNodeMap, classOf[ObjectNode])
            }

            val row = cosmosRowConverter.fromObjectNodeToRow(
              ItemsTable.defaultSchemaForInferenceDisabled,
              objectNode,
              SchemaConversionModes.Strict)

            SparkRowItem(row, None).asInstanceOf[T]
          }
        }
      )

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
          .queryItems("SELECT * FROM c", queryOptions, classOf[SparkRowItem])
          .handle(r => {
            if (r.getResults.size() > 0) {
              val row = r.getResults.get(r.getResults.size() - 1).row
              row.get(row.fieldIndex("id")).asInstanceOf[String]
            } else {
              lastIdOfPage.set("")
            }
          })
      },
      2,
      Queues.XS_BUFFER_SIZE,
      None
    )
    retryingIterator.maxRetryIntervalInMs = 5
    retryingIterator.maxRetryCount = maxRetryCountPerIOOperation
    val idsWithRetries = new ConcurrentHashMap[String, Long]()

    assertThrows[ServiceUnavailableException]({
    while (retryingIterator.executeWithRetry(
      "hasNext",
      () => simulateExecutionWithNonTransientErrors(
        lastIdRetrieved,
        idsWithRetries,
        () => retryingIterator.hasNext))) {

      val node = retryingIterator.next()
      val row = node.row
      val idRetrieved = row.get(row.fieldIndex("id")).asInstanceOf[String]
      logInfo(s"Last ID retrieved: $idRetrieved")
      lastIdRetrieved.set(idRetrieved)
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
    // last document of one page (and before retrieving the next one)
    if (!idSnapshot.equals("") &&
      idSnapshot.equals(lastIdOfPage.get()) &&
        idsWithRetries.computeIfAbsent(idSnapshot, _ => 0) < maxRetryCountPerIOOperation &&
        idsWithRetries.computeIfPresent(
          idSnapshot, (_, currentRetryCount) => currentRetryCount + 1) < maxRetryCountPerIOOperation) {

      //scalastyle:off null
      throw new ServiceUnavailableException("Dummy 503", null, null, HttpConstants.SubStatusCodes.UNKNOWN)
      //scalastyle:on null
    } else {
      func()
    }
  }

  private def simulateExecutionWithNonTransientErrors[T]
  (
    lastIdRetrieved: AtomicReference[String],
    idsWithRetries: ConcurrentHashMap[String, Long],
    func: () => T): T = {

    val idSnapshot = lastIdRetrieved.get

    // transient I/O errors can only happen in reality between
    // pages - and the retry logic depends on this assertion
    // so the test here will only ever inject an error after retrieving the
    // last document of one page (and before retrieving the next one)
    if (
      idsWithRetries.computeIfAbsent(idSnapshot, _ => 0) <= maxRetryCountPerIOOperation * 100 &&
      idsWithRetries.computeIfPresent(
        idSnapshot, (_, currentRetryCount) => currentRetryCount + 1) <= maxRetryCountPerIOOperation * 100) {

      //scalastyle:off null
      throw new ServiceUnavailableException("Dummy 503", null, null, HttpConstants.SubStatusCodes.UNKNOWN)
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
