// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.routing.PartitionKeyInternal
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, Utils}
import com.azure.cosmos.models.{CosmosItemIdentity, PartitionKey}

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
          case arrayList: util.ArrayList[Object] => Some(createCosmosItemIdentityWithMultiHashPartitionKey(idValue, arrayList.toArray))
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
        .toPartitionKey(PartitionKeyInternal.fromObjectArray(pkValuesArray, false))

    new CosmosItemIdentity(partitionKey, idValue)
  }
}
