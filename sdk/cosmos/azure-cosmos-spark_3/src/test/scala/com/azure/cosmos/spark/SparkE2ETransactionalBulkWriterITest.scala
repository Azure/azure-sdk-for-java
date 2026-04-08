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

  "transactional write with ItemDeleteIfNotModified" should "skip stale ETag (412) and still delete item with current ETag" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemDeleteIfNotModified")

    val staleId = s"delete-ifnm-stale-${UUID.randomUUID()}"
    val staleSeed = Utils.getSimpleObjectMapper.createObjectNode()
    staleSeed.put("id", staleId)
    staleSeed.put("pk", partitionKeyValue)
    staleSeed.put("name", "BeforeUpdate")
    container.createItem(staleSeed, new PartitionKey(partitionKeyValue), null).block()

    val staleRead = container.readItem(staleId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    val staleEtag = staleRead.getETag

    // Change the document to force ETag mismatch for staleEtag.
    staleSeed.put("name", "AfterUpdate")
    container.upsertItem(staleSeed, new PartitionKey(partitionKeyValue), null).block()

    val deleteId = s"delete-ifnm-ok-${UUID.randomUUID()}"
    val deleteSeed = Utils.getSimpleObjectMapper.createObjectNode()
    deleteSeed.put("id", deleteId)
    deleteSeed.put("pk", partitionKeyValue)
    deleteSeed.put("name", "ToDelete")
    container.createItem(deleteSeed, new PartitionKey(partitionKeyValue), null).block()

    val deleteSchemaWithEtag = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false),
      StructField("_etag", StringType, nullable = false)
    ))

    val deleteEtag = container.readItem(deleteId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block().getETag

    // Write stale row first (single-candidate 412 path): should be skipped and not deleted.
    val staleDeleteDf = spark.createDataFrame(Seq(
      Row(staleId, partitionKeyValue, "IgnoredDueTo412", staleEtag)
    ).asJava, deleteSchemaWithEtag)

    staleDeleteDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    val staleAfter = container.readItem(staleId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    staleAfter should not be null
    staleAfter.getItem.get("name").asText() shouldEqual "AfterUpdate"

    // Then write valid row (single-candidate conditional delete): should be deleted.
    val validDeleteDf = spark.createDataFrame(Seq(
      Row(deleteId, partitionKeyValue, "DeleteMe", deleteEtag)
    ).asJava, deleteSchemaWithEtag)

    validDeleteDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    val deleted = container
      .queryItems(s"SELECT * FROM c WHERE c.id = '$deleteId' AND c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    deleted.size() shouldBe 0
  }

  it should "skip missing item (404/0) and delete existing item" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemDeleteIfNotModified")

    val existingId = s"delete-ifnm-existing-${UUID.randomUUID()}"
    val existingSeed = Utils.getSimpleObjectMapper.createObjectNode()
    existingSeed.put("id", existingId)
    existingSeed.put("pk", partitionKeyValue)
    existingSeed.put("name", "DeleteMe")
    container.createItem(existingSeed, new PartitionKey(partitionKeyValue), null).block()
    val existingEtag = container.readItem(existingId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block().getETag

    val missingId = s"delete-ifnm-missing-${UUID.randomUUID()}"

    val deleteSchemaWithEtag = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false),
      StructField("_etag", StringType, nullable = true)
    ))

    // Write missing row separately so 404 reconstruction has a unique candidate.
    val deleteMissingDf = spark.createDataFrame(Seq(
      Row(missingId, partitionKeyValue, "Missing", "stale-etag")
    ).asJava, deleteSchemaWithEtag)

    deleteMissingDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Write existing row separately to validate delete behavior for present docs.
    val deleteExistingDf = spark.createDataFrame(Seq(
      Row(existingId, partitionKeyValue, "DeleteMe", existingEtag)
    ).asJava, deleteSchemaWithEtag)

    deleteExistingDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    val missingAfter = container
      .queryItems(s"SELECT * FROM c WHERE c.id = '$missingId' AND c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    missingAfter.size() shouldBe 0

    val existingAfter = container
      .queryItems(s"SELECT * FROM c WHERE c.id = '$existingId' AND c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    existingAfter.size() shouldBe 0
  }

  it should "fail fast when _etag is missing or null for ItemDeleteIfNotModified" in {
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemDeleteIfNotModified")

    val missingEtagDf = spark.createDataFrame(Seq(
      Row(s"delete-ifnm-missing-etag-${UUID.randomUUID()}", partitionKeyValue, "DeleteMe")
    ).asJava, simpleSchema)

    val missingEtagException = intercept[Exception] {
      missingEtagDf.write
        .format("cosmos.oltp")
        .options(writeConfig)
        .mode(SaveMode.Append)
        .save()
    }

    val nullEtagSchema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false),
      StructField("_etag", StringType, nullable = true)
    ))

    val nullEtagDf = spark.createDataFrame(Seq(
      Row(s"delete-ifnm-null-etag-${UUID.randomUUID()}", partitionKeyValue, "DeleteMe", null)
    ).asJava, nullEtagSchema)

    val nullEtagException = intercept[Exception] {
      nullEtagDf.write
        .format("cosmos.oltp")
        .options(writeConfig)
        .mode(SaveMode.Append)
        .save()
    }

    def collectCauseMessages(t: Throwable): String = {
      Iterator
        .iterate(t)(_.getCause)
        .takeWhile(_ != null)
        .map(ex => Option(ex.getMessage).getOrElse(""))
        .mkString(" | ")
    }

    val expectedError = "_etag is a mandatory field for write strategy ItemDeleteIfNotModified"
    collectCauseMessages(missingEtagException) should include(expectedError)
    collectCauseMessages(nullEtagException) should include(expectedError)
  }

  it should "skip stale replace (412) and still create no-ETag items" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemOverwriteIfNotModified")

    val staleId = s"overwrite-ifnm-stale-${UUID.randomUUID()}"
    val staleSeed = Utils.getSimpleObjectMapper.createObjectNode()
    staleSeed.put("id", staleId)
    staleSeed.put("pk", partitionKeyValue)
    staleSeed.put("name", "BeforeUpdate")
    container.createItem(staleSeed, new PartitionKey(partitionKeyValue), null).block()

    val staleRead = container.readItem(staleId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    val staleEtag = staleRead.getETag

    // Force stale ETag for the conditional replace row.
    staleSeed.put("name", "AfterUpdate")
    container.upsertItem(staleSeed, new PartitionKey(partitionKeyValue), null).block()

    val newId = s"overwrite-ifnm-create-${UUID.randomUUID()}"

    val schemaWithOptionalEtag = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("pk", StringType, nullable = false),
      StructField("name", StringType, nullable = false),
      StructField("_etag", StringType, nullable = true)
    ))

    val rows = Seq(
      Row(staleId, partitionKeyValue, "ShouldNotOverwrite", staleEtag),
      Row(newId, partitionKeyValue, "CreatedViaFallback", null)
    )
    val df = spark.createDataFrame(rows.asJava, schemaWithOptionalEtag)

    df.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    val staleAfter = container.readItem(staleId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    staleAfter.getItem.get("name").asText() shouldEqual "AfterUpdate"

    val created = container.readItem(newId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    created should not be null
    created.getItem.get("name").asText() shouldEqual "CreatedViaFallback"
  }

  "transactional write with ItemPatchIfExists" should "skip missing docs (404) and patch existing docs" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) ++ Map(
      "spark.cosmos.write.strategy" -> "ItemPatchIfExists",
      "spark.cosmos.write.patch.defaultOperationType" -> "Set",
      "spark.cosmos.write.patch.columnConfigs" -> "[col(name).op(set)]"
    )

    val existingId = s"patch-ifexists-existing-${UUID.randomUUID()}"
    val existingSeed = Utils.getSimpleObjectMapper.createObjectNode()
    existingSeed.put("id", existingId)
    existingSeed.put("pk", partitionKeyValue)
    existingSeed.put("name", "BeforePatch")
    container.createItem(existingSeed, new PartitionKey(partitionKeyValue), null).block()

    val missingId = s"patch-ifexists-missing-${UUID.randomUUID()}"
    // Write missing row separately so 404 reconstruction has a unique candidate.
    val patchMissingDf = spark.createDataFrame(Seq(
      Row(missingId, partitionKeyValue, "IgnoredMissing")
    ).asJava, simpleSchema)

    patchMissingDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Write existing row separately to validate patch behavior for existing docs.
    val patchExistingDf = spark.createDataFrame(Seq(
      Row(existingId, partitionKeyValue, "AfterPatch")
    ).asJava, simpleSchema)

    patchExistingDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    val existingAfter = container.readItem(existingId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    existingAfter.getItem.get("name").asText() shouldEqual "AfterPatch"

    val missingAfter = container
      .queryItems(s"SELECT * FROM c WHERE c.id = '$missingId' AND c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    missingAfter.size() shouldBe 0
  }

  "transactional write with ItemPatch and predicate" should "fail batch when predicate is false (412)" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString
    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) ++ Map(
      "spark.cosmos.write.strategy" -> "ItemPatch",
      "spark.cosmos.write.patch.defaultOperationType" -> "Set",
      "spark.cosmos.write.patch.columnConfigs" -> "[col(name).op(set)]",
      "spark.cosmos.write.patch.filter" -> "from c where c.name = 'Allowed'"
    )

    val blockedId = s"patch-filter-blocked-${UUID.randomUUID()}"
    val blockedSeed = Utils.getSimpleObjectMapper.createObjectNode()
    blockedSeed.put("id", blockedId)
    blockedSeed.put("pk", partitionKeyValue)
    blockedSeed.put("name", "Blocked")
    container.createItem(blockedSeed, new PartitionKey(partitionKeyValue), null).block()

    val allowedId = s"patch-filter-allowed-${UUID.randomUUID()}"
    val allowedSeed = Utils.getSimpleObjectMapper.createObjectNode()
    allowedSeed.put("id", allowedId)
    allowedSeed.put("pk", partitionKeyValue)
    allowedSeed.put("name", "Allowed")
    container.createItem(allowedSeed, new PartitionKey(partitionKeyValue), null).block()

    // First row fails predicate (412); second row should be rolled back by transactional atomicity.
    val patchRows = Seq(
      Row(blockedId, partitionKeyValue, "BlockedAfter"),
      Row(allowedId, partitionKeyValue, "AllowedAfter")
    )
    val patchDf = spark.createDataFrame(patchRows.asJava, simpleSchema)

    intercept[Exception] {
      patchDf.write
        .format("cosmos.oltp")
        .options(writeConfig)
        .mode(SaveMode.Append)
        .save()
    }

    val blockedAfter = container.readItem(blockedId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    blockedAfter.getItem.get("name").asText() shouldEqual "Blocked"

    val allowedAfter = container.readItem(allowedId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    allowedAfter.getItem.get("name").asText() shouldEqual "Allowed"
  }

  // =====================================================
  // Error / Atomicity Tests
  // =====================================================

  "transactional write with ItemAppend on existing docs" should "reconstruct on 409 and still create new docs" in {
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

    operationsDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // 409 conflict on existingId is reconstructed to read; new doc should still be created.
    val queryResult = container
      .queryItems(s"SELECT * FROM c WHERE c.id = '$newId' AND c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    queryResult.size() shouldBe 1

    // Verify the original doc is unchanged
    val originalDoc = container.readItem(existingId, new PartitionKey(partitionKeyValue), classOf[ObjectNode]).block()
    originalDoc.getItem.get("name").asText() shouldEqual "AlreadyExists"
  }

  "transactional batch with ItemDelete" should "reconstruct on missing item (404/0) and allow subsequent valid deletes" in {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val partitionKeyValue = UUID.randomUUID().toString

    // Seed doc B only
    val idB = s"docB-${UUID.randomUUID()}"
    val seedNode = Utils.getSimpleObjectMapper.createObjectNode()
    seedNode.put("id", idB)
    seedNode.put("pk", partitionKeyValue)
    seedNode.put("name", "DocB")
    container.createItem(seedNode, new PartitionKey(partitionKeyValue), null).block()

    // First batch: delete only A (missing) so reconstruction target is unambiguous.
    val idA = s"docA-${UUID.randomUUID()}"
    val deleteMissingDf = spark.createDataFrame(Seq(
      Row(idA, partitionKeyValue, "phantom")
    ).asJava, simpleSchema)

    val writeConfig = getBaseWriteConfig(cosmosContainersWithPkAsPartitionKey) +
      ("spark.cosmos.write.strategy" -> "ItemDelete")

    deleteMissingDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    // Second batch: delete only B (exists) to validate normal delete behavior after reconstruction.
    val deleteExistingDf = spark.createDataFrame(Seq(
      Row(idB, partitionKeyValue, "DocB")
    ).asJava, simpleSchema)

    deleteExistingDf.write
      .format("cosmos.oltp")
      .options(writeConfig)
      .mode(SaveMode.Append)
      .save()

    val docAAfter = container
      .queryItems(s"SELECT * FROM c WHERE c.id = '$idA' AND c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    docAAfter.size() shouldBe 0

    val docBAfter = container
      .queryItems(s"SELECT * FROM c WHERE c.id = '$idB' AND c.pk = '$partitionKeyValue'", classOf[ObjectNode])
      .collectList()
      .block()
    docBAfter.size() shouldBe 0
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
      // Should have the 3 business docs written by the batch
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

}
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals

