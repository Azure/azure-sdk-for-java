/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.FeedResponsePage;
import com.microsoft.azure.documentdb.PartitionKeyDefinition;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

public class DocumentQueryTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(DocumentQueryTest.class);
    
    private static Database createdDatabase;
    private static DocumentCollection createdCollection;
    private static AsyncDocumentClient houseKeepingClient;
    private static List<Document> createdDocuments = new ArrayList<>();

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    public static String getCollectionLink() {
        return createdCollection.getSelfLink();
    }

    static protected DocumentCollection getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    @Factory(dataProvider = "clientBuilders")
    public DocumentQueryTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments() throws Exception {

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponsePage<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FeedResponsePageListValidator<Document> validator = new FeedResponsePageListValidator
                .Builder<Document>()
                .containsExactly(createdDocuments
                        .stream()
                        .map(d -> d.getResourceId())
                        .collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponsePageValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponsePage<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FeedResponsePageListValidator<Document> validator = new FeedResponsePageListValidator.Builder<Document>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponsePageValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocumentsWithPageSize() throws Exception {

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.setPageSize(3);
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponsePage<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FeedResponsePageListValidator<Document> validator = new FeedResponsePageListValidator
                .Builder<Document>()
                .containsExactly(createdDocuments
                        .stream()
                        .map(d -> d.getResourceId())
                        .collect(Collectors.toList()))
                .numberOfPages((createdDocuments.size() + 1) / 3)
                .allPagesSatisfy(new FeedResponsePageValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderBy() throws Exception {

        String query = "SELECT * FROM r ORDER BY r.prop ASC";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setPageSize(3);
        Observable<FeedResponsePage<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FeedResponsePageListValidator<Document> validator = new FeedResponsePageListValidator.Builder<Document>()
                .containsExactly(createdDocuments.stream()
                        .sorted((e1, e2) -> Integer.compare(e1.getInt("prop"), e2.getInt("prop")))
                        .map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages((createdDocuments.size() + 1) / 3)
                .allPagesSatisfy(new FeedResponsePageValidator.Builder<Document>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, enabled = false)
    public void invalidQuerySytax() throws Exception {

        // NOTE: this test passes on Linux but not on Windows in the presence of dlls
        
        // ServiceJNIWrapper in DocumentClient throws IllegalArgumentException instead of DocumentClientException
        // after the behavior is fixed enable this test
        String query = "I am an invalid query";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponsePage<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .notNullActivityId()
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void crossPartitionQueryNotEnabled() throws Exception {

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        Observable<FeedResponsePage<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    public static void createDocument(AsyncDocumentClient client, int cnt) throws DocumentClientException {
        Document docDefinition = getDocumentDefinition(cnt);

        Document createdDocument = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();
        createdDocuments.add(createdDocument);
    }
    
    @BeforeSuite(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public static void beforeSuite() throws Exception {
        houseKeepingClient = createRxWrapperDocumentClient().build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(houseKeepingClient, d);
        createdCollection = safeCreateCollection(houseKeepingClient, createdDatabase.getSelfLink(), getCollectionDefinition());
        for(int i = 0; i < 5; i++) {
            createDocument(houseKeepingClient, i);
        }
    }

    @AfterSuite(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public static void afterSuite() {
        deleteDatabase(houseKeepingClient, createdDatabase.getId());
        houseKeepingClient.close();
    }
    
    @BeforeSuite
    public static void createDocuments() throws Exception {

    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        // set up the client        
        client = clientBuilder.build();

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
