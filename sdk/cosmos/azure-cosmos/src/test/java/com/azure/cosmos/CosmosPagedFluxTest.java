/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosPagedFluxTest extends TestSuiteBase {

    private static final int NUM_OF_ITEMS = 10;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncContainer cosmosAsyncContainer;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public CosmosPagedFluxTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosPagedFluxTest() throws JsonProcessingException {
        assertThat(this.cosmosAsyncClient).isNull();
        this.cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.cosmosAsyncClient);
        cosmosAsyncContainer =
            cosmosAsyncClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
        createItems(NUM_OF_ITEMS);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.cosmosAsyncClient).isNotNull();
        this.cosmosAsyncClient.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsByPageWithCosmosPagedFluxHandler() throws Exception {
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedFlux<ObjectNode> cosmosPagedFlux =
            cosmosAsyncContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);

        AtomicInteger handleCount = new AtomicInteger();
        AtomicInteger chainedHandleCount = new AtomicInteger();
        AtomicInteger yetAnotherChainedHandleCount = new AtomicInteger();
        cosmosPagedFlux = cosmosPagedFlux.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            if (cosmosDiagnostics != null) {
                handleCount.incrementAndGet();
            }
        });

        cosmosPagedFlux = cosmosPagedFlux.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            if (cosmosDiagnostics != null) {
                chainedHandleCount.incrementAndGet();
            }
        });

        cosmosPagedFlux = cosmosPagedFlux.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            if (cosmosDiagnostics != null) {
                yetAnotherChainedHandleCount.incrementAndGet();
            }
        });

        AtomicInteger feedResponseCount = new AtomicInteger();
        cosmosPagedFlux.byPage().toIterable().forEach(feedResponse -> {
            feedResponseCount.incrementAndGet();
        });

        assertThat(handleCount.get() >= 1).isTrue();
        assertThat(handleCount.get()).isEqualTo(feedResponseCount.get());
        assertThat(handleCount.get()).isEqualTo(chainedHandleCount.get());
        assertThat(handleCount.get()).isEqualTo(yetAnotherChainedHandleCount.get());
    }

    private FeedResponse<ObjectNode> generateFeedResponse(String prefix, int pageSequenceNumber, int documentStartIndex) {
        String id1 = prefix + "_Page" + String.format("%02d", pageSequenceNumber) + "_" +
            String.format("%02d", documentStartIndex);
        String id2 = prefix + "_Page" + String.format("%02d", pageSequenceNumber) + "_" +
            String.format("%02d", documentStartIndex + 1);
        String continuationToken = prefix + "_Page" + String.format("%02d", pageSequenceNumber) + "_ContinuationToken";
        try {
            FeedResponse<ObjectNode> r = ModelBridgeInternal.createFeedResponse(
                Arrays.asList(new ObjectNode[] { getDocumentDefinition(id1, id1), getDocumentDefinition(id2, id2) }),
                new ConcurrentHashMap<>()
            );

            ModelBridgeInternal.setFeedResponseContinuationToken(continuationToken, r);

            return r;
        } catch (JsonProcessingException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void fabianmDummy() throws Exception {
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        Flux<FeedResponse<ObjectNode>> slowProducer = Flux
            .just(
                generateFeedResponse("Slow", 1, 1),
                generateFeedResponse("Slow", 2, 1),
                generateFeedResponse("Slow", 3, 1),
                generateFeedResponse("Slow", 4, 1),
                generateFeedResponse("Slow", 5, 1)
            )
            .delayElements(Duration.ofMillis(500));

        Flux<FeedResponse<ObjectNode>> fastProducer = Flux
            .just(
                generateFeedResponse("Fast", 1, 1),
                generateFeedResponse("Fast", 2, 1),
                generateFeedResponse("Fast", 3, 1),
                generateFeedResponse("Fast", 4, 1),
                generateFeedResponse("Fast", 5, 1)
            )
            .map(r -> {
                if (r.getContinuationToken().contains("Page05")) {
                    throw new IllegalStateException("abc");
                }

                return r;
            });
            //.delayElements(Duration.ofMillis(100));;

        List<Flux<FeedResponse<ObjectNode>>> toBeMerged = new ArrayList<>();
        toBeMerged.add(slowProducer);
        toBeMerged.add(fastProducer);
        Flux<FeedResponse<ObjectNode>> mergedFlux = Flux.mergeSequential(toBeMerged, 1, 2);

        CosmosPagedFlux<ObjectNode> cosmosPagedFlux = UtilBridgeInternal.createCosmosPagedFlux(
            (options) -> mergedFlux
        );

        cosmosPagedFlux = cosmosPagedFlux.handle(feedResponse -> {
            System.out.println("HANDLE: " + feedResponse.getContinuationToken());
        });

        AtomicInteger feedResponseCount = new AtomicInteger();
        cosmosPagedFlux.byPage().toIterable().forEach(feedResponse -> {
            System.out.println("FOREACH: " + feedResponse.getContinuationToken());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsBySubscribeWithCosmosPagedFluxHandler() throws Exception {

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedFlux<ObjectNode> cosmosPagedFlux =
            cosmosAsyncContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);

        AtomicInteger handleCount = new AtomicInteger();
        cosmosPagedFlux = cosmosPagedFlux.handle(feedResponse -> {
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();
            if (cosmosDiagnostics != null) {
                handleCount.incrementAndGet();
            }
        });

        //  Drain the results of reading the items
        cosmosPagedFlux.toIterable().forEach(objectNode -> {});

        assertThat(handleCount.get() >= 1).isTrue();
    }

    private void createItems(int numOfItems) throws JsonProcessingException {
        for (int i = 0; i < numOfItems; i++) {
            ObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString(), String.valueOf(i));
            cosmosAsyncContainer.createItem(properties).block();
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
