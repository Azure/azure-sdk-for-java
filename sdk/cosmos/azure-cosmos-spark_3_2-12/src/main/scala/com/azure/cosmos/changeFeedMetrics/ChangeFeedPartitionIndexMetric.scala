// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.changeFeedMetrics

import com.azure.cosmos.spark.CosmosConstants.MetricNames
import org.apache.spark.sql.connector.metric.CustomMetric

/***
 * This metric is used to capture the cosmos partition index in a consistent way
 */
private[cosmos] class ChangeFeedPartitionIndexMetric extends CustomMetric {

 override def name(): String = MetricNames.ChangeFeedPartitionIndex

 override def description(): String = MetricNames.ChangeFeedPartitionIndex

 override def aggregateTaskMetrics(taskMetrics: Array[Long]): String = {
  taskMetrics.mkString(",")
 }
}
