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

import java.util.ArrayList;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

public class TopQueryTests extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(TopQueryTests.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private ArrayList<Document> docs = new ArrayList<Document>();

    private String partitionKey = "mypk";
    private int firstPk = 0;
    private int secondPk = 1;
    private String field = "field";

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public TopQueryTests(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocumentsWithTop() throws Exception {

        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxItemCount(9);
        options.setMaxDegreeOfParallelism(2);

        int expectedTotalSize = 20;
        int expectedNumberOfPages = 3;
        int[] expectedPageLengths = new int[] {9, 9, 2};

        for (int i = 0; i < 2; i++) {
            Observable<FeedResponse<Document>> queryObservable1 = client.queryDocuments(createdCollection.getSelfLink(), 
                    "SELECT TOP 0 value AVG(c.field) from c", options);

            FeedResponseListValidator<Document> validator1 = new FeedResponseListValidator.Builder<Document>()
                    .totalSize(0)
                    .build();

            validateQuerySuccess(queryObservable1, validator1, TIMEOUT);

            Observable<FeedResponse<Document>> queryObservable2 = client.queryDocuments(createdCollection.getSelfLink(), 
                    "SELECT TOP 1 value AVG(c.field) from c", options);

            FeedResponseListValidator<Document> validator2 = new FeedResponseListValidator.Builder<Document>()
                    .totalSize(1)
                    .build();

            validateQuerySuccess(queryObservable2, validator2, TIMEOUT);

            Observable<FeedResponse<Document>> queryObservable3 = client.queryDocuments(createdCollection.getSelfLink(), 
                    "SELECT TOP 20 * from c", options);

            FeedResponseListValidator<Document> validator3 = new FeedResponseListValidator.Builder<Document>()
                    .totalSize(expectedTotalSize)
                    .numberOfPages(expectedNumberOfPages)
                    .pageLengths(expectedPageLengths)
                    .build();

            validateQuerySuccess(queryObservable3, validator3, TIMEOUT);

            if (i == 0) {
                options.setPartitionKey(new PartitionKey(firstPk));
                options.setEnableCrossPartitionQuery(false);

                expectedTotalSize = 10;
                expectedNumberOfPages = 2;
                expectedPageLengths = new int[] {9, 1};

            }
        }
    }

    public void bulkInsert(AsyncDocumentClient client) {
        generateTestData();

        for (int i = 0; i < docs.size(); i++) {
            createDocument(client, createdDatabase.getId(), createdCollection.getId(), docs.get(i));
        }
    }

    public void generateTestData() {

        for (int i = 0; i < 10; i++) {
            Document d = new Document();
            d.setId(Integer.toString(i));
            d.set(field, i);
            d.set(partitionKey, firstPk);
            docs.add(d);
        }

        for (int i = 10; i < 20; i++) {
            Document d = new Document();
            d.setId(Integer.toString(i));
            d.set(field, i);
            d.set(partitionKey, secondPk);
            docs.add(d);
        }

    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {        
        safeDeleteDatabase(client, DATABASE_ID);
        safeClose(client);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();

        Database d1 = new Database();
        d1.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d1);

        createdCollection = createCollection(client, createdDatabase.getId(),
                getCollectionDefinition());

        bulkInsert(client);

    }
}
