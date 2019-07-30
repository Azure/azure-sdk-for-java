// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readCollections() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<CosmosContainerProperties>> feedObservable = createdDatabase.readAllContainers(options);

        int expectedPageSize = (createdCollections.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .totalSize(createdCollections.size())
                .exactlyContainsInAnyOrder(createdCollections.stream().map(d -> d.read().block().properties().resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);

    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
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
        partitionKeyDef.paths(paths);
        CosmosContainerProperties collection = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        return database.createContainer(collection, new CosmosContainerRequestOptions()).block().container();
    }
}
