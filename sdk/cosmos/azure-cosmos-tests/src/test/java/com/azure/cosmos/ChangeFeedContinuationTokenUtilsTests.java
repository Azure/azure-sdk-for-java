/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosChangeFeedContinuationTokenUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedContinuationTokenUtilsTests extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public ChangeFeedContinuationTokenUtilsTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @Test(groups = { "emulator" }, timeOut = 2 * TIMEOUT)
    public void extractContinuationTokens() {
        // create a container with at least 3 partitions
        String testContainerId = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(testContainerId, "/mypk");

        try {
            CosmosAsyncContainer testContainer =
                createCollection(this.createdDatabase, containerProperties, new CosmosContainerRequestOptions(), 18000);

            List<FeedRange> feedRanges = testContainer.getFeedRanges().block();
            assertThat(feedRanges.size()).isEqualTo(3);

            // create few items into the container
            for (int i = 0; i < 50; i++) {
                testContainer.createItem(TestItem.createNewItem(UUID.randomUUID().toString())).block();
            }

            // validate items persisted on each feedRange
            Map<FeedRange, List<String>> pkValuesByFeedRange = new ConcurrentHashMap<>();
            for (FeedRange feedRange : feedRanges) {
                CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
                cosmosQueryRequestOptions.setFeedRange(feedRange);
                List<String> pkValues = testContainer.readAllItems(cosmosQueryRequestOptions, TestItem.class)
                    .map(TestItem::getMypk)
                    .collectList()
                    .block();
                assertThat(pkValues.size()).isGreaterThan(0);
                pkValuesByFeedRange.put(feedRange, pkValues);
            }

            // do initial query change feed
            AtomicReference<String> continuationToken = new AtomicReference<>();
            testContainer
                .queryChangeFeed(
                    CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(
                        FeedRange.forFullRange()),
                    JsonNode.class)
                .byPage()
                .doOnNext(response -> {
                    continuationToken.set(response.getContinuationToken());
                })
                .blockLast();
            assertThat(continuationToken.get()).isNotEmpty();
            ChangeFeedState changeFeedState = ChangeFeedState.fromString(continuationToken.get());
            assertThat(changeFeedState).isInstanceOf(ChangeFeedStateV1.class);
            ChangeFeedStateV1 changeFeedStateV1 = (ChangeFeedStateV1) changeFeedState;
            assertThat(changeFeedStateV1.getContinuation().getContinuationTokenCount()).isEqualTo(3);

            // create few more items on each feed range
            List<String> expectedNewItems = new ArrayList<>();
            for (FeedRange feedRange : pkValuesByFeedRange.keySet()) {
                List<String> pkValues = pkValuesByFeedRange.get(feedRange);
                for (int i = 0; i < 5; i++) {
                    TestItem testItem = TestItem.createNewItem(pkValues.get(0));
                    testContainer.createItem(testItem).block();
                    expectedNewItems.add(testItem.getId());
                }
            }

            // extract continuation tokens and make sure we can still use the new continuation token to read back all items
            List<Integer> expectedContinuationTokenCounts = Arrays.asList(null, -1, 0, 1, 2, 3, 4);
            for (Integer expectedContinuationTokenCount : expectedContinuationTokenCounts) {
                Map<FeedRange, String> extractedTokens =
                    CosmosChangeFeedContinuationTokenUtils.extractContinuationTokens(continuationToken.get(), expectedContinuationTokenCount);

                if (expectedContinuationTokenCount == null ||
                    expectedContinuationTokenCount <= 0 ||
                    expectedContinuationTokenCount > 3) {
                    assertThat(extractedTokens.size()).isEqualTo(3);
                } else {
                    assertThat(extractedTokens.size()).isEqualTo(expectedContinuationTokenCount);
                }

                List<String> fetchedItems = new ArrayList<>();
                for (FeedRange feedRange : extractedTokens.keySet()) {
                    CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                        CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(extractedTokens.get(feedRange));

                    testContainer.queryChangeFeed(changeFeedRequestOptions, TestItem.class)
                        .byPage()
                        .doOnNext(response -> {
                            fetchedItems.addAll(
                                response
                                    .getResults()
                                    .stream()
                                    .map(TestItem::getId)
                                    .collect(Collectors.toList()));
                        })
                        .blockLast();
                }

                assertThat(fetchedItems.size()).isEqualTo(expectedNewItems.size());
                assertThat(fetchedItems.containsAll(expectedNewItems)).isTrue();
            }
        } finally {
            this.createdDatabase
                .getContainer(testContainerId)
                .delete()
                .onErrorResume(throwable -> {
                    logger.warn("Failed to delete container {}", testContainerId, throwable);
                    return Mono.empty();
                })
                .block();
        }
    }

    @AfterClass(groups = { "emulator" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeClose(this.client);
    }

    private static class TestItem {
        private String id;
        private String mypk;
        private String prop;

        public TestItem(){}

        public TestItem(String id, String mypk, String prop) {
            this.id = id;
            this.mypk = mypk;
            this.prop = prop;
        }

        public static TestItem createNewItem(String pkValue) {
            return new TestItem(UUID.randomUUID().toString(), pkValue, UUID.randomUUID().toString());
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public String getProp() {
            return prop;
        }

        public void setProp(String prop) {
            this.prop = prop;
        }
    }
}
