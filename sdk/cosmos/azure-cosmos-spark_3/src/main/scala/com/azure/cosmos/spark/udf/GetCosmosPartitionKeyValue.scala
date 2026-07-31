// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.udf

import com.azure.cosmos.spark.CosmosPartitionKeyHelper
import org.apache.spark.sql.api.java.UDF1

@SerialVersionUID(1L)
class GetCosmosPartitionKeyValue extends UDF1[Object, String] {
  // Null is a valid partition-key value (JSON null). A null input is serialized as a
  // single-level partition key with a JSON null component; parsing that string back via
  // CosmosPartitionKeyHelper.tryParsePartitionKey yields a PartitionKey built with
  // addNullValue(). If the caller instead wants PartitionKey.NONE semantics (absent PK
  // field) they should filter the null row before calling this UDF and use the schema-matched
  // readManyByPartitionKeys path with readManyByPk.nullHandling=None. That None mode is only
  // supported for single-path partition keys; hierarchical partition keys reject it.
  override def call(partitionKeyValue: Object): String = {
    partitionKeyValue match {
      case null =>
        CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(List(null))
      // for subpartitions case - Seq covers both WrappedArray (Scala 2.12) and ArraySeq (Scala 2.13)
      case seq: Seq[Any] =>
        CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(seq.map(_.asInstanceOf[Object]).toList)
      case _ =>
        CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(List(partitionKeyValue))
    }
  }
}
