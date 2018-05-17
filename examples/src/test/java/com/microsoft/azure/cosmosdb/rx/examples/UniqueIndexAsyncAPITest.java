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

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.UniqueKey;
import com.microsoft.azure.cosmosdb.UniqueKeyPolicy;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class UniqueIndexAsyncAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueIndexAsyncAPITest.class);
    private static final String DATABASE_ID = "async-test-db";

    private AsyncDocumentClient client;
    private Database createdDatabase;

    @Test
    public void uniqueIndex()  {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/field"));
        uniqueKeyPolicy.setUniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        DocumentCollection collection = client.createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single().getResource();

        Document doc1 = new Document("{ 'name':'Alan Turning', 'field': 'Mathematics', 'other' : 'Logic' }");
        Document doc2 = new Document("{ 'name':'Al-Khwarizmi', 'field': 'Mathematics' , other' : 'Algebra '}");
        Document doc3 = new Document("{ 'name':'Alan Turning', 'field': 'Mathematics', 'other' : 'CS' }");

        client.createDocument(getCollectionLink(collection), doc1, null, false).toBlocking().single().getResource();
        client.createDocument(getCollectionLink(collection), doc2, null, false).toBlocking().single().getResource();

        // doc1 got inserted with the same values for 'name' and 'field'
        // so inserting a new one with the same values will violate unique index constraint.
        Observable<ResourceResponse<Document>> docCreation =
                client.createDocument(getCollectionLink(collection), doc3, null, false);

        TestSubscriber<ResourceResponse<Document>> subscriber = new TestSubscriber<>();
        docCreation.subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertError(DocumentClientException.class);
        assertThat(subscriber.getOnErrorEvents(), hasSize(1));

        // error code for failure is conflict
        assertThat(((DocumentClientException) subscriber.getOnErrorEvents().get(0)).getStatusCode(), equalTo(409));
    }

    @Before
    public void setUp()  {
        // Sets up the requirements for each test
        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        // Clean up the database.
        this.cleanUpGeneratedDatabases();

        Database databaseDefinition = new Database();
        databaseDefinition.setId(DATABASE_ID);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        // Create database
        createdDatabase = client.createDatabase(databaseDefinition, null)
                .toBlocking().single().getResource();
    }

    @After
    public void shutdown() {
        cleanUpGeneratedDatabases();
        client.close();
    }

    private String getCollectionLink(DocumentCollection collection) {
        return "dbs/" + createdDatabase.getId() + "/colls/" + collection.getId();
    }
    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.getId();
    }

    private void cleanUpGeneratedDatabases() {
        LOGGER.info("cleanup databases invoked");

        String[] allDatabaseIds = { DATABASE_ID };

        for (String id : allDatabaseIds) {
            try {
                List<FeedResponse<Database>> feedResponsePages = client
                        .queryDatabases(new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                                new SqlParameterCollection(new SqlParameter("@id", id))), null)
                        .toList().toBlocking().single();

                if (!feedResponsePages.get(0).getResults().isEmpty()) {
                    Database res = feedResponsePages.get(0).getResults().get(0);
                    LOGGER.info("deleting a database " + feedResponsePages.get(0));
                    client.deleteDatabase("dbs/" + res.getId(), null).toBlocking().single();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
