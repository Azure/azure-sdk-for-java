// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.InternalObjectNode
import com.azure.cosmos.models.{PartitionKey, PartitionKeyDefinition}
import com.fasterxml.jackson.databind.node.ObjectNode

private object PartitionKeyHelper {
  def getPartitionKeyPath(objectNode: ObjectNode,
                          partitionKeyDefinition: PartitionKeyDefinition): PartitionKey = {
      com.azure.cosmos.implementation.PartitionKeyHelper.extractPartitionKeyFromDocument(
          new InternalObjectNode(objectNode),
          partitionKeyDefinition)
  }
}
