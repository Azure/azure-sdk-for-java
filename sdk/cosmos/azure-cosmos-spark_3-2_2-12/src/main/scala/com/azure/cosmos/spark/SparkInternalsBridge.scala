// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.guava25.base.MoreObjects.firstNonNull
import com.azure.cosmos.implementation.guava25.base.Strings.emptyToNull
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.TaskContext
import org.apache.spark.sql.connector.metric.CustomTaskMetric
import org.apache.spark.sql.execution.metric.SQLMetric
import org.apache.spark.util.AccumulatorV2

import java.lang.reflect.Method
import java.util.Locale
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import scala.collection.mutable.ArrayBuffer

//scalastyle:off multiple.string.literals
object SparkInternalsBridge extends BasicLoggingTrait {
  private val SPARK_REFLECTION_ACCESS_ALLOWED_PROPERTY = "COSMOS.SPARK_REFLECTION_ACCESS_ALLOWED"
  private val SPARK_REFLECTION_ACCESS_ALLOWED_VARIABLE = "COSMOS_SPARK_REFLECTION_ACCESS_ALLOWED"

  private val DEFAULT_SPARK_REFLECTION_ACCESS_ALLOWED = true
  val NUM_ROWS_PER_UPDATE = 100

  private val BUILTIN_OUTPUT_METRICS = Set("bytesWritten", "recordsWritten")

  private val accumulatorsMethod : AtomicReference[Method] = new AtomicReference[Method]()
  private val outputMetricsMethod : AtomicReference[Method] = new AtomicReference[Method]()
  private val setBytesWrittenMethod : AtomicReference[Method] = new AtomicReference[Method]()
  private val setRecordsWrittenMethod : AtomicReference[Method] = new AtomicReference[Method]()

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
    if (!reflectionAccessAllowed.get) {
      Map.empty[String, SQLMetric]
    } else {
      Option.apply(TaskContext.get()) match {
        case Some(taskCtx) => getInternalCustomTaskMetricsAsSQLMetricInternal(knownCosmosMetricNames, taskCtx)
        case None => Map.empty[String, SQLMetric]
      }
    }
  }

  private def getAccumulators(taskCtx: TaskContext): Option[ArrayBuffer[AccumulatorV2[_, _]]] = {
    try {
      val taskMetrics: Object = taskCtx.taskMetrics()
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

  private def getOutputMetrics(taskCtx: TaskContext): Option[Object] = {
    try {
      val taskMetrics: Object = taskCtx.taskMetrics()

      val method = Option(outputMetricsMethod.get) match {
        case Some(existing) => existing
        case None =>
          val newMethod = taskMetrics.getClass.getMethod("outputMetrics")
          newMethod.setAccessible(true)
          outputMetricsMethod.set(newMethod)
          newMethod
      }

      val outputMetrics = method.invoke(taskMetrics)
      Option(outputMetrics)
    } catch {
      case e: Exception =>
        logInfo(s"Could not invoke getOutputMetrics via reflection - Error ${e.getMessage}", e)

        // reflection failed - disabling it for the future
        reflectionAccessAllowed.set(false)
        None
    }
  }

  private def setBytesWritten(outputMetrics: Object, metricValue: Object): Unit = {
    try {
      val method = Option(setBytesWrittenMethod.get) match {
        case Some(existing) => existing
        case None =>
          val newMethod = outputMetrics.getClass.getMethod("setBytesWritten", java.lang.Long.TYPE)
          newMethod.setAccessible(true)
          setBytesWrittenMethod.set(newMethod)
          newMethod
      }

      method.invoke(outputMetrics, metricValue)
    } catch {
      case e: Exception =>
        logInfo(s"Could not invoke setBytesWritten via reflection - Error ${e.getMessage}", e)

        // reflection failed - disabling it for the future
        reflectionAccessAllowed.set(false)
    }
  }

  private def setRecordsWritten(outputMetrics: Object, metricValue: Object): Unit = {
    try {
      val method = Option(setRecordsWrittenMethod.get) match {
        case Some(existing) => existing
        case None =>
          val newMethod = outputMetrics.getClass.getMethod("setRecordsWritten", java.lang.Long.TYPE)
          newMethod.setAccessible(true)
          setRecordsWrittenMethod.set(newMethod)
          newMethod
      }
      method.invoke(outputMetrics, metricValue)
    } catch {
      case e: Exception =>
        logInfo(s"Could not invoke setRecordsWritten via reflection - Error ${e.getMessage}", e)

        // reflection failed - disabling it for the future
        reflectionAccessAllowed.set(false)
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

  def updateInternalTaskMetrics(currentMetricsValues: Seq[CustomTaskMetric]): Unit = {
    if (reflectionAccessAllowed.get) {
      currentMetricsValues.foreach { metric =>
        val metricName = metric.name()
        val metricValue = metric.value()

        if (BUILTIN_OUTPUT_METRICS.contains(metricName)) {
          Option(TaskContext.get()).map(getOutputMetrics).foreach { outputMetricsOption =>

            outputMetricsOption match {
              case Some(outputMetrics) =>

                metricName match {
                  case "bytesWritten" => setBytesWritten(outputMetrics, metricValue.asInstanceOf[Object])
                  case "recordsWritten" => setRecordsWritten(outputMetrics, metricValue.asInstanceOf[Object])
                  case _ => // no-op
                }
              case None =>
            }
          }
        }
      }
    }
  }
}
//scalastyle:on multiple.string.literals
