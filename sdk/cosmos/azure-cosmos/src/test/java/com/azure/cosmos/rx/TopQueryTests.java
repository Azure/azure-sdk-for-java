// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.internal.FeedResponseListValidator;
import com.azure.cosmos.internal.RetryAnalyzer;
import com.azure.cosmos.internal.Utils.ValueHolder;
import com.azure.cosmos.internal.query.TakeContinuationToken;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class TopQueryTests extends TestSuiteBase {
    private CosmosAsyncContainer createdCollection;
    private ArrayList<CosmosItemProperties> docs = new ArrayList<CosmosItemProperties>();

    private String partitionKey = "mypk";
    private int firstPk = 0;
    private int secondPk = 1;
    private String field = "field";

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public TopQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    //FIXME: line 82 Assertion error. Error message "Expecting <0> to be greater than <0>" : FastIntegrationTests (Builds: SingeRegionSessionTcp)
    @Ignore
    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void queryDocumentsWithTop(boolean qmEnabled) throws Exception {

        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.maxItemCount(9);
        options.setMaxDegreeOfParallelism(2);
        options.populateQueryMetrics(qmEnabled);

        int expectedTotalSize = 20;
        int expectedNumberOfPages = 3;
        int[] expectedPageLengths = new int[] { 9, 9, 2 };

        for (int i = 0; i < 2; i++) {
            Flux<FeedResponse<CosmosItemProperties>> queryObservable1 = createdCollection.queryItems("SELECT TOP 0 value AVG(c.field) from c", options);

            FeedResponseListValidator<CosmosItemProperties> validator1 = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                    .totalSize(0).build();

            validateQuerySuccess(queryObservable1, validator1, TIMEOUT);

            Flux<FeedResponse<CosmosItemProperties>> queryObservable2 = createdCollection.queryItems("SELECT TOP 1 value AVG(c.field) from c", options);

            FeedResponseListValidator<CosmosItemProperties> validator2 = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                    .totalSize(1).build();

            validateQuerySuccess(queryObservable2, validator2, TIMEOUT);

            Flux<FeedResponse<CosmosItemProperties>> queryObservable3 = createdCollection.queryItems("SELECT TOP 20 * from c", options);

            FeedResponseListValidator<CosmosItemProperties> validator3 = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                    .totalSize(expectedTotalSize).numberOfPages(expectedNumberOfPages).pageLengths(expectedPageLengths)
                    .hasValidQueryMetrics(qmEnabled).build();

            validateQuerySuccess(queryObservable3, validator3, TIMEOUT);

            if (i == 0) {
                options.partitionKey(new PartitionKey(firstPk));
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
            List<CosmosItemProperties> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            Set<String> actualIds = new HashSet<String>();
            for (CosmosItemProperties document : receivedDocuments) {
                actualIds.add(document.getResourceId());
            }

            assertThat(actualIds.size()).describedAs("total number of results").isEqualTo(topCount);
        }
    }

    private List<CosmosItemProperties> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<CosmosItemProperties> receivedDocuments = new ArrayList<CosmosItemProperties>();

        do {
            FeedOptions options = new FeedOptions();
            options.maxItemCount(pageSize);
            options.setEnableCrossPartitionQuery(true);
            options.setMaxDegreeOfParallelism(2);
            options.requestContinuation(requestContinuation);
            Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.first();
            TestSubscriber<FeedResponse<CosmosItemProperties>> testSubscriber = new TestSubscriber<>();
            queryObservable.subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            FeedResponse<CosmosItemProperties> firstPage = (FeedResponse<CosmosItemProperties>) testSubscriber.getEvents().get(0).get(0);
            requestContinuation = firstPage.getContinuationToken();
            receivedDocuments.addAll(firstPage.getResults());
            continuationTokens.add(requestContinuation);
        } while (requestContinuation != null);

        return receivedDocuments;
    }

    public void bulkInsert(CosmosAsyncClient client) {
        generateTestData();

        for (int i = 0; i < docs.size(); i++) {
            createDocument(createdCollection, docs.get(i));
        }
    }

    public void generateTestData() {

        for (int i = 0; i < 10; i++) {
            CosmosItemProperties d = new CosmosItemProperties();
            d.setId(Integer.toString(i));
            BridgeInternal.setProperty(d, field, i);
            BridgeInternal.setProperty(d, partitionKey, firstPk);
            docs.add(d);
        }

        for (int i = 10; i < 20; i++) {
            CosmosItemProperties d = new CosmosItemProperties();
            d.setId(Integer.toString(i));
            BridgeInternal.setProperty(d, field, i);
            BridgeInternal.setProperty(d, partitionKey, secondPk);
            docs.add(d);
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder().buildAsyncClient();
        createdCollection = getSharedSinglePartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        bulkInsert(client);

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }
}
