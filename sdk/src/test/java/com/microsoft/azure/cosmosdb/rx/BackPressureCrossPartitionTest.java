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
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentClientUnderTest;

import io.reactivex.subscribers.TestSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.util.concurrent.Queues;
import rx.Observable;

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
    private List<CosmosItemSettings> createdDocuments;

    private CosmosClient client;
    private int numberOfPartitions;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    static protected CosmosContainerSettings getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.String);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.Number);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(
                UUID.randomUUID().toString(),
                partitionKeyDef);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
    }

    @Factory(dataProvider = "simpleClientBuildersWithDirectHttps")
    public BackPressureCrossPartitionTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    private void warmUp() {
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
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

    // TODO: DANOBLE: Investigate Direct TCP performance issue
    // Links: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028

    @Test(groups = { "long" }, dataProvider = "queryProvider", timeOut = 2 * TIMEOUT)
    public void query(String query, int maxItemCount, int maxExpectedBufferedCountForBackPressure, int expectedNumberOfResults) throws Exception {
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxItemCount(maxItemCount);
        options.setMaxDegreeOfParallelism(2);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

        RxDocumentClientUnderTest rxClient = (RxDocumentClientUnderTest)CosmosBridgeInternal.getAsyncDocumentClient(client);
        rxClient.httpRequests.clear();

        log.info("instantiating subscriber ...");
        TestSubscriber<FeedResponse<CosmosItemSettings>> subscriber = new TestSubscriber<>(1);
        queryObservable.publishOn(Schedulers.elastic()).subscribe(subscriber);
        int sleepTimeInMillis = 40000;
        int i = 0;

        // use a test subscriber and request for more result and sleep in between
        try {
            while(subscriber.completions() == 0 && subscriber.errorCount() == 0) {
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
        } catch (Throwable error) {
            if (this.clientBuilder.getConfigs().getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.getDesiredConsistencyLevel());
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }

        try {
            subscriber.assertNoErrors();
            subscriber.assertComplete();
            assertThat(subscriber.values().stream().mapToInt(p -> p.getResults().size()).sum()).isEqualTo(expectedNumberOfResults);
        } catch (Throwable error) {
            if (this.clientBuilder.getConfigs().getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.getDesiredConsistencyLevel());
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @BeforeClass(groups = { "long" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        options.offerThroughput(20000);
        client = new ClientUnderTestBuilder(clientBuilder).build();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = createCollection(createdDatabase, getCollectionDefinition(), options);

        ArrayList<CosmosItemSettings> docDefList = new ArrayList<>();
        for(int i = 0; i < numberOfDocs; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        createdDocuments = bulkInsertBlocking(
                createdCollection,
                docDefList);

        numberOfPartitions = CosmosBridgeInternal.getAsyncDocumentClient(client).readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Observable.from(p.getResults())).toList().toBlocking().single().size();

        waitIfNeededForReplicasToCatchUp(clientBuilder);
        warmUp();
    }

    // TODO: DANOBLE: Investigate Direct TCP performance issue
    // Links: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028

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
