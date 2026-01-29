// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;

// End to end sanity tests for basic thin client functionality.
public class ThinClientE2ETest {
    private static final Logger logger = LoggerFactory.getLogger(ThinClientE2ETest.class);
    private static final String thinClientEndpointIndicator = ":10250/";
    private static final String DATABASE_NAME = "db1";
    private static final String CONTAINER_NAME = "ct1";
    private static final String PARTITION_KEY_PATH = "/partitionKey";
    private static final String ID_FIELD = "id";
    private static final String PARTITION_KEY_FIELD = "partitionKey";
    private static final int THROUGHPUT_RU = 35_000;

    private static CosmosAsyncClient sharedClient;
    private static CosmosAsyncDatabase sharedDatabase;
    private static CosmosAsyncContainer sharedContainer;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass(groups = {"thinclient"})
    public void beforeClass() {
        // If running locally, uncomment these lines
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        System.setProperty("COSMOS.HTTP2_ENABLED", "true");

        sharedClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();

        // Create database if not exists
        sharedClient.createDatabaseIfNotExists(DATABASE_NAME).block();
        sharedDatabase = sharedClient.getDatabase(DATABASE_NAME);

        // Create container with 35,000 RU/s manual throughput
        CosmosContainerProperties containerDef = new CosmosContainerProperties(CONTAINER_NAME, PARTITION_KEY_PATH);
        ThroughputProperties throughputConfig = ThroughputProperties.createManualThroughput(THROUGHPUT_RU);
        sharedDatabase.createContainerIfNotExists(containerDef, throughputConfig).block();
        sharedContainer = sharedDatabase.getContainer(CONTAINER_NAME);
    }

    @AfterClass(groups = {"thinclient"})
    public void afterClass() {
        if (sharedClient != null) {
            sharedClient.close();
        }
    }

    /**
     * Helper method to create a test document with id and partitionKey fields.
     */
    private ObjectNode createTestDocument(String id, String partitionKey) {
        ObjectNode doc = mapper.createObjectNode();
        doc.put(ID_FIELD, id);
        doc.put(PARTITION_KEY_FIELD, partitionKey);
        return doc;
    }

    /**
     * Helper method to delete all documents from the container.
     */
    private void emptyContainer() {
        List<ObjectNode> allDocs = sharedContainer
            .queryItems("SELECT * FROM c", new CosmosQueryRequestOptions(), ObjectNode.class)
            .collectList()
            .block();

        if (allDocs != null && !allDocs.isEmpty()) {
            for (ObjectNode doc : allDocs) {
                String id = doc.get(ID_FIELD).asText();
                String pk = doc.get(PARTITION_KEY_FIELD).asText();
                try {
                    sharedContainer.deleteItem(id, new PartitionKey(pk)).block();
                } catch (Exception e) {
                    logger.warn("Failed to delete document with id: {}", id, e);
                }
            }
        }
    }

    /**
     * Helper method to delete specific documents by their ids and partition keys.
     */
    private void deleteDocuments(List<ObjectNode> documents) {
        for (ObjectNode doc : documents) {
            String id = doc.get(ID_FIELD).asText();
            String pk = doc.get(PARTITION_KEY_FIELD).asText();
            try {
                sharedContainer.deleteItem(id, new PartitionKey(pk)).block();
            } catch (Exception e) {
                logger.warn("Failed to delete document with id: {}", id, e);
            }
        }
    }

    /**
     * Helper method to drain all pages from a query using continuation token.
     */
    private List<ObjectNode> drainQueryWithContinuation(String query, CosmosQueryRequestOptions options) {
        List<ObjectNode> allResults = new ArrayList<>();
        List<CosmosDiagnostics> allDiagnostics = new ArrayList<>();
        String continuationToken = null;

        do {
            Iterable<FeedResponse<ObjectNode>> pages = sharedContainer
                .queryItems(query, options, ObjectNode.class)
                .byPage(continuationToken, 10)
                .toIterable();

            for (FeedResponse<ObjectNode> page : pages) {
                allResults.addAll(page.getResults());
                allDiagnostics.add(page.getCosmosDiagnostics());
                continuationToken = page.getContinuationToken();
            }
        } while (continuationToken != null);

        // Assert thin client endpoint used for all requests
        for (CosmosDiagnostics diagnostics : allDiagnostics) {
            assertThinClientEndpointUsed(diagnostics);
        }

        return allResults;
    }

    /**
     * Test: SELECT * FROM C type query
     * 1. Empty the container
     * 2. Create N documents
     * 3. Execute SELECT * FROM C with continuation token draining
     * 4. Assert only thin-client endpoint is used
     * 5. Assert all documents are drained
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQuerySelectAll() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        try {
            // 1. Empty container
            emptyContainer();

            // 2. Create N documents
            int numDocs = 25;
            for (int i = 0; i < numDocs; i++) {
                String id = UUID.randomUUID().toString();
                String pk = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, pk);
                sharedContainer.createItem(doc, new PartitionKey(pk), null).block();
                createdDocs.add(doc);
            }

            // 3. Execute SELECT * FROM C query with draining
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            List<ObjectNode> results = drainQueryWithContinuation("SELECT * FROM c", queryOptions);

            // 5. Assert all documents are drained
            assertThat(results.size()).isEqualTo(numDocs);

            // Verify all created document ids are in results
            List<String> createdIds = createdDocs.stream()
                .map(doc -> doc.get(ID_FIELD).asText())
                .collect(Collectors.toList());
            List<String> resultIds = results.stream()
                .map(doc -> doc.get(ID_FIELD).asText())
                .collect(Collectors.toList());
            assertThat(resultIds.containsAll(createdIds)).isTrue();

        } finally {
            // Cleanup: delete created documents
            deleteDocuments(createdDocs);
        }
    }

    /**
     * Test: SELECT * FROM C WHERE c.id = '' type query
     * 1. Empty the container
     * 2. Create N documents
     * 3. Execute SELECT * FROM C WHERE c.id = @id with continuation token draining
     * 4. Assert only thin-client endpoint is used
     * 5. Assert only the document with specified id is obtained
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQuerySelectById() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        try {
            // 1. Empty container
            emptyContainer();

            // 2. Create N documents
            int numDocs = 10;
            String targetId = null;
            String targetPk = null;

            for (int i = 0; i < numDocs; i++) {
                String id = UUID.randomUUID().toString();
                String pk = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, pk);
                sharedContainer.createItem(doc, new PartitionKey(pk), null).block();
                createdDocs.add(doc);

                // Pick the 5th document as our target
                if (i == 4) {
                    targetId = id;
                    targetPk = pk;
                }
            }

            // 3. Execute SELECT * FROM C WHERE c.id = @id query
            String query = "SELECT * FROM c WHERE c.id = @id";
            SqlQuerySpec querySpec = new SqlQuerySpec(query);
            querySpec.setParameters(Arrays.asList(new SqlParameter("@id", targetId)));

            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

            List<ObjectNode> allResults = new ArrayList<>();
            List<CosmosDiagnostics> allDiagnostics = new ArrayList<>();
            String continuationToken = null;

            do {
                Iterable<FeedResponse<ObjectNode>> pages = sharedContainer
                    .queryItems(querySpec, queryOptions, ObjectNode.class)
                    .byPage(continuationToken, 10)
                    .toIterable();

                for (FeedResponse<ObjectNode> page : pages) {
                    allResults.addAll(page.getResults());
                    allDiagnostics.add(page.getCosmosDiagnostics());
                    continuationToken = page.getContinuationToken();
                }
            } while (continuationToken != null);

            // 4. Assert thin client endpoint used for all requests
            for (CosmosDiagnostics diagnostics : allDiagnostics) {
                assertThinClientEndpointUsed(diagnostics);
            }

            // 5. Assert only document with specified id is obtained
            assertThat(allResults.size()).isEqualTo(1);
            assertThat(allResults.get(0).get(ID_FIELD).asText()).isEqualTo(targetId);
            assertThat(allResults.get(0).get(PARTITION_KEY_FIELD).asText()).isEqualTo(targetPk);

        } finally {
            // Cleanup: delete created documents
            deleteDocuments(createdDocs);
        }
    }

    /**
     * Test: SELECT * FROM C with CosmosQueryRequestOptions partition key
     * 1. Empty the container
     * 2. Create N documents (some with same partition key)
     * 3. Execute SELECT * FROM C with partition key in CosmosQueryRequestOptions
     * 4. Assert only thin-client endpoint is used
     * 5. Assert only documents with specified partition key are obtained
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryWithPartitionKeyOption() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        try {
            // 1. Empty container
            emptyContainer();

            // 2. Create N documents - some with a common partition key
            String targetPk = UUID.randomUUID().toString();
            int docsWithTargetPk = 5;
            int docsWithOtherPk = 5;

            // Create documents with target partition key
            for (int i = 0; i < docsWithTargetPk; i++) {
                String id = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, targetPk);
                sharedContainer.createItem(doc, new PartitionKey(targetPk), null).block();
                createdDocs.add(doc);
            }

            // Create documents with different partition keys
            for (int i = 0; i < docsWithOtherPk; i++) {
                String id = UUID.randomUUID().toString();
                String pk = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, pk);
                sharedContainer.createItem(doc, new PartitionKey(pk), null).block();
                createdDocs.add(doc);
            }

            // 3. Execute SELECT * FROM C with partition key in options
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setPartitionKey(new PartitionKey(targetPk));

            List<ObjectNode> allResults = new ArrayList<>();
            List<CosmosDiagnostics> allDiagnostics = new ArrayList<>();
            String continuationToken = null;

            do {
                Iterable<FeedResponse<ObjectNode>> pages = sharedContainer
                    .queryItems("SELECT * FROM c", queryOptions, ObjectNode.class)
                    .byPage(continuationToken, 10)
                    .toIterable();

                for (FeedResponse<ObjectNode> page : pages) {
                    allResults.addAll(page.getResults());
                    allDiagnostics.add(page.getCosmosDiagnostics());
                    continuationToken = page.getContinuationToken();
                }
            } while (continuationToken != null);

            // 4. Assert thin client endpoint used for all requests
            for (CosmosDiagnostics diagnostics : allDiagnostics) {
                assertThinClientEndpointUsed(diagnostics);
            }

            // 5. Assert only documents with specified partition key are obtained
            assertThat(allResults.size()).isEqualTo(docsWithTargetPk);
            for (ObjectNode result : allResults) {
                assertThat(result.get(PARTITION_KEY_FIELD).asText()).isEqualTo(targetPk);
            }

        } finally {
            // Cleanup: delete created documents
            deleteDocuments(createdDocs);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryLegacy() {
        String idValue = UUID.randomUUID().toString();
        try {
            ObjectNode doc = createTestDocument(idValue, idValue);
            sharedContainer.createItem(doc, new PartitionKey(idValue), null).block();

            String query = "select * from c WHERE c." + PARTITION_KEY_FIELD + "=@id";
            SqlQuerySpec querySpec = new SqlQuerySpec(query);
            querySpec.setParameters(Arrays.asList(new SqlParameter("@id", idValue)));
            CosmosQueryRequestOptions requestOptions =
                new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(idValue));
            FeedResponse<ObjectNode> response = sharedContainer
                .queryItems(querySpec, requestOptions, ObjectNode.class)
                .byPage()
                .blockFirst();

            ObjectNode docFromResponse = response.getResults().get(0);
            assertThat(docFromResponse.get(PARTITION_KEY_FIELD).textValue()).isEqualTo(idValue);
            assertThat(docFromResponse.get(ID_FIELD).textValue()).isEqualTo(idValue);
            assertThinClientEndpointUsed(response.getCosmosDiagnostics());

        } finally {
            // Cleanup
            try {
                sharedContainer.deleteItem(idValue, new PartitionKey(idValue)).block();
            } catch (Exception e) {
                logger.warn("Failed to cleanup document: {}", idValue, e);
            }
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientBulk() {
        String idValue = UUID.randomUUID().toString();
        try {
            ObjectNode doc = createTestDocument(idValue, idValue);

            Flux<CosmosBulkOperationResponse<Object>> responsesFlux = sharedContainer.executeBulkOperations(Flux.just(
                CosmosBulkOperations.getCreateItemOperation(doc, new PartitionKey(idValue))
            ));

            List<CosmosBulkOperationResponse<Object>> responses = responsesFlux.collectList().block();

            assertThat(responses.size()).isEqualTo(1);
            assertThat(responses.get(0).getException()).isNull();
            CosmosBulkItemResponse bulkResponse = responses.get(0).getResponse();
            assertThat(bulkResponse.isSuccessStatusCode()).isEqualTo(true);
            assertThinClientEndpointUsed(bulkResponse.getCosmosDiagnostics());
        } finally {
            // Cleanup
            try {
                sharedContainer.deleteItem(idValue, new PartitionKey(idValue)).block();
            } catch (Exception e) {
                logger.warn("Failed to cleanup document: {}", idValue, e);
            }
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientBatch() {
        String pkValue = UUID.randomUUID().toString();
        String idValue1 = UUID.randomUUID().toString();
        String idValue2 = UUID.randomUUID().toString();
        try {
            ObjectNode doc1 = createTestDocument(idValue1, pkValue);
            ObjectNode doc2 = createTestDocument(idValue2, pkValue);

            CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(pkValue));
            batch.createItemOperation(doc1);
            batch.createItemOperation(doc2);

            CosmosBatchResponse response = sharedContainer
                .executeCosmosBatch(batch)
                .block();

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThinClientEndpointUsed(response.getDiagnostics());
        } finally {
            // Cleanup
            try {
                sharedContainer.deleteItem(idValue1, new PartitionKey(pkValue)).block();
                sharedContainer.deleteItem(idValue2, new PartitionKey(pkValue)).block();
            } catch (Exception e) {
                logger.warn("Failed to cleanup documents", e);
            }
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientIncrementalChangeFeed() {
        String pkValue = UUID.randomUUID().toString();
        String idValue1 = UUID.randomUUID().toString();
        String idValue2 = UUID.randomUUID().toString();
        try {
            ObjectNode doc1 = createTestDocument(idValue1, pkValue);
            ObjectNode doc2 = createTestDocument(idValue2, pkValue);

            CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(pkValue));
            batch.createItemOperation(doc1);
            batch.createItemOperation(doc2);

            sharedContainer.executeCosmosBatch(batch).block();

            FeedResponse<ObjectNode> changeFeedResponse = sharedContainer
                .queryChangeFeed(CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(FeedRange.forFullRange()), ObjectNode.class)
                .byPage()
                .blockFirst();

            assertThat(changeFeedResponse).isNotNull();
            assertThat(changeFeedResponse.getResults()).isNotNull();
            assertThat(changeFeedResponse.getResults().size()).isGreaterThanOrEqualTo(1);
            assertThinClientEndpointUsed(changeFeedResponse.getCosmosDiagnostics());
        } finally {
            // Cleanup
            try {
                sharedContainer.deleteItem(idValue1, new PartitionKey(pkValue)).block();
                sharedContainer.deleteItem(idValue2, new PartitionKey(pkValue)).block();
            } catch (Exception e) {
                logger.warn("Failed to cleanup documents", e);
            }
        }
    }

    private static void assertThinClientEndpointUsed(CosmosDiagnostics diagnostics) {
        assertThat(diagnostics).isNotNull();

        CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
        assertThat(ctx).isNotNull();

        Collection<CosmosDiagnosticsRequestInfo> requests = ctx.getRequestInfo();
        assertThat(requests).isNotNull();
        assertThat(requests.size()).isPositive();

        int requestCountAgainstThinClientEndpoint = 0;

        for (CosmosDiagnosticsRequestInfo requestInfo : requests) {
            logger.info(
                "Endpoint: {}, RequestType: {}, Partition: {}/{}, ActivityId: {}",
                requestInfo.getEndpoint(),
                requestInfo.getRequestType(),
                requestInfo.getPartitionId(),
                requestInfo.getPartitionKeyRangeId(),
                requestInfo.getActivityId());

            if (requestInfo.getEndpoint().contains(thinClientEndpointIndicator)) {
                requestCountAgainstThinClientEndpoint++;
            }
        }

        assertThat(requestCountAgainstThinClientEndpoint).isEqualTo(requests.size());
    }


    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientDocumentPointOperations() {
        String idValue = UUID.randomUUID().toString();
        String idValue2 = null;
        try {
            ObjectNode doc = createTestDocument(idValue, idValue);

            // create
            CosmosItemResponse<ObjectNode> createResponse = sharedContainer.createItem(doc).block();
            assertThat(createResponse.getStatusCode()).isEqualTo(201);
            assertThat(createResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(createResponse.getDiagnostics());

            // read
            CosmosItemResponse<ObjectNode> readResponse = sharedContainer.readItem(idValue, new PartitionKey(idValue), ObjectNode.class).block();
            assertThat(readResponse.getStatusCode()).isEqualTo(200);
            assertThat(readResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(readResponse.getDiagnostics());

            idValue2 = UUID.randomUUID().toString();
            ObjectNode doc2 = createTestDocument(idValue2, idValue);

            // replace
            CosmosItemResponse<ObjectNode> replaceResponse = sharedContainer.replaceItem(doc2, idValue, new PartitionKey(idValue)).block();
            assertThat(replaceResponse.getStatusCode()).isEqualTo(200);
            assertThat(replaceResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(replaceResponse.getDiagnostics());

            CosmosItemResponse<ObjectNode> readAfterReplaceResponse = sharedContainer.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            assertThat(readAfterReplaceResponse.getStatusCode()).isEqualTo(200);
            ObjectNode replacedItemFromRead = readAfterReplaceResponse.getItem();
            assertThat(replacedItemFromRead.get(ID_FIELD).asText()).isEqualTo(idValue2);
            assertThat(replacedItemFromRead.get(PARTITION_KEY_FIELD).asText()).isEqualTo(idValue);
            assertThinClientEndpointUsed(readAfterReplaceResponse.getDiagnostics());

            ObjectNode doc3 = createTestDocument(idValue2, idValue);
            doc3.put("newField", "newValue");

            // upsert
            CosmosItemResponse<ObjectNode> upsertResponse = sharedContainer.upsertItem(doc3, new PartitionKey(idValue), new CosmosItemRequestOptions()).block();
            assertThat(upsertResponse.getStatusCode()).isEqualTo(200);
            assertThat(upsertResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(upsertResponse.getDiagnostics());

            CosmosItemResponse<ObjectNode> readAfterUpsertResponse = sharedContainer.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            ObjectNode upsertedItemFromRead = readAfterUpsertResponse.getItem();
            assertThat(upsertedItemFromRead.get(ID_FIELD).asText()).isEqualTo(idValue2);
            assertThat(upsertedItemFromRead.get(PARTITION_KEY_FIELD).asText()).isEqualTo(idValue);
            assertThat(upsertedItemFromRead.get("newField").asText()).isEqualTo("newValue");
            assertThinClientEndpointUsed(readAfterUpsertResponse.getDiagnostics());

            // patch
            CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
            patchOperations.add("/anotherNewField", "anotherNewValue");
            patchOperations.replace("/newField", "patchedNewField");
            CosmosItemResponse<ObjectNode> patchResponse = sharedContainer.patchItem(idValue2, new PartitionKey(idValue), patchOperations, ObjectNode.class).block();
            assertThat(patchResponse.getStatusCode()).isEqualTo(200);
            assertThat(patchResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(patchResponse.getDiagnostics());

            CosmosItemResponse<ObjectNode> readAfterPatchResponse = sharedContainer.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            ObjectNode patchedItemFromRead = readAfterPatchResponse.getItem();
            assertThat(patchedItemFromRead.get(ID_FIELD).asText()).isEqualTo(idValue2);
            assertThat(patchedItemFromRead.get(PARTITION_KEY_FIELD).asText()).isEqualTo(idValue);
            assertThat(patchedItemFromRead.get("newField").asText()).isEqualTo("patchedNewField");
            assertThat(patchedItemFromRead.get("anotherNewField").asText()).isEqualTo("anotherNewValue");
            assertThinClientEndpointUsed(readAfterPatchResponse.getDiagnostics());

            // delete
            CosmosItemResponse<Object> deleteResponse = sharedContainer.deleteItem(idValue2, new PartitionKey(idValue)).block();
            assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
            assertThat(deleteResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(deleteResponse.getDiagnostics());
            idValue2 = null; // Mark as already deleted
        } finally {
            // Cleanup - only if not already deleted in the test
            if (idValue2 != null) {
                try {
                    sharedContainer.deleteItem(idValue2, new PartitionKey(idValue)).block();
                } catch (Exception e) {
                    logger.warn("Failed to cleanup document: {}", idValue2, e);
                }
            }
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientStoredProcedure() {
        String sprocId = "createDocSproc_" + UUID.randomUUID().toString();
        String pkValue = UUID.randomUUID().toString();
        String docId = UUID.randomUUID().toString();
        try {
            // Create a stored procedure that creates a document
            CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties(
                sprocId,
                "function createDocument(docToCreate) {" +
                    "    var context = getContext();" +
                    "    var container = context.getCollection();" +
                    "    var response = context.getResponse();" +
                    "    var accepted = container.createDocument(" +
                    "        container.getSelfLink()," +
                    "        docToCreate," +
                    "        function(err, docCreated) {" +
                    "            if (err) throw new Error('Error creating document: ' + err.message);" +
                    "            response.setBody(docCreated);" +
                    "        }" +
                    "    );" +
                    "    if (!accepted) throw new Error('Document creation was not accepted');" +
                    "}"
            );

            // Create stored procedure
            CosmosStoredProcedureResponse createResponse = sharedContainer.getScripts()
                .createStoredProcedure(storedProcedureDef)
                .block();
            assertThat(createResponse).isNotNull();
            assertThat(createResponse.getStatusCode()).isEqualTo(201);

            // Execute stored procedure with a specific partition key to create a document
            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
            options.setPartitionKey(new PartitionKey(pkValue));

            String docToCreate = String.format("{\"%s\": \"%s\", \"%s\": \"%s\"}", ID_FIELD, docId, PARTITION_KEY_FIELD, pkValue);

            CosmosStoredProcedureResponse executeResponse = sharedContainer.getScripts()
                .getStoredProcedure(sprocId)
                .execute(Arrays.asList(docToCreate), options)
                .block();

            assertThat(executeResponse).isNotNull();
            assertThat(executeResponse.getStatusCode()).isEqualTo(200);
            assertThat(executeResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(executeResponse.getDiagnostics());

            // Verify the document was created by reading it
            CosmosItemResponse<ObjectNode> readResponse = sharedContainer.readItem(docId, new PartitionKey(pkValue), ObjectNode.class).block();
            assertThat(readResponse).isNotNull();
            assertThat(readResponse.getStatusCode()).isEqualTo(200);
            assertThat(readResponse.getItem().get(ID_FIELD).asText()).isEqualTo(docId);
            assertThat(readResponse.getItem().get(PARTITION_KEY_FIELD).asText()).isEqualTo(pkValue);

        } finally {
            // Cleanup - delete the created document and stored procedure
            try {
                sharedContainer.deleteItem(docId, new PartitionKey(pkValue)).block();
            } catch (Exception e) {
                logger.warn("Failed to cleanup document: {}", docId, e);
            }
            try {
                sharedContainer.getScripts().getStoredProcedure(sprocId).delete().block();
            } catch (Exception e) {
                logger.warn("Failed to cleanup stored procedure: {}", sprocId, e);
            }
        }
    }

    // ==================== Query Plan Feature Tests ====================
    // These tests verify that various query features work correctly through thin client

    /**
     * Test: ORDER BY query
     * Verifies that ORDER BY queries work correctly through thin client
     * Expected: hasOrderBy=true, rewrittenQuery present
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryPlanOrderBy() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        try {
            // Create documents with different _ts values (achieved by creating at different times)
            for (int i = 0; i < 5; i++) {
                String id = UUID.randomUUID().toString();
                String pk = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, pk);
                doc.put("sortField", i);
                sharedContainer.createItem(doc, new PartitionKey(pk), null).block();
                createdDocs.add(doc);
            }

            // Execute ORDER BY query
            String query = "SELECT * FROM c ORDER BY c.sortField";
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

            List<ObjectNode> results = new ArrayList<>();
            List<CosmosDiagnostics> allDiagnostics = new ArrayList<>();

            Iterable<FeedResponse<ObjectNode>> pages = sharedContainer
                .queryItems(query, queryOptions, ObjectNode.class)
                .byPage()
                .toIterable();

            for (FeedResponse<ObjectNode> page : pages) {
                results.addAll(page.getResults());
                allDiagnostics.add(page.getCosmosDiagnostics());
            }

            // Assert thin client endpoint used
            for (CosmosDiagnostics diagnostics : allDiagnostics) {
                assertThinClientEndpointUsed(diagnostics);
            }

            // Verify results are ordered by sortField ascending
            assertThat(results.size()).isGreaterThanOrEqualTo(5);

            // Validate ordering - each result's sortField should be >= previous
            Integer previousSortField = null;
            for (ObjectNode result : results) {
                if (result.has("sortField")) {
                    int currentSortField = result.get("sortField").asInt();
                    if (previousSortField != null) {
                        assertThat(currentSortField)
                            .as("Results should be ordered by sortField ascending")
                            .isGreaterThanOrEqualTo(previousSortField);
                    }
                    previousSortField = currentSortField;
                }
            }

        } finally {
            deleteDocuments(createdDocs);
        }
    }

    /**
     * Test: Aggregate query (COUNT)
     * Verifies that aggregate queries work correctly through thin client
     * Expected: hasAggregates=true, aggregates array populated
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryPlanAggregate() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        String commonPk = UUID.randomUUID().toString();
        try {
            // Create documents
            int numDocs = 5;
            for (int i = 0; i < numDocs; i++) {
                String id = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, commonPk);
                sharedContainer.createItem(doc, new PartitionKey(commonPk), null).block();
                createdDocs.add(doc);
            }

            // Execute COUNT aggregate query
            String query = "SELECT VALUE COUNT(1) FROM c";
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setPartitionKey(new PartitionKey(commonPk));

            FeedResponse<Integer> response = sharedContainer
                .queryItems(query, queryOptions, Integer.class)
                .byPage()
                .blockFirst();

            assertThat(response).isNotNull();
            assertThat(response.getResults().size()).isEqualTo(1);
            assertThat(response.getResults().get(0)).isEqualTo(numDocs);
            assertThinClientEndpointUsed(response.getCosmosDiagnostics());

        } finally {
            deleteDocuments(createdDocs);
        }
    }

    /**
     * Test: Query with partition key filter (single range)
     * Verifies that queries with partition key filters return narrow ranges (not full range)
     * Expected: Single narrow range targeting specific partition
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryPlanWithPartitionKeyFilterSingleRange() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        try {
            // Create a document with specific id
            String targetId = UUID.randomUUID().toString();
            String targetPk = UUID.randomUUID().toString();
            ObjectNode doc = createTestDocument(targetId, targetPk);
            sharedContainer.createItem(doc, new PartitionKey(targetPk), null).block();
            createdDocs.add(doc);

            // Execute query with id filter
            String query = "SELECT * FROM c WHERE c.id = @id";
            SqlQuerySpec querySpec = new SqlQuerySpec(query);
            querySpec.setParameters(Arrays.asList(new SqlParameter("@id", targetId)));

            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

            FeedResponse<ObjectNode> response = sharedContainer
                .queryItems(querySpec, queryOptions, ObjectNode.class)
                .byPage()
                .blockFirst();

            assertThat(response).isNotNull();
            assertThat(response.getResults().size()).isEqualTo(1);
            assertThat(response.getResults().get(0).get(ID_FIELD).asText()).isEqualTo(targetId);
            assertThinClientEndpointUsed(response.getCosmosDiagnostics());

        } finally {
            deleteDocuments(createdDocs);
        }
    }

    /**
     * Test: DISTINCT query
     * Verifies that DISTINCT queries work correctly through thin client
     * Expected: hasDistinct=true
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryPlanDistinct() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        String commonPk = UUID.randomUUID().toString();
        try {
            // Create documents with some duplicate category values
            String[] categories = {"cat1", "cat2", "cat1", "cat3", "cat2"};
            for (int i = 0; i < categories.length; i++) {
                String id = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, commonPk);
                doc.put("category", categories[i]);
                sharedContainer.createItem(doc, new PartitionKey(commonPk), null).block();
                createdDocs.add(doc);
            }

            // Execute DISTINCT query
            String query = "SELECT DISTINCT VALUE c.category FROM c";
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setPartitionKey(new PartitionKey(commonPk));

            List<String> results = sharedContainer
                .queryItems(query, queryOptions, String.class)
                .collectList()
                .block();

            assertThat(results).isNotNull();
            assertThat(results.size()).isEqualTo(3); // cat1, cat2, cat3

            // Get diagnostics from a page query
            FeedResponse<String> response = sharedContainer
                .queryItems(query, queryOptions, String.class)
                .byPage()
                .blockFirst();
            assertThinClientEndpointUsed(response.getCosmosDiagnostics());

        } finally {
            deleteDocuments(createdDocs);
        }
    }

    /**
     * Test: TOP query
     * Verifies that TOP queries work correctly through thin client
     * Expected: hasTop=true, top=10
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryPlanTop() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        String commonPk = UUID.randomUUID().toString();
        try {
            // Create more documents than the TOP limit
            int numDocs = 20;
            for (int i = 0; i < numDocs; i++) {
                String id = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, commonPk);
                sharedContainer.createItem(doc, new PartitionKey(commonPk), null).block();
                createdDocs.add(doc);
            }

            // Execute TOP 10 query
            String query = "SELECT TOP 10 * FROM c";
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setPartitionKey(new PartitionKey(commonPk));

            List<ObjectNode> results = new ArrayList<>();
            List<CosmosDiagnostics> allDiagnostics = new ArrayList<>();

            Iterable<FeedResponse<ObjectNode>> pages = sharedContainer
                .queryItems(query, queryOptions, ObjectNode.class)
                .byPage()
                .toIterable();

            for (FeedResponse<ObjectNode> page : pages) {
                results.addAll(page.getResults());
                allDiagnostics.add(page.getCosmosDiagnostics());
            }

            // Assert thin client endpoint used
            for (CosmosDiagnostics diagnostics : allDiagnostics) {
                assertThinClientEndpointUsed(diagnostics);
            }

            // Verify exactly 10 results returned
            assertThat(results.size()).isEqualTo(10);

        } finally {
            deleteDocuments(createdDocs);
        }
    }

    /**
     * Test: GROUP BY query with aggregates
     * Verifies that GROUP BY queries work correctly through thin client
     * Expected: hasGroupBy=true, hasAggregates=true
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryPlanGroupBy() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        String commonPk = UUID.randomUUID().toString();
        try {
            // Create documents with category field
            String[] categories = {"cat1", "cat1", "cat2", "cat2", "cat2", "cat3"};
            for (int i = 0; i < categories.length; i++) {
                String id = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, commonPk);
                doc.put("category", categories[i]);
                sharedContainer.createItem(doc, new PartitionKey(commonPk), null).block();
                createdDocs.add(doc);
            }

            // Execute GROUP BY query with COUNT aggregate
            String query = "SELECT c.category, COUNT(1) as cnt FROM c GROUP BY c.category";
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setPartitionKey(new PartitionKey(commonPk));

            List<ObjectNode> results = new ArrayList<>();
            List<CosmosDiagnostics> allDiagnostics = new ArrayList<>();

            Iterable<FeedResponse<ObjectNode>> pages = sharedContainer
                .queryItems(query, queryOptions, ObjectNode.class)
                .byPage()
                .toIterable();

            for (FeedResponse<ObjectNode> page : pages) {
                results.addAll(page.getResults());
                allDiagnostics.add(page.getCosmosDiagnostics());
            }

            // Assert thin client endpoint used
            for (CosmosDiagnostics diagnostics : allDiagnostics) {
                assertThinClientEndpointUsed(diagnostics);
            }

            // Verify 3 groups returned (cat1, cat2, cat3)
            assertThat(results.size()).isEqualTo(3);

            // Verify counts are correct
            for (ObjectNode result : results) {
                String category = result.get("category").asText();
                int count = result.get("cnt").asInt();
                switch (category) {
                    case "cat1":
                        assertThat(count).isEqualTo(2);
                        break;
                    case "cat2":
                        assertThat(count).isEqualTo(3);
                        break;
                    case "cat3":
                        assertThat(count).isEqualTo(1);
                        break;
                    default:
                        fail("Unexpected category: " + category);
                }
            }

        } finally {
            deleteDocuments(createdDocs);
        }
    }

    /**
     * Test: Invalid query returns error
     * Verifies that invalid queries return proper errors through thin client
     * Expected: 400 BadRequest error
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryPlanInvalidQuery() {
        // Execute invalid query (typo in SELECT and FROM)
        String invalidQuery = "SELEC * FORM c";
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

        try {
            sharedContainer
                .queryItems(invalidQuery, queryOptions, ObjectNode.class)
                .byPage()
                .blockFirst();
            fail("Expected exception for invalid query");
        } catch (Exception e) {
            // Verify we get a proper error (400 BadRequest is expected)
            assertThat(e).isNotNull();
            logger.info("Expected error for invalid query: {}", e.getMessage());
        }
    }

    /**
     * Test: OFFSET LIMIT query
     * Verifies that OFFSET LIMIT queries work correctly through thin client
     * Expected: hasOffset=true, hasLimit=true
     */
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQueryPlanOffsetLimit() {
        List<ObjectNode> createdDocs = new ArrayList<>();
        String commonPk = UUID.randomUUID().toString();
        try {
            // Create documents with index values for ordering
            int numDocs = 15;
            for (int i = 0; i < numDocs; i++) {
                String id = UUID.randomUUID().toString();
                ObjectNode doc = createTestDocument(id, commonPk);
                doc.put("idx", i);
                sharedContainer.createItem(doc, new PartitionKey(commonPk), null).block();
                createdDocs.add(doc);
            }

            // Execute OFFSET LIMIT query - skip first 5, take next 5
            String query = "SELECT * FROM c ORDER BY c.idx OFFSET 5 LIMIT 5";
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setPartitionKey(new PartitionKey(commonPk));

            List<ObjectNode> results = new ArrayList<>();
            List<CosmosDiagnostics> allDiagnostics = new ArrayList<>();

            Iterable<FeedResponse<ObjectNode>> pages = sharedContainer
                .queryItems(query, queryOptions, ObjectNode.class)
                .byPage()
                .toIterable();

            for (FeedResponse<ObjectNode> page : pages) {
                results.addAll(page.getResults());
                allDiagnostics.add(page.getCosmosDiagnostics());
            }

            // Assert thin client endpoint used
            for (CosmosDiagnostics diagnostics : allDiagnostics) {
                assertThinClientEndpointUsed(diagnostics);
            }

            // Verify exactly 5 results returned (after skipping 5)
            assertThat(results.size()).isEqualTo(5);

            // Verify the idx values are 5, 6, 7, 8, 9
            for (int i = 0; i < results.size(); i++) {
                assertThat(results.get(i).get("idx").asInt()).isEqualTo(i + 5);
            }

        } finally {
            deleteDocuments(createdDocs);
        }
    }
}
