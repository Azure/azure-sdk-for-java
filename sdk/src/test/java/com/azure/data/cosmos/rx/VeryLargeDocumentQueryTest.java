/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
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
        docDefinition.set("largeString", StringUtils.repeat("x", size));

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
