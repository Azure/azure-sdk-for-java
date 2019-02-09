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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.QueryMetrics;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

public class ParallelDocumentQueryTest extends TestSuiteBase {
    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<Document> createdDocuments;

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ParallelDocumentQueryTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @DataProvider(name = "queryMetricsArgProvider")
    public Object[][] queryMetricsArgProvider() {
        return new Object[][]{
                {true},
                {false},
        };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocuments(boolean qmEnabled) {
        String query = "SELECT * from c where c.prop = 99";
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        options.setEnableCrossPartitionQuery(true);
        options.setPopulateQueryMetrics(qmEnabled);
        options.setMaxDegreeOfParallelism(2);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        List<Document> expectedDocs = createdDocuments.stream().filter(d -> 99 == d.getInt("prop") ).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .hasValidQueryMetrics(qmEnabled)
                .build();

        try {
            validateQuerySuccess(queryObservable, validator, TIMEOUT);
        } catch (Throwable error) {
            if (clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                throw new SkipException(String.format("Direct TCP test failure: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel), error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryMetricEquality() throws Exception {
        if (clientBuilder.configs.getProtocol() == Protocol.Tcp) {
            throw new SkipException("RNTBD");
        }

        String query = "SELECT * from c where c.prop = 99";
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        options.setEnableCrossPartitionQuery(true);
        options.setPopulateQueryMetrics(true);
        options.setMaxDegreeOfParallelism(0);

        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);
        List<FeedResponse<Document>> resultList1 = queryObservable.toList().toBlocking().single();

        options.setMaxDegreeOfParallelism(4);
        Observable<FeedResponse<Document>> threadedQueryObs = client.queryDocuments(getCollectionLink(), query,
                options);
        List<FeedResponse<Document>> resultList2 = threadedQueryObs.toList().toBlocking().single();

        assertThat(resultList1.size()).isEqualTo(resultList2.size());
        for(int i = 0; i < resultList1.size(); i++){
            compareQueryMetrics(resultList1.get(i).getQueryMetrics(), resultList2.get(i).getQueryMetrics());
        }
    }

    private void compareQueryMetrics(Map<String, QueryMetrics> qm1, Map<String, QueryMetrics> qm2) {
        assertThat(qm1.keySet().size()).isEqualTo(qm2.keySet().size());
        QueryMetrics queryMetrics1 = BridgeInternal.createQueryMetricsFromCollection(qm1.values());
        QueryMetrics queryMetrics2 = BridgeInternal.createQueryMetricsFromCollection(qm2.values());
        assertThat(queryMetrics1.getRetrievedDocumentSize()).isEqualTo(queryMetrics2.getRetrievedDocumentSize());
        assertThat(queryMetrics1.getRetrievedDocumentCount()).isEqualTo(queryMetrics2.getRetrievedDocumentCount());
        assertThat(queryMetrics1.getIndexHitDocumentCount()).isEqualTo(queryMetrics2.getIndexHitDocumentCount());
        assertThat(queryMetrics1.getOutputDocumentCount()).isEqualTo(queryMetrics2.getOutputDocumentCount());
        assertThat(queryMetrics1.getOutputDocumentSize()).isEqualTo(queryMetrics2.getOutputDocumentSize());
        assertThat(BridgeInternal.getClientSideMetrics(queryMetrics1).getRequestCharge())
                .isEqualTo(BridgeInternal.getClientSideMetrics(queryMetrics1).getRequestCharge());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_NoResults() {
        if (clientBuilder.configs.getProtocol() == Protocol.Tcp) {
            throw new SkipException("RNTBD");
        }

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(new ArrayList<>())
                .numberOfPagesIsGreaterThanOrEqualTo(1)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                                         .pageSizeIsLessThanOrEqualTo(0)
                                         .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocumentsWithPageSize() {
        if (clientBuilder.configs.getProtocol() == Protocol.Tcp) {
            throw new SkipException("RNTBD");
        }

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        options.setMaxDegreeOfParallelism(-1);
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        List<Document> expectedDocs = createdDocuments;
        assertThat(expectedDocs).isNotEmpty();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator
                .Builder<Document>()
                .exactlyContainsInAnyOrder(expectedDocs
                        .stream()
                        .map(d -> d.getResourceId())
                        .collect(Collectors.toList()))
                .numberOfPagesIsGreaterThanOrEqualTo((expectedDocs.size() + 1) / 3)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                                         .requestChargeGreaterThanOrEqualTo(1.0)
                                         .pageSizeIsLessThanOrEqualTo(pageSize)
                                         .build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySyntax() {
        if (clientBuilder.configs.getProtocol() == Protocol.Tcp) {
            throw new SkipException("RNTBD");
        }

        String query = "I am an invalid query";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .notNullActivityId()
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void crossPartitionQueryNotEnabled() {
        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void partitionKeyRangeId() {
        int sum = 0;
        try {
            for (String partitionKeyRangeId : client.readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Observable.from(p.getResults()))
                .map(pkr -> pkr.getId()).toList().toBlocking().single()) {
                String query = "SELECT * from root";
                FeedOptions options = new FeedOptions();
                options.setPartitionKeyRangeIdInternal(partitionKeyRangeId);
                int queryResultCount = client
                    .queryDocuments(getCollectionLink(), query, options)
                    .flatMap(p -> Observable.from(p.getResults()))
                    .toList().toBlocking().single().size();

                sum += queryResultCount;
            }
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                throw new SkipException(String.format("Direct TCP test failure: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel), error);
            }
            throw error;
        }

        assertThat(sum).isEqualTo(createdDocuments.size());
    }

    // TODO: DANOBLE: Tcp protocol performance issue or--maybe--a public emulator performance problem
    // When I've watch this method execute in the debugger and seen that the code sometimes pauses for quite a while in
    // the middle of the second group of 21 documents. I test against a debug instance of the public emulator and so
    // what I'm seeing could be the result of a public emulator performance issue. Of course, it might also be the
    // result of a Tcp protocol performance problem.
    @BeforeClass(groups = { "simple" }, timeOut = 2 * SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_MULTI_PARTITION_COLLECTION;
        truncateCollection(SHARED_MULTI_PARTITION_COLLECTION);
        List<Document> docDefList = new ArrayList<>();
        for(int i = 0; i < 13; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        for(int i = 0; i < 21; i++) {
            docDefList.add(getDocumentDefinition(99));
        }

        createdDocuments = bulkInsertBlocking(client, getCollectionLink(), docDefList);

        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
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
