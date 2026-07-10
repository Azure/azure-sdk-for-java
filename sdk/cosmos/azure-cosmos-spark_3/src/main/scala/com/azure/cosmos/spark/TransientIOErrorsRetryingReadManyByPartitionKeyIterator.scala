// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.OperationCancelledException
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.models.FeedResponse
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.{CosmosPagedFlux, CosmosPagedIterable}

import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import scala.concurrent.{Await, ExecutionContext, Future}

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

/**
 * Retry-safe iterator for readManyByPartitionKeys that uses continuation-token-based
 * replay (matching the pattern of TransientIOErrorsRetryingIterator).
 *
 * On transient I/O failures the iterator re-creates the underlying CosmosPagedFlux from the
 * continuation token of the last fully-committed page. A page is "committed" only after all
 * its items have been drained by the caller; the continuation token is captured from each
 * FeedResponse and used as the resume point on retry. This avoids the correctness issues of
 * page-count-based skipping (where the server is not guaranteed to return the same page
 * boundaries across requests).
 *
 * Note: transient I/O failures can only occur during {@code hasNext} (which fetches the next
 * FeedResponse page from the network). Once a page has been fetched, iterating over its
 * in-memory items ({@code next()}) performs no I/O and cannot trigger a retry. Therefore,
 * partially-consumed pages are never replayed and the iterator provides exactly-once
 * delivery in practice.
 */
private[spark] class TransientIOErrorsRetryingReadManyByPartitionKeyIterator[TSparkRow]
(
  val cosmosPagedFluxFactory: String => CosmosPagedFlux[TSparkRow],
  val pageSize: Int,
  val pagePrefetchBufferSize: Int,
  val operationContextAndListener: Option[OperationContextAndListenerTuple],
  val classType: Class[TSparkRow]
) extends BufferedIterator[TSparkRow] with BasicLoggingTrait with AutoCloseable {

  private[spark] var maxRetryIntervalInMs = CosmosConstants.maxRetryIntervalForTransientFailuresInMs
  private[spark] var maxRetryCount = CosmosConstants.maxRetryCountForTransientFailures

  private val maxPageRetrievalTimeout = scala.concurrent.duration.FiniteDuration(
    5 + CosmosConstants.readOperationEndToEndTimeoutInSeconds,
    scala.concurrent.duration.SECONDS)

  // scalastyle:off null
  private val lastContinuationToken = new AtomicReference[String](null)
  private val pendingContinuationToken = new AtomicReference[String](null)
  // scalastyle:on null
  private val retryCount = new AtomicLong(0)
  private lazy val operationContextString = operationContextAndListener match {
    case Some(o) => if (o.getOperationContext != null) {
      o.getOperationContext.toString
    } else {
      "n/a"
    }
    case None => "n/a"
  }

  private[spark] var currentFeedResponseIterator: Option[BufferedIterator[FeedResponse[TSparkRow]]] = None
  private[spark] var currentItemIterator: Option[BufferedIterator[TSparkRow]] = None

  override def hasNext: Boolean = {
    executeWithRetry("hasNextInternal", () => hasNextInternal)
  }

  private def hasNextInternal: Boolean = {
    var returnValue: Option[Boolean] = None

    while (returnValue.isEmpty) {
      returnValue = hasNextInternalCore
    }

    returnValue.get
  }

  private def hasNextInternalCore: Option[Boolean] = {
    if (hasBufferedNext) {
      Some(true)
    } else {
      // All items from the previous page have been drained — promote the pending
      // continuation token so that any retry resumes from the NEXT page rather than
      // replaying items the caller has already consumed.
      val pending = pendingContinuationToken.getAndSet(null)
      if (pending != null) {
        lastContinuationToken.set(pending)
      }

      val feedResponseIterator = currentFeedResponseIterator match {
        case Some(existing) => existing
        case None =>
          val newPagedFlux = cosmosPagedFluxFactory.apply(lastContinuationToken.get)
          currentFeedResponseIterator = Some(
            new CosmosPagedIterable[TSparkRow](
              newPagedFlux,
              pageSize,
              pagePrefetchBufferSize
            )
              .iterableByPage()
              .iterator
              .asScala
              .buffered
          )

          currentFeedResponseIterator.get
      }

      val hasNext: Boolean = try {
        Await.result(
          Future {
            feedResponseIterator.hasNext
          }(TransientIOErrorsRetryingIterator.executionContext),
          maxPageRetrievalTimeout)
      } catch {
        case endToEndTimeoutException: OperationCancelledException =>
          val message = s"End-to-end timeout hit when trying to retrieve the next page. " +
            s"ContinuationToken: $lastContinuationToken, Context: $operationContextString"
          logError(message, throwable = endToEndTimeoutException)
          throw endToEndTimeoutException

        case timeoutException: TimeoutException =>
          val message = s"Attempting to retrieve the next page timed out. " +
            s"ContinuationToken: $lastContinuationToken, Context: $operationContextString"
          logError(message, timeoutException)
          val exception = new OperationCancelledException(message, null)
          exception.setStackTrace(timeoutException.getStackTrace)
          throw exception

        case other: Throwable => throw other
      }

      if (hasNext) {
        val feedResponse = feedResponseIterator.next()
        if (operationContextAndListener.isDefined) {
          operationContextAndListener.get.getOperationListener.feedResponseProcessedListener(
            operationContextAndListener.get.getOperationContext,
            feedResponse)
        }
        val iteratorCandidate = feedResponse.getResults.iterator().asScala.buffered
        // Store the continuation token from this page as "pending". It will only be promoted
        // to lastContinuationToken (the retry resume point) after all items in this page have
        // been drained by the caller. This ensures that on a transient failure mid-page the
        // retry resumes from the PREVIOUS page's continuation (i.e. re-fetches the current
        // page) and never skips items.
        pendingContinuationToken.set(feedResponse.getContinuationToken)

        if (iteratorCandidate.hasNext) {
          currentItemIterator = Some(iteratorCandidate)
          Some(true)
        } else {
          // empty page interleaved - attempt to get next FeedResponse
          None
        }
      } else {
        // Flux exhausted
        Some(false)
      }
    }
  }

  private def hasBufferedNext: Boolean = {
    currentItemIterator match {
      case Some(iterator) => if (iterator.hasNext) {
        true
      } else {
        currentItemIterator = None
        false
      }
      case None => false
    }
  }

  override def next(): TSparkRow = {
    currentItemIterator.get.next()
  }

  override def head: TSparkRow = {
    currentItemIterator.get.head
  }

  private[spark] def executeWithRetry[T](methodName: String, func: () => T): T = {
    TransientIOErrorsRetryingIterator.executeWithRetry(
      "TransientIOErrorsRetryingReadManyByPartitionKeyIterator",
      methodName,
      func,
      maxRetryCount,
      maxRetryIntervalInMs,
      retryCount,
      () => {
        currentItemIterator = None
        currentFeedResponseIterator = None
        pendingContinuationToken.set(null)
      })
  }
  override def close(): Unit = {
    currentItemIterator = None
    currentFeedResponseIterator = None
  }
}
