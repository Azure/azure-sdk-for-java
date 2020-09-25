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
import com.azure.cosmos.implementation.RxDocumentClientUnderTest;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedFlux;
import io.reactivex.subscribers.TestSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class BackPressureCrossPartitionTest extends TestSuiteBase {
    private final Logger log = LoggerFactory.getLogger(BackPressureCrossPartitionTest.class);

    private static final int TIMEOUT = 1800000;
    private static final int SETUP_TIMEOUT = 60000;

    private int numberOfDocs = 1000;
    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private List<InternalObjectNode> createdDocuments;

    private CosmosAsyncClient client;
    private int numberOfPartitions;

    public String getCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    static protected CosmosContainerProperties getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath("/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
                UUID.randomUUID().toString(),
                partitionKeyDef);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
    }

    // RxDocumentClientUnderTest spy used in this test only has support for GW request capturing
    // So only running the test against GW is relevant
    @Factory(dataProvider = "simpleClientBuilderGatewaySession")
    public BackPressureCrossPartitionTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    private void warmUp() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        // ensure collection is cached
        createdCollection.queryItems("SELECT * FROM r", options, InternalObjectNode.class).byPage().blockFirst();
    }

    @DataProvider(name = "queryProvider")
    public Object[][] queryProvider() {
        return new Object[][] {
                // query, maxItemCount, max expected back pressure buffered, total number of expected query results
            { "SELECT * FROM r", 1, Queues.SMALL_BUFFER_SIZE, numberOfDocs},
            { "SELECT * FROM r", 100, Queues.SMALL_BUFFER_SIZE, numberOfDocs},
            { "SELECT * FROM r ORDER BY r.prop", 100, Queues.SMALL_BUFFER_SIZE + 3 * numberOfPartitions, numberOfDocs},
            { "SELECT TOP 500 * FROM r", 1, Queues.SMALL_BUFFER_SIZE, 500},
            { "SELECT TOP 500 * FROM r", 100, Queues.SMALL_BUFFER_SIZE, 500},
            { "SELECT TOP 500 * FROM r ORDER BY r.prop", 100, Queues.SMALL_BUFFER_SIZE + 3 * numberOfPartitions , 500},
        };
    }

    @Test(groups = { "long" }, dataProvider = "queryProvider", timeOut = 2 * TIMEOUT)
    public void queryPages(String query, int maxItemCount, int maxExpectedBufferedCountForBackPressure, int expectedNumberOfResults) throws Exception {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest) CosmosBridgeInternal.getAsyncDocumentClient(client);
        rxClient.httpRequests.clear();

        log.info("instantiating subscriber ...");
        TestSubscriber<FeedResponse<InternalObjectNode>> subscriber = new TestSubscriber<>(1);
        queryObservable.byPage(maxItemCount).publishOn(Schedulers.elastic(), 1).subscribe(subscriber);
        int sleepTimeInMillis = 10000;
        int i = 0;

        // use a test subscriber and request for more result and sleep in between
        while (subscriber.completions() == 0 && subscriber.errorCount() == 0) {
            log.debug("loop " + i);

            TimeUnit.MILLISECONDS.sleep(sleepTimeInMillis);
            sleepTimeInMillis /= 2;

            if (sleepTimeInMillis > 4000) {
                // validate that only one item is returned to subscriber in each iteration
                assertThat(subscriber.valueCount() - i).isEqualTo(1);
            }

            log.debug("subscriber.getValueCount(): " + subscriber.valueCount());
            log.debug("client.httpRequests.size(): " + rxClient.httpRequests.size());
            // validate that the difference between the number of requests to backend
            // and the number of returned results is always less than a fixed threshold
            assertThat(rxClient.httpRequests.size() - subscriber.valueCount())
                .isLessThanOrEqualTo(maxExpectedBufferedCountForBackPressure);

            log.debug("requesting more");
            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertComplete();
        assertThat(subscriber.values().stream().mapToInt(p -> p.getResults().size()).sum()).isEqualTo(expectedNumberOfResults);
    }

    @Test(groups = { "long" }, dataProvider = "queryProvider", timeOut = 2 * TIMEOUT)
    public void queryItems(String query, int maxItemCount, int maxExpectedBufferedCountForBackPressure, int expectedNumberOfResults) throws Exception {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest) CosmosBridgeInternal.getAsyncDocumentClient(client);
        rxClient.httpRequests.clear();

        log.info("instantiating subscriber ...");
        TestSubscriber<InternalObjectNode> subscriber = new TestSubscriber<>(1);
        queryObservable.publishOn(Schedulers.elastic(), 1).subscribe(subscriber);
        int sleepTimeInMillis = 10000;
        int i = 0;

        // use a test subscriber and request for more result and sleep in between
        while (subscriber.completions() == 0 && subscriber.errorCount() == 0) {
            log.debug("loop " + i);

            TimeUnit.MILLISECONDS.sleep(sleepTimeInMillis);
            sleepTimeInMillis /= 2;

            if (sleepTimeInMillis > 4000) {
                // validate that only one item is returned to subscriber in each iteration
                assertThat(subscriber.valueCount() - i).isEqualTo(1);
            }

            log.debug("subscriber.getValueCount(): " + subscriber.valueCount());
            log.debug("client.httpRequests.size(): " + rxClient.httpRequests.size());
            // validate that the difference between the number of requests to backend
            // and the number of returned results is always less than a fixed threshold
            assertThat(rxClient.httpRequests.size() - subscriber.valueCount())
                .isLessThanOrEqualTo(maxExpectedBufferedCountForBackPressure);

            log.debug("requesting more");
            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertComplete();
        assertThat(Integer.valueOf(subscriber.values().size())).isEqualTo(expectedNumberOfResults);
    }

    @BeforeClass(groups = { "long" }, timeOut = SETUP_TIMEOUT)
    public void before_BackPressureCrossPartitionTest() {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        client = new ClientUnderTestBuilder(getClientBuilder()).buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = createCollection(createdDatabase, getCollectionDefinition(), options, 20000);

        ArrayList<InternalObjectNode> docDefList = new ArrayList<>();
        for(int i = 0; i < numberOfDocs; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        createdDocuments = bulkInsertBlocking(
                createdCollection,
                docDefList);

        numberOfPartitions = CosmosBridgeInternal.getAsyncDocumentClient(client).readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Flux.fromIterable(p.getResults())).collectList().single().block().size();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        warmUp();
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
