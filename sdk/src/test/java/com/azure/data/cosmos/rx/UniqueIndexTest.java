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
package com.azure.data.cosmos.rx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.azure.data.cosmos.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.HttpConstants;

public class UniqueIndexTest extends TestSuiteBase {
    protected static final int TIMEOUT = 30000;
    protected static final int SETUP_TIMEOUT = 20000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    private final String databaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase database;

    private CosmosContainer collection;

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void insertWithUniqueIndex() throws Exception {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.paths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.uniqueKeyPolicy(uniqueKeyPolicy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.indexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath();
        excludedPath.path("/*");
        indexingPolicy.excludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath();
        includedPath1.path("/name/?");
        includedPath1.indexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));

        IncludedPath includedPath2 = new IncludedPath();
        includedPath2.path("/description/?");
        includedPath2.indexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));
        collectionDefinition.indexingPolicy(indexingPolicy);

        ObjectMapper om = new ObjectMapper();

        JsonNode doc1 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", JsonNode.class);
        JsonNode doc2 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"playwright\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);
        JsonNode doc3 = om.readValue("{\"name\":\"حافظ شیرازی\",\"description\":\"poet\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);

        collection = database.createContainer(collectionDefinition).block().container();

        CosmosItem item = collection.createItem(doc1).block().item();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(PartitionKey.None);
        CosmosItemProperties itemSettings = item.read(options).block().properties();
        assertThat(itemSettings.id()).isEqualTo(doc1.get("id").textValue());

        try {
            collection.createItem(doc1).block();
            fail("Did not throw due to unique constraint (create)");
        } catch (RuntimeException e) {
            assertThat(getDocumentClientException(e).statusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }

        collection.createItem(doc2).block();
        collection.createItem(doc3).block();    
    }

    @Test(groups = { "long" }, timeOut = TIMEOUT * 1000)
    public void replaceAndDeleteWithUniqueIndex() throws Exception {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.paths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.uniqueKeyPolicy(uniqueKeyPolicy);

        collection = database.createContainer(collectionDefinition).block().container();

        ObjectMapper om = new ObjectMapper();

        ObjectNode doc1 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);
        ObjectNode doc3 = om.readValue("{\"name\":\"Rabindranath Tagore\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);
        ObjectNode doc2 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"mathematician\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);

        CosmosItemProperties doc1Inserted = collection.createItem(doc1, new CosmosItemRequestOptions()).block().properties();

        collection.getItem(doc1.get("id").asText(), PartitionKey.None).replace(doc1Inserted, new CosmosItemRequestOptions()).block().properties();     // REPLACE with same values -- OK.

        CosmosItemProperties doc2Inserted =  collection.createItem(doc2, new CosmosItemRequestOptions()).block().properties();
        CosmosItemProperties doc2Replacement = new CosmosItemProperties(doc1Inserted.toJson());
        doc2Replacement.id( doc2Inserted.id());

        try {
            collection.getItem(doc2Inserted.id(), PartitionKey.None).replace(doc2Replacement, new CosmosItemRequestOptions()).block(); // REPLACE doc2 with values from doc1 -- Conflict.
            fail("Did not throw due to unique constraint");
        }
        catch (RuntimeException ex) {
            assertThat(getDocumentClientException(ex).statusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }

        doc3.put("id", doc1Inserted.id());
        collection.getItem(doc1Inserted.id(), PartitionKey.None).replace(doc3).block();             // REPLACE with values from doc3 -- OK.

        collection.getItem(doc1Inserted.id(), PartitionKey.None).delete().block();
        collection.createItem(doc1, new CosmosItemRequestOptions()).block();
    }

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void uniqueKeySerializationDeserialization() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.paths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.uniqueKeyPolicy(uniqueKeyPolicy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.indexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath();
        excludedPath.path("/*");
        indexingPolicy.excludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath();
        includedPath1.path("/name/?");
        includedPath1.indexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));

        IncludedPath includedPath2 = new IncludedPath();
        includedPath2.path("/description/?");
        includedPath2.indexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        collectionDefinition.indexingPolicy(indexingPolicy);

        CosmosContainer createdCollection = database.createContainer(collectionDefinition).block().container();

        CosmosContainerSettings collection = createdCollection.read().block().settings();

        assertThat(collection.uniqueKeyPolicy()).isNotNull();
        assertThat(collection.uniqueKeyPolicy().uniqueKeys()).isNotNull();
        assertThat(collection.uniqueKeyPolicy().uniqueKeys())
                .hasSameSizeAs(collectionDefinition.uniqueKeyPolicy().uniqueKeys());
        assertThat(collection.uniqueKeyPolicy().uniqueKeys()
                .stream().map(ui -> ui.paths()).collect(Collectors.toList()))
                .containsExactlyElementsOf(
                        ImmutableList.of(ImmutableList.of("/name", "/description")));
    }

    private CosmosClientException getDocumentClientException(RuntimeException e) {
        CosmosClientException dce = Utils.as(e.getCause(), CosmosClientException.class);
        assertThat(dce).isNotNull();
        return dce;
    }

    @BeforeClass(groups = { "long" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client
        client = CosmosClient.builder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .connectionPolicy(ConnectionPolicy.defaultPolicy())
                .consistencyLevel(ConsistencyLevel.SESSION).build();

        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "long" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
