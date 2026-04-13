// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.routing.PartitionKeyInternal
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, Utils}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

import java.util

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object CosmosPartitionKeyHelper extends BasicLoggingTrait {
  // pattern will be recognized
  // pk(partitionKeyValue)
  //
  // (?i) : The whole matching is case-insensitive
  // pk[(](.*)[)]: partitionKey Value
  private val cosmosPartitionKeyStringRegx = """(?i)pk[(](.*)[)]""".r
  private val objectMapper = Utils.getSimpleObjectMapper

  def getCosmosPartitionKeyValueString(partitionKeyValue: List[Object]): String = {
    s"pk(${objectMapper.writeValueAsString(partitionKeyValue.asJava)})"
  }

  def tryParsePartitionKey(cosmosPartitionKeyString: String): Option[PartitionKey] = {
    cosmosPartitionKeyString match {
      case cosmosPartitionKeyStringRegx(pkValue) =>
        val partitionKeyValue = Utils.parse(pkValue, classOf[Object])
        partitionKeyValue match {
          case arrayList: util.ArrayList[Object] =>
            Some(
              ImplementationBridgeHelpers
                .PartitionKeyHelper
                .getPartitionKeyAccessor
                .toPartitionKey(PartitionKeyInternal.fromObjectArray(arrayList.toArray, false)))
          case _ => Some(new PartitionKey(partitionKeyValue))
        }
      case _ => None
    }
  }
}
