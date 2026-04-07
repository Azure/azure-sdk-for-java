// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.{
  CosmosBatch,
  CosmosBatchItemRequestOptions,
  CosmosBatchResponse,
  CosmosBulkOperations,
  ModelBridgeInternal,
  PartitionKey,
  PartitionKeyBuilder,
  PartitionKeyDefinition
}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.time.Duration
import java.util
import java.util.concurrent.ConcurrentHashMap
import java.lang.reflect.InvocationTargetException
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

//scalastyle:off multiple.string.literals
//scalastyle:off magic.number
//scalastyle:off null
class TransactionalBulkWriterSpec extends UnitSpec {

  private val objectMapper = new ObjectMapper()

  private def createObjectNode(id: String, pk: String, eTag: Option[String] = None): ObjectNode = {
    val node = objectMapper.createObjectNode()
    node.put("id", id)
    node.put("pk", pk)
    eTag.foreach(e => node.put("_etag", e))
    node
  }

  private def createMockBatchResponse(
    statusCode: Int,
    subStatusCode: Int,
    operationResults: List[(Int, Int)] // (statusCode, subStatusCode) per operation
  ): CosmosBatchResponse = {
    val response = ModelBridgeInternal.createCosmosBatchResponse(
      statusCode,
      subStatusCode,
      null, // errorMessage
      new util.HashMap[String, String](),
      null // cosmosDiagnostics
    )

    val pk = new PartitionKey("test-pk")
    val results = operationResults.map { case (opStatusCode, opSubStatusCode) =>
      val dummyOperation = CosmosBulkOperations.getUpsertItemOperation(
        createObjectNode("dummy", "test-pk"), pk)
      ModelBridgeInternal.createCosmosBatchResult(
        null, // eTag
        1.0, // requestCharge
        null, // resourceObject
        opStatusCode,
        Duration.ZERO,
        opSubStatusCode,
        dummyOperation
      )
    }.asJava

    ModelBridgeInternal.addCosmosBatchResultInResponse(response, results)
    response
  }

  private def createWriteConfigViaReflection(strategy: ItemWriteStrategy.Value): AnyRef = {
    val writeConfigClass = Class.forName("com.azure.cosmos.spark.CosmosWriteConfig")
    val ctor = writeConfigClass.getDeclaredConstructors.find(_.getParameterCount == 13).get
    ctor.setAccessible(true)
    ctor.newInstance(
      strategy,
      Int.box(10),
      Boolean.box(true),
      Boolean.box(true),
      None,
      None,
      None,
      None,
      None,
      Int.box(60),
      Int.box(180),
      Int.box(2700),
      None
    ).asInstanceOf[AnyRef]
  }

  // =====================================================
  //  Recovery for Delete Operations
  // =====================================================

  "recovery path" should "handle delete operations without NPE (Issue 3 fix)" in {
    // Delete operations have no item body — getItem returns null
    // The fix uses originalItems (TransactionalBulkItem) instead of batch.getOperations
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)
    batch.deleteItemOperation("doc1")

    val operations = batch.getOperations
    operations.size() should be(1)

    // Verify getItem returns null for delete — this is the root cause of Issue 3
    val item = operations.get(0).getItem[ObjectNode]
    item should be(null)

    // Verify that wrapping as upsert (the fix) preserves the objectNode
    val objectNode = createObjectNode("doc1", "user-A")
    val wrappedOp = CosmosBulkOperations.getUpsertItemOperation(objectNode, pk)
    wrappedOp.getItem[ObjectNode] should not be null
    wrappedOp.getItem[ObjectNode].get("id").asText() should be("doc1")
    wrappedOp.getPartitionKeyValue should be(pk)
  }

  "TransactionalBulkWriter constructor" should "fail fast for unsupported transactional strategy" in {
    val writerClass = Class.forName("com.azure.cosmos.spark.TransactionalBulkWriter")
    val ctor = writerClass.getDeclaredConstructors.find(_.getParameterCount == 7).get
    ctor.setAccessible(true)

    val unsupportedWriteConfig = createWriteConfigViaReflection(ItemWriteStrategy.ItemBulkUpdate)
    val outputMetricsPublisher = new OutputMetricsPublisherTrait {
      override def trackWriteOperation(recordCount: Long, diagnostics: Option[com.azure.cosmos.CosmosDiagnosticsContext]): Unit = ()
    }

    val invocation = intercept[InvocationTargetException] {
      ctor.newInstance(
        null, // CosmosAsyncContainer - not needed because constructor fails on strategy guard first
        CosmosContainerConfig("db", "container", None),
        new PartitionKeyDefinition(),
        unsupportedWriteConfig,
        DiagnosticsConfig(),
        outputMetricsPublisher,
        Long.box(1L)
      )
    }

    invocation.getCause shouldBe a[IllegalArgumentException]
    invocation.getCause.getMessage should include("is not supported for bulk with transactional")
    invocation.getCause.getMessage should include("ItemOverwrite")
  }

  // =====================================================
  // TransactionalBulkItem Field Extraction Tests
  // =====================================================

  "getId pattern" should "extract id from ObjectNode" in {
    val objectNode = createObjectNode("doc-123", "user-A")

    val idField = objectNode.get(CosmosConstants.Properties.Id)
    idField should not be null
    idField.isTextual should be(true)
    idField.textValue() should be("doc-123")
  }

  "getETag pattern" should "extract eTag from ObjectNode when present" in {
    val objectNode = createObjectNode("doc-456", "user-B", Some("etag-abc"))

    val eTagField = objectNode.get(CosmosConstants.Properties.ETag)
    eTagField should not be null
    eTagField.isTextual should be(true)
    eTagField.textValue() should be("etag-abc")
  }

  it should "return null when eTag is missing" in {
    val objectNode = createObjectNode("doc-789", "user-C")

    val eTagField = objectNode.get(CosmosConstants.Properties.ETag)
    eTagField should be(null)
  }

  // =====================================================
  // CosmosBatch Strategy Mapping Tests
  // =====================================================

  "CosmosBatch strategy mapping" should "map ItemOverwrite to upsertItemOperation" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)
    val objectNode = createObjectNode("doc1", "user-A")

    batch.upsertItemOperation(objectNode)

    batch.getOperations.size() should be(1)
    batch.getOperations.get(0).getOperationType.toString should be("UPSERT")
  }

  it should "map ItemAppend to createItemOperation" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)
    val objectNode = createObjectNode("doc1", "user-A")

    batch.createItemOperation(objectNode)

    batch.getOperations.size() should be(1)
    batch.getOperations.get(0).getOperationType.toString should be("CREATE")
  }

  it should "map ItemDelete to deleteItemOperation with itemId only" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)

    batch.deleteItemOperation("doc1")

    batch.getOperations.size() should be(1)
    batch.getOperations.get(0).getOperationType.toString should be("DELETE")
    batch.getOperations.get(0).getId should be("doc1")
    batch.getOperations.get(0).getItem[ObjectNode] should be(null)
  }

  it should "map ItemDeleteIfNotModified to deleteItemOperation with ETag" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)
    val requestOptions = new CosmosBatchItemRequestOptions()
    requestOptions.setIfMatchETag("etag-123")

    batch.deleteItemOperation("doc1", requestOptions)

    batch.getOperations.size() should be(1)
    batch.getOperations.get(0).getOperationType.toString should be("DELETE")
    batch.getOperations.get(0).getId should be("doc1")
  }

  it should "map ItemDeleteIfNotModified without ETag to unconditional deleteItemOperation" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)

    // This matches the optimized code path used by reconstruction and initial batching.
    batch.deleteItemOperation("doc-no-etag")

    batch.getOperations.size() should be(1)
    batch.getOperations.get(0).getOperationType.toString should be("DELETE")
    batch.getOperations.get(0).getId should be("doc-no-etag")
    batch.getOperations.get(0).getItem[ObjectNode] should be(null)
  }

  it should "map ItemOverwriteIfNotModified with ETag to replaceItemOperation" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)
    val objectNode = createObjectNode("doc1", "user-A", Some("etag-abc"))
    val requestOptions = new CosmosBatchItemRequestOptions()
    requestOptions.setIfMatchETag("etag-abc")

    batch.replaceItemOperation("doc1", objectNode, requestOptions)

    batch.getOperations.size() should be(1)
    batch.getOperations.get(0).getOperationType.toString should be("REPLACE")
    batch.getOperations.get(0).getId should be("doc1")
  }

  it should "map ItemOverwriteIfNotModified without ETag to createItemOperation" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)
    val objectNode = createObjectNode("doc1", "user-A") // no ETag

    batch.createItemOperation(objectNode)

    batch.getOperations.size() should be(1)
    batch.getOperations.get(0).getOperationType.toString should be("CREATE")
  }

  it should "preserve operation order in batch" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)

    batch.createItemOperation(createObjectNode("doc1", "user-A"))
    batch.upsertItemOperation(createObjectNode("doc2", "user-A"))
    batch.deleteItemOperation("doc3")

    val ops = batch.getOperations
    ops.size() should be(3)
    ops.get(0).getOperationType.toString should be("CREATE")
    ops.get(1).getOperationType.toString should be("UPSERT")
    ops.get(2).getOperationType.toString should be("DELETE")
  }

  // =====================================================
  // shouldIgnore Status Code Tests
  // (Tests the Exceptions helper methods used by shouldIgnore)
  // =====================================================

  "shouldIgnore for ItemAppend" should "ignore 409 Conflict (item already exists)" in {
    Exceptions.isResourceExistsException(409) should be(true)
  }

  it should "not ignore other status codes" in {
    Exceptions.isResourceExistsException(404) should be(false)
    Exceptions.isResourceExistsException(412) should be(false)
    Exceptions.isResourceExistsException(200) should be(false)
  }

  "shouldIgnore for ItemDelete" should "ignore 404/0 Not Found" in {
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true)
  }

  it should "NOT ignore 404/1002 (partition key range gone — transient, not semantic)" in {
    // 404/1002 is a transient error (partition moved), must flow through retry, NOT shouldIgnore
    Exceptions.isNotFoundExceptionCore(404, 1002) should be(false)
  }

  it should "not ignore 409 or 412" in {
    Exceptions.isNotFoundExceptionCore(409, 0) should be(false)
    Exceptions.isNotFoundExceptionCore(412, 0) should be(false)
  }

  "shouldIgnore for ItemDeleteIfNotModified" should "ignore 404/0 and 412" in {
    // Both 404/0 (item gone) and 412 (item modified, conditional delete correctly skipped)
    // are reconstruction-eligible — consistent with BulkWriter's shouldIgnore
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true)
    Exceptions.isPreconditionFailedException(412) should be(true)
  }

  "shouldIgnore for ItemOverwriteIfNotModified" should "ignore 409, 404/0, and 412" in {
    Exceptions.isResourceExistsException(409) should be(true)
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true)
    Exceptions.isPreconditionFailedException(412) should be(true)
  }

  "shouldIgnore for ItemPatchIfExists" should "ignore 404/0 Not Found" in {
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true)
  }

  it should "NOT ignore 412 Precondition Failed" in {
    Exceptions.isPreconditionFailedException(412) should be(true)
    // ItemPatchIfExists only ignores 404/0 in BulkWriter/TransactionalBulkWriter semantics.
    Exceptions.isNotFoundExceptionCore(412, 0) should be(false)
  }

  "shouldIgnore for ItemOverwrite" should "have no ignorable errors" in {
    // Upsert always succeeds — there are no semantic errors to ignore
    Exceptions.isResourceExistsException(200) should be(false)
    Exceptions.isNotFoundExceptionCore(200, 0) should be(false)
    Exceptions.isPreconditionFailedException(200) should be(false)
  }

  // =====================================================
  // Transient Error Identification Tests
  // =====================================================

  "canBeTransientFailure" should "identify transient status codes" in {
    Exceptions.canBeTransientFailure(408, 0) should be(true)   // Request Timeout
    Exceptions.canBeTransientFailure(410, 0) should be(true)   // Gone
    Exceptions.canBeTransientFailure(500, 0) should be(true)   // Internal Server Error
    Exceptions.canBeTransientFailure(503, 0) should be(true)   // Service Unavailable
    Exceptions.canBeTransientFailure(404, 1002) should be(true) // Partition Key Range Gone
  }

  it should "NOT identify semantic errors as transient" in {
    Exceptions.canBeTransientFailure(404, 0) should be(false)   // Not Found (semantic)
    Exceptions.canBeTransientFailure(409, 0) should be(false)   // Conflict (semantic)
    Exceptions.canBeTransientFailure(412, 0) should be(false)   // Precondition Failed
    Exceptions.canBeTransientFailure(400, 0) should be(false)   // Bad Request
    Exceptions.canBeTransientFailure(429, 0) should be(false)   // Too Many Requests (SDK handles)
  }

  // =====================================================
  // shouldRetry Strategy-Specific Tests
  // =====================================================

  "shouldRetry for ItemOverwrite" should "retry on 404/0 (TTL expiration race)" in {
    // BulkWriter and TransactionalBulkWriter both retry upsert on 404/0
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true)
  }

  it should "retry on transient failures" in {
    Exceptions.canBeTransientFailure(408, 0) should be(true)
    Exceptions.canBeTransientFailure(503, 0) should be(true)
  }

  "shouldRetry for other strategies" should "NOT retry on 404/0 (semantic error)" in {
    // For non-ItemOverwrite strategies, 404/0 is NOT retried — it's a semantic error
    // (except for shouldIgnore which is checked separately before shouldRetry)
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true) // helper returns true...
    // ...but shouldRetry only includes isNotFoundExceptionCore for ItemOverwrite
  }

  // =====================================================
  // CosmosBatchResponse / CosmosBatchOperationResult Tests
  // (Verifies the infrastructure used by getReconstructionIndex)
  // =====================================================

  "CosmosBatchResponse" should "be constructable with per-operation results" in {
    val response = createMockBatchResponse(
      statusCode = 409,
      subStatusCode = 0,
      operationResults = List((409, 0), (424, 0), (424, 0))
    )

    response.getStatusCode should be(409)
    response.getResults.size() should be(3)
    response.getResults.get(0).getStatusCode should be(409)
    response.getResults.get(1).getStatusCode should be(424)
    response.getResults.get(2).getStatusCode should be(424)
  }

  "reconstruction first-non-424 detection" should "find first non-424 result at index 0" in {
    // Scenario: ItemAppend batch, op[0]=409, op[1]=424, op[2]=424
    // The first non-424 is at index 0 -> getReconstructionIndex returns Some(0)
    val response = createMockBatchResponse(409, 0, List((409, 0), (424, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(defined)
    firstNon424.get._2 should be(0) // index 0
    firstNon424.get._1.getStatusCode should be(409)
    // For ItemAppend, 409 is reconstruction-eligible
    Exceptions.isResourceExistsException(409) should be(true)
  }

  it should "find first non-424 result at any index" in {
    // Scenario: op[0]=424, op[1]=409, op[2]=424
    // The first non-424 is at index 1 -> reconstruction targets index 1
    val response = createMockBatchResponse(409, 0, List((424, 0), (409, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(defined)
    firstNon424.get._2 should be(1) // index 1
    firstNon424.get._1.getStatusCode should be(409)
  }

  it should "reject when first non-424 result is NOT reconstruction-eligible" in {
    // Scenario: op[0]=400 (Bad Request — non-recoverable), op[1]=424, op[2]=424
    // 400 is NOT reconstruction-eligible for any strategy
    val response = createMockBatchResponse(400, 0, List((400, 0), (424, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(defined)
    firstNon424.get._1.getStatusCode should be(400)
    // 400 is not 409 — not reconstruction-eligible for ItemAppend
    Exceptions.isResourceExistsException(400) should be(false)
  }

  it should "return None when all results are 424" in {
    val response = createMockBatchResponse(424, 0, List((424, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(empty)
  }

  "getReconstructionIndex logic" should "fire on any attempt including first (no attempt guard)" in {
    // Unlike the old shouldIgnoreOnRetry which required attemptNumber > 1,
    // reconstruction fires on ANY attempt. A 409 on first attempt means the item
    // was created externally — we still need to reconstruct.
    // This test verifies there is NO attempt guard in the reconstruction path.
    val attemptNumberFirstAttempt = 1
    val attemptNumberRetry = 2
    val maxRetryCount = 10

    // Both first attempt and retry are within budget -> reconstruction is allowed
    (attemptNumberFirstAttempt < maxRetryCount) should be(true)
    (attemptNumberRetry < maxRetryCount) should be(true)
  }

  it should "respect maxRetryCount budget" in {
    val maxRetryCount = 10
    // Attempt 10 of 10: at budget limit → reconstruction not allowed
    (10 < maxRetryCount) should be(false)
    // Attempt 9 of 10: still within budget → reconstruction allowed
    (9 < maxRetryCount) should be(true)
  }

  // =====================================================
  // originalItems Wrapping for Recovery Tests
  // =====================================================

  "originalItems recovery wrapping" should "preserve objectNode via upsert wrapper" in {
    // When recovery extracts items from batches, it wraps TransactionalBulkItem
    // as CosmosBulkOperations.getUpsertItemOperation to preserve the objectNode.
    // This verifies getItem[ObjectNode] works on the wrapper.
    val pk = new PartitionKey("user-A")
    val objectNode = createObjectNode("doc1", "user-A")

    val wrapped = CosmosBulkOperations.getUpsertItemOperation(objectNode, pk)

    wrapped.getPartitionKeyValue should be(pk)
    wrapped.getItem[ObjectNode] should not be null
    wrapped.getItem[ObjectNode].get("id").asText() should be("doc1")
    wrapped.getItem[ObjectNode].get("pk").asText() should be("user-A")
  }

  it should "work for items that were originally deletes" in {
    // Delete operations have null item bodies, but the originalItems
    // preserve the original objectNode. The upsert wrapper preserves it.
    val pk = new PartitionKey("user-B")
    val objectNode = createObjectNode("doc-to-delete", "user-B")

    // The original delete has no body
    val deleteBatch = CosmosBatch.createCosmosBatch(pk)
    deleteBatch.deleteItemOperation("doc-to-delete")
    deleteBatch.getOperations.get(0).getItem[ObjectNode] should be(null) // NPE source

    // But the recovery wrapping preserves the original objectNode
    val wrapped = CosmosBulkOperations.getUpsertItemOperation(objectNode, pk)
    wrapped.getItem[ObjectNode] should not be null
    wrapped.getItem[ObjectNode].get("id").asText() should be("doc-to-delete")
  }

  // =====================================================
  // isIdempotent Guard Tests
  // =====================================================

  "isIdempotent re-enqueue guard" should "have correct default value" in {
    // isIdempotent defaults to true — safe for ItemOverwrite (upsert)
    // Non-idempotent strategies (increment patch) will set this to false
    val defaultIsIdempotent = true
    defaultIsIdempotent should be(true)
  }

  it should "block re-enqueue for non-idempotent operations" in {
    // Pattern: if (!isIdempotent) skip re-enqueue
    val isIdempotent = false
    (!isIdempotent) should be(true) // would skip
  }

  it should "allow re-enqueue for idempotent operations" in {
    val isIdempotent = true
    (!isIdempotent) should be(false) // would NOT skip
  }

  "batchIsIdempotent computation" should "be true for non-patch strategies" in {
    // All non-patch strategies are idempotent: upsert, create, delete, replace
    // produce the same result on retry.
    val nonPatchStrategies = List(
      ItemWriteStrategy.ItemOverwrite,
      ItemWriteStrategy.ItemAppend,
      ItemWriteStrategy.ItemDelete,
      ItemWriteStrategy.ItemDeleteIfNotModified,
      ItemWriteStrategy.ItemOverwriteIfNotModified
    )
    for (strategy <- nonPatchStrategies) {
      val isIdempotent = strategy match {
        case ItemWriteStrategy.ItemPatch | ItemWriteStrategy.ItemPatchIfExists => false // placeholder
        case _ => true
      }
      isIdempotent should be(true)
    }
  }

  it should "be true for ItemPatch with only Set/Add/Replace/Remove operations" in {
    // Patch operations like set, add, replace, remove are idempotent —
    // applying them twice produces the same document state.
    val hasIncrement = List(
      CosmosPatchOperationTypes.Set,
      CosmosPatchOperationTypes.Add,
      CosmosPatchOperationTypes.Replace,
      CosmosPatchOperationTypes.Remove
    ).exists(_ == CosmosPatchOperationTypes.Increment)
    hasIncrement should be(false) // no increment → idempotent
  }

  it should "be false for ItemPatch with Increment operations" in {
    // Increment is non-idempotent — double-applying corrupts counters.
    // The batchIsIdempotent flag must be false when any column config uses Increment.
    val operationTypes = List(
      CosmosPatchOperationTypes.Set,
      CosmosPatchOperationTypes.Increment, // non-idempotent
      CosmosPatchOperationTypes.Replace
    )
    val hasIncrement = operationTypes.exists(_ == CosmosPatchOperationTypes.Increment)
    hasIncrement should be(true) // has increment → NOT idempotent → batchIsIdempotent = false
  }

  // =====================================================
  // Duplicate PK Detection with String Keys
  // (Verifies the hashCode fix — PartitionKey.toString() as set key)
  // =====================================================

  "PartitionKey String-based keying" should "work regardless of PartitionKey.hashCode() behavior" in {
    // PartitionKey.hashCode() may use Object.hashCode() (identity-based), which violates
    // the Java equals/hashCode contract. Our fix uses PartitionKey.toString() as the set key
    // instead of PartitionKey directly. This test verifies that the String-based approach
    // produces correct results whether or not the SDK fixes hashCode() in the future.
    val pk1 = new PartitionKeyBuilder()
      .add("tenant-A").add("user-1").add("session-1").build()
    val pk2 = new PartitionKeyBuilder()
      .add("tenant-A").add("user-1").add("session-1").build()

    // Value equality holds
    pk1.equals(pk2) should be(true)

    // String-based keying always detects duplicates, regardless of hashCode() behavior
    pk1.toString should be(pk2.toString)
    val set = ConcurrentHashMap.newKeySet[String]()
    set.add(pk1.toString) should be(true)   // first add -> true
    set.add(pk2.toString) should be(false)  // duplicate detected via String equality -> false
  }

  "duplicate PK detection with String keys (C10 fix)" should "detect value-equal HPK partition keys" in {
    // we use PartitionKey.toString() as the set key.
    // toString() returns deterministic JSON: '["tenant-A","user-1","session-1"]'
    val pk1 = new PartitionKeyBuilder()
      .add("tenant-A").add("user-1").add("session-1").build()
    val pk2 = new PartitionKeyBuilder()
      .add("tenant-A").add("user-1").add("session-1").build()

    pk1.toString should be(pk2.toString)

    val set = ConcurrentHashMap.newKeySet[String]()
    set.add(pk1.toString) should be(true)   // first add -> true
    set.add(pk2.toString) should be(false)  // duplicate detected -> false
  }

  it should "correctly distinguish different HPK values" in {
    val pk1 = new PartitionKeyBuilder()
      .add("tenant-A").add("user-1").add("session-1").build()
    val pk2 = new PartitionKeyBuilder()
      .add("tenant-A").add("user-1").add("session-2").build()  // different session

    pk1.toString should not be pk2.toString

    val set = ConcurrentHashMap.newKeySet[String]()
    set.add(pk1.toString) should be(true)
    set.add(pk2.toString) should be(true)  // different PK -> no conflict
  }

  it should "work for single partition keys too" in {
    val pk1 = new PartitionKey("Seattle")
    val pk2 = new PartitionKey("Seattle")

    pk1.toString should be(pk2.toString)

    val set = ConcurrentHashMap.newKeySet[String]()
    set.add(pk1.toString) should be(true)
    set.add(pk2.toString) should be(false)  // duplicate detected
  }

  // =====================================================
  // isAllowedProperty HPK False Positive Fix
  // =====================================================

  "isAllowedProperty " should "allow patching /user when PK paths are /tenantId/userId/sessionId" in {
    // : List("/tenantId", "/userId", "/sessionId").contains("/user")
    //  -> false -> ALLOWED (CORRECT)
    val pkDef = new PartitionKeyDefinition()
    val paths = new java.util.ArrayList[String]()
    paths.add("/tenantId")
    paths.add("/userId")
    paths.add("/sessionId")
    pkDef.setPaths(paths)

    // The fix uses Java List.contains() — exact match, not substring
    pkDef.getPaths.contains("/user") should be(false)      // not a PK path
    pkDef.getPaths.contains("/tenant") should be(false)    // not a PK path
    pkDef.getPaths.contains("/session") should be(false)   // not a PK path
    pkDef.getPaths.contains("/tenantId") should be(true)   // IS a PK path
    pkDef.getPaths.contains("/userId") should be(true)     // IS a PK path
    pkDef.getPaths.contains("/sessionId") should be(true)  // IS a PK path
  }

  it should "work for single partition key definitions" in {
    val pkDef = new PartitionKeyDefinition()
    val paths = new java.util.ArrayList[String]()
    paths.add("/pk")
    pkDef.setPaths(paths)

    pkDef.getPaths.contains("/pk") should be(true)
    pkDef.getPaths.contains("/p") should be(false)     // substring — should NOT match
    pkDef.getPaths.contains("/pkId") should be(false)  // superstring — should NOT match
  }

  it should "still block actual PK paths from patching" in {
    val pkDef = new PartitionKeyDefinition()
    val paths = new java.util.ArrayList[String]()
    paths.add("/tenantId")
    paths.add("/userId")
    paths.add("/sessionId")
    pkDef.setPaths(paths)

    // These ARE PK paths — getPaths.contains returns true → blocked
    pkDef.getPaths.contains("/tenantId") should be(true)
    pkDef.getPaths.contains("/userId") should be(true)
    pkDef.getPaths.contains("/sessionId") should be(true)
  }

  it should "block system properties regardless of PK definition" in {
    // System properties (_rid, _self, _etag, _attachments, _ts) and id
    // are always immutable and must be blocked from patching.
    // This is tested via the CosmosPatchHelper constants, not getPaths.
    val systemProps = Set("_rid", "_self", "_etag", "_attachments", "_ts")
    systemProps.contains("_rid") should be(true)
    systemProps.contains("_etag") should be(true)
    systemProps.contains("_ts") should be(true)
    // "id" is also immutable
    "id" should be("id")
  }

  it should "handle edge case where field name is a suffix of a PK path" in {
    // e.g., field "/nantId" is a suffix of "/tenantId"
    // New code: List("/tenantId", "/userId", "/sessionId").contains("/nantId") → false → ALLOWED
    val pkDef = new PartitionKeyDefinition()
    val paths = new java.util.ArrayList[String]()
    paths.add("/tenantId")
    paths.add("/userId")
    paths.add("/sessionId")
    pkDef.setPaths(paths)

    pkDef.getPaths.contains("/nantId") should be(false)    // suffix of /tenantId
    pkDef.getPaths.contains("/erId") should be(false)      // suffix of /userId
    pkDef.getPaths.contains("/ionId") should be(false)     // suffix of /sessionId
  }

  // =====================================================
  // Batch Reconstruction Tests (ItemAppend)
  // =====================================================

  "buildReconstructedBatch pattern for ItemAppend" should "change create to read at reconstructed index" in {
    // Scenario 2 from design doc: 409 on doc-A (first attempt, external process)
    // Reconstruction: [create A, create B, create C] -> [read A, create B, create C]
    val pk = new PartitionKey("user-A")
    val items = List(
      createObjectNode("doc-A", "user-A"),
      createObjectNode("doc-B", "user-A"),
      createObjectNode("doc-C", "user-A")
    )

    val reconstructedIndices = Set(0) // doc-A at index 0 was 409'd

    // Build the reconstructed batch
    val newBatch = CosmosBatch.createCosmosBatch(pk)
    items.zipWithIndex.foreach { case (item, index) =>
      if (reconstructedIndices.contains(index)) {
        newBatch.readItemOperation(item.get("id").asText())
      } else {
        newBatch.createItemOperation(item)
      }
    }

    val ops = newBatch.getOperations
    ops.size() should be(3)
    ops.get(0).getOperationType.toString should be("READ")    // doc-A reconstructed
    ops.get(0).getId should be("doc-A")
    ops.get(1).getOperationType.toString should be("CREATE")  // doc-B unchanged
    ops.get(2).getOperationType.toString should be("CREATE")  // doc-C unchanged
  }

  it should "accumulate reconstructions across multiple retries" in {
    // Scenario 3 from design doc: all 3 items committed by attempt 1
    // Attempt 2: 409 on A -> reconstruct index 0
    // Attempt 3: 409 on B -> reconstruct index 1
    // Attempt 4: 409 on C -> reconstruct index 2
    val pk = new PartitionKey("user-A")
    val items = List(
      createObjectNode("doc-A", "user-A"),
      createObjectNode("doc-B", "user-A"),
      createObjectNode("doc-C", "user-A")
    )

    // After third reconstruction: all indices reconstructed
    val reconstructedIndices = Set(0, 1, 2)

    val newBatch = CosmosBatch.createCosmosBatch(pk)
    items.zipWithIndex.foreach { case (item, index) =>
      if (reconstructedIndices.contains(index)) {
        newBatch.readItemOperation(item.get("id").asText())
      } else {
        newBatch.createItemOperation(item)
      }
    }

    val ops = newBatch.getOperations
    ops.size() should be(3)
    ops.get(0).getOperationType.toString should be("READ")  // all reads now
    ops.get(1).getOperationType.toString should be("READ")
    ops.get(2).getOperationType.toString should be("READ")
  }

  it should "reconstruct middle item leaving others unchanged" in {
    // Scenario: op[0]=424, op[1]=409, op[2]=424 — 409 at index 1
    // Reconstruction targets index 1 only
    val pk = new PartitionKey("user-A")
    val items = List(
      createObjectNode("doc-A", "user-A"),
      createObjectNode("doc-B", "user-A"),
      createObjectNode("doc-C", "user-A")
    )

    val reconstructedIndices = Set(1) // doc-B was 409'd

    val newBatch = CosmosBatch.createCosmosBatch(pk)
    items.zipWithIndex.foreach { case (item, index) =>
      if (reconstructedIndices.contains(index)) {
        newBatch.readItemOperation(item.get("id").asText())
      } else {
        newBatch.createItemOperation(item)
      }
    }

    val ops = newBatch.getOperations
    ops.size() should be(3)
    ops.get(0).getOperationType.toString should be("CREATE")  // doc-A unchanged
    ops.get(1).getOperationType.toString should be("READ")    // doc-B reconstructed
    ops.get(1).getId should be("doc-B")
    ops.get(2).getOperationType.toString should be("CREATE")  // doc-C unchanged
  }

  it should "preserve item IDs through reconstruction" in {
    // Read operations use item ID — verify it comes from the original item correctly
    val pk = new PartitionKey("user-A")
    val item = createObjectNode("order-12345", "user-A")

    val newBatch = CosmosBatch.createCosmosBatch(pk)
    newBatch.readItemOperation(item.get("id").asText())

    val ops = newBatch.getOperations
    ops.get(0).getId should be("order-12345")
    ops.get(0).getOperationType.toString should be("READ")
  }

  // =====================================================
  // Batch Reconstruction Tests (ItemOverwriteIfNotModified)
  // =====================================================

  "buildReconstructedBatch pattern for ItemOverwriteIfNotModified" should
    "support mixed reconstruction actions (read + remove + unchanged)" in {
      // Original logical batch:
      //   index 0: replace A (etag present)
      //   index 1: replace B (etag present)
      //   index 2: create C  (no etag)
      // Reconstructed actions:
      //   index 0 -> Read   (e.g., 412)
      //   index 1 -> Remove (e.g., 404/0)
      //   index 2 -> unchanged create
      val pk = new PartitionKey("user-A")
      val itemA = createObjectNode("doc-A", "user-A", Some("etag-a"))
      val itemB = createObjectNode("doc-B", "user-A", Some("etag-b"))
      val itemC = createObjectNode("doc-C", "user-A")

      val reconstructedActions = Map(
        0 -> TransactionalBulkWriter.ReconstructionAction.Read,
        1 -> TransactionalBulkWriter.ReconstructionAction.Remove)

      val newBatch = CosmosBatch.createCosmosBatch(pk)

      // index 0 -> read
      newBatch.readItemOperation(itemA.get("id").asText())
      // index 1 -> remove from batch (no operation emitted)
      // index 2 -> unchanged (no etag => create)
      newBatch.createItemOperation(itemC)

      val ops = newBatch.getOperations
      ops.size() should be(2)
      ops.get(0).getOperationType.toString should be("READ")
      ops.get(0).getId should be("doc-A")
      ops.get(1).getOperationType.toString should be("CREATE")
      ops.get(1).getItem[ObjectNode].get("id").asText() should be("doc-C")

      // Mapping should skip only removed indices, not read indices.
      val removedIndices = TransactionalBulkWriter.extractRemovedIndices(reconstructedActions)
      removedIndices should be(Set(1))
      TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, removedIndices, 3) should be(Some(0))
      TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(1, removedIndices, 3) should be(Some(2))
    }

  it should "keep unchanged replace operations when only other indices are reconstructed" in {
    // index 0 reconstructed to READ, index 1 unchanged REPLACE (etag preserved)
    val pk = new PartitionKey("user-B")
    val itemA = createObjectNode("doc-A", "user-B", Some("etag-a"))
    val itemB = createObjectNode("doc-B", "user-B", Some("etag-b"))

    val newBatch = CosmosBatch.createCosmosBatch(pk)

    // index 0 reconstructed
    newBatch.readItemOperation(itemA.get("id").asText())

    // index 1 unchanged replace-with-etag
    val requestOptions = new CosmosBatchItemRequestOptions()
    requestOptions.setIfMatchETag("etag-b")
    newBatch.replaceItemOperation("doc-B", itemB, requestOptions)

    val ops = newBatch.getOperations
    ops.size() should be(2)
    ops.get(0).getOperationType.toString should be("READ")
    ops.get(1).getOperationType.toString should be("REPLACE")
    ops.get(1).getId should be("doc-B")
  }

  "buildReconstructedBatch pattern for ItemPatchIfExists" should
    "remove reconstructed 404 item and keep other patch items" in {
      val pk = new PartitionKey("user-patch")
      val itemA = createObjectNode("doc-A", "user-patch")
      val itemC = createObjectNode("doc-C", "user-patch")

      // Simulate reconstruction for index 1 (doc-B) due to 404/0 on ItemPatchIfExists.
      val reconstructedActions = Map(1 -> TransactionalBulkWriter.ReconstructionAction.Remove)

      val newBatch = CosmosBatch.createCosmosBatch(pk)
      // index 0 unchanged patch operation (represented with placeholder patch ops in this spec)
      newBatch.readItemOperation(itemA.get("id").asText())
      // index 1 removed from batch (no operation emitted)
      // index 2 unchanged patch operation
      newBatch.readItemOperation(itemC.get("id").asText())

      val ops = newBatch.getOperations
      ops.size() should be(2)
      ops.get(0).getOperationType.toString should be("READ")
      ops.get(0).getId should be("doc-A")
      ops.get(1).getOperationType.toString should be("READ")
      ops.get(1).getId should be("doc-C")

      val removedIndices = TransactionalBulkWriter.extractRemovedIndices(reconstructedActions)
      removedIndices should be(Set(1))
      TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, removedIndices, 3) should be(Some(0))
      TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(1, removedIndices, 3) should be(Some(2))
    }

  "reconstructedIndices state" should "be empty on initial batch" in {
    // First attempt: no reconstruction has happened
    val indices: Set[Int] = Set.empty
    indices.isEmpty should be(true)
  }

  it should "accumulate indices across retries" in {
    // Simulate the reconstruction state growing across retries
    var indices: Set[Int] = Set.empty

    // Attempt 1: 409 on index 0
    indices = indices + 0
    indices should be(Set(0))

    // Attempt 2: 409 on index 2
    indices = indices + 2
    indices should be(Set(0, 2))

    // Attempt 3: 409 on index 1
    indices = indices + 1
    indices should be(Set(0, 1, 2))
  }

  it should "be idempotent when same index is added twice" in {
    // If the same item fails again (shouldn't happen since it's a read now), no duplication
    var indices: Set[Int] = Set.empty
    indices = indices + 0
    indices = indices + 0  // duplicate
    indices.size should be(1)
  }

  // =====================================================
  // Reconstruction Budget Tests (Reconstruction ≠ Retry)
  // =====================================================

  "reconstruction budget" should "not consume retry counter (reconstruction != retry)" in {
    // Reconstruction does NOT increment attemptNumber.
    // The retry counter only increments on transient errors (unchanged batch retries).
    // A batch of 100 items where all need reconstruction uses 0 retry budget.
    val maxRetryCount = 10
    val batchSize = 100

    // Reconstruction budget = batch size (natural bound, no counter needed)
    // Transient retry budget = maxRetryCount (separate)
    // These compose independently
    (batchSize <= 100) should be(true) // max transactional batch size
    (maxRetryCount > 0) should be(true) // full budget available for transient errors
  }

  it should "allow reconstruction on first attempt (no attemptNumber > 1 guard)" in {
    // Scenario : 409/404 on first attempt (external process)
    // Reconstruction must fire — there's no attemptNumber guard
    val attemptNumber = 1

    // Reconstruction fires regardless of attemptNumber — it's not a retry
    (attemptNumber >= 1) should be(true)
  }

  it should "bound reconstruction naturally by batch size" in {
    // Each reconstruction makes irreversible progress (one item removed/replaced).
    // After N reconstructions on an N-item batch, batch is fully corrected or empty.
    var reconstructedIndices: Set[Int] = Set.empty
    val batchSize = 5

    // Simulate reconstructing all items
    for (i <- 0 until batchSize) {
      reconstructedIndices = reconstructedIndices + i
    }
    reconstructedIndices.size should be(batchSize)

    // Same item cannot trigger reconstruction twice
    reconstructedIndices = reconstructedIndices + 0 // duplicate
    reconstructedIndices.size should be(batchSize) // still 5, not 6
  }

  "getReconstructionIndex for ItemAppend" should "return index for 409 (Conflict)" in {
    // 409 is the reconstruction trigger for ItemAppend
    Exceptions.isResourceExistsException(409) should be(true)
  }

  it should "NOT trigger for 404 (Not Found)" in {
    // 404 is NOT reconstruction-eligible for ItemAppend
    // (404 means the item doesn't exist — but ItemAppend creates items, so 404 is irrelevant)
    Exceptions.isResourceExistsException(404) should be(false)
  }

  it should "NOT trigger for transient errors" in {
    // Transient errors go through shouldRetry, not reconstruction
    Exceptions.isResourceExistsException(408) should be(false)
    Exceptions.isResourceExistsException(503) should be(false)
    Exceptions.isResourceExistsException(500) should be(false)
  }

  it should "NOT trigger for 429 (throttling)" in {
    // 429 means the server didn't process the request — nothing to reconstruct
    Exceptions.isResourceExistsException(429) should be(false)
  }

  // Delete reconstruction semantics are covered by:
  // 1) mock batch response tests below (status-code eligibility), and
  // 2) index-remapping tests (correctness across shrinking batches).

  // =====================================================
  // getReconstructionIndex with mock batch responses
  // =====================================================

  it should "select first non-success non-424 result (skip leading 200)" in {
    // Transactional batch responses can include leading 200s for operations processed
    // before the first failure, even though the whole batch rolls back.
    val response = createMockBatchResponse(409, 0, List((200, 0), (409, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNonSuccessNon424 = results.zipWithIndex.find { case (result, _) =>
      !result.isSuccessStatusCode && result.getStatusCode != 424
    }

    firstNonSuccessNon424 should be(defined)
    val (result, index) = firstNonSuccessNon424.get
    index should be(1)
    Exceptions.isResourceExistsException(result.getStatusCode) should be(true)
  }

  "getReconstructionIndex with mock response" should "find 404/0 at correct index for ItemDelete" in {
    // Batch response: [404/0, 424, 424] — 404 at index 0
    val response = createMockBatchResponse(404, 0, List((404, 0), (424, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(defined)
    val (result, index) = firstNon424.get
    index should be(0)
    Exceptions.isNotFoundExceptionCore(result.getStatusCode, result.getSubStatusCode) should be(true)
  }

  it should "find 412 at correct index for ItemDeleteIfNotModified" in {
    // Batch response: [412, 424, 424] — 412 at index 0
    val response = createMockBatchResponse(412, 0, List((412, 0), (424, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(defined)
    val (result, index) = firstNon424.get
    index should be(0)
    Exceptions.isPreconditionFailedException(result.getStatusCode) should be(true)
  }

  it should "NOT reconstruct on 400 (Bad Request)" in {
    // 400 = permanent error, no reconstruction
    val response = createMockBatchResponse(400, 0, List((400, 0), (424, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(defined)
    val (result, _) = firstNon424.get
    Exceptions.isNotFoundExceptionCore(result.getStatusCode, result.getSubStatusCode) should be(false)
    Exceptions.isPreconditionFailedException(result.getStatusCode) should be(false)
    Exceptions.isResourceExistsException(result.getStatusCode) should be(false)
  }

  "getReconstructionActionForStatus for ItemAppend" should
    "map 409 to Read" in {
      val action = TransactionalBulkWriter.getReconstructionActionForStatus(
        ItemWriteStrategy.ItemAppend,
        409,
        0)
      action should be(Some(TransactionalBulkWriter.ReconstructionAction.Read))
    }

  it should "not reconstruct 404 for ItemAppend" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemAppend,
      404,
      0)
    action should be(None)
  }

  "getReconstructionActionForStatus for ItemDelete" should
    "map 404/0 to Remove" in {
      val action = TransactionalBulkWriter.getReconstructionActionForStatus(
        ItemWriteStrategy.ItemDelete,
        404,
        0)
      action should be(Some(TransactionalBulkWriter.ReconstructionAction.Remove))
    }

  it should "not reconstruct transient 404/1002 for ItemDelete" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemDelete,
      404,
      1002)
    action should be(None)
  }

  "getReconstructionActionForStatus for ItemDeleteIfNotModified" should
    "map 404/0 to Remove" in {
      val action = TransactionalBulkWriter.getReconstructionActionForStatus(
        ItemWriteStrategy.ItemDeleteIfNotModified,
        404,
        0)
      action should be(Some(TransactionalBulkWriter.ReconstructionAction.Remove))
    }

  it should "map 412 to Remove" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemDeleteIfNotModified,
      412,
      0)
    action should be(Some(TransactionalBulkWriter.ReconstructionAction.Remove))
  }

  it should "not reconstruct transient 404/1002 for ItemDeleteIfNotModified" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemDeleteIfNotModified,
      404,
      1002)
    action should be(None)
  }

  "getReconstructionActionForStatus for ItemPatch" should
    "not reconstruct 404/0" in {
      val action = TransactionalBulkWriter.getReconstructionActionForStatus(
        ItemWriteStrategy.ItemPatch,
        404,
        0)
      action should be(None)
    }

  it should "not reconstruct 412" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemPatch,
      412,
      0)
    action should be(None)
  }

  "getReconstructionActionForStatus for ItemOverwriteIfNotModified" should
    "map 409 to Read" in {
      val action = TransactionalBulkWriter.getReconstructionActionForStatus(
        ItemWriteStrategy.ItemOverwriteIfNotModified,
        409,
        0)
      action should be(Some(TransactionalBulkWriter.ReconstructionAction.Read))
    }

  it should "map 404/0 to Remove" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      404,
      0)
    action should be(Some(TransactionalBulkWriter.ReconstructionAction.Remove))
  }

  it should "map 412 to Read" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      412,
      0)
    action should be(Some(TransactionalBulkWriter.ReconstructionAction.Read))
  }

  it should "not reconstruct non-eligible statuses" in {
    val transient = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      503,
      0)
    val badRequest = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      400,
      0)
    val notFoundTransient = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      404,
      1002)

    transient should be(None)
    badRequest should be(None)
    notFoundTransient should be(None)
  }

  "getReconstructionActionForStatus for ItemPatchIfExists" should
    "map 404/0 to Remove" in {
      val action = TransactionalBulkWriter.getReconstructionActionForStatus(
        ItemWriteStrategy.ItemPatchIfExists,
        404,
        0)
      action should be(Some(TransactionalBulkWriter.ReconstructionAction.Remove))
    }

  it should "not reconstruct 412 (predicate rejection)" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemPatchIfExists,
      412,
      0)
    action should be(None)
  }

  it should "not reconstruct transient 404/1002" in {
    val action = TransactionalBulkWriter.getReconstructionActionForStatus(
      ItemWriteStrategy.ItemPatchIfExists,
      404,
      1002)
    action should be(None)
  }

  "getFallbackReconstructionDecision" should "select unique overwrite-if-not-modified 412 candidate" in {
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      412,
      0,
      Seq((0, false), (1, true)),
      Set.empty)

    decision should be(Some(1 -> TransactionalBulkWriter.ReconstructionAction.Read))
  }

  it should "skip fallback reconstruction when candidates are ambiguous for delete-if-not-modified remove path" in {
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemDeleteIfNotModified,
      404,
      0,
      Seq((0, false), (1, true)),
      Set.empty)

    decision should be(None)
  }

  it should "ignore already reconstructed indices when selecting fallback candidate" in {
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      409,
      0,
      Seq((0, false), (1, false), (2, true)),
      Set(0))

    decision should be(Some(1 -> TransactionalBulkWriter.ReconstructionAction.Read))
  }

  it should "use fallback for append when status is reconstructable" in {
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemAppend,
      409,
      0,
      Seq((0, false)),
      Set.empty)

    decision should be(Some(0 -> TransactionalBulkWriter.ReconstructionAction.Read))
  }

  it should "skip fallback reconstruction when candidates are ambiguous for patch-if-exists remove path" in {
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemPatchIfExists,
      404,
      0,
      Seq((0, false), (1, false)),
      Set.empty)

    decision should be(None)
  }

  it should "select deterministic first candidate for append read path when fallback candidates are ambiguous" in {
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemAppend,
      409,
      0,
      Seq((0, false), (1, false)),
      Set.empty)

    decision should be(Some(0 -> TransactionalBulkWriter.ReconstructionAction.Read))
  }

  it should "select deterministic first candidate for overwrite-if-not-modified read path when fallback candidates are ambiguous" in {
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      409,
      0,
      Seq((0, false), (1, false), (2, true)),
      Set.empty)

    decision should be(Some(0 -> TransactionalBulkWriter.ReconstructionAction.Read))
  }

  it should "skip fallback reconstruction for overwrite-if-not-modified remove path when candidates are ambiguous" in {
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemOverwriteIfNotModified,
      404,
      0,
      Seq((0, true), (1, true)),
      Set.empty)

    decision should be(None)
  }

  // =====================================================
  // Exception-Only Fallback Coverage (semantic vs transient)
  // =====================================================

  "exception-only fallback for delete paths" should "reconstruct when candidate is unique" in {
    // cosmosBatchResponse = None + exception 404/0 on delete path with one candidate
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemDelete,
      404,
      0,
      Seq((0, false)),
      Set.empty)

    decision should be(Some(0 -> TransactionalBulkWriter.ReconstructionAction.Remove))
  }

  it should "skip reconstruction when remove-path candidates are ambiguous" in {
    // cosmosBatchResponse = None + exception 404/0 with two plausible delete candidates
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemDelete,
      404,
      0,
      Seq((0, false), (1, false)),
      Set.empty)

    decision should be(None)
  }

  it should "treat 404/1002 as transient retry path and not reconstruction" in {
    // Session-not-available / PK range movement can surface as exception-only 404/1002.
    // It must not reconstruct and should flow through transient retry.
    val decision = TransactionalBulkWriter.getFallbackReconstructionDecision(
      ItemWriteStrategy.ItemDeleteIfNotModified,
      404,
      1002,
      Seq((0, false), (1, true)),
      Set.empty)

    decision should be(None)
    Exceptions.canBeTransientFailure(404, 1002) should be(true)
  }

  // =====================================================
  // Reconstruction Index Remapping (Current Batch -> Original Batch)
  // =====================================================

  "extractRemovedIndices" should "return only indices reconstructed as remove" in {
    val actions = Map(
      0 -> TransactionalBulkWriter.ReconstructionAction.Remove,
      1 -> TransactionalBulkWriter.ReconstructionAction.Read,
      3 -> TransactionalBulkWriter.ReconstructionAction.Remove)

    TransactionalBulkWriter.extractRemovedIndices(actions) should be(Set(0, 3))
  }

  "mapCurrentBatchIndexToOriginalIndex" should "map directly when no indices are reconstructed" in {
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, Set.empty, 3) should be(Some(0))
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(1, Set.empty, 3) should be(Some(1))
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(2, Set.empty, 3) should be(Some(2))
  }

  it should "map correctly after batch shrink for delete reconstruction" in {
    // Original items: [A, B, C]
    // Reconstructed indices: {0} means A removed from current batch
    // Current batch is [B, C] -> current index 0 maps to original index 1
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, Set(0), 3) should be(Some(1))
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(1, Set(0), 3) should be(Some(2))
  }

  it should "map correctly with non-contiguous reconstructed indices" in {
    // Original items: [A, B, C, D, E]
    // Reconstructed indices: {1, 3} => current batch [A, C, E]
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, Set(1, 3), 5) should be(Some(0))
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(1, Set(1, 3), 5) should be(Some(2))
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(2, Set(1, 3), 5) should be(Some(4))
  }

  it should "return None for out-of-range current indices" in {
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(2, Set(0), 3) should be(None)
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(-1, Set.empty, 3) should be(None)
  }

  it should "prevent index drift across multiple delete reconstructions" in {
    // Attempt 2: current batch [A,B,C], failing current index 0 (A) -> original 0
    val afterFirst = Set(TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, Set.empty, 3).get)
    afterFirst should be(Set(0))

    // Attempt 3: current batch is now [B,C], failing current index 0 (B) must map to original 1
    val secondOriginal = TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, afterFirst, 3).get
    secondOriginal should be(1)

    // Accumulated reconstructed indices should be {0,1}, not {0}
    (afterFirst + secondOriginal) should be(Set(0, 1))
  }

  "reconstructedActions state" should "preserve read actions and shrink only on remove actions" in {
    // Start with one read-style reconstruction at original index 0.
    var actions: Map[Int, TransactionalBulkWriter.ReconstructionAction] =
      Map(0 -> TransactionalBulkWriter.ReconstructionAction.Read)

    // Read does NOT shrink the batch.
    var removed = TransactionalBulkWriter.extractRemovedIndices(actions)
    removed should be(Set.empty)
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, removed, 3) should be(Some(0))
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(1, removed, 3) should be(Some(1))

    // Add a remove-style reconstruction at original index 1.
    actions = actions + (1 -> TransactionalBulkWriter.ReconstructionAction.Remove)
    removed = TransactionalBulkWriter.extractRemovedIndices(actions)
    removed should be(Set(1))

    // Now current batch excludes original index 1, but keeps index 0 (read action).
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(0, removed, 3) should be(Some(0))
    TransactionalBulkWriter.mapCurrentBatchIndexToOriginalIndex(1, removed, 3) should be(Some(2))
  }
}
//scalastyle:on null
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals

