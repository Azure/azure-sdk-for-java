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
    with CosmosClient
    with AutoCleanableCosmosContainer {

  "ItemsPartitionReaderWithReadMany" should "be able to get all items for the feedRange targeted to" in {
    // first create few items
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val partitionKeyDefinition = container.read().block().getProperties.getPartitionKeyDefinition
    val allItems = ListBuffer[String]()
    for (_ <- 1 to 20) {
      val id = UUID.randomUUID().toString
      allItems += id
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("id", id)
      container.createItem(objectNode).block()
      logInfo(s"ID of test doc: $id")
    }

    val feedRanges = container.getFeedRanges().block()
    val sparkPartitionNormalizedRange = SparkBridgeImplementationInternal.toNormalizedRange(feedRanges.get(0))

    // then get all items overlap with sparkPartitionNormalizedRange
    val itemsOnPlannedFeedRange =
      allItems.filter(id => {
        val feedRange =
          SparkBridgeImplementationInternal.partitionKeyToNormalizedRange(
            new PartitionKey(id),
            partitionKeyDefinition)

        SparkBridgeImplementationInternal.doRangesOverlap(feedRange, sparkPartitionNormalizedRange)
      }).toList

    val config = Map(
      "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
      "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.applicationName" -> "ItemsScan",
      "spark.cosmos.read.runtimeFiltering.enabled" -> "true",
      "spark.cosmos.read.readManyFiltering.enabled" -> "true"
    )

    val readSchema = StructType(Seq(
      StructField("id", StringType, false),
      StructField("_itemIdentity", StringType, true)
    ))

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
        itemsOnPlannedFeedRange.map(id => CosmosItemIdentityHelper.getCosmosItemIdentityValueString(id, new PartitionKey(id)))
      )

    val cosmosRowConverter = CosmosRowConverter.get(CosmosSerializationConfig.parseSerializationConfig(config))
    val itemsReadFromPartitionReader = ListBuffer[ObjectNode]()
    while (itemsPartitionReaderWithReadMany.next()) {
      itemsReadFromPartitionReader += cosmosRowConverter.fromInternalRowToObjectNode(itemsPartitionReaderWithReadMany.get(), readSchema)
    }

    itemsReadFromPartitionReader.size shouldEqual itemsOnPlannedFeedRange.size
    // check _itemIdentity column is added
    itemsReadFromPartitionReader.foreach(item => {
      item.get("_itemIdentity").asText() shouldEqual
        CosmosItemIdentityHelper.getCosmosItemIdentityValueString(item.get("id").asText(), new PartitionKey(item.get("id").asText()))
    })

    itemsReadFromPartitionReader.map(item => item.get("id").asText()) should contain allElementsOf (itemsOnPlannedFeedRange)
  }

  private def getCosmosClientMetadataCachesSnapshots(): Broadcast[CosmosClientMetadataCachesSnapshots] = {
    val cosmosClientMetadataCachesSnapshot = new CosmosClientMetadataCachesSnapshot()
    cosmosClientMetadataCachesSnapshot.serialize(cosmosClient)

    spark.sparkContext.broadcast(
      CosmosClientMetadataCachesSnapshots(
        cosmosClientMetadataCachesSnapshot,
        Option.empty[CosmosClientMetadataCachesSnapshot]))
  }
}
