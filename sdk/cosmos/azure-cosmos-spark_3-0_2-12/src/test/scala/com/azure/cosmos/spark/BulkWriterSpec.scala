// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.models.PartitionKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomUtils
import scala.collection.mutable.Map

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import
import java.util.UUID

//scalastyle:off multiple.string.literals
class BulkWriterSpec extends IntegrationSpec with CosmosClient with AutoCleanableCosmosContainer {
  val objectMapper = new ObjectMapper()

  "Bulk Writer" can "upsert item" taggedAs (RequiresCosmosEndpoint) in {
    val container = getContainer()

    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, maxRetryCount = 0, bulkEnabled = true)

    val bulkWriter = BulkWriter(container, writeConfig)

    val items = Map[String, ObjectNode]()
    for(i <- 0 until 100) {
      val item = getItem()
      val id = item.get("id").textValue()
      items += (id -> item)
      bulkWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    bulkWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items.get(itemFromDB.get("id").textValue()).get

      for (expectedField <- expectedItem.fields().asScala) {
        itemFromDB.get(expectedField.getKey).equals(expectedField.getValue)
      }
    }
  }

  private def getItem(): ObjectNode = {
    val objectNode = objectMapper.createObjectNode()
    objectNode.put("id", UUID.randomUUID().toString)

    objectNode.put("propString", UUID.randomUUID().toString)
    objectNode.put("propInt", RandomUtils.nextInt())
    objectNode.put("propBoolean", RandomUtils.nextBoolean())

    objectNode
  }

  private def getContainer(): CosmosAsyncContainer = {
    cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
  }
}
//scalastyle:on multiple.string.literals
