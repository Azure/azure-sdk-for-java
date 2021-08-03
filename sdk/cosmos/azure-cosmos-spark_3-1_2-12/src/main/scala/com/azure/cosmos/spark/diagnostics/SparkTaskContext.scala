// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationContext;

private[spark] trait WriteOperation {
  def sparkTaskContext : SparkTaskContext
  def itemIdentifier: CosmosItemIdentifier
}

private[spark] case class UpsertOperation(sparkTaskContext: SparkTaskContext, itemIdentifier: CosmosItemIdentifier)
  extends WriteOperation

private[spark] case class CreateOperation(sparkTaskContext: SparkTaskContext, itemIdentifier: CosmosItemIdentifier)
  extends WriteOperation

private[spark] case class DeleteOperation(sparkTaskContext: SparkTaskContext, itemIdentifier: CosmosItemIdentifier)
  extends WriteOperation

private[spark] case class SparkTaskContext(correlationActivityId: String,
                                           stageId: Int,
                                           partitionId: Long,
                                           taskAttemptId: Long,
                                           details: String) extends OperationContext {

  @transient private lazy val cachedToString = {
    "SparkTaskContext(" +
      "correlationActivityId=" + correlationActivityId +
      ",stageId=" + stageId +
      ",partitionId=" + partitionId +
      ",taskAttemptId=" + taskAttemptId +
      ",details=" + details + ")"
  }

  override def getCorrelationActivityId: String = correlationActivityId

  override def toString: String = {
    cachedToString
  }
}
