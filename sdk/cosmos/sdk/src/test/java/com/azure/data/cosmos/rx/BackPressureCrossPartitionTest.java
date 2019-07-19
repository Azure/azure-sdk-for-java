// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ClientUnderTestBuilder;
import com.azure.data.cosmos.CosmosBridgeInternal;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.DataType;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.IncludedPath;
import com.azure.data.cosmos.Index;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.RxDocumentClientUnderTest;
import com.azure.data.cosmos.internal.TestUtils;
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
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class BackPressureCrossPartitionTest extends TestSuiteBase {
    private final Logger log = LoggerFactory.getLogger(BackPressureCrossPartitionTest.class);

    private static final int TIMEOUT = 1800000;
    private static final int SETUP_TIMEOUT = 60000;

    private int numberOfDocs = 4000;
    private CosmosDatabase createdDatabase;
    private CosmosContainer createdCollection;
    private List<CosmosItemProperties> createdDocuments;

    private CosmosClient client;
    private int numberOfPartitions;

    public String getCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.id(), createdCollection.id());
    }

    static protected CosmosContainerProperties getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.path("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.STRING);
        BridgeInternal.setProperty(stringIndex, "precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        BridgeInternal.setProperty(numberIndex, "precision", -1);
        indexes.add(numberIndex);
        includedPath.indexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
                UUID.randomUUID().toString(),
                partitionKeyDef);
        collectionDefinition.indexingPolicy(indexingPolicy);

        return collectionDefinition;
    }

    @Factory(dataProvider = "simpleClientBuildersWithDirectHttps")
    public BackPressureCrossPartitionTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    private void warmUp() {
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        // ensure collection is cached
        createdCollection.queryItems("SELECT * FROM r", options).blockFirst();
    }

    @DataProvider(name = "queryProvider")
    public Object[][] queryProvider() {
        return new Object[][] {
                // query, maxItemCount, max expected back pressure buffered, total number of expected query results
                { "SELECT * FROM r", 1, 2 * Queues.SMALL_BUFFER_SIZE, numberOfDocs},
                { "SELECT * FROM r", 100, 2 * Queues.SMALL_BUFFER_SIZE, numberOfDocs},
                { "SELECT * FROM r ORDER BY r.prop", 100, 2 * Queues.SMALL_BUFFER_SIZE + 3 * numberOfPartitions, numberOfDocs},
                { "SELECT TOP 1000 * FROM r", 1, 2 * Queues.SMALL_BUFFER_SIZE, 1000},
                { "SELECT TOP 1000 * FROM r", 100, 2 * Queues.SMALL_BUFFER_SIZE, 1000},
                { "SELECT TOP 1000 * FROM r ORDER BY r.prop", 100, 2 * Queues.SMALL_BUFFER_SIZE + 3 * numberOfPartitions , 1000},
        };
    }

    // TODO: DANOBLE: Investigate DIRECT TCP performance issue
    // Links: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028

    @Test(groups = { "long" }, dataProvider = "queryProvider", timeOut = 2 * TIMEOUT)
    public void query(String query, int maxItemCount, int maxExpectedBufferedCountForBackPressure, int expectedNumberOfResults) throws Exception {
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.maxItemCount(maxItemCount);
        options.maxDegreeOfParallelism(2);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest)CosmosBridgeInternal.getAsyncDocumentClient(client);
        rxClient.httpRequests.clear();

        log.info("instantiating subscriber ...");
        TestSubscriber<FeedResponse<CosmosItemProperties>> subscriber = new TestSubscriber<>(1);
        queryObservable.publishOn(Schedulers.elastic(), 1).subscribe(subscriber);
        int sleepTimeInMillis = 40000;
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
        assertThat(subscriber.values().stream().mapToInt(p -> p.results().size()).sum()).isEqualTo(expectedNumberOfResults);
    }

    @BeforeClass(groups = { "long" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        client = new ClientUnderTestBuilder(clientBuilder()).build();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = createCollection(createdDatabase, getCollectionDefinition(), options, 20000);

        ArrayList<CosmosItemProperties> docDefList = new ArrayList<>();
        for(int i = 0; i < numberOfDocs; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        createdDocuments = bulkInsertBlocking(
                createdCollection,
                docDefList);

        numberOfPartitions = CosmosBridgeInternal.getAsyncDocumentClient(client).readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Flux.fromIterable(p.results())).collectList().single().block().size();

        waitIfNeededForReplicasToCatchUp(clientBuilder());
        warmUp();
    }

    // TODO: DANOBLE: Investigate DIRECT TCP performance issue
    // Links: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028

    @AfterClass(groups = { "long" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(createdCollection);
        safeClose(client);
    }

    private static CosmosItemProperties getDocumentDefinition(int cnt) {
        String uuid = UUID.randomUUID().toString();
        CosmosItemProperties doc = new CosmosItemProperties(String.format("{ "
            + "\"id\": \"%s\", "
            + "\"prop\" : %d, "
            + "\"mypk\": \"%s\", "
            + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
            + "}"
            , uuid, cnt, uuid));
        return doc;
    }
}
