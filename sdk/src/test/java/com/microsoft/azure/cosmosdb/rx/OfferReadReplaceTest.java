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

import java.util.List;

import com.microsoft.azure.cosmosdb.DatabaseForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.ResourceResponse;

import rx.Observable;

import javax.net.ssl.SSLException;

public class OfferReadReplaceTest extends TestSuiteBase {

    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public OfferReadReplaceTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readAndReplaceOffer() {

        client.readOffers(null).toBlocking().subscribe((offersFeed) -> {
            try {
                int i;
                List<Offer> offers = offersFeed.getResults();
                for (i = 0; i < offers.size(); i++) {
                    if (offers.get(i).getOfferResourceId().equals(createdCollection.getResourceId())) {
                        break;
                    }
                }

                Offer rOffer = client.readOffer(offers.get(i).getSelfLink()).toBlocking().single().getResource();
                int oldThroughput = rOffer.getThroughput();
                
                Observable<ResourceResponse<Offer>> readObservable = client.readOffer(offers.get(i).getSelfLink());

                // validate offer read
                ResourceResponseValidator<Offer> validatorForRead = new ResourceResponseValidator.Builder<Offer>()
                        .withOfferThroughput(oldThroughput)
                        .notNullEtag()
                        .build();

                validateSuccess(readObservable, validatorForRead);

                // update offer
                int newThroughput = oldThroughput + 100;
                offers.get(i).setThroughput(newThroughput);
                Observable<ResourceResponse<Offer>> replaceObservable = client.replaceOffer(offers.get(i));

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
        client = clientBuilder.build();
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
