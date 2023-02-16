// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils
import com.azure.cosmos.{CosmosAsyncContainer, CosmosException}
import com.azure.cosmos.models.{CosmosContainerProperties, PartitionKey, ThroughputProperties}
import com.azure.cosmos.spark.utils.CosmosPatchTestHelper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomUtils
import org.apache.spark.MockTaskContext
import org.apache.spark.sql.types.{BooleanType, DoubleType, FloatType, IntegerType, LongType, StringType, StructField, StructType}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import
import java.util.UUID

//scalastyle:off multiple.string.literals
//scalastyle:off magic.number
class PointWriterITest extends IntegrationSpec with CosmosClient with AutoCleanableCosmosContainer {
  val objectMapper = new ObjectMapper()

  "Point Writer" can "upsert item" in  {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition

    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, maxRetryCount = 3, bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    val items = mutable.Map[String, ObjectNode]()
    for(_ <- 0 until 5000) {
      val item = getItem(UUID.randomUUID().toString)
      val id = item.get("id").textValue()
      items += (id -> item)
      pointWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    pointWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
    }
  }

  "Point Writer" can "delete items" in  {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition

    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, maxRetryCount = 3, bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    val items = mutable.Map[String, ObjectNode]()
    for(_ <- 0 until 5000) {
      val item = getItem(UUID.randomUUID().toString)
      val id = item.get("id").textValue()
      items += (id -> item)
      pointWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    pointWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
    }

    val deleteConfig = CosmosWriteConfig(ItemWriteStrategy.ItemDelete, maxRetryCount = 3, bulkEnabled = false)

    val pointDeleter = new PointWriter(
      container, partitionKeyDefinition, deleteConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    for(i <- 0 until 5000) {
      val item = allItems(i)
      pointDeleter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    pointDeleter.flushAndClose()
    val allItemsAfterDelete = readAllItems()

    allItemsAfterDelete should have size 0
  }

  "Point Writer" can "delete only unmodified items" in  {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition

    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemOverwrite, maxRetryCount = 3, bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    val items = mutable.Map[String, ObjectNode]()
    for(_ <- 0 until 5000) {
      val item = getItem(UUID.randomUUID().toString)
      val id = item.get("id").textValue()
      items += (id -> item)
      pointWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    pointWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    val pointUpdater = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

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
      pointUpdater.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    pointUpdater.flushAndClose()
    val allItemsAfterUpdate = readAllItems()

    allItemsAfterUpdate should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
    }

    val deleteConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemDeleteIfNotModified,
      maxRetryCount = 3,
      bulkEnabled = false)

    val pointDeleter = new PointWriter(
      container, partitionKeyDefinition, deleteConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    for(i <- 0 until 5000) {
      val item = allItems(i)
      pointDeleter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    pointDeleter.flushAndClose()
    val allItemsAfterDelete = readAllItems()

    allItemsAfterDelete should have size 10
  }

  "Point Writer" can "create item with duplicates" in {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
    val writeConfig = CosmosWriteConfig(ItemWriteStrategy.ItemAppend, maxRetryCount = 0, bulkEnabled = false)
    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())
    val items = new mutable.HashMap[String, mutable.Set[ObjectNode]] with mutable.MultiMap[String, ObjectNode]

    for(i <- 0 until 5000) {
      val item = getItem((i % 100).toString)
      val id = item.get("id").textValue()
      items.addBinding(id, item)
      pointWriter.scheduleWrite(new PartitionKey(id), item)
    }

    pointWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val itemsWithSameIdList = items(itemFromDB.get("id").textValue())
      itemsWithSameIdList.toStream.exists(itemOriginallyAttemptedToBeInserted =>
        secondObjectNodeHasAllFieldsOfFirstObjectNode(itemOriginallyAttemptedToBeInserted, itemFromDB)) shouldEqual true
    }
  }

  "Point Writer" can "upsert items if not modified" in  {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition

    val writeConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemOverwriteIfNotModified, maxRetryCount = 3, bulkEnabled = false)

    var pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    val items = mutable.Map[String, ObjectNode]()
    for(_ <- 0 until 5000) {
      val item = getItem(UUID.randomUUID().toString)
      val id = item.get("id").textValue()
      items += (id -> item)
      pointWriter.scheduleWrite(new PartitionKey(item.get("id").textValue()), item)
    }

    pointWriter.flushAndClose()
    val allItems = readAllItems()

    allItems should have size items.size

    pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    val secondWriteId = UUID.randomUUID().toString
    // now modify the items read back from DB (so they have etag)
    // subsequent write operation should update all of them
    for(itemFromDB <- allItems) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
      itemFromDB.put("secondWriteId", secondWriteId)
      pointWriter.scheduleWrite(new PartitionKey(itemFromDB.get("id").textValue()), itemFromDB)
    }

    pointWriter.flushAndClose()
    val allItemsAfterSecondWrite = readAllItems()
    allItemsAfterSecondWrite should have size items.size

    val expectedItemsAfterSecondWrite = mutable.Map[String, ObjectNode]()
    for(itemFromDB <- allItemsAfterSecondWrite) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = items(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
      itemFromDB.get("secondWriteId").asText shouldEqual secondWriteId
      expectedItemsAfterSecondWrite.put(itemFromDB.get("id").textValue(), itemFromDB)
    }

    val thirdWriteId = UUID.randomUUID().toString
    pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())
    // now modify the items read back from DB after the first write
    // (so they have stale etag) and modify them
    // subsequent write operation should update none of them because all etags are stale
    for(itemFromDB <- allItems) {
      itemFromDB.put("thirdWriteId", thirdWriteId)
      pointWriter.scheduleWrite(new PartitionKey(itemFromDB.get("id").textValue()), itemFromDB)
    }

    pointWriter.flushAndClose()

    val allItemsAfterThirdWrite = readAllItems()
    allItemsAfterThirdWrite should have size items.size

    for(itemFromDB <- allItemsAfterThirdWrite) {
      items.contains(itemFromDB.get("id").textValue()) shouldBe true
      val expectedItem = expectedItemsAfterSecondWrite(itemFromDB.get("id").textValue())
      secondObjectNodeHasAllFieldsOfFirstObjectNode(expectedItem, itemFromDB) shouldEqual true
      itemFromDB.get("secondWriteId").asText shouldEqual secondWriteId
      itemFromDB.get("thirdWriteId") shouldEqual null
    }
  }

  "Point Writer" can "partial update item with simple types" in {
    val partialUpdateSchema = StructType(Seq(
      StructField("propInt", IntegerType),
      StructField("propLong", LongType),
      StructField("propFloat", FloatType),
      StructField("propDouble", DoubleType),
      StructField("propBoolean", BooleanType),
      StructField("propString", StringType),
    ))

    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
    val strippedPartitionKeyPath = CosmosPatchTestHelper.getStrippedPartitionKeyPath(partitionKeyDefinition)
    val writeConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemOverwrite,
      5,
      bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    // First create one item, as patch can only operate on existing items
    val itemWithFullSchema = CosmosPatchTestHelper.getPatchItemWithFullSchema(UUID.randomUUID().toString, strippedPartitionKeyPath)
    val id = itemWithFullSchema.get("id").textValue()
    val partitionKey = new PartitionKey(itemWithFullSchema.get(strippedPartitionKeyPath).textValue())

    pointWriter.scheduleWrite(partitionKey, itemWithFullSchema)
    pointWriter.flushAndClose()
    // make sure the item exists
    container.readItem(id, partitionKey, classOf[ObjectNode]).block()

    // Test for each cosmos patch operation type, ignore increment type for as there will be a separate test for it
    CosmosPatchOperationTypes.values.foreach(operationType => {
      operationType match {
        case CosmosPatchOperationTypes.Increment => // no-op
        case _ =>
          // get the latest status of the item
          val originalItem: ObjectNode = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()
          val patchPartialUpdateItem =
            CosmosPatchTestHelper.getPatchItemWithSchema(
              strippedPartitionKeyPath,
              partialUpdateSchema,
              originalItem)

          val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
          patchPartialUpdateItem.fields().asScala.foreach(field => {
            columnConfigsMap += field.getKey -> CosmosPatchColumnConfig(
              field.getKey, operationType, s"/${field.getKey}", false)
          })

          val pointWriterForPatch = CosmosPatchTestHelper.getPointWriterForPatch(columnConfigsMap, container, partitionKeyDefinition)

          operationType match {
            case CosmosPatchOperationTypes.None =>
              try {
                pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
                pointWriterForPatch.flushAndClose()
              } catch {
                case e: IllegalStateException => e.getMessage.contains(s"There is no operations included in the patch operation for itemId: $id") shouldEqual true
              }

            case CosmosPatchOperationTypes.Add | CosmosPatchOperationTypes.Set | CosmosPatchOperationTypes.Replace =>
              pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
              pointWriterForPatch.flushAndClose()
              val updatedItem: ObjectNode = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

              for (field: StructField <- partialUpdateSchema.fields) {
                field.dataType match {
                  case IntegerType =>
                    updatedItem.get(field.name).intValue() shouldEqual(patchPartialUpdateItem.get(field.name).intValue())
                  case LongType =>
                    updatedItem.get(field.name).longValue() shouldEqual(patchPartialUpdateItem.get(field.name).longValue())
                  case FloatType =>
                    updatedItem.get(field.name).floatValue() shouldEqual(patchPartialUpdateItem.get(field.name).floatValue())
                  case DoubleType =>
                    updatedItem.get(field.name).doubleValue() shouldEqual(patchPartialUpdateItem.get(field.name).doubleValue())
                  case BooleanType =>
                    updatedItem.get(field.name).booleanValue() shouldEqual(patchPartialUpdateItem.get(field.name).booleanValue())
                  case StringType =>
                    updatedItem.get(field.name).textValue() shouldEqual(patchPartialUpdateItem.get(field.name).textValue())
                  case _ =>
                    throw new IllegalArgumentException(s"${field.dataType} is not supported for simple types")
                }
              }
            case CosmosPatchOperationTypes.Remove =>
              pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
              pointWriterForPatch.flushAndClose()
              val updatedItem: ObjectNode = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

              for (field: StructField <- partialUpdateSchema.fields) {
                updatedItem.get(field.name) should be (null)
              }
            case _ =>
              throw new IllegalArgumentException(s"$operationType is not supported")
          }
      }
    })
  }

  "Point Writer" can "partial update item with array types" in {
    val partialUpdateSchema = StructType(Seq(
      StructField("newItemInPropArray", StringType),
    ))

    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
    val partitionKeyPath = CosmosPatchTestHelper.getStrippedPartitionKeyPath(partitionKeyDefinition)

    val writeConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemOverwrite,
      5,
      bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    // First create one item, as patch can only operate on existing items
    val itemWithFullSchema = CosmosPatchTestHelper.getPatchItemWithFullSchema(UUID.randomUUID().toString, partitionKeyPath)
    val id = itemWithFullSchema.get("id").textValue()
    val partitionKey = new PartitionKey(itemWithFullSchema.get(partitionKeyPath).textValue())

    pointWriter.scheduleWrite(partitionKey, itemWithFullSchema)
    pointWriter.flushAndClose()
    // make sure the item exists
    container.readItem(id, partitionKey, classOf[ObjectNode]).block()

    // Test for each cosmos patch operation type, ignore increment as there is a separate test for it
    CosmosPatchOperationTypes.values.foreach(operationType => {
      operationType match {
        case CosmosPatchOperationTypes.Increment => // no-op
        case _ =>
          // get the latest status of the item
          val originalItem: ObjectNode = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()
          val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]

          // Only trying to operate at 0 index
          columnConfigsMap += "newItemInPropArray" -> CosmosPatchColumnConfig(
            "newItemInPropArray", operationType, "/propArray/0", false)

          val pointWriterForPatch = CosmosPatchTestHelper.getPointWriterForPatch(columnConfigsMap, container, partitionKeyDefinition)
          val patchPartialUpdateItem = CosmosPatchTestHelper.getPatchItemWithSchema(id, partitionKeyPath, partialUpdateSchema)

          operationType match {
            case CosmosPatchOperationTypes.None =>
              try {
                pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
                pointWriterForPatch.flushAndClose()
              } catch {
                case e: IllegalStateException => e.getMessage.contains(s"There is no operations included in the patch operation for itemId: $id") shouldEqual true
              }
            case _ =>
              pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
              pointWriterForPatch.flushAndClose()

              val updatedItem: ObjectNode = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

              val updatedArrayList = updatedItem.get("propArray").elements().asScala.toList
              val originalArrayList = originalItem.get("propArray").elements().asScala.toList

              operationType match {
                case CosmosPatchOperationTypes.Add =>
                  updatedArrayList.size shouldEqual (originalArrayList.size + 1)
                  updatedArrayList(0).asText() shouldEqual patchPartialUpdateItem.get("newItemInPropArray").asText()

                case CosmosPatchOperationTypes.Set | CosmosPatchOperationTypes.Replace =>
                  updatedArrayList.size shouldEqual originalArrayList.size
                  updatedArrayList(0).asText() shouldEqual patchPartialUpdateItem.get("newItemInPropArray").asText()

                case CosmosPatchOperationTypes.Remove =>
                  updatedArrayList.size shouldEqual (originalArrayList.size - 1)
                case _ =>
                  throw new IllegalArgumentException(s"$operationType is not supported")
              }
          }
      }
    })
  }

  "Point Writer" can "partial update item with nested object with different mapping path" in {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
    val writeConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemOverwrite,
      5,
      bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    // First create one item with nestedObject, as patch can only operate on existing items
    val itemWithNestedObject: ObjectNode = objectMapper.createObjectNode()
    itemWithNestedObject.put("id", UUID.randomUUID().toString)
    val familyObject = itemWithNestedObject.putObject("family")
    familyObject.put("state", "NY")
    val parentObject = familyObject.putObject("parent1")
    parentObject.put("firstName", "Julie")
    parentObject.put("lastName", "Anderson")

    val id = itemWithNestedObject.get("id").textValue()
    val partitionKey = new PartitionKey(id)

    pointWriter.scheduleWrite(partitionKey, itemWithNestedObject)
    pointWriter.flushAndClose()
    // make sure the item exists
    container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

    // patch item by adding parent2
    val parent2PropertyName = "parent2"
    val partialUpdateNode = objectMapper.createObjectNode()
    partialUpdateNode.put("id", id)
    val newParentNode = partialUpdateNode.putObject(parent2PropertyName)
    newParentNode.put("firstName", "John")
    newParentNode.put("lastName", "Anderson")
    val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
    columnConfigsMap += parent2PropertyName -> CosmosPatchColumnConfig(
      parent2PropertyName, CosmosPatchOperationTypes.Add, s"/family/parent2", false)

    val pointWriterForPatch = CosmosPatchTestHelper.getPointWriterForPatch(columnConfigsMap, container, partitionKeyDefinition)
    pointWriterForPatch.scheduleWrite(partitionKey, partialUpdateNode)
    pointWriterForPatch.flushAndClose()

    // Validate parent2 has been inserted
    val updatedItem = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

    val updatedParent2Object = updatedItem.get("family").get(parent2PropertyName)
    updatedParent2Object should not be null
    updatedParent2Object.get("firstName") shouldEqual newParentNode.get("firstName")
    updatedParent2Object.get("lastName") shouldEqual newParentNode.get("lastName")
  }

  "Point Writer" can "partial update item with increment operation type" in {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
    val strippedPartitionKeyPath = CosmosPatchTestHelper.getStrippedPartitionKeyPath(partitionKeyDefinition)
    val writeConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemOverwrite,
      5,
      bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    // First create one item, as patch can only operate on existing items
    val itemWithFullSchema = CosmosPatchTestHelper.getPatchItemWithFullSchema(UUID.randomUUID().toString, strippedPartitionKeyPath)
    val id = itemWithFullSchema.get("id").textValue()
    val partitionKey = new PartitionKey(itemWithFullSchema.get(strippedPartitionKeyPath).textValue())

    pointWriter.scheduleWrite(partitionKey, itemWithFullSchema)
    pointWriter.flushAndClose()
    // make sure the item exists
    val originalItem = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

    // Patch operation will fail as it is trying to apply increment type for non-numeric type
    try {
      val incrementPartialUpdateInvalidSchema = StructType(Seq(
        StructField("propInt", IntegerType),
        StructField("propString", StringType),
      ))
      val patchPartialUpdateItem =
        CosmosPatchTestHelper.getPatchItemWithSchema(
          strippedPartitionKeyPath,
          incrementPartialUpdateInvalidSchema,
          originalItem)

      val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
      patchPartialUpdateItem.fields().asScala.foreach(field => {
        columnConfigsMap += field.getKey -> CosmosPatchColumnConfig(
          field.getKey, CosmosPatchOperationTypes.Increment, s"/${field.getKey}", false)
      })

      val pointWriterForPatch = CosmosPatchTestHelper.getPointWriterForPatch(columnConfigsMap, container, partitionKeyDefinition)

      pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
      pointWriterForPatch.flushAndClose()

      fail("Test should fail since Increment operation type does not support for non-numeric type")
    } catch {
      case e: Exception => e.getMessage should startWith("Increment operation is not supported for non-numeric type class com.fasterxml.jackson.databind.node.TextNode")
    }

    // Patch operation will succeed as it only apply increment on numeric type
    val incrementPartialUpdateValidSchema = StructType(Seq(
      StructField("propInt", IntegerType),
      StructField("propLong", LongType),
      StructField("propFloat", FloatType),
      StructField("propDouble", DoubleType)
    ))
    val patchPartialUpdateItem =
      CosmosPatchTestHelper.getPatchItemWithSchema(
        strippedPartitionKeyPath,
        incrementPartialUpdateValidSchema,
        originalItem)

    val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
    patchPartialUpdateItem.fields().asScala.foreach(field => {
      columnConfigsMap += field.getKey -> CosmosPatchColumnConfig(
        field.getKey, CosmosPatchOperationTypes.Increment, s"/${field.getKey}", false)
    })

    val pointWriterForPatch = CosmosPatchTestHelper.getPointWriterForPatch(columnConfigsMap, container, partitionKeyDefinition)

    pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
    pointWriterForPatch.flushAndClose()
  }

  "Point Writer" should "skip partial update for cosmos system properties" in {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
    val strippedPartitionKeyPath = CosmosPatchTestHelper.getStrippedPartitionKeyPath(partitionKeyDefinition)
    val writeConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemOverwrite,
      5,
      bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    // First create one item, as patch can only operate on existing items
    val itemWithFullSchema = CosmosPatchTestHelper.getPatchItemWithFullSchema(UUID.randomUUID().toString, strippedPartitionKeyPath)
    val id = itemWithFullSchema.get("id").textValue()
    val partitionKey = new PartitionKey(itemWithFullSchema.get(strippedPartitionKeyPath).textValue())

    pointWriter.scheduleWrite(partitionKey, itemWithFullSchema)
    pointWriter.flushAndClose()
    // make sure the item exists
    val originalItem = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

    // Cosmos patch does not support for system properties
    // if we send request to patch for them, server is going to return exception with "Invalid patch request: Cannot patch system property"
    // so the test is to make sure we have skipped these properties and the request can succeed for other properties
    val partialUpdateSchema = StructType(Seq(
      StructField("propInt", IntegerType),
      StructField("_ts", IntegerType),
      StructField("_etag", StringType),
      StructField("_self", StringType),
      StructField("_rid", StringType),
      StructField("_attachment", StringType)
    ))

    val patchPartialUpdateItem =
      CosmosPatchTestHelper.getPatchItemWithSchema(
        strippedPartitionKeyPath,
        partialUpdateSchema,
        originalItem)

    val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
    patchPartialUpdateItem.fields().asScala.foreach(field => {
      columnConfigsMap += field.getKey -> CosmosPatchColumnConfig(
        field.getKey, CosmosPatchOperationTypes.Set, s"/${field.getKey}", false)
    })

    val pointWriterForPatch = CosmosPatchTestHelper.getPointWriterForPatch(columnConfigsMap, container, partitionKeyDefinition)

    pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
    pointWriterForPatch.flushAndClose()

    val updatedItem: ObjectNode = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem
    updatedItem.get("propInt").asInt() == patchPartialUpdateItem.get("propInt").asInt()
  }

  "Point Writer" should "skip partial update for id and partitionKey properties" in {

    val partitionKeyPathSameAsIdArray = Array(false, true)

    for (partitionKeyPathSameAsId <- partitionKeyPathSameAsIdArray) {
      // create container
      val throughputProperties = ThroughputProperties.createManualThroughput(Defaults.DefaultContainerThroughput)
      val partitionKeyPath = if (partitionKeyPathSameAsId) "/id" else "/pk"
      val strippedPartitionKeyPath = partitionKeyPath.substring(1)
      val containerProperties = new CosmosContainerProperties(UUID.randomUUID().toString, partitionKeyPath)
      val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
      val containerCreationResponse = cosmosClient
       .getDatabase(cosmosDatabase)
       .createContainerIfNotExists(containerProperties, throughputProperties).block()
      val container =
        cosmosClient.getDatabase(cosmosDatabase).getContainer(containerCreationResponse.getProperties.getId)

      try {
        val writeConfig = CosmosWriteConfig(
          ItemWriteStrategy.ItemOverwrite,
          5,
          bulkEnabled = true,
          bulkMaxPendingOperations = Some(900)
        )

        val bulkWriter = new BulkWriter(container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None))

        // First create one item, as patch can only operate on existing items
        val itemWithFullSchema = CosmosPatchTestHelper.getPatchItemWithFullSchema(UUID.randomUUID().toString, strippedPartitionKeyPath)
        val id = itemWithFullSchema.get("id").textValue()
        val partitionKey = new PartitionKey(itemWithFullSchema.get(strippedPartitionKeyPath).textValue())

        bulkWriter.scheduleWrite(partitionKey, itemWithFullSchema)
        bulkWriter.flushAndClose()
        // make sure the item exists
        val originalItem = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

        // Cosmos patch does not support for id/partitionKey property
        // so the test is to make sure we have skipped id/paritionKey property and the request can succeed for other properties
        val partialUpdateSchema = StructType(Seq(
          StructField("propInt", IntegerType)
        ))

        val patchPartialUpdateItem =
          CosmosPatchTestHelper.getPatchItemWithSchema(
            strippedPartitionKeyPath,
            partialUpdateSchema,
            originalItem)

        val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
        patchPartialUpdateItem.fields().asScala.foreach(field => {
          columnConfigsMap += field.getKey -> CosmosPatchColumnConfig(
            field.getKey, CosmosPatchOperationTypes.Set, s"/${field.getKey}", false)
        })

        val bulkWriterForPatch = CosmosPatchTestHelper.getBulkWriterForPatch(columnConfigsMap, container, partitionKeyDefinition)

        patchPartialUpdateItem.fields().asScala.foreach(field => {
          columnConfigsMap += field.getKey -> CosmosPatchColumnConfig(
            field.getKey, CosmosPatchOperationTypes.Set, s"/${field.getKey}", false)
        })

        bulkWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
        bulkWriterForPatch.flushAndClose()

        val updatedItem: ObjectNode = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem
        updatedItem.get("propInt").asInt() shouldEqual patchPartialUpdateItem.get("propInt").asInt()
      } finally {
        container.delete().block()
      }
    }
  }

  "Point Writer" can "patch item with condition" in {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
    val strippedPartitionKeyPath = CosmosPatchTestHelper.getStrippedPartitionKeyPath(partitionKeyDefinition)
    val writeConfig = CosmosWriteConfig(
      ItemWriteStrategy.ItemOverwrite,
      5,
      bulkEnabled = false)

    val pointWriter = new PointWriter(
      container, partitionKeyDefinition, writeConfig, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())

    // First create one item, as patch can only operate on existing items
    val itemWithFullSchema = CosmosPatchTestHelper.getPatchItemWithFullSchema(UUID.randomUUID().toString, strippedPartitionKeyPath)
    val id = itemWithFullSchema.get("id").textValue()
    val partitionKey = new PartitionKey(itemWithFullSchema.get(strippedPartitionKeyPath).textValue())

    pointWriter.scheduleWrite(partitionKey, itemWithFullSchema)
    pointWriter.flushAndClose()
    // make sure the item exists
    val originalItem = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem()

    // Cosmos patch does not support for system properties
    // if we send request to patch for them, server is going to return exception with "Invalid patch request: Cannot patch system property"
    // so the test is to make sure we have skipped these properties and the request can succeed for other properties
    val partialUpdateSchema = StructType(Seq(
      StructField("propInt", IntegerType)
    ))

    val patchPartialUpdateItem =
      CosmosPatchTestHelper.getPatchItemWithSchema(
        strippedPartitionKeyPath,
        partialUpdateSchema,
        originalItem)

    val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
    patchPartialUpdateItem.fields().asScala.foreach(field => {
      columnConfigsMap += field.getKey -> CosmosPatchColumnConfig(
        field.getKey, CosmosPatchOperationTypes.Set, s"/${field.getKey}", false)
    })

    val pointWriterForPatch =
      CosmosPatchTestHelper.getPointWriterForPatch(
        columnConfigsMap,
        container,
        partitionKeyDefinition,
        Some(s"from c where c.propInt > ${Integer.MAX_VALUE}")) // using a always false condition

    try {
      pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
      pointWriterForPatch.flushAndClose()
      fail("Test should fail with 412 since the condition is false :" + originalItem.get("propInt").asInt())
    } catch {
      case e: CosmosException =>
        e.getMessage.contains("\"statusCode\":412,\"subStatusCode\":1110") shouldEqual true
    }

    // since the condition is always false, so the item should not be updated
    val updatedItem: ObjectNode = container.readItem(id, partitionKey, classOf[ObjectNode]).block().getItem
    objectMapper.writeValueAsString(updatedItem) shouldEqual objectMapper.writeValueAsString(originalItem)
  }

  "Point Writer" should "throw exception if no valid operations are included in patch operation" in {
    val container = getContainer
    val containerProperties = container.read().block().getProperties
    val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition
    val partitionKeyPath = StringUtils.join(partitionKeyDefinition.getPaths, "")

    val id = UUID.randomUUID().toString
    val partitionKey = new PartitionKey(id)

    val partialUpdateSchema = StructType(Seq(
      StructField("_ts", IntegerType)
    ))

    val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
    partialUpdateSchema.fields.foreach(field => {
      columnConfigsMap += field.name -> CosmosPatchColumnConfig(
        field.name, CosmosPatchOperationTypes.Set, s"/${field.name}", false)
    })

    val pointWriterForPatch = CosmosPatchTestHelper.getPointWriterForPatch(columnConfigsMap, container, partitionKeyDefinition)
    val patchPartialUpdateItem = CosmosPatchTestHelper.getPatchItemWithSchema(id, partitionKeyPath, partialUpdateSchema)

    try {
      pointWriterForPatch.scheduleWrite(partitionKey, patchPartialUpdateItem)
      pointWriterForPatch.flushAndClose()

      fail("Test should fail with IllegalStateException")
    } catch {
      case e: IllegalStateException => e.getMessage.contains(s"There is no operations included in the patch operation for itemId: $id") shouldEqual true
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
