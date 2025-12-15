// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ResourceResponseValidator;
import com.azure.cosmos.implementation.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

//TODO: change to use external TestSuiteBase
public class OfferReadReplaceTest extends TestSuiteBase {

    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public OfferReadReplaceTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readAndReplaceOffer() {

        QueryFeedOperationState offerDummyState = TestUtils.createDummyQueryFeedOperationState(
            ResourceType.Offer,
            OperationType.ReadFeed,
            new CosmosQueryRequestOptions(),
            client);

        try {
            List<Offer> offers = client
                .readOffers(offerDummyState)
                .map(FeedResponse::getResults)
                .flatMap(list -> Flux.fromIterable(list)).collectList().block();

            int i;
            for (i = 0; i < offers.size(); i++) {
                if (offers.get(i).getOfferResourceId().equals(createdCollection.getResourceId())) {
                    break;
                }
            }

            Offer rOffer = client.readOffer(offers.get(i).getSelfLink()).single().block().getResource();
            int oldThroughput = rOffer.getThroughput();

            Mono<ResourceResponse<Offer>> readObservable = client.readOffer(offers.get(i).getSelfLink());

            // validate offer read
            ResourceResponseValidator<Offer> validatorForRead = new ResourceResponseValidator.Builder<Offer>()
                .withOfferThroughput(oldThroughput)
                .notNullEtag()
                .build();

            validateSuccess(readObservable, validatorForRead);

            // update offer
            int newThroughput = oldThroughput + 100;
            offers.get(i).setThroughput(newThroughput);
            Mono<ResourceResponse<Offer>> replaceObservable = client.replaceOffer(offers.get(i));

            // validate offer replace
            ResourceResponseValidator<Offer> validatorForReplace = new ResourceResponseValidator.Builder<Offer>()
                .withOfferThroughput(newThroughput)
                .notNullEtag()
                .build();

            validateSuccess(replaceObservable, validatorForReplace);
        } finally {
            safeClose(offerDummyState);
        }
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_OfferReadReplaceTest() {
        client = clientBuilder().build();
        createdDatabase = createDatabase(client, databaseId);
        createdCollection = createCollection(client, createdDatabase.getId(),
                getCollectionDefinition());
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase);
        safeClose(client);
    }
}
