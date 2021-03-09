// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.CosmosPredicates.assertNotNullOrEmpty
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.connector.read.InputPartition

private[spark] object CosmosInputPartition {
  private val IdPropertyName: String = "id"
  private val FeedRangePropertyName: String = "state"
  private val EndLsnPropertyName: String = "state"
  private val V1Identifier: String = "com.azure.cosmos.spark.inputPartition.v1"

  private val objectMapper = new ObjectMapper()
  def fromJson(json: String): CosmosInputPartition = {
    assertNotNullOrEmpty(json, "json")

    val parsedNode = objectMapper.readTree(json)
    if (parsedNode != null &&
      parsedNode.isObject &&
      parsedNode.get(IdPropertyName) != null &&
      parsedNode.get(IdPropertyName).asText("") == V1Identifier &&
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
      val message = s"Unable to deserialize input partition '$json'."
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
    case Some(lsn) => raw"""{"$CosmosInputPartition.IdPropertyName":"$CosmosInputPartition.V1Identifier",""" +
      raw""""$CosmosInputPartition.FeedRangePropertyName":["${feedRange.min}","${feedRange.max}"],""" +
      raw""""$CosmosInputPartition.EndLsnPropertyName":${String.valueOf(lsn)}}"""
    case None => raw"""{"$CosmosInputPartition.IdPropertyName":"$CosmosInputPartition.V1Identifier",""" +
      raw""""$CosmosInputPartition.FeedRangePropertyName":["${feedRange.min}","${feedRange.max}"]}"""
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
