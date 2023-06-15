// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.ChangeFeedSparkRowItem
import com.azure.cosmos.implementation.spark.OperationContext
import com.azure.cosmos.models.{FeedResponse, PartitionKeyDefinition}

import java.util.concurrent.atomic.AtomicLong

private[spark] class FeedDiagnosticsSlf4jLogger(classType: Class[_], includeDetails: Boolean)
  extends DefaultMinimalSlf4jLogger(classType: Class[_]) {

  @transient private lazy val inFlightResponsesCounter = new AtomicLong(0)

  private[this] def extractIdentities(response: FeedResponse[_]): Option[String] = {

    if (!response.isInstanceOf[FeedResponse[ChangeFeedSparkRowItem]]) {
      None
    } else {
      val changeFeedResponse = response.asInstanceOf[FeedResponse[ChangeFeedSparkRowItem]]
      val sb = new StringBuilder()
      changeFeedResponse.getResults.forEach(item => {
        if (sb.length > 0) {
          sb.append(", (")
        } else {
          sb.append("(")
        }

        sb.append(item.lsn)
        sb.append(", ")
        item.pkValue match {
          case Some(pk) => sb.append(pk.toString)
          case None => sb.append("n/a")
        }
        sb.append(", ")
        sb.append(item.row.getAs[String]("id"))
        sb.append(")")
      })

      Some(sb.toString)
    }
  }

  override def feedResponseReceivedListener
  (
    context: OperationContext,
    response: FeedResponse[_]
  ): Unit = {

    val inFlightResponses = inFlightResponsesCounter.incrementAndGet()

    if (isInfoLogEnabled) {
      if (includeDetails) {
        logInfo(
          s"Received - ItemCount: ${response.getResults.size} CT: ${getContinuationTokenHash(response)}, " +
            s"In-flight-responses: $inFlightResponses Context: $context Items: ${extractIdentities(response)}")

      } else {
        logInfo(
          s"Received - ItemCount: ${response.getResults.size} CT: ${getContinuationTokenHash(response)}, " +
            s"In-flight-responses: $inFlightResponses Context: $context")
      }
    }
  }

  override def feedResponseProcessedListener
  (
    context: OperationContext,
    response: FeedResponse[_]
  ): Unit = {

    val inFlightResponses = inFlightResponsesCounter.decrementAndGet()

    if (isInfoLogEnabled) {
      if (includeDetails) {
        logInfo(
          s"Processed - ItemCount: ${response.getResults.size} CT: ${getContinuationTokenHash(response)}, " +
            s"In-flight-responses: $inFlightResponses Context: $context Items: ${extractIdentities(response)}")
      } else {
        logInfo(
          s"Processed - ItemCount: ${response.getResults.size} CT: ${getContinuationTokenHash(response)}, " +
            s"In-flight-responses: $inFlightResponses Context: $context")
      }
    }
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
