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
package com.microsoft.azure.cosmosdb.rx;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosClientBuilder;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;

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
    private List<CosmosItemSettings> createdDocuments;

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public SinglePartitionReadFeedDocumentsTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readDocuments() {
        final FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxItemCount(2);
        final Flux<FeedResponse<CosmosItemSettings>> feedObservable = createdCollection.listItems(options);
        final int expectedPageSize = (createdDocuments.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<CosmosItemSettings> validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .totalSize(createdDocuments.size())
                .numberOfPages(expectedPageSize)
                .exactlyContainsInAnyOrder(createdDocuments.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdCollection = getSharedSinglePartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        List<CosmosItemSettings> docDefList = new ArrayList<>();

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

    private CosmosItemSettings getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemSettings doc = new CosmosItemSettings(String.format("{ "
            + "\"id\": \"%s\", "
            + "\"mypk\": \"%s\", "
            + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
            + "}"
            , uuid, uuid));
        return doc;
    }
}
