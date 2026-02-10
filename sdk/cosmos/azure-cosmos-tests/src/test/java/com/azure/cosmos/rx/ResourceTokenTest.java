// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for resource token functionality using public APIs.
 */
public class ResourceTokenTest extends TestSuiteBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PARTITION_KEY_PATH = "/mypk";
    private static final String USER_NAME = "TestUser";

    private final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdContainer;
    private CosmosAsyncUser createdUser;
    private CosmosPermissionProperties createdPermission;
    private ObjectNode createdDocument;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public ResourceTokenTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readDocumentWithPermission() {
        // Use the resource token to create a new client
        String resourceToken = createdPermission.getToken();
        assertThat(resourceToken).isNotNull();
        assertThat(resourceToken).isNotEmpty();

        // Create a client with the resource token
        CosmosAsyncClient resourceTokenClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .resourceToken(resourceToken)
            .buildAsyncClient();

        try {
            CosmosAsyncContainer containerWithToken = resourceTokenClient
                .getDatabase(databaseId)
                .getContainer(createdContainer.getId());

            // Read the document using resource token
            String partitionKeyValue = createdDocument.get("mypk").asText();
            CosmosItemResponse<ObjectNode> response = containerWithToken
                .readItem(
                    createdDocument.get("id").asText(),
                    new PartitionKey(partitionKeyValue),
                    ObjectNode.class
                )
                .block();

            assertThat(response).isNotNull();
            assertThat(response.getItem()).isNotNull();
            assertThat(response.getItem().get("id").asText()).isEqualTo(createdDocument.get("id").asText());
        } finally {
            resourceTokenClient.close();
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void createAndReadPermission() {
        // Create another permission
        String permissionId = "TestPermission_" + UUID.randomUUID().toString();
        CosmosPermissionProperties permissionProperties = new CosmosPermissionProperties()
            .setId(permissionId)
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdContainer.getId());

        CosmosPermissionProperties createdPerm = createdUser
            .createPermission(permissionProperties, null)
            .block()
            .getProperties();

        assertThat(createdPerm).isNotNull();
        assertThat(createdPerm.getId()).isEqualTo(permissionId);
        assertThat(createdPerm.getPermissionMode()).isEqualTo(PermissionMode.READ);
        assertThat(createdPerm.getToken()).isNotNull();

        // Read the permission
        CosmosPermissionProperties readPerm = createdUser
            .getPermission(permissionId)
            .read(null)
            .block()
            .getProperties();

        assertThat(readPerm).isNotNull();
        assertThat(readPerm.getId()).isEqualTo(permissionId);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void replacePermission() {
        // Create a permission with Read mode
        String permissionId = "PermToReplace_" + UUID.randomUUID().toString();
        CosmosPermissionProperties permissionProperties = new CosmosPermissionProperties()
            .setId(permissionId)
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdContainer.getId());

        createdUser.createPermission(permissionProperties, null).block();

        // Replace with All mode
        permissionProperties.setPermissionMode(PermissionMode.ALL);
        CosmosPermissionProperties replacedPerm = createdUser
            .getPermission(permissionId)
            .replace(permissionProperties, null)
            .block()
            .getProperties();

        assertThat(replacedPerm).isNotNull();
        assertThat(replacedPerm.getPermissionMode()).isEqualTo(PermissionMode.ALL);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void deletePermission() {
        // Create a permission
        String permissionId = "PermToDelete_" + UUID.randomUUID().toString();
        CosmosPermissionProperties permissionProperties = new CosmosPermissionProperties()
            .setId(permissionId)
            .setPermissionMode(PermissionMode.READ)
            .setContainerName(createdContainer.getId());

        createdUser.createPermission(permissionProperties, null).block();

        // Delete the permission
        createdUser.getPermission(permissionId).delete(null).block();

        // Verify deletion - reading should fail
        try {
            createdUser.getPermission(permissionId).read(null).block();
            fail("Should have thrown exception when reading deleted permission");
        } catch (Exception e) {
            // Expected - permission was deleted
            assertThat(e).isNotNull();
        }
    }

    @BeforeClass(groups = { "fast" }, timeOut = SETUP_TIMEOUT)
    public void before_ResourceTokenTest() {
        client = getClientBuilder().buildAsyncClient();
        client.createDatabase(databaseId).block();
        createdDatabase = client.getDatabase(databaseId);

        // Create container
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(
            UUID.randomUUID().toString(),
            PARTITION_KEY_PATH
        );
        createdDatabase.createContainer(
            containerProperties,
            ThroughputProperties.createManualThroughput(10100),
            new CosmosContainerRequestOptions()
        ).block();
        createdContainer = createdDatabase.getContainer(containerProperties.getId());

        // Create a document
        String partitionKeyValue = UUID.randomUUID().toString();
        ObjectNode document = OBJECT_MAPPER.createObjectNode();
        document.put("id", UUID.randomUUID().toString());
        document.put("mypk", partitionKeyValue);
        document.put("prop", "value");

        createdDocument = createdContainer.createItem(document).block().getItem();

        // Create user
        CosmosUserProperties userProperties = new CosmosUserProperties();
        userProperties.setId(USER_NAME + "_" + UUID.randomUUID().toString());
        createdDatabase.createUser(userProperties).block();
        createdUser = createdDatabase.getUser(userProperties.getId());

        // Create permission for reading the container
        CosmosPermissionProperties permissionProperties = new CosmosPermissionProperties()
            .setId("PermissionForContainer")
            .setPermissionMode(PermissionMode.ALL)
            .setContainerName(createdContainer.getId())
            .setResourcePartitionKey(new PartitionKey(partitionKeyValue));

        createdPermission = createdUser
            .createPermission(permissionProperties, null)
            .block()
            .getProperties();
    }

    @AfterClass(groups = { "fast" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}
