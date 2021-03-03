// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.ChangeFeedOffset.{IdPropertyName, StatePropertyName, V1Identifier}
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.connector.read.streaming.{Offset, PartitionOffset}

private case class ChangeFeedOffset
(
  changeFeedState: String
) extends Offset
  with Serializable
  with PartitionOffset
  with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  @transient private lazy val jsonPersisted =
    raw"""{"$IdPropertyName":"$V1Identifier","$StatePropertyName":"$changeFeedState"}"""

  override def json(): String = jsonPersisted
}

private object ChangeFeedOffset {
  private val IdPropertyName: String = "id"
  private val StatePropertyName: String = "state"
  private val V1Identifier: String = "com.azure.cosmos.spark.changeFeed.offset.v1"
  private val objectMapper = new ObjectMapper()

  def fromJson(json: String): ChangeFeedOffset = {
    val parsedNode = objectMapper.readTree(json)
    if (parsedNode != null &&
      parsedNode.isObject &&
      parsedNode.get(IdPropertyName) != null &&
      parsedNode.get(IdPropertyName).asText("") == V1Identifier &&
      parsedNode.get(StatePropertyName) != null &&
      parsedNode.get(StatePropertyName).isTextual &&
      parsedNode.get(StatePropertyName).asText("") != "") {

      ChangeFeedOffset(parsedNode.get(StatePropertyName).asText)
    } else {
        val message = s"Unable to deserialize offset '$json'."
        throw new IllegalStateException(message)
    }
  }
}
