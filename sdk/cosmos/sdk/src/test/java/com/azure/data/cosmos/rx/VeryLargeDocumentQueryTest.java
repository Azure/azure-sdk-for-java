// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.io.FileUtils.ONE_MB;

//FIXME: beforeClass method times out.
@Ignore
public class VeryLargeDocumentQueryTest extends TestSuiteBase {

    private final static int TIMEOUT = 60000;
    private final static int SETUP_TIMEOUT = 60000;
    private CosmosContainer createdCollection;

    private CosmosClient client;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public VeryLargeDocumentQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryLargeDocuments() {

        int cnt = 5;

        for(int i = 0; i < cnt; i++) {
            createLargeDocument();
        }

        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);

        Flux<FeedResponse<CosmosItemProperties>> feedResponseFlux = createdCollection.queryItems("SELECT * FROM r",
            options);

        AtomicInteger totalCount = new AtomicInteger();
        StepVerifier.create(feedResponseFlux.subscribeOn(Schedulers.single()))
                    .thenConsumeWhile(feedResponse -> {
                        int size = feedResponse.results().size();
                        totalCount.addAndGet(size);
                        return true;
                    })
                    .expectComplete()
                    .verify(Duration.ofMillis(subscriberValidationTimeout));
    }

    private void createLargeDocument() {
        CosmosItemProperties docDefinition = getDocumentDefinition();

        //Keep size as ~ 1.999MB to account for size of other props
        int size = (int) (ONE_MB * 1.999);
        BridgeInternal.setProperty(docDefinition, "largeString", StringUtils.repeat("x", size));

        Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());

        StepVerifier.create(createObservable.subscribeOn(Schedulers.single()))
                    .expectNextMatches(cosmosItemResponse -> cosmosItemResponse.properties().id().equals(docDefinition.id()))
                    .expectComplete()
                    .verify(Duration.ofMillis(subscriberValidationTimeout));
    }

    @BeforeClass(groups = { "emulator" }, timeOut = 2 * SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static CosmosItemProperties getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemProperties doc = new CosmosItemProperties(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "}"
                , uuid, uuid));
        return doc;
    }
}
