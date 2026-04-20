// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, CosmosException}
import com.azure.cosmos.implementation.OperationCancelledException
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.models.{CosmosReadManyRequestOptions, FeedResponse, PartitionKey, SqlQuerySpec}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.CosmosPagedIterable

import java.util.concurrent.{ExecutorService, SynchronousQueue, ThreadPoolExecutor, TimeUnit, TimeoutException}
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random
import scala.util.control.Breaks

/**
 * Retry-safe iterator for readManyByPartitionKeys. The full partition-key list is passed to the
 * SDK in a single call - the SDK is responsible for fan-out and per-physical-partition batching
 * (see Configs.getReadManyByPkMaxBatchSize()). This iterator therefore wraps a single
 * CosmosPagedIterable and, on transient I/O failures, re-creates the underlying flux and
 * skips the pages that were already emitted upstream so no row is delivered twice.
 */
private[spark] class TransientIOErrorsRetryingReadManyByPartitionKeyIterator[TSparkRow]
(
  val container: CosmosAsyncContainer,
  val partitionKeys: java.util.List[PartitionKey],
  val customQuery: Option[SqlQuerySpec],
  val queryOptions: CosmosReadManyRequestOptions,
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
  private val retryCount = new AtomicLong(0)
  private lazy val operationContextString = operationContextAndListener match {
    case Some(o) => if (o.getOperationContext != null) {
      o.getOperationContext.toString
    } else {
      "n/a"
    }
    case None => "n/a"
  }

  // Number of pages that have been fully emitted upstream. On retry, we recreate the flux
  // and skip this many pages before emitting any item, so already-delivered rows are not
  // re-emitted. A page is "committed" as soon as we surface its first item to the caller -
  // subsequent failures while still inside that page cannot be recovered from without
  // risking duplication, so we fail fast in that case.
  private var pagesCommitted: Long = 0
  // Whether the currently-buffered page has emitted at least one item. If true, we have
  // passed the point of no return for this page: any transient failure here must surface,
  // because we cannot partially-skip within a page on retry.
  private var currentPagePartiallyConsumed: Boolean = false

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
          val pagedFlux = customQuery match {
            case Some(query) =>
              container.readManyByPartitionKeys(partitionKeys, query, queryOptions, classType)
            case None =>
              container.readManyByPartitionKeys(partitionKeys, queryOptions, classType)
          }

          val rawIterator = new CosmosPagedIterable[TSparkRow](
            pagedFlux,
            pageSize,
            pagePrefetchBufferSize
          )
            .iterableByPage()
            .iterator

          // Skip pages already emitted upstream (replay-safe retry).
          var skipped: Long = 0
          while (skipped < pagesCommitted && rawIterator.hasNext) {
            rawIterator.next()
            skipped += 1
          }
          if (skipped < pagesCommitted) {
            // The server returned fewer pages than before - cannot safely replay.
            // Surface a clean error rather than silently emitting a truncated result.
            throw new IllegalStateException(
              s"readManyByPartitionKeys retry replay failed: expected to skip $pagesCommitted " +
                s"already-emitted pages but only $skipped were available. Context: $operationContextString")
          }

          // scalastyle:off underscore.import
          import scala.collection.JavaConverters._
          // scalastyle:on underscore.import
          currentFeedResponseIterator = Some(rawIterator.asScala.buffered)

          currentFeedResponseIterator.get
      }

      val hasNext: Boolean = try {
        Await.result(
          Future {
            feedResponseIterator.hasNext
          }(TransientIOErrorsRetryingReadManyByPartitionKeyIterator.executionContext),
          maxPageRetrievalTimeout)
      } catch {
        case endToEndTimeoutException: OperationCancelledException =>
          val message = s"End-to-end timeout hit when trying to retrieve the next page. " +
            s"Context: $operationContextString"
          logError(message, throwable = endToEndTimeoutException)
          throw endToEndTimeoutException

        case timeoutException: TimeoutException =>
          val message = s"Attempting to retrieve the next page timed out. " +
            s"Context: $operationContextString"
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
        // scalastyle:off underscore.import
        import scala.collection.JavaConverters._
        // scalastyle:on underscore.import
        val iteratorCandidate = feedResponse.getResults.iterator().asScala.buffered

        if (iteratorCandidate.hasNext) {
          currentItemIterator = Some(iteratorCandidate)
          currentPagePartiallyConsumed = false
          Some(true)
        } else {
          // empty page - count it as committed (no items to replay) and try again
          pagesCommitted += 1
          None
        }
      } else {
        // Flux exhausted
        currentFeedResponseIterator = None
        Some(false)
      }
    }
  }

  private def hasBufferedNext: Boolean = {
    currentItemIterator match {
      case Some(iterator) => if (iterator.hasNext) {
        true
      } else {
        // Entire page drained -> it is now committed for replay-skipping purposes.
        pagesCommitted += 1
        currentPagePartiallyConsumed = false
        currentItemIterator = None
        false
      }
      case None => false
    }
  }

  override def next(): TSparkRow = {
    executeWithRetry("next", () => {
      val value = currentItemIterator.get.next()
      currentPagePartiallyConsumed = true
      value
    })
  }

  override def head: TSparkRow = {
    executeWithRetry("head", () => currentItemIterator.get.head)
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
              if (currentPagePartiallyConsumed) {
                // We have already emitted items from the current page upstream. Replaying
                // the flux would re-skip only completed pages, not items within a page -
                // which would cause silent duplication. Fail the task instead.
                logError(
                  s"Transient failure in TransientIOErrorsRetryingReadManyByPartitionKeyIterator." +
                    s"$methodName after items from the current page were already emitted - " +
                    s"cannot safely retry without duplicating rows.",
                  cosmosException)
                throw cosmosException
              }
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
                    s"(pagesCommitted=$pagesCommitted)",
                  cosmosException)
              }
            } else {
              throw cosmosException
            }
          case other: Throwable => throw other
        }

        // Reset iterators; pagesCommitted is intentionally preserved so replay can skip them.
        currentItemIterator = None
        currentFeedResponseIterator = None
        Thread.sleep(retryIntervalInMs)
      }
    }

    returnValue.get
  }

  //  Clean up iterator references - the underlying Reactor subscription from
  //  CosmosPagedIterable.iterator will be cleaned up when the iterator is GC'd.
  //  This matches the behavior of TransientIOErrorsRetryingIterator; any still-prefetched
  //  pages are discarded with the iterator.
  override def close(): Unit = {
    currentItemIterator = None
    currentFeedResponseIterator = None
  }
}

private object TransientIOErrorsRetryingReadManyByPartitionKeyIterator extends BasicLoggingTrait {
  private val maxConcurrency = SparkUtils.getNumberOfHostCPUCores

  val executorService: ExecutorService = new ThreadPoolExecutor(
    maxConcurrency,
    maxConcurrency,
    0L,
    TimeUnit.MILLISECONDS,
    new SynchronousQueue(),
    SparkUtils.daemonThreadFactory(),
    new ThreadPoolExecutor.CallerRunsPolicy()
  )

  val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executorService)
}
