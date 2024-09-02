// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.PartitionKeyBasedBloomFilter;
import com.azure.cosmos.implementation.RegionScopedSessionContainer;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.guava25.base.Charsets;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.ImmutableSet;
import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnel;
import com.azure.cosmos.implementation.guava25.hash.Funnels;
import com.azure.cosmos.implementation.guava25.hash.PrimitiveSink;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class SessionConsistencyWithRegionScopingTests extends TestSuiteBase {

    private static final Logger logger = LoggerFactory.getLogger(SessionConsistencyWithRegionScopingTests.class);
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor cosmosClientBuilderAccessor
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private static final ImplementationBridgeHelpers.PartitionKeyHelper.PartitionKeyAccessor partitionKeyAccessor
        = ImplementationBridgeHelpers.PartitionKeyHelper.getPartitionKeyAccessor();

    private static final boolean BLOOM_FILTER_FORCED_ACCESSED_FLAG = true;
    private static final boolean SPLIT_REQUESTED_FLAG = true;
    private static final boolean MULTI_PARTITION_CONTAINER_REQUESTED_FLAG = true;

    private List<String> writeRegions;
    private List<String> readRegions;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public SessionConsistencyWithRegionScopingTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "readYouWriteWithNoExplicitRegionSwitchingTestContext")
    public Object[][] readYouWriteWithNoExplicitRegionSwitchingTestContext() {

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> pointReadYourPointCreate_BothFromFirstPreferredRegionFunc = (container, shouldInjectPreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();
            CosmosItemResponse<TestObject> testObjectFromRead = container.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> pointReadAfterPartitionSplitAndPointCreate_BothFromFirstPreferredRegionFunc = (container, shouldInjectPreferredRegions) -> {

            TestObject testObjectToBeCreated = TestObject.create();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            ThroughputResponse throughputResponse = container.replaceThroughput(ThroughputProperties.createManualThroughput(10_100)).block();

            while (true) {
                assert throughputResponse != null;
                boolean isReplacePending = throughputResponse.isReplacePending();

                if (!isReplacePending) {
                    break;
                }
                throughputResponse = container.readThroughput().block();
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("Waiting for split to complete...");
            }

            logger.info("Split complete!");

            String id = testObjectToBeCreated.getId();
            CosmosItemResponse<TestObject> testObjectFromRead = container.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> pointReadYourLatestUpsert_UpsertsFromPreferredRegionReadFromPreferredRegionFunc = (container, shouldInjectPreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            testObjectToBeCreated.setStringProp(UUID.randomUUID().toString());

            TestObject testObjectModified = testObjectToBeCreated;

            container.upsertItem(testObjectModified, new PartitionKey(pk), new CosmosItemRequestOptions()).block();

            CosmosItemResponse<TestObject> testObjectFromRead = container.readItem(id, new PartitionKey(pk), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectModified, testObjectFromRead.getItem());

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> queryTargetedToLogicalPartitionFollowingCreates_queryFromFirstPreferredRegionCreateInFirstPreferredRegionFunc = (container, shouldInjectPreferredRegions) -> {
            Map<String, TestObject> idToTestObjectsCreated = new HashMap<>();

            TestObject testObjectToBeCreated = TestObject.create();
            idToTestObjectsCreated.put(testObjectToBeCreated.getId(), testObjectToBeCreated);

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c");

            List<TestObject> testObjectsFromQuery = container
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

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> crossPartitionedQueryFollowingCreates_queryFromFirstPreferredRegionCreatesInFirstPreferredRegionFunc = (container, shouldInjectPreferredRegions) -> {

            Map<String, TestObject> idToTestObjectsCreated = new HashMap<>();

            TestObject testObjectToBeCreated1 = TestObject.create();
            idToTestObjectsCreated.put(testObjectToBeCreated1.getId(), testObjectToBeCreated1);

            container.createItem(testObjectToBeCreated1).block();

            TestObject testObjectToBeCreated2 = TestObject.create();
            idToTestObjectsCreated.put(testObjectToBeCreated2.getId(), testObjectToBeCreated2);

            container.createItem(testObjectToBeCreated2).block();

            SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c");

            List<TestObject> testObjectsFromQuery = container
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

            return ImmutableSet.of(testObjectToBeCreated1.getMypk(), testObjectToBeCreated2.getMypk());
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> deleteYourLatestUpsert_deleteAndUpsertInFirstPreferredRegionFunc = (container, shouldInjectPreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            CosmosItemResponse<Object> deleteOperationResponse = container.deleteItem(id, new PartitionKey(pk)).block();

            assertThat(deleteOperationResponse).isNotNull();
            assertThat(deleteOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NO_CONTENT);

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> replaceYourLatestUpsert_replaceAndUpsertInFirstPreferredRegionFunc = (container, shouldInjectPreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            TestObject testObjectReplacement = testObjectToBeCreated;
            testObjectReplacement.setStringProp(UUID.randomUUID().toString());

            CosmosItemResponse<TestObject> replaceOperationResponse = container.replaceItem(testObjectReplacement, id, new PartitionKey(pk)).block();

            assertThat(replaceOperationResponse).isNotNull();
            assertThat(replaceOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> readYourPatchYourCreateFunc = (container, shouldInjectPreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            CosmosItemResponse<TestObject> itemResponse = container.createItem(testObjectToBeCreated).block();

            assertThat(itemResponse).isNotNull();
            assertThat(itemResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);

            CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/" + "newProperty", "newVal");
            CosmosItemResponse<TestObject> patchOperationResponse = container.patchItem(id, new PartitionKey(pk), patchOperations, TestObject.class).block();

            assertThat(patchOperationResponse).isNotNull();
            assertThat(patchOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            CosmosItemResponse<JsonNode> readOperationResponse = container.readItem(id, new PartitionKey(pk), JsonNode.class).block();

            assertThat(readOperationResponse).isNotNull();
            assertThat(readOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            JsonNode itemReadAsJsonNode = readOperationResponse.getItem();

            assertThat(itemReadAsJsonNode).isNotNull();
            assertThat(itemReadAsJsonNode.get("newProperty")).isNotNull();
            assertThat(itemReadAsJsonNode.get("newProperty").asText()).isNotNull();
            assertThat(itemReadAsJsonNode.get("newProperty").asText()).isEqualTo("newVal");

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> bulkReadYourBulkCreateFunc = (container, shouldInjectPreferredRegions) -> {

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

            List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = container.executeBulkOperations(createOperationsFlux).collectList().block();

            assertThat(bulkCreateResponses).isNotNull();
            assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

            bulkCreateResponses.forEach(bulkCreateResponse -> {
                assertThat(bulkCreateResponse.getResponse()).isNotNull();
                assertThat(bulkCreateResponse.getResponse().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
            });

            List<CosmosBulkOperationResponse<Object>> bulkReadResponses = container.executeBulkOperations(readOperationsFlux).collectList().block();
            Set<String> pksRead = new HashSet<>();

            assertThat(bulkReadResponses).isNotNull();
            assertThat(bulkReadResponses.size()).isEqualTo(createOperationCount);

            bulkReadResponses.forEach(bulkReadResponse -> {
                assertThat(bulkReadResponse.getResponse()).isNotNull();
                assertThat(bulkReadResponse.getResponse().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
                pksRead.add(bulkReadResponse.getResponse().getItem(TestItem.class).getMypk());
            });

            return pksRead;
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> changeFeed_fromBeginning_forFullRange_withSessionGuaranteeFunc = (container, shouldInjectPreferredRegions) -> {

            int createOperationCount = 10;
            Set<String> idsAddedByBulkCreate = new HashSet<>();

            Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, createOperationCount).map(i -> {
                String documentId = UUID.randomUUID().toString();
                TestItem testItem = new TestItem(documentId, documentId, documentId);

                idsAddedByBulkCreate.add(documentId);
                return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));
            });

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

            while (responseIterator.hasNext()) {
                FeedResponse<JsonNode> response = responseIterator.next();

                assertThat(response).isNotNull();
                assertThat(response.getResults()).isNotNull();

                results.addAll(response.getResults());

                changeFeedRequestOptions = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(response.getContinuationToken());

                if (results.size() >= idsAddedByBulkCreate.size()) {
                    break;
                }
            }

            assertThat(results.size() >= idsAddedByBulkCreate.size()).isTrue();

            Set<String> idsReceivedFromChangeFeedRequest = new HashSet<>();

            results.forEach(instanceReceivedFromChangeFeedRequest ->
                idsReceivedFromChangeFeedRequest.add(instanceReceivedFromChangeFeedRequest.get("id").asText()));

            idsAddedByBulkCreate.forEach(idAddedByBulkCreate ->
                assertThat(idsReceivedFromChangeFeedRequest.contains(idAddedByBulkCreate)).isTrue());

            return idsReceivedFromChangeFeedRequest;
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> readYourCreateGuaranteeWithinBatchFunc = (container, shouldInjectPreferredRegions) -> {
            String documentId = UUID.randomUUID().toString();
            PartitionKey partitionKey = new PartitionKey(documentId);

            TestObject testObjectToBeCreated = TestObject.create(documentId);

            CosmosBatch batch = CosmosBatch.createCosmosBatch(partitionKey);
            batch.createItemOperation(testObjectToBeCreated);
            batch.readItemOperation(testObjectToBeCreated.getId());

            CosmosBatchResponse batchResponse = container.executeCosmosBatch(batch).block();

            List<CosmosBatchOperationResult> batchResponseResults = batchResponse.getResults();

            assertThat(batchResponse.getResults()).isNotNull();
            assertThat(batchResponse.getResults().size()).isEqualTo(2);
            assertThat(batchResponseResults.get(0).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
            assertThat(batchResponseResults.get(1).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            return ImmutableSet.of(documentId);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> changeFeed_fromBeginning_forLogicalPartition_withSessionGuaranteeFunc = (container, shouldInjectPreferredRegions) -> {
            int createOperationCount = 10;
            Set<String> idsAddedByBulkCreate = new HashSet<>();

            Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, createOperationCount).map(i -> {
                String documentId = UUID.randomUUID().toString();
                TestItem testItem = new TestItem(documentId, documentId, documentId);

                idsAddedByBulkCreate.add(documentId);
                return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));
            });

            List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = container.executeBulkOperations(createOperationsFlux).collectList().block();

            assertThat(bulkCreateResponses).isNotNull();
            assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

            String idToObserveUsingChangeFeed = idsAddedByBulkCreate.stream().collect(Collectors.toList()).get(0);

            CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(idToObserveUsingChangeFeed)));

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

                if (results.size() >= 1) {
                    break;
                }
            }

            assertThat(results.size()).isGreaterThanOrEqualTo(1);

            Set<String> idsReceivedFromChangeFeedRequest = new HashSet<>();

            results.forEach(instanceReceivedFromChangeFeedRequest ->
                idsReceivedFromChangeFeedRequest.add(instanceReceivedFromChangeFeedRequest.get("id").asText()));

            assertThat(idsReceivedFromChangeFeedRequest.size()).isEqualTo(1);

            String idReceivedFromChangeFeedRequest = idsReceivedFromChangeFeedRequest.stream().collect(Collectors.toList()).get(0);
            assertThat(idReceivedFromChangeFeedRequest).isEqualTo(idToObserveUsingChangeFeed);

            return idsReceivedFromChangeFeedRequest;
        };

        Object[][] readYouWriteWithNoExplicitRegionSwitching_testConfigs = new Object[][] {
            {
                pointReadYourPointCreate_BothFromFirstPreferredRegionFunc,
                "pointReadYourPointCreate_BothFromFirstPreferredRegion",
                "Document read should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                pointReadAfterPartitionSplitAndPointCreate_BothFromFirstPreferredRegionFunc,
                "pointReadAfterPartitionSplitAndPointCreate_BothFromFirstPreferredRegion",
                "Document read should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                pointReadYourLatestUpsert_UpsertsFromPreferredRegionReadFromPreferredRegionFunc,
                "pointReadAfterPartitionSplitAndPointCreate_BothFromFirstPreferredRegion",
                "Document read should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                queryTargetedToLogicalPartitionFollowingCreates_queryFromFirstPreferredRegionCreateInFirstPreferredRegionFunc,
                "queryTargetedToLogicalPartitionFollowingCreates_queryFromFirstPreferredRegionCreateInFirstPreferredRegion",
                "Document query should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                crossPartitionedQueryFollowingCreates_queryFromFirstPreferredRegionCreatesInFirstPreferredRegionFunc,
                "queryTargetedToLogicalPartitionFollowingCreates_queryFromFirstPreferredRegionCreateInFirstPreferredRegion",
                "Document query should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                deleteYourLatestUpsert_deleteAndUpsertInFirstPreferredRegionFunc,
                "deleteYourLatestUpsert_deleteAndUpsertInFirstPreferredRegion",
                "Document query should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                replaceYourLatestUpsert_replaceAndUpsertInFirstPreferredRegionFunc,
                "replaceYourLatestUpsert_replaceAndUpsertInFirstPreferredRegion",
                "Document replace operation should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                readYourPatchYourCreateFunc,
                "readYourPatchYourCreate",
                "Document patch operation should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                bulkReadYourBulkCreateFunc,
                "bulkReadYourBulkCreate",
                "Bulk operation should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                changeFeed_fromBeginning_forFullRange_withSessionGuaranteeFunc,
                "changeFeed_fromBeginning_forFullRange_withSessionGuarantee",
                "Change feed operation should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                changeFeed_fromBeginning_forLogicalPartition_withSessionGuaranteeFunc,
                "changeFeed_fromBeginning_forLogicalPartition_withSessionGuarantee",
                "Change feed operation should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                readYourCreateGuaranteeWithinBatchFunc,
                "readYourCreateGuaranteeWithinBatch",
                "Read your create within batch should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            }
        };

        return addBooleanFlagsToAllTestConfigs(readYouWriteWithNoExplicitRegionSwitching_testConfigs);
    }

    @DataProvider(name = "readManyWithNoExplicitRegionSwitchingTestContext")
    public Object[][] readManyWithNoExplicitRegionSwitchingTestContext() {

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> readManyWithSolelyPointReadsFunc = (container, shouldUsePreferredRegions) -> {

            int createOperationCount = 100;

            for (int i = 0; i < createOperationCount; i++) {

                TestObject testObject = TestObject.create();
                String documentId = testObject.getId();

                container.createItem(testObject, new PartitionKey(documentId), new CosmosItemRequestOptions()).block();
            }

            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec();
            sqlQuerySpec.setQueryText("SELECT * FROM c OFFSET 0 LIMIT 1");

            List<FeedRange> feedRanges = container.getFeedRanges().block();

            Set<String> idsToUseWithReadMany = new HashSet<>();

            assertThat(feedRanges).isNotNull();
            int feedRangesCount = feedRanges.size();

            feedRanges.forEach(feedRange -> {
                Iterator<FeedResponse<TestObject>> responseIterator = container
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

                FeedResponse<InternalObjectNode> readManyResult = container
                    .readMany(cosmosItemIdentities, InternalObjectNode.class).block();

                assertThat(readManyResult).isNotNull();
                assertThat(readManyResult.getResults()).isNotNull();
                assertThat(readManyResult.getResults().size()).isEqualTo(feedRangesCount);
            } else {
                fail("Not all physical partitions have data!");
            }

            return idsToUseWithReadMany;
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> readManyWithSolelyQueriesFunc = (container, shouldUsePreferredRegions) -> {

            int createOperationCount = 100;
            Set<String> idsToUseWithReadMany = new HashSet<>();

            for (int i = 0; i < createOperationCount; i++) {

                TestObject testObject = TestObject.create();
                String documentId = testObject.getId();

                container.createItem(testObject, new PartitionKey(documentId), new CosmosItemRequestOptions()).block();

                idsToUseWithReadMany.add(documentId);
            }

            List<CosmosItemIdentity> cosmosItemIdentities = idsToUseWithReadMany
                .stream()
                .map(id -> new CosmosItemIdentity(new PartitionKey(id), id))
                .collect(Collectors.toList());

            FeedResponse<InternalObjectNode> readManyResult = container
                .readMany(cosmosItemIdentities, InternalObjectNode.class).block();

            assertThat(readManyResult).isNotNull();
            assertThat(readManyResult.getResults()).isNotNull();
            assertThat(readManyResult.getResults().size()).isEqualTo(idsToUseWithReadMany.size());

            return idsToUseWithReadMany;
        };

        Object[][] readManyWithNoExplicitRegionSwitching_testConfigs = new Object[][]{
            {
                readManyWithSolelyPointReadsFunc,
                "readManyWithSolelyPointReads",
                "readMany operation with solely point reads should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG
            },
            {
                readManyWithSolelyQueriesFunc,
                "readManyWithSolelyQueries",
                "readMany operation with solely queries should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG
            }
        };

        return addBooleanFlagsToAllTestConfigs(readManyWithNoExplicitRegionSwitching_testConfigs);
    }

    @DataProvider(name = "readManyWithExplicitRegionSwitchingTestContext")
    public Object[][] readManyWithExplicitRegionSwitchingTestContext() {

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> readManyWithSolelyPointReadFollowingCreates_readManyInSecondPreferredRegion_createsInFirstPreferredRegion_supportingQueriesThroughHelperContainer_Func = (container, shouldInjectPreferredRegions) -> {

            try (CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), this.readRegions, false, shouldInjectPreferredRegions)) {

                CosmosAsyncDatabase database = client.getDatabase(container.getDatabase().getId());
                CosmosAsyncContainer helperContainer = database.getContainer(container.getId());

                int createOperationCount = 100;
                Set<String> idsWithCreatesInSecondPreferredRegion = new HashSet<>();

                for (int i = 0; i < createOperationCount; i++) {

                    TestObject testObject = TestObject.create();
                    String documentId = testObject.getId();

                    PartitionKey partitionKey = new PartitionKey(documentId);

                    container.createItem(testObject, partitionKey, new CosmosItemRequestOptions())
                        .doOnSuccess(response -> idsWithCreatesInSecondPreferredRegion.add(response.getItem().getId()))
                        .block();
                }

                SqlQuerySpec sqlQuerySpec = new SqlQuerySpec();
                sqlQuerySpec.setQueryText("SELECT * FROM c OFFSET 0 LIMIT 1");

                List<FeedRange> feedRanges = helperContainer.getFeedRanges().block();

                Set<String> idsToUseWithReadMany = new HashSet<>();

                assertThat(feedRanges).isNotNull();
                int feedRangesCount = feedRanges.size();

                feedRanges.forEach(feedRange -> {
                    Iterator<FeedResponse<TestObject>> responseIterator = helperContainer
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

                    FeedResponse<InternalObjectNode> readManyResult = container
                        .readMany(
                            cosmosItemIdentities,
                            new CosmosReadManyRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))),
                            InternalObjectNode.class)
                        .block();

                    assertThat(readManyResult).isNotNull();
                    assertThat(readManyResult.getResults()).isNotNull();
                    assertThat(readManyResult.getResults().size()).isEqualTo(feedRangesCount);
                } else {
                    fail("Not all physical partitions have data!");
                }

                return idsToUseWithReadMany;
            } catch (Exception ex) {
                logger.error("Exception occurred : ", ex);
                fail("Creates, Queries and readMany operation should have succeeded!");
            }

            return new HashSet<>();
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> readManyWithSolelyQueriesFollowingCreates_readManyInSecondPreferredRegion_createsInFirstPreferredRegion_Func = (container, shouldInjectPreferredRegions) -> {

            Set<String> idsToUseWithReadMany = new HashSet<>();
            int createOperationCount = 100;

            for (int i = 0; i < createOperationCount; i++) {

                TestObject testObject = TestObject.create();
                String documentId = testObject.getId();

                container.createItem(testObject, new PartitionKey(documentId), new CosmosItemRequestOptions())
                    .doOnSuccess(response -> idsToUseWithReadMany.add(response.getItem().getId()))
                    .block();

                idsToUseWithReadMany.add(documentId);
            }

            List<CosmosItemIdentity> cosmosItemIdentities = idsToUseWithReadMany
                .stream()
                .map(id -> new CosmosItemIdentity(new PartitionKey(id), id))
                .collect(Collectors.toList());

            FeedResponse<InternalObjectNode> readManyResult = container
                .readMany(
                    cosmosItemIdentities,
                    new CosmosReadManyRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))),
                    InternalObjectNode.class)
                .block();

            assertThat(readManyResult).isNotNull();
            assertThat(readManyResult.getResults()).isNotNull();
            assertThat(readManyResult.getResults().size()).isEqualTo(idsToUseWithReadMany.size());

            return idsToUseWithReadMany;
        };

        Object[][] readManyWithExplicitRegionSwitching_testConfigs = new Object[][] {
            {
                readManyWithSolelyPointReadFollowingCreates_readManyInSecondPreferredRegion_createsInFirstPreferredRegion_supportingQueriesThroughHelperContainer_Func,
                "readManyWithSolelyPointReadFollowingCreates_readManyInSecondPreferredRegion_createsInFirstPreferredRegion_supportingQueriesThroughHelperContainer",
                "readMany operation with solely point reads should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG
            },
            {
                readManyWithSolelyQueriesFollowingCreates_readManyInSecondPreferredRegion_createsInFirstPreferredRegion_Func,
                "readManyWithSolelyQueriesFollowingCreates_readManyInSecondPreferredRegion_createsInFirstPreferredRegion",
                "readMany operation with solely queries should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG
            }
        };

        return addBooleanFlagsToAllTestConfigs(readManyWithExplicitRegionSwitching_testConfigs);
    }

    @DataProvider(name = "readYouWriteWithExplicitRegionSwitchingTestContext")
    public Object[][] readYouWriteWithExplicitRegionSwitchingTestContext() {

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> pointReadYourPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegionFunc = (container, shouldUsePreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            CosmosItemResponse<TestObject> testObjectFromRead = container.readItem(id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> pointReadAfterPartitionSplitAndPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegionFunc = (container, shouldUsePreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            ThroughputResponse throughputResponse = container.replaceThroughput(ThroughputProperties.createManualThroughput(10_100)).block();

            while (true) {
                assert throughputResponse != null;
                boolean isReplacePending = throughputResponse.isReplacePending();

                if (!isReplacePending) {
                    break;
                }
                throughputResponse = container.readThroughput().block();
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("Waiting for split to complete...");
            }

            logger.info("Split complete!");
            CosmosItemResponse<TestObject> testObjectFromRead = container.readItem(id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> deleteYourLatestUpsert_deleteInSecondPreferredRegionAndUpsertInFirstPreferredRegionFunc = (container, shouldUsePreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            CosmosItemResponse<Object> deleteOperationResponse = container.deleteItem(id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0)))).block();

            assertThat(deleteOperationResponse).isNotNull();
            assertThat(deleteOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NO_CONTENT);

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> replaceYourLatestUpsert_replaceInSecondPreferredRegionAndUpsertInFirstPreferredRegionFunc = (container, shouldUsePreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            TestObject testObjectReplacement = testObjectToBeCreated;
            testObjectReplacement.setStringProp(UUID.randomUUID().toString());

            CosmosItemResponse<TestObject> replaceOperationResponse = container.replaceItem(
                    testObjectReplacement,
                    id,
                    new PartitionKey(pk),
                    new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))))
                .block();

            assertThat(replaceOperationResponse).isNotNull();
            assertThat(replaceOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> readYourPatchYourCreate_createInFirstPreferredRegion_readAndPatchInSecondPreferredRegionFunc = (container, shouldUsePreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            CosmosItemResponse<TestObject> itemResponse = container.createItem(testObjectToBeCreated).block();

            assertThat(itemResponse).isNotNull();
            assertThat(itemResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);

            CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/" + "newProperty", "newVal");
            CosmosItemResponse<TestObject> patchOperationResponse = container.patchItem(
                    id,
                    new PartitionKey(pk),
                    patchOperations,
                    (CosmosPatchItemRequestOptions) new CosmosPatchItemRequestOptions()
                        .setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))),
                    TestObject.class)
                .block();

            assertThat(patchOperationResponse).isNotNull();
            assertThat(patchOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            CosmosItemResponse<JsonNode> readOperationResponse = container.readItem(
                    id,
                    new PartitionKey(pk),
                    new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))),
                    JsonNode.class)
                .block();

            assertThat(readOperationResponse).isNotNull();
            assertThat(readOperationResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            JsonNode itemReadAsJsonNode = readOperationResponse.getItem();

            assertThat(itemReadAsJsonNode).isNotNull();
            assertThat(itemReadAsJsonNode.get("newProperty")).isNotNull();
            assertThat(itemReadAsJsonNode.get("newProperty").asText()).isNotNull();
            assertThat(itemReadAsJsonNode.get("newProperty").asText()).isEqualTo("newVal");

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> readYourCreate_readBatchInSecondPreferredRegion_createBatchInFirstPreferredRegionFunc = (container, shouldUsePreferredRegions) -> {
            String documentId = UUID.randomUUID().toString();
            PartitionKey partitionKey = new PartitionKey(documentId);

            TestObject testObjectToBeCreated = TestObject.create(documentId);

            CosmosBatch batchForCreate = CosmosBatch.createCosmosBatch(partitionKey);
            batchForCreate.createItemOperation(testObjectToBeCreated);

            CosmosBatch batchForRead = CosmosBatch.createCosmosBatch(partitionKey);
            batchForRead.readItemOperation(testObjectToBeCreated.getId());

            CosmosBatchResponse batchCreateResponse = container.executeCosmosBatch(batchForCreate).block();

            assertThat(batchCreateResponse).isNotNull();
            assertThat(batchCreateResponse.getResults()).isNotNull();

            List<CosmosBatchOperationResult> batchCreateResponseResults = batchCreateResponse.getResults();

            assertThat(batchCreateResponseResults.size()).isEqualTo(1);
            assertThat(batchCreateResponseResults.get(0).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);

            CosmosBatchResponse batchReadResponse = container.executeCosmosBatch(
                    batchForRead,
                    new CosmosBatchRequestOptions()
                        .setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))))
                .block();

            assertThat(batchReadResponse).isNotNull();
            assertThat(batchReadResponse.getResults()).isNotNull();

            List<CosmosBatchOperationResult> batchReadResponseResults = batchReadResponse.getResults();

            assertThat(batchReadResponseResults.size()).isEqualTo(1);
            assertThat(batchReadResponseResults.get(0).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);

            return ImmutableSet.of(documentId);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> bulkReadFromSecondPreferredRegionYourBulkCreateInFirstPreferredRegionFunc = (container, shouldUsePreferredRegions) -> {
            int createOperationCount = 10;
            List<String> idsToCreate = new ArrayList<>();

            CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), this.writeRegions, false, shouldUsePreferredRegions);
            CosmosAsyncDatabase database = client.getDatabase(container.getDatabase().getId());
            CosmosAsyncContainer helperContainer = database.getContainer(container.getId());

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

            List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = helperContainer
                .executeBulkOperations(
                    createOperationsFlux)
                .collectList().block();

            assertThat(bulkCreateResponses).isNotNull();
            assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

            bulkCreateResponses.forEach(bulkCreateResponse -> {
                assertThat(bulkCreateResponse.getResponse()).isNotNull();
                assertThat(bulkCreateResponse.getResponse().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
            });

            List<CosmosBulkOperationResponse<Object>> bulkReadResponses = container.executeBulkOperations(
                    readOperationsFlux,
                    new CosmosBulkExecutionOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))))
                .collectList()
                .block();

            assertThat(bulkReadResponses).isNotNull();
            assertThat(bulkReadResponses.size()).isEqualTo(createOperationCount);

            Set<String> idsRead = new HashSet<>();

            bulkReadResponses.forEach(bulkReadResponse -> {
                assertThat(bulkReadResponse.getResponse()).isNotNull();
                assertThat(bulkReadResponse.getResponse().getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
                idsRead.add(bulkReadResponse.getResponse().getItem(TestObject.class).getId());
            });

            safeClose(client);
            return idsRead;
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> changeFeed_fromBeginning_fromSecondPreferredRegion_forFullRange_withCreatesOnFirstPreferredRegion_withSessionGuaranteeFunc = (container, shouldUsePreferredRegions) -> {
            int createOperationCount = 10;
            Set<String> idsAddedByCreates = new HashSet<>();

            Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, createOperationCount).map(i -> {
                String documentId = UUID.randomUUID().toString();
                TestItem testItem = new TestItem(documentId, documentId, documentId);

                idsAddedByCreates.add(documentId);
                return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));
            });

            List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = container
                .executeBulkOperations(createOperationsFlux)
                .collectList()
                .block();

            assertThat(bulkCreateResponses).isNotNull();
            assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

            CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forFullRange())
                .setExcludedRegions(ImmutableList.of(this.writeRegions.get(0)));

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

            return idsReceivedFromChangeFeedRequest;
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> changeFeed_fromBeginningAndFromSecondPreferredRegion_forLogicalPartition_withCreatesOnFirstPreferredRegion_withSessionGuaranteeFunc = (container, shouldUsePreferredRegions) -> {

            try (CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), this.writeRegions, false, shouldUsePreferredRegions)) {
                int createOperationCount = 10;
                Set<String> idsAddedByBulkCreate = new HashSet<>();

                CosmosAsyncDatabase database = client.getDatabase(container.getDatabase().getId());
                CosmosAsyncContainer helperContainer = database.getContainer(container.getId());

                Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, createOperationCount).map(i -> {
                    String documentId = UUID.randomUUID().toString();
                    TestItem testItem = new TestItem(documentId, documentId, documentId);

                    idsAddedByBulkCreate.add(documentId);
                    return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));
                });

                List<CosmosBulkOperationResponse<Object>> bulkCreateResponses = helperContainer.executeBulkOperations(createOperationsFlux).collectList().block();

                assertThat(bulkCreateResponses).isNotNull();
                assertThat(bulkCreateResponses.size()).isEqualTo(createOperationCount);

                String idToObserveUsingChangeFeed = idsAddedByBulkCreate.stream().collect(Collectors.toList()).get(0);

                CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
                    .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(idToObserveUsingChangeFeed)))
                    .setExcludedRegions(ImmutableList.of(this.writeRegions.get(0)));

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

                    if (results.size() >= 1) {
                        break;
                    }
                }

                assertThat(results.size()).isGreaterThanOrEqualTo(1);

                Set<String> idsReceivedFromChangeFeedRequest = new HashSet<>();

                results.forEach(instanceReceivedFromChangeFeedRequest ->
                    idsReceivedFromChangeFeedRequest.add(instanceReceivedFromChangeFeedRequest.get("id").asText()));

                assertThat(idsReceivedFromChangeFeedRequest.size()).isEqualTo(1);

                String idReceivedFromChangeFeedRequest = idsReceivedFromChangeFeedRequest.stream().collect(Collectors.toList()).get(0);
                assertThat(idReceivedFromChangeFeedRequest).isEqualTo(idToObserveUsingChangeFeed);

                return idsReceivedFromChangeFeedRequest;
            } catch (Exception ex) {
                logger.error("Exception occurred : ", ex);
                fail("Bulk creates or change feed operations failed!");
            }

            return new HashSet<>();
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> pointReadFollowsQueryFollowsPointCreate_createInFirstPreferredRegion_pointReadAndQueryInSecondPreferredRegion_Func = (container, shouldUsePreferredRegions) -> {
            TestObject testObjectToBeCreated = TestObject.create();

            String id = testObjectToBeCreated.getId();
            String pk = testObjectToBeCreated.getMypk();

            container.createItem(testObjectToBeCreated).block();

            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Iterator<FeedResponse<TestObject>> feedResponseIterator = container
                .queryItems("SELECT * FROM C", new CosmosQueryRequestOptions(), TestObject.class).byPage().toIterable().iterator();

            while (feedResponseIterator.hasNext()) {
                FeedResponse<TestObject> feedResponse = feedResponseIterator.next();
                assertThat(feedResponse.getResults()).isNotNull();
                assertThat(feedResponse.getResults()).isNotEmpty();
            }

            CosmosItemResponse<TestObject> testObjectFromRead = container.readItem(id, new PartitionKey(pk), new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))), TestObject.class).block();

            assertThat(testObjectFromRead).isNotNull();
            validateTestObjectEquality(testObjectToBeCreated, testObjectFromRead.getItem());

            return ImmutableSet.of(pk);
        };

        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> pointReadFollowsQueryOnDifferentPartitionAsPointReadFollowsPointCreate_createInFirstPreferredRegion_pointReadAndQueryInSecondPreferredRegion_Func = (container, shouldUsePreferredRegions) -> {
            try (CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), this.writeRegions, false, shouldUsePreferredRegions)) {

                int createOperationCount = 100;

                CosmosAsyncDatabase database = client.getDatabase(container.getDatabase().getId());
                CosmosAsyncContainer helperContainer = database.getContainer(container.getId());

                Flux.range(0, createOperationCount)
                    .flatMap(integer -> {
                        TestObject objectToBeCreated = TestObject.create();
                        return container.upsertItem(
                                objectToBeCreated,
                                new PartitionKey(objectToBeCreated.getMypk()),
                                new CosmosItemRequestOptions())
                            .onErrorResume(throwable -> {
                                logger.warn("Throwable: ", throwable);
                                return Mono.empty();
                            });
                    })
                    .blockLast();

                List<FeedRange> feedRanges = helperContainer.getFeedRanges(true).block();

                assertThat(feedRanges).isNotNull();
                assertThat(feedRanges.size()).as("feedRanges' size is expected to be greater than 1.").isGreaterThan(1);

                FeedRange feedRangeOne = feedRanges.get(0);
                FeedRange feedRangeTwo = feedRanges.get(1);

                SqlQuerySpec selectFirstQuery = new SqlQuerySpec("SELECT * FROM C OFFSET 0 LIMIT 1");

                List<TestObject> objectsFromFirstFeedRange = helperContainer
                    .queryItems(selectFirstQuery, new CosmosQueryRequestOptions().setFeedRange(feedRangeOne), TestObject.class)
                    .collectList()
                    .block();

                assertThat(objectsFromFirstFeedRange).isNotNull();
                assertThat(objectsFromFirstFeedRange).isNotEmpty();
                assertThat(objectsFromFirstFeedRange.size()).isEqualTo(1);

                List<TestObject> objectsFromSecondFeedRange = container.queryItems(
                    "SELECT * FROM C",
                        new CosmosQueryRequestOptions()
                            .setFeedRange(feedRangeTwo)
                            .setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))),
                        TestObject.class)
                    .collectList()
                    .block();

                assertThat(objectsFromSecondFeedRange).isNotNull();
                assertThat(objectsFromSecondFeedRange).isNotEmpty();

                CosmosItemResponse<TestObject> readItemResponse = container.readItem(
                        objectsFromFirstFeedRange.get(0).getId(),
                        new PartitionKey(objectsFromFirstFeedRange.get(0).getMypk()),
                        new CosmosItemRequestOptions().setExcludedRegions(ImmutableList.of(this.writeRegions.get(0))),
                        TestObject.class)
                    .block();

                assertThat(readItemResponse).isNotNull();

                TestObject testObjectFromRead = readItemResponse.getItem();

                assertThat(testObjectFromRead).isNotNull();
                validateTestObjectEquality(objectsFromFirstFeedRange.get(0), testObjectFromRead);

                return ImmutableSet.of(testObjectFromRead.getMypk());
            } catch (Exception ex) {
                logger.error("Exception occurred : ", ex);
                fail("Reads, queries and creates should have succeeded!");
            }

            return new HashSet<>();
        };

        Object[][] pointReadYourPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegion_testConfigs = new Object[][] {
            {
                pointReadYourPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegionFunc,
                "pointReadYourPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegion",
                "Document read should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                pointReadAfterPartitionSplitAndPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegionFunc,
                "pointReadAfterPartitionSplitAndPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegionFunc",
                "Document read should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                deleteYourLatestUpsert_deleteInSecondPreferredRegionAndUpsertInFirstPreferredRegionFunc,
                "deleteYourLatestUpsert_deleteInSecondPreferredRegionAndUpsertInFirstPreferredRegion",
                "Document read should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                replaceYourLatestUpsert_replaceInSecondPreferredRegionAndUpsertInFirstPreferredRegionFunc,
                "replaceYourLatestUpsert_replaceInSecondPreferredRegionAndUpsertInFirstPreferredRegion",
                "Document replace should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                readYourPatchYourCreate_createInFirstPreferredRegion_readAndPatchInSecondPreferredRegionFunc,
                "readYourPatchYourCreate_createInFirstPreferredRegion_readAndPatchInSecondPreferredRegion",
                "Document patch or read should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                readYourCreate_readBatchInSecondPreferredRegion_createBatchInFirstPreferredRegionFunc,
                "readYourCreate_readBatchInSecondPreferredRegion_createBatchInFirstPreferredRegion",
                "Batch with read operations should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                bulkReadFromSecondPreferredRegionYourBulkCreateInFirstPreferredRegionFunc,
                "bulkReadFromSecondPreferredRegionYourBulkCreateInFirstPreferredRegion",
                "Bulk execution with read operations should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                changeFeed_fromBeginning_fromSecondPreferredRegion_forFullRange_withCreatesOnFirstPreferredRegion_withSessionGuaranteeFunc,
                "changeFeed_fromBeginning_fromSecondPreferredRegion_forFullRange_withCreatesOnFirstPreferredRegion_withSessionGuarantee",
                "Change feed execution should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                changeFeed_fromBeginningAndFromSecondPreferredRegion_forLogicalPartition_withCreatesOnFirstPreferredRegion_withSessionGuaranteeFunc,
                "changeFeed_fromBeginningAndFromSecondPreferredRegion_forLogicalPartition_withCreatesOnFirstPreferredRegion_withSessionGuarantee",
                "Change feed execution should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                readYourCreate_readBatchInSecondPreferredRegion_createBatchInFirstPreferredRegionFunc,
                "readYourCreate_readBatchInSecondPreferredRegion_createBatchInFirstPreferredRegion",
                "Batch with read operations should have succeeded...",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                pointReadFollowsQueryFollowsPointCreate_createInFirstPreferredRegion_pointReadAndQueryInSecondPreferredRegion_Func,
                "pointReadFollowsQueryFollowsPointCreate_createInFirstPreferredRegion_pointReadAndQueryInSecondPreferredRegion",
                "Point read or query operation should have succeeded...",
                !BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                !MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            },
            {
                pointReadFollowsQueryOnDifferentPartitionAsPointReadFollowsPointCreate_createInFirstPreferredRegion_pointReadAndQueryInSecondPreferredRegion_Func,
                "pointReadFollowsQueryOnDifferentPartitionAsPointReadFollowsPointCreate_createInFirstPreferredRegion_pointReadAndQueryInSecondPreferredRegion",
                "Point read or query should have succeeded!",
                BLOOM_FILTER_FORCED_ACCESSED_FLAG,
                !SPLIT_REQUESTED_FLAG,
                MULTI_PARTITION_CONTAINER_REQUESTED_FLAG
            }
        };

        return addBooleanFlagsToAllTestConfigs(pointReadYourPointCreate_CreateFromFirstPreferredRegionReadFromSecondPreferredRegion_testConfigs);
    }

    @BeforeClass(groups = {"multi-region", "multi-master"})
    public void beforeClass() {

        try (CosmosAsyncClient tempClient = getClientBuilder().buildAsyncClient()) {

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(tempClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            this.readRegions = new ArrayList<>(getAccountLevelLocationContext(databaseAccount, false).serviceOrderedReadableRegions);
            this.writeRegions = new ArrayList<>(getAccountLevelLocationContext(databaseAccount, true).serviceOrderedWriteableRegions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "readYouWriteWithNoExplicitRegionSwitchingTestContext", timeOut = 80 * TIMEOUT)
    public void readYouWriteWithNoExplicitRegionSwitching(
        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> func,
        String testId,
        String genericErrorMessage,
        boolean shouldBloomFilterBeAccessed,
        boolean shouldSinglePartitionContainerBeSplit,
        boolean isMultiPartitionContainerRequired,
        boolean shouldInjectPreferredRegions) throws InterruptedException {

        logger.info("Executing test with id : {}", testId);

        assertThat(this.readRegions.size()).isGreaterThan(1);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), this.readRegions, true, shouldInjectPreferredRegions);
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);
        CosmosContainerProperties expectedCosmosContainerProperties;

        CosmosAsyncContainer resolvedContainer;

        boolean shouldDeleteContainer = false;

        if (isMultiPartitionContainerRequired) {
            resolvedContainer = getSharedMultiPartitionCosmosContainer(client);
            expectedCosmosContainerProperties = new CosmosContainerProperties(resolvedContainer.getId(), "/mypk");
        } else if (shouldSinglePartitionContainerBeSplit) {
            String containerId = UUID.randomUUID() + "-" + "container";
            expectedCosmosContainerProperties = new CosmosContainerProperties(containerId, "/mypk");
            database.createContainerIfNotExists(expectedCosmosContainerProperties).block();
            resolvedContainer = database.getContainer(containerId);
            shouldDeleteContainer = true;
        } else {
            resolvedContainer = getSharedSinglePartitionCosmosContainer(client);
            expectedCosmosContainerProperties = new CosmosContainerProperties(resolvedContainer.getId(), "/mypk");
        }

        Thread.sleep(10_000);

        try {
            RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
            String normalizedSecondPreferredRegion = this.readRegions.get(1).toLowerCase(Locale.ROOT).trim().replace(" ", "");
            Set<String> possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion = func.apply(resolvedContainer, shouldInjectPreferredRegions);

            assertThat(possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion.size())
                .as("possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion size should be greater than or equal to 1.")
                .isGreaterThanOrEqualTo(1);

            possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion.forEach(possiblePartitionKeyWhichSawRequestsInSecondPreferredRegion -> {
                validateBloomFilterPresence(
                    shouldBloomFilterBeAccessed,
                    new PartitionKey(possiblePartitionKeyWhichSawRequestsInSecondPreferredRegion),
                    expectedCosmosContainerProperties.getPartitionKeyDefinition(),
                    resolvedContainer.getLinkWithoutTrailingSlash(),
                    normalizedSecondPreferredRegion,
                    documentClient
                );
            });

        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail(genericErrorMessage);
        } finally {

            if (shouldDeleteContainer) {
                safeDeleteCollection(resolvedContainer);
            }

            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "readYouWriteWithExplicitRegionSwitchingTestContext", timeOut = 80 * TIMEOUT)
    public void readYouWriteWithExplicitRegionSwitching(
        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> func,
        String testId,
        String genericErrorMessage,
        boolean shouldBloomFilterBeAccessed,
        boolean shouldSinglePartitionContainerBeSplit,
        boolean isMultiPartitionContainerRequired,
        boolean shouldInjectPreferredRegions) throws InterruptedException {

        logger.info("Executing test with id : {}", testId);

        assertThat(this.writeRegions.size()).isGreaterThan(1);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), this.writeRegions, true, shouldInjectPreferredRegions);
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);
        CosmosContainerProperties expectedCosmosContainerProperties;
        CosmosAsyncContainer resolvedContainer;

        boolean shouldDeleteContainer = false;

        if (isMultiPartitionContainerRequired) {
            resolvedContainer = getSharedMultiPartitionCosmosContainer(client);
            expectedCosmosContainerProperties = new CosmosContainerProperties(resolvedContainer.getId(), "/mypk");
        } else if (shouldSinglePartitionContainerBeSplit) {
            String containerId = UUID.randomUUID() + "-" + "container";
            expectedCosmosContainerProperties = new CosmosContainerProperties(containerId, "/mypk");
            database.createContainerIfNotExists(expectedCosmosContainerProperties).block();
            resolvedContainer = database.getContainer(containerId);
            shouldDeleteContainer = true;
        } else {
            resolvedContainer = getSharedSinglePartitionCosmosContainer(client);
            expectedCosmosContainerProperties = new CosmosContainerProperties(resolvedContainer.getId(), "/mypk");
        }

        Thread.sleep(10_000);

        try {
            RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
            String normalizedSecondPreferredRegion = this.writeRegions.get(1).toLowerCase(Locale.ROOT).trim().replace(" ", "");
            Set<String> possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion = func.apply(resolvedContainer, shouldInjectPreferredRegions);

            assertThat(possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion.size())
                .as("possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion size should be greater than or equal to 1.")
                .isGreaterThanOrEqualTo(1);

            possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion.forEach(possiblePartitionKeyWhichSawRequestsInSecondPreferredRegion -> {
                validateBloomFilterPresence(
                    shouldBloomFilterBeAccessed,
                    new PartitionKey(possiblePartitionKeyWhichSawRequestsInSecondPreferredRegion),
                    expectedCosmosContainerProperties.getPartitionKeyDefinition(),
                    resolvedContainer.getLinkWithoutTrailingSlash(),
                    normalizedSecondPreferredRegion,
                    documentClient
                );
            });

        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail(genericErrorMessage);
        } finally {

            if (shouldDeleteContainer) {
                safeDeleteCollection(resolvedContainer);
            }

            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "readManyWithNoExplicitRegionSwitchingTestContext", timeOut = 10 * TIMEOUT)
    public void readManyWithNoExplicitRegionSwitching(
        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> func,
        String testId,
        String genericErrorMessage,
        boolean shouldBloomFilterBeAccessed,
        boolean shouldInjectPreferredRegions) throws InterruptedException {

        logger.info("Executing test with id : {}", testId);

        assertThat(this.readRegions.size()).isGreaterThan(1);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), this.readRegions, true, shouldInjectPreferredRegions);
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties expectedCosmosContainerProperties = new CosmosContainerProperties(containerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(50_000);

        CosmosAsyncContainer resolvedContainer;

        database.createContainerIfNotExists(expectedCosmosContainerProperties, throughputProperties).block();
        resolvedContainer = database.getContainer(containerId);

        Thread.sleep(30_000);

        try {
            RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
            String normalizedSecondPreferredRegion = this.readRegions.get(1).toLowerCase(Locale.ROOT).trim().replace(" ", "");
            Set<String> possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion = func.apply(resolvedContainer, shouldInjectPreferredRegions);

            assertThat(possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion.size())
                .as("possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion size should be greater than or equal to 1.")
                .isGreaterThanOrEqualTo(1);

            possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion.forEach(possiblePartitionKeyWhichSawRequestsInSecondPreferredRegion -> {
                validateBloomFilterPresence(
                    shouldBloomFilterBeAccessed,
                    new PartitionKey(possiblePartitionKeyWhichSawRequestsInSecondPreferredRegion),
                    expectedCosmosContainerProperties.getPartitionKeyDefinition(),
                    resolvedContainer.getLinkWithoutTrailingSlash(),
                    normalizedSecondPreferredRegion,
                    documentClient
                );
            });

        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail(genericErrorMessage);
        } finally {
            safeDeleteCollection(resolvedContainer);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "readManyWithExplicitRegionSwitchingTestContext", timeOut = 10 * TIMEOUT)
    public void readManyWithExplicitRegionSwitching(
        BiFunction<CosmosAsyncContainer, Boolean, Set<String>> func,
        String testId,
        String genericErrorMessage,
        boolean shouldBloomFilterBeAccessed,
        boolean shouldInjectPreferredRegions) throws InterruptedException {
        logger.info("Executing test with id : {}", testId);

        assertThat(this.writeRegions.size()).isGreaterThan(1);

        CosmosAsyncClient client = buildAsyncClient(getClientBuilder(), this.writeRegions, true, shouldInjectPreferredRegions);
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties expectedCosmosContainerProperties = new CosmosContainerProperties(containerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(50_000);

        CosmosAsyncContainer resolvedContainer;

        database.createContainerIfNotExists(expectedCosmosContainerProperties, throughputProperties).block();
        resolvedContainer = database.getContainer(containerId);

        Thread.sleep(30_000);

        try {
            RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
            String normalizedSecondPreferredRegion = this.writeRegions.get(1).toLowerCase(Locale.ROOT).trim().replace(" ", "");
            Set<String> possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion = func.apply(resolvedContainer, shouldInjectPreferredRegions);

            assertThat(possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion.size())
                .as("possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion size should be greater than or equal to 1.")
                .isGreaterThanOrEqualTo(1);

            possiblePartitionKeysWhichSawRequestsInSecondPreferredRegion.forEach(possiblePartitionKeyWhichSawRequestsInSecondPreferredRegion -> {
                validateBloomFilterPresence(
                    shouldBloomFilterBeAccessed,
                    new PartitionKey(possiblePartitionKeyWhichSawRequestsInSecondPreferredRegion),
                    expectedCosmosContainerProperties.getPartitionKeyDefinition(),
                    resolvedContainer.getLinkWithoutTrailingSlash(),
                    normalizedSecondPreferredRegion,
                    documentClient
                );
            });

        } catch (Exception ex) {
            logger.error("Exception occurred : ", ex);
            fail(genericErrorMessage);
        } finally {
            safeDeleteCollection(resolvedContainer);
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

    @Test(groups = {"unit"})
    public void testFppRate() {

        Funnel<Integer> integerFunnel = Funnels.integerFunnel();
        Set<Integer> actualNumbers = new HashSet<>();
        Random random = new Random();

        BloomFilter<Integer> integerBasedBloomFilter = BloomFilter.create(integerFunnel, 10_000_000, 0.001);

        int falsePositiveCount = 0;

        for (int i = 1; i <= 10_000_000; i++) {
            int valPicked = random.nextInt(Integer.MAX_VALUE);

            actualNumbers.add(valPicked);
            integerBasedBloomFilter.put(valPicked);
        }

        for (int i = 1; i <= 10_000_000; i++) {
            boolean isPresentInBloomFilter = integerBasedBloomFilter.mightContain(i);
            boolean isPresentInActualSet = actualNumbers.contains(i);

            if (!isPresentInActualSet && isPresentInBloomFilter) {
                falsePositiveCount++;
            }
        }

        double fppRate = (double) falsePositiveCount / 10_000_000d;

        logger.info("False positives count : {}", falsePositiveCount);
        logger.info("FPP Rate : {}", fppRate);

        falsePositiveCount = 0;
        fppRate = 0d;

        for (int i = 1; i <= 10_000_000; i++) {
            int valPicked = random.nextInt(Integer.MAX_VALUE);

            actualNumbers.add(valPicked);
            integerBasedBloomFilter.put(valPicked);
        }

        for (int i = 1; i <= 20_000_000; i++) {
            boolean isPresentInBloomFilter = integerBasedBloomFilter.mightContain(i);
            boolean isPresentInActualSet = actualNumbers.contains(i);

            if (!isPresentInActualSet && isPresentInBloomFilter) {
                falsePositiveCount++;
            }
        }

        fppRate = (double) falsePositiveCount / 20_000_000;

        logger.info("False positives count : {}", falsePositiveCount);
        logger.info("FPP Rate : {}", fppRate);
    }

    private Object[][] addBooleanFlagsToAllTestConfigs(Object[][] testConfigs) {
        List<List<Object>> intermediateTestConfigList = new ArrayList<>();
        boolean[] possibleBooleans = new boolean[]{true, false};

        for (boolean possibleBoolean : possibleBooleans) {
            for (Object[] testConfigForSingleTest : testConfigs) {
                List<Object> testConfigForSingleTestAsMutableList = new ArrayList<>(Arrays.asList(testConfigForSingleTest));
                testConfigForSingleTestAsMutableList.add(possibleBoolean);
                intermediateTestConfigList.add(testConfigForSingleTestAsMutableList);
            }
        }

        testConfigs = intermediateTestConfigList.stream()
            .map(l -> l.stream().toArray(Object[]::new))
            .toArray(Object[][]::new);

        return testConfigs;
    }

    private static CosmosAsyncClient buildAsyncClient(
        CosmosClientBuilder clientBuilder,
        List<String> preferredRegions,
        boolean isRegionScopedSessionCapturingEnabled,
        boolean shouldPreferredRegionsBeInjectedInClient) {

        clientBuilder = clientBuilder
            .preferredRegions(shouldPreferredRegionsBeInjectedInClient ? preferredRegions : Collections.emptyList())
            // override this to ensure a write requests can indeed be routed
            // to a satellite region in a multi-write account
            .multipleWriteRegionsEnabled(true);
        cosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled(clientBuilder, isRegionScopedSessionCapturingEnabled);
        return clientBuilder.buildAsyncClient();
    }

    private AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions,
            regionMap);
    }

    private static void validateTestObjectEquality(TestObject testObject1, TestObject testObject2) {
        assertThat(testObject1.getId()).isEqualTo(testObject2.getId());
        assertThat(testObject1.getMypk()).isEqualTo(testObject2.getMypk());
        assertThat(testObject1.getStringProp()).isEqualTo(testObject2.getStringProp());
    }

    private static void validateBloomFilterPresence(
        boolean shouldPartitionKeyBePresentInBloomFilter,
        PartitionKey partitionKey,
        PartitionKeyDefinition partitionKeyDefinition,
        String collectionNameBasedLink,
        String normalizedRegion,
        RxDocumentClientImpl documentClient) {

        PartitionKeyInternal internalPartitionKey = partitionKeyAccessor.getPartitionKeyInternal(partitionKey);

        ISessionContainer sessionContainer = ReflectionUtils.getSessionContainer(documentClient);
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(documentClient);
        DatabaseAccount databaseAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();
        ConnectionPolicy connectionPolicy = globalEndpointManager.getConnectionPolicy();

        if (!(databaseAccountSnapshot.getEnableMultipleWriteLocations() && connectionPolicy.isMultipleWriteRegionsEnabled())) {
            assertThat(sessionContainer instanceof SessionContainer).isTrue();
            return;
        }

        assertThat(sessionContainer instanceof RegionScopedSessionContainer).isTrue();
        RegionScopedSessionContainer regionScopedSessionContainer = (RegionScopedSessionContainer) sessionContainer;

        if (shouldPartitionKeyBePresentInBloomFilter) {
            assertThat(
                regionScopedSessionContainer.isPartitionKeyResolvedToARegion(
                    internalPartitionKey,
                    partitionKeyDefinition,
                    collectionNameBasedLink,
                    normalizedRegion))
                .isTrue();
        } else {
            assertThat(
                regionScopedSessionContainer.isPartitionKeyResolvedToARegion(
                    internalPartitionKey,
                    partitionKeyDefinition,
                    collectionNameBasedLink,
                    normalizedRegion))
                .isFalse();
        }
    }

    private static class AccountLevelLocationContext {
        private final List<String> serviceOrderedReadableRegions;
        private final List<String> serviceOrderedWriteableRegions;
        private final Map<String, String> regionNameToEndpoint;

        public AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions,
            Map<String, String> regionNameToEndpoint) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
            this.regionNameToEndpoint = regionNameToEndpoint;
        }
    }
}
