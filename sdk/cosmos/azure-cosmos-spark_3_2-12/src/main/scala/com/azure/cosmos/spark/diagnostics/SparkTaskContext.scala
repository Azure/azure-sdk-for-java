// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationContext
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull

import java.util.UUID;

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

private[spark] case class ReplaceOperation(sparkTaskContext: SparkTaskContext, itemIdentifier: CosmosItemIdentifier)
  extends WriteOperation

private[spark] case class PatchOperation(sparkTaskContext: SparkTaskContext, itemIdentifier: CosmosItemIdentifier)
 extends WriteOperation

private[spark] case class SparkTaskContext(correlationActivityId: UUID,
                                           stageId: Int,
                                           partitionId: Long,
                                           taskAttemptId: Long,
                                           details: String) extends OperationContext {

  requireNotNull(correlationActivityId, "correlationActivityId")

  private val correlationActivityIdAsString = correlationActivityId.toString

  @transient private lazy val cachedToString = {
    "SparkTaskContext(" +
      "correlationActivityId=" + correlationActivityIdAsString +
      ",stageId=" + stageId +
      ",partitionId=" + partitionId +
      ",taskAttemptId=" + taskAttemptId +
      ",details=" + details + ")"
  }

  override def getCorrelationActivityId: String = correlationActivityIdAsString

  override def toString: String = {
    cachedToString
  }
}
