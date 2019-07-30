// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.internal.AsyncDocumentClient.Builder;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.*;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.TestSuiteBase;
import org.assertj.core.util.Strings;
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

//TODO: change to use external TestSuiteBase
public class OfferQueryTest extends TestSuiteBase {

    public final static int SETUP_TIMEOUT = 40000;
    public final String databaseId = DatabaseForTest.generateId();

    private List<DocumentCollection> createdCollections = new ArrayList<>();

    private AsyncDocumentClient client;

    private String getDatabaseLink() {
        return TestUtils.getDatabaseNameLink(databaseId);
    }

    @Factory(dataProvider = "clientBuilders")
    public OfferQueryTest(Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryOffersWithFilter() throws Exception {
        String collectionResourceId = createdCollections.get(0).resourceId();
        String query = String.format("SELECT * from c where c.offerResourceId = '%s'", collectionResourceId);

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);
        Flux<FeedResponse<Offer>> queryObservable = client.queryOffers(query, null);

        List<Offer> allOffers = client.readOffers(null).flatMap(f -> Flux.fromIterable(f.results())).collectList().single().block();
        List<Offer> expectedOffers = allOffers.stream().filter(o -> collectionResourceId.equals(o.getString("offerResourceId"))).collect(Collectors.toList());

        assertThat(expectedOffers).isNotEmpty();

        int expectedPageSize = (expectedOffers.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<Offer> validator = new FeedResponseListValidator.Builder<Offer>()
                .totalSize(expectedOffers.size())
                .exactlyContainsInAnyOrder(expectedOffers.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Offer>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 100)
    public void queryOffersFilterMorePages() throws Exception {
        
        List<String> collectionResourceIds = createdCollections.stream().map(c -> c.resourceId()).collect(Collectors.toList());
        String query = String.format("SELECT * from c where c.offerResourceId in (%s)", 
                Strings.join(collectionResourceIds.stream().map(s -> "'" + s + "'").collect(Collectors.toList())).with(","));

        FeedOptions options = new FeedOptions();
        options.maxItemCount(1);
        Flux<FeedResponse<Offer>> queryObservable = client.queryOffers(query, options);

        List<Offer> expectedOffers = client.readOffers(null).flatMap(f -> Flux.fromIterable(f.results()))
                .collectList()
                .single().block()
                .stream().filter(o -> collectionResourceIds.contains(o.getOfferResourceId()))
                .collect(Collectors.toList());

        assertThat(expectedOffers).hasSize(createdCollections.size());

        int expectedPageSize = (expectedOffers.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<Offer> validator = new FeedResponseListValidator.Builder<Offer>()
                .totalSize(expectedOffers.size())
                .exactlyContainsInAnyOrder(expectedOffers.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Offer>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void queryCollections_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        Flux<FeedResponse<DocumentCollection>> queryObservable = client.queryCollections(getDatabaseLink(), query, options);

        FeedResponseListValidator<DocumentCollection> validator = new FeedResponseListValidator.Builder<DocumentCollection>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<DocumentCollection>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder().build();

        Database d1 = new Database();
        d1.id(databaseId);
        createDatabase(client, d1);

        for(int i = 0; i < 3; i++) {
            DocumentCollection collection = new DocumentCollection();
            collection.id(UUID.randomUUID().toString());
            
            PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
            ArrayList<String> paths = new ArrayList<String>();
            paths.add("/mypk");
            partitionKeyDef.paths(paths);
            collection.setPartitionKey(partitionKeyDef);
            
            createdCollections.add(createCollection(client, databaseId, collection));
        }        
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, databaseId);
        safeClose(client);
    }
}
