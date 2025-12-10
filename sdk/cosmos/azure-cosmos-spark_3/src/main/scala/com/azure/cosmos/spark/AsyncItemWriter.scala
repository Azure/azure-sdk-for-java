// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKey
import com.fasterxml.jackson.databind.node.ObjectNode

private  trait AsyncItemWriter {
  /**
    * Schedule a write to happen in async and return immediately
    * @param partitionKeyValue the partition key value
    * @param objectNode the json object node
    */
  def scheduleWrite(partitionKeyValue: PartitionKey, objectNode: ObjectNode): Unit

  /**
    * Wait for all remaining work
    * Throws if any of the work resulted in failure
    */
  def flushAndClose(): Unit

  /**
   * Don't wait for any remaining work but signal to the writer the ungraceful close
   * Should not throw any exceptions
   */
  def abort(shouldThrow: Boolean): Unit

  private[spark] def getETag(objectNode: ObjectNode) = {
    val eTagField = objectNode.get(CosmosConstants.Properties.ETag)
    if (eTagField != null && eTagField.isTextual) {
      Some(eTagField.textValue())
    } else {
      None
    }
  }
}
