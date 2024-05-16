// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.models.{CosmosItemIdentity, CosmosReadManyRequestOptions}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] class TransientIOErrorsRetryingReadManyIterator[TSparkRow]
(
  val container: CosmosAsyncContainer,
  val readManyFilterList: Iterator[CosmosItemIdentity],
  val queryOptions: CosmosReadManyRequestOptions,
  val pageSize: Int,
  val operationContextAndListener: Option[OperationContextAndListenerTuple],
  val classType: Class[TSparkRow]
) extends BufferedIterator[TSparkRow] with BasicLoggingTrait with AutoCloseable {

  private[spark] var currentItemIterator: Option[BufferedIterator[TSparkRow]] = None
  private val readManyFilterBatchIterator = readManyFilterList.grouped(pageSize)

  override def hasNext: Boolean = {
    if (hasBufferedNext) {
      true
    } else {
      hasNextInternal
    }
  }

  /** *
   * Checks whether more records exists - this will potentially trigger I/O operations and retries
   *
   * @return true (more records exist), false (no more records exist), None (unknown call should be repeated)
   */
  private def hasNextInternal: Boolean = {
    var returnValue: Option[Boolean] = None

    while (returnValue.isEmpty) {
      if (readManyFilterBatchIterator.hasNext) {
        // fetch items for the next readMany filter batch
        val readManyFilterBatch = readManyFilterBatchIterator.next()
        returnValue =
          TransientErrorsRetryPolicy.executeWithRetry(
            () => hasNextInternalCore(readManyFilterBatch),
            statusResetFuncBetweenRetry = Some(() => { currentItemIterator = None })
          )
      } else {
        returnValue = Some(false)
      }
    }

    returnValue.get
  }

  /** *
   * Checks whether more records exists - this will potentially trigger I/O operations and retries
   *
   * @return true (more records exist), false (no more records exist), None (unknown call should be repeated)
   */
  private def hasNextInternalCore(readManyFilterList: List[CosmosItemIdentity]): Option[Boolean] = {
    val feedResponse = ImplementationBridgeHelpers
      .CosmosAsyncContainerHelper
      .getCosmosAsyncContainerAccessor
      .readMany(container, readManyFilterList.asJava, queryOptions, classType)
      .block()

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
      // empty page interleaved
      // need to get attempt to get next FeedResponse to determine whether more records exist
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
