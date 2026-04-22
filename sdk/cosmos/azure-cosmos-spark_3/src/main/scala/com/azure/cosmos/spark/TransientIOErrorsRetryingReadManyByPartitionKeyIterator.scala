// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.OperationCancelledException
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.models.FeedResponse
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.{CosmosPagedFlux, CosmosPagedIterable}

import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random
import scala.util.control.Breaks

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

  private val rnd = Random
  // scalastyle:off null
  private val lastContinuationToken = new AtomicReference[String](null)
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
        // INVARIANT: it is safe to record the continuation token BEFORE the items in this
        // FeedResponse have been drained because executeWithRetry only wraps `hasNext`,
        // and the buffered iterator is always fully drained before the next page is fetched.
        // If a transient failure occurs while draining items from the *current* page, the
        // page is replayed from this continuation token, which is the start of *this* page
        // (the server's continuation token points at the next page), so on retry the SDK
        // re-issues the request that produced the page we were processing. Items already
        // emitted to the caller may be re-emitted; readManyByPartitionKeys is idempotent
        // and returns documents (not deltas), so duplicates from replay are acceptable.
        lastContinuationToken.set(feedResponse.getContinuationToken)

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
    val loop = new Breaks()
    var returnValue: Option[T] = None

    loop.breakable {
      while (true) {
        val retryIntervalInMs = rnd.nextInt(maxRetryIntervalInMs)

        try {
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
                  s"Too many transient failure retry attempts in " +
                    s"TransientIOErrorsRetryingReadManyByPartitionKeyIterator.$methodName",
                  cosmosException)
                throw cosmosException
              } else {
                logWarning(
                  s"Transient failure handled in " +
                    s"TransientIOErrorsRetryingReadManyByPartitionKeyIterator.$methodName -" +
                    s" will be retried (attempt#$retryCountSnapshot) in ${retryIntervalInMs}ms " +
                    s"(continuationToken=$lastContinuationToken)",
                  cosmosException)
              }
            } else {
              throw cosmosException
            }
          case other: Throwable => throw other
        }

        currentItemIterator = None
        currentFeedResponseIterator = None
        Thread.sleep(retryIntervalInMs)
      }
    }

    returnValue.get
  }

  override def close(): Unit = {
    currentItemIterator = None
    currentFeedResponseIterator = None
  }
}
