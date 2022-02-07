// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.models.FeedResponse
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.{CosmosPagedFlux, CosmosPagedIterable}
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import java.util.concurrent.locks.ReentrantReadWriteLock
import scala.util.Random
import scala.util.control.Breaks

// This iterator exists to allow adding more extensive retries for transient
// IO errors when draining a query or change feed query
// The Java SDK has built-in retry policies - but those retry policies are
// pretty restrictive - only allowing retries for 30 seconds for example in eventual consistency mode
// The built-in SDK policies are optimized for OLTP scenarios - assuming that client machines
// are not resource/CPU exhausted etc. In Spark scenarios it is much more common
// that executors at least temporarily have pegged CPU, often queries are very IO intense,
// use large page sizes etc. The retry policy against transient IO errors needs to be more robust
// as a consequence for Spark scenarios.
// The iterator below allows retries based on the continuation token of the previously received response
// because we know that IO errors cannot happen iterating over documents of one page it is safe
// to use the continuation token to keep draining on the retry
// TODO @fabianm - we should still have a discussion whether it would be worth to allow tweaking
//  the retry policy of the SDK. But having the Spark specific retries for now to get some experience
//  can help making the right decisions if/how to expose this in the SDK
private class TransientIOErrorsRetryingIterator
(
  cosmosPagedFluxFactory: String => CosmosPagedFlux[ObjectNode],
  pageSize: Int
) extends Iterator[ObjectNode] with BasicLoggingTrait {

  private[spark] var maxRetryIntervalInMs = CosmosConstants.maxRetryIntervalForTransientFailuresInMs
  private[spark] var maxRetryCount = CosmosConstants.maxRetryCountForTransientFailures
  private val lock = new ReentrantReadWriteLock()
  private val read = lock.readLock()
  private val write = lock.writeLock()

  private val rnd = Random
  // scalastyle:off null
  private val lastContinuationToken = new AtomicReference[String](null)
  // scalastyle:on null
  private val retryCount = new AtomicLong(0)
  private var currentCosmosPagedIterable: CosmosPagedIterable[ObjectNode] = _
  private[spark] var currentIterator: java.util.Iterator[ObjectNode] = _

  reinitialize()

  override def hasNext: Boolean = {
    executeWithRetry("hasNext", () => currentIterator.hasNext)
  }

  override def next(): ObjectNode = {
    executeWithRetry("next", () => currentIterator.next)
  }

  private def reinitialize(): Unit = {
    try {
      write.lock()
      currentCosmosPagedIterable = new CosmosPagedIterable[ObjectNode](
        cosmosPagedFluxFactory.apply(lastContinuationToken.get),
        pageSize
      ).handle((r: FeedResponse[ObjectNode]) => lastContinuationToken.set(r.getContinuationToken))
      currentIterator = currentCosmosPagedIterable.iterator()
    } finally write.unlock()
  }

  private[spark] def executeWithRetry[T](methodName: String, func: () => T): T = {
    val loop = new Breaks()
    var returnValue: Option[T] = None

    loop.breakable {
      while (true) {
        val retryIntervalInMs = rnd.nextInt(maxRetryIntervalInMs)

        try {
          read.lock()
          returnValue = Some(func())
          retryCount.set(0)
          loop.break
        }
        catch {
          case cosmosException: CosmosException =>
            if (Exceptions.canBeTransientFailure(cosmosException.getStatusCode, cosmosException.getSubStatusCode)) {
              val retryCountSnapshot = retryCount.incrementAndGet()
              if (retryCountSnapshot > maxRetryCount) {
                logError(
                  s"Too many transient failure retry attempts in TransientIOErrorsRetryingIterator.$methodName",
                  cosmosException)
                throw cosmosException
              } else {
                logWarning(
                  s"Transient failure handled in TransientIOErrorsRetryingIterator.$methodName -" +
                    s" will be retried (attempt#$retryCountSnapshot) in ${retryIntervalInMs}ms",
                  cosmosException)
              }
            } else {
              throw cosmosException
            }
          case other: Throwable => throw other
        } finally read.unlock()

        reinitialize()
        Thread.sleep(retryIntervalInMs)
      }
    }

    returnValue.get
  }
}
