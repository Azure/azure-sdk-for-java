// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal, TestConfigurations, Utils}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.diagnostics.DiagnosticsContext
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.MockTaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import java.util.UUID
import scala.collection.mutable.ListBuffer

class ItemsPartitionReaderWithReadManyITest
  extends IntegrationSpec
    with Spark
    with AutoCleanableCosmosContainersWithPkAsPartitionKey {
  private val idProperty = "id"
  private val pkProperty = "pk"
  private val itemIdentityProperty = "_itemIdentity"

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  it should "be able to get all items for the feedRange targeted to" in {
    val testCases = Array(
      (cosmosContainer, idProperty),
      (cosmosContainersWithPkAsPartitionKey, pkProperty)
    )

    for (testCase <- testCases) {
      logInfo(s"TestCase: containerName ${testCase._1}, partitionKeyProperty ${testCase._2}")
      val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(testCase._1)

      // first create few items
      val partitionKeyDefinition = container.read().block().getProperties.getPartitionKeyDefinition
      val allItems = ListBuffer[ObjectNode]()
      for (_ <- 1 to 20) {
        val objectNode = getNewItem(testCase._2)
        container.createItem(objectNode).block()
        allItems += objectNode
        logInfo(s"ID of test doc: ${objectNode.get(idProperty).asText()}")
      }

      val feedRanges = container.getFeedRanges().block()
      val sparkPartitionNormalizedRange = SparkBridgeImplementationInternal.toNormalizedRange(feedRanges.get(0))

      // then get all items overlap with sparkPartitionNormalizedRange
      val itemsOnPlannedFeedRange =
        allItems.filter(objectNode => {
          val feedRange =
            SparkBridgeImplementationInternal.partitionKeyToNormalizedRange(
              new PartitionKey(objectNode.get(testCase._2).asText()),
              partitionKeyDefinition)

          SparkBridgeImplementationInternal.doRangesOverlap(feedRange, sparkPartitionNormalizedRange)
        }).toList

      val config = Map(
        "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
        "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> testCase._1,
        "spark.cosmos.read.inferSchema.enabled" -> "true",
        "spark.cosmos.applicationName" -> "ItemsScan",
        "spark.cosmos.read.runtimeFiltering.enabled" -> "true",
        "spark.cosmos.read.readManyFiltering.enabled" -> "true"
      )

      val readSchema = getSchema(testCase._2)

      val diagnosticsContext = DiagnosticsContext(UUID.randomUUID(), "")
      val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)
      val cosmosClientMetadataCachesSnapshots = getCosmosClientMetadataCachesSnapshots()

      val itemsPartitionReaderWithReadMany =
        ItemsPartitionReaderWithReadMany(
          config,
          sparkPartitionNormalizedRange,
          readSchema,
          diagnosticsContext,
          cosmosClientMetadataCachesSnapshots,
          diagnosticsConfig,
          "",
          MockTaskContext.mockTaskContext(),
          itemsOnPlannedFeedRange
            .map(objectNode =>
              CosmosItemIdentityHelper.tryParseCosmosItemIdentity(
              CosmosItemIdentityHelper.getCosmosItemIdentityValueString(
                objectNode.get(idProperty).asText(),
                List(objectNode.get(testCase._2).asText()))
              ).get).iterator
        )

      val cosmosRowConverter = CosmosRowConverter.get(CosmosSerializationConfig.parseSerializationConfig(config))
      val itemsReadFromPartitionReader = ListBuffer[ObjectNode]()
      while (itemsPartitionReaderWithReadMany.next()) {
        itemsReadFromPartitionReader += cosmosRowConverter.fromInternalRowToObjectNode(itemsPartitionReaderWithReadMany.get(), readSchema)
      }

      itemsReadFromPartitionReader.size shouldEqual itemsOnPlannedFeedRange.size
      if (testCase._2.equalsIgnoreCase(idProperty)) {
        // validate there is no _itemIdentity property being populated
        itemsReadFromPartitionReader.foreach(item => {
          item.get(itemIdentityProperty) should be (null)
        })
      } else {
        // check _itemIdentity column is added
        itemsReadFromPartitionReader.foreach(item => {
          item.get(itemIdentityProperty).asText() shouldEqual
            CosmosItemIdentityHelper.getCosmosItemIdentityValueString(item.get(idProperty).asText(), List(item.get(testCase._2).asText()))
        })
      }

      // validate fetched all items on the selected feedRange
      val idsOnPlannedFeedRange = itemsOnPlannedFeedRange
        .map(objectNode => objectNode.get(idProperty).asText())
      itemsReadFromPartitionReader.map(item => item.get(idProperty).asText()).toList should contain allElementsOf (idsOnPlannedFeedRange)
    }
  }

  private def getCosmosClientMetadataCachesSnapshots(): Broadcast[CosmosClientMetadataCachesSnapshots] = {
    val cosmosClientMetadataCachesSnapshot = new CosmosClientMetadataCachesSnapshot()
    cosmosClientMetadataCachesSnapshot.serialize(cosmosClient)

    spark.sparkContext.broadcast(
      CosmosClientMetadataCachesSnapshots(
        cosmosClientMetadataCachesSnapshot,
        Option.empty[CosmosClientMetadataCachesSnapshot]))
  }

  private def getSchema(partitionKeyProperty: String): StructType = {
    if (partitionKeyProperty.equalsIgnoreCase(idProperty)) {
      StructType(Seq(
        StructField(idProperty, StringType, false)
      ))
    } else {
      StructType(Seq(
        StructField(idProperty, StringType, false),
        StructField(partitionKeyProperty, StringType, false),
        StructField("_itemIdentity", StringType, true)
      ))
    }
  }

  private def getNewItem(partitionKeyProperty: String): ObjectNode = {
    val objectNode = Utils.getSimpleObjectMapper.createObjectNode()

    val id = UUID.randomUUID().toString
    objectNode.put(idProperty, id)

    if (!partitionKeyProperty.equalsIgnoreCase(idProperty)) {
      val pk = UUID.randomUUID().toString
      objectNode.put(partitionKeyProperty, pk)

    }

    objectNode
  }

  //scalastyle:on multiple.string.literals
  //scalastyle:on magic.number
}
