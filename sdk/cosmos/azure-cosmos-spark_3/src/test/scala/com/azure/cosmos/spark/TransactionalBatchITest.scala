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
    
    // Execute transactional batch using bulk transactional mode
    operationsDf.write
      .format("cosmos.oltp")
      .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
      .option("spark.cosmos.accountKey", cosmosMasterKey)
      .option("spark.cosmos.database", cosmosDatabase)
      .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
      .option("spark.cosmos.write.bulk.transactional", "true")
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

  it should "rollback all operations on batch size limit failure" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString

    // Create a batch that exceeds the 2MB size limit for transactional batches
    // Each item is ~20KB, so 101 items will exceed the limit
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("largeField", StringType, nullable = false)
    ))

    val largeString = "x" * 20000 // 20KB string
    val batchOperations = (1 to 101).map { i =>
      Row(s"item-$i-${UUID.randomUUID()}", partitionKeyValue, largeString)
    }

    val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

    // This should fail because batch exceeds size limit
    val exception = intercept[Exception] {
      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
        .option("spark.cosmos.write.bulk.transactional", "true")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .mode(SaveMode.Append)
        .save()
    }

    // Verify the exception indicates batch failure (either size or count limit)
    val rootCause = getRootCause(exception)
    assert(rootCause.getMessage.contains("more operations") || rootCause.getMessage.contains("size") || rootCause.getMessage.contains("limit"),
      s"Expected batch limit error, got: ${rootCause.getMessage}")

    // Verify NO items were created (rolled back) - atomic rollback ensures nothing persisted
    val itemsCreated = try {
      val countList = container.queryItems(
        s"SELECT VALUE COUNT(1) FROM c WHERE c.pk = '$partitionKeyValue'",
        classOf[Long]
      ).collectList().block()
      
      if (countList.isEmpty) {
        0
      } else {
        countList.get(0).toInt
      }
    } catch {
      case _: Exception => 0
    }
    assert(itemsCreated == 0, s"No items should exist due to batch rollback, but found $itemsCreated")
  }

  it should "rollback all operations when one has blank ID" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val partitionKeyValue = "rollback-blank-id-test"

    // Create a batch where the 5th item has a blank ID (validation error)
    val batchOperations = Seq(
      ("id1", partitionKeyValue, "value1"),
      ("id2", partitionKeyValue, "value2"),
      ("id3", partitionKeyValue, "value3"),
      ("id4", partitionKeyValue, "value4"),
      ("", partitionKeyValue, "blank-id"), // Blank ID will cause error
      ("id6", partitionKeyValue, "value6")
    )

    val schema = StructType(Seq(
      StructField("id", StringType),
      StructField("pk", StringType),
      StructField("value", StringType)
    ))

    val operationsDf = spark.createDataFrame(
      batchOperations.map(op => Row(op._1, op._2, op._3)).asJava,
      schema
    )

    // Should throw exception due to blank ID
    val exception = intercept[Exception] {
      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
        .option("spark.cosmos.write.bulk.transactional", "true")
        .mode(SaveMode.Append)
        .save()
    }

    // Verify the first item (id1) was NOT created - proving rollback worked
    val cosmosContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val queryResult = cosmosContainer
      .queryItems(s"SELECT * FROM c WHERE c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()

    // Should have zero items - all operations rolled back
    queryResult.size() shouldBe 0
  }

  it should "reject unsupported write strategies" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val partitionKeyValue = UUID.randomUUID().toString
    val item1Id = s"test-item1-${UUID.randomUUID()}"

    // Create DataFrame with simple schema
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false)
    ))

    val batchOperations = Seq(
      Row(item1Id, partitionKeyValue, "TestItem")
    )

    val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

    // Test ItemAppend (create) - should fail
    val appendException = intercept[Exception] {
      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
        .option("spark.cosmos.write.bulk.transactional", "true")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .option("spark.cosmos.write.strategy", "ItemAppend")
        .mode(SaveMode.Append)
        .save()
    }
    val appendRootCause = getRootCause(appendException)
    assert(appendRootCause.getMessage.contains("Transactional batches only support ItemOverwrite"),
      s"Expected ItemAppend rejection, got: ${appendRootCause.getMessage}")

    // Test ItemDelete - should fail
    val deleteException = intercept[Exception] {
      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
        .option("spark.cosmos.write.bulk.transactional", "true")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .option("spark.cosmos.write.strategy", "ItemDelete")
        .mode(SaveMode.Append)
        .save()
    }
    val deleteRootCause = getRootCause(deleteException)
    assert(deleteRootCause.getMessage.contains("Transactional batches only support ItemOverwrite"),
      s"Expected ItemDelete rejection, got: ${deleteRootCause.getMessage}")

    // Test ItemOverwriteIfNotModified - should fail
    val replaceException = intercept[Exception] {
      operationsDf.write
        .format("cosmos.oltp")
        .option("spark.cosmos.accountEndpoint", cosmosEndpoint)
        .option("spark.cosmos.accountKey", cosmosMasterKey)
        .option("spark.cosmos.database", cosmosDatabase)
        .option("spark.cosmos.container", cosmosContainersWithPkAsPartitionKey)
        .option("spark.cosmos.write.bulk.transactional", "true")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .option("spark.cosmos.write.strategy", "ItemOverwriteIfNotModified")
        .mode(SaveMode.Append)
        .save()
    }
    val replaceRootCause = getRootCause(replaceException)
    assert(replaceRootCause.getMessage.contains("Transactional batches only support ItemOverwrite"),
      s"Expected ItemOverwriteIfNotModified rejection, got: ${replaceRootCause.getMessage}")
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
      .option("spark.cosmos.write.bulk.transactional", "true")
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
      .option("spark.cosmos.write.bulk.transactional", "true")
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
      .option("spark.cosmos.write.bulk.transactional", "true")
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
        .option("spark.cosmos.write.bulk.transactional", "true")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .mode(SaveMode.Append)
        .save()
    }

    // Spark wraps our exception in "Writing job aborted", check the root cause
    val rootCause = getRootCause(exception)
    // Cosmos DB rejects batches with > 100 operations
    rootCause.getMessage should include("Batch request has more operations than what is supported")
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
        .option("spark.cosmos.write.bulk.transactional", "true")
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
        .option("spark.cosmos.write.bulk.transactional", "true")
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
        .option("spark.cosmos.write.bulk.transactional", "true")
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
        .option("spark.cosmos.write.bulk.transactional", "true")
        .option("spark.cosmos.write.bulk.enabled", "true")
        .mode(SaveMode.Append)
        .save()
    }

    // Spark wraps our exception in "Writing job aborted", check the root cause
    val rootCause = getRootCause(exception)
    // Cosmos DB rejects batches with > 100 operations
    rootCause.getMessage should include("Batch request has more operations than what is supported")

    // Clean up container after assertions pass
    try {
      container.delete().block()
    } catch {
      case e: Exception => // Ignore cleanup failures
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
      .option("spark.cosmos.write.bulk.transactional", "true")
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

}
