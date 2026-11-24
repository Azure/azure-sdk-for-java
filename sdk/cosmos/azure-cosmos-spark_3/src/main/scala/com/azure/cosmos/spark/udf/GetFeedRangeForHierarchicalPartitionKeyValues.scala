// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.spark.CosmosPredicates.requireNotNullOrEmpty
import org.apache.spark.sql.api.java.UDF2


@SerialVersionUID(1L)
class GetFeedRangeForHierarchicalPartitionKeyValues extends UDF2[String, Object, String] {
  override def call
  (
    partitionKeyDefinitionJson: String,
    partitionKeyValueJsonArray: Object
  ): String = {

    requireNotNullOrEmpty(partitionKeyDefinitionJson, "partitionKeyDefinitionJson")

    val range = SparkBridgeImplementationInternal
      .hierarchicalPartitionKeyValuesToNormalizedRange(partitionKeyValueJsonArray, partitionKeyDefinitionJson)

    s"${range.min}-${range.max}"
  }
}
