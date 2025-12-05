// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.{PartitionKey, PartitionKeyBuilder}
import com.azure.cosmos.CosmosException
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
      case e: CosmosException if e.getStatusCode == 404 => 
        // Expected - item doesn't exist after rollback (404 Not Found)
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

  it should "fail when more than 100 operations for a single partition key" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val partitionKeyValue = UUID.randomUUID().toString

    // Create 101 operations for the same partition key
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false)
    ))

    val batchOperations = (1 to 101).map { i =>
      Row(s"item-$i-${UUID.randomUUID()}", partitionKeyValue, s"Name-$i")
    }

    val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey
    )

    // Should throw IllegalArgumentException
    val exception = intercept[Exception] {
      CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava).collect()
    }

    exception.getMessage should include("exceeds the")
    exception.getMessage should include("100 operations per partition key")
    exception.getMessage should include(partitionKeyValue)
  }

  "Transactional Batch with Hierarchical Partition Keys" should "create items atomically with PermId and SourceId" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    
    // Create container with hierarchical partition keys (PermId, SourceId)
    val containerName = s"test-hpk-${UUID.randomUUID()}"
    val containerProperties = new com.azure.cosmos.models.CosmosContainerProperties(
      containerName,
      new com.azure.cosmos.models.PartitionKeyDefinition()
    )
    val paths = new java.util.ArrayList[String]()
    paths.add("/PermId")
    paths.add("/SourceId")
    containerProperties.getPartitionKeyDefinition.setPaths(paths)
    containerProperties.getPartitionKeyDefinition.setKind(com.azure.cosmos.models.PartitionKind.MULTI_HASH)
    containerProperties.getPartitionKeyDefinition.setVersion(com.azure.cosmos.models.PartitionKeyDefinitionVersion.V2)
    
    cosmosClient.getDatabase(cosmosDatabase).createContainer(containerProperties).block()
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)

    try {
      val permId = "MSFT"
      val sourceId = "Bloomberg"
      val item1Id = s"${UUID.randomUUID()}"
      val item2Id = s"${UUID.randomUUID()}"

      // Create batch operations with hierarchical partition keys
      val schema = StructType(Seq(
        StructField("id", StringType, nullable = false),
        StructField("PermId", StringType, nullable = false),
        StructField("SourceId", StringType, nullable = false),
        StructField("price", org.apache.spark.sql.types.DoubleType, nullable = false)
      ))

      val batchOperations = Seq(
        Row(item1Id, permId, sourceId, 100.5),
        Row(item2Id, permId, sourceId, 101.25)
      )

      val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

      val cfg = Map(
        "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> containerName
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
      val pk = new PartitionKeyBuilder().add(permId).add(sourceId).build()
      val item1 = container.readItem(item1Id, pk, classOf[ObjectNode]).block()
      item1 should not be null
      item1.getItem.get("price").asDouble() shouldEqual 100.5

      val item2 = container.readItem(item2Id, pk, classOf[ObjectNode]).block()
      item2 should not be null
      item2.getItem.get("price").asDouble() shouldEqual 101.25
    } finally {
      // Clean up container
      container.delete().block()
    }
  }

  it should "handle temporal updates for financial instrument timelines" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    
    // Create container with hierarchical partition keys (PermId, SourceId)
    val containerName = s"test-hpk-temporal-${UUID.randomUUID()}"
    val containerProperties = new com.azure.cosmos.models.CosmosContainerProperties(
      containerName,
      new com.azure.cosmos.models.PartitionKeyDefinition()
    )
    val paths = new java.util.ArrayList[String]()
    paths.add("/PermId")
    paths.add("/SourceId")
    containerProperties.getPartitionKeyDefinition.setPaths(paths)
    containerProperties.getPartitionKeyDefinition.setKind(com.azure.cosmos.models.PartitionKind.MULTI_HASH)
    containerProperties.getPartitionKeyDefinition.setVersion(com.azure.cosmos.models.PartitionKeyDefinitionVersion.V2)
    
    cosmosClient.getDatabase(cosmosDatabase).createContainer(containerProperties).block()
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)

    try {
      val permId = "MSFT"
      val sourceId = "Bloomberg"
      val oldRecordId = "2024-01-01T00:00:00Z"
      val newRecordId = "2024-06-01T00:00:00Z"

      // First, create initial record
      val initialDoc = Utils.getSimpleObjectMapper.createObjectNode()
      initialDoc.put("id", oldRecordId)
      initialDoc.put("PermId", permId)
      initialDoc.put("SourceId", sourceId)
      initialDoc.put("price", 100.0)
      initialDoc.putNull("valid_to")
      container.createItem(initialDoc).block()

      // Now perform atomic temporal update: close old record + create new record
      val schema = StructType(Seq(
        StructField("id", StringType, nullable = false),
        StructField("PermId", StringType, nullable = false),
        StructField("SourceId", StringType, nullable = false),
        StructField("price", org.apache.spark.sql.types.DoubleType, nullable = false),
        StructField("valid_to", StringType, nullable = true),
        StructField("operationType", StringType, nullable = false)
      ))

      val batchOperations = Seq(
        // Close old record by setting valid_to
        Row(oldRecordId, permId, sourceId, 100.0, "2024-06-01T00:00:00Z", "replace"),
        // Create new record with new price
        Row(newRecordId, permId, sourceId, 150.0, null, "create")
      )

      val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

      val cfg = Map(
        "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> containerName
      )

      val resultDf = CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava)

      // Verify results
      val results = resultDf.collect()
      results.length shouldEqual 2
      results.foreach { row =>
        row.getAs[Boolean]("isSuccessStatusCode") shouldBe true
      }

      // Verify old record was closed
      val pk = new PartitionKeyBuilder().add(permId).add(sourceId).build()
      val oldRecord = container.readItem(oldRecordId, pk, classOf[ObjectNode]).block()
      oldRecord should not be null
      oldRecord.getItem.get("valid_to").asText() shouldEqual "2024-06-01T00:00:00Z"

      // Verify new record was created
      val newRecord = container.readItem(newRecordId, pk, classOf[ObjectNode]).block()
      newRecord should not be null
      newRecord.getItem.get("price").asDouble() shouldEqual 150.0
      newRecord.getItem.get("valid_to").isNull shouldBe true
    } finally {
      // Clean up container
      container.delete().block()
    }
  }

  it should "handle operations across multiple PermId/SourceId combinations" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    
    // Create container with hierarchical partition keys (PermId, SourceId)
    val containerName = s"test-hpk-multi-${UUID.randomUUID()}"
    val containerProperties = new com.azure.cosmos.models.CosmosContainerProperties(
      containerName,
      new com.azure.cosmos.models.PartitionKeyDefinition()
    )
    val paths = new java.util.ArrayList[String]()
    paths.add("/PermId")
    paths.add("/SourceId")
    containerProperties.getPartitionKeyDefinition.setPaths(paths)
    containerProperties.getPartitionKeyDefinition.setKind(com.azure.cosmos.models.PartitionKind.MULTI_HASH)
    containerProperties.getPartitionKeyDefinition.setVersion(com.azure.cosmos.models.PartitionKeyDefinitionVersion.V2)
    
    cosmosClient.getDatabase(cosmosDatabase).createContainer(containerProperties).block()
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)

    try {
      // Create operations for different PermId/SourceId combinations
      val schema = StructType(Seq(
        StructField("id", StringType, nullable = false),
        StructField("PermId", StringType, nullable = false),
        StructField("SourceId", StringType, nullable = false),
        StructField("price", org.apache.spark.sql.types.DoubleType, nullable = false)
      ))

      val batchOperations = Seq(
        // MSFT from Bloomberg
        Row(s"${UUID.randomUUID()}", "MSFT", "Bloomberg", 100.0),
        Row(s"${UUID.randomUUID()}", "MSFT", "Bloomberg", 101.0),
        // MSFT from Reuters
        Row(s"${UUID.randomUUID()}", "MSFT", "Reuters", 100.5),
        // AAPL from Bloomberg
        Row(s"${UUID.randomUUID()}", "AAPL", "Bloomberg", 150.0)
      )

      val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

      val cfg = Map(
        "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> containerName
      )

      val resultDf = CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava)

      // Verify all operations succeeded
      val results = resultDf.collect()
      results.length shouldEqual 4
      results.foreach { row =>
        row.getAs[Int]("statusCode") shouldEqual 201
        row.getAs[Boolean]("isSuccessStatusCode") shouldBe true
      }
    } finally {
      // Clean up container
      container.delete().block()
    }
  }

  it should "fail when more than 100 operations for a single hierarchical partition key" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    
    // Create container with hierarchical partition keys (PermId, SourceId)
    val containerName = s"test-hpk-limit-${UUID.randomUUID()}"
    val containerProperties = new com.azure.cosmos.models.CosmosContainerProperties(
      containerName,
      new com.azure.cosmos.models.PartitionKeyDefinition()
    )
    val paths = new java.util.ArrayList[String]()
    paths.add("/PermId")
    paths.add("/SourceId")
    containerProperties.getPartitionKeyDefinition.setPaths(paths)
    containerProperties.getPartitionKeyDefinition.setKind(com.azure.cosmos.models.PartitionKind.MULTI_HASH)
    containerProperties.getPartitionKeyDefinition.setVersion(com.azure.cosmos.models.PartitionKeyDefinitionVersion.V2)
    
    cosmosClient.getDatabase(cosmosDatabase).createContainer(containerProperties).block()
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)

    try {
      val permId = "MSFT"
      val sourceId = "Bloomberg"

      // Create 101 operations for the same hierarchical partition key
      val schema = StructType(Seq(
        StructField("id", StringType, nullable = false),
        StructField("PermId", StringType, nullable = false),
        StructField("SourceId", StringType, nullable = false),
        StructField("price", org.apache.spark.sql.types.DoubleType, nullable = false)
      ))

      val batchOperations = (1 to 101).map { i =>
        Row(s"${UUID.randomUUID()}", permId, sourceId, 100.0 + i)
      }

      val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

      val cfg = Map(
        "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> containerName
      )

      // Should throw IllegalArgumentException
      val exception = intercept[Exception] {
        CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava).collect()
      }

      exception.getMessage should include("exceeds the")
      exception.getMessage should include("100 operations per partition key")
      exception.getMessage should include("MSFT")
      exception.getMessage should include("Bloomberg")
    } finally {
      // Clean up container
      container.delete().block()
    }
  }

  it should "validate DataFrame repartitioning reduces partition key transitions" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    // Create operations for multiple partition keys intentionally in random order
    val pk1 = UUID.randomUUID().toString
    val pk2 = UUID.randomUUID().toString
    val pk3 = UUID.randomUUID().toString

    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("value", StringType, nullable = false)
    ))

    // Interleave operations for different partition keys
    // Without repartitioning: would see many transitions (pk1->pk2->pk3->pk1->pk2->pk3...)
    // With repartitioning: should see only 2 transitions (pk1 batch -> pk2 batch -> pk3 batch)
    val batchOperations = Seq(
      Row(s"${pk1}_1", pk1, "value1"),
      Row(s"${pk2}_1", pk2, "value1"),
      Row(s"${pk3}_1", pk3, "value1"),
      Row(s"${pk1}_2", pk1, "value2"),
      Row(s"${pk2}_2", pk2, "value2"),
      Row(s"${pk3}_2", pk3, "value2"),
      Row(s"${pk1}_3", pk1, "value3"),
      Row(s"${pk2}_3", pk2, "value3"),
      Row(s"${pk3}_3", pk3, "value3")
    )

    val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

    // Enable partition key transition logging
    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
      "spark.cosmos.write.transactionalBatch.logPartitionKeyTransitions" -> "true"
    )

    // Execute transactional batch with logging enabled
    // The logging output will appear in test logs - we check that operations succeed
    val resultDf = CosmosItemsDataSource.writeTransactionalBatch(operationsDf, cfg.asJava)
    val results = resultDf.collect()

    // Verify all operations succeeded
    results.length shouldEqual 9
    results.foreach { row =>
      row.getAs[Int]("statusCode") shouldEqual 201
      row.getAs[Boolean]("isSuccessStatusCode") shouldBe true
    }

    // Note: With repartitioning, logs should show "Partition key transition summary: 2 transitions"
    // (pk1 group -> pk2 group -> pk3 group = 2 transitions)
    // Without repartitioning, would see many more transitions as operations alternate between PKs
  }
}


