// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ClientUnderTestBuilder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.RxDocumentClientUnderTest;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.util.CosmosPagedFlux;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class BackPressureTest extends TestSuiteBase {

    private static final int TIMEOUT = 200000;
    private static final int SETUP_TIMEOUT = 60000;

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private List<InternalObjectNode> createdDocuments;

    private CosmosAsyncClient client;

    public String getCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    private static CosmosContainerProperties getSinglePartitionCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        return collectionDefinition;
    }

    // RxDocumentClientUnderTest spy used in this test only has support for GW request capturing
    // So only running the test against GW is relevant
    @Factory(dataProvider = "simpleClientBuilderGatewaySession")
    public BackPressureTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "long" }, timeOut = 3 * TIMEOUT)
    public void readFeedPages() throws Exception {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection
            .queryItems("SELECT * FROM r", options, InternalObjectNode.class);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest) CosmosBridgeInternal.getAsyncDocumentClient(client);
        AtomicInteger valueCount = new AtomicInteger();
        rxClient.httpRequests.clear();

        TestSubscriber<FeedResponse<InternalObjectNode>> subscriber = new TestSubscriber<FeedResponse<InternalObjectNode>>(1);
        queryObservable.byPage(1).doOnNext(feedResponse -> {
            if (!feedResponse.getResults().isEmpty()) {
                valueCount.incrementAndGet();
            }
        }).publishOn(Schedulers.elastic(), 1).subscribe(subscriber);

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
            assertThat(rxClient.httpRequests.size() - subscriber.valueCount())
                .isLessThanOrEqualTo(Queues.SMALL_BUFFER_SIZE);

            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertComplete();
        assertThat(valueCount.get()).isEqualTo(createdDocuments.size());
    }

    @Test(groups = { "long" }, timeOut = 3 * TIMEOUT)
    public void readFeedItems() throws Exception {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection
            .queryItems("SELECT * FROM r", options, InternalObjectNode.class);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest) CosmosBridgeInternal.getAsyncDocumentClient(client);
        AtomicInteger valueCount = new AtomicInteger();
        rxClient.httpRequests.clear();

        TestSubscriber<InternalObjectNode> subscriber = new TestSubscriber<>(1);
        queryObservable.doOnNext(feedResponse -> {
            valueCount.incrementAndGet();
        }).publishOn(Schedulers.elastic(), 1).subscribe(subscriber);

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
            assertThat(rxClient.httpRequests.size() - subscriber.valueCount())
                .isLessThanOrEqualTo(Queues.SMALL_BUFFER_SIZE);

            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertComplete();
        assertThat(valueCount.get()).isEqualTo(createdDocuments.size());
    }

    @Test(groups = { "long" }, timeOut = 3 * TIMEOUT)
    public void queryPages() throws Exception {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems("SELECT * from r", options, InternalObjectNode.class);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest)CosmosBridgeInternal.getAsyncDocumentClient(client);
        rxClient.httpRequests.clear();

        TestSubscriber<FeedResponse<InternalObjectNode>> subscriber = new TestSubscriber<FeedResponse<InternalObjectNode>>(1);
        AtomicInteger valueCount = new AtomicInteger();

        queryObservable.byPage(1).doOnNext(feedResponse -> {
            if (!feedResponse.getResults().isEmpty()) {
                valueCount.incrementAndGet();
            }
        }).publishOn(Schedulers.elastic(), 1).subscribe(subscriber);

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

        assertThat(valueCount.get()).isEqualTo(createdDocuments.size());
    }

    @Test(groups = { "long" }, timeOut = 3 * TIMEOUT)
    public void queryItems() throws Exception {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems("SELECT * from r", options, InternalObjectNode.class);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest)CosmosBridgeInternal.getAsyncDocumentClient(client);
        rxClient.httpRequests.clear();

        TestSubscriber<InternalObjectNode> subscriber = new TestSubscriber<>(1);
        AtomicInteger valueCount = new AtomicInteger();

        queryObservable.doOnNext(internalObjectNode -> {
            valueCount.incrementAndGet();
        }).publishOn(Schedulers.elastic(), 1).subscribe(subscriber);

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

        logger.debug("final value count {}", valueCount);
        assertThat(valueCount.get()).isEqualTo(createdDocuments.size());
    }

    @BeforeClass(groups = { "long" }, timeOut = 2 * SETUP_TIMEOUT)
    public void before_BackPressureTest() throws Exception {

        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        client = new ClientUnderTestBuilder(getClientBuilder()).buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);

        createdCollection = createCollection(createdDatabase, getSinglePartitionCollectionDefinition(), options, 1000);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest)CosmosBridgeInternal.getAsyncDocumentClient(client);

        // increase throughput to max for a single partition collection to avoid throttling
        // for bulk insert and later queries.
        Offer offer = rxClient.queryOffers(
                String.format("SELECT * FROM r WHERE r.offerResourceId = '%s'",
                    createdCollection.read().block().getProperties().getResourceId())
                        , null).take(1).map(FeedResponse::getResults).single().block().get(0);
        offer.setThroughput(6000);
        offer = rxClient.replaceOffer(offer).block().getResource();
        assertThat(offer.getThroughput()).isEqualTo(6000);

        ArrayList<InternalObjectNode> docDefList = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        createdDocuments = bulkInsertBlocking(createdCollection, docDefList);

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        warmUp();
    }

    private void warmUp() {
        // ensure collection is cached
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        createdCollection.queryItems("SELECT * from r", options, InternalObjectNode.class).byPage().blockFirst();
    }

    @AfterClass(groups = { "long" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(createdCollection);
        safeClose(client);
    }

    private static InternalObjectNode getDocumentDefinition(int cnt) {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"prop\" : %d, "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, cnt, uuid));
        return doc;
    }
}
