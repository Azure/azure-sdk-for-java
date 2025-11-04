// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.changeFeedMetrics.{ChangeFeedMetricsListener, ChangeFeedMetricsTracker}
import com.azure.cosmos.implementation.guava25.collect.{HashBiMap, Maps}
import org.apache.spark.Success
import org.apache.spark.executor.{ExecutorMetrics, TaskMetrics}
import org.apache.spark.scheduler.{SparkListenerTaskEnd, TaskInfo}
import org.apache.spark.sql.execution.metric.SQLMetric
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, when}

import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

class ChangeFeedMetricsListenerSpec extends UnitSpec {
  "ChangeFeedMetricsListener" should "be able to capture changeFeed performance metrics" in {
   val taskEnd = SparkListenerTaskEnd(
    stageId = 1,
    stageAttemptId = 0,
    taskType = "ResultTask",
    reason = Success,
    taskInfo = mock(classOf[TaskInfo]),
    taskExecutorMetrics = mock(classOf[ExecutorMetrics]),
    taskMetrics = mock(classOf[TaskMetrics])
   )

   val metrics = Map[String, SQLMetric](
    CosmosConstants.MetricNames.ChangeFeedPartitionIndex -> new SQLMetric("index", 1),
    CosmosConstants.MetricNames.ChangeFeedLsnRange -> new SQLMetric("lsn", 100),
    CosmosConstants.MetricNames.ChangeFeedItemsCnt -> new SQLMetric("items", 100)
   )

   // create sparkInternalsBridge mock
   val sparkInternalsBridge = mock(classOf[SparkInternalsBridge])
   when(sparkInternalsBridge.getInternalCustomTaskMetricsAsSQLMetric(
    ArgumentMatchers.any[Set[String]],
    ArgumentMatchers.any[TaskMetrics]
   )).thenReturn(metrics)

   val partitionIndexMap = Maps.synchronizedBiMap(HashBiMap.create[NormalizedRange, Long]())
   partitionIndexMap.put(NormalizedRange("0", "FF"), 1)

   val partitionMetricsMap = new ConcurrentHashMap[NormalizedRange, ChangeFeedMetricsTracker]()
   val changeFeedMetricsListener = new ChangeFeedMetricsListener(partitionIndexMap, partitionMetricsMap)

   // set the internal sparkInternalsBridgeField
   val sparkInternalsBridgeField: Field = classOf[ChangeFeedMetricsListener].getDeclaredField("sparkInternalsBridge")
   sparkInternalsBridgeField.setAccessible(true)
   sparkInternalsBridgeField.set(changeFeedMetricsListener, sparkInternalsBridge)

   //  verify that metrics will be properly tracked
   changeFeedMetricsListener.onTaskEnd(taskEnd)
   partitionMetricsMap.size() shouldBe 1
   partitionMetricsMap.containsKey(NormalizedRange("0", "FF")) shouldBe true
   partitionMetricsMap.get(NormalizedRange("0", "FF")).getWeightedChangeFeedItemsPerLsn.get shouldBe 1
  }

 it should "ignore metrics for unknown partition index" in {
  val taskEnd = SparkListenerTaskEnd(
   stageId = 1,
   stageAttemptId = 0,
   taskType = "ResultTask",
   reason = Success,
   taskInfo = mock(classOf[TaskInfo]),
   taskExecutorMetrics = mock(classOf[ExecutorMetrics]),
   taskMetrics = mock(classOf[TaskMetrics])
  )

  val metrics = Map[String, SQLMetric](
   CosmosConstants.MetricNames.ChangeFeedPartitionIndex -> new SQLMetric("index", 10),
   CosmosConstants.MetricNames.ChangeFeedLsnRange -> new SQLMetric("lsn", 100),
   CosmosConstants.MetricNames.ChangeFeedItemsCnt -> new SQLMetric("items", 100)
  )

  // create sparkInternalsBridge mock
  val sparkInternalsBridge = mock(classOf[SparkInternalsBridge])
  when(sparkInternalsBridge.getInternalCustomTaskMetricsAsSQLMetric(
   ArgumentMatchers.any[Set[String]],
   ArgumentMatchers.any[TaskMetrics]
  )).thenReturn(metrics)

  val partitionIndexMap = Maps.synchronizedBiMap(HashBiMap.create[NormalizedRange, Long]())
  partitionIndexMap.put(NormalizedRange("0", "FF"), 1)

  val partitionMetricsMap = new ConcurrentHashMap[NormalizedRange, ChangeFeedMetricsTracker]()
  val changeFeedMetricsListener = new ChangeFeedMetricsListener(partitionIndexMap, partitionMetricsMap)

  // set the internal sparkInternalsBridgeField
  val sparkInternalsBridgeField: Field = classOf[ChangeFeedMetricsListener].getDeclaredField("sparkInternalsBridge")
  sparkInternalsBridgeField.setAccessible(true)
  sparkInternalsBridgeField.set(changeFeedMetricsListener, sparkInternalsBridge)

  // because partition index 10 does not exist in the partitionIndexMap, it will be ignored
  changeFeedMetricsListener.onTaskEnd(taskEnd)
  partitionMetricsMap shouldBe empty
 }

 it should "ignore unrelated metrics" in {
  val taskEnd = SparkListenerTaskEnd(
   stageId = 1,
   stageAttemptId = 0,
   taskType = "ResultTask",
   reason = Success,
   taskInfo = mock(classOf[TaskInfo]),
   taskExecutorMetrics = mock(classOf[ExecutorMetrics]),
   taskMetrics = mock(classOf[TaskMetrics])
  )

  val metrics = Map[String, SQLMetric](
   "unknownMetrics" -> new SQLMetric("index", 10)
  )

  // create sparkInternalsBridge mock
  val sparkInternalsBridge = mock(classOf[SparkInternalsBridge])
  when(sparkInternalsBridge.getInternalCustomTaskMetricsAsSQLMetric(
   ArgumentMatchers.any[Set[String]],
   ArgumentMatchers.any[TaskMetrics]
  )).thenReturn(metrics)

  val partitionIndexMap = Maps.synchronizedBiMap(HashBiMap.create[NormalizedRange, Long]())
  partitionIndexMap.put(NormalizedRange("0", "FF"), 1)

  val partitionMetricsMap = new ConcurrentHashMap[NormalizedRange, ChangeFeedMetricsTracker]()
  val changeFeedMetricsListener = new ChangeFeedMetricsListener(partitionIndexMap, partitionMetricsMap)

  // set the internal sparkInternalsBridgeField
  val sparkInternalsBridgeField: Field = classOf[ChangeFeedMetricsListener].getDeclaredField("sparkInternalsBridge")
  sparkInternalsBridgeField.setAccessible(true)
  sparkInternalsBridgeField.set(changeFeedMetricsListener, sparkInternalsBridge)

  // because partition index 10 does not exist in the partitionIndexMap, it will be ignored
  changeFeedMetricsListener.onTaskEnd(taskEnd)
  partitionMetricsMap shouldBe empty
 }
}
