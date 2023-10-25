// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.spark.CosmosPredicates.requireNotNullOrEmpty
import org.apache.spark.sql.api.java.UDF2


@SerialVersionUID(1L)
class GetFeedRangeForPartitionKeyValues extends UDF2[String, String, String] {
  override def call
  (
    partitionKeyDefinitionJson: String,
    partitionKeyValue: String
  ): String = {

    requireNotNullOrEmpty(partitionKeyDefinitionJson, "partitionKeyDefinitionJson")

    val range = SparkBridgeImplementationInternal
      .partitionKeyValuesToNormalizedRange(partitionKeyValue, partitionKeyDefinitionJson)

    s"${range.min}-${range.max}"
  }
}
