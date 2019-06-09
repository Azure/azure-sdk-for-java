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
package com.azure.data.cosmos.rx.examples;

import com.azure.data.cosmos.*;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class UniqueIndexAsyncAPITest {
    private final static int TIMEOUT = 60000;

    private AsyncDocumentClient client;
    private Database createdDatabase;

    @Test(groups = "samples", timeOut = TIMEOUT)
    public void uniqueIndex() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.paths(ImmutableList.of("/name", "/field"));
        uniqueKeyPolicy.uniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        DocumentCollection collection = client.createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single().getResource();

        Document doc1 = new Document("{ 'name':'Alan Turning', 'field': 'Mathematics', 'other' : 'Logic' }");
        Document doc2 = new Document("{ 'name':'Al-Khwarizmi', 'field': 'Mathematics' , 'other' : 'Algebra '}");
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
        subscriber.assertError(CosmosClientException.class);
        assertThat(subscriber.getOnErrorEvents(), hasSize(1));

        // error code for failure is conflict
        assertThat(((CosmosClientException) subscriber.getOnErrorEvents().get(0)).statusCode(), equalTo(409));
    }

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {
        // Sets up the requirements for each test
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);
        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());

        // CREATE database
        createdDatabase = Utils.createDatabaseForTest(client);
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        Utils.safeClose(client);
    }

    private String getCollectionLink(DocumentCollection collection) {
        return "dbs/" + createdDatabase.id() + "/colls/" + collection.id();
    }

    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.id();
    }
}
