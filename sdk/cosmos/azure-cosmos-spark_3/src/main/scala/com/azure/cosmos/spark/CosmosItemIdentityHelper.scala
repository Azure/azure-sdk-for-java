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
    s"id($id).pk(${objectMapper.writeValueAsString(partitionKeyValue.asJava)})"
  }

  def tryParseCosmosItemIdentity(cosmosItemIdentityString: String): Option[CosmosItemIdentity] = {
    cosmosItemIdentityString match {
      case cosmosItemIdentityStringRegx(idValue, pkValue) =>
        val partitionKeyValue = Utils.parse(pkValue, classOf[Object])
        partitionKeyValue match {
          case arrayList: util.ArrayList[_] => 
            // Convert Jackson JsonNode objects to their primitive values
            // This is necessary because Utils.parse returns JsonNode instances when deserializing from JSON
            // In Scala 2.13, the deprecated JavaConverters behaves differently, so we need explicit conversion
            val pkValuesArray = new Array[Object](arrayList.size())
            var i = 0
            while (i < arrayList.size()) {
              pkValuesArray(i) = arrayList.get(i) match {
                case node: JsonNode => convertJsonNodeToPrimitive(node)
                case other => other.asInstanceOf[Object]
              }
              i += 1
            }
            Some(createCosmosItemIdentityWithMultiHashPartitionKey(idValue, pkValuesArray))
          case _ => Some(new CosmosItemIdentity(new PartitionKey(partitionKeyValue), idValue))
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
      // For any other type, return the node itself and let the partition key logic handle it
      node
    }
  }
}
