// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package org.apache.spark

object SparkInternalsBridge {

  val NUM_ROWS_PER_UPDATE = 100

  def updateInternalTaskMetrics(recordsWrittenSnapshot: Long, bytesWrittenSnapshot: Long): Unit = {
    Option(TaskContext.get()) match {
      case Some(taskContext) =>
        val outputMetrics = taskContext.taskMetrics.outputMetrics
        outputMetrics.setRecordsWritten(recordsWrittenSnapshot)
        outputMetrics.setBytesWritten(bytesWrittenSnapshot)
      case None =>
    }
  }
}
