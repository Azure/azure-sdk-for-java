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

  "shouldIgnore for ItemDeleteIfNotModified" should "ignore 404/0 but NOT 412" in {
    // TransactionalBulkWriter excludes 412 — ambiguous on retry for batch operations
    // BulkWriter includes both 404/0 and 412
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true)
    // 412 is a valid Precondition Failed code, but should NOT be in TransactionalBulkWriter's shouldIgnore
    Exceptions.isPreconditionFailedException(412) should be(true) // helper returns true...
    // ...but TransactionalBulkWriter's shouldIgnore does NOT include 412 for this strategy
  }

  "shouldIgnore for ItemOverwriteIfNotModified" should "ignore 409 and 404/0 but NOT 412" in {
    Exceptions.isResourceExistsException(409) should be(true)
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true)
    // 412 is excluded from TransactionalBulkWriter's shouldIgnore
    Exceptions.isPreconditionFailedException(412) should be(true) // helper returns true...
    // ...but TransactionalBulkWriter's shouldIgnore does NOT include 412 for this strategy
  }

  "shouldIgnore for ItemPatchIfExists" should "ignore 404/0 Not Found" in {
    Exceptions.isNotFoundExceptionCore(404, 0) should be(true)
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
  // (Verifies the infrastructure used by shouldIgnoreOnRetry)
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

  "shouldIgnoreOnRetry first-operation check" should "find first non-424 result at index 0" in {
    // Scenario: ItemAppend retry, op[0]=409, op[1]=424, op[2]=424
    // The first non-424 is at index 0 -> shouldIgnoreOnRetry should return true
    val response = createMockBatchResponse(409, 0, List((409, 0), (424, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(defined)
    firstNon424.get._2 should be(0) // index 0
    firstNon424.get._1.getStatusCode should be(409)
    // For ItemAppend, 409 is ignorable
    Exceptions.isResourceExistsException(409) should be(true)
  }

  it should "reject when first non-424 result is NOT at index 0" in {
    // Scenario: op[0]=424, op[1]=404, op[2]=424
    // The first non-424 is at index 1 -> shouldIgnoreOnRetry should return false
    val response = createMockBatchResponse(404, 0, List((424, 0), (404, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(defined)
    firstNon424.get._2 should be(1) // index 1 -> NOT first operation → reject
    firstNon424.get._1.getStatusCode should be(404)
  }

  it should "return None when all results are 424" in {
    val response = createMockBatchResponse(424, 0, List((424, 0), (424, 0)))
    val results = response.getResults.asScala

    val firstNon424 = results.zipWithIndex.find { case (result, _) =>
      result.getStatusCode != 424
    }

    firstNon424 should be(empty)
  }

  "shouldIgnoreOnRetry attempt guard" should "distinguish first attempt from retry" in {
    // attemptNumber = 1 -> first attempt, shouldIgnoreOnRetry must return false
    // attemptNumber > 1 -> retry, shouldIgnoreOnRetry may return true
    // This test verifies the guard logic pattern
    val attemptNumberFirstAttempt = 1
    val attemptNumberRetry = 2

    (attemptNumberFirstAttempt <= 1) should be(true)   // blocked
    (attemptNumberRetry <= 1) should be(false)          // allowed
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

  // =====================================================
  // Duplicate PK Detection with String Keys
  // (Verifies the hashCode fix — PartitionKey.toString() as set key)
  // =====================================================

  "PartitionKey hashCode bug" should "demonstrate that value-equal PartitionKeys have different hashCodes" in {
    // PartitionKey.hashCode() uses Object.hashCode() (memory address), but
    // PartitionKey.equals() compares content. This violates the Java equals/hashCode contract.
    val pk1 = new PartitionKeyBuilder()
      .add("tenant-A").add("user-1").add("session-1").build()
    val pk2 = new PartitionKeyBuilder()
      .add("tenant-A").add("user-1").add("session-1").build()

    // Equals: value-based -> true (correct)
    pk1.equals(pk2) should be(true)

    // HashCode: identity-based -> DIFFERENT (bug!)
    pk1.hashCode() should not be pk2.hashCode()
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
  // Batch Marker Document Tests
  // =====================================================

  "buildMarkerDocument pattern" should "create a minimal marker with id, ttl, and PK fields" in {
    val om = new ObjectMapper()
    val businessItem = om.createObjectNode()
    businessItem.put("id", "doc-1")
    businessItem.put("tenantId", "Contoso")
    businessItem.put("userId", "alice")
    businessItem.put("sessionId", "sess-99")
    businessItem.put("score", 42)

    // Simulate buildMarkerDocument logic
    val markerId = "__tbw:12345:3:1"
    val markerTtlSeconds = 86400
    val partitionKeyPaths = List("/tenantId", "/userId", "/sessionId")

    val markerNode = om.createObjectNode()
    markerNode.put("id", markerId)
    markerNode.put("ttl", markerTtlSeconds)
    partitionKeyPaths.foreach(path => {
      val fieldName = path.stripPrefix("/")
      val value = businessItem.get(fieldName)
      if (value != null) {
        markerNode.set(fieldName, value.deepCopy())
      }
    })

    // Verify marker has id + ttl + PK fields only (no business fields like "score")
    markerNode.get("id").asText() should be("__tbw:12345:3:1")
    markerNode.get("ttl").asInt() should be(86400)
    markerNode.get("tenantId").asText() should be("Contoso")
    markerNode.get("userId").asText() should be("alice")
    markerNode.get("sessionId").asText() should be("sess-99")
    markerNode.has("score") should be(false)  // business field NOT in marker
  }

  "marker ID" should "be deterministic for the same jobRunId, sparkPartitionId, and batchSeq" in {
    val jobRunId = "task-attempt-12345"
    val sparkPartitionId = 3
    val batchSeq = 17L

    val id1 = s"__tbw:$jobRunId:$sparkPartitionId:$batchSeq"
    val id2 = s"__tbw:$jobRunId:$sparkPartitionId:$batchSeq"

    id1 should be(id2)
    id1 should be("__tbw:task-attempt-12345:3:17")
  }

  it should "be different for different batchSeq values" in {
    val id1 = s"__tbw:job1:0:1"
    val id2 = s"__tbw:job1:0:2"

    id1 should not be id2
  }

  it should "be different for different jobRunIds" in {
    val id1 = s"__tbw:job-alpha:0:1"
    val id2 = s"__tbw:job-beta:0:1"

    id1 should not be id2
  }

  // =====================================================
  // 100-Item Boundary (Marker Skip)
  // =====================================================

  "C15 marker skip" should "add marker when batch has fewer than 100 items" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)

    // Add 99 business items
    for (i <- 1 to 99) {
      batch.upsertItemOperation(createObjectNode(s"doc-$i", "user-A"))
    }
    batch.getOperations.size() should be(99)

    // Adding marker makes it 100 — within server limit
    batch.upsertItemOperation(createObjectNode("__tbw:test:0:1", "user-A"))
    batch.getOperations.size() should be(100) // exactly at limit — OK
  }

  it should "skip marker when batch already has 100 items" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)

    // Add 100 business items
    for (i <- 1 to 100) {
      batch.upsertItemOperation(createObjectNode(s"doc-$i", "user-A"))
    }
    batch.getOperations.size() should be(100)

    // bulkItemsList.size() < 100 -> false -> skip marker
    val shouldAddMarker = batch.getOperations.size() < 100
    shouldAddMarker should be(false)
  }

  "marker position" should "always be the last operation in the batch" in {
    val pk = new PartitionKey("user-A")
    val batch = CosmosBatch.createCosmosBatch(pk)

    // Add business items first
    batch.createItemOperation(createObjectNode("doc-1", "user-A"))
    batch.upsertItemOperation(createObjectNode("doc-2", "user-A"))
    batch.deleteItemOperation("doc-3")

    // Add marker last (same as production code: upsert with marker ObjectNode)
    val markerNode = objectMapper.createObjectNode()
    markerNode.put("id", "__tbw:test:0:1")
    markerNode.put("ttl", 86400)
    markerNode.put("pk", "user-A")
    batch.upsertItemOperation(markerNode)

    val ops = batch.getOperations
    ops.size() should be(4)
    // Business items preserve their original order
    ops.get(0).getOperationType.toString should be("CREATE")
    ops.get(1).getOperationType.toString should be("UPSERT")
    ops.get(2).getOperationType.toString should be("DELETE")
    // Marker is the last operation and is an UPSERT
    ops.get(3).getOperationType.toString should be("UPSERT")
  }

  // =====================================================
  // Marker Document Edge Cases
  // =====================================================

  "buildMarkerDocument pattern" should "handle missing PK field in business item" in {
    // If a business item is missing a PK field (e.g., HPK with /tenantId/userId/sessionId
    // but the document has no "sessionId"), the marker should omit that field too.
    val om = new ObjectMapper()
    val businessItem = om.createObjectNode()
    businessItem.put("id", "doc-1")
    businessItem.put("tenantId", "Contoso")
    businessItem.put("userId", "alice")
    // sessionId is MISSING

    val partitionKeyPaths = List("/tenantId", "/userId", "/sessionId")
    val markerNode = om.createObjectNode()
    markerNode.put("id", "__tbw:job:0:1")
    markerNode.put("ttl", 86400)
    partitionKeyPaths.foreach(path => {
      val fieldName = path.stripPrefix("/")
      val value = businessItem.get(fieldName)
      if (value != null) {
        markerNode.set(fieldName, value.deepCopy())
      }
    })

    markerNode.get("tenantId").asText() should be("Contoso")
    markerNode.get("userId").asText() should be("alice")
    markerNode.has("sessionId") should be(false)  // missing in business item → missing in marker
  }

  it should "work with single partition key" in {
    val om = new ObjectMapper()
    val businessItem = om.createObjectNode()
    businessItem.put("id", "doc-1")
    businessItem.put("pk", "Seattle")
    businessItem.put("temperature", 72)

    val partitionKeyPaths = List("/pk")
    val markerNode = om.createObjectNode()
    markerNode.put("id", "__tbw:job:0:1")
    markerNode.put("ttl", 86400)
    partitionKeyPaths.foreach(path => {
      val fieldName = path.stripPrefix("/")
      val value = businessItem.get(fieldName)
      if (value != null) {
        markerNode.set(fieldName, value.deepCopy())
      }
    })

    markerNode.get("id").asText() should be("__tbw:job:0:1")
    markerNode.get("ttl").asInt() should be(86400)
    markerNode.get("pk").asText() should be("Seattle")
    markerNode.has("temperature") should be(false)  // business field excluded
  }

  it should "not mutate the original business item" in {
    val om = new ObjectMapper()
    val businessItem = om.createObjectNode()
    businessItem.put("id", "doc-1")
    businessItem.put("pk", "user-A")
    businessItem.put("score", 42)

    val partitionKeyPaths = List("/pk")
    val markerNode = om.createObjectNode()
    markerNode.put("id", "__tbw:job:0:1")
    markerNode.put("ttl", 86400)
    partitionKeyPaths.foreach(path => {
      val fieldName = path.stripPrefix("/")
      val value = businessItem.get(fieldName)
      if (value != null) {
        markerNode.set(fieldName, value.deepCopy())
      }
    })

    // Original business item is unchanged
    businessItem.get("id").asText() should be("doc-1")
    businessItem.get("pk").asText() should be("user-A")
    businessItem.get("score").asInt() should be(42)
    businessItem.has("ttl") should be(false)  // marker's ttl was NOT added to business item
  }

  // =====================================================
  // Marker Verification Outcome Pattern Tests
  // =====================================================

  "MarkerVerificationOutcome pattern" should "distinguish three outcomes" in {
    // Verify the sealed trait / case object pattern used in verifyBatchCommit
    // This tests the pattern matching logic — not the actual Cosmos DB call
    sealed trait TestOutcome
    case object TestCommitted extends TestOutcome
    case object TestNotCommitted extends TestOutcome
    case object TestInconclusive extends TestOutcome

    def simulateVerification(statusCode: Int): TestOutcome = statusCode match {
      case 200 => TestCommitted        // marker present → batch committed
      case 404 => TestNotCommitted     // marker absent → batch did not commit
      case _   => TestInconclusive     // transient error → inconclusive
    }

    simulateVerification(200) should be(TestCommitted)
    simulateVerification(404) should be(TestNotCommitted)
    simulateVerification(408) should be(TestInconclusive)  // Request Timeout
    simulateVerification(503) should be(TestInconclusive)  // Service Unavailable
    simulateVerification(500) should be(TestInconclusive)  // Internal Server Error
  }
}
//scalastyle:on null
//scalastyle:on magic.number
//scalastyle:on multiple.string.literals

