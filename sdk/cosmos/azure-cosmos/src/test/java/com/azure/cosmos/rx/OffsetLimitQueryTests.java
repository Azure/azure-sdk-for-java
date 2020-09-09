// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.query.OffsetContinuationToken;
import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class OffsetLimitQueryTests extends TestSuiteBase {
    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private ArrayList<InternalObjectNode> docs = new ArrayList<>();

    private String partitionKey = "mypk";
    private int firstPk = 0;
    private int secondPk = 1;
    private String field = "field";

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public OffsetLimitQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocuments(Boolean qmEnabled) {
        int skipCount = 4;
        int takeCount = 10;
        String query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }

        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options,
                                                                                                InternalObjectNode.class);

        FeedResponseListValidator<InternalObjectNode> validator =
            new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(takeCount)
                .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                                     .requestChargeGreaterThanOrEqualTo(1.0)
                                     .build())
                .hasValidQueryMetrics(qmEnabled)
                .build();

        validateQuerySuccess(queryObservable.byPage(5), validator, TIMEOUT);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void drainAllDocumentsUsingOffsetLimit() {
        int skipCount = 0;
        int takeCount = 2;
        String query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        CosmosPagedFlux<InternalObjectNode> queryObservable;

        int totalDocsObtained = 0;
        int totalDocs = docs.size();
        int expectedNumCalls = totalDocs / takeCount;
        int numCalls = 0;
        FeedResponse<InternalObjectNode> finalResponse = null;

        while (numCalls < expectedNumCalls) {
            query = "SELECT * from c OFFSET " + skipCount + " LIMIT " + takeCount;
            queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);
            Iterator<FeedResponse<InternalObjectNode>> iterator = queryObservable.byPage(5).toIterable().iterator();
            while (iterator.hasNext()) {
                FeedResponse<InternalObjectNode> next = iterator.next();
                totalDocsObtained += next.getResults().size();
                finalResponse = next;
            }
            numCalls++;
            skipCount += takeCount;
        }
        assertThat(totalDocsObtained).isEqualTo(docs.size());
        assertThat(finalResponse.getContinuationToken()).isNull();
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

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsWithDistinct(Boolean qmEnabled) {
        int skipCount = 4;
        int takeCount = 10;
        String query =
            String.format("SELECT DISTINCT c.id from c OFFSET %s LIMIT %s", skipCount, takeCount);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }

        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<String> expectedIds =
            docs.stream().skip(4).limit(10).map(doc -> doc.getResourceId()).collect(Collectors.toList());

        FeedResponseListValidator<InternalObjectNode> validator =
            new FeedResponseListValidator.Builder<InternalObjectNode>()
                .containsExactly(expectedIds)
                .numberOfPages(3)
                .hasValidQueryMetrics(qmEnabled)
                .build();

        validateQuerySuccess(queryObservable.byPage(5), validator, TIMEOUT);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsWithAggregate(Boolean qmEnabled) {
        int skipCount = 0;
        int takeCount = 10;
        String query =
            String.format("SELECT VALUE MAX(c.%s) from c OFFSET %s LIMIT %s", field, skipCount, takeCount);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }

        CosmosPagedFlux<JsonNode> queryObservable = createdCollection.queryItems(query, options, JsonNode.class);

        // The pipeline execution sequence is Aggregrate, skip, and top/limit, hence finding the max from among the docs
        InternalObjectNode expectedDoc = docs
            .stream()
            .max(Comparator.comparing(r -> ModelBridgeInternal.getIntFromJsonSerializable(r, field)))
            .get();

        FeedResponseListValidator<JsonNode> validator =
            new FeedResponseListValidator.Builder<JsonNode>()
                .withAggregateValue(ModelBridgeInternal.getIntFromJsonSerializable(expectedDoc, field))
                .numberOfPages(1)
                .hasValidQueryMetrics(qmEnabled)
                .build();

        validateQuerySuccess(queryObservable.byPage(5), validator, TIMEOUT);
    }

    private void queryWithContinuationTokensAndPageSizes(String query, int[] pageSizes, int takeCount) {
        for (int pageSize : pageSizes) {
            List<InternalObjectNode> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            Set<String> actualIds = new HashSet<String>();
            for (InternalObjectNode InternalObjectNode : receivedDocuments) {
                actualIds.add(InternalObjectNode.getResourceId());
            }

            assertThat(actualIds.size()).describedAs("total number of results").isEqualTo(takeCount);
        }
    }

    private List<InternalObjectNode> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<InternalObjectNode> receivedDocuments = new ArrayList<InternalObjectNode>();

        do {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            CosmosPagedFlux<InternalObjectNode> queryObservable =
                createdCollection.queryItems(query, options, InternalObjectNode.class);

            TestSubscriber<FeedResponse<InternalObjectNode>> testSubscriber = new TestSubscriber<>();
            queryObservable.byPage(requestContinuation,5).subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            @SuppressWarnings("unchecked")
            FeedResponse<InternalObjectNode> firstPage =
                (FeedResponse<InternalObjectNode>) testSubscriber.getEvents().get(0).get(0);
            requestContinuation = firstPage.getContinuationToken();
            receivedDocuments.addAll(firstPage.getResults());

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

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = {"simple"}, timeOut = 3 * SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = this.getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        bulkInsert();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }
}

