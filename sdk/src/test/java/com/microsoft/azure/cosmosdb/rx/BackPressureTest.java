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

import com.microsoft.azure.cosmos.ClientUnderTestBuilder;
import com.microsoft.azure.cosmos.CosmosBridgeInternal;
import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosClientBuilder;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosContainerRequestOptions;
import com.microsoft.azure.cosmos.CosmosContainerSettings;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentClientUnderTest;

import io.reactivex.subscribers.TestSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.util.concurrent.Queues;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class BackPressureTest extends TestSuiteBase {

    private static final int TIMEOUT = 200000;
    private static final int SETUP_TIMEOUT = 60000;

    private CosmosDatabase createdDatabase;
    private CosmosContainer createdCollection;
    private List<CosmosItemSettings> createdDocuments;

    private CosmosClient client;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    private static CosmosContainerSettings getSinglePartitionCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        return collectionDefinition;
    }

    @Factory(dataProvider = "simpleClientBuildersWithDirectHttps")
    public BackPressureTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void readFeed() throws Exception {
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(1);
        options.setEnableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.listItems(options);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest)CosmosBridgeInternal.getAsyncDocumentClient(client);
        rxClient.httpRequests.clear();

        TestSubscriber<FeedResponse<CosmosItemSettings>> subscriber = new TestSubscriber<FeedResponse<CosmosItemSettings>>(1);
        queryObservable.publishOn(Schedulers.elastic()).subscribe(subscriber);
        int sleepTimeInMillis = 10000; // 10 seconds

        int i = 0;
        // use a test subscriber and request for more result and sleep in between
        while (subscriber.completions() == 0 && subscriber.getEvents().get(1).isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(sleepTimeInMillis);
            sleepTimeInMillis /= 2;

            if (sleepTimeInMillis > 1000) {
                // validate that only one item is returned to subscriber in each iteration
                assertThat(subscriber.valueCount() - i).isEqualTo(1);
            }
            // validate that only one item is returned to subscriber in each iteration
            // validate that the difference between the number of requests to backend
            // and the number of returned results is always less than a fixed threshold
            assertThat(rxClient.httpRequests.size() - subscriber.getEvents().get(0).size())
                .isLessThanOrEqualTo(Queues.SMALL_BUFFER_SIZE);

            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertComplete();
        assertThat(subscriber.getEvents().get(0)).hasSize(createdDocuments.size());
    }

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void query() throws Exception {
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(1);
        options.setEnableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems("SELECT * from r", options);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest)CosmosBridgeInternal.getAsyncDocumentClient(client);
        rxClient.httpRequests.clear();

        TestSubscriber<FeedResponse<CosmosItemSettings>> subscriber = new TestSubscriber<FeedResponse<CosmosItemSettings>>(1);
        queryObservable.publishOn(Schedulers.elastic()).subscribe(subscriber);
        int sleepTimeInMillis = 10000;

        int i = 0;
        // use a test subscriber and request for more result and sleep in between
        while(subscriber.completions() == 0 && subscriber.getEvents().get(1).isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(sleepTimeInMillis);
            sleepTimeInMillis /= 2;

            if (sleepTimeInMillis > 1000) {
                // validate that only one item is returned to subscriber in each iteration
                assertThat(subscriber.valueCount() - i).isEqualTo(1);
            }
            // validate that the difference between the number of requests to backend
            // and the number of returned results is always less than a fixed threshold
            assertThat(rxClient.httpRequests.size() - subscriber.valueCount())
                    .isLessThanOrEqualTo(Queues.SMALL_BUFFER_SIZE);

            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertComplete();

        assertThat(subscriber.getEvents().get(0)).hasSize(createdDocuments.size());
    }

    // TODO: DANOBLE: Investigate Direct TCP performance issue
    // NOTE: This method requires multiple SHUTDOWN_TIMEOUT intervals
    // SEE: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028

    @BeforeClass(groups = { "long" }, timeOut = 2 * SETUP_TIMEOUT)
    public void beforeClass() throws Exception {

        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        options.offerThroughput(1000);
        client = new ClientUnderTestBuilder(clientBuilder).build();
        createdDatabase = getSharedCosmosDatabase(client);

        createdCollection = createCollection(createdDatabase, getSinglePartitionCollectionDefinition(), options);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest)CosmosBridgeInternal.getAsyncDocumentClient(client);

        // increase throughput to max for a single partition collection to avoid throttling
        // for bulk insert and later queries.
        Offer offer = rxClient.queryOffers(
                String.format("SELECT * FROM r WHERE r.offerResourceId = '%s'",
                        createdCollection.read().block().getCosmosContainerSettings().getResourceId())
                        , null).first().map(FeedResponse::getResults).toBlocking().single().get(0);
        offer.setThroughput(6000);
        offer = rxClient.replaceOffer(offer).toBlocking().single().getResource();
        assertThat(offer.getThroughput()).isEqualTo(6000);

        ArrayList<CosmosItemSettings> docDefList = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        createdDocuments = bulkInsertBlocking(createdCollection, docDefList);

        waitIfNeededForReplicasToCatchUp(clientBuilder);
        warmUp();
    }

    private void warmUp() {
        // ensure collection is cached
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        createdCollection.queryItems("SELECT * from r", options).blockFirst();
    }

    // TODO: DANOBLE: Investigate Direct TCP performance issue
    // NOTE: This method requires multiple SHUTDOWN_TIMEOUT intervals
    // SEE: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028

    @AfterClass(groups = { "long" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(createdCollection);
        safeClose(client);
    }

    private static CosmosItemSettings getDocumentDefinition(int cnt) {
        String uuid = UUID.randomUUID().toString();
        CosmosItemSettings doc = new CosmosItemSettings(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"prop\" : %d, "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, cnt, uuid));
        return doc;
    }
}
