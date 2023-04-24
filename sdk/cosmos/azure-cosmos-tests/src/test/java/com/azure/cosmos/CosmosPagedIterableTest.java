/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
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
import reactor.core.publisher.SynchronousSink;
import reactor.util.concurrent.Queues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    @Test(groups = { "simple" }, timeOut = TIMEOUT, enabled = false)
    public void queryItemsWithCosmosPagedIterable() throws Exception {

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setMaxBufferedItemCount(10);
        CosmosPagedIterable<ObjectNode> cosmosPagedIterable = cosmosContainer.queryItems("select * from c",
                cosmosQueryRequestOptions, ObjectNode.class);

        Iterable<FeedResponse<ObjectNode>> feedResponses = cosmosPagedIterable.iterableByPage(10);
        //  Just creating iterator drains all the results!
        Iterator<FeedResponse<ObjectNode>> iterator = feedResponses.iterator();
        if (iterator.hasNext()) {
            FeedResponse<ObjectNode> next = iterator.next();
            logger.info("Next is : {}", next.getResults().size());
        }
        Thread.sleep(5 * 1000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, enabled = false)
    public void queryItemsWithCosmosPagedFlux() throws Exception {

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setMaxBufferedItemCount(10);
        CosmosAsyncContainer cosmosAsyncContainer = CosmosBridgeInternal.getCosmosAsyncContainer(cosmosContainer);
        CosmosPagedFlux<ObjectNode> cosmosPagedFlux = cosmosAsyncContainer.queryItems("select * from c",
                cosmosQueryRequestOptions, ObjectNode.class);

        CosmosPagedIterable<ObjectNode> cosmosPagedIterable = new CosmosPagedIterable<>(cosmosPagedFlux, 10, 1);
        Iterator<FeedResponse<ObjectNode>> iterator = cosmosPagedIterable.iterableByPage().iterator();
        if (iterator.hasNext()) {
            FeedResponse<ObjectNode> next = iterator.next();
            logger.info("Next is : {}", next.getResults().size());
        }
        Thread.sleep(5 * 1000);
    }

    @Test(groups = {"unit"})
    public void validatePrefetchControl() {
        AtomicInteger prefetchEager1 = new AtomicInteger();
        int bathSize1 = 1;
        int numPages1 = 100;
        Flux<FeedResponse<Long>> eagerDrain1 = Flux.fromIterable(Arrays.asList(1))
                .flatMap(x -> validatePrefetchControl(numPages1, 10, prefetchEager1)
                        .flatMapSequential(Flux::just, 1, 1));
        // assert 32 to 37 pages are fetched eagerly even though batchSize is set to 1
        assertThat(validate(eagerDrain1, prefetchEager1, bathSize1).get()).isBetween(32, 37);

        int bathSize2 = 1;
        int numPages2 = 100;
        AtomicInteger prefetchEager2 = new AtomicInteger();
        List<Flux<FeedResponse<Long>>> fluxList1 = Arrays.asList(validatePrefetchControl(numPages2, 10, prefetchEager2));
        Flux<FeedResponse<Long>> fastDrain2 = Flux.fromIterable(Arrays.asList(1))
                .flatMap(x -> Flux.mergeSequential(fluxList1, 1, 1));
        // assert 32 to 37 pages are fetched eagerly even though batchSize is set to 1
        assertThat(validate(fastDrain2, prefetchEager2, bathSize2).get()).isBetween(32, 37);

        int batchSize3 = 19;
        int numPages3 = 100;
        AtomicInteger prefetchLazy1 = new AtomicInteger();
        Flux<FeedResponse<Long>> lazyDrain1 = Flux.fromIterable(Arrays.asList(1))
                .flatMap(x -> validatePrefetchControl(numPages3, 10, prefetchLazy1)
                        .flatMapSequential(Flux::just, 1, 1), Queues.SMALL_BUFFER_SIZE, 1);
        // assert that no. of pages fetched is close to the batch size
        assertThat(validate(lazyDrain1, prefetchLazy1, batchSize3).get())
                .isLessThan(4 + batchSize3)
                .isGreaterThanOrEqualTo(batchSize3);

        int batchSize4 = 37;
        int numPages4 = 100;
        AtomicInteger prefetchLazy2 = new AtomicInteger();
        List<Flux<FeedResponse<Long>>> fluxList2 = Arrays.asList(validatePrefetchControl(numPages4, 10, prefetchLazy2));
        Flux<FeedResponse<Long>> lazyDrain2 = Flux.just(Arrays.asList(1))
                .flatMap(x -> Flux
                        .mergeSequential(fluxList2, 1, 1), Queues.SMALL_BUFFER_SIZE, 1);
        // assert that no. of pages fetched is close to the batch size
        assertThat(validate(lazyDrain2, prefetchLazy2, batchSize4).get())
                .isLessThan(4 + batchSize4)
                .isGreaterThanOrEqualTo(batchSize4);
    }

    private AtomicInteger validate(Flux<FeedResponse<Long>> flux, AtomicInteger pagesPrefetched, int batchSize) {
        Iterator<FeedResponse<Long>> iterator = flux.toIterable(batchSize).iterator();
        if (iterator.hasNext()) {
            iterator.next();
        }
        return pagesPrefetched;
    }

    private Flux<FeedResponse<Long>> validatePrefetchControl(int numPages, int pageSize, AtomicInteger pagesFetched) {
        return Flux.generate(Tuple::new, (Tuple state, SynchronousSink<FeedResponse<Long>> sink) -> {
            if (state.pageIdx.get() < numPages) {
                state.feedResponse = ModelBridgeInternal.createFeedResponse(LongStream.range(state.pageIdx.get(), state.pageIdx.get() + pageSize)
                                .boxed()
                                .collect(Collectors.toList()),
                        new HashMap<>());
                sink.next(state.feedResponse);
                state.pageIdx.addAndGet(1);
            } else {
                sink.complete();
            }
            return state;
        }).doOnNext(response -> pagesFetched.addAndGet(1));
    }

    static class Tuple {
        AtomicInteger pageIdx;
        FeedResponse<Long> feedResponse;

        Tuple() {
            pageIdx = new AtomicInteger(0);
            feedResponse = ModelBridgeInternal.createFeedResponse(new ArrayList<>(), new HashMap<>());
        }
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

                    return ModelBridgeInternal.createFeedResponse(
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
