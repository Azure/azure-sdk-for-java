// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.guava25.base.MoreObjects.firstNonNull
import com.azure.cosmos.implementation.guava25.base.Strings.emptyToNull
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.TaskContext
import org.apache.spark.executor.TaskMetrics
import org.apache.spark.sql.execution.metric.SQLMetric
import org.apache.spark.util.AccumulatorV2

import java.lang.reflect.Method
import java.util.Locale
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import scala.collection.mutable.ArrayBuffer

class SparkInternalsBridge {
  // Only used in ChangeFeedMetricsListener, which is easier for test validation
  def getInternalCustomTaskMetricsAsSQLMetric(
                                              knownCosmosMetricNames: Set[String],
                                              taskMetrics: TaskMetrics) : Map[String, SQLMetric] = {
    SparkInternalsBridge.getInternalCustomTaskMetricsAsSQLMetricInternal(knownCosmosMetricNames, taskMetrics)
  }
}

object SparkInternalsBridge extends BasicLoggingTrait {
  private val SPARK_REFLECTION_ACCESS_ALLOWED_PROPERTY = "COSMOS.SPARK_REFLECTION_ACCESS_ALLOWED"
  private val SPARK_REFLECTION_ACCESS_ALLOWED_VARIABLE = "COSMOS_SPARK_REFLECTION_ACCESS_ALLOWED"

  private val DEFAULT_SPARK_REFLECTION_ACCESS_ALLOWED = true
  private val accumulatorsMethod : AtomicReference[Method] = new AtomicReference[Method]()

  private def getSparkReflectionAccessAllowed: Boolean = {
    val allowedText = System.getProperty(
      SPARK_REFLECTION_ACCESS_ALLOWED_PROPERTY,
      firstNonNull(
        emptyToNull(System.getenv.get(SPARK_REFLECTION_ACCESS_ALLOWED_VARIABLE)),
        String.valueOf(DEFAULT_SPARK_REFLECTION_ACCESS_ALLOWED)))

    try {
      java.lang.Boolean.valueOf(allowedText.toUpperCase(Locale.ROOT))
    }
    catch {
      case e: Exception =>
        logError(s"Parsing spark reflection access allowed $allowedText failed. Using the default $DEFAULT_SPARK_REFLECTION_ACCESS_ALLOWED.", e)
        DEFAULT_SPARK_REFLECTION_ACCESS_ALLOWED
    }
  }

  private final lazy val reflectionAccessAllowed = new AtomicBoolean(getSparkReflectionAccessAllowed)

  def getInternalCustomTaskMetricsAsSQLMetric(knownCosmosMetricNames: Set[String]): Map[String, SQLMetric] = {
    Option.apply(TaskContext.get()) match {
      case Some(taskCtx) => getInternalCustomTaskMetricsAsSQLMetricInternal(knownCosmosMetricNames, taskCtx.taskMetrics())
      case None => Map.empty[String, SQLMetric]
    }
  }

  def getInternalCustomTaskMetricsAsSQLMetric(knownCosmosMetricNames: Set[String], taskMetrics: TaskMetrics): Map[String, SQLMetric] = {

    if (!reflectionAccessAllowed.get) {
      Map.empty[String, SQLMetric]
    } else {
      getInternalCustomTaskMetricsAsSQLMetricInternal(knownCosmosMetricNames, taskMetrics)
    }
  }

  private def getAccumulators(taskMetrics: TaskMetrics): Option[ArrayBuffer[AccumulatorV2[_, _]]] = {
    try {
      val method = Option(accumulatorsMethod.get) match {
        case Some(existing) => existing
        case None =>
          val newMethod = taskMetrics.getClass.getMethod("externalAccums")
          newMethod.setAccessible(true)
          accumulatorsMethod.set(newMethod)
          newMethod
      }

      val accums = method.invoke(taskMetrics).asInstanceOf[ArrayBuffer[AccumulatorV2[_, _]]]

      Some(accums)
    } catch {
      case e: Exception =>
        logInfo(s"Could not invoke getAccumulators via reflection - Error ${e.getMessage}", e)

        // reflection failed - disabling it for the future
        reflectionAccessAllowed.set(false)
        None
    }
  }

  private def getInternalCustomTaskMetricsAsSQLMetricInternal(
                                                               knownCosmosMetricNames: Set[String],
                                                               taskMetrics: TaskMetrics): Map[String, SQLMetric] = {
    getAccumulators(taskMetrics) match {
      case Some(accumulators) => accumulators
        .filter(accumulable => accumulable.isInstanceOf[SQLMetric]
          && accumulable.name.isDefined
          && knownCosmosMetricNames.contains(accumulable.name.get))
        .map(accumulable => {
          val sqlMetric = accumulable.asInstanceOf[SQLMetric]
          sqlMetric.name.get -> sqlMetric
        })
        .toMap[String, SQLMetric]
      case None => Map.empty[String, SQLMetric]
    }
  }
}
