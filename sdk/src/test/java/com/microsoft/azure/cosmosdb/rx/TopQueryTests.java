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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import com.microsoft.azure.cosmosdb.RetryAnalyzer;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;
import com.microsoft.azure.cosmosdb.rx.internal.query.TakeContinuationToken;

import io.reactivex.subscribers.TestSubscriber;
import reactor.core.publisher.Flux;

public class TopQueryTests extends TestSuiteBase {
    private CosmosContainer createdCollection;
    private ArrayList<CosmosItemSettings> docs = new ArrayList<CosmosItemSettings>();

    private String partitionKey = "mypk";
    private int firstPk = 0;
    private int secondPk = 1;
    private String field = "field";

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public TopQueryTests(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void queryDocumentsWithTop(boolean qmEnabled) throws Exception {

        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxItemCount(9);
        options.setMaxDegreeOfParallelism(2);
        options.setPopulateQueryMetrics(qmEnabled);

        int expectedTotalSize = 20;
        int expectedNumberOfPages = 3;
        int[] expectedPageLengths = new int[] { 9, 9, 2 };

        for (int i = 0; i < 2; i++) {
            Flux<FeedResponse<CosmosItemSettings>> queryObservable1 = createdCollection.queryItems("SELECT TOP 0 value AVG(c.field) from c", options);

            FeedResponseListValidator<CosmosItemSettings> validator1 = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                    .totalSize(0).build();

            try {
                validateQuerySuccess(queryObservable1, validator1, TIMEOUT);
            } catch (Throwable error) {
                if (this.clientBuilder.getConfigs().getProtocol() == Protocol.Tcp) {
                    String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s",
                            this.clientBuilder.getDesiredConsistencyLevel());
                    logger.info(message, error);
                    throw new SkipException(message, error);
                }
                throw error;
            }

            Flux<FeedResponse<CosmosItemSettings>> queryObservable2 = createdCollection.queryItems("SELECT TOP 1 value AVG(c.field) from c", options);

            FeedResponseListValidator<CosmosItemSettings> validator2 = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                    .totalSize(1).build();

            validateQuerySuccess(queryObservable2, validator2, TIMEOUT);

            Flux<FeedResponse<CosmosItemSettings>> queryObservable3 = createdCollection.queryItems("SELECT TOP 20 * from c", options);

            FeedResponseListValidator<CosmosItemSettings> validator3 = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                    .totalSize(expectedTotalSize).numberOfPages(expectedNumberOfPages).pageLengths(expectedPageLengths)
                    .hasValidQueryMetrics(qmEnabled).build();

            validateQuerySuccess(queryObservable3, validator3, TIMEOUT);

            if (i == 0) {
                options.setPartitionKey(new PartitionKey(firstPk));
                options.setEnableCrossPartitionQuery(false);

                expectedTotalSize = 10;
                expectedNumberOfPages = 2;
                expectedPageLengths = new int[] { 9, 1 };

            }
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void topContinuationTokenRoundTrips() throws Exception {
        {
            // Positive
            TakeContinuationToken takeContinuationToken = new TakeContinuationToken(42, "asdf");
            String serialized = takeContinuationToken.toString();
            ValueHolder<TakeContinuationToken> outTakeContinuationToken = new ValueHolder<TakeContinuationToken>();

            assertThat(TakeContinuationToken.tryParse(serialized, outTakeContinuationToken)).isTrue();
            TakeContinuationToken deserialized = outTakeContinuationToken.v;

            assertThat(deserialized.getTakeCount()).isEqualTo(42);
            assertThat(deserialized.getSourceToken()).isEqualTo("asdf");
        }

        {
            // Negative
            ValueHolder<TakeContinuationToken> outTakeContinuationToken = new ValueHolder<TakeContinuationToken>();
            assertThat(
                    TakeContinuationToken.tryParse("{\"property\": \"Not a valid token\"}", outTakeContinuationToken))
                            .isFalse();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 10, retryAnalyzer = RetryAnalyzer.class)
    public void queryDocumentsWithTopContinuationTokens() throws Exception {
        String query = "SELECT TOP 8 * FROM c";
        this.queryWithContinuationTokensAndPageSizes(query, new int[] { 1, 5, 10 }, 8);
    }

    private void queryWithContinuationTokensAndPageSizes(String query, int[] pageSizes, int topCount) {
        for (int pageSize : pageSizes) {
            List<CosmosItemSettings> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            Set<String> actualIds = new HashSet<String>();
            for (CosmosItemSettings document : receivedDocuments) {
                actualIds.add(document.getResourceId());
            }

            assertThat(actualIds.size()).describedAs("total number of results").isEqualTo(topCount);
        }
    }

    private List<CosmosItemSettings> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<CosmosItemSettings> receivedDocuments = new ArrayList<CosmosItemSettings>();

        do {
            FeedOptions options = new FeedOptions();
            options.setMaxItemCount(pageSize);
            options.setEnableCrossPartitionQuery(true);
            options.setMaxDegreeOfParallelism(2);
            options.setRequestContinuation(requestContinuation);
            Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.first();
            TestSubscriber<FeedResponse<CosmosItemSettings>> testSubscriber = new TestSubscriber<>();
            queryObservable.subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            FeedResponse<CosmosItemSettings> firstPage = (FeedResponse<CosmosItemSettings>) testSubscriber.getEvents().get(0).get(0);
            requestContinuation = firstPage.getResponseContinuation();
            receivedDocuments.addAll(firstPage.getResults());
            continuationTokens.add(requestContinuation);
        } while (requestContinuation != null);

        return receivedDocuments;
    }

    public void bulkInsert(CosmosClient client) {
        generateTestData();

        for (int i = 0; i < docs.size(); i++) {
            createDocument(createdCollection, docs.get(i));
        }
    }

    public void generateTestData() {

        for (int i = 0; i < 10; i++) {
            CosmosItemSettings d = new CosmosItemSettings();
            d.setId(Integer.toString(i));
            d.set(field, i);
            d.set(partitionKey, firstPk);
            docs.add(d);
        }

        for (int i = 10; i < 20; i++) {
            CosmosItemSettings d = new CosmosItemSettings();
            d.setId(Integer.toString(i));
            d.set(field, i);
            d.set(partitionKey, secondPk);
            docs.add(d);
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        createdCollection = getSharedSinglePartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        bulkInsert(client);

        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }
}
