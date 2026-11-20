// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, TestConfigurations, Utils}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.diagnostics.DiagnosticsContext
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.MockTaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import java.util.UUID
import scala.collection.mutable.ListBuffer

class ItemsPartitionReaderWithReadManyByPartitionKeyITest
  extends IntegrationSpec
    with Spark
    with AutoCleanableCosmosContainersWithPkAsPartitionKey {
  private val idProperty = "id"
  private val pkProperty = "pk"

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  it should "be able to retrieve all items for given partition keys" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    // Create items with known PK values
    val partitionKeyDefinition = container.read().block().getProperties.getPartitionKeyDefinition
    val allItemsByPk = scala.collection.mutable.Map[String, ListBuffer[ObjectNode]]()
    val pkValues = List("pkA", "pkB", "pkC")

    for (pk <- pkValues) {
      allItemsByPk(pk) = ListBuffer[ObjectNode]()
      for (_ <- 1 to 5) {
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put(idProperty, UUID.randomUUID().toString)
        objectNode.put(pkProperty, pk)
        container.createItem(objectNode).block()
        allItemsByPk(pk) += objectNode
      }
    }

    val config = Map(
      "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
      "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.applicationName" -> "ReadManyByPKTest"
    )

    val readSchema = StructType(Seq(
      StructField(idProperty, StringType, false),
      StructField(pkProperty, StringType, false)
    ))

    val diagnosticsContext = DiagnosticsContext(UUID.randomUUID(), "")
    val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)
    val cosmosClientMetadataCachesSnapshots = getCosmosClientMetadataCachesSnapshots()

    // Read items for pkA and pkB (not pkC)
    val targetPks = List("pkA", "pkB")
    val pkIterator = targetPks.map(pk => new PartitionKey(pk)).iterator

    val reader = ItemsPartitionReaderWithReadManyByPartitionKey(
      config,
      NormalizedRange("", "FF"),
      readSchema,
      diagnosticsContext,
      cosmosClientMetadataCachesSnapshots,
      diagnosticsConfig,
      "",
      MockTaskContext.mockTaskContext(),
      pkIterator
    )

    val cosmosRowConverter = CosmosRowConverter.get(CosmosSerializationConfig.parseSerializationConfig(config))
    val itemsReadFromReader = ListBuffer[ObjectNode]()
    while (reader.next()) {
      itemsReadFromReader += cosmosRowConverter.fromInternalRowToObjectNode(reader.get(), readSchema)
    }

    // Should have 10 items (5 for pkA + 5 for pkB)
    itemsReadFromReader.size shouldEqual 10

    // All items should be from pkA or pkB
    itemsReadFromReader.foreach(item => {
      val pk = item.get(pkProperty).asText()
      targetPks should contain(pk)
    })

    // Validate all expected IDs are present
    val expectedIds = (allItemsByPk("pkA") ++ allItemsByPk("pkB")).map(_.get(idProperty).asText()).toSet
    val actualIds = itemsReadFromReader.map(_.get(idProperty).asText()).toSet
    actualIds shouldEqual expectedIds

    reader.close()
  }

  it should "return empty results for non-existent partition keys" in {
    val config = Map(
      "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
      "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
      "spark.cosmos.read.inferSchema.enabled" -> "true",
      "spark.cosmos.applicationName" -> "ReadManyByPKEmptyTest"
    )

    val readSchema = StructType(Seq(
      StructField(idProperty, StringType, false),
      StructField(pkProperty, StringType, false)
    ))

    val diagnosticsContext = DiagnosticsContext(UUID.randomUUID(), "")
    val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)
    val cosmosClientMetadataCachesSnapshots = getCosmosClientMetadataCachesSnapshots()

    val pkIterator = List(new PartitionKey("nonExistentPk")).iterator

    val reader = ItemsPartitionReaderWithReadManyByPartitionKey(
      config,
      NormalizedRange("", "FF"),
      readSchema,
      diagnosticsContext,
      cosmosClientMetadataCachesSnapshots,
      diagnosticsConfig,
      "",
      MockTaskContext.mockTaskContext(),
      pkIterator
    )

    val itemsReadFromReader = ListBuffer[ObjectNode]()
    val cosmosRowConverter = CosmosRowConverter.get(CosmosSerializationConfig.parseSerializationConfig(config))
    while (reader.next()) {
      itemsReadFromReader += cosmosRowConverter.fromInternalRowToObjectNode(reader.get(), readSchema)
    }

    itemsReadFromReader.size shouldEqual 0
    reader.close()
  }

  private def getCosmosClientMetadataCachesSnapshots(): Broadcast[CosmosClientMetadataCachesSnapshots] = {
    val cosmosClientMetadataCachesSnapshot = new CosmosClientMetadataCachesSnapshot()
    cosmosClientMetadataCachesSnapshot.serialize(cosmosClient)

    spark.sparkContext.broadcast(
      CosmosClientMetadataCachesSnapshots(
        cosmosClientMetadataCachesSnapshot,
        Option.empty[CosmosClientMetadataCachesSnapshot]))
  }

  //scalastyle:on multiple.string.literals
  //scalastyle:on magic.number
}
