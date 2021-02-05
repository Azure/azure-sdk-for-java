// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.{CosmosAsyncContainer, CosmosPatchOperations, TransactionalBatch}
import com.fasterxml.jackson.databind.node.{JsonNodeType, ObjectNode}

// TODO: this is unused at this point, parked code till patch is ready on the service sidePatchWriter
private case class PatchWriter(cosmosContainer: CosmosAsyncContainer) {

  // scalastyle:off cyclomatic.null
  // scalastyle:off cyclomatic.complexity
  private def objectNodeToPatch(objectNode: ObjectNode) : CosmosPatchOperations = {
    val cosmosPatchOperations = CosmosPatchOperations.create()
    val it = objectNode.fields()
    while (it.hasNext) {
      val child = it.next()

      val path = "/" + child.getKey
      val jsonNode = child.getValue

      // TODO: moderakh support nested object
      assert(child.getValue.isValueNode)
      child.getValue.getNodeType match {
        case JsonNodeType.ARRAY => throw new UnsupportedOperationException
        case JsonNodeType.BINARY => throw new UnsupportedOperationException
        case JsonNodeType.ARRAY => throw new UnsupportedOperationException
        case JsonNodeType.BOOLEAN =>cosmosPatchOperations.set(path, jsonNode.asBoolean())
        case JsonNodeType.MISSING => throw new UnsupportedOperationException
        case JsonNodeType.NULL => throw new UnsupportedOperationException
        case JsonNodeType.NUMBER => cosmosPatchOperations.set(path, jsonNode.asDouble())
        case JsonNodeType.OBJECT => throw new UnsupportedOperationException
        case JsonNodeType.POJO => throw new UnsupportedOperationException
        case JsonNodeType.STRING => cosmosPatchOperations.set(path, jsonNode.asText())
      }
    }
    cosmosPatchOperations
  }
  // scalastyle:on cyclomatic.null
  // scalastyle:on cyclomatic.complexity

  def upsert(partitionKeyValue: PartitionKey, objectNode: ObjectNode) : Unit = {
    val patchOperations = objectNodeToPatch(objectNode)

    val batch = TransactionalBatch.createTransactionalBatch(partitionKeyValue)
    batch.patchItemOperation(objectNode.get("id").asText(), patchOperations)

    val result = cosmosContainer.executeTransactionalBatch(batch).block()
    result.getStatusCode
  }
}
