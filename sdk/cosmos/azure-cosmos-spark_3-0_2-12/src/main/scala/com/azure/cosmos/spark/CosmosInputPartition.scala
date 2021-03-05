// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.ChangeFeedOffset.{IdPropertyName, StatePropertyName, V1Identifier, objectMapper}
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.connector.read.InputPartition

private[spark] object CosmosInputPartition {
  private val IdPropertyName: String = "id"
  private val FeedRangePropertyName: String = "state"
  private val EndLsnPropertyName: String = "state"
  private val V1Identifier: String = "com.azure.cosmos.spark.inputPartition.v1"

  private val objectMapper = new ObjectMapper()
  def fromJson(json: String): CosmosInputPartition = {
    val parsedNode = objectMapper.readTree(json)
    if (parsedNode != null &&
      parsedNode.isObject &&
      parsedNode.get(IdPropertyName) != null &&
      parsedNode.get(IdPropertyName).asText("") == V1Identifier &&
      parsedNode.get(FeedRangePropertyName) != null &&
      parsedNode.get(FeedRangePropertyName).isTextual &&
      parsedNode.get(FeedRangePropertyName).asText("") != "") {

      val endLsn = if (parsedNode.get(EndLsnPropertyName) != null &&
        parsedNode.get(EndLsnPropertyName).asLong(-1) != -1) {
          Some(parsedNode.get(EndLsnPropertyName).asLong(-1))
        } else {
        None
      }

      CosmosInputPartition(parsedNode.get(FeedRangePropertyName).asText, endLsn)
    } else {
      val message = s"Unable to deserialize input partition '$json'."
      throw new IllegalStateException(message)
    }
  }
}


private[spark] case class CosmosInputPartition
(
  feedRangeJson: String,
  endLsn: Option[Long]
) extends InputPartition {

  @transient private[spark] val feedRange: FeedRange = FeedRange.fromString(feedRangeJson)

  @transient private lazy val jsonPersisted = endLsn match {
    case Some(lsn) => raw"""{"$CosmosInputPartition.IdPropertyName":"$CosmosInputPartition.V1Identifier",""" +
      raw""""$CosmosInputPartition.FeedRangePropertyName":"$feedRangeJson",""" +
      raw""""$CosmosInputPartition.EndLsnPropertyName":${String.valueOf(lsn)}}"""
    case None => raw"""{"$CosmosInputPartition.IdPropertyName":"$CosmosInputPartition.V1Identifier",""" +
      raw""""$CosmosInputPartition.FeedRangePropertyName":"$feedRangeJson"}"""
  }


  def json(): String = jsonPersisted
}
