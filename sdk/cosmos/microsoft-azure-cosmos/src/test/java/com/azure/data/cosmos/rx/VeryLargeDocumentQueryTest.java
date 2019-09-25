// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
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
    private CosmosAsyncContainer createdCollection;

    private CosmosAsyncClient client;

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
        options.setEnableCrossPartitionQuery(true);

        Flux<FeedResponse<CosmosItemProperties>> feedResponseFlux = createdCollection.queryItems("SELECT * FROM r",
            options);

        AtomicInteger totalCount = new AtomicInteger();
        StepVerifier.create(feedResponseFlux.subscribeOn(Schedulers.single()))
                    .thenConsumeWhile(feedResponse -> {
                        int size = feedResponse.getResults().size();
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

        Mono<CosmosAsyncItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());

        StepVerifier.create(createObservable.subscribeOn(Schedulers.single()))
                    .expectNextMatches(cosmosItemResponse -> cosmosItemResponse.getProperties().getId().equals(docDefinition.getId()))
                    .expectComplete()
                    .verify(Duration.ofMillis(subscriberValidationTimeout));
    }

    @BeforeClass(groups = { "emulator" }, timeOut = 2 * SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
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
