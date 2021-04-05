// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, InternalObjectNode, RxDocumentClientImpl}
import com.azure.cosmos.models.{PartitionKey, PartitionKeyDefinition}
import com.fasterxml.jackson.databind.node.ObjectNode

private object PartitionKeyHelper {
  def getPartitionKeyPath(objectNode: ObjectNode,
                          partitionKeyDefinition: PartitionKeyDefinition): PartitionKey = {
    val partitionKeyInternal = RxDocumentClientImpl
      .extractPartitionKeyValueFromDocument(
        new InternalObjectNode(objectNode),
        partitionKeyDefinition)
    ImplementationBridgeHelpers
      .PartitionKeyHelper
      .getPartitionKeyAccessor
      .toPartitionKey(partitionKeyInternal)
  }
}
