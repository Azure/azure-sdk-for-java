// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.PartitionKey
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SaveMode}

import java.util.UUID
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class TransactionalBatchITest extends IntegrationSpec
  with Spark
  with AutoCleanableCosmosContainersWithPkAsPartitionKey {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  "Transactional Batch" should "create items atomically" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    val partitionKeyValue = UUID.randomUUID().toString
    val item1Id = s"test-item1-${UUID.randomUUID()}"
    val item2Id = s"test-item2-${UUID.randomUUID()}"

    // Create batch operations DataFrame with flat columns (like normal writes)
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false)
    ))

    val batchOperations = Seq(
      Row(item1Id, partitionKeyValue, "Alice"),
      Row(item2Id, partitionKeyValue, "Bob")
    )

    val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

    // Execute transactional batch (defaults to upsert)
    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey
    )

    val resultDf = CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava)

    // Verify results
    val results = resultDf.collect()
    results.length shouldEqual 2
    results.foreach { row =>
      row.getAs[Int]("statusCode") shouldEqual 201
      row.getAs[Boolean]("isSuccessStatusCode") shouldBe true
    }

    // Verify items were created
    val item1 = container.readItem(item1Id, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    item1 should not be null
    item1.getItem.get("name").asText() shouldEqual "Alice"

    val item2 = container.readItem(item2Id, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    item2 should not be null
    item2.getItem.get("name").asText() shouldEqual "Bob"
  }

  it should "rollback all operations on failure" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    val partitionKeyValue = UUID.randomUUID().toString
    val item1Id = s"test-item1-${UUID.randomUUID()}"
    val duplicateId = s"duplicate-${UUID.randomUUID()}"

    // First create an item that we'll try to create again (should fail)
    val existingDoc = Utils.getSimpleObjectMapper.createObjectNode()
    existingDoc.put("id", duplicateId)
    existingDoc.put("pk", partitionKeyValue)
    existingDoc.put("name", "Existing")
    container.createItem(existingDoc).block()

    // Create batch with one valid create and one duplicate create (should fail entire batch)
    // Use flat columns with explicit operationType
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false),
      StructField("operationType", StringType, nullable = false)
    ))

    val batchOperations = Seq(
      Row(item1Id, partitionKeyValue, "NewItem", "create"),
      Row(duplicateId, partitionKeyValue, "Duplicate", "create")
    )

    val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey
    )

    val resultDf = CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava)

    // Verify batch failed
    val results = resultDf.collect()
    results.length shouldEqual 2
    
    // All operations should have failed due to atomicity
    results.foreach { row =>
      row.getAs[Boolean]("isSuccessStatusCode") shouldBe false
    }

    // Verify item1 was NOT created (rolled back)
    try {
      container.readItem(item1Id, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
      fail("Item1 should not exist after batch rollback")
    } catch {
      case _: Exception => // Expected - item doesn't exist
    }
  }

  it should "support simplified schema with default upsert operation" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    val partitionKeyValue = UUID.randomUUID().toString
    val item1Id = s"test-item1-${UUID.randomUUID()}"
    val item2Id = s"test-item2-${UUID.randomUUID()}"

    // Create batch operations DataFrame with simplified schema (no operationType = default upsert)
    val simplifiedSchema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false),
      StructField("age", IntegerType, nullable = false)
    ))

    val batchOperations = Seq(
      Row(item1Id, partitionKeyValue, "Alice", 30),
      Row(item2Id, partitionKeyValue, "Bob", 25)
    )

    val operationsDf = spark.createDataFrame(batchOperations.asJava, simplifiedSchema)

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey
    )

    // Execute transactional batch - should default to upsert
    val resultDf = CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava)

    // Verify results
    val results = resultDf.collect()
    results.length shouldEqual 2
    results.foreach { row =>
      row.getAs[Int]("statusCode") shouldEqual 201
      row.getAs[Boolean]("isSuccessStatusCode") shouldBe true
    }

    // Verify items were created
    val item1 = container.readItem(item1Id, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    item1 should not be null
    item1.getItem.get("name").asText() shouldEqual "Alice"
    item1.getItem.get("age").asInt() shouldEqual 30

    val item2 = container.readItem(item2Id, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    item2 should not be null
    item2.getItem.get("name").asText() shouldEqual "Bob"
    item2.getItem.get("age").asInt() shouldEqual 25
  }

  it should "preserve order with simplified schema" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    val partitionKeyValue = UUID.randomUUID().toString
    val baseId = s"order-test-${UUID.randomUUID()}"

    // Create batch with multiple operations in specific order
    val simplifiedSchema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("sequence", IntegerType, nullable = false)
    ))

    val batchOperations = (1 to 10).map { i =>
      Row(s"$baseId-$i", partitionKeyValue, i)
    }

    val operationsDf = spark.createDataFrame(batchOperations.asJava, simplifiedSchema)

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey
    )

    val resultDf = CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava)

    // Verify all operations succeeded
    val results = resultDf.collect()
    results.length shouldEqual 10
    results.foreach { row =>
      row.getAs[Boolean]("isSuccessStatusCode") shouldBe true
    }

    // Verify items were created in order
    (1 to 10).foreach { i =>
      val item = container.readItem(s"$baseId-$i", new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
      item should not be null
      item.getItem.get("sequence").asInt() shouldEqual i
    }
  }

  it should "support update operations with simplified schema" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    val partitionKeyValue = UUID.randomUUID().toString
    val itemId = s"test-item-${UUID.randomUUID()}"

    // First create an item
    val initialDoc = Utils.getSimpleObjectMapper.createObjectNode()
    initialDoc.put("id", itemId)
    initialDoc.put("pk", partitionKeyValue)
    initialDoc.put("name", "InitialName")
    initialDoc.put("version", 1)
    container.createItem(initialDoc).block()

    // Now update it using simplified schema (defaults to upsert)
    val simplifiedSchema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false),
      StructField("version", IntegerType, nullable = false)
    ))

    val updateOperations = Seq(
      Row(itemId, partitionKeyValue, "UpdatedName", 2)
    )

    val operationsDf = spark.createDataFrame(updateOperations.asJava, simplifiedSchema)

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey
    )

    val resultDf = CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava)

    // Verify update succeeded
    val results = resultDf.collect()
    results.length shouldEqual 1
    results.head.getAs[Int]("statusCode") shouldEqual 200 // OK for upsert on existing item
    results.head.getAs[Boolean]("isSuccessStatusCode") shouldBe true

    // Verify item was updated
    val updatedItem = container.readItem(itemId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    updatedItem should not be null
    updatedItem.getItem.get("name").asText() shouldEqual "UpdatedName"
    updatedItem.getItem.get("version").asInt() shouldEqual 2
  }
}
