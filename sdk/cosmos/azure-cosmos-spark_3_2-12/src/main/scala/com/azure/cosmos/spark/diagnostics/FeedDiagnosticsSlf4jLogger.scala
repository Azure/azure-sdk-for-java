// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.{ChangeFeedSparkRowItem, SparkRowItem}
import com.azure.cosmos.implementation.spark.OperationContext
import com.azure.cosmos.models.{FeedResponse, PartitionKeyDefinition}

import java.util.concurrent.atomic.AtomicLong
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.{TypeTag, typeOf}

private[spark] class FeedDiagnosticsSlf4jLogger(classType: Class[_], includeDetails: Boolean)
  extends DefaultMinimalSlf4jLogger(classType: Class[_]) {

  @transient private lazy val inFlightResponsesCounter = new AtomicLong(0)

  private[this] def extractIdentities(response: FeedResponse[_]): Option[String] = {

    val firstItem = if (response.getResults.size() > 0) {
      Some(response.getResults.get(0))
    } else {
      None
    }

    if (firstItem.isEmpty) {
      None
    } else if (ChangeFeedSparkRowItem.getClass.getName.equals(s"${firstItem.get.getClass.getName}$$")) {
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

    } else if (SparkRowItem.getClass.getName.equals(s"${firstItem.get.getClass.getName}$$")) {
      val queryResponse = response.asInstanceOf[FeedResponse[SparkRowItem]]
      val sb = new StringBuilder()
      queryResponse.getResults.forEach(item => {
        if (sb.length > 0) {
          sb.append(", (")
        } else {
          sb.append("(")
        }

        item.pkValue match {
          case Some(pk) => sb.append(pk.toString)
          case None => sb.append("n/a")
        }
        sb.append(", ")
        sb.append(item.row.getAs[String]("id"))
        sb.append(")")
      })

      Some(sb.toString)
    } else {
      None
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
            s"In-flight-responses: $inFlightResponses Context: $context Items: ${extractIdentities(response).getOrElse("")}")

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
            s"In-flight-responses: $inFlightResponses Context: $context Items: ${extractIdentities(response).getOrElse("")}")
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
