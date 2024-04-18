// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.PartitionKeyBasedBloomFilter;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.guava25.base.Charsets;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnel;
import com.azure.cosmos.implementation.guava25.hash.PrimitiveSink;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchItemRequestOptions;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class SessionConsistencyWithRegionScopingTests extends TestSuiteBase {

    private static final Logger logger = LoggerFactory.getLogger(SessionConsistencyWithRegionScopingTests.class);
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor cosmosClientBuilderAccessor
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private Map<String, String> writeRegionMap;
    private Map<String, String> readRegionMap;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public SessionConsistencyWithRegionScopingTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region", "multi-master"})
    public void beforeClass() {

        try (CosmosAsyncClient tempClient = getClientBuilder().buildAsyncClient()) {

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(tempClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            this.writeRegionMap = getRegionMap(databaseAccount, true);
            this.readRegionMap = getRegionMap(databaseAccount, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void pointReadYourPointCreate_BothFromFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();
            CosmosItemResponse<TestObject> testObjectFromRead = containerWithSinglePartition.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document read should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = 80 * TIMEOUT)
    public void pointReadAfterPartitionSplitAndPointCreate_BothFromFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);

        String databaseId = UUID.randomUUID() + "-" + "db";
        String containerId = UUID.randomUUID() + "-" + "container";

        client.createDatabase(databaseId).block();
        CosmosAsyncDatabase asyncDatabase = client.getDatabase(databaseId);
        asyncDatabase.createContainerIfNotExists(new CosmosContainerProperties(containerId, "/mypk")).block();
        CosmosAsyncContainer asyncContainer = asyncDatabase.getContainer(containerId);

        Thread.sleep(10_000);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            asyncContainer.createItem(testObjectToBeCreated).block();

            ThroughputResponse throughputResponse = asyncContainer.replaceThroughput(ThroughputProperties.createManualThroughput(10_100)).block();

            while (true) {
                assert throughputResponse != null;
                boolean isReplacePending = throughputResponse.isReplacePending();

                if (!isReplacePending) {
                    break;
                }
                throughputResponse = asyncContainer.readThroughput().block();
                Thread.sleep(10_000);
                logger.info("Waiting for split to complete...");
            }

            logger.info("Split complete!");
            CosmosItemResponse<TestObject> testObjectFromRead = asyncContainer.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document read should have succeeded...");
        } finally {
            safeDeleteCollection(asyncContainer);
            safeDeleteDatabase(asyncDatabase);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void pointReadYourPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegion() throws InterruptedException {

        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            Thread.sleep(1_000);

            CosmosItemResponse<TestObject> testObjectFromRead = containerWithSinglePartition.readItem(id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(preferredRegions.get(0))), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document read should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = 80 * TIMEOUT)
    public void pointReadAfterPartitionSplitAndPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegion() throws InterruptedException {

        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);

        String databaseId = UUID.randomUUID() + "-" + "db";
        String containerId = UUID.randomUUID() + "-" + "container";

        client.createDatabase(databaseId).block();
        CosmosAsyncDatabase asyncDatabase = client.getDatabase(databaseId);
        asyncDatabase.createContainerIfNotExists(new CosmosContainerProperties(containerId, "/mypk")).block();
        CosmosAsyncContainer asyncContainer = asyncDatabase.getContainer(containerId);

        Thread.sleep(10_000);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            asyncContainer.createItem(testObjectToBeCreated).block();

            ThroughputResponse throughputResponse = asyncContainer.replaceThroughput(ThroughputProperties.createManualThroughput(10_100)).block();

            while (true) {
                assert throughputResponse != null;
                boolean isReplacePending = throughputResponse.isReplacePending();

                if (!isReplacePending) {
                    break;
                }
                throughputResponse = asyncContainer.readThroughput().block();
                Thread.sleep(10_000);
                logger.info("Waiting for split to complete...");
            }

            logger.info("Split complete!");
            CosmosItemResponse<TestObject> testObjectFromRead = asyncContainer.readItem(id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(preferredRegions.get(0))), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document read should have succeeded...");
        } finally {
            safeDeleteCollection(asyncContainer);
            safeDeleteDatabase(asyncDatabase);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void pointReadYourLatestUpsert_UpsertsFromPreferredRegionReadFromPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            testObjectToBeCreated.setStringProp(UUID.randomUUID().toString());

            TestObject testObjectModified = testObjectToBeCreated;

            containerWithSinglePartition.upsertItem(testObjectModified, new PartitionKey(pk), new CosmosItemRequestOptions()).block();

            CosmosItemResponse<TestObject> testObjectFromRead = containerWithSinglePartition.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectModified, testObjectFromRead.getItem());
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document read should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void queryTargetedToLogicalPartitionFollowingCreates_queryFromFirstPreferredRegionCreateInFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());
        Map<String, TestObject> idToTestObjectsCreated = new HashMap<>();

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();
            idToTestObjectsCreated.put(testObjectToBeCreated.getId(), testObjectToBeCreated);

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c");

            List<TestObject> testObjectsFromQuery = containerWithSinglePartition
                .queryItems(querySpec, new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(pk)), TestObject.class)
                .collectList()
                .block();
            assertThat(testObjectsFromQuery).isNotNull();

            Map<String, TestObject> idToTestObjectsFromQuery = testObjectsFromQuery
                .stream()
                .collect(Collectors.toMap(
                    TestObject::getId,
                    testObject -> testObject
                ));

            for (String idOfObjectCreated : idToTestObjectsCreated.keySet()) {
                assertThat(idToTestObjectsFromQuery.containsKey(idOfObjectCreated)).isTrue();
                validateTestObjectEquality(idToTestObjectsFromQuery.get(idOfObjectCreated), idToTestObjectsCreated.get(idOfObjectCreated));
            }
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document query should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void crossPartitionedQueryFollowingCreates_queryFromFirstPreferredRegionCreatesInFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());
        Map<String, TestObject> idToTestObjectsCreated = new HashMap<>();

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithMultiplePartitions = getSharedMultiPartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated1 = TestObject.create();
            idToTestObjectsCreated.put(testObjectToBeCreated1.getId(), testObjectToBeCreated1);

            containerWithMultiplePartitions.createItem(testObjectToBeCreated1).block();

            TestObject testObjectToBeCreated2 = TestObject.create();
            idToTestObjectsCreated.put(testObjectToBeCreated2.getId(), testObjectToBeCreated2);

            containerWithMultiplePartitions.createItem(testObjectToBeCreated2).block();

            SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c");

            List<TestObject> testObjectsFromQuery = containerWithMultiplePartitions
                .queryItems(querySpec, TestObject.class)
                .collectList()
                .block();

            assertThat(testObjectsFromQuery).isNotNull();

            Map<String, TestObject> idToTestObjectsFromQuery = testObjectsFromQuery
                .stream()
                .collect(Collectors.toMap(
                    TestObject::getId,
                    testObject -> testObject
                ));

            for (String idOfObjectCreated : idToTestObjectsCreated.keySet()) {
                assertThat(idToTestObjectsFromQuery.containsKey(idOfObjectCreated)).isTrue();
                validateTestObjectEquality(idToTestObjectsFromQuery.get(idOfObjectCreated), idToTestObjectsCreated.get(idOfObjectCreated));
            }
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document query should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void deleteYourLatestUpsert_deleteAndUpsertInFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            CosmosItemResponse<Object> deleteOperationResponse = containerWithSinglePartition.deleteItem(id, new PartitionKey(pk)).block();

            assertThat(deleteOperationResponse).isNotNull();
            assertThat(deleteOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NO_CONTENT);
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document delete should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void deleteYourLatestUpsert_deleteInSecondPreferredRegionAndUpsertInFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.writeRegionMap.keySet());

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            CosmosItemResponse<Object> deleteOperationResponse = containerWithSinglePartition.deleteItem(id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(preferredRegions.get(0)))).block();

            assertThat(deleteOperationResponse).isNotNull();
            assertThat(deleteOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NO_CONTENT);
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document delete should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void replaceYourLatestUpsert_replaceAndUpsertInFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.readRegionMap.keySet());

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            TestObject testObjectReplacement = testObjectToBeCreated;
            testObjectReplacement.setStringProp(UUID.randomUUID().toString());

            CosmosItemResponse<TestObject> replaceOperationResponse = containerWithSinglePartition.replaceItem(testObjectReplacement, id, new PartitionKey(pk)).block();

            assertThat(replaceOperationResponse).isNotNull();
            assertThat(replaceOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document replace operation should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void replaceYourLatestUpsert_replaceInSecondPreferredRegionAndUpsertInFirstPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.writeRegionMap.keySet());

        Thread.sleep(10_000);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            containerWithSinglePartition.createItem(testObjectToBeCreated).block();

            TestObject testObjectReplacement = testObjectToBeCreated;
            testObjectReplacement.setStringProp(UUID.randomUUID().toString());

            CosmosItemResponse<TestObject> replaceOperationResponse = containerWithSinglePartition.replaceItem(testObjectReplacement, id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(preferredRegions.get(0)))).block();

            assertThat(replaceOperationResponse).isNotNull();
            assertThat(replaceOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Document replace operation should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void readYourCreateGuaranteeWithinBatchOperation() {

        List<String> preferredRegions = new ArrayList<>(writeRegionMap.keySet());

        String documentId = UUID.randomUUID().toString();
        PartitionKey partitionKey = new PartitionKey(documentId);

        TestObject testObjectToBeCreated = TestObject.create(documentId);

        CosmosBatch batch = CosmosBatch.createCosmosBatch(partitionKey);
        batch.createItemOperation(testObjectToBeCreated);
        batch.readItemOperation(testObjectToBeCreated.getId());

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {

            CosmosBatchResponse batchResponse = containerWithSinglePartition.executeCosmosBatch(batch).block();

            List<CosmosBatchOperationResult> batchResponseResults = batchResponse.getResults();

            assertThat(batchResponse.getResults()).isNotNull();
            assertThat(batchResponse.getResults().size()).isEqualTo(2);
            assertThat(batchResponseResults.get(0).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
            assertThat(batchResponseResults.get(1).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Batch operation should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void readYourCreate_readBatchInFirstPreferredRegion_createBatchInSecondPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(writeRegionMap.keySet());
        assertThat(preferredRegions.size()).isGreaterThan(1);

        String documentId = UUID.randomUUID().toString();
        PartitionKey partitionKey = new PartitionKey(documentId);

        TestObject testObjectToBeCreated = TestObject.create(documentId);

        CosmosBatch batchForCreate = CosmosBatch.createCosmosBatch(partitionKey);
        batchForCreate.createItemOperation(testObjectToBeCreated);

        CosmosBatch batchForRead = CosmosBatch.createCosmosBatch(partitionKey);
        batchForRead.readItemOperation(testObjectToBeCreated.getId());

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        Thread.sleep(10_000);

        try {

            CosmosBatchResponse batchCreateResponse = containerWithSinglePartition
                .executeCosmosBatch(
                    batchForCreate,
                    new CosmosBatchRequestOptions()
                        .setExcludedRegions(ImmutableList.of(preferredRegions.get(0)))
                ).block();

            assertThat(batchCreateResponse).isNotNull();
            assertThat(batchCreateResponse.getResults()).isNotNull();

            List<CosmosBatchOperationResult> batchCreateResponseResults = batchCreateResponse.getResults();

            assertThat(batchCreateResponseResults.size()).isEqualTo(1);
            assertThat(batchCreateResponseResults.get(0).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);

            CosmosBatchResponse batchReadResponse = containerWithSinglePartition.executeCosmosBatch(batchForRead).block();

            assertThat(batchReadResponse).isNotNull();
            assertThat(batchReadResponse.getResults()).isNotNull();

            List<CosmosBatchOperationResult> batchReadResponseResults = batchReadResponse.getResults();

            assertThat(batchReadResponseResults.size()).isEqualTo(1);
            assertThat(batchReadResponseResults.get(0).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Batch operation should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void bulkReadYourBulkCreate() {
        List<String> preferredRegions = new ArrayList<>(writeRegionMap.keySet());

        int createOperationCount = 10;
        List<String> idsToCreate = new ArrayList<>();

        Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, createOperationCount).map(i -> {

            String documentId = UUID.randomUUID().toString();
            TestItem testItem = new TestItem(documentId, documentId, documentId);

            idsToCreate.add(documentId);

            return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));
        });

        Flux<CosmosItemOperation> readOperationsFlux = Flux.range(0, createOperationCount).map(i -> {
            String alreadyCreatedId = idsToCreate.get(i);
            return CosmosBulkOperations.getReadItemOperation(alreadyCreatedId, new PartitionKey(alreadyCreatedId));
        });

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        try {

            List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = containerWithSinglePartition.executeBulkOperations(createOperationsFlux).collectList().block();

            assertThat(bulkCreateResponses).isNotNull();
            assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

            bulkCreateResponses.forEach(bulkCreateResponse -> {
                assertThat(bulkCreateResponse.getResponse()).isNotNull();
                assertThat(bulkCreateResponse.getResponse().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
            });

            List<CosmosBulkOperationResponse<Object>> bulkReadResponses = containerWithSinglePartition.executeBulkOperations(readOperationsFlux).collectList().block();

            assertThat(bulkReadResponses).isNotNull();
            assertThat(bulkReadResponses.size()).isEqualTo(createOperationCount);

            bulkReadResponses.forEach(bulkReadResponse -> {
                assertThat(bulkReadResponse.getResponse()).isNotNull();
                assertThat(bulkReadResponse.getResponse().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            });
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Bulk operation should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void bulkReadFromFirstPreferredRegionYourBulkCreateInSecondPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(writeRegionMap.keySet());
        assertThat(preferredRegions.size()).isGreaterThan(1);

        int createOperationCount = 10;
        List<String> idsToCreate = new ArrayList<>();

        Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, createOperationCount).map(i -> {

            String documentId = UUID.randomUUID().toString();
            TestItem testItem = new TestItem(documentId, documentId, documentId);

            idsToCreate.add(documentId);

            return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));
        });

        Flux<CosmosItemOperation> readOperationsFlux = Flux.range(0, createOperationCount).map(i -> {
            String alreadyCreatedId = idsToCreate.get(i);
            return CosmosBulkOperations.getReadItemOperation(alreadyCreatedId, new PartitionKey(alreadyCreatedId));
        });

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer containerWithSinglePartition = getSharedSinglePartitionCosmosContainer(client);

        Thread.sleep(10_000);

        try {

            List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = containerWithSinglePartition
                .executeBulkOperations(
                    createOperationsFlux,
                    new CosmosBulkExecutionOptions()
                        .setExcludedRegions(ImmutableList.of(preferredRegions.get(0))))
                .collectList().block();

            assertThat(bulkCreateResponses).isNotNull();
            assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

            bulkCreateResponses.forEach(bulkCreateResponse -> {
                assertThat(bulkCreateResponse.getResponse()).isNotNull();
                assertThat(bulkCreateResponse.getResponse().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
            });

            List<CosmosBulkOperationResponse<Object>> bulkReadResponses = containerWithSinglePartition.executeBulkOperations(readOperationsFlux).collectList().block();

            assertThat(bulkReadResponses).isNotNull();
            assertThat(bulkReadResponses.size()).isEqualTo(createOperationCount);

            bulkReadResponses.forEach(bulkReadResponse -> {
                assertThat(bulkReadResponse.getResponse()).isNotNull();
                assertThat(bulkReadResponse.getResponse().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            });
        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail("Bulk operation should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void changeFeed_fromBeginning_forFullRange_withSessionGuarantee() {

        List<String> preferredRegions = new ArrayList<>(this.writeRegionMap.keySet());

        int createOperationCount = 10;
        Set<String> idsAddedByBulkCreate = new HashSet<>();

        Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, createOperationCount).map(i -> {
            String documentId = UUID.randomUUID().toString();
            TestItem testItem = new TestItem(documentId, documentId, documentId);

            idsAddedByBulkCreate.add(documentId);
            return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));
        });

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer container = getSharedSinglePartitionCosmosContainer(client);

        try {

            List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = container.executeBulkOperations(createOperationsFlux).collectList().block();

            assertThat(bulkCreateResponses).isNotNull();
            assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

            CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forFullRange());

            Iterator<FeedResponse<JsonNode>> responseIterator = container
                .queryChangeFeed(changeFeedRequestOptions, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            List<JsonNode> results = new ArrayList<>();
            int resultSizeCounter = 0;

            while (responseIterator.hasNext()) {
                FeedResponse<JsonNode> response = responseIterator.next();

                assertThat(response).isNotNull();
                assertThat(response.getResults()).isNotNull();

                resultSizeCounter += response.getResults().size();
                results.addAll(response.getResults());

                changeFeedRequestOptions = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(response.getContinuationToken());

                if (resultSizeCounter >= idsAddedByBulkCreate.size()) {
                    break;
                }
            }

            assertThat(results.size() >= idsAddedByBulkCreate.size()).isTrue();

            Set<String> idsReceivedFromChangeFeedRequest = new HashSet<>();

            results.forEach(instanceReceivedFromChangeFeedRequest ->
                idsReceivedFromChangeFeedRequest.add(instanceReceivedFromChangeFeedRequest.get("id").asText()));

            idsAddedByBulkCreate.forEach(idAddedByBulkCreate ->
                assertThat(idsReceivedFromChangeFeedRequest.contains(idAddedByBulkCreate)).isTrue());

        } catch (Exception ex) {
            logger.error("Exception occurred...", ex);
            fail("Change feed operation should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, timeOut = TIMEOUT)
    public void changeFeed_fromBeginning_fromFirstPreferredRegion_forFullRange_withCreatesOnSecondPreferredRegion_withSessionGuarantee() {

        List<String> preferredRegions = new ArrayList<>(this.writeRegionMap.keySet());
        assertThat(preferredRegions.size()).isGreaterThan(1);

        int createOperationCount = 10;
        Set<String> idsAddedByCreates = new HashSet<>();

        Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, createOperationCount).map(i -> {
            String documentId = UUID.randomUUID().toString();
            TestItem testItem = new TestItem(documentId, documentId, documentId);

            idsAddedByCreates.add(documentId);
            return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));
        });

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncContainer container = getSharedSinglePartitionCosmosContainer(client);

        try {

            List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = container
                .executeBulkOperations(
                    createOperationsFlux,
                    new CosmosBulkExecutionOptions().setExcludedRegions(ImmutableList.of(preferredRegions.get(0))))
                .collectList()
                .block();

            assertThat(bulkCreateResponses).isNotNull();
            assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

            CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forFullRange());

            Iterator<FeedResponse<JsonNode>> responseIterator = container
                .queryChangeFeed(changeFeedRequestOptions, JsonNode.class)
                .byPage()
                .toIterable()
                .iterator();

            List<JsonNode> results = new ArrayList<>();

            while (responseIterator.hasNext()) {
                FeedResponse<JsonNode> response = responseIterator.next();

                assertThat(response).isNotNull();
                assertThat(response.getResults()).isNotNull();

                results.addAll(response.getResults());

                changeFeedRequestOptions = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(response.getContinuationToken());

                if (results.size() >= idsAddedByCreates.size()) {
                    break;
                }
            }

            assertThat(results.size() >= idsAddedByCreates.size()).isTrue();

            Set<String> idsReceivedFromChangeFeedRequest = new HashSet<>();

            results.forEach(instanceReceivedFromChangeFeedRequest ->
                idsReceivedFromChangeFeedRequest.add(instanceReceivedFromChangeFeedRequest.get("id").asText()));

            idsAddedByCreates.forEach(idAddedByBulkCreate ->
                assertThat(idsReceivedFromChangeFeedRequest.contains(idAddedByBulkCreate)).isTrue());

        } catch (Exception ex) {
            logger.error("Exception occurred...", ex);
            fail("Change feed operation should have succeeded...");
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = 3 * TIMEOUT)
    public void readManyWithSolelyPointReads() throws InterruptedException {

        List<String> preferredRegions = new ArrayList<>(this.writeRegionMap.keySet());

        int createOperationCount = 100;
        int readManyContainerProvisionedThroughput = 50_000;

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncDatabase sharedDatabase = getSharedCosmosDatabase(client);

        String readManyContainerId = UUID.randomUUID() + "-read-many";

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(readManyContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(readManyContainerProvisionedThroughput);

        sharedDatabase.createContainer(containerProperties, throughputProperties).block();
        CosmosAsyncContainer readManyContainer = sharedDatabase.getContainer(readManyContainerId);

        Thread.sleep(60_000);

        try {
            for (int i = 0; i < createOperationCount; i++) {

                TestObject testObject = TestObject.create();
                String documentId = testObject.getId();

                readManyContainer.createItem(testObject, new PartitionKey(documentId), new CosmosItemRequestOptions()).block();
            }

            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec();
            sqlQuerySpec.setQueryText("SELECT * FROM c OFFSET 0 LIMIT 1");

            List<FeedRange> feedRanges = readManyContainer.getFeedRanges().block();

            Set<String> idsToUseWithReadMany = new HashSet<>();

            assertThat(feedRanges).isNotNull();
            int feedRangesCount = feedRanges.size();

            feedRanges.forEach(feedRange -> {
                Iterator<FeedResponse<TestObject>> responseIterator = readManyContainer
                    .queryItems(sqlQuerySpec, new CosmosQueryRequestOptions().setFeedRange(feedRange), TestObject.class)
                    .byPage()
                    .toIterable()
                    .iterator();

                while (responseIterator.hasNext()) {
                    FeedResponse<TestObject> response = responseIterator.next();

                    assertThat(response).isNotNull();
                    assertThat(response.getResults()).isNotNull();

                    List<TestObject> results = response.getResults();

                    assertThat(results.size()).isEqualTo(1);

                    idsToUseWithReadMany.add(results.get(0).getId());
                }
            });

            if (idsToUseWithReadMany.size() == feedRangesCount) {

                List<CosmosItemIdentity> cosmosItemIdentities = idsToUseWithReadMany
                    .stream()
                    .map(id -> new CosmosItemIdentity(new PartitionKey(id), id))
                    .collect(Collectors.toList());

                FeedResponse<InternalObjectNode> readManyResult = readManyContainer
                    .readMany(cosmosItemIdentities, InternalObjectNode.class).block();

                assertThat(readManyResult).isNotNull();
                assertThat(readManyResult.getResults()).isNotNull();
                assertThat(readManyResult.getResults().size()).isEqualTo(feedRangesCount);
            } else {
                fail("Not all physical partitions have data!");
            }

        } catch (Exception ex) {
            logger.error("Exception occurred...", ex);
            fail("readMany operation should have succeeded...");
        } finally {
            safeDeleteCollection(readManyContainer);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = 3 * TIMEOUT)
    public void readManyWithSolelyQueries() throws InterruptedException {

        List<String> preferredRegions = new ArrayList<>(this.writeRegionMap.keySet());

        int createOperationCount = 100;
        int readManyContainerProvisionedThroughput = 50_000;

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncDatabase sharedDatabase = getSharedCosmosDatabase(client);

        String readManyContainerId = UUID.randomUUID() + "-read-many";

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(readManyContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(readManyContainerProvisionedThroughput);

        sharedDatabase.createContainer(containerProperties, throughputProperties).block();
        CosmosAsyncContainer readManyContainer = sharedDatabase.getContainer(readManyContainerId);

        Thread.sleep(60_000);

        Set<String> idsToUseWithReadMany = new HashSet<>();

        try {
            for (int i = 0; i < createOperationCount; i++) {

                TestObject testObject = TestObject.create();
                String documentId = testObject.getId();

                readManyContainer.createItem(testObject, new PartitionKey(documentId), new CosmosItemRequestOptions()).block();

                idsToUseWithReadMany.add(documentId);
            }

            List<CosmosItemIdentity> cosmosItemIdentities = idsToUseWithReadMany
                .stream()
                .map(id -> new CosmosItemIdentity(new PartitionKey(id), id))
                .collect(Collectors.toList());

            FeedResponse<InternalObjectNode> readManyResult = readManyContainer
                .readMany(cosmosItemIdentities, InternalObjectNode.class).block();

            assertThat(readManyResult).isNotNull();
            assertThat(readManyResult.getResults()).isNotNull();
            assertThat(readManyResult.getResults().size()).isEqualTo(idsToUseWithReadMany.size());
        } catch (Exception ex) {
            logger.error("Exception occurred...", ex);
            fail("readMany operation should have succeeded...");
        } finally {
            safeDeleteCollection(readManyContainer);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, timeOut = 3 * TIMEOUT)
    public void readManyWithSolelyPointReadFollowingCreatesOnSecondPreferredRegion() throws InterruptedException {
        List<String> preferredRegions = new ArrayList<>(this.writeRegionMap.keySet());

        assertThat(preferredRegions.size()).isGreaterThan(1);

        int createOperationCount = 100;
        int readManyContainerProvisionedThroughput = 50_000;

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncDatabase sharedDatabase = getSharedCosmosDatabase(client);

        String readManyContainerId = UUID.randomUUID() + "-read-many";

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(readManyContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(readManyContainerProvisionedThroughput);

        sharedDatabase.createContainer(containerProperties, throughputProperties).block();
        CosmosAsyncContainer readManyContainer = sharedDatabase.getContainer(readManyContainerId);

        Thread.sleep(60_000);

        try {
            for (int i = 0; i < createOperationCount; i++) {

                TestObject testObject = TestObject.create();
                String documentId = testObject.getId();

                readManyContainer.createItem(
                    testObject,
                    new PartitionKey(documentId),
                    new CosmosItemRequestOptions()
                        .setExcludedRegions(ImmutableList.of(preferredRegions.get(0)))
                ).block();
            }

            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec();
            sqlQuerySpec.setQueryText("SELECT * FROM c OFFSET 0 LIMIT 1");

            List<FeedRange> feedRanges = readManyContainer.getFeedRanges().block();

            Set<String> idsToUseWithReadMany = new HashSet<>();

            assertThat(feedRanges).isNotNull();
            int feedRangesCount = feedRanges.size();

            feedRanges.forEach(feedRange -> {
                Iterator<FeedResponse<TestObject>> responseIterator = readManyContainer
                    .queryItems(sqlQuerySpec, new CosmosQueryRequestOptions().setFeedRange(feedRange), TestObject.class)
                    .byPage()
                    .toIterable()
                    .iterator();

                while (responseIterator.hasNext()) {
                    FeedResponse<TestObject> response = responseIterator.next();

                    assertThat(response).isNotNull();
                    assertThat(response.getResults()).isNotNull();

                    List<TestObject> results = response.getResults();

                    assertThat(results.size()).isEqualTo(1);

                    idsToUseWithReadMany.add(results.get(0).getId());
                }
            });

            if (idsToUseWithReadMany.size() == feedRangesCount) {

                List<CosmosItemIdentity> cosmosItemIdentities = idsToUseWithReadMany
                    .stream()
                    .map(id -> new CosmosItemIdentity(new PartitionKey(id), id))
                    .collect(Collectors.toList());

                FeedResponse<InternalObjectNode> readManyResult = readManyContainer
                    .readMany(cosmosItemIdentities, InternalObjectNode.class).block();

                assertThat(readManyResult).isNotNull();
                assertThat(readManyResult.getResults()).isNotNull();
                assertThat(readManyResult.getResults().size()).isEqualTo(feedRangesCount);
            } else {
                fail("Not all physical partitions have data!");
            }

        } catch (Exception ex) {
            logger.error("Exception occurred...", ex);
            fail("readMany operation should have succeeded...");
        } finally {
            safeDeleteCollection(readManyContainer);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = 3 * TIMEOUT)
    public void readManyWithSolelyQueriesFollowingCreatesOnSecondPreferredRegion() throws InterruptedException {

        List<String> preferredRegions = new ArrayList<>(this.writeRegionMap.keySet());

        assertThat(preferredRegions.size()).isGreaterThan(1);

        int createOperationCount = 100;
        int readManyContainerProvisionedThroughput = 50_000;

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), preferredRegions, true);
        CosmosAsyncDatabase sharedDatabase = getSharedCosmosDatabase(client);

        String readManyContainerId = UUID.randomUUID() + "-read-many";

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(readManyContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(readManyContainerProvisionedThroughput);

        sharedDatabase.createContainer(containerProperties, throughputProperties).block();
        CosmosAsyncContainer readManyContainer = sharedDatabase.getContainer(readManyContainerId);

        Thread.sleep(60_000);

        Set<String> idsToUseWithReadMany = new HashSet<>();

        try {
            for (int i = 0; i < createOperationCount; i++) {

                TestObject testObject = TestObject.create();
                String documentId = testObject.getId();

                readManyContainer.createItem(
                    testObject,
                    new PartitionKey(documentId),
                    new CosmosItemRequestOptions()
                        .setExcludedRegions(ImmutableList.of(preferredRegions.get(0))
                        )
                ).block();

                idsToUseWithReadMany.add(documentId);
            }

            List<CosmosItemIdentity> cosmosItemIdentities = idsToUseWithReadMany
                .stream()
                .map(id -> new CosmosItemIdentity(new PartitionKey(id), id))
                .collect(Collectors.toList());

            FeedResponse<InternalObjectNode> readManyResult = readManyContainer
                .readMany(cosmosItemIdentities, InternalObjectNode.class).block();

            assertThat(readManyResult).isNotNull();
            assertThat(readManyResult.getResults()).isNotNull();
            assertThat(readManyResult.getResults().size()).isEqualTo(idsToUseWithReadMany.size());
        } catch (Exception ex) {
            logger.error("Exception occurred...", ex);
            fail("readMany operation should have succeeded...");
        } finally {
            safeDeleteCollection(readManyContainer);
            safeClose(client);
        }
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void testBloomFilterSetup() {

        Funnel<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType> pkBasedTypeFunnel = new Funnel<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType>() {
            @Override
            public void funnel(PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType from, PrimitiveSink into) {
                into
                    .putLong(from.getCollectionRid())
                    .putString(from.getEffectivePartitionKeyString(), Charsets.UTF_8)
                    .putString(from.getRegion(), Charsets.UTF_8);
            }
        };

        BloomFilter<PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType> partitionKeyBasedBloomFilter = BloomFilter.create(pkBasedTypeFunnel, 10_000, 0.001);

        partitionKeyBasedBloomFilter.put(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk1", "eastus", 1L));

        assertThat(partitionKeyBasedBloomFilter.mightContain(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk1", "eastus", 1L))).isTrue();
        assertThat(partitionKeyBasedBloomFilter.mightContain(new PartitionKeyBasedBloomFilter.PartitionKeyBasedBloomFilterType("pk2", "eastus", 1L))).isFalse();
    }

    @Test(groups = {"multi-region", "multi-master"})
    @AfterClass
    public void afterClass() {}

    private static CosmosAsyncClient buildAsyncClient(CosmosClientBuilder clientBuilder, List<String> preferredRegions, boolean isRegionScopedSessionCapturingEnabled) {
        clientBuilder = clientBuilder.preferredRegions(preferredRegions);
        cosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled(clientBuilder, isRegionScopedSessionCapturingEnabled);
        return clientBuilder.buildAsyncClient();
    }

    private static Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }

    private static void validateTestObjectEquality(TestObject testObject1, TestObject testObject2) {
        assertThat(testObject1.getId()).isEqualTo(testObject2.getId());
        assertThat(testObject1.getMypk()).isEqualTo(testObject2.getMypk());
        assertThat(testObject1.getStringProp()).isEqualTo(testObject2.getStringProp());
    }
}
