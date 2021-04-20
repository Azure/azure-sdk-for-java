// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationContext;

private[spark] case class SparkTaskContext(correlationActivityId: String, stageId: Int, partitionId: Long, details: String) extends OperationContext {

  @transient private lazy val cachedToString = {
    "SparkTaskContext(" +
      "correlationActivityId=" + correlationActivityId +
      ",stageId=" + stageId +
      ",partitionId=" + partitionId +
      ",details=" + details + ")"
  }

  override def getCorrelationActivityId: String = correlationActivityId

  override def toString: String = {
    cachedToString
  }
}
