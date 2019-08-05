// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionQueryTest extends TestSuiteBase {
    private final static int TIMEOUT = 30000;
    private final String databaseId = CosmosDatabaseForTest.generateId();
    private List<CosmosContainer> createdCollections = new ArrayList<>();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;

   @Factory(dataProvider = "clientBuilders")
    public CollectionQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryCollectionsWithFilter() throws Exception {
        
        String filterCollectionId = createdCollections.get(0).id();
        String query = String.format("SELECT * from c where c.id = '%s'", filterCollectionId);

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);
        Flux<FeedResponse<CosmosContainerProperties>> queryObservable = createdDatabase.queryContainers(query, options);

        List<CosmosContainer> expectedCollections = createdCollections.stream()
                .filter(c -> StringUtils.equals(filterCollectionId, c.id()) ).collect(Collectors.toList());

        assertThat(expectedCollections).isNotEmpty();

        int expectedPageSize = (expectedCollections.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .totalSize(expectedCollections.size())
                .exactlyContainsInAnyOrder(expectedCollections.stream().map(d -> d.read().block().properties().resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllCollections() throws Exception {

        String query = "SELECT * from c";

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);
        Flux<FeedResponse<CosmosContainerProperties>> queryObservable = createdDatabase.queryContainers(query, options);

        List<CosmosContainer> expectedCollections = createdCollections;

        assertThat(expectedCollections).isNotEmpty();

        int expectedPageSize = (expectedCollections.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .totalSize(expectedCollections.size())
                .exactlyContainsInAnyOrder(expectedCollections.stream().map(d -> d.read().block().properties().resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryCollections_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosContainerProperties>> queryObservable = createdDatabase.queryContainers(query, options);

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }
    
    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder().build();
        createdDatabase = createDatabase(client, databaseId);

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerProperties collection = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        createdCollections.add(createCollection(client, databaseId, collection));
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}
