// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
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
// Uses rx.TestSuiteBase (local package)
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

import static org.assertj.core.api.Assertions.assertThat;

//TODO: change to use external TestSuiteBase
public class ReadFeedOffersTest extends TestSuiteBase {

    protected static final int FEED_TIMEOUT = 60000;
    protected static final int SETUP_TIMEOUT = 60000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private final List<DocumentCollection> createdCollections = new ArrayList<>();
    private List<Offer> expectedOffers = new ArrayList<>();

    private AsyncDocumentClient client;

    @Factory(dataProvider = "internalClientBuilders")
    public ReadFeedOffersTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "query" }, timeOut = FEED_TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readOffers() throws Exception {

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, 2);

        try (CosmosAsyncClient cosmosClient = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .buildAsyncClient()) {
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

            // readOffers is an account-global read. Live tests share a fixed account, so
            // other test runs may create/delete containers (and thus offers) concurrently.
            // Assert only that the offers for the containers created by this test are present
            // (containment) rather than asserting the exact account-wide set/count/page-count.
            FeedResponseListValidator<Offer> validator = new FeedResponseListValidator.Builder<Offer>()
                .containsResourceIds(expectedOffers.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPagesIsGreaterThanOrEqualTo(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Offer>()
                    .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
            validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
        }
    }

    @BeforeClass(groups = { "query" }, timeOut = 6 * SETUP_TIMEOUT)
    public void before_ReadFeedOffersTest() {
        client = clientBuilder().build();
        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 3; i++) {
            createdCollections.add(createCollections(client));
        }

        QueryFeedOperationState offerDummyState = TestUtils.createDummyQueryFeedOperationState(
            ResourceType.Offer,
            OperationType.ReadFeed,
            new CosmosQueryRequestOptions(),
            client
        );

        try {
            List<String> createdCollectionRids = createdCollections.stream()
                .map(DocumentCollection::getResourceId)
                .collect(Collectors.toList());
            // An Offer's getOfferResourceId() is the resource id of the collection it applies to,
            // whereas getResourceId() is the Offer's own resource id. Filter the account-global
            // offer feed down to the offers that belong to the collections this test created.
            expectedOffers = client.readOffers(offerDummyState)
                              .map(FeedResponse::getResults)
                              .collectList()
                              .map(list -> list.stream().flatMap(Collection::stream).collect(Collectors.toList()))
                              .single()
                              .block()
                              .stream()
                              .filter(o -> createdCollectionRids.contains(o.getOfferResourceId()))
                              .collect(Collectors.toList());
        } finally {
            safeClose(offerDummyState);
        }

        // Guard against the containment assertion silently degrading to a no-op: each created
        // collection has exactly one dedicated-throughput offer, so we must have resolved one
        // offer per collection. If this fails the offer read/filtering is broken, not the account.
        assertThat(expectedOffers)
            .describedAs("offers resolved for the collections created by this test")
            .hasSize(createdCollections.size());
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase);
        safeClose(client);
    }

    public DocumentCollection createCollections(AsyncDocumentClient client) {
        return executeControlPlaneWithRetry(() -> {
            DocumentCollection collection = new DocumentCollection();
            collection.setId(UUID.randomUUID().toString());

            PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
            ArrayList<String> paths = new ArrayList<String>();
            paths.add("/mypk");
            partitionKeyDef.setPaths(paths);
            collection.setPartitionKey(partitionKeyDef);

            return client.createCollection(getDatabaseLink(), collection, null).block().getResource();
        });
    }

    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.getId();
    }
}
