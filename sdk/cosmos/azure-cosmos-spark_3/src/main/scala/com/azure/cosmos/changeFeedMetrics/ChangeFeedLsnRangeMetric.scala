// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.changeFeedMetrics

import com.azure.cosmos.spark.CosmosConstants.MetricNames
import org.apache.spark.sql.connector.metric.CustomSumMetric

/***
 * This metric is used to track the lsn range for partition within a change feed micro-batch
 */
private[cosmos] class ChangeFeedLsnRangeMetric extends CustomSumMetric {

 override def name(): String = MetricNames.ChangeFeedLsnRange

 override def description(): String = MetricNames.ChangeFeedLsnRange
}
