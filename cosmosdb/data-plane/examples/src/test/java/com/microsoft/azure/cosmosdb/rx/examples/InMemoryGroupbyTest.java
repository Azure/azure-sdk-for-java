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
package com.microsoft.azure.cosmosdb.rx.examples;

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observables.GroupedObservable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InMemoryGroupbyTest {
    private final static int TIMEOUT = 60000;

    private static AsyncDocumentClient asyncClient;
    private static Database createdDatabase;
    private static DocumentCollection createdCollection;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public static void setUp() throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);
        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        // Create database
        createdDatabase = Utils.createDatabaseForTest(asyncClient);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        // Create collection
        createdCollection = asyncClient
                .createCollection("dbs/" + createdDatabase.getId(), collectionDefinition, null)
                .toBlocking().single().getResource();

        int numberOfPayers = 10;
        int numberOfDocumentsPerPayer = 10;

        for (int i = 0; i < numberOfPayers; i++) {

            for (int j = 0; j < numberOfDocumentsPerPayer; j++) {

                LocalDateTime currentTime = LocalDateTime.now();

                Document doc = new Document(String.format("{ "
                        + "'id' : '%s',"
                        + "'site_id': 'ABC', "
                        + "'payer_id': %d, "
                        + " 'created_time' : %d "
                        + "}", UUID.randomUUID().toString(), i, currentTime.getSecond()));
                asyncClient.createDocument(getCollectionLink(), doc, null, true).toBlocking().single();

                Thread.sleep(100);
            }
        }
        System.out.println("finished inserting documents");
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public static void shutdown() {
        Utils.safeClean(asyncClient, createdDatabase);
        asyncClient.close();
    }

    /**
     * Queries Documents and performs Group by operation after fetching the Documents.
     * If you want to understand the steps in more details see {@link #groupByInMemory_MoreDetail()}
     * @throws Exception
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void groupByInMemory() {
        // If you want to understand the steps in more details see groupByInMemoryMoreDetail()
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);

        Observable<Document> documentsObservable = asyncClient
                .queryDocuments(getCollectionLink(),
                        new SqlQuerySpec("SELECT * FROM root r WHERE r.site_id=@site_id",
                                new SqlParameterCollection(new SqlParameter("@site_id", "ABC"))),
                        options)
                .flatMap(page -> Observable.from(page.getResults()));

        final LocalDateTime now = LocalDateTime.now();

        List<List<Document>> resultsGroupedAsLists = documentsObservable
                .filter(doc -> Math.abs(now.getSecond() - doc.getInt("created_time")) <= 90)
                .groupBy(doc -> doc.getInt("payer_id")).flatMap(grouped -> grouped.toList())
                .toList()
                .toBlocking()
                .single();

        for(List<Document> resultsForEachPayer :resultsGroupedAsLists) {
            System.out.println("documents with payer_id : " + resultsForEachPayer.get(0).getInt("payer_id") + " are " + resultsForEachPayer);
        }
    }

    /**
     * This does the same thing as {@link #groupByInMemory_MoreDetail()} but with pedagogical details
     * @throws Exception
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void groupByInMemory_MoreDetail() {

        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(requestPageSize);


        Observable<Document> documentsObservable = asyncClient
                .queryDocuments(getCollectionLink(),
                        new SqlQuerySpec("SELECT * FROM root r WHERE r.site_id=@site_id",
                                new SqlParameterCollection(new SqlParameter("@site_id", "ABC"))),
                        options)
                .flatMap(page -> Observable.from(page.getResults()));

        final LocalDateTime now = LocalDateTime.now();

        Observable<GroupedObservable<Integer, Document>> groupedByPayerIdObservable = documentsObservable
                .filter(doc -> Math.abs(now.getSecond() - doc.getInt("created_time")) <= 90)
                .groupBy(doc -> doc.getInt("payer_id"));

        Observable<List<Document>> docsGroupedAsList = groupedByPayerIdObservable.flatMap(grouped -> {
            Observable<List<Document>> list = grouped.toList();
            return list;
        });

        List<List<Document>> resultsGroupedAsLists = docsGroupedAsList.toList().toBlocking().single();

        for(List<Document> resultsForEachPayer : resultsGroupedAsLists) {
            System.out.println("documents with payer_id : " + resultsForEachPayer.get(0).getInt("payer_id") + " are " + resultsForEachPayer);
        }
    }

    private static  String getCollectionLink() {
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId();
    }
}
