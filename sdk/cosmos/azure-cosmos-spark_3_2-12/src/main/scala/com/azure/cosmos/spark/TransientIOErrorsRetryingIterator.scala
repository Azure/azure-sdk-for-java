// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.models.FeedResponse
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.util.{CosmosPagedFlux, CosmosPagedIterable}
import reactor.core.scheduler.Schedulers

import java.util.concurrent.{ExecutorService, SynchronousQueue, ThreadPoolExecutor, TimeUnit, TimeoutException}
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import scala.util.Random
import scala.util.control.Breaks
import scala.concurrent.{Await, ExecutionContext, Future}
import com.azure.cosmos.implementation.{ChangeFeedSparkRowItem, OperationCancelledException, SparkBridgeImplementationInternal}


// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

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
private class TransientIOErrorsRetryingIterator[TSparkRow]
(
  val cosmosPagedFluxFactory: String => CosmosPagedFlux[TSparkRow],
  val pageSize: Int,
  val pagePrefetchBufferSize: Int,
  val operationContextAndListener: Option[OperationContextAndListenerTuple],
  val endLsn: Option[Long]
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
  private val lastPagedFlux = new AtomicReference[Option[CosmosPagedFlux[TSparkRow]]](None)
  override def hasNext: Boolean = {
    executeWithRetry("hasNextInternal", () => hasNextInternal)
  }

  /***
   * Checks whether more records exists - this will potentially trigger I/O operations and retries
   * @return true (more records exist), false (no more records exist), None (unknown call should be repeated)
   */
  private def hasNextInternal: Boolean = {
    var returnValue: Option[Boolean] = None

    while (returnValue.isEmpty) {
      returnValue = hasNextInternalCore
    }

    returnValue.get
  }

  /***
   * Checks whether more records exists - this will potentially trigger I/O operations and retries
   * @return true (more records exist), false (no more records exist), None (unknown call should be repeated)
   */
  private def hasNextInternalCore: Option[Boolean] = {
    if (hasBufferedNext) {
      Some(true)
    } else {
      val feedResponseIterator = currentFeedResponseIterator match {
        case Some(existing) => existing
        case None =>
          val newPagedFlux = Some(cosmosPagedFluxFactory.apply(lastContinuationToken.get))
          lastPagedFlux.getAndSet(newPagedFlux) match {
            case Some(oldPagedFlux) => {
              logInfo(s"Attempting to cancel oldPagedFlux, Context: $operationContextString")
              oldPagedFlux.cancelOn(Schedulers.boundedElastic()).onErrorComplete().subscribe().dispose()
            }
            case None =>
          }
          currentFeedResponseIterator = Some(
            new CosmosPagedIterable[TSparkRow](
              newPagedFlux.get,
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
          val message = s"End-to-end timeout hit when trying to retrieve the next page. Continuation" +
            s"token: $lastContinuationToken, Context: $operationContextString"

          logError(message, throwable = endToEndTimeoutException)

          throw endToEndTimeoutException
        case timeoutException: TimeoutException =>

          val message = s"Attempting to retrieve the next page timed out. Continuation" +
            s"token: $lastContinuationToken, Context: $operationContextString"

          logError(message, timeoutException)

          val exception = new OperationCancelledException(
            message,
            null
          );
          exception.setStackTrace(timeoutException.getStackTrace());
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
        lastContinuationToken.set(feedResponse.getContinuationToken)

        if (iteratorCandidate.hasNext && validateNextLsn(iteratorCandidate)) {
          currentItemIterator = Some(iteratorCandidate)
          Some(true)
        } else {
          // empty page interleaved
          // need to get attempt to get next FeedResponse to determine whether more records exist
          None
        }
      } else {
        Some(false)
      }
    }
  }

  private def hasBufferedNext: Boolean = {
    currentItemIterator match {
      case Some(iterator) => if (iterator.hasNext && validateNextLsn(iterator)) {
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
        }

        currentItemIterator = None
        currentFeedResponseIterator = None
        Thread.sleep(retryIntervalInMs)
      }
    }

    returnValue.get
  }

  private[this] def validateNextLsn(itemIterator: BufferedIterator[TSparkRow]): Boolean = {
    this.endLsn match {
      case None =>
        // Only relevant in change feed
        // In batch mode endLsn is cleared - we will always continue reading until the change feed is
        // completely drained so all partitions return 304
        true
      case Some(endLsn) =>
        // In streaming mode we only continue until we hit the endOffset's continuation Lsn
        if (itemIterator.isEmpty) {
          return false
        }
        val node = itemIterator.head.asInstanceOf[ChangeFeedSparkRowItem]
        assert(node.lsn != null, "Change feed responses must have _lsn property.")
        assert(node.lsn != "", "Change feed responses must have non empty _lsn.")
        val nextLsn = SparkBridgeImplementationInternal.toLsn(node.lsn)

        nextLsn <= endLsn
    }
  }

  //  Correct way to cancel a flux and dispose it
  //  https://github.com/reactor/reactor-core/blob/main/reactor-core/src/test/java/reactor/core/publisher/scenarios/FluxTests.java#L837
  override def close(): Unit = {
    lastPagedFlux.getAndSet(None) match {
      case Some(oldPagedFlux) => oldPagedFlux.cancelOn(Schedulers.boundedElastic()).onErrorComplete().subscribe().dispose()
      case None =>
    }
  }
}

private object TransientIOErrorsRetryingIterator extends BasicLoggingTrait {
  private val maxConcurrency = SparkUtils.getNumberOfHostCPUCores

  val executorService: ExecutorService = new ThreadPoolExecutor(
    maxConcurrency,
    maxConcurrency,
    0L,
    TimeUnit.MILLISECONDS,
    // A synchronous queue does not have any internal capacity, not even a capacity of one.
    new SynchronousQueue(),
    SparkUtils.daemonThreadFactory(),
    // if all worker threads are busy,
    // this policy makes the caller thread execute the task.
    // This provides a simple feedback control mechanism that will slow down the rate that new tasks are submitted.
    new ThreadPoolExecutor.CallerRunsPolicy()
  )

  val executionContext = ExecutionContext.fromExecutorService(executorService)
}
