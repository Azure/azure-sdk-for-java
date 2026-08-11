// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosItemOperation

private[spark] class BulkWriterNoProgressException
(
  val message: String,
  val commitAttempt: Long,
  val activeBulkWriteOperations: Option[List[CosmosItemOperation]]) extends RuntimeException(message) {
}
