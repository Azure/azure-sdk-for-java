/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.*;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

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
    public void itemCRUDTest() throws Exception {

        List<String> pkIds = new ArrayList<>();
        pkIds.add("Redmond");
        pkIds.add("98052");

        Object[] pkValue = new Object[2];
        pkValue[0] = pkIds.get(0);
        pkValue[1] = pkIds.get(1);

        PartitionKey partitionKey = new PartitionKey(pkValue);

        String documentId = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(documentId, pkIds);
        createdMultiHashContainer.createItem(properties);


        CosmosItemResponse<ObjectNode> readResponse1 = createdMultiHashContainer.readItem(
                                                        documentId, partitionKey, ObjectNode.class);

        validateIdOfItemResponse(documentId, readResponse1);
        assertThat(readResponse1.getItem().equals(properties));
    }

    private ObjectNode getDocumentDefinition(String documentId, List<String> pkIds) throws JsonProcessingException {

        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"city\": \"%s\", "
                + "\"zipcode\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
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
}
