// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.FeedResponse;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.io.FileUtils.ONE_MB;

public class VeryLargeDocumentQueryTest extends TestSuiteBase {

    private final static int TIMEOUT = 60000;
    private final static int SETUP_TIMEOUT = 60000;
    private CosmosAsyncContainer createdCollection;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public VeryLargeDocumentQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    // TODO (DANOBLE) VeryLargeDocumentQueryTest::queryLargeDocuments intermittently times out
    //  Move this test back into the emulator group after we've addressed query performance on 4.X.
    //  see https://github.com/Azure/azure-sdk-for-java/issues/6377
    @Test(groups = { "simple" }, timeOut = 2 * TIMEOUT)
    public void queryLargeDocuments() {

        int cnt = 5;

        for (int i = 0; i < cnt; i++) {
            createLargeDocument();
        }

        FeedOptions options = new FeedOptions();
        
        Flux<FeedResponse<CosmosItemProperties>> feedResponseFlux = createdCollection.queryItems("SELECT * FROM r",
            options, CosmosItemProperties.class);

        AtomicInteger totalCount = new AtomicInteger();
        StepVerifier.create(feedResponseFlux.subscribeOn(Schedulers.single()))
                    .thenConsumeWhile(feedResponse -> {
                        int size = feedResponse.getResults().size();
                        totalCount.addAndGet(size);
                        return true;
                    })
                    .expectComplete()
                    .verify(Duration.ofMillis(2 * TIMEOUT)); // TODO: Doubling timeout. Remove after increasing perf.
    }

    private void createLargeDocument() {
        CosmosItemProperties docDefinition = getDocumentDefinition();

        //Keep size as ~ 1.999MB to account for size of other props
        int size = (int) (ONE_MB * 1.999);
        BridgeInternal.setProperty(docDefinition, "largeString", StringUtils.repeat("x", size));

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> createObservable = 
            createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());

        StepVerifier.create(createObservable.subscribeOn(Schedulers.single()))
                    .expectNextMatches(cosmosItemResponse -> cosmosItemResponse.getProperties().getId().equals(docDefinition.getId()))
                    .expectComplete()
                    .verify(Duration.ofMillis(subscriberValidationTimeout));
    }

    // TODO (DANOBLE) beforeClass method intermittently times out within the SETUP_TIMEOUT interval.
    //  see see https://github.com/Azure/azure-sdk-for-java/issues/6377
    @BeforeClass(groups = { "simple" }, timeOut = 2 * SETUP_TIMEOUT)
    public void before_VeryLargeDocumentQueryTest() {
        client = clientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
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
