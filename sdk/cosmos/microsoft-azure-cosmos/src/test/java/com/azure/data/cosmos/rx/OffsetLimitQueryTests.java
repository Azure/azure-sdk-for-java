// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.query.OffsetContinuationToken;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class OffsetLimitQueryTests extends TestSuiteBase {
    private CosmosDatabase createdDatabase;
    private CosmosContainer createdCollection;
    private ArrayList<CosmosItemProperties> docs = new ArrayList<>();

    private String partitionKey = "mypk";
    private int firstPk = 0;
    private int secondPk = 1;
    private String field = "field";

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public OffsetLimitQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocuments(boolean qmEnabled) {
        int skipCount = 4;
        int takeCount = 10;
        String query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(5);
        options.enableCrossPartitionQuery(true);
        options.populateQueryMetrics(qmEnabled);
        options.maxDegreeOfParallelism(2);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

        FeedResponseListValidator<CosmosItemProperties> validator =
            new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(takeCount)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                                     .requestChargeGreaterThanOrEqualTo(1.0)
                                     .build())
                .hasValidQueryMetrics(qmEnabled)
                .build();

        validateQuerySuccess(queryObservable, validator, TIMEOUT);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void drainAllDocumentsUsingOffsetLimit() {
        int skipCount = 0;
        int takeCount = 2;
        String query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.maxDegreeOfParallelism(2);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable;

        int totalDocsObtained = 0;
        int totalDocs = docs.size();
        int expectedNumCalls = totalDocs / takeCount;
        int numCalls = 0;
        FeedResponse<CosmosItemProperties> finalResponse = null;

        while (numCalls < expectedNumCalls) {
            query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;
            queryObservable = createdCollection.queryItems(query, options);
            Iterator<FeedResponse<CosmosItemProperties>> iterator = queryObservable.toIterable().iterator();
            while (iterator.hasNext()) {
                FeedResponse<CosmosItemProperties> next = iterator.next();
                totalDocsObtained += next.results().size();
                finalResponse = next;
            }
            numCalls++;
            skipCount += takeCount;
        }
        assertThat(totalDocsObtained).isEqualTo(docs.size());
        assertThat(finalResponse.continuationToken()).isNull();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void offsetContinuationTokenRoundTrips() {
        // Positive
        OffsetContinuationToken offsetContinuationToken = new OffsetContinuationToken(42, "asdf");
        String serialized = offsetContinuationToken.toString();
        Utils.ValueHolder<OffsetContinuationToken> outOffsetContinuationToken = new Utils.ValueHolder<>();

        assertThat(OffsetContinuationToken.tryParse(serialized, outOffsetContinuationToken)).isTrue();
        OffsetContinuationToken deserialized = outOffsetContinuationToken.v;

        assertThat(deserialized.getOffset()).isEqualTo(42);
        assertThat(deserialized.getSourceToken()).isEqualTo("asdf");

        // Negative
        Utils.ValueHolder<OffsetContinuationToken> outTakeContinuationToken =
            new Utils.ValueHolder<OffsetContinuationToken>();
        assertThat(
            OffsetContinuationToken.tryParse("{\"property\": \"Not a valid token\"}", outTakeContinuationToken))
            .isFalse();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT * 10)
    public void queryDocumentsWithOffsetContinuationTokens() {
        int skipCount = 3;
        int takeCount = 10;
        String query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;
        this.queryWithContinuationTokensAndPageSizes(query, new int[] {1, 5, 15}, takeCount);
    }

    private void queryWithContinuationTokensAndPageSizes(String query, int[] pageSizes, int takeCount) {
        for (int pageSize : pageSizes) {
            List<CosmosItemProperties> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            Set<String> actualIds = new HashSet<String>();
            for (CosmosItemProperties CosmosItemProperties : receivedDocuments) {
                actualIds.add(CosmosItemProperties.resourceId());
            }

            assertThat(actualIds.size()).describedAs("total number of results").isEqualTo(takeCount);
        }
    }

    private List<CosmosItemProperties> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<CosmosItemProperties> receivedDocuments = new ArrayList<CosmosItemProperties>();

        do {
            FeedOptions options = new FeedOptions();
            options.maxItemCount(pageSize);
            options.enableCrossPartitionQuery(true);
            options.maxDegreeOfParallelism(2);
            options.requestContinuation(requestContinuation);
            Flux<FeedResponse<CosmosItemProperties>> queryObservable =
                createdCollection.queryItems(query, options);

            TestSubscriber<FeedResponse<CosmosItemProperties>> testSubscriber = new TestSubscriber<>();
            queryObservable.subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            FeedResponse<CosmosItemProperties> firstPage =
                (FeedResponse<CosmosItemProperties>) testSubscriber.getEvents().get(0).get(0);
            requestContinuation = firstPage.continuationToken();
            receivedDocuments.addAll(firstPage.results());

            continuationTokens.add(requestContinuation);
        } while (requestContinuation != null);

        return receivedDocuments;
    }

    public void bulkInsert() {
        generateTestData();
        voidBulkInsertBlocking(createdCollection, docs);
    }

    public void generateTestData() {

        for (int i = 0; i < 10; i++) {
            CosmosItemProperties d = new CosmosItemProperties();
            d.id(Integer.toString(i));
            BridgeInternal.setProperty(d, field, i);
            BridgeInternal.setProperty(d, partitionKey, firstPk);
            docs.add(d);
        }

        for (int i = 10; i < 20; i++) {
            CosmosItemProperties d = new CosmosItemProperties();
            d.id(Integer.toString(i));
            BridgeInternal.setProperty(d, field, i);
            BridgeInternal.setProperty(d, partitionKey, secondPk);
            docs.add(d);
        }
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = this.clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        bulkInsert();

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }
}

