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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;
import rx.observers.TestSubscriber;


public class SinglePartitionDocumentQueryTest extends TestSuiteBase {

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<Document> createdDocuments = new ArrayList<>();

    private AsyncDocumentClient client;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    @Factory(dataProvider = "clientBuildersWithDirect")
    public SinglePartitionDocumentQueryTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocuments(boolean queryMetricsEnabled) throws Exception {

        String query = "SELECT * from c where c.prop = 99";

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        options.setPopulateQueryMetrics(queryMetricsEnabled);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        List<Document> expectedDocs = createdDocuments.stream().filter(d -> 99 == d.getInt("prop") ).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .hasValidQueryMetrics(queryMetricsEnabled)
                .build();

        try {
            validateQuerySuccess(queryObservable, validator, 10000);
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_ParameterizedQueryWithInClause() throws Exception {
        String query = "SELECT * from c where c.prop IN (@param1, @param2)";
        SqlParameterCollection params = new SqlParameterCollection(new SqlParameter("@param1", 3), new SqlParameter("@param2", 4));
        SqlQuerySpec sqs = new SqlQuerySpec(query, params);
        
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), sqs, options);

        List<Document> expectedDocs = createdDocuments.stream().filter(d -> (3 == d.getInt("prop") || 4 == d.getInt("prop"))).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        try {
            validateQuerySuccess(queryObservable, validator, 10000);
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_ParameterizedQuery() throws Exception {
        String query = "SELECT * from c where c.prop = @param";
        SqlParameterCollection params = new SqlParameterCollection(new SqlParameter("@param", 3));
        SqlQuerySpec sqs = new SqlQuerySpec(query, params);

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), sqs, options);

        List<Document> expectedDocs = createdDocuments.stream().filter(d -> 3 == d.getInt("prop")).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        try {
            validateQuerySuccess(queryObservable, validator, 10000);
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocumentsWithPageSize() throws Exception {

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(3);
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<Document>> queryObservable = client
            .queryDocuments(getCollectionLink(), query, options);

        List<Document> expectedDocs = createdDocuments;
        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator
            .Builder<Document>()
            .exactlyContainsInAnyOrder(createdDocuments
                .stream()
                .map(d -> d.getResourceId())
                .collect(Collectors.toList()))
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                .requestChargeGreaterThanOrEqualTo(1.0).build())
            .build();

        try {
            validateQuerySuccess(queryObservable, validator);
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderBy() throws Exception {

        String query = "SELECT * FROM r ORDER BY r.prop ASC";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxItemCount(3);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        List<Document> expectedDocs = createdDocuments;        
        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(createdDocuments.stream()
                        .sorted((e1, e2) -> Integer.compare(e1.getInt("prop"), e2.getInt("prop")))
                        .map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        try {
            validateQuerySuccess(queryObservable, validator);
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 1000)
    public void continuationToken() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.prop ASC";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxItemCount(3);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        
        TestSubscriber<FeedResponse<Document>> subscriber = new TestSubscriber<>();
        queryObservable.first().subscribe(subscriber);
        
        subscriber.awaitTerminalEvent();
        subscriber.assertCompleted();
        subscriber.assertNoErrors();
        assertThat(subscriber.getValueCount()).isEqualTo(1);
        FeedResponse<Document> page = subscriber.getOnNextEvents().get(0);
        assertThat(page.getResults()).hasSize(3);
        
        assertThat(page.getResponseContinuation()).isNotEmpty();
        
        
        options.setRequestContinuation(page.getResponseContinuation());
        queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);
        

        List<Document> expectedDocs = createdDocuments.stream().filter(d -> (d.getInt("prop") > 2)).collect(Collectors.toList());
        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        assertThat(expectedDocs).hasSize(createdDocuments.size() -3);
        
        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(expectedDocs.stream()
                        .sorted((e1, e2) -> Integer.compare(e1.getInt("prop"), e2.getInt("prop")))
                        .map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySytax() throws Exception {
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

    public Document createDocument(AsyncDocumentClient client, int cnt) {
        Document docDefinition = getDocumentDefinition(cnt);
        return client.createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_SINGLE_PARTITION_COLLECTION_WITHOUT_PARTITION_KEY;
        truncateCollection(SHARED_SINGLE_PARTITION_COLLECTION_WITHOUT_PARTITION_KEY);

        for(int i = 0; i < 5; i++) {
            createdDocuments.add(createDocument(client, i));
        }

        for(int i = 0; i < 8; i++) {
            createdDocuments.add(createDocument(client, 99));
        }

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
