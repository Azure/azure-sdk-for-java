// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.changeFeedMetrics

import com.azure.cosmos.spark.CosmosConstants.MetricNames
import org.apache.spark.sql.connector.metric.CustomSumMetric

/***
 * This metric is used to track the lsn gap/range for partition within a change feed micro-batch
 */
private[cosmos] class ChangeFeedLsnGapMetric extends CustomSumMetric {

 override def name(): String = MetricNames.ChangeFeedLsnGap

 override def description(): String = MetricNames.ChangeFeedLsnGap
}
