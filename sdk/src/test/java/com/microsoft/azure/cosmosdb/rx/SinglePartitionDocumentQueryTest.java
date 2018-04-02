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
import java.util.UUID;
import java.util.stream.Collectors;

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


public class SinglePartitionDocumentQueryTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(SinglePartitionDocumentQueryTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<Document> createdDocuments = new ArrayList<>();

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    static protected DocumentCollection getCollectionDefinition() {

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        return collectionDefinition;
    }

    @Factory(dataProvider = "clientBuilders")
    public SinglePartitionDocumentQueryTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments() throws Exception {

        String query = "SELECT * from c where c.prop = 99";

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
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
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
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
        validateQuerySuccess(queryObservable, validator);
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

    public Document createDocument(AsyncDocumentClient client, int cnt) throws DocumentClientException {
        Document docDefinition = getDocumentDefinition(cnt);
        return client.createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition());

        for(int i = 0; i < 5; i++) {
            createdDocuments.add(createDocument(client, i));
        }

        for(int i = 0; i < 8; i++) {
            createdDocuments.add(createDocument(client, 99));
        }
        
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
