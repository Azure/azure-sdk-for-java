// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

//TODO: change to use external TestSuiteBase
public class ReadFeedOffersTest extends TestSuiteBase {

    protected static final int FEED_TIMEOUT = 60000;
    protected static final int SETUP_TIMEOUT = 60000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private List<Offer> allOffers = new ArrayList<>();

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedOffersTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT)
    public void readOffers() throws Exception {

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, 2);

        CosmosAsyncClient cosmosClient = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .buildAsyncClient();
        QueryFeedOperationState dummyState = new QueryFeedOperationState(
            cosmosClient,
            "SomeSpanName",
            "SomeDBName",
            "SomeContainerName",
            ResourceType.Document,
            OperationType.Query,
            null,
            options,
            new CosmosPagedFluxOptions()
        );

        Flux<FeedResponse<Offer>> feedObservable = client.readOffers(dummyState);

        int maxItemCount = ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(options);
        int expectedPageSize = (allOffers.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<Offer> validator = new FeedResponseListValidator.Builder<Offer>()
                .totalSize(allOffers.size())
                .exactlyContainsInAnyOrder(allOffers.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Offer>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_ReadFeedOffersTest() {
        client = clientBuilder().build();
        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 3; i++) {
            createCollections(client);
        }

        QueryFeedOperationState offerDummyState = TestUtils.createDummyQueryFeedOperationState(
            ResourceType.Offer,
            OperationType.ReadFeed,
            new CosmosQueryRequestOptions(),
            client
        );

        try {
            allOffers = client.readOffers(offerDummyState)
                              .map(FeedResponse::getResults)
                              .collectList()
                              .map(list -> list.stream().flatMap(Collection::stream).collect(Collectors.toList()))
                              .single()
                              .block();
        } finally {
            safeClose(offerDummyState);
        }
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase);
        safeClose(client);
    }

    public DocumentCollection createCollections(AsyncDocumentClient client) {
        DocumentCollection collection = new DocumentCollection();
        collection.setId(UUID.randomUUID().toString());

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        collection.setPartitionKey(partitionKeyDef);

        return client.createCollection(getDatabaseLink(), collection, null).block().getResource();
    }

    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.getId();
    }
}
