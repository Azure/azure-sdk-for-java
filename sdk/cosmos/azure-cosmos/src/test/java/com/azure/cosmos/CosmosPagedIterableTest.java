/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.accesshelpers.FeedResponseHelper;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.cosmos.util.UtilBridgeInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.util.concurrent.Queues;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosPagedIterableTest extends TestSuiteBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosPagedIterableTest.class);
    private static final int NUM_OF_ITEMS = 10;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CosmosClient cosmosClient;
    private CosmosContainer cosmosContainer;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public CosmosPagedIterableTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosPagedIterableTest() throws JsonProcessingException {
        assertThat(this.cosmosClient).isNull();
        this.cosmosClient = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.cosmosClient.asyncClient());
        cosmosContainer =
            cosmosClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
        createItems(NUM_OF_ITEMS);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        if (this.cosmosClient != null) {
            this.cosmosClient.close();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsByPageWithCosmosPagedIterableHandler() throws Exception {
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<ObjectNode> cosmosPagedIterable =
            cosmosContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);

        AtomicInteger handleCount = new AtomicInteger();
        cosmosPagedIterable = cosmosPagedIterable.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            if (cosmosDiagnostics != null) {
                handleCount.incrementAndGet();
            }
        });

        AtomicInteger feedResponseCount = new AtomicInteger();
        cosmosPagedIterable.iterableByPage().forEach(feedResponse -> {
            feedResponseCount.incrementAndGet();
        });

        assertThat(handleCount.get() >= 1).isTrue();
        assertThat(handleCount.get()).isEqualTo(feedResponseCount.get());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsBySubscribeWithCosmosPagedIterableHandler() throws Exception {

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<ObjectNode> cosmosPagedIterable =
            cosmosContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);

        AtomicInteger handleCount = new AtomicInteger();
        cosmosPagedIterable = cosmosPagedIterable.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            if (cosmosDiagnostics != null) {
                handleCount.incrementAndGet();
            }
        });

        //  Drain the results of reading the items
        cosmosPagedIterable.forEach(objectNode -> { });

        assertThat(handleCount.get() >= 1).isTrue();
    }

    @Test(groups = { "unit" })
    public void PagePrefetchCountReasonablyLow() {

        validatePrefetchCount(null);
        validatePrefetchCount(1);
        validatePrefetchCount(4);
        validatePrefetchCount(8);
        validatePrefetchCount(Queues.XS_BUFFER_SIZE);
        validatePrefetchCount(50);
    }

    private void validatePrefetchCount(Integer prefetchCount) {
        final int SMALLEST_POSSIBLE_QUEUE_SIZE_LARGER_THAN_ONE = 8;
        int effectivePrefetchCount;

        if (prefetchCount == null) {
            effectivePrefetchCount = SMALLEST_POSSIBLE_QUEUE_SIZE_LARGER_THAN_ONE;
        } else if (prefetchCount == 1) {
            effectivePrefetchCount = 1;
        } else {
            effectivePrefetchCount = Math.max(8, prefetchCount);
        }

        LOGGER.info(
            "ValidatePrefetchCount - prefetchCount {}, effectivePrefetchCount {}",
            prefetchCount,
            effectivePrefetchCount);

        assertThat(effectivePrefetchCount).isGreaterThan(0);
        assertThat(effectivePrefetchCount).isLessThan(70);

        AtomicLong lastRetrieved = new AtomicLong(0);
        AtomicLong pagesRetrieved = new AtomicLong(0);

        Flux<FeedResponse<Long>> feedResponseGenerator =
            Flux
                // generate 1000 pages
                .range(1, 1000)
                .map(pageIndex -> {
                    pagesRetrieved.set(pageIndex);
                    lastRetrieved.set((pageIndex * 100));

                    return FeedResponseHelper.createFeedResponse(
                        LongStream.range((pageIndex - 1) * 100 + 1, (pageIndex * 100) + 1)
                                  .boxed()
                                  .collect(Collectors.toList()),
                        new HashMap<>()
                    );
                });

        Function<CosmosPagedFluxOptions, Flux<FeedResponse<Long>>>  pageGenerator = (options) -> {
            lastRetrieved.set(0);
            pagesRetrieved.set(0);
            return feedResponseGenerator;
        };

        CosmosPagedFlux<Long> flux = UtilBridgeInternal
            .createCosmosPagedFlux(pageGenerator);

        CosmosPagedIterable<Long> pagedIterable = prefetchCount == null ?
            new CosmosPagedIterable<Long>(flux, 1000000) :
            new CosmosPagedIterable<Long>(flux, 1000000, prefetchCount);
        Iterator<FeedResponse<Long>> iterator = pagedIterable.iterableByPage().iterator();
        assertThat(iterator.hasNext()).isTrue();
        validatePage(iterator.next(), 100L);
        assertThat(iterator.hasNext()).isTrue();
        validatePage(iterator.next(), 200L);
        assertThat(iterator.hasNext()).isTrue();
        validatePage(iterator.next(), 300L);
        assertThat(iterator.hasNext()).isTrue();
        validatePage(iterator.next(), 400L);
        assertThat(iterator.hasNext()).isTrue();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(pagesRetrieved.get()).isLessThanOrEqualTo(4 + effectivePrefetchCount);
        assertThat(lastRetrieved.get()).isLessThanOrEqualTo((4 + effectivePrefetchCount) * 100);
    }

    private void validatePage(FeedResponse<Long> page, Long expectedLastValue) {
        assertThat(page).isNotNull();
        assertThat(page.getResults()).isNotNull();
        assertThat(page.getResults().size()).isEqualTo(100);
        assertThat(page.getResults().get(0)).isEqualTo(expectedLastValue - 99);
        assertThat(page.getResults().get(99)).isEqualTo(expectedLastValue);
    }

    private void createItems(int numOfItems) throws JsonProcessingException {
        for (int i = 0; i < numOfItems; i++) {
            ObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString(), String.valueOf(i));
            cosmosContainer.createItem(properties);
        }
    }

    private ObjectNode getDocumentDefinition(String documentId, String pkId) throws JsonProcessingException {

        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , documentId, pkId);
        return
            OBJECT_MAPPER.readValue(json, ObjectNode.class);
    }

}
