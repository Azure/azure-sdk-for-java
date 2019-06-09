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

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosContainerRequestOptions;
import com.microsoft.azure.cosmos.CosmosContainerSettings;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosDatabaseForTest;
import com.microsoft.azure.cosmos.CosmosItem;
import com.microsoft.azure.cosmos.CosmosItemRequestOptions;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.ExcludedPath;
import com.microsoft.azure.cosmosdb.HashIndex;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.UniqueKey;
import com.microsoft.azure.cosmosdb.UniqueKeyPolicy;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;

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
        partitionKeyDef.setPaths(paths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
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

        ObjectMapper om = new ObjectMapper();

        JsonNode doc1 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", JsonNode.class);
        JsonNode doc2 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"playwright\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);
        JsonNode doc3 = om.readValue("{\"name\":\"حافظ شیرازی\",\"description\":\"poet\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);

        collection = database.createContainer(collectionDefinition).block().getContainer();

        CosmosItem item = collection.createItem(doc1).block().getCosmosItem();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(PartitionKey.None);
        CosmosItemSettings itemSettings = item.read(options).block().getCosmosItemSettings();
        assertThat(itemSettings.getId()).isEqualTo(doc1.get("id").textValue());

        try {
            collection.createItem(doc1).block();
            fail("Did not throw due to unique constraint (create)");
        } catch (RuntimeException e) {
            assertThat(getDocumentClientException(e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }

        collection.createItem(doc2).block();
        collection.createItem(doc3).block();    
    }

    @Test(groups = { "long" }, timeOut = TIMEOUT * 1000)
    public void replaceAndDeleteWithUniqueIndex() throws Exception {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.setUniqueKeys(Collections.singleton(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        collection = database.createContainer(collectionDefinition).block().getContainer();

        ObjectMapper om = new ObjectMapper();

        ObjectNode doc1 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);
        ObjectNode doc3 = om.readValue("{\"name\":\"Rabindranath Tagore\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);
        ObjectNode doc2 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"mathematician\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);

        CosmosItemSettings doc1Inserted = collection.createItem(doc1, new CosmosItemRequestOptions()).block().getCosmosItemSettings();

        collection.getItem(doc1.get("id").asText(), PartitionKey.None).replace(doc1Inserted, new CosmosItemRequestOptions()).block().getCosmosItemSettings();     // Replace with same values -- OK.

        CosmosItemSettings doc2Inserted =  collection.createItem(doc2, new CosmosItemRequestOptions()).block().getCosmosItemSettings();
        CosmosItemSettings doc2Replacement = new CosmosItemSettings(doc1Inserted.toJson());
        doc2Replacement.setId( doc2Inserted.getId());

        try {
            collection.getItem(doc2Inserted.getId(), PartitionKey.None).replace(doc2Replacement, new CosmosItemRequestOptions()).block(); // Replace doc2 with values from doc1 -- Conflict.
            fail("Did not throw due to unique constraint");
        }
        catch (RuntimeException ex) {
            assertThat(getDocumentClientException(ex).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }

        doc3.put("id", doc1Inserted.getId());
        collection.getItem(doc1Inserted.getId(), PartitionKey.None).replace(doc3).block();             // Replace with values from doc3 -- OK.

        collection.getItem(doc1Inserted.getId(), PartitionKey.None).delete().block();
        collection.createItem(doc1, new CosmosItemRequestOptions()).block();
    }

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void uniqueKeySerializationDeserialization() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
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

        CosmosContainer createdCollection = database.createContainer(collectionDefinition).block().getContainer();

        CosmosContainerSettings collection = createdCollection.read().block().getCosmosContainerSettings();

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

    @BeforeClass(groups = { "long" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client
        client = CosmosClient.builder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .connectionPolicy(ConnectionPolicy.GetDefault())
                .consistencyLevel(ConsistencyLevel.Session).build();

        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "long" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
