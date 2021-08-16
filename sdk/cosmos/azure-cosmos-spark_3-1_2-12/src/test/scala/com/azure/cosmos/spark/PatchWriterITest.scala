// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.UUID

// TODO: moderakh once BE support patch enable this, for now the code is parked
class PatchWriterITest extends IntegrationSpec with CosmosClient with AutoCleanableCosmosContainer {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  it should "support patch" ignore  {
    val container = cosmosClient.getDatabase(cosmosDatabase)
      .getContainer(cosmosContainer)

    val patchWriter = PatchWriter(container)

    val objectMapper = new ObjectMapper()

    val objectNode = objectMapper.createObjectNode()
    val id = UUID.randomUUID().toString

    objectNode.put("id", id)
//    container.createItem(objectNode).block()
    objectNode.put("sequence", "Fibonacci sequence")
    objectNode.put("firstElement", 1)
    objectNode.put("goldenRatio", 1.6180339887498948482)
    val partitionKeyValue = new PartitionKey(id)

//    container.createItem(objectNode).block()
    patchWriter.upsert(partitionKeyValue, objectNode)
    Thread.sleep(1000)
    val actual = container.readItem(id, partitionKeyValue, classOf[ObjectNode]).block().getItem
    actual.get("id").asText() shouldEqual id
    actual.get("sequence").asText() shouldEqual "Fibonacci sequence"
    actual.get("firstElement").asInt() shouldEqual 1
    actual.get("goldenRatio").asDouble() shouldEqual 1.6180339887498948482
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
