// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.FeedRange
import org.apache.spark.sql.connector.read.InputPartition

private[spark] case class FeedRangeInputPartition(feedRangeJson: String) extends InputPartition {
  @transient private[spark] val feedRange = FeedRange.fromString(feedRangeJson)
}
