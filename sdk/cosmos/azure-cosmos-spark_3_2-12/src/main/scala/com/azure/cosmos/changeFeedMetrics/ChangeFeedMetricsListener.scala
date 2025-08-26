// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.changeFeedMetrics

import com.azure.cosmos.implementation.guava25.collect.BiMap
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.{CosmosConstants, NormalizedRange, SparkInternalsBridge}
import org.apache.spark.scheduler.{SparkListener, SparkListenerTaskEnd}
import org.apache.spark.sql.execution.metric.SQLMetric

import java.util.concurrent.ConcurrentHashMap

private[cosmos] class ChangeFeedMetricsListener(
     partitionIndexMap: BiMap[NormalizedRange, Long],
     partitionMetricsMap: ConcurrentHashMap[NormalizedRange, ChangeFeedMetricsTracker]) extends SparkListener with BasicLoggingTrait{

 override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
  try {
   val metrics = SparkInternalsBridge.getInternalCustomTaskMetricsAsSQLMetric(
    Set(
     CosmosConstants.MetricNames.ChangeFeedLsnGap,
     CosmosConstants.MetricNames.ChangeFeedFetchedChangesCnt,
     CosmosConstants.MetricNames.ChangeFeedPartitionIndex
    ),
    taskEnd.taskMetrics
   )

   if (metrics.contains(CosmosConstants.MetricNames.ChangeFeedPartitionIndex)) {
    val index = metrics(CosmosConstants.MetricNames.ChangeFeedPartitionIndex).value

    val normalizedRange = partitionIndexMap.inverse().get(index)
    if (normalizedRange != null) {
     partitionMetricsMap.putIfAbsent(normalizedRange, new ChangeFeedMetricsTracker(index, normalizedRange))
     val metricsTracker = partitionMetricsMap.get(normalizedRange)
     val changesCnt = getChangesCnt(metrics)
     val lsnGap = getLsnGap(metrics)

     if (changesCnt >= 0 && lsnGap >= 0) {
      metricsTracker.track(
       metrics(CosmosConstants.MetricNames.ChangeFeedLsnGap).value,
       metrics(CosmosConstants.MetricNames.ChangeFeedFetchedChangesCnt).value
      )
     }

     logInfo(s"onTaskEnd for partition index $index, changesCnt $changesCnt, lsnGap $lsnGap")
    }
   }
  } catch {
   // using metrics to tune the change feed micro batch is optimization
   // suppress any exceptions captured
   case e: Throwable =>
    logWarning("Tracking changeFeed metrics failed", e)
  }
 }

 def getChangesCnt(metrics: Map[String, SQLMetric]): Long = {
  if (metrics.contains(CosmosConstants.MetricNames.ChangeFeedFetchedChangesCnt)) {
   metrics(CosmosConstants.MetricNames.ChangeFeedFetchedChangesCnt).value
  } else {
   -1
  }
 }

 def getLsnGap(metrics: Map[String, SQLMetric]): Long = {
  if (metrics.contains(CosmosConstants.MetricNames.ChangeFeedLsnGap)) {
   metrics(CosmosConstants.MetricNames.ChangeFeedLsnGap).value
  } else {
   -1
  }
 }
}
