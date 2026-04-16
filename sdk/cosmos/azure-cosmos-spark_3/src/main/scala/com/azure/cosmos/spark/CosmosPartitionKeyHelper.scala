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
        scala.util.Try(Utils.parse(pkValue, classOf[Object])).toOption.flatMap {
          case arrayList: util.ArrayList[Object @unchecked] =>
            Some(
              ImplementationBridgeHelpers
                .PartitionKeyHelper
                .getPartitionKeyAccessor
                .toPartitionKey(PartitionKeyInternal.fromObjectArray(arrayList.toArray, false)))
          case other => Some(new PartitionKey(other))
        }
      case _ => None
    }
  }
}
