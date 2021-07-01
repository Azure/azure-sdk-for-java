// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.ChangeFeedOffset.{IdPropertyName, InputPartitionsPropertyName, StatePropertyName, V1Identifier}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.spark.sql.connector.read.streaming.{Offset, PartitionOffset}

import scala.collection.mutable.ArrayBuffer

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private case class ChangeFeedOffset
(
  changeFeedState: String,
  inputPartitions: Option[Array[CosmosInputPartition]]
) extends Offset
  with Serializable
  with PartitionOffset {

  @transient private lazy val jsonPersisted = inputPartitions match {
    case Some(partitions) =>
      val partitionsJson = String.join(",", partitions.map(p => raw"""${p.json()}""" ).toList.asJava)
      raw"""{"$IdPropertyName":"$V1Identifier",""" +
        raw""""$StatePropertyName":"$changeFeedState",""" +
        raw""""$InputPartitionsPropertyName":[$partitionsJson]}"""
    case None => raw"""{"$IdPropertyName":"$V1Identifier","$StatePropertyName":"$changeFeedState"}"""
  }

  override def json(): String = jsonPersisted
}

private object ChangeFeedOffset {
  private val IdPropertyName: String = "id"
  private val StatePropertyName: String = "state"
  private val InputPartitionsPropertyName: String = "partitions"
  private val V1Identifier: String = "com.azure.cosmos.spark.changeFeed.offset.v1"
  private val objectMapper = new ObjectMapper()

  def fromJson(json: String): ChangeFeedOffset = {
    val parsedNode = objectMapper.readTree(json)
    if (isValidJson(parsedNode)) {

      // Input partitions are serialized here to avoid having to calculate the latest LSN again
      // We need the latest LSN to calculate the endOffset/latestOffset - so we calculate
      // the input partitions already and pass it via the end offset to planInputPartitions call
      val inputPartitions = if (parsedNode.get(InputPartitionsPropertyName) != null &&
        parsedNode.get(InputPartitionsPropertyName).isArray) {
        val arrayNode = parsedNode.get(InputPartitionsPropertyName).asInstanceOf[ArrayNode]
        val inputPartitions = ArrayBuffer[CosmosInputPartition]()
        for (i <- 0 until arrayNode.size) {
          inputPartitions += CosmosInputPartition.fromJson(arrayNode.get(i))
        }
        Some(inputPartitions.toArray)
      } else {
        None
      }

      ChangeFeedOffset(parsedNode.get(StatePropertyName).asText, inputPartitions)
    } else {
      val message = s"Unable to deserialize offset '$json'."
      throw new IllegalArgumentException(message)
    }
  }

  private[this] def isValidJson(parsedNode: JsonNode): Boolean = {
    parsedNode != null &&
      parsedNode.isObject &&
      parsedNode.get(IdPropertyName) != null &&
      parsedNode.get(IdPropertyName).asText("") == V1Identifier &&
      parsedNode.get(StatePropertyName) != null &&
      parsedNode.get(StatePropertyName).isTextual &&
      parsedNode.get(StatePropertyName).asText("") != ""
  }
}
