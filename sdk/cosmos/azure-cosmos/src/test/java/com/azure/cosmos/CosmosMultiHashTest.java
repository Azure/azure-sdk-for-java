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
        logger.info("starting cleanup....");
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
    private void validateDocCRUDandQuery() throws Exception {

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

            Document doc3 = new Document();
            doc3.setId(UUID.randomUUID().toString());
            doc3.set("city", "Stonybrook");
            doc3.set("zipcode", "11794");
            docs.add(doc3);

            Document doc4 = new Document();
            doc4.setId(UUID.randomUUID().toString());
            doc4.set("city", "Stonybrook");
            doc4.set("zipcode", "11791");
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
                    CosmosItemResponse<Document> response =
                        createdMultiHashContainer.createItem(doc, partitionKey, new CosmosItemRequestOptions());
                }
                catch (Exception e) {
                    assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.\n" ));
                }
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
                }
            }

            // Query Tests.

            for (int i = 0; i < docs.size(); i++) {
                Document doc_current = docs.get(i);
                //Build the partition key
                PartitionKey partitionKey = new PartitionKeyBuilder()
                    .add(doc_current.getString("city"))
                    .add(doc_current.getString("zipcode"))
                    .build();

                //Build the query request options
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                queryRequestOptions.setPartitionKey(partitionKey);

                //Run the query.
                String query = String.format("SELECT * from c where c.id = '%s'", doc_current.getId());

                CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
                    createdMultiHashContainer.queryItems(query, queryRequestOptions, InternalObjectNode.class);
                assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

                query = String.format("SELECT * from c where c.id = '%s'", doc_current.getId());
                queryRequestOptions = new CosmosQueryRequestOptions();
                feedResponseIterator1 =
                    createdMultiHashContainer.queryItems(query, queryRequestOptions, InternalObjectNode.class);
                assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();
            }

            String query = String.format("SELECT * from c where c.city = '%s'", docs.get(2).getString("city"));
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
                createdMultiHashContainer.queryItems(query, queryRequestOptions, InternalObjectNode.class);
            assertThat(feedResponseIterator1.stream().count()).isEqualTo(3);

        }
        catch (Exception e)
        {
            assertThat(false);
        }

    }
}
