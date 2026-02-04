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

 private val sparkInternalsBridge = new SparkInternalsBridge()
 override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
  try {
   val metrics = sparkInternalsBridge.getInternalCustomTaskMetricsAsSQLMetric(
    Set(
     CosmosConstants.MetricNames.ChangeFeedLsnRange,
     CosmosConstants.MetricNames.ChangeFeedItemsCnt,
     CosmosConstants.MetricNames.ChangeFeedPartitionIndex
    ),
    taskEnd.taskMetrics
   )

   if (metrics.contains(CosmosConstants.MetricNames.ChangeFeedPartitionIndex)) {
    val index = metrics(CosmosConstants.MetricNames.ChangeFeedPartitionIndex).value

    val normalizedRange = partitionIndexMap.inverse().get(index)
    if (normalizedRange != null) {
     partitionMetricsMap.putIfAbsent(normalizedRange, ChangeFeedMetricsTracker(index, normalizedRange))
     val metricsTracker = partitionMetricsMap.get(normalizedRange)
     val changeFeedItemsCnt = getFetchedItemCnt(metrics)
     val lsnRange = getLsnRange(metrics)

     if (changeFeedItemsCnt >= 0 && lsnRange >= 0) {
      metricsTracker.track(
       metrics(CosmosConstants.MetricNames.ChangeFeedLsnRange).value,
       metrics(CosmosConstants.MetricNames.ChangeFeedItemsCnt).value
      )
     }

     logInfo(s"onTaskEnd for partition index $index, changeFeedItemsCnt $changeFeedItemsCnt, lsnRange $lsnRange")
    }
   }
  } catch {
   // using metrics to tune the change feed micro batch is optimization
   // suppress any exceptions captured
   case e: Throwable =>
    logWarning("Tracking changeFeed metrics failed", e)
  }
 }

 private def getFetchedItemCnt(metrics: Map[String, SQLMetric]): Long = {
  if (metrics.contains(CosmosConstants.MetricNames.ChangeFeedItemsCnt)) {
   metrics(CosmosConstants.MetricNames.ChangeFeedItemsCnt).value
  } else {
   -1
  }
 }

 private def getLsnRange(metrics: Map[String, SQLMetric]): Long = {
  if (metrics.contains(CosmosConstants.MetricNames.ChangeFeedLsnRange)) {
   metrics(CosmosConstants.MetricNames.ChangeFeedLsnRange).value
  } else {
   -1
  }
 }
}
