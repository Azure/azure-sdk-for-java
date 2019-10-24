// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncItem;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosContainerProperties;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.DataType;
import com.azure.cosmos.ExcludedPath;
import com.azure.cosmos.HashIndex;
import com.azure.cosmos.IncludedPath;
import com.azure.cosmos.IndexingMode;
import com.azure.cosmos.IndexingPolicy;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.PartitionKeyDefinition;
import com.azure.cosmos.UniqueKey;
import com.azure.cosmos.UniqueKeyPolicy;
import com.azure.cosmos.internal.HttpConstants;
import com.azure.cosmos.internal.TestConfigurations;
import com.azure.cosmos.internal.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UniqueIndexTest extends TestSuiteBase {
    protected static final int TIMEOUT = 30000;
    protected static final int SETUP_TIMEOUT = 20000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    private final String databaseId = CosmosDatabaseForTest.generateId();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    private CosmosAsyncContainer collection;

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void insertWithUniqueIndex() throws Exception {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Lists.newArrayList(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath();
        excludedPath.setPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath();
        includedPath1.setPath("/name/?");
        includedPath1.setIndexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));
        includedPath1.setIndexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));

        IncludedPath includedPath2 = new IncludedPath();
        includedPath2.setPath("/description/?");
        includedPath2.setIndexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        ObjectMapper om = new ObjectMapper();

        JsonNode doc1 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", JsonNode.class);
        JsonNode doc2 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"playwright\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);
        JsonNode doc3 = om.readValue("{\"name\":\"حافظ شیرازی\",\"description\":\"poet\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);

        collection = database.createContainer(collectionDefinition).block().getContainer();

        CosmosAsyncItem item = collection.createItem(doc1).block().getItem();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(PartitionKey.None);
        CosmosItemProperties itemSettings = item.read(options).block().getProperties();
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

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Lists.newArrayList(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        collection = database.createContainer(collectionDefinition).block().getContainer();

        ObjectMapper om = new ObjectMapper();

        ObjectNode doc1 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);
        ObjectNode doc3 = om.readValue("{\"name\":\"Rabindranath Tagore\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);
        ObjectNode doc2 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"mathematician\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);

        CosmosItemProperties doc1Inserted = collection.createItem(doc1, new CosmosItemRequestOptions()).block().getProperties();

        collection.getItem(doc1.get("id").asText(), PartitionKey.None).replace(doc1Inserted, new CosmosItemRequestOptions()).block().getProperties();     // REPLACE with same values -- OK.

        CosmosItemProperties doc2Inserted =  collection.createItem(doc2, new CosmosItemRequestOptions()).block().getProperties();
        CosmosItemProperties doc2Replacement = new CosmosItemProperties(doc1Inserted.toJson());
        doc2Replacement.setId( doc2Inserted.getId());

        try {
            collection.getItem(doc2Inserted.getId(), PartitionKey.None).replace(doc2Replacement, new CosmosItemRequestOptions()).block(); // REPLACE doc2 with values from doc1 -- Conflict.
            fail("Did not throw due to unique constraint");
        }
        catch (RuntimeException ex) {
            assertThat(getDocumentClientException(ex).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }

        doc3.put("id", doc1Inserted.getId());
        collection.getItem(doc1Inserted.getId(), PartitionKey.None).replace(doc3).block();             // REPLACE with values from doc3 -- OK.

        collection.getItem(doc1Inserted.getId(), PartitionKey.None).delete().block();
        collection.createItem(doc1, new CosmosItemRequestOptions()).block();
    }

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void uniqueKeySerializationDeserialization() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setPaths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Lists.newArrayList(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath();
        excludedPath.setPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath();
        includedPath1.setPath("/name/?");
        includedPath1.setIndexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));

        IncludedPath includedPath2 = new IncludedPath();
        includedPath2.setPath("/description/?");
        includedPath2.setIndexes(Collections.singletonList(new HashIndex(DataType.STRING, 7)));
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        collectionDefinition.setIndexingPolicy(indexingPolicy);

        CosmosAsyncContainer createdCollection = database.createContainer(collectionDefinition).block().getContainer();

        CosmosContainerProperties collection = createdCollection.read().block().getProperties();

        assertThat(collection.getUniqueKeyPolicy()).isNotNull();
        assertThat(collection.getUniqueKeyPolicy().uniqueKeys()).isNotNull();
        assertThat(collection.getUniqueKeyPolicy().uniqueKeys())
                .hasSameSizeAs(collectionDefinition.getUniqueKeyPolicy().uniqueKeys());
        assertThat(collection.getUniqueKeyPolicy().uniqueKeys()
                .stream().map(ui -> ui.getPaths()).collect(Collectors.toList()))
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
        client = CosmosAsyncClient.builder()
                .setEndpoint(TestConfigurations.HOST)
                .setKey(TestConfigurations.MASTER_KEY)
                .setConnectionPolicy(ConnectionPolicy.getDefaultPolicy())
                .setConsistencyLevel(ConsistencyLevel.SESSION).buildAsyncClient();

        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "long" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
