/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosMultiHashTest extends TestSuiteBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final JsonNodeFactory JSON_NODE_FACTORY_INSTANCE = JsonNodeFactory.withExactBigDecimals(true);

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;
    private CosmosContainer createdMultiHashContainer;

    @Factory(dataProvider = "clientBuilders")
    public CosmosMultiHashTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosMultiHashTest() {

        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
        String collectionName = UUID.randomUUID().toString();

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setKind(PartitionKind.MULTI_HASH);
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/city");
        paths.add("/zipcode");
        partitionKeyDefinition.setPaths(paths);

        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName, partitionKeyDefinition);

        //MultiHash collection create
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);

        //MultiHash collection read
        createdMultiHashContainer = createdDatabase.getContainer(collectionName);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting cleanup....");
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void itemCRUD() throws Exception {

        List<String> pkIds = new ArrayList<>();
        pkIds.add("Redmond");
        pkIds.add("98052");

        PartitionKey partitionKey =
            new PartitionKeyBuilder()
            .add(pkIds.get(0))
            .add(pkIds.get(1))
            .build();

        String documentId = UUID.randomUUID().toString();
        ObjectNode properties =  getItem(documentId, pkIds);
         createdMultiHashContainer.createItem(properties);

        CosmosItemResponse<ObjectNode> readResponse1 = createdMultiHashContainer.readItem(
                                                        documentId, partitionKey, ObjectNode.class);
        validateIdOfItemResponse(documentId, readResponse1);
        assertThat(readResponse1.getItem().equals(properties));
    }

    private ObjectNode getItem(String documentId, List<String> pkIds) throws JsonProcessingException {


        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"city\": \"%s\", "
                + "\"zipcode\": \"%s\" "
                + "}"
            , documentId, pkIds.get(0), pkIds.get(1));
        return
            OBJECT_MAPPER.readValue(json, ObjectNode.class);
    }

    private void validateItemResponse(InternalObjectNode containerProperties,
                                      CosmosItemResponse<InternalObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateIdOfItemResponse(String expectedId, CosmosItemResponse<ObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(expectedId);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    private void validateDocCRUDandQuery() throws Exception {

        ArrayList<ObjectNode> docs = new ArrayList<ObjectNode>(3);

        ObjectNode doc = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc.set("id", new TextNode(UUID.randomUUID().toString()));
        doc.set("city", new TextNode("Redmond"));
        doc.set("zipcode", new TextNode("98052"));
        docs.add(doc);

        ObjectNode doc1 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc1.set("id", new TextNode(UUID.randomUUID().toString()));
        doc1.set("city", new TextNode("Pittsburgh"));
        doc1.set("zipcode", new TextNode("15232"));
        docs.add(doc1);

        ObjectNode doc2 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc2.set("id", new TextNode(UUID.randomUUID().toString()));
        doc2.set("city", new TextNode("Stonybrook"));
        doc2.set("zipcode", new TextNode("11790"));
        docs.add(doc2);

        ObjectNode doc3 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc3.set("id", new TextNode(UUID.randomUUID().toString()));
        doc3.set("city", new TextNode("Stonybrook"));
        doc3.set("zipcode", new TextNode("11794"));
        docs.add(doc3);

        ObjectNode doc4 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc4.set("id", new TextNode(UUID.randomUUID().toString()));
        doc4.set("city", new TextNode("Stonybrook"));
        doc4.set("zipcode", new TextNode("11791"));
        docs.add(doc4);

        //Document Create
        for (int i = 0; i < docs.size(); i++) {
            createdMultiHashContainer.createItem(docs.get(i));
        }
        //Document Create - Negative test
        {
            PartitionKey partitionKey =
                new PartitionKeyBuilder()
                    .add("Redmond")
                    .build();
            try {
                CosmosItemResponse<ObjectNode> response =
                    createdMultiHashContainer.createItem(doc, partitionKey, new CosmosItemRequestOptions());
            } catch (Exception e) {
                assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.\n"));
            }
        }

        //Document Read
        {
            for (int i = 0; i < docs.size(); i++) {
                ObjectNode doc_current = docs.get(i);
                PartitionKey partitionKey = new PartitionKeyBuilder()
                    .add(doc_current.get("city").asText())
                    .add(doc_current.get("zipcode").asText())
                    .build();
                CosmosItemResponse<ObjectNode> response = createdMultiHashContainer.readItem(doc_current.get("id").asText(), partitionKey, ObjectNode.class);
            }
        }

        // Query Tests.

        for (int i = 0; i < docs.size(); i++) {
            ObjectNode doc_current = docs.get(i);
            //Build the partition key
            PartitionKey partitionKey = new PartitionKeyBuilder()
                .add(doc_current.get("city").asText())
                .add(doc_current.get("zipcode").asText())
                .build();

            //Build the query request options
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setPartitionKey(partitionKey);

            //Run the query.
            String query = String.format("SELECT * from c where c.id = '%s'", doc_current.get("id").asText());

            CosmosPagedIterable<ObjectNode> feedResponseIterator1 =
                createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
            assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

            query = String.format("SELECT * from c where c.id = '%s'", doc_current.get("id").asText());
            queryRequestOptions = new CosmosQueryRequestOptions();
            feedResponseIterator1 =
                createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
            assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();
        }

        String query = String.format("SELECT * from c where c.city = '%s'", docs.get(2).get("city").asText());
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedIterable<ObjectNode> feedResponseIterator1 =
            createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator1.stream().count()).isEqualTo(3);

    }
}
