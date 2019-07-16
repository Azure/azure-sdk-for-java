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

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.DocumentClientTest;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.UniqueKey;
import com.azure.data.cosmos.UniqueKeyPolicy;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.reactivex.subscribers.TestSubscriber;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UniqueIndexAsyncAPITest extends DocumentClientTest {

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
        uniqueKeyPolicy.uniqueKeys(Lists.newArrayList(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        DocumentCollection collection = client.createCollection(getDatabaseLink(), collectionDefinition, null).single().block().getResource();

        Document doc1 = new Document("{ 'name':'Alan Turning', 'field': 'Mathematics', 'other' : 'Logic' }");
        Document doc2 = new Document("{ 'name':'Al-Khwarizmi', 'field': 'Mathematics' , 'other' : 'Algebra '}");
        Document doc3 = new Document("{ 'name':'Alan Turning', 'field': 'Mathematics', 'other' : 'CS' }");

        client.createDocument(getCollectionLink(collection), doc1, null, false).single().block().getResource();
        client.createDocument(getCollectionLink(collection), doc2, null, false).single().block().getResource();

        // doc1 got inserted with the same values for 'name' and 'field'
        // so inserting a new one with the same values will violate unique index constraint.
        Flux<ResourceResponse<Document>> docCreation =
                client.createDocument(getCollectionLink(collection), doc3, null, false);

        TestSubscriber<ResourceResponse<Document>> subscriber = new TestSubscriber<>();
        docCreation.subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertError(CosmosClientException.class);
        assertThat(subscriber.errorCount(), Matchers.equalTo(1));

        // error code for failure is conflict
        assertThat(((CosmosClientException) subscriber.getEvents().get(1).get(0)).statusCode(), equalTo(409));
    }

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy().connectionMode(ConnectionMode.DIRECT);

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION);

        this.client = this.clientBuilder().build();

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
