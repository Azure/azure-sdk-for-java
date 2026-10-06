// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.connector.metric.CustomSumMetric

private[cosmos] class CosmosRecordsSkippedMetric extends CustomSumMetric {
  override def name(): String = CosmosConstants.MetricNames.RecordsSkipped

  override def description(): String = "number of records skipped as an intentional no-op by the write strategy"
}
