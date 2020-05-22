// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.FeedOptions;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
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

    @Test(groups = { "simple" }, timeOut = 2 * TIMEOUT)
    public void queryLargeDocuments() {

        int cnt = 5;

        for (int i = 0; i < cnt; i++) {
            createLargeDocument();
        }

        FeedOptions options = new FeedOptions();
        
        CosmosPagedFlux<CosmosItemProperties> feedResponseFlux = createdCollection.queryItems("SELECT * FROM r",
            options, CosmosItemProperties.class);

        AtomicInteger totalCount = new AtomicInteger();
        StepVerifier.create(feedResponseFlux.byPage().subscribeOn(Schedulers.single()))
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
                    .expectNextMatches(cosmosItemResponse -> BridgeInternal.getProperties(cosmosItemResponse).getId().equals(docDefinition.getId()))
                    .expectComplete()
                    .verify(Duration.ofMillis(subscriberValidationTimeout));
    }

    @BeforeClass(groups = { "simple" }, timeOut = 2 * SETUP_TIMEOUT)
    public void before_VeryLargeDocumentQueryTest() {
        client = getClientBuilder().buildAsyncClient();
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
