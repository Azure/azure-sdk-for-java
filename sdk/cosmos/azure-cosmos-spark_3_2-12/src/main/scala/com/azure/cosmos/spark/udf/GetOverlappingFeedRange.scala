// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.models.SparkModelBridgeInternal
import com.azure.cosmos.spark.CosmosPredicates.requireNotNullOrEmpty
import org.apache.spark.sql.api.java.UDF3

@SerialVersionUID(1L)
class GetOverlappingFeedRange extends UDF3[String, Object, Array[String], String] {
  override def call
  (
    partitionKeyDefinitionJson: String,
    partitionKeyValue: Object,
    targetFeedRanges: Array[String]
  ): String = {
    requireNotNullOrEmpty(partitionKeyDefinitionJson, "partitionKeyDefinitionJson")

    val pkDefinition = SparkModelBridgeInternal.createPartitionKeyDefinitionFromJson(partitionKeyDefinitionJson)
    SparkBridgeImplementationInternal.getOverlappingRange(targetFeedRanges, partitionKeyValue, pkDefinition)
  }
}
