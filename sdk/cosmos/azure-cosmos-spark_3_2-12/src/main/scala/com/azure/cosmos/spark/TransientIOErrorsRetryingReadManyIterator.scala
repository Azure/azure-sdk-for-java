// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.models.{CosmosItemIdentity, CosmosQueryRequestOptions}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{CosmosAsyncContainer, CosmosException}

import java.util.concurrent.atomic.AtomicLong
import scala.collection.JavaConverters._
import scala.util.Random
import scala.util.control.Breaks

class TransientIOErrorsRetryingReadManyIterator[TSparkRow]
(
    val container: CosmosAsyncContainer,
    val readManyFilterList: List[CosmosItemIdentity],
    val queryOptions: CosmosQueryRequestOptions,
    val pageSize: Int,
    val operationContextAndListener: Option[OperationContextAndListenerTuple],
    val classType: Class[TSparkRow]
) extends BufferedIterator[TSparkRow] with BasicLoggingTrait with AutoCloseable {

    private[spark] var maxRetryIntervalInMs = CosmosConstants.maxRetryIntervalForTransientFailuresInMs
    private[spark] var maxRetryCount = CosmosConstants.maxRetryCountForTransientFailures

    private val rnd = Random
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

    private[spark] var currentItemIterator: Option[BufferedIterator[TSparkRow]] = None
    private val readManyFilterBatchIterator = readManyFilterList.grouped(pageSize)

    override def hasNext: Boolean = {
        if (hasBufferedNext) {
            true
        } else {
            hasNextInternal
        }
    }

    /***
     * Checks whether more records exists - this will potentially trigger I/O operations and retries
     * @return true (more records exist), false (no more records exist), None (unknown call should be repeated)
     */
    private def hasNextInternal: Boolean = {
        var returnValue: Option[Boolean] = None

        while (returnValue.isEmpty) {
            if (readManyFilterBatchIterator.hasNext) {
                val readManyFilterBatch = readManyFilterBatchIterator.next()
                returnValue = executeWithRetry("hasNextInternal", () => hasNextInternalCore(readManyFilterBatch))
            } else {
                returnValue = Some(false)
            }
        }

        returnValue.get
    }

    /***
     * Checks whether more records exists - this will potentially trigger I/O operations and retries
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
                Thread.sleep(retryIntervalInMs)
            }
        }

        returnValue.get
    }

    //  Correct way to cancel a flux and dispose it
    //  https://github.com/reactor/reactor-core/blob/main/reactor-core/src/test/java/reactor/core/publisher/scenarios/FluxTests.java#L837
    override def close(): Unit = {}
}
