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
import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ExcludedPath;
import com.microsoft.azure.cosmosdb.HashIndex;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.UniqueKey;
import com.microsoft.azure.cosmosdb.UniqueKeyPolicy;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;

import rx.Observable;
import rx.observers.TestSubscriber;

public class UniqueIndexTest extends TestSuiteBase {
    private final static String DATABASE_ID = getDatabaseId(UniqueIndexTest.class);

    protected static final int TIMEOUT = 20000;
    protected static final int SETUP_TIMEOUT = 20000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    private AsyncDocumentClient client;
    private Database database;

    private DocumentCollection collection;

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void insertWithUniqueIndex() throws Exception {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.setUniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.Consistent);
        ExcludedPath excludedPath = new ExcludedPath();
        excludedPath.setPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath();
        includedPath1.setPath("/name/?");
        includedPath1.setIndexes(Collections.singletonList(new HashIndex(DataType.String, 7)));

        IncludedPath includedPath2 = new IncludedPath();
        includedPath2.setPath("/description/?");
        includedPath2.setIndexes(Collections.singletonList(new HashIndex(DataType.String, 7)));
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        ObjectMapper om = new ObjectMapper();

        JsonNode doc1 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"poet\"}", JsonNode.class);
        JsonNode doc2 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"playwright\"}", JsonNode.class);
        JsonNode doc3 = om.readValue("{\"name\":\"حافظ شیرازی\",\"description\":\"poet\"}", JsonNode.class);

        collection = client.createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single().getResource();

        Document dd = client.createDocument(getCollectionLink(collection), doc1, null, false).toBlocking().single().getResource();

        client.readDocument(dd.getSelfLink(), null).toBlocking().single();

        try {
            client.createDocument(getCollectionLink(collection), doc1, null, false).toBlocking().single();
            fail("Did not throw due to unique constraint (create)");
        } catch (RuntimeException e) {
            assertThat(getDocumentClientException(e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }

        client.createDocument(getCollectionLink(collection), doc2, null, false).toBlocking().single();
        client.createDocument(getCollectionLink(collection), doc3, null, false).toBlocking().single();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceAndDeleteWithUniqueIndex() throws Exception {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.setUniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        collection = client.createCollection(getDatabaseLink(), collectionDefinition, null).toBlocking().single().getResource();

        ObjectMapper om = new ObjectMapper();

        ObjectNode doc1 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"poet\"}", ObjectNode.class);
        ObjectNode doc3 = om.readValue("{\"name\":\"Rabindranath Tagore\",\"description\":\"poet\"}", ObjectNode.class);
        ObjectNode doc2 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"mathematician\"}", ObjectNode.class);

        Document doc1Inserted =  client.createDocument(
                getCollectionLink(collection), doc1, null, false).toBlocking().single().getResource();

        client.replaceDocument(doc1Inserted.getSelfLink(), doc1Inserted, null).toBlocking().single();     // Replace with same values -- OK.

        Document doc2Inserted =  client.createDocument(getCollectionLink(collection), doc2, null, false).toBlocking().single().getResource();
        Document doc2Replacement = new Document(doc1Inserted.toJson());
        doc2Replacement.setId( doc2Inserted.getId());

        try {
            client.replaceDocument(doc2Inserted.getSelfLink(), doc2Replacement, null).toBlocking().single(); // Replace doc2 with values from doc1 -- Conflict.
            fail("Did not throw due to unique constraint");
        }
        catch (RuntimeException ex) {
            assertThat(getDocumentClientException(ex).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }

        doc3.put("id", doc1Inserted.getId());
        client.replaceDocument(doc1Inserted.getSelfLink(), doc3, null).toBlocking().single();             // Replace with values from doc3 -- OK.

        client.deleteDocument(doc1Inserted.getSelfLink(), null).toBlocking().single();
        client.createDocument(getCollectionLink(collection), doc1, null, false).toBlocking().single();
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void uniqueKeySerializationDeserialization() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.setUniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.Consistent);
        ExcludedPath excludedPath = new ExcludedPath();
        excludedPath.setPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath();
        includedPath1.setPath("/name/?");
        includedPath1.setIndexes(Collections.singletonList(new HashIndex(DataType.String, 7)));

        IncludedPath includedPath2 = new IncludedPath();
        includedPath2.setPath("/description/?");
        includedPath2.setIndexes(Collections.singletonList(new HashIndex(DataType.String, 7)));
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        collectionDefinition.setIndexingPolicy(indexingPolicy);

        DocumentCollection createdCollection = client.createCollection(database.getSelfLink(), collectionDefinition,
                null).toBlocking().single().getResource();

        DocumentCollection collection = client.readCollection(getCollectionLink(createdCollection), null)
                .toBlocking().single().getResource();

        assertThat(collection.getUniqueKeyPolicy()).isNotNull();
        assertThat(collection.getUniqueKeyPolicy().getUniqueKeys()).isNotNull();
        assertThat(collection.getUniqueKeyPolicy().getUniqueKeys())
                .hasSameSizeAs(collectionDefinition.getUniqueKeyPolicy().getUniqueKeys());
        assertThat(collection.getUniqueKeyPolicy().getUniqueKeys()
                .stream().map(ui -> ui.getPaths()).collect(Collectors.toList()))
                .containsExactlyElementsOf(
                        ImmutableList.of(ImmutableList.of("/name", "/description")));
    }

    private DocumentClientException getDocumentClientException(RuntimeException e) {
        DocumentClientException dce = com.microsoft.azure.cosmosdb.rx.internal.Utils.as(e.getCause(), DocumentClientException.class);
        assertThat(dce).isNotNull();
        return dce;
    }

    private String getDatabaseLink() {
        return database.getSelfLink();
    }
    
    public String getCollectionLink() {
        return "dbs/" + database.getId() + "/colls/" + collection.getId();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client
        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session).build();

        Database databaseDefinition = new Database();
        databaseDefinition.setId(DATABASE_ID);

        database = safeCreateDatabase(client, databaseDefinition);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, DATABASE_ID);
        safeClose(client);
    }
}
