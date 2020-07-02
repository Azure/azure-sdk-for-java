// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionQueryTest extends TestSuiteBase {
    private final static int TIMEOUT = 300000;
    private final String databaseId = CosmosDatabaseForTest.generateId();
    private List<CosmosAsyncContainer> createdCollections = new ArrayList<>();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

   @Factory(dataProvider = "clientBuilders")
    public CollectionQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryCollectionsWithFilter() throws Exception {

        String filterCollectionId = createdCollections.get(0).getId();
        String query = String.format("SELECT * from c where c.id = '%s'", filterCollectionId);

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int maxItemCount = 2;
        CosmosPagedFlux<CosmosContainerProperties> queryObservable = createdDatabase.queryContainers(query, options);

        List<CosmosAsyncContainer> expectedCollections = createdCollections.stream()
                .filter(c -> StringUtils.equals(filterCollectionId, c.getId()) ).collect(Collectors.toList());

        assertThat(expectedCollections).isNotEmpty();

        int expectedPageSize = (expectedCollections.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .totalSize(expectedCollections.size())
                .exactlyContainsInAnyOrder(expectedCollections.stream().map(d -> d.read().block().getProperties().getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllCollections() throws Exception {

        String query = "SELECT * from c";

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int maxItemCount = 2;
        CosmosPagedFlux<CosmosContainerProperties> queryObservable = createdDatabase.queryContainers(query, options);

        List<CosmosAsyncContainer> expectedCollections = createdCollections;

        assertThat(expectedCollections).isNotEmpty();

        int expectedPageSize = (expectedCollections.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .totalSize(expectedCollections.size())
                .exactlyContainsInAnyOrder(expectedCollections.stream().map(d -> d.read().block().getProperties().getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryCollections_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        CosmosPagedFlux<CosmosContainerProperties> queryObservable = createdDatabase.queryContainers(query, options);

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryByRecreatCollectionWithSameName() throws Exception {
        String testCollectionId = UUID.randomUUID().toString();
        CosmosContainerProperties collectionDefinition = getCollectionDefinition(testCollectionId);
        CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
        String query = "SELECT * FROM r";

        // step1: create container and do query
        this.createdDatabase.createContainer(collectionDefinition).block();
        CosmosAsyncContainer container = this.createdDatabase.getContainer(collectionDefinition.getId());
        InternalObjectNode document1 = getDocumentDefinition();
        container.createItem(document1).block();
        container.queryItems(query, requestOptions, InternalObjectNode.class);

        // step2: delete the container created on step 1
        safeDeleteCollection(container);

        // step3: create a new collection with the same id as step 1 and do query
        this.createdDatabase.createContainer(collectionDefinition).block();
        container = this.createdDatabase.getContainer(collectionDefinition.getId());
        container.createItem(document1).block();
        CosmosPagedFlux<InternalObjectNode> queryFlux = container.queryItems(query, requestOptions, InternalObjectNode.class);
        FeedResponseListValidator<InternalObjectNode> queryValidator = new FeedResponseListValidator.Builder<InternalObjectNode>()
            .totalSize(1)
            .numberOfPages(1)
            .build();
        validateQuerySuccess(queryFlux.byPage(10), queryValidator);

        safeDeleteCollection(container);
    }


    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_CollectionQueryTest() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, databaseId);

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collection = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        createdCollections.add(createCollection(client, databaseId, collection));
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    private static InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "}"
            , uuid, uuid));
        return doc;
    }
}
