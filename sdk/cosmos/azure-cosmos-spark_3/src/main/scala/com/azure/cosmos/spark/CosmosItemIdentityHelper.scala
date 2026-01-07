// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.routing.PartitionKeyInternal
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, Utils}
import com.azure.cosmos.models.{CosmosItemIdentity, PartitionKey}
import com.fasterxml.jackson.databind.JsonNode

import java.util

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object CosmosItemIdentityHelper {
  // pattern will be recognized
  // 1. id(idValue).pk(partitionKeyValue)
  //
  // (?i) : The whole matching is case-insensitive
  // id[(](.*?)[)]: id value
  // [.]pk[(](.*)[)]: partitionKey Value
  private val cosmosItemIdentityStringRegx = """(?i)id[(](.*?)[)][.]pk[(](.*)[)]""".r
  private val objectMapper = Utils.getSimpleObjectMapper

  def getCosmosItemIdentityValueString(id: String, partitionKeyValue: List[Object]): String = {
    // Explicitly create a Java ArrayList to avoid Scala 2.12/2.13 differences in .asJava behavior
    val javaList = new util.ArrayList[Object](partitionKeyValue.size)
    partitionKeyValue.foreach(value => javaList.add(value))
    s"id($id).pk(${objectMapper.writeValueAsString(javaList)})"
  }

  def tryParseCosmosItemIdentity(cosmosItemIdentityString: String): Option[CosmosItemIdentity] = {
    cosmosItemIdentityString match {
      case cosmosItemIdentityStringRegx(idValue, pkValue) =>
        // Parse the partition key value from JSON string
        // Use JsonNode first, then convert to ArrayList to avoid Scala 2.12/2.13 differences
        val pkValueNode = objectMapper.readTree(pkValue)
        
        if (pkValueNode.isArray) {
          // Multi-value partition key (hierarchical)
          val pkValuesArray = new Array[Object](pkValueNode.size())
          var i = 0
          while (i < pkValueNode.size()) {
            pkValuesArray(i) = convertJsonNodeToPrimitive(pkValueNode.get(i))
            i += 1
          }
          Some(createCosmosItemIdentityWithMultiHashPartitionKey(idValue, pkValuesArray))
        } else {
          // Single value partition key
          val primitiveValue = convertJsonNodeToPrimitive(pkValueNode)
          Some(new CosmosItemIdentity(new PartitionKey(primitiveValue), idValue))
        }
      case _ => None
    }
  }

  private[this] def createCosmosItemIdentityWithMultiHashPartitionKey(idValue: String, pkValuesArray: Array[Object])  = {
    val partitionKey =
      ImplementationBridgeHelpers
        .PartitionKeyHelper
        .getPartitionKeyAccessor
        .toPartitionKey(PartitionKeyInternal.fromObjectArray(pkValuesArray, true))

    new CosmosItemIdentity(partitionKey, idValue)
  }
  
  private[this] def convertJsonNodeToPrimitive(node: JsonNode): Object = {
    if (node.isTextual) {
      node.asText()
    } else if (node.isBoolean) {
      Boolean.box(node.asBoolean())
    } else if (node.isInt || node.isLong) {
      Long.box(node.asLong())
    } else if (node.isDouble || node.isFloat) {
      Double.box(node.asDouble())
    } else if (node.isNull) {
      null
    } else {
      throw new IllegalArgumentException(
        s"Invalid partition key value: partition keys must be primitive values (string, number, boolean, or null), got JsonNode type: ${node.getNodeType}"
      )
    }
  }
}
