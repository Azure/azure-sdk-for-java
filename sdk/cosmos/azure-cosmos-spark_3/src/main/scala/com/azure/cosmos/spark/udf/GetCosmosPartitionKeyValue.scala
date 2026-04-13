// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.udf

import com.azure.cosmos.spark.CosmosPartitionKeyHelper
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import org.apache.spark.sql.api.java.UDF1

@SerialVersionUID(1L)
class GetCosmosPartitionKeyValue extends UDF1[Object, String] {
  override def call
  (
    partitionKeyValue: Object
  ): String = {
    requireNotNull(partitionKeyValue, "partitionKeyValue")

    partitionKeyValue match {
      // for subpartitions case - Seq covers both WrappedArray (Scala 2.12) and ArraySeq (Scala 2.13)
      case seq: Seq[Any] =>
        CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(seq.map(_.asInstanceOf[Object]).toList)
      case _ => CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(List(partitionKeyValue))
    }
  }
}
