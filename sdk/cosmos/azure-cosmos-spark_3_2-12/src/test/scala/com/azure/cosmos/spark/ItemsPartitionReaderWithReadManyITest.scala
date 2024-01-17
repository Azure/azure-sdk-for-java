// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal, TestConfigurations, Utils}
import com.azure.cosmos.models.{PartitionKey, ThroughputProperties}
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
    val idNotPkContainerName = "idNotPkContainer-" + UUID.randomUUID().toString
    val idNotPkContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(idNotPkContainerName)

    try {
      cosmosClient.getDatabase(cosmosDatabase)
        .createContainerIfNotExists(idNotPkContainerName, "/pk", ThroughputProperties.createManualThroughput(400))
        .block()

      // first create few items
      val partitionKeyDefinition = idNotPkContainer.read().block().getProperties.getPartitionKeyDefinition
      val allItems = ListBuffer[ObjectNode]()
      for (_ <- 1 to 20) {
        val id = UUID.randomUUID().toString
        val pk = UUID.randomUUID().toString
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("id", id)
        objectNode.put("pk", pk)
        idNotPkContainer.createItem(objectNode).block()
        allItems += objectNode
        logInfo(s"ID of test doc: $id")
      }

      val feedRanges = idNotPkContainer.getFeedRanges().block()
      val sparkPartitionNormalizedRange = SparkBridgeImplementationInternal.toNormalizedRange(feedRanges.get(0))

      // then get all items overlap with sparkPartitionNormalizedRange
      val itemsOnPlannedFeedRange =
        allItems.filter(objectNode => {
          val feedRange =
            SparkBridgeImplementationInternal.partitionKeyToNormalizedRange(
              new PartitionKey(objectNode.get("pk").asText()),
              partitionKeyDefinition)

          SparkBridgeImplementationInternal.doRangesOverlap(feedRange, sparkPartitionNormalizedRange)
        }).toList

      val config = Map(
        "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
        "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> idNotPkContainerName,
        "spark.cosmos.read.inferSchema.enabled" -> "true",
        "spark.cosmos.applicationName" -> "ItemsScan",
        "spark.cosmos.read.runtimeFiltering.enabled" -> "true",
        "spark.cosmos.read.readManyFiltering.enabled" -> "true"
      )

      val readSchema = StructType(Seq(
        StructField("id", StringType, false),
        StructField("pk", StringType, false),
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
          itemsOnPlannedFeedRange
            .map(objectNode =>
              CosmosItemIdentityHelper.getCosmosItemIdentityValueString(
                objectNode.get("id").asText(),
                new PartitionKey(objectNode.get("pk").asText())))
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
          CosmosItemIdentityHelper.getCosmosItemIdentityValueString(item.get("id").asText(), new PartitionKey(item.get("pk").asText()))
      })

      val idsOnPlannedFeedRange = itemsOnPlannedFeedRange
        .map(objectNode => objectNode.get("id"))
      itemsReadFromPartitionReader.map(item => item.get("id").asText()) should contain allElementsOf (idsOnPlannedFeedRange)
    } finally {
      idNotPkContainer.delete().block()
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
}
