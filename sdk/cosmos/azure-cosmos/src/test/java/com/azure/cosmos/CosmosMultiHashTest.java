/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.*;

import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;
    private CosmosContainer createdMultiHashContainer;

    @Factory(dataProvider = "clientBuilders")
    public CosmosMultiHashTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {

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

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        //MultiHash Collection delete
        createdMultiHashContainer.delete();
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
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

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    private void validateQueryForMultiHash() throws Exception {

        try {
            ArrayList<Document> docs = new ArrayList<Document>(3);

            Document doc = new Document();
            doc.setId(UUID.randomUUID().toString());
            doc.set("city", "Redmond");
            doc.set("zipcode", "98052");
            docs.add(doc);

            Document doc1 = new Document();
            doc1.setId(UUID.randomUUID().toString());
            doc1.set("city", "Pittsburgh");
            doc1.set("zipcode", "15232");
            docs.add(doc1);

            Document doc2 = new Document();
            doc2.setId(UUID.randomUUID().toString());
            doc2.set("city", "Stonybrook");
            doc2.set("zipcode", "11790");
            docs.add(doc2);

            //Document Create
            {
                createdMultiHashContainer.createItem(doc);
                createdMultiHashContainer.createItem(doc1);
                createdMultiHashContainer.createItem(doc2);
            }
            //Document Create - Negative test
            {
                PartitionKey partitionKey =
                    new PartitionKeyBuilder()
                        .add("Redmond")
                        .build();
                CosmosItemResponse<Document> response =
                    createdMultiHashContainer.createItem(doc, partitionKey, new CosmosItemRequestOptions());

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
            }

            //Document Read
            {
                for (int i = 0; i < docs.size(); i++) {
                    Document doc_current = docs.get(i);
                    PartitionKey partitionKey = new PartitionKeyBuilder()
                        .add(doc_current.getString("city"))
                        .add(doc_current.getString("zipcode"))
                        .build();
                    CosmosItemResponse<Document> response = createdMultiHashContainer.readItem(doc_current.getId(), partitionKey, Document.class);
                    assertThat(doc_current.toJson()).isEqualTo(response.getItem().toJson());
                }
            }


        }
        catch (Exception e)
        {
            assertThat(false);
        }

    }
}
