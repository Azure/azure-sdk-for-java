// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics;


import com.azure.cosmos.implementation.spark.OperationContext;

import java.io.Serializable;

case class SparkOperationContext(correlationActivityId: String, stageId: Int, partitionId: Long, details: String) extends OperationContext {
  override def getCorrelationActivityId: String = correlationActivityId
}