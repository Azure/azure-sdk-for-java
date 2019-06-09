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
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;

import reactor.core.publisher.Flux;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SinglePartitionReadFeedDocumentsTest extends TestSuiteBase {

    private CosmosContainer createdCollection;
    private List<CosmosItemProperties> createdDocuments;

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public SinglePartitionReadFeedDocumentsTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readDocuments() {
        final FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.maxItemCount(2);
        final Flux<FeedResponse<CosmosItemProperties>> feedObservable = createdCollection.listItems(options);
        final int expectedPageSize = (createdDocuments.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(createdDocuments.size())
                .numberOfPages(expectedPageSize)
                .exactlyContainsInAnyOrder(createdDocuments.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdCollection = getSharedSinglePartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        List<CosmosItemProperties> docDefList = new ArrayList<>();

        for(int i = 0; i < 5; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments = bulkInsertBlocking(createdCollection, docDefList);
        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private CosmosItemProperties getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemProperties doc = new CosmosItemProperties(String.format("{ "
            + "\"id\": \"%s\", "
            + "\"mypk\": \"%s\", "
            + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
            + "}"
            , uuid, uuid));
        return doc;
    }
}
