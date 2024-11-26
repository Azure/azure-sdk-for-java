// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.CosmosPredicates.requireNotNullOrEmpty
import org.apache.spark.sql.api.java.UDF2

@SerialVersionUID(1L)
class GetFeedRangesForBuckets extends UDF2[String, Int, Array[String]] {
  override def call
  (
    partitionKeyDefinitionJson: String,
    bucketCount: Int
  ): Array[String] = {

    requireNotNullOrEmpty(partitionKeyDefinitionJson, "partitionKeyDefinitionJson")

    SparkBridgeImplementationInternal.trySplitFeedRanges(partitionKeyDefinitionJson,
      FeedRange.forFullRange().asInstanceOf[FeedRangeEpkImpl],
      bucketCount)
  }
}
