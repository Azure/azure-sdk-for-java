// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// scalastyle:off magic.number
// scalastyle:off multiple.string.literals

package com.azure.cosmos.spark

import com.azure.cosmos.changeFeedMetrics.{ChangeFeedMetricsListener, ChangeFeedMetricsTracker}
import com.azure.cosmos.implementation.guava25.collect.{HashBiMap, Maps}
import org.apache.spark.Success
import org.apache.spark.executor.{ExecutorMetrics, TaskMetrics}
import org.apache.spark.scheduler.{SparkListenerTaskEnd, TaskInfo}
import org.apache.spark.sql.execution.metric.{SQLMetric, SQLMetrics}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, when}

import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

class ChangeFeedMetricsListenerITest extends IntegrationSpec with SparkWithJustDropwizardAndNoSlf4jMetrics {
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

   val indexMetric = SQLMetrics.createMetric(spark.sparkContext, "index")
   indexMetric.set(1)
   val lsnMetric = SQLMetrics.createMetric(spark.sparkContext, "lsn")
   lsnMetric.set(100)
   val itemsMetric = SQLMetrics.createMetric(spark.sparkContext, "items")
   itemsMetric.set(100)

   val metrics = Map[String, SQLMetric](
    CosmosConstants.MetricNames.ChangeFeedPartitionIndex -> indexMetric,
    CosmosConstants.MetricNames.ChangeFeedLsnRange -> lsnMetric,
    CosmosConstants.MetricNames.ChangeFeedItemsCnt -> itemsMetric
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

  val indexMetric2 = SQLMetrics.createMetric(spark.sparkContext, "index")
  indexMetric2.set(10)
  val lsnMetric2 = SQLMetrics.createMetric(spark.sparkContext, "lsn")
  lsnMetric2.set(100)
  val itemsMetric2 = SQLMetrics.createMetric(spark.sparkContext, "items")
  itemsMetric2.set(100)

  val metrics = Map[String, SQLMetric](
   CosmosConstants.MetricNames.ChangeFeedPartitionIndex -> indexMetric2,
   CosmosConstants.MetricNames.ChangeFeedLsnRange -> lsnMetric2,
   CosmosConstants.MetricNames.ChangeFeedItemsCnt -> itemsMetric2
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

  val unknownMetric3 = SQLMetrics.createMetric(spark.sparkContext, "unknown")
  unknownMetric3.set(10)

  val metrics = Map[String, SQLMetric](
   "unknownMetrics" -> unknownMetric3
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
