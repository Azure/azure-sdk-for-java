// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal, TestConfigurations, Utils}
import com.azure.cosmos.models.PartitionKey
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.expressions.Expressions
import org.apache.spark.sql.sources.{Filter, In}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import java.util.UUID
import scala.collection.mutable.ListBuffer

class ItemsScanITest
  extends IntegrationSpec
    with Spark
    with AutoCleanableCosmosContainersWithPkAsPartitionKey {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  private val idProperty = "id"
  private val pkProperty = "pk"
  private val itemIdentityProperty = "_itemIdentity"

  private val analyzedAggregatedFilters =
    AnalyzedAggregatedFilters(
      QueryFilterAnalyzer.rootParameterizedQuery,
      false,
      Array.empty[Filter],
      Array.empty[Filter],
      Option.empty[List[ReadManyFilter]])

  it should "only return readMany filtering property when runtTimeFiltering is enabled and readMany filtering is enabled" in {
    val clientMetadataCachesSnapshots = getCosmosClientMetadataCachesSnapshots()

    val testCases = Array(
      // containerName, partitionKey property, expected readMany filtering property
      (cosmosContainer, idProperty, idProperty),
      (cosmosContainersWithPkAsPartitionKey, pkProperty, itemIdentityProperty)
    )

    for (testCase <- testCases) {
      val partitionKeyDefinition =
        cosmosClient
          .getDatabase(cosmosDatabase)
          .getContainer(testCase._1)
          .read()
          .block()
          .getProperties
          .getPartitionKeyDefinition

      for (runTimeFilteringEnabled <- Array(true, false)) {
        for (readManyFilteringEnabled <- Array(true, false)) {
          logInfo(s"TestCase: containerName ${testCase._1}, partitionKeyProperty ${testCase._2}, " +
            s"runtimeFilteringEnabled $runTimeFilteringEnabled, readManyFilteringEnabled $readManyFilteringEnabled")

          val config = Map(
            "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
            "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> testCase._1,
            "spark.cosmos.read.inferSchema.enabled" -> "true",
            "spark.cosmos.applicationName" -> "ItemsScan",
            "spark.cosmos.read.runtimeFiltering.enabled" -> runTimeFilteringEnabled.toString,
            "spark.cosmos.read.readManyFiltering.enabled" -> readManyFilteringEnabled.toString
          )
          val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
          val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)
          val schema = getDefaultSchema(testCase._2)

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
            arrayReferences should contain theSameElementsAs Array(Expressions.column(testCase._3))
          } else {
            arrayReferences shouldBe empty
          }
        }
      }
    }
  }

  it should "only prune partitions when runtTimeFiltering is enabled and readMany filtering is enabled" in {
    val clientMetadataCachesSnapshots = getCosmosClientMetadataCachesSnapshots()

    val testCases = Array(
      //containerName, partitionKeyProperty, expected readManyFiltering property
      (cosmosContainer, idProperty, idProperty),
      (cosmosContainersWithPkAsPartitionKey, pkProperty, itemIdentityProperty)
    )
    for (testCase <- testCases) {
      val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(testCase._1)
      val partitionKeyDefinition = container.read().block().getProperties.getPartitionKeyDefinition

      // assert that there is more than one range
      val feedRanges = container.getFeedRanges.block()
      feedRanges.size() should be > 1

      // first inject few items
      val matchingItemList = ListBuffer[ObjectNode]()
      for (_ <- 1 to 20) {
        val objectNode = getNewItem(testCase._2)
        container.createItem(objectNode).block()
        matchingItemList += objectNode
        logInfo(s"ID of test doc: ${objectNode.get(idProperty).asText()}")
      }

      // choose one of the items created above and filter by it
      val runtimeFilters = getReadManyFilters(Array(matchingItemList(0)), testCase._2, testCase._3)

      for (runTimeFilteringEnabled <- Array(true, false)) {
        for (readManyFilteringEnabled <- Array(true, false)) {
          logInfo(s"TestCase: containerName ${testCase._1}, partitionKeyProperty ${testCase._2}, " +
            s"runtimeFilteringEnabled $runTimeFilteringEnabled, readManyFilteringEnabled $readManyFilteringEnabled")

          val config = Map(
            "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
            "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> testCase._1,
            "spark.cosmos.read.inferSchema.enabled" -> "true",
            "spark.cosmos.applicationName" -> "ItemsScan",
            "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
            "spark.cosmos.read.runtimeFiltering.enabled" -> runTimeFilteringEnabled.toString,
            "spark.cosmos.read.readManyFiltering.enabled" -> readManyFilteringEnabled.toString
          )
          val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
          val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)

          val schema = getDefaultSchema(testCase._2)
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
                new PartitionKey(getPartitionKeyValue(matchingItemList(0), s"/${testCase._2}")),
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
  }

  private def getCosmosClientMetadataCachesSnapshots(): Broadcast[CosmosClientMetadataCachesSnapshots] = {
    val cosmosClientMetadataCachesSnapshot = new CosmosClientMetadataCachesSnapshot()
    cosmosClientMetadataCachesSnapshot.serialize(cosmosClient)

    spark.sparkContext.broadcast(
      CosmosClientMetadataCachesSnapshots(
        cosmosClientMetadataCachesSnapshot,
        Option.empty[CosmosClientMetadataCachesSnapshot]))
  }

  private def getReadManyFilters(
                                  filteringItems: Array[ObjectNode],
                                  partitionKeyProperty: String,
                                  readManyFilteringProperty: String): Array[Filter] = {
    val readManyFilterValues =
      filteringItems
        .map(filteringItem => getReadManyFilteringValue(filteringItem, partitionKeyProperty, readManyFilteringProperty))

    if (partitionKeyProperty.equalsIgnoreCase(idProperty)) {
      Array[Filter](In(idProperty, readManyFilterValues.map(_.asInstanceOf[Any])))
    } else {
      Array[Filter](In(readManyFilteringProperty, readManyFilterValues.map(_.asInstanceOf[Any])))
    }
  }

  private def getReadManyFilteringValue(
                                         objectNode: ObjectNode,
                                         partitionKeyProperty: String,
                                         readManyFilteringProperty: String): String = {

    if (readManyFilteringProperty.equals(itemIdentityProperty)) {
      CosmosItemIdentityHelper
        .getCosmosItemIdentityValueString(
          objectNode.get(idProperty).asText(),
          List(objectNode.get(partitionKeyProperty).asText()))
    } else {
      objectNode.get(idProperty).asText()
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

  private def getDefaultSchema(partitionKeyProperty: String): StructType = {
    if (!partitionKeyProperty.equalsIgnoreCase(idProperty)) {
      StructType(Seq(
        StructField(idProperty, StringType),
        StructField(pkProperty, StringType),
        StructField(itemIdentityProperty, StringType)
      ))
    } else {
      StructType(Seq(
        StructField(idProperty, StringType)
      ))
    }
  }

  //scalastyle:on multiple.string.literals
  //scalastyle:on magic.number
}
