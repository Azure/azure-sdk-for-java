// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package org.apache.spark

import org.apache.spark.sql.execution.metric.SQLMetric

object SparkInternalsBridge {
  def getInternalCustomTaskMetricsAsSQLMetric(knownCosmosMetricNames: Set[String]) : Map[String, SQLMetric] = {
    Option.apply(TaskContext.get()) match {
      case Some(taskCtx) => taskCtx
        .taskMetrics()
        .accumulators()
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
