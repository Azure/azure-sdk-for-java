// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.guava25.base.MoreObjects.firstNonNull
import com.azure.cosmos.implementation.guava25.base.Strings.emptyToNull
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.TaskContext

import java.lang.reflect.Method
import java.util.Locale
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

//scalastyle:off multiple.string.literals
object SparkInternalsBridge extends BasicLoggingTrait {
  private val SPARK_REFLECTION_ACCESS_ALLOWED_PROPERTY = "COSMOS.SPARK_REFLECTION_ACCESS_ALLOWED"
  private val SPARK_REFLECTION_ACCESS_ALLOWED_VARIABLE = "COSMOS_SPARK_REFLECTION_ACCESS_ALLOWED"

  private val DEFAULT_SPARK_REFLECTION_ACCESS_ALLOWED = true

  val NUM_ROWS_PER_UPDATE = 100
  private val outputMetricsMethod: AtomicReference[Method] = new AtomicReference[Method]()
  private val setBytesWrittenMethod: AtomicReference[Method] = new AtomicReference[Method]()
  private val setRecordsWrittenMethod: AtomicReference[Method] = new AtomicReference[Method]()

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

  def updateInternalTaskMetrics(recordsWrittenSnapshot: Long, bytesWrittenSnapshot: Long): Unit = {
    if (reflectionAccessAllowed.get) {
      Option(TaskContext.get()) match {
        case Some(taskContext) =>
          getOutputMetrics(taskContext) match {
            case Some(outputMetrics) =>
              setRecordsWritten(outputMetrics, recordsWrittenSnapshot.asInstanceOf[Object])
              setBytesWritten(outputMetrics, bytesWrittenSnapshot.asInstanceOf[Object])
            case None =>
          }
      }
    }
  }
}
//scalastyle:on multiple.string.literals
