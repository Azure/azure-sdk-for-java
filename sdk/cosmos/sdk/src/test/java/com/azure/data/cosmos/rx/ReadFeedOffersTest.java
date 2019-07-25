// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.*;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.TestSuiteBase;
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

    @Test(groups = { "emulator" }, timeOut = FEED_TIMEOUT)
    public void readOffers() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<Offer>> feedObservable = client.readOffers(options);

        int expectedPageSize = (allOffers.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<Offer> validator = new FeedResponseListValidator.Builder<Offer>()
                .totalSize(allOffers.size())
                .exactlyContainsInAnyOrder(allOffers.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Offer>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdDatabase = createDatabase(client, databaseId);

        for(int i = 0; i < 3; i++) {
            createCollections(client);
        }

        allOffers = client.readOffers(null)
                          .map(FeedResponse::results)
                          .collectList()
                          .map(list -> list.stream().flatMap(Collection::stream).collect(Collectors.toList()))
                          .single()
                          .block();
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase);
        safeClose(client);
    }

    public DocumentCollection createCollections(AsyncDocumentClient client) {
        DocumentCollection collection = new DocumentCollection();
        collection.id(UUID.randomUUID().toString());
        
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        collection.setPartitionKey(partitionKeyDef);

        return client.createCollection(getDatabaseLink(), collection, null).single().block().getResource();
    }

    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.id();
    }
}
