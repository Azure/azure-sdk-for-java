// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.*;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.internal.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

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

        client.readOffers(null).subscribe((offersFeed) -> {
            try {
                int i;
                List<Offer> offers = offersFeed.results();
                for (i = 0; i < offers.size(); i++) {
                    if (offers.get(i).getOfferResourceId().equals(createdCollection.resourceId())) {
                        break;
                    }
                }

                Offer rOffer = client.readOffer(offers.get(i).selfLink()).single().block().getResource();
                int oldThroughput = rOffer.getThroughput();
                
                Flux<ResourceResponse<Offer>> readObservable = client.readOffer(offers.get(i).selfLink());

                // validate offer read
                ResourceResponseValidator<Offer> validatorForRead = new ResourceResponseValidator.Builder<Offer>()
                        .withOfferThroughput(oldThroughput)
                        .notNullEtag()
                        .build();

                validateSuccess(readObservable, validatorForRead);

                // update offer
                int newThroughput = oldThroughput + 100;
                offers.get(i).setThroughput(newThroughput);
                Flux<ResourceResponse<Offer>> replaceObservable = client.replaceOffer(offers.get(i));

                // validate offer replace
                ResourceResponseValidator<Offer> validatorForReplace = new ResourceResponseValidator.Builder<Offer>()
                        .withOfferThroughput(newThroughput)
                        .notNullEtag()
                        .build();
                
                validateSuccess(replaceObservable, validatorForReplace);
                
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdDatabase = createDatabase(client, databaseId);
        createdCollection = createCollection(client, createdDatabase.id(),
                getCollectionDefinition());
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase);
        safeClose(client);
    }
}
