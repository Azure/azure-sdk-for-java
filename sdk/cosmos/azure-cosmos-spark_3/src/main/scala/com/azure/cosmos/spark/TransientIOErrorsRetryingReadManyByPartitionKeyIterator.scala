// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.implementation.OperationCancelledException
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.models.{CosmosReadManyRequestOptions, PartitionKey, SqlQuerySpec}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

import java.util.concurrent.{ExecutorService, SynchronousQueue, ThreadPoolExecutor, TimeUnit, TimeoutException}
import scala.concurrent.{Await, ExecutionContext, Future}

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

/**
 * Retry-safe iterator for readManyByPartitionKey that batches partition keys and retries
 * each batch independently on transient I/O errors. This avoids the continuation-token problem
 * where TransientIOErrorsRetryingIterator would re-read all data from scratch on retry,
 * causing silent data duplication.
 */
private[spark] class TransientIOErrorsRetryingReadManyByPartitionKeyIterator[TSparkRow]
(
  val container: CosmosAsyncContainer,
  val partitionKeys: java.util.List[PartitionKey],
  val customQuery: Option[SqlQuerySpec],
  val queryOptions: CosmosReadManyRequestOptions,
  val pageSize: Int,
  val operationContextAndListener: Option[OperationContextAndListenerTuple],
  val classType: Class[TSparkRow]
) extends BufferedIterator[TSparkRow] with BasicLoggingTrait with AutoCloseable {

  private val maxPageRetrievalTimeout = scala.concurrent.duration.FiniteDuration(
    5 + CosmosConstants.readOperationEndToEndTimeoutInSeconds,
    scala.concurrent.duration.SECONDS)

  private[spark] var currentItemIterator: Option[BufferedIterator[TSparkRow]] = None
  private val pkBatchIterator = partitionKeys.asScala.iterator.grouped(pageSize)

  override def hasNext: Boolean = {
    if (hasBufferedNext) {
      true
    } else {
      hasNextInternal
    }
  }

  private def hasNextInternal: Boolean = {
    var returnValue: Option[Boolean] = None

    while (returnValue.isEmpty) {
      if (pkBatchIterator.hasNext) {
        val pkBatch = pkBatchIterator.next().toList
        returnValue =
          TransientErrorsRetryPolicy.executeWithRetry(
            () => hasNextInternalCore(pkBatch),
            statusResetFuncBetweenRetry = Some(() => { currentItemIterator = None })
          )
      } else {
        returnValue = Some(false)
      }
    }

    returnValue.get
  }

  private def hasNextInternalCore(pkBatch: List[PartitionKey]): Option[Boolean] = {
    val pkJavaList = new java.util.ArrayList[PartitionKey](pkBatch.asJava)
    val results = try {
      Await.result(
        Future {
          val flux = customQuery match {
            case Some(query) =>
              container.readManyByPartitionKey(pkJavaList, query, queryOptions, classType)
            case None =>
              container.readManyByPartitionKey(pkJavaList, queryOptions, classType)
          }

          // Collect all pages for this batch into a single list
          flux.collectList().block()
        }(TransientIOErrorsRetryingReadManyByPartitionKeyIterator.executionContext),
        maxPageRetrievalTimeout)
    } catch {
      case endToEndTimeoutException: OperationCancelledException =>
        val operationContextString = operationContextAndListener match {
          case Some(o) => if (o.getOperationContext != null) {
            o.getOperationContext.toString
          } else {
            "n/a"
          }
          case None => "n/a"
        }

        val message = s"End-to-end timeout hit when trying to retrieve readManyByPartitionKey batch. " +
          s"Batch size: ${pkBatch.size}, Context: $operationContextString"

        logError(message, throwable = endToEndTimeoutException)

        throw endToEndTimeoutException
      case timeoutException: TimeoutException =>
        val operationContextString = operationContextAndListener match {
          case Some(o) => if (o.getOperationContext != null) {
            o.getOperationContext.toString
          } else {
            "n/a"
          }
          case None => "n/a"
        }

        val message = s"Attempting to retrieve readManyByPartitionKey batch timed out. " +
          s"Batch size: ${pkBatch.size}, Context: $operationContextString"

        logError(message, timeoutException)

        val exception = new OperationCancelledException(
          message,
          null
        )
        exception.setStackTrace(timeoutException.getStackTrace)
        throw exception

      case other: Throwable => throw other
    }

    val iteratorCandidate = results.iterator().asScala.buffered

    if (iteratorCandidate.hasNext) {
      currentItemIterator = Some(iteratorCandidate)
      Some(true)
    } else {
      None
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

  override def close(): Unit = {}
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
