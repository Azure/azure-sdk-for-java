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

  // Helper method to get root cause of exception
  private def getRootCause(t: Throwable): Throwable = {
    if (t.getCause == null) t else getRootCause(t.getCause)
  }

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
    
    // Execute transactional batch using ItemTransactionalBatch write strategy
    operationsDf.write
      .format("cosmos.oltp")
      .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
      .option("spark.cosmos.accountKey", cosmosMasterKey)
      .option("spark.cosmos.database", cosmosDatabase)
      .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
      .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
      .option("spark.cosmos.write.bulk.enabled", "true")
      .mode(SaveMode.Append)
      .save()

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
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("operationType", StringType, nullable = false),
      StructField("name", StringType, nullable = false)
    ))

    val batchOperations = Seq(
      Row(item1Id, partitionKeyValue, "create", "NewItem"),
      Row(duplicateId, partitionKeyValue, "create", "Duplicate")
    )

    val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

    // This should fail because we're using create operations and duplicateId already exists
    val exception = intercept[Exception] {
      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
        .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .mode(SaveMode.Append)
        .save()
    }

    // Verify the exception message indicates transactional batch failure
    // Spark wraps our exception in "Writing job aborted", check the root cause
    // Transactional batch failures may show as statusCode 424 (Failed Dependency) when one operation fails
    val rootCause = getRootCause(exception)
    assert(rootCause.getMessage.contains("424") || rootCause.getMessage.contains("409"),
      s"Expected transactional batch failure error (424 or 409), got: ${rootCause.getMessage}")

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

    // Create batch operations DataFrame with simplified schema
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

    // Execute transactional batch - defaults to ItemOverwrite (upsert)
    operationsDf.write
      .format("cosmos.oltp")
      .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
      .option("spark.cosmos.accountKey", cosmosMasterKey)
      .option("spark.cosmos.database", cosmosDatabase)
      .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
      .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
      .option("spark.cosmos.write.bulk.enabled", "true")
      .mode(SaveMode.Append)
      .save()

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

    operationsDf.write
      .format("cosmos.oltp")
      .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
      .option("spark.cosmos.accountKey", cosmosMasterKey)
      .option("spark.cosmos.database", cosmosDatabase)
      .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
      .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
      .option("spark.cosmos.write.bulk.enabled", "true")
      .mode(SaveMode.Append)
      .save()

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

    // Now update it using transactional batch (defaults to upsert)
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

    operationsDf.write
      .format("cosmos.oltp")
      .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
      .option("spark.cosmos.accountKey", cosmosMasterKey)
      .option("spark.cosmos.database", cosmosDatabase)
      .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
      .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
      .option("spark.cosmos.write.bulk.enabled", "true")
      .mode(SaveMode.Append)
      .save()

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

    // Should throw exception due to exceeding 100 operations per partition key limit
    val exception = intercept[Exception] {
      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
        .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .mode(SaveMode.Append)
        .save()
    }

    // Spark wraps our exception in "Writing job aborted", check the root cause
    val rootCause = getRootCause(exception)
    rootCause.getMessage should include("exceeds")
    rootCause.getMessage should include("101 operations")
    rootCause.getMessage should include("maximum allowed limit of 100")
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

      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", containerName)
        .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .mode(SaveMode.Append)
        .save()

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
        StructField("valid_to", StringType, nullable = true)
      ))

      val batchOperations = Seq(
        // Close old record by setting valid_to (using upsert to replace)
        Row(oldRecordId, permId, sourceId, 100.0, "2024-06-01T00:00:00Z"),
        // Create new record with new price
        Row(newRecordId, permId, sourceId, 150.0, null)
      )

      val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", containerName)
        .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .mode(SaveMode.Append)
        .save()

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

      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", containerName)
        .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .mode(SaveMode.Append)
        .save()

      // All operations should succeed since they're across different partition key combinations
      // Each unique PermId/SourceId combination is treated as a separate transactional batch
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

      // Should throw exception due to exceeding 100 operations per partition key limit
      val exception = intercept[Exception] {
        operationsDf.write
          .format("cosmos.oltp")
          .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
          .option("spark.cosmos.accountKey", cosmosMasterKey)
          .option("spark.cosmos.database", cosmosDatabase)
          .option("spark.cosmos.container", containerName)
          .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
          .option("spark.cosmos.write.bulk.enabled", "true")
          .mode(SaveMode.Append)
          .save()
      }

      // Spark wraps our exception in "Writing job aborted", check the root cause
      val rootCause = getRootCause(exception)
      rootCause.getMessage should include("exceeds")
      rootCause.getMessage should include("101 operations")
      rootCause.getMessage should include("maximum allowed limit of 100")
    } finally {
      // Clean up container
      container.delete().block()
    }
  }

  it should "handle multiple partition keys with repartitioning" in {
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
    // With repartitioning, operations should be grouped by partition key
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

    // Execute transactional batch
    operationsDf.write
      .format("cosmos.oltp")
      .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
      .option("spark.cosmos.accountKey", cosmosMasterKey)
      .option("spark.cosmos.database", cosmosDatabase)
      .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
      .option("spark.cosmos.write.strategy", "ItemTransactionalBatch")
      .option("spark.cosmos.write.bulk.enabled", "true")
      .mode(SaveMode.Append)
      .save()

    // Verify all operations succeeded
    Seq(pk1, pk2, pk3).foreach { pk =>
      (1 to 3).foreach { i =>
        val item = container.readItem(s"${pk}_$i", new PartitionKey(pk), classOf[ObjectNode]).block()
        item should not be null
        item.getItem.get("value").asText() shouldEqual s"value$i"
      }
    }
  }

  "Transactional batch with per-row operation types" should "support mixed operations in same partition key" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    // Pre-create an item for replace operation
    val itemForReplace = Utils.getSimpleObjectMapper.createObjectNode()
    itemForReplace.put("id", "item-to-replace")
    itemForReplace.put("pk", "mixed-ops")
    itemForReplace.put("prop", "original-value")
    container.createItem(itemForReplace).block()

    // Pre-create item for delete operation
    val itemForDelete = Utils.getSimpleObjectMapper.createObjectNode()
    itemForDelete.put("id", "item-to-delete")
    itemForDelete.put("pk", "mixed-ops")
    itemForDelete.put("prop", "to-be-deleted")
    container.createItem(itemForDelete).block()

    // Create DataFrame with mixed operations
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("operationType", StringType, nullable = false),
      StructField("prop", StringType, nullable = false)
    ))

    val mixedOpsItems = Seq(
      Row("item-new-create", "mixed-ops", "create", "created-value"),
      Row("item-new-upsert", "mixed-ops", "upsert", "upserted-value"),
      Row("item-to-replace", "mixed-ops", "replace", "replaced-value"),
      Row("item-to-delete", "mixed-ops", "delete", "delete-value")
    )

    val mixedOpsDf = spark.createDataFrame(mixedOpsItems.asJava, schema)

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
      "spark.cosmos.write.strategy" -> "ItemTransactionalBatch",
      "spark.cosmos.write.maxRetryCount" -> "3",
      "spark.cosmos.write.bulk.enabled" -> "true"
    )

    // Write with mixed operations
    mixedOpsDf.write.format("cosmos.oltp").mode(SaveMode.Append).options(writeCfg).save()

    // Verify all operations succeeded
    // 1. Check created item
    val createdItem = container.readItem("item-new-create", new PartitionKey("mixed-ops"), classOf[ObjectNode]).block().getItem
    createdItem.get("prop").asText() shouldEqual "created-value"

    // 2. Check upserted item
    val upsertedItem = container.readItem("item-new-upsert", new PartitionKey("mixed-ops"), classOf[ObjectNode]).block().getItem
    upsertedItem.get("prop").asText() shouldEqual "upserted-value"

    // 3. Check replaced item
    val replacedItem = container.readItem("item-to-replace", new PartitionKey("mixed-ops"), classOf[ObjectNode]).block().getItem
    replacedItem.get("prop").asText() shouldEqual "replaced-value"

    // 4. Verify deleted item no longer exists
    try {
      container.readItem("item-to-delete", new PartitionKey("mixed-ops"), classOf[ObjectNode]).block()
      fail("Item should have been deleted")
    } catch {
      case e: CosmosException => e.getStatusCode shouldEqual 404
    }
  }

  it should "rollback all mixed operations when one fails" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    // Pre-create an item
    val existingItem = Utils.getSimpleObjectMapper.createObjectNode()
    existingItem.put("id", "existing-item")
    existingItem.put("pk", "rollback-test")
    existingItem.put("prop", "original")
    container.createItem(existingItem).block()

    // Create DataFrame with mixed operations where create will fail (duplicate)
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("operationType", StringType, nullable = false),
      StructField("prop", StringType, nullable = false)
    ))

    val rollbackItems = Seq(
      Row("new-item-1", "rollback-test", "create", "new-value-1"),
      Row("existing-item", "rollback-test", "create", "duplicate-will-fail"),
      Row("new-item-2", "rollback-test", "upsert", "new-value-2")
    )

    val rollbackDf = spark.createDataFrame(rollbackItems.asJava, schema)

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
      "spark.cosmos.write.strategy" -> "ItemTransactionalBatch",
      "spark.cosmos.write.maxRetryCount" -> "3",
      "spark.cosmos.write.bulk.enabled" -> "true"
    )

    // Attempt write - should fail atomically
    try {
      rollbackDf.write.format("cosmos.oltp").mode(SaveMode.Append).options(writeCfg).save()
      fail("Transactional batch should have failed due to duplicate create")
    } catch {
      case e: Exception =>
        // Expected failure (409 Conflict or 424 Failed Dependency)
        println(s"Expected failure: ${e.getMessage}")
    }

    // Verify NO operations were committed (atomic rollback)
    // 1. Original item should be unchanged
    val originalItem = container.readItem("existing-item", new PartitionKey("rollback-test"), classOf[ObjectNode]).block().getItem
    originalItem.get("prop").asText() shouldEqual "original"

    // 2. new-item-1 should NOT exist (rollback)
    try {
      container.readItem("new-item-1", new PartitionKey("rollback-test"), classOf[ObjectNode]).block()
      fail("new-item-1 should not exist due to rollback")
    } catch {
      case e: CosmosException => e.getStatusCode shouldEqual 404
    }

    // 3. new-item-2 should NOT exist (rollback)
    try {
      container.readItem("new-item-2", new PartitionKey("rollback-test"), classOf[ObjectNode]).block()
      fail("new-item-2 should not exist due to rollback")
    } catch {
      case e: CosmosException => e.getStatusCode shouldEqual 404
    }
  }

  it should "support delete operations in transactional batch" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    // Pre-create items to delete
    val itemToDelete1 = Utils.getSimpleObjectMapper.createObjectNode()
    itemToDelete1.put("id", "delete-item-1")
    itemToDelete1.put("pk", "delete-test")
    itemToDelete1.put("prop", "delete-me-1")
    container.createItem(itemToDelete1).block()

    val itemToDelete2 = Utils.getSimpleObjectMapper.createObjectNode()
    itemToDelete2.put("id", "delete-item-2")
    itemToDelete2.put("pk", "delete-test")
    itemToDelete2.put("prop", "delete-me-2")
    container.createItem(itemToDelete2).block()

    // Create DataFrame with create + delete operations
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("operationType", StringType, nullable = false),
      StructField("prop", StringType, nullable = false)
    ))

    val deleteItems = Seq(
      Row("new-item", "delete-test", "create", "new-value"),
      Row("delete-item-1", "delete-test", "delete", "ignored"),
      Row("delete-item-2", "delete-test", "delete", "ignored")
    )

    val deleteDf = spark.createDataFrame(deleteItems.asJava, schema)

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
      "spark.cosmos.write.strategy" -> "ItemTransactionalBatch",
      "spark.cosmos.write.maxRetryCount" -> "3",
      "spark.cosmos.write.bulk.enabled" -> "true"
    )

    // Execute transactional batch
    deleteDf.write.format("cosmos.oltp").mode(SaveMode.Append).options(writeCfg).save()

    // Verify new item was created
    val newItem = container.readItem("new-item", new PartitionKey("delete-test"), classOf[ObjectNode]).block().getItem
    newItem.get("prop").asText() shouldEqual "new-value"

    // Verify items were deleted
    try {
      container.readItem("delete-item-1", new PartitionKey("delete-test"), classOf[ObjectNode]).block()
      fail("delete-item-1 should have been deleted")
    } catch {
      case e: CosmosException => e.getStatusCode shouldEqual 404
    }

    try {
      container.readItem("delete-item-2", new PartitionKey("delete-test"), classOf[ObjectNode]).block()
      fail("delete-item-2 should have been deleted")
    } catch {
      case e: CosmosException => e.getStatusCode shouldEqual 404
    }
  }

  it should "default to global strategy when operationType column absent" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)

    // Create DataFrame WITHOUT operationType column
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("prop", StringType, nullable = false)
    ))

    val backwardCompatItems = Seq(
      Row("compat-item-1", "compat-test", "value-1"),
      Row("compat-item-2", "compat-test", "value-2")
    )

    val compatDf = spark.createDataFrame(backwardCompatItems.asJava, schema)

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
      "spark.cosmos.write.strategy" -> "ItemTransactionalBatch",
      "spark.cosmos.write.maxRetryCount" -> "3",
      "spark.cosmos.write.bulk.enabled" -> "true"
    )

    // Write without operationType column (should default to upsert)
    compatDf.write.format("cosmos.oltp").mode(SaveMode.Append).options(writeCfg).save()

    // Verify items were created (upsert default for ItemTransactionalBatch)
    val item1 = container.readItem("compat-item-1", new PartitionKey("compat-test"), classOf[ObjectNode]).block().getItem
    item1.get("prop").asText() shouldEqual "value-1"

    val item2 = container.readItem("compat-item-2", new PartitionKey("compat-test"), classOf[ObjectNode]).block().getItem
    item2.get("prop").asText() shouldEqual "value-2"

    // Update items - should succeed with upsert
    val updateItems = Seq(
      Row("compat-item-1", "compat-test", "updated-1"),
      Row("compat-item-2", "compat-test", "updated-2")
    )

    val updateDf = spark.createDataFrame(updateItems.asJava, schema)

    updateDf.write.format("cosmos.oltp").mode(SaveMode.Append).options(writeCfg).save()

    // Verify items were updated
    val updatedItem1 = container.readItem("compat-item-1", new PartitionKey("compat-test"), classOf[ObjectNode]).block().getItem
    updatedItem1.get("prop").asText() shouldEqual "updated-1"

    val updatedItem2 = container.readItem("compat-item-2", new PartitionKey("compat-test"), classOf[ObjectNode]).block().getItem
    updatedItem2.get("prop").asText() shouldEqual "updated-2"
  }
}
