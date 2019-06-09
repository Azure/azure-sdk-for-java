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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosContainerRequestOptions;
import com.microsoft.azure.cosmos.CosmosContainerSettings;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosDatabaseForTest;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;

import reactor.core.publisher.Flux;

public class ReadFeedCollectionsTest extends TestSuiteBase {

    protected static final int FEED_TIMEOUT = 60000;
    protected static final int SETUP_TIMEOUT = 60000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    public final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosDatabase createdDatabase;
    private List<CosmosContainer> createdCollections = new ArrayList<>();

    private CosmosClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedCollectionsTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readCollections() throws Exception {

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);

        Flux<FeedResponse<CosmosContainerSettings>> feedObservable = createdDatabase.listContainers(options);

        int expectedPageSize = (createdCollections.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<CosmosContainerSettings> validator = new FeedResponseListValidator.Builder<CosmosContainerSettings>()
                .totalSize(createdCollections.size())
                .exactlyContainsInAnyOrder(createdCollections.stream().map(d -> d.read().block().getCosmosContainerSettings().getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerSettings>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);

    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 3; i++) {
            createdCollections.add(createCollections(createdDatabase));
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    public CosmosContainer createCollections(CosmosDatabase database) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        CosmosContainerSettings collection = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        return database.createContainer(collection, new CosmosContainerRequestOptions()).block().getContainer();
    }
}
