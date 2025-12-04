// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosNotFoundTests extends TestSuiteBase {

    private CosmosClient clientDirect;
    private CosmosClient clientGateway;
    private CosmosContainer existingContainer;
    private CosmosDatabase testDatabase;
    private String createdItemId;
    private String createdItemPk;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public CosmosNotFoundTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "connectionModeProvider")
    public static Object[][] connectionModeProvider() {
        return new Object[][] {
            { ConnectionMode.DIRECT, "Direct Connection Mode" },
//            { ConnectionMode.GATEWAY, "Gateway Connection Mode" }
        };
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosNotFoundTests() {
        // Create Direct mode client
        assertThat(this.clientDirect).isNull();
        this.clientDirect = getClientBuilder()
            .directMode()
            .buildClient();

        // Create Gateway mode client
        assertThat(this.clientGateway).isNull();
        this.clientGateway = getClientBuilder()
            .gatewayMode()
            .buildClient();

        // Get shared container and create an item in it
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.clientDirect.asyncClient());
        existingContainer = clientDirect.getDatabase(asyncContainer.getDatabase().getId())
            .getContainer(asyncContainer.getId());

        // Get/create test database for this test class
        CosmosAsyncDatabase asyncDatabase = getSharedCosmosDatabase(this.clientDirect.asyncClient());
        testDatabase = clientDirect.getDatabase(asyncDatabase.getId());

        // Create a test document
        this.createdItemId = UUID.randomUUID().toString();
        this.createdItemPk = UUID.randomUUID().toString();
        InternalObjectNode properties = getDocumentDefinition(createdItemId, createdItemPk);
        existingContainer.createItem(properties);
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        if (this.clientDirect != null) {
            this.clientDirect.close();
        }
        if (this.clientGateway != null) {
            this.clientGateway.close();
        }
    }

    @Test(groups = {"fast"}, dataProvider = "connectionModeProvider", timeOut = TIMEOUT)
    public void readItemFromNonExistentContainer(ConnectionMode connectionMode, String testDescription) {
        logger.info("Running test: {}", testDescription);

        CosmosClient client = connectionMode == ConnectionMode.DIRECT ? clientDirect : clientGateway;

        // Try to read the item from a non-existent container
        String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();
        CosmosContainer nonExistentContainer = client
            .getDatabase(existingContainer.asyncContainer.getDatabase().getId())
            .getContainer(nonExistentContainerId);

        try {
            nonExistentContainer.readItem(
                createdItemId,
                new PartitionKey(createdItemPk),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class
            );
            fail("Expected CosmosException to be thrown when reading from non-existent container");
        } catch (CosmosException e) {
            logger.info("CosmosException caught for {}: StatusCode={}, SubStatusCode={}",
                testDescription, e.getStatusCode(), e.getSubStatusCode());

            // Verify status code is 404 (Not Found)
            assertThat(e.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            // Verify sub-status code is either 0 or 1003
            assertThat(e.getSubStatusCode())
                .as("Sub-status code should be 0 or 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                );

            // Log the diagnostics for debugging purposes
            logger.info("Diagnostics: {}", e.getDiagnostics());
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readItemFromNonExistentContainerDirectMode() {
        logger.info("Running test: Read item from non-existent container in Direct Connection Mode");

        // Try to read the item from a non-existent container using Direct mode client
        String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();
        CosmosContainer nonExistentContainer = clientDirect
            .getDatabase(existingContainer.asyncContainer.getDatabase().getId())
            .getContainer(nonExistentContainerId);

        try {
            nonExistentContainer.readItem(
                createdItemId,
                new PartitionKey(createdItemPk),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class
            );
            fail("Expected CosmosException to be thrown when reading from non-existent container");
        } catch (CosmosException e) {
            logger.info("CosmosException caught (Direct): StatusCode={}, SubStatusCode={}",
                e.getStatusCode(), e.getSubStatusCode());

            assertThat(e.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            assertThat(e.getSubStatusCode())
                .as("Sub-status code should be 0 or 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                );

            logger.info("Diagnostics: {}", e.getDiagnostics());
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readItemFromNonExistentContainerGatewayMode() {
        logger.info("Running test: Read item from non-existent container in Gateway Connection Mode");

        // Try to read the item from a non-existent container using Gateway mode client
        String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();
        CosmosContainer nonExistentContainer = clientGateway
            .getDatabase(existingContainer.asyncContainer.getDatabase().getId())
            .getContainer(nonExistentContainerId);

        try {
            nonExistentContainer.readItem(
                createdItemId,
                new PartitionKey(createdItemPk),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class
            );
            fail("Expected CosmosException to be thrown when reading from non-existent container");
        } catch (CosmosException e) {
            logger.info("CosmosException caught (Gateway): StatusCode={}, SubStatusCode={}",
                e.getStatusCode(), e.getSubStatusCode());

            assertThat(e.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            assertThat(e.getSubStatusCode())
                .as("Sub-status code should be 0 or 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    1003
                );

            logger.info("Diagnostics: {}", e.getDiagnostics());
        }
    }

    @Test(groups = {"fast"}, dataProvider = "connectionModeProvider", timeOut = TIMEOUT)
    public void readItemFromDeletedContainer(ConnectionMode connectionMode, String testDescription) throws InterruptedException {
        logger.info("Running test: Read item from deleted container - {}", testDescription);

        // Create a dedicated container for this test
        String testContainerId = "CosmosNotFoundTestsContainer_" + UUID.randomUUID();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(testContainerId, "/mypk");
        testDatabase.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400));

        CosmosClient clientToUse = connectionMode == ConnectionMode.DIRECT ? clientDirect : clientGateway;
        CosmosContainer testContainer = clientToUse.getDatabase(testDatabase.getId()).getContainer(testContainerId);

        Thread.sleep(5000);

        // Create an item in the container
        String itemId = UUID.randomUUID().toString();
        String itemPk = UUID.randomUUID().toString();
        InternalObjectNode doc = getDocumentDefinition(itemId, itemPk);
        testContainer.createItem(doc);

        // Create a different client instance to delete the container
        CosmosClient deletingClient = connectionMode == ConnectionMode.DIRECT
            ? getClientBuilder().directMode().buildClient()
            : getClientBuilder().gatewayMode().buildClient();

        try {
            // Delete the container using the different client instance
            CosmosContainer containerToDelete = deletingClient.getDatabase(testDatabase.getId()).getContainer(testContainerId);
            containerToDelete.delete();

            Thread.sleep(5000);

            // Try to read the item from the deleted container using the original client
            try {
                testContainer.readItem(
                    itemId,
                    new PartitionKey(itemPk),
                    new CosmosItemRequestOptions(),
                    InternalObjectNode.class
                );
                fail("Expected CosmosException to be thrown when reading from deleted container");
            } catch (CosmosException e) {
                logger.info("CosmosException caught for deleted container test ({}): StatusCode={}, SubStatusCode={}",
                    testDescription, e.getStatusCode(), e.getSubStatusCode());

                // Verify status code is 404 (Not Found)
                assertThat(e.getStatusCode())
                    .as("Status code should be 404 (Not Found)")
                    .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

                // Verify sub-status code is either 0 or 1003
                assertThat(e.getSubStatusCode())
                    .as("Sub-status code should be 0 or 1003")
                    .isIn(
                        HttpConstants.SubStatusCodes.UNKNOWN,
                        1003
                    );

                logger.info("Diagnostics: {}", e.getDiagnostics());
            }
        } finally {
            deletingClient.close();
        }
    }

    private InternalObjectNode getDocumentDefinition(String documentId, String pkId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties = new InternalObjectNode(String.format(
            "{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]], "
                + "\"prop\": \"%s\""
                + "}"
            , documentId, pkId, uuid));
        return properties;
    }
}
