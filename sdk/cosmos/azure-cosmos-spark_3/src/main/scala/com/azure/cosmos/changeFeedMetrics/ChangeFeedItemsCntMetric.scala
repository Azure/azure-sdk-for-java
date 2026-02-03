// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.changeFeedMetrics

import com.azure.cosmos.spark.CosmosConstants.MetricNames
import org.apache.spark.sql.connector.metric.CustomSumMetric

/***
 * This metric is used to track the partition total items count within a change feed micro-batch
 * Note: not all the items will be returned back to spark
 */
private[cosmos] class ChangeFeedItemsCntMetric extends CustomSumMetric {

 override def name(): String = MetricNames.ChangeFeedItemsCnt

 override def description(): String = MetricNames.ChangeFeedItemsCnt
}
