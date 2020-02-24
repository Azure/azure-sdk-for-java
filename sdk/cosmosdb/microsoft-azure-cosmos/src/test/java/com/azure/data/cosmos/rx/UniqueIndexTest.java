// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.DataType;
import com.azure.data.cosmos.ExcludedPath;
import com.azure.data.cosmos.HashIndex;
import com.azure.data.cosmos.IncludedPath;
import com.azure.data.cosmos.IndexingMode;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.UniqueKey;
import com.azure.data.cosmos.UniqueKeyPolicy;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.TestConfigurations;
import com.azure.data.cosmos.internal.TestUtils;
import com.azure.data.cosmos.internal.Utils;
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
    private CosmosClient client;
    private CosmosDatabase database;

    private CosmosContainer collection;

    @Test(groups = { "long" }, timeOut = TIMEOUT)
    public void insertWithUniqueIndex() throws Exception {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.paths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Lists.newArrayList(uniqueKey));
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

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.paths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Lists.newArrayList(uniqueKey));
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

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        UniqueKeyPolicy uniqueKeyPolicy = new UniqueKeyPolicy();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.paths(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.uniqueKeys(Lists.newArrayList(uniqueKey));
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

        CosmosContainerProperties collection = createdCollection.read().block().properties();

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
