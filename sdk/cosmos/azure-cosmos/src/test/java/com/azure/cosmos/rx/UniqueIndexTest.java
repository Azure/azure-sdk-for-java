// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.UniqueKey;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        UniqueKey uniqueKey = new UniqueKey(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.setUniqueKeys(Lists.newArrayList(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath("/name/?");

        IncludedPath includedPath2 = new IncludedPath("/description/?");
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        ObjectMapper om = new ObjectMapper();

        JsonNode doc1 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", JsonNode.class);
        JsonNode doc2 = om.readValue("{\"name\":\"Alexander Pushkin\",\"description\":\"playwright\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);
        JsonNode doc3 = om.readValue("{\"name\":\"حافظ شیرازی\",\"description\":\"poet\",\"id\": \"" + UUID.randomUUID().toString() + "\"}", JsonNode.class);

        database.createContainer(collectionDefinition).block();
        collection = database.getContainer(collectionDefinition.getId());

        InternalObjectNode properties = BridgeInternal.getProperties(collection.createItem(doc1).block());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        InternalObjectNode itemSettings =
            BridgeInternal.getProperties(
                collection.readItem(properties.getId(), PartitionKey.NONE, options, InternalObjectNode.class)
                                             .block());
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
        UniqueKey uniqueKey = new UniqueKey(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.setUniqueKeys(Lists.newArrayList(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        database.createContainer(collectionDefinition).block();
        collection = database.getContainer(collectionDefinition.getId());

        ObjectMapper om = new ObjectMapper();

        ObjectNode doc1 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);
        ObjectNode doc3 = om.readValue("{\"name\":\"Rabindranath Tagore\",\"description\":\"poet\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);
        ObjectNode doc2 = om.readValue("{\"name\":\"عمر خیّام\",\"description\":\"mathematician\",\"id\": \""+ UUID.randomUUID().toString() +"\"}", ObjectNode.class);

        InternalObjectNode doc1Inserted =
            BridgeInternal.getProperties(collection.createItem(doc1, new CosmosItemRequestOptions()).block());

        BridgeInternal.getProperties(collection.replaceItem(doc1Inserted, doc1.get("id").asText(), PartitionKey.NONE, new CosmosItemRequestOptions())
            .block());     // REPLACE with same values -- OK.

        InternalObjectNode doc2Inserted = BridgeInternal.getProperties(collection
                                                                             .createItem(doc2, new CosmosItemRequestOptions())
                                                                             .block());
        InternalObjectNode doc2Replacement = new InternalObjectNode(ModelBridgeInternal.toJsonFromJsonSerializable(doc1Inserted));
        doc2Replacement.setId( doc2Inserted.getId());

        try {
            collection.replaceItem(doc2Replacement, doc2Inserted.getId(), PartitionKey.NONE,
                               new CosmosItemRequestOptions()).block(); // REPLACE doc2 with values from doc1 -- Conflict.
            fail("Did not throw due to unique constraint");
        }
        catch (RuntimeException ex) {
            assertThat(getDocumentClientException(ex).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }

        doc3.put("id", doc1Inserted.getId());
        collection.replaceItem(doc3, doc1Inserted.getId(), PartitionKey.NONE).block();             // REPLACE with values from doc3 -- OK.

        collection.deleteItem(doc1Inserted.getId(), PartitionKey.NONE).block();
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
        UniqueKey uniqueKey = new UniqueKey(ImmutableList.of("/name", "/description"));
        uniqueKeyPolicy.setUniqueKeys(Lists.newArrayList(uniqueKey));
        collectionDefinition.setUniqueKeyPolicy(uniqueKeyPolicy);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath("/name/?");

        IncludedPath includedPath2 = new IncludedPath("/description/?");
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        collectionDefinition.setIndexingPolicy(indexingPolicy);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer createdCollection = database.getContainer(collectionDefinition.getId());

        CosmosContainerProperties collection = createdCollection.read().block().getProperties();

        assertThat(collection.getUniqueKeyPolicy()).isNotNull();
        assertThat(collection.getUniqueKeyPolicy().getUniqueKeys()).isNotNull();
        assertThat(collection.getUniqueKeyPolicy().getUniqueKeys())
                .hasSameSizeAs(collectionDefinition.getUniqueKeyPolicy().getUniqueKeys());
        assertThat(collection.getUniqueKeyPolicy().getUniqueKeys()
                .stream().map(ui -> ui.getPaths()).collect(Collectors.toList()))
                .containsExactlyElementsOf(
                        ImmutableList.of(ImmutableList.of("/name", "/description")));
    }

    private CosmosException getDocumentClientException(RuntimeException e) {
        CosmosException dce = Utils.as(e, CosmosException.class);
        assertThat(dce).isNotNull();
        return dce;
    }

    @BeforeClass(groups = { "long" }, timeOut = SETUP_TIMEOUT)
    public void before_UniqueIndexTest() {
        // set up the client
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(DirectConnectionConfig.getDefaultConfig())
            .consistencyLevel(ConsistencyLevel.SESSION)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "long" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }
}
