// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.guava25.base.MoreObjects.firstNonNull
import com.azure.cosmos.implementation.guava25.base.Strings.emptyToNull
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.TaskContext
import org.apache.spark.sql.execution.metric.SQLMetric
import org.apache.spark.util.AccumulatorV2

import java.lang.reflect.Method
import java.util.Locale
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

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

  def getInternalCustomTaskMetricsAsSQLMetric(knownCosmosMetricNames: Set[String]) : Map[String, SQLMetric] = {

    if (!reflectionAccessAllowed.get) {
      Map.empty[String, SQLMetric]
    } else {
      Option.apply(TaskContext.get()) match {
        case Some(taskCtx) => getInternalCustomTaskMetricsAsSQLMetricInternal(knownCosmosMetricNames, taskCtx)
        case None => Map.empty[String, SQLMetric]
      }
    }
  }

  private def getAccumulators(taskCtx: TaskContext): Option[Seq[AccumulatorV2[_, _]]] = {
    try {
      val taskMetrics: Object = taskCtx.taskMetrics()
      val method = Option(accumulatorsMethod.get) match {
        case Some(existing) => existing
        case None =>
          val newMethod = taskMetrics.getClass.getMethod("accumulators")
          newMethod.setAccessible(true)
          accumulatorsMethod.set(newMethod)
          newMethod
      }

      val accums = method.invoke(taskMetrics).asInstanceOf[Seq[AccumulatorV2[_, _]]]

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
                                                               taskCtx: TaskContext): Map[String, SQLMetric] = {
    getAccumulators(taskCtx) match {
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
