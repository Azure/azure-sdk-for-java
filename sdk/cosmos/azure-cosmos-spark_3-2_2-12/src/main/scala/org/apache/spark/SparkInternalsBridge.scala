// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package org.apache.spark

import org.apache.spark.sql.connector.metric.CustomTaskMetric
import org.apache.spark.sql.execution.metric.SQLMetric

object SparkInternalsBridge {

  val NUM_ROWS_PER_UPDATE = 100

  private val BUILTIN_OUTPUT_METRICS = Set("bytesWritten", "recordsWritten")

  def getInternalCustomTaskMetricsAsSQLMetric(knownCosmosMetricNames: Set[String]) : Map[String, SQLMetric] = {
    Option.apply(TaskContext.get()) match {
      case Some(taskCtx) => taskCtx
        .taskMetrics()
        .externalAccums
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
    currentMetricsValues.foreach { metric =>
      val metricName = metric.name()
      val metricValue = metric.value()

      if (BUILTIN_OUTPUT_METRICS.contains(metricName)) {
        Option(TaskContext.get()).map(_.taskMetrics().outputMetrics).foreach { outputMetrics =>
          metricName match {
            case "bytesWritten" => outputMetrics.setBytesWritten(metricValue)
            case "recordsWritten" => outputMetrics.setRecordsWritten(metricValue)
            case _ => // no-op
          }
        }
      }
    }
  }
}
