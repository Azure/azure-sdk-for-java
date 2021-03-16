// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKey
import com.fasterxml.jackson.databind.node.ObjectNode

trait AsyncItemWriter {
  /**
    * Schedule a write to happen in async and return immediately
    * @param partitionKeyValue
    * @param objectNode
    */
  def scheduleWrite(partitionKeyValue: PartitionKey, objectNode: ObjectNode)

  /**
    * Wait for all remaining work
    * Throws if any of the work resulted in failure
    */
  def flushAndClose()
}
