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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentClientUnderTest;

import rx.Observable;
import rx.internal.util.RxRingBuffer;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class BackPressureTest extends TestSuiteBase {

    private static final int TIMEOUT = 120000;
    private static final int SETUP_TIMEOUT = 60000;

    private static final String DATABASE_ID = getDatabaseId(BackPressureTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<Document> createdDocuments;

    private Builder clientBuilder;
    private RxDocumentClientUnderTest client;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(DATABASE_ID, createdCollection.getId());
    }

    static protected DocumentCollection getSinglePartitionCollectionDefinition() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        return collectionDefinition;
    }

    @Factory(dataProvider = "clientBuilders")
    public BackPressureTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readFeed() throws Exception {
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(1);
        Observable<FeedResponse<Document>> queryObservable = client
                .readDocuments(getCollectionLink(), options);

        client.httpRequests.clear();

        TestSubscriber subscriber = new TestSubscriber(1);
        queryObservable.observeOn(Schedulers.io(), 1).subscribe(subscriber);
        int sleepTimeInMillis = 10000; // 10 seconds

        int i = 0;
        // use a test subscriber and request for more result and sleep in between
        while(subscriber.getCompletions() == 0 && subscriber.getOnErrorEvents().isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(sleepTimeInMillis);
            sleepTimeInMillis /= 2;

            if (sleepTimeInMillis > 1000) {
                // validate that only one item is returned to subscriber in each iteration
                assertThat(subscriber.getValueCount() - i).isEqualTo(1);
            }
            // validate that only one item is returned to subscriber in each iteration
            // validate that the difference between the number of requests to backend
            // and the number of returned results is always less than a fixed threshold
            assertThat(client.httpRequests.size() - subscriber.getOnNextEvents().size())
                    .isLessThanOrEqualTo(RxRingBuffer.SIZE);

            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertCompleted();

        assertThat(subscriber.getOnNextEvents()).hasSize(createdDocuments.size());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void query() throws Exception {
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(1);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), "SELECT * from r", options);

        client.httpRequests.clear();

        TestSubscriber subscriber = new TestSubscriber(1);
        queryObservable.observeOn(Schedulers.io(), 1).subscribe(subscriber);
        int sleepTimeInMillis = 10000;

        int i = 0;
        // use a test subscriber and request for more result and sleep in between
        while(subscriber.getCompletions() == 0 && subscriber.getOnErrorEvents().isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(sleepTimeInMillis);
            sleepTimeInMillis /= 2;

            if (sleepTimeInMillis > 1000) {
                // validate that only one item is returned to subscriber in each iteration
                assertThat(subscriber.getValueCount() - i).isEqualTo(1);
            }
            // validate that the difference between the number of requests to backend
            // and the number of returned results is always less than a fixed threshold
            assertThat(client.httpRequests.size() - subscriber.getValueCount())
                    .isLessThanOrEqualTo(RxRingBuffer.SIZE);

            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertCompleted();

        assertThat(subscriber.getOnNextEvents()).hasSize(createdDocuments.size());
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = new ClientUnderTestBuilder(clientBuilder).build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(1000);
        createdCollection = safeCreateCollection(client, createdDatabase.getId(), getSinglePartitionCollectionDefinition(), options);

        // increase throughput to max for a single partition collection to avoid throttling
        // for bulk insert and later queries.
        Offer offer = client.queryOffers(
                String.format("SELECT * FROM r WHERE r.offerResourceId = '%s'",
                        createdCollection.getResourceId())
                        , null).first().map(FeedResponse::getResults).toBlocking().single().get(0);
        offer.getContent().put("offerThroughput", 10000);
        offer = client.replaceOffer(offer).toBlocking().single().getResource();
        assertThat(offer.getContent().get("offerThroughput")).isEqualTo(10000);

        ArrayList<Document> docDefList = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        Observable<ResourceResponse<Document>> documentBulkInsertObs = bulkInsert(
                client,
                getCollectionLink(),
                docDefList,
                200);

        createdDocuments = documentBulkInsertObs.map(ResourceResponse::getResource).toList().toBlocking().single();

        warmUp();
    }

    private void warmUp() {
        // ensure collection is cached
        client.queryDocuments(getCollectionLink(), "SELECT * from r", null).first().toBlocking().single();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private static Document getDocumentDefinition(int cnt) {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"prop\" : %d, "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, cnt, uuid));
        return doc;
    }
}
