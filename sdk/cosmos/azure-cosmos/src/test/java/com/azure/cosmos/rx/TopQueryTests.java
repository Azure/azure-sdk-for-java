// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.RetryAnalyzer;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.azure.cosmos.implementation.query.TakeContinuationToken;
import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class TopQueryTests extends TestSuiteBase {
    private CosmosAsyncContainer createdCollection;
    private ArrayList<InternalObjectNode> docs = new ArrayList<InternalObjectNode>();

    private String partitionKey = "mypk";
    private int firstPk = 0;
    private int secondPk = 1;
    private String field = "field";

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public TopQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider", retryAnalyzer = RetryAnalyzer.class)
    public void queryDocumentsWithTop(Boolean qmEnabled) throws Exception {

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }

        int expectedTotalSize = 20;
        int expectedNumberOfPages = 3;
        int[] expectedPageLengths = new int[] { 9, 9, 2 };

        for (int i = 0; i < 2; i++) {
            CosmosPagedFlux<JsonNode> queryObservable1 = createdCollection.queryItems("SELECT TOP 0 value AVG(c.field) from c",
                options,
                                                                                      JsonNode.class);

            FeedResponseListValidator<JsonNode> validator1 = new FeedResponseListValidator.Builder<JsonNode>()
                    .totalSize(0).build();

            validateQuerySuccess(queryObservable1.byPage(9), validator1, TIMEOUT);

            CosmosPagedFlux<JsonNode> queryObservable2 = createdCollection.queryItems("SELECT TOP 1 value AVG(c.field) from c",
                options,
                                                                                      JsonNode.class);

            FeedResponseListValidator<JsonNode> validator2 = new FeedResponseListValidator.Builder<JsonNode>()
                    .totalSize(1).build();

            validateQuerySuccess(queryObservable2.byPage(), validator2, TIMEOUT);

            CosmosPagedFlux<InternalObjectNode> queryObservable3 = createdCollection.queryItems("SELECT TOP 20 * from c", options, InternalObjectNode.class);

            FeedResponseListValidator<InternalObjectNode> validator3 = new FeedResponseListValidator.Builder<InternalObjectNode>()
                    .totalSize(expectedTotalSize).numberOfPages(expectedNumberOfPages).pageLengths(expectedPageLengths)
                    .hasValidQueryMetrics(qmEnabled).build();

            validateQuerySuccess(queryObservable3.byPage(), validator3, TIMEOUT);

            if (i == 0) {
                options.setPartitionKey(new PartitionKey(firstPk));
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
            List<InternalObjectNode> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            Set<String> actualIds = new HashSet<String>();
            for (InternalObjectNode document : receivedDocuments) {
                actualIds.add(document.getResourceId());
            }

            assertThat(actualIds.size()).describedAs("total number of results").isEqualTo(topCount);
        }
    }

    private List<InternalObjectNode> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<InternalObjectNode> receivedDocuments = new ArrayList<InternalObjectNode>();

        do {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setMaxDegreeOfParallelism(2);
            CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.<InternalObjectNode>queryItems(query, options, InternalObjectNode.class);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.first();
            TestSubscriber<FeedResponse<InternalObjectNode>> testSubscriber = new TestSubscriber<>();
            queryObservable.byPage(requestContinuation, pageSize).subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            @SuppressWarnings("unchecked")
            FeedResponse<InternalObjectNode> firstPage = (FeedResponse<InternalObjectNode>) testSubscriber.getEvents().get(0).get(0);
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
            InternalObjectNode d = new InternalObjectNode();
            d.setId(Integer.toString(i));
            BridgeInternal.setProperty(d, field, i);
            BridgeInternal.setProperty(d, partitionKey, firstPk);
            docs.add(d);
        }

        for (int i = 10; i < 20; i++) {
            InternalObjectNode d = new InternalObjectNode();
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
    public void before_TopQueryTests() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedSinglePartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        bulkInsert(client);

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }
}
