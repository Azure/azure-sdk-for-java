// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.connector.metric.CustomSumMetric

private[cosmos] class CosmosRecordsWrittenMetric extends CustomSumMetric {
  override def name(): String = CosmosConstants.MetricNames.RecordsWritten

  override def description(): String = CosmosConstants.MetricNames.RecordsWritten
}
