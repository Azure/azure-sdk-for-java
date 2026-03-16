// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.models.{PartitionKey, PartitionKeyBuilder}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SaveMode}

import java.util.UUID
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

//scalastyle:off multiple.string.literals
//scalastyle:off magic.number
class SparkE2ETransactionalBulkWriterITest extends IntegrationSpec
  with Spark
  with AutoCleanableCosmosContainersWithPkAsPartitionKey {

  // These tests require the Cosmos Emulator running locally
  // Run with: -Dspark-e2e_3-5_2-12=true

  private def getBaseWriteConfig(container: String): Map[String, String] = Map(
    "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
    "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
    "spark.cosmos.database" -> cosmosDatabase,
    "spark.cosmos.container" -> container,
    "spark.cosmos.write.bulk.enabled" -> "true",
    "spark.cosmos.write.bulk.transactional" -> "true"
  )


  private val simpleSchema = StructType(Seq(
    StructField("id", StringType, nullable = false),
    StructField("pk", StringType, nullable = false),
    StructField("name", StringType, nullable = false)
  ))

  // =====================================================
  // Happy Path — Each Write Strategy
  // =====================================================

  "transactional write with ItemOverwrite" should "upsert documents atomically" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemOverwrite")

    val batchOperations = Seq(
      Row(s"upsert-1-${UUID.randomUUID()}", partitionKeyValue, "Alice"),
      Row(s"upsert-2-${UUID.randomUUID()}", partitionKeyValue, "Bob"),
      Row(s"upsert-3-${UUID.randomUUID()}", partitionKeyValue, "Charlie")
    )
    val operationsDf = spark.createDataFrame(batchOperations.asJava, simpleSchema)

    operationsDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Verify all 3 docs exist
    val queryResult = container
      .queryItems(s"SELECT * FROM c WHERE c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    queryResult.size() shouldBe 3
  }

  "transactional write with ItemAppend" should "create new documents" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemAppend")

    val item1Id = s"append-1-${UUID.randomUUID()}"
    val item2Id = s"append-2-${UUID.randomUUID()}"

    val batchOperations = Seq(
      Row(item1Id, partitionKeyValue, "Doc1"),
      Row(item2Id, partitionKeyValue, "Doc2")
    )
    val operationsDf = spark.createDataFrame(batchOperations.asJava, simpleSchema)

    operationsDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Verify both docs were created
    val item1 = container.readItem(item1Id, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    item1 should not be null
    item1.getItem.get("name").asText() shouldEqual "Doc1"

    val item2 = container.readItem(item2Id, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    item2 should not be null
    item2.getItem.get("name").asText() shouldEqual "Doc2"
  }

  "transactional write with ItemDelete" should "delete existing documents" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString

    // Seed 3 documents to delete
    val ids = (1 to 3).map { i =>
      val id = s"delete-$i-${UUID.randomUUID()}"
      val seedNode = Utils.getSimpleObjectMapper.createObjectNode()
      seedNode.put("id", id)
      seedNode.put("pk", partitionKeyValue)
      seedNode.put("name", s"ToDelete-$i")
      container.createItem(seedNode, new PartitionKey(partitionKeyValue), null).block()
      id
    }

    // Verify they exist
    val beforeCount = container
      .queryItems(s"SELECT * FROM c WHERE c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    beforeCount.size() shouldBe 3

    // Build delete DataFrame (needs id and pk columns)
    val deleteRows = ids.map(id => Row(id, partitionKeyValue, "placeholder"))
    val deleteDf = spark.createDataFrame(deleteRows.asJava, simpleSchema)

    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemDelete")

    deleteDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Verify all 3 docs were deleted
    val afterCount = container
      .queryItems(s"SELECT * FROM c WHERE c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    afterCount.size() shouldBe 0
  }

  "transactional write with ItemOverwriteIfNotModified" should "create new docs when no ETag present" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemOverwriteIfNotModified")

    val item1Id = s"conditional-${UUID.randomUUID()}"

    // No ETag → falls back to CREATE
    val batchOperations = Seq(
      Row(item1Id, partitionKeyValue, "ConditionalDoc")
    )
    val operationsDf = spark.createDataFrame(batchOperations.asJava, simpleSchema)

    operationsDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Verify item was created
    val item = container.readItem(item1Id, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    item should not be null
    item.getItem.get("name").asText() shouldEqual "ConditionalDoc"
  }

  // =====================================================
  // Error / Atomicity Tests
  // =====================================================

  "transactional write with ItemAppend on existing docs" should "FAIL entire batch on first attempt" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString

    // Seed one document
    val existingId = s"existing-${UUID.randomUUID()}"
    val seedNode = Utils.getSimpleObjectMapper.createObjectNode()
    seedNode.put("id", existingId)
    seedNode.put("pk", partitionKeyValue)
    seedNode.put("name", "AlreadyExists")
    container.createItem(seedNode, new PartitionKey(partitionKeyValue), null).block()

    // Try to create a batch with the existing doc + a new doc (same PK)
    val newId = s"new-${UUID.randomUUID()}"
    val batchOperations = Seq(
      Row(existingId, partitionKeyValue, "Duplicate"),  // 409 — already exists
      Row(newId, partitionKeyValue, "NewDoc")
    )
    val operationsDf = spark.createDataFrame(batchOperations.asJava, simpleSchema)

    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemAppend")

    // Should fail because existingId already exists -> 409 -> batch rolls back
    intercept[Exception] {
      operationsDf.write
        .format("cosmos.oltp")
        .options(writeConfig)
        .mode(SaveMode.Append)
        .save()
    }

    // Verify the new doc was NOT created (rolled back)
    val queryResult = container
      .queryItems(s"SELECT * FROM c WHERE c.id = '$newId' AND c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    queryResult.size() shouldBe 0

    // Verify the original doc is unchanged
    val originalDoc = container.readItem(existingId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    originalDoc.getItem.get("name").asText() shouldEqual "AlreadyExists"
  }

  "transactional batch atomicity" should "roll back all operations on failure" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString

    // Seed doc B only
    val idB = s"docB-${UUID.randomUUID()}"
    val seedNode = Utils.getSimpleObjectMapper.createObjectNode()
    seedNode.put("id", idB)
    seedNode.put("pk", partitionKeyValue)
    seedNode.put("name", "DocB")
    container.createItem(seedNode, new PartitionKey(partitionKeyValue), null).block()

    // Try to delete [A (missing), B (exists)] — A will cause 404 -> entire batch rolls back
    val idA = s"docA-${UUID.randomUUID()}"
    val deleteRows = Seq(
      Row(idA, partitionKeyValue, "phantom"),
      Row(idB, partitionKeyValue, "DocB")
    )
    val deleteDf = spark.createDataFrame(deleteRows.asJava, simpleSchema)

    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemDelete")

    // Should fail because doc A doesn't exist
    intercept[Exception] {
      deleteDf.write
        .format("cosmos.oltp")
        .options(writeConfig)
        .mode(SaveMode.Append)
        .save()
    }

    // Verify doc B was NOT deleted (entire batch rolled back)
    val docB = container.readItem(idB, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    docB should not be null
    docB.getItem.get("name").asText() shouldEqual "DocB"
  }

  "transactional write across multiple partition keys" should "group into separate atomic batches" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val pk1 = UUID.randomUUID().toString
    val pk2 = UUID.randomUUID().toString

    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemOverwrite")

    // Items with 2 different PKs — should be grouped into 2 separate batches
    val batchOperations = Seq(
      Row(s"pk1-1-${UUID.randomUUID()}", pk1, "A"),
      Row(s"pk1-2-${UUID.randomUUID()}", pk1, "B"),
      Row(s"pk2-1-${UUID.randomUUID()}", pk2, "C"),
      Row(s"pk2-2-${UUID.randomUUID()}", pk2, "D")
    )
    val operationsDf = spark.createDataFrame(batchOperations.asJava, simpleSchema)

    operationsDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Verify all docs exist in correct partitions
    val pk1Count = container
      .queryItems(s"SELECT * FROM c WHERE c.pk = '$pk1'", classOf[ObjectNode])
      .collectList()
      .block()
    pk1Count.size() shouldBe 2

    val pk2Count = container
      .queryItems(s"SELECT * FROM c WHERE c.pk = '$pk2'", classOf[ObjectNode])
      .collectList()
      .block()
    pk2Count.size() shouldBe 2
  }

  // =====================================================
  // HPK-Specific E2E Tests
  // =====================================================

  "transactional write with HPK ItemOverwrite" should "upsert documents with 2-level partition key" in {
    // Create container with hierarchical partition keys
    val containerName = s"test-hpk-upsert-${UUID.randomUUID()}"
    val containerProperties = new com.azure.cosmos.models.CosmosContainerProperties(
      containerName,
      new com.azure.cosmos.models.PartitionKeyDefinition()
    )
    val paths = new java.util.ArrayList[String]()
    paths.add("/tenantId")
    paths.add("/userId")
    containerProperties.getPartitionKeyDefinition.setPaths(paths)
    containerProperties.getPartitionKeyDefinition.setKind(com.azure.cosmos.models.PartitionKind.MULTI_HASH)
    containerProperties.getPartitionKeyDefinition.setVersion(com.azure.cosmos.models.PartitionKeyDefinitionVersion.V2)
    cosmosClient.getDatabase(cosmosDatabase).createContainer(containerProperties).block()
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)

    try {
      val writeConfig = Map(
        "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
        "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> containerName,
        "spark.cosmos.write.bulk.enabled" -> "true",
        "spark.cosmos.write.bulk.transactional" -> "true",
        "spark.cosmos.write.strategy" -> "ItemOverwrite"
      )

      val schema = StructType(Seq(
        StructField("id", StringType, nullable = false),
        StructField("tenantId", StringType, nullable = false),
        StructField("userId", StringType, nullable = false),
        StructField("score", IntegerType, nullable = false)
      ))

      val batchOperations = Seq(
        Row(s"doc-1-${UUID.randomUUID()}", "Contoso", "alice", 100),
        Row(s"doc-2-${UUID.randomUUID()}", "Contoso", "alice", 200),
        Row(s"doc-3-${UUID.randomUUID()}", "Contoso", "alice", 300)
      )
      val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

      operationsDf.write
        .format("cosmos.oltp")
        .options(writeConfig)
        .mode(SaveMode.Append)
        .save()

      // Verify all 3 docs exist via query
      val queryResult = container
        .queryItems(s"SELECT * FROM c WHERE c.tenantId = 'Contoso' AND c.userId = 'alice'", classOf[ObjectNode])
        .collectList()
        .block()
      // Should have 3 business docs (marker is actively deleted after success)
      queryResult.size() should be >= 3
    } finally {
      container.delete().block()
    }
  }

  "transactional write with HPK batch grouping" should "create separate batches per full HPK value" in {
    // This test verifies that the String-based PK comparison correctly
    // distinguishes different HPK values that share a common prefix.
    val containerName = s"test-hpk-grouping-${UUID.randomUUID()}"
    val containerProperties = new com.azure.cosmos.models.CosmosContainerProperties(
      containerName,
      new com.azure.cosmos.models.PartitionKeyDefinition()
    )
    val paths = new java.util.ArrayList[String]()
    paths.add("/tenantId")
    paths.add("/userId")
    containerProperties.getPartitionKeyDefinition.setPaths(paths)
    containerProperties.getPartitionKeyDefinition.setKind(com.azure.cosmos.models.PartitionKind.MULTI_HASH)
    containerProperties.getPartitionKeyDefinition.setVersion(com.azure.cosmos.models.PartitionKeyDefinitionVersion.V2)
    cosmosClient.getDatabase(cosmosDatabase).createContainer(containerProperties).block()
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)

    try {
      val writeConfig = Map(
        "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
        "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> containerName,
        "spark.cosmos.write.bulk.enabled" -> "true",
        "spark.cosmos.write.bulk.transactional" -> "true",
        "spark.cosmos.write.strategy" -> "ItemOverwrite"
      )

      val schema = StructType(Seq(
        StructField("id", StringType, nullable = false),
        StructField("tenantId", StringType, nullable = false),
        StructField("userId", StringType, nullable = false),
        StructField("name", StringType, nullable = false)
      ))

      // Two different HPK values — must produce separate batches
      val batchOperations = Seq(
        Row(s"alice-1-${UUID.randomUUID()}", "Contoso", "alice", "A1"),
        Row(s"alice-2-${UUID.randomUUID()}", "Contoso", "alice", "A2"),
        Row(s"bob-1-${UUID.randomUUID()}", "Contoso", "bob", "B1"),
        Row(s"bob-2-${UUID.randomUUID()}", "Contoso", "bob", "B2")
      )
      val operationsDf = spark.createDataFrame(batchOperations.asJava, schema)

      operationsDf.write
        .format("cosmos.oltp")
        .options(writeConfig)
        .mode(SaveMode.Append)
        .save()

      // Verify docs in alice's partition
      val aliceCount = container
        .queryItems(s"SELECT * FROM c WHERE c.tenantId = 'Contoso' AND c.userId = 'alice'", classOf[ObjectNode])
        .collectList()
        .block()
      aliceCount.size() should be >= 2

      // Verify docs in bob's partition
      val bobCount = container
        .queryItems(s"SELECT * FROM c WHERE c.tenantId = 'Contoso' AND c.userId = 'bob'", classOf[ObjectNode])
        .collectList()
        .block()
      bobCount.size() should be >= 2
    } finally {
      container.delete().block()
    }
  }

  // =====================================================
  // Marker Cleanup Verification
  // =====================================================

  "transactional write marker cleanup" should "not leave marker documents after successful write" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemOverwrite")

    val batchOperations = Seq(
      Row(s"marker-test-1-${UUID.randomUUID()}", partitionKeyValue, "Doc1"),
      Row(s"marker-test-2-${UUID.randomUUID()}", partitionKeyValue, "Doc2")
    )
    val operationsDf = spark.createDataFrame(batchOperations.asJava, simpleSchema)

    operationsDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Small delay to allow async marker deletion to complete
    Thread.sleep(2000)

    // Query all docs for this partition key
    val allDocs = container
      .queryItems(s"SELECT * FROM c WHERE c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()

    // Should have only business docs — marker should be actively deleted
    val markerDocs = allDocs.asScala.filter(doc =>
      doc.has("id") && doc.get("id").asText().startsWith("__tbw:"))

    markerDocs.size shouldBe 0  // marker was actively deleted after success
    // Business docs should exist
    val businessDocs = allDocs.asScala.filter(doc =>
      doc.has("id") && !doc.get("id").asText().startsWith("__tbw:"))
    businessDocs.size shouldBe 2
  }
}
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals

