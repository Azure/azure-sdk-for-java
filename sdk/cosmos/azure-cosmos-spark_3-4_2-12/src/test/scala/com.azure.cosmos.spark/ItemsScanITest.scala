// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal, TestConfigurations, Utils}
import com.azure.cosmos.models.{PartitionKey, ThroughputProperties}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.expressions.Expressions
import org.apache.spark.sql.sources.{Filter, In}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import java.util.UUID
import scala.collection.mutable.ListBuffer

class ItemsScanITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
    with AutoCleanableCosmosContainer {
  private val schema = StructType(Seq(
    StructField("id", StringType)
  ))

  private val analyzedAggregatedFilters =
    AnalyzedAggregatedFilters(
      QueryFilterAnalyzer.rootParameterizedQuery,
      false,
      Array.empty[Filter],
      Array.empty[Filter],
      Option.empty[List[ReadManyFilter]])

  it should "only return readMany filtering property when runtTimeFiltering is enabled and readMany filtering is enabled" in {
    val clientMetadataCachesSnapshots = getCosmosClientMetadataCachesSnapshots()
    val partitionKeyDefinition =
      cosmosClient
        .getDatabase(cosmosDatabase)
        .getContainer(cosmosContainer)
        .read().block()
        .getProperties
        .getPartitionKeyDefinition

    for (runTimeFilteringEnabled <- Array(true, false)) {
      for (readManyFilteringEnabled <- Array(true, false)) {
        val config = Map(
          "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
          "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
          "spark.cosmos.database" -> cosmosDatabase,
          "spark.cosmos.container" -> cosmosContainer,
          "spark.cosmos.read.inferSchema.enabled" -> "true",
          "spark.cosmos.applicationName" -> "ItemsScan",
          "spark.cosmos.read.runtimeFiltering.enabled" -> runTimeFilteringEnabled.toString,
          "spark.cosmos.read.readManyFiltering.enabled" -> readManyFilteringEnabled.toString
        )
        val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
        val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)
        val itemScan = new ItemsScan(
          spark,
          schema,
          config,
          readConfig,
          analyzedAggregatedFilters,
          clientMetadataCachesSnapshots,
          diagnosticsConfig,
          "",
          partitionKeyDefinition)
        val arrayReferences = itemScan.filterAttributes()

        if (runTimeFilteringEnabled && readManyFilteringEnabled) {
          arrayReferences.size shouldBe 1
          // since id is the partitionKey, so id will be returned as the readMany filtering property
          arrayReferences should contain theSameElementsAs Array(Expressions.column(CosmosConstants.Properties.Id))
        } else {
          arrayReferences shouldBe empty
        }
      }
    }
  }

  it should "return _itemIdentity as readMany filtering property when id is not the partitionKey" in {
    val clientMetadataCachesSnapshots = getCosmosClientMetadataCachesSnapshots()

    val idNotPkContainerName = "idNotPkContainer-" + UUID.randomUUID().toString
    val idNotPkContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(idNotPkContainerName)

    try {
      cosmosClient.getDatabase(cosmosDatabase)
        .createContainerIfNotExists(idNotPkContainerName, "/pk", ThroughputProperties.createManualThroughput(400))
        .block()

      val partitionKeyDefinition = idNotPkContainer.read().block().getProperties.getPartitionKeyDefinition

      for (runTimeFilteringEnabled <- Array(true, false)) {
        for (readManyFilteringEnabled <- Array(true, false)) {
          val config = Map(
            "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
            "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> idNotPkContainerName,
            "spark.cosmos.read.inferSchema.enabled" -> "true",
            "spark.cosmos.applicationName" -> "ItemsScan",
            "spark.cosmos.read.runtimeFiltering.enabled" -> runTimeFilteringEnabled.toString,
            "spark.cosmos.read.readManyFiltering.enabled" -> readManyFilteringEnabled.toString
          )
          val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
          val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)
          val itemScan = new ItemsScan(
            spark,
            schema,
            config,
            readConfig,
            analyzedAggregatedFilters,
            clientMetadataCachesSnapshots,
            diagnosticsConfig,
            "",
            partitionKeyDefinition)
          val arrayReferences = itemScan.filterAttributes()

          if (runTimeFilteringEnabled && readManyFilteringEnabled) {
            arrayReferences.size shouldBe 1
            // since id is the partitionKey, so id will be returned as the readMany filtering property
            arrayReferences should contain theSameElementsAs Array(Expressions.column("_itemIdentity"))
          } else {
            arrayReferences shouldBe empty
          }
        }
      }
    } finally {
      idNotPkContainer.delete().block()
    }
  }

  it should "only prune partitions when runtTimeFiltering is enabled and readMany filtering is enabled" in {
    val clientMetadataCachesSnapshots = getCosmosClientMetadataCachesSnapshots()

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val partitionKeyDefinition = container.read().block().getProperties.getPartitionKeyDefinition

    // assert that there is more than one range
    val feedRanges = container.getFeedRanges.block()
    feedRanges.size()  should be > 1

    // first inject few items
    val idList = ListBuffer[String]()
    for (_ <- 1 to 20) {
      val id = UUID.randomUUID().toString
      idList += id
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("id", id)
      container.createItem(objectNode).block()
      logInfo(s"ID of test doc: $id")
    }

    // choose one of the items created above and filter by it
    val runtimeFilters = Array[Filter](
      In("id", Array(idList(0)))
    )

    for (runTimeFilteringEnabled <- Array(true, false)) {
      for (readManyFilteringEnabled <- Array(true, false)) {
        val config = Map(
          "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
          "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
          "spark.cosmos.database" -> cosmosDatabase,
          "spark.cosmos.container" -> cosmosContainer,
          "spark.cosmos.read.inferSchema.enabled" -> "true",
          "spark.cosmos.applicationName" -> "ItemsScan",
          "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
          "spark.cosmos.read.runtimeFiltering.enabled" -> runTimeFilteringEnabled.toString,
          "spark.cosmos.read.readManyFiltering.enabled" -> readManyFilteringEnabled.toString
        )
        val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
        val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)
        val itemScan = new ItemsScan(
          spark,
          schema,
          config,
          readConfig,
          analyzedAggregatedFilters,
          clientMetadataCachesSnapshots,
          diagnosticsConfig,
          "",
          partitionKeyDefinition)

        val plannedInputPartitions = itemScan.planInputPartitions()
        plannedInputPartitions.length shouldBe feedRanges.size() // using restrictive strategy

        itemScan.filter(runtimeFilters)
        val plannedInputPartitionAfterFiltering = itemScan.planInputPartitions()

        if (runTimeFilteringEnabled && readManyFilteringEnabled) {
          // partition can be pruned
          plannedInputPartitionAfterFiltering.length shouldBe 1
          val filterItemFeedRange =
            SparkBridgeImplementationInternal.partitionKeyToNormalizedRange(
              new PartitionKey(idList(0)),
              partitionKeyDefinition)

          val rangesOverlap =
            SparkBridgeImplementationInternal.doRangesOverlap(
              filterItemFeedRange,
              plannedInputPartitionAfterFiltering(0).asInstanceOf[CosmosInputPartition].feedRange)

          rangesOverlap shouldBe true
        } else {
          // no partition will be pruned
          plannedInputPartitionAfterFiltering.length shouldBe plannedInputPartitions.length
          plannedInputPartitionAfterFiltering should contain theSameElementsAs plannedInputPartitions
        }
      }
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
