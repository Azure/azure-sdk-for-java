// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.examples;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.UniqueKey;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.TestConfigurations;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.reactivex.subscribers.TestSubscriber;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

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
        collectionDefinition.setId(UUID.randomUUID().toString());
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/field"));
        uniqueKeyPolicy.setUniqueKeys(Lists.newArrayList(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        DocumentCollection collection = client.createCollection(getDatabaseLink(), collectionDefinition, null).single().block().getResource();

        Document doc1 = new Document("{ 'name':'Alan Turning', 'field': 'Mathematics', 'other' : 'Logic' }");
        Document doc2 = new Document("{ 'name':'Al-Khwarizmi', 'field': 'Mathematics' , 'other' : 'Algebra '}");
        Document doc3 = new Document("{ 'name':'Alan Turning', 'field': 'Mathematics', 'other' : 'CS' }");

        doc1.setId(UUID.randomUUID().toString());
        doc2.setId(UUID.randomUUID().toString());
        doc3.setId(UUID.randomUUID().toString());

        client.createDocument(getCollectionLink(collection), doc1, null, false).single().block().getResource();
        client.createDocument(getCollectionLink(collection), doc2, null, false).single().block().getResource();

        // doc1 got inserted with the same values for 'name' and 'field'
        // so inserting a new one with the same values will violate unique index constraint.
        Mono<ResourceResponse<Document>> docCreation =
                client.createDocument(getCollectionLink(collection), doc3, null, false);

        TestSubscriber<ResourceResponse<Document>> subscriber = new TestSubscriber<>();
        docCreation.subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertError(CosmosClientException.class);
        assertThat(subscriber.errorCount(), Matchers.equalTo(1));

        // error code for failure is conflict
        assertThat(((CosmosClientException) subscriber.getEvents().get(1).get(0)).getStatusCode(), equalTo(409));
    }

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void before_UniqueIndexAsyncAPITest() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy().setConnectionMode(ConnectionMode.DIRECT);

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true);

        this.client = this.clientBuilder().build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        // CREATE getDatabase
        createdDatabase = Utils.createDatabaseForTest(client);
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        Utils.safeClose(client);
    }

    private String getCollectionLink(DocumentCollection collection) {
        return "dbs/" + createdDatabase.getId() + "/colls/" + collection.getId();
    }

    private String getDatabaseLink() {
        return "dbs/" + createdDatabase.getId();
    }
}
