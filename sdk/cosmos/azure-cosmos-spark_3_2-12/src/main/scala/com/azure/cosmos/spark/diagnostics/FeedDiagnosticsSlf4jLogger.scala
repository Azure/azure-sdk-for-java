// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationContext
import com.azure.cosmos.models.FeedResponse

import java.util.concurrent.atomic.AtomicLong

private[spark] class FeedDiagnosticsSlf4jLogger(classType: Class[_])
  extends DefaultMinimalSlf4jLogger(classType: Class[_]) {

  @transient private lazy val inFlightResponsesCounter = new AtomicLong(0)

  override def feedResponseReceivedListener
  (
    context: OperationContext,
    response: FeedResponse[_]
  ): Unit = {

    val inFlightResponses = inFlightResponsesCounter.incrementAndGet()

    logInfo(
      s"Received - ItemCount: ${response.getResults.size} CT: ${getContinuationTokenHash(response)}, " +
        s"In-flight-responses: $inFlightResponses Context: $context")
  }

  override def feedResponseProcessedListener
  (
    context: OperationContext,
    response: FeedResponse[_]
  ): Unit = {

    val inFlightResponses = inFlightResponsesCounter.decrementAndGet()

    logInfo(
      s"Processed - ItemCount: ${response.getResults.size} CT: ${getContinuationTokenHash(response)}, " +
        s"In-flight-responses: $inFlightResponses Context: $context")
  }

  def getInFlightResponseCountSnapshot : Long = {
    inFlightResponsesCounter.get()
  }

  private[this] def getContinuationTokenHash(response: FeedResponse[_]): Option[Int] = {
    Option(response.getContinuationToken) match {
      case Some(ct) => Some(ct.hashCode)
      case None => None
    }
  }
}
