// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.CosmosInputPartition.{EndLsnPropertyName, FeedRangePropertyName}
import com.azure.cosmos.spark.CosmosPredicates.assertNotNull
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.spark.sql.connector.read.InputPartition

private[spark] object CosmosInputPartition {
  private val FeedRangePropertyName: String = "range"
  private val EndLsnPropertyName: String = "endLsn"
  private val objectMapper = new ObjectMapper()

  def fromJson(parsedNode: JsonNode): CosmosInputPartition = {
    assertNotNull(parsedNode, "parsedNode")

    if (parsedNode.isObject &&
      parsedNode.get(FeedRangePropertyName) != null &&
      parsedNode.get(FeedRangePropertyName).isArray &&
      parsedNode.get(FeedRangePropertyName).size() == 2 ) {

      val endLsn = if (parsedNode.get(EndLsnPropertyName) != null &&
        parsedNode.get(EndLsnPropertyName).asLong(-1) != -1) {
          Some(parsedNode.get(EndLsnPropertyName).asLong(-1))
        } else {
        None
      }

      val feedRange = NormalizedRange(
        parsedNode.get(FeedRangePropertyName).get(0).asText(""),
        parsedNode.get(FeedRangePropertyName).get(1).asText("FF"))

      CosmosInputPartition(feedRange, endLsn)
    } else {
      val message = s"Unable to deserialize input partition '${objectMapper.writeValueAsString(parsedNode)}'."
      throw new IllegalStateException(message)
    }
  }
}

private[spark] case class CosmosInputPartition
(
  feedRange: NormalizedRange,
  endLsn: Option[Long],
  continuationState: Option[String] = None
) extends InputPartition {

  // Intentionally leaving out the change feed state when serializing input partition to json
  // the continuation state will be provided later and added by calling withContinuationState
  @transient private lazy val jsonPersisted = endLsn match {
    case Some(lsn) => raw"""{"$FeedRangePropertyName":["${feedRange.min}","${feedRange.max}"],""" +
      raw""""$EndLsnPropertyName":${String.valueOf(lsn)}}"""
    case None => raw"""{"$FeedRangePropertyName":["${feedRange.min}","${feedRange.max}"]}"""
  }

  def json(): String = jsonPersisted

  private[spark] def withContinuationState(continuationState: String, clearEndLsn: Boolean): InputPartition = {
    val effectiveEndLsn = if (clearEndLsn) {
      None
    } else {
      this.endLsn
    }
    CosmosInputPartition(this.feedRange, effectiveEndLsn , Some(continuationState))
  }

  private[spark] def clearEndLsn(): InputPartition = {
    CosmosInputPartition(this.feedRange, None, this.continuationState)
  }
}
