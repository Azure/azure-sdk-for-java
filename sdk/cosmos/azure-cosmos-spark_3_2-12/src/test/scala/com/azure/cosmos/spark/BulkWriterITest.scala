// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, CosmosException}
import com.azure.cosmos.models.{CosmosContainerProperties, PartitionKey, ThroughputProperties, UniqueKey, UniqueKeyPolicy}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomUtils

import scala.collection.mutable

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import
import java.util.UUID

//scalastyle:off multiple.string.literals
//scalastyle:off magic.number
//scalastyle:off null
class BulkWriterITest extends IntegrationSpec with CosmosClient with AutoCleanableCosmosContainer {
  val objectMapper = new ObjectMapper()

  "Bulk Writer" can "upsert item" in  {
    val container = getContainer

    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, 5, bulkEnabled = true, bulkMaxPendingOperations = Some(900))

    val bulkWriter = new BulkWriter(container, writeConfig, DiagnosticsConfig(Option.empty))

    val items = mutable.Map[String, ObjectNode]()
    for(_ <- 0 until 5000) {
      val item = getItem(UUID.randomUUID().toString)
      val id = item.get("id").textValue()
      items += (id -> item)
      bulkWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    bulkWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
    }
  }

  "Bulk Writer" can "upsert item with empty id causing 400" in  {
    val container = getContainer

    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, 5, bulkEnabled = true, bulkMaxPendingOperations = Some(900))

    val bulkWriter = new BulkWriter(container, writeConfig, DiagnosticsConfig(Option.empty))

    val items = mutable.Map[String, ObjectNode]()
    val item = getItem("")
    val id = item.get("id").textValue()
    items += (id -> item)

    val thrown = intercept[CosmosException] {
      bulkWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)

      bulkWriter.flushAndClose()
    }

    thrown should not be null
    thrown.getStatusCode shouldEqual 400
    thrown.getSubStatusCode shouldEqual 0

    val allItems = readAllItems()

    allItems should have size 0
  }

  "Bulk Writer" can "upsert item with unique key violations throws 409" in  {
    val throughputProperties = ThroughputProperties.createManualThroughput(Defaults.DefaultContainerThroughput)
    val containerProperties = new CosmosContainerProperties(cosmosContainer + "withUK", "/pk")
    val uniqueKeys = new java.util.ArrayList[UniqueKey]()
    val paths = new java.util.ArrayList[String]()
    paths.add("/LogicalPartitionScopeUniqueColumn")
    uniqueKeys.add(new UniqueKey(paths))
    val uniqueKeyPolicy = new UniqueKeyPolicy()
      .setUniqueKeys(uniqueKeys)
    containerProperties.setUniqueKeyPolicy(uniqueKeyPolicy)
    val containerCreationResponse = cosmosClient
      .getDatabase(cosmosDatabase)
      .createContainerIfNotExists(containerProperties, throughputProperties).block()
    val container =
      cosmosClient.getDatabase(cosmosDatabase).getContainer(containerCreationResponse.getProperties.getId)

    try {
      val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, 5, bulkEnabled = true, bulkMaxPendingOperations = Some(900))

      val bulkWriter = new BulkWriter(container, writeConfig, DiagnosticsConfig(Option.empty))

      val onlyOnePartitionKeyValue = UUID.randomUUID().toString
      val duplicateUniqueKeyValue = UUID.randomUUID().toString
      val items = mutable.Map[String, ObjectNode]()
      val item1 = getItem(UUID.randomUUID().toString)
      item1.put("pk", onlyOnePartitionKeyValue)
      item1.put("LogicalPartitionScopeUniqueColumn", duplicateUniqueKeyValue)
      val id1 = item1.get("id").textValue()
      items += (id1 -> item1)

      val item2 = getItem(UUID.randomUUID().toString)
      item2.put("pk", onlyOnePartitionKeyValue)
      item2.put("LogicalPartitionScopeUniqueColumn", duplicateUniqueKeyValue)
      val id2 = item2.get("id").textValue()
      items += (id2 -> item2)

      val thrown = intercept[CosmosException] {
        bulkWriter.scheduleWrite(new PartitionKey(onlyOnePartitionKeyValue), item1)
        bulkWriter.scheduleWrite(new PartitionKey(onlyOnePartitionKeyValue), item2)

        bulkWriter.flushAndClose()
      }

      thrown should not be null
      thrown.getStatusCode shouldEqual 409
      thrown.getSubStatusCode shouldEqual 0

      val allItems = container
        .queryItems("SELECT * FROM r", classOf[ObjectNode])
        .toIterable
        .asScala
        .toList

      allItems.size < 2 shouldEqual true
    } finally {
      container.delete().block()
    }
  }

  "Bulk Writer" can "append item with unique key violations succeeds and ignores 409" in  {
    val throughputProperties = ThroughputProperties.createManualThroughput(Defaults.DefaultContainerThroughput)
    val containerProperties = new CosmosContainerProperties(cosmosContainer + "withUK", "/pk")
    val uniqueKeys = new java.util.ArrayList[UniqueKey]()
    val paths = new java.util.ArrayList[String]()
    paths.add("/LogicalPartitionScopeUniqueColumn")
    uniqueKeys.add(new UniqueKey(paths))
    val uniqueKeyPolicy = new UniqueKeyPolicy()
      .setUniqueKeys(uniqueKeys)
    containerProperties.setUniqueKeyPolicy(uniqueKeyPolicy)
    val containerCreationResponse = cosmosClient
      .getDatabase(cosmosDatabase)
      .createContainerIfNotExists(containerProperties, throughputProperties).block()
    val container =
      cosmosClient.getDatabase(cosmosDatabase).getContainer(containerCreationResponse.getProperties.getId)

    try {
      val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemAppend, 5, bulkEnabled = true, bulkMaxPendingOperations = Some(900))

      val bulkWriter = new BulkWriter(container, writeConfig, DiagnosticsConfig(Option.empty))

      val onlyOnePartitionKeyValue = UUID.randomUUID().toString
      val duplicateUniqueKeyValue = UUID.randomUUID().toString
      val items = mutable.Map[String, ObjectNode]()
      val item1 = getItem(UUID.randomUUID().toString)
      item1.put("pk", onlyOnePartitionKeyValue)
      item1.put("LogicalPartitionScopeUniqueColumn", duplicateUniqueKeyValue)
      val id1 = item1.get("id").textValue()
      items += (id1 -> item1)

      val item2 = getItem(UUID.randomUUID().toString)
      item2.put("pk", onlyOnePartitionKeyValue)
      item2.put("LogicalPartitionScopeUniqueColumn", duplicateUniqueKeyValue)
      val id2 = item2.get("id").textValue()
      items += (id2 -> item2)

      bulkWriter.scheduleWrite(new PartitionKey(onlyOnePartitionKeyValue), item1)
      bulkWriter.scheduleWrite(new PartitionKey(onlyOnePartitionKeyValue), item2)

      bulkWriter.flushAndClose()

      val allItems = container
        .queryItems("SELECT * FROM r", classOf[ObjectNode])
        .toIterable
        .asScala
        .toList

      // Only one record should have been persisted successfully
      // the other one got 409 due to the unique key constraint validation
      // and with ItemAppend 409 should get ignored
      allItems should have size 1
    } finally {
      container.delete().block()
    }
  }

  "Bulk Writer" can "delete items" in  {
    val container = getContainer

    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, 5, bulkEnabled = true, bulkMaxPendingOperations = Some(900))

    val bulkWriter = new BulkWriter(container, writeConfig, DiagnosticsConfig(Option.empty))

    val items = mutable.Map[String, ObjectNode]()
    for(_ <- 0 until 5000) {
      val item = getItem(UUID.randomUUID().toString)
      val id = item.get("id").textValue()
      items += (id -> item)
      bulkWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    bulkWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
    }

    val deleteConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemDelete,
      5,
      bulkEnabled = true,
      bulkMaxPendingOperations = Some(900))

    val bulkDeleter = new BulkWriter(container, deleteConfig, DiagnosticsConfig(Option.empty))

    for(i <- 0 until 5000) {
      val item = allItems(i)
      bulkDeleter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    bulkDeleter.flushAndClose()
    val allItemsAfterDelete = readAllItems()

    allItemsAfterDelete should have size 0
  }

  "Bulk Writer" can "delete only unmodified items" in  {
    val container = getContainer

    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, 5, bulkEnabled = true, bulkMaxPendingOperations = Some(900))

    val bulkWriter = new BulkWriter(container, writeConfig, DiagnosticsConfig(Option.empty))

    val items = mutable.Map[String, ObjectNode]()
    for(_ <- 0 until 5000) {
      val item = getItem(UUID.randomUUID().toString)
      val id = item.get("id").textValue()
      items += (id -> item)
      bulkWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    bulkWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
    }

    val bulkUpdater = new BulkWriter(container, writeConfig, DiagnosticsConfig(Option.empty))

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
    }

    for(i <- 0 until 10) {
      val item = allItems(i)
      item.put("propString", UUID.randomUUID().toString)
      val id = item.get("id").textValue()
      items.put(id, item)
      bulkUpdater.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    bulkUpdater.flushAndClose()
    val allItemsAfterUpdate = readAllItems()

    allItemsAfterUpdate should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
    }

    val deleteConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemDeleteIfNotModified,
      5,
      bulkEnabled = true,
      bulkMaxPendingOperations = Some(900))

    val bulkDeleter = new BulkWriter(container, deleteConfig, DiagnosticsConfig(Option.empty))

    for(i <- 0 until 5000) {
      val item = allItems(i)
      bulkDeleter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    bulkDeleter.flushAndClose()
    val allItemsAfterDelete = readAllItems()

    allItemsAfterDelete should have size 10
  }

  "Bulk Writer" can "create item with duplicates" in {
    val container = getContainer
    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemAppend, maxRetryCount = 5, bulkEnabled = true, Some(900))
    val bulkWriter = new BulkWriter(container, writeConfig, DiagnosticsConfig(Option.empty))
    val items = new mutable.HashMap[String, mutable.Set[ObjectNode]] with mutable.MultiMap[String, ObjectNode]

    for(i <- 0 until 5000) {
      val item = getItem((i % 100).toString)
      val id = item.get("id").textValue()
      items.addBinding(id, item)
      bulkWriter.scheduleWrite(new PartitionKey(id), item)
    }

    bulkWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val itemsWithSameIdList = items(itemFromDB.get("id").textValue())
      itemsWithSameIdList.toStream.exists(itemOriginallyAttemptedToBeInserted =>
        secondObjectNodeHasAllFieldsOfFirstObjectNode(itemOriginallyAttemptedToBeInserted, itemFromDB)) shouldEqual true
    }
  }

  private def getItem(id: String): ObjectNode = {
    val objectNode = objectMapper.createObjectNode()
    objectNode.put("id", id)
    objectNode.put("propString", UUID.randomUUID().toString)
    objectNode.put("propInt", RandomUtils.nextInt())
    objectNode.put("propBoolean", RandomUtils.nextBoolean())

    objectNode
  }

  def secondObjectNodeHasAllFieldsOfFirstObjectNode(originalInsertedItem: ObjectNode, itemReadFromDatabase: ObjectNode): Boolean = {
    !originalInsertedItem.fields().asScala.exists(expectedField => {
      itemReadFromDatabase.get(expectedField.getKey) == null ||
        !itemReadFromDatabase.get(expectedField.getKey).equals(expectedField.getValue)
    })
  }

  private def getContainer: CosmosAsyncContainer = {
    cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
  }
}
//scalastyle:on multiple.string.literals
//scalastyle:on magic.number
//scalastyle:on null
