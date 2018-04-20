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
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentClientUnderTest;

import rx.Observable;
import rx.internal.util.RxRingBuffer;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class BackPressureCrossPartitionTest extends TestSuiteBase {
    private final Logger log = LoggerFactory.getLogger(BackPressureCrossPartitionTest.class);

    private static final int TIMEOUT = 1800000;
    private static final int SETUP_TIMEOUT = 60000;

    private static final String DATABASE_ID = getDatabaseId(BackPressureCrossPartitionTest.class);

    private int numberOfDocs = 4000;
    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<Document> createdDocuments;

    private Builder clientBuilder;
    private RxDocumentClientUnderTest client;
    private int numberOfPartitions;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(DATABASE_ID, createdCollection.getId());
    }

    static protected DocumentCollection getCollectionDefinition() {
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

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
    }

    @Factory(dataProvider = "clientBuilders")
    public BackPressureCrossPartitionTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    private void warmUp() {
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        // ensure collection is cached
        client.queryDocuments(getCollectionLink(), "SELECT * FROM r", options).first().toBlocking().single();
    }

    @DataProvider(name = "queryProvider")
    public Object[][] queryProvider() {
        return new Object[][] {
                // query, maxItemCount, max expected back pressure buffered, total number of expected query results
                { "SELECT * FROM r", 1, 2 * RxRingBuffer.SIZE, numberOfDocs},
                { "SELECT * FROM r", 100, 2 * RxRingBuffer.SIZE, numberOfDocs},
                { "SELECT * FROM r ORDER BY r.prop", 100, 2 * RxRingBuffer.SIZE + 3 * numberOfPartitions, numberOfDocs},
                { "SELECT TOP 1000 * FROM r", 1, 2 * RxRingBuffer.SIZE, 1000},
                { "SELECT TOP 1000 * FROM r", 100, 2 * RxRingBuffer.SIZE, 1000},
                { "SELECT TOP 1000 * FROM r ORDER BY r.prop", 100, 2 * RxRingBuffer.SIZE + 3 * numberOfPartitions , 1000},
        };
    }

    @Test(groups = { "simple" }, dataProvider = "queryProvider", timeOut = TIMEOUT)
    public void query(String query, int maxItemCount, int maxExpectedBufferedCountForBackPressure, int expectedNumberOfResults) throws Exception {
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxItemCount(maxItemCount);
        options.setMaxDegreeOfParallelism(2);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        client.httpRequests.clear();

        log.info("instantiating subscriber ...");
        TestSubscriber<FeedResponse<Document>> subscriber = new TestSubscriber<>(1);
        queryObservable.observeOn(Schedulers.io(), 1).subscribe(subscriber);
        int sleepTimeInMillis = 20000;

        int i = 0;
        // use a test subscriber and request for more result and sleep in between
        while(subscriber.getCompletions() == 0 && subscriber.getOnErrorEvents().isEmpty()) {
            log.debug("loop " + i);

            TimeUnit.MILLISECONDS.sleep(sleepTimeInMillis);
            sleepTimeInMillis /= 2;

            if (sleepTimeInMillis > 1000) {
                // validate that only one item is returned to subscriber in each iteration
                assertThat(subscriber.getValueCount() - i).isEqualTo(1);
            }

            log.debug("subscriber.getValueCount(): " + subscriber.getValueCount());
            log.debug("client.httpRequests.size(): " + client.httpRequests.size());
            // validate that the difference between the number of requests to backend
            // and the number of returned results is always less than a fixed threshold

            assertThat(client.httpRequests.size() - subscriber.getValueCount())
                    .isLessThanOrEqualTo(maxExpectedBufferedCountForBackPressure);

            log.debug("requesting more");
            subscriber.requestMore(1);
            i++;
        }

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        assertThat(subscriber.getOnNextEvents().stream().mapToInt(p -> p.getResults().size()).sum()).isEqualTo(expectedNumberOfResults);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = new ClientUnderTestBuilder(clientBuilder).build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(20000);
        createdCollection = safeCreateCollection(client, createdDatabase.getId(), getCollectionDefinition(), options);

        ArrayList<Document> docDefList = new ArrayList<>();
        for(int i = 0; i < numberOfDocs; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        Observable<ResourceResponse<Document>> documentBulkInsertObs = bulkInsert(
                client,
                getCollectionLink(),
                docDefList,
                1000);

        createdDocuments = documentBulkInsertObs.map(ResourceResponse::getResource).toList().toBlocking().single();

        numberOfPartitions = client.readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Observable.from(p.getResults())).toList().toBlocking().single().size();

        warmUp();
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
