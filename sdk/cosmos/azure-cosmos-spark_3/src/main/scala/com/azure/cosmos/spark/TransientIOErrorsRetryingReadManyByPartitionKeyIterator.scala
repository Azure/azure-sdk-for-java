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

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

/**
 * Retry-safe iterator for readManyByPartitionKey that batches partition keys and lazily
 * iterates pages within each batch via CosmosPagedIterable — consistent with how
 * TransientIOErrorsRetryingIterator handles normal queries. On transient I/O errors the
 * current batch's flux is recreated and pages already consumed are replayed, avoiding
 * the memory overhead of collectList and matching the query iterator's structure.
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

  private[spark] var currentFeedResponseIterator: Option[BufferedIterator[FeedResponse[TSparkRow]]] = None
  private[spark] var currentItemIterator: Option[BufferedIterator[TSparkRow]] = None

  private val pkBatchIterator = partitionKeys.asScala.iterator.grouped(pageSize)
  // Track the current batch so we can replay it on retry
  private var currentBatch: Option[java.util.List[PartitionKey]] = None

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
          // Need a new feed response iterator — either for the current batch (on retry)
          // or for the next batch
          val batch = currentBatch match {
            case Some(b) => b // retry of current batch
            case None =>
              if (pkBatchIterator.hasNext) {
                val nextBatch = new java.util.ArrayList[PartitionKey](pkBatchIterator.next().toList.asJava)
                currentBatch = Some(nextBatch)
                nextBatch
              } else {
                return Some(false) // no more batches
              }
          }

          val pagedFlux = customQuery match {
            case Some(query) =>
              container.readManyByPartitionKey(batch, query, queryOptions, classType)
            case None =>
              container.readManyByPartitionKey(batch, queryOptions, classType)
          }

          currentFeedResponseIterator = Some(
            new CosmosPagedIterable[TSparkRow](
              pagedFlux,
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
        val iteratorCandidate = feedResponse.getResults.iterator().asScala.buffered

        if (iteratorCandidate.hasNext) {
          currentItemIterator = Some(iteratorCandidate)
          Some(true)
        } else {
          // empty page interleaved — try again
          None
        }
      } else {
        // Current batch's flux is exhausted — move to next batch
        currentBatch = None
        currentFeedResponseIterator = None
        None
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

  override def head(): TSparkRow = {
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
                    s" will be retried (attempt#$retryCountSnapshot) in ${retryIntervalInMs}ms",
                  cosmosException)
              }
            } else {
              throw cosmosException
            }
          case other: Throwable => throw other
        }

        // Reset iterators but keep currentBatch so the batch is replayed
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
