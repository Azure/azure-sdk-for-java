// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.faultinjection.FaultInjectionTestBase;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ThroughputProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosNotFoundTests extends FaultInjectionTestBase {

    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor accessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();

    private CosmosAsyncClient commonAsyncClient;
    private CosmosAsyncContainer existingAsyncContainer;
    private CosmosAsyncDatabase testAsyncDatabase;
    private String createdItemPk;
    private TestObject objectToCreate;

    @Factory(dataProvider = "simpleClientBuildersWithoutRetryOnThrottledRequests")
    public CosmosNotFoundTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosNotFoundTests() {
        this.commonAsyncClient = getClientBuilder().buildAsyncClient();

        // Get shared container and create an item in it
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.commonAsyncClient);
        this.existingAsyncContainer = this.commonAsyncClient.getDatabase(asyncContainer.getDatabase().getId())
            .getContainer(asyncContainer.getId());

        // Get/create test database for this test class
        CosmosAsyncDatabase asyncDatabase = getSharedCosmosDatabase(this.commonAsyncClient);
        this.testAsyncDatabase = this.commonAsyncClient.getDatabase(asyncDatabase.getId());

        // Create a test document
        this.createdItemPk = UUID.randomUUID().toString();

        TestObject testObject = TestObject.create(this.createdItemPk);

        this.existingAsyncContainer.createItem(testObject).block();
        this.objectToCreate = testObject;
    }

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            {OperationType.Read},
            {OperationType.Replace},
            {OperationType.Query},
            {OperationType.ReadFeed}
        };
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.commonAsyncClient);
    }

    @Test(groups = {"fast"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void performDocumentOperationOnNonExistentContainer(OperationType operationType) {

        CosmosAsyncClient asyncClientToUse = getClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        // Try to read the item from a non-existent container
        String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();
        CosmosAsyncContainer nonExistentContainer = asyncClientToUse
            .getDatabase(existingAsyncContainer.getDatabase().getId())
            .getContainer(nonExistentContainerId);

        try {

            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(nonExistentContainer, operationType, this.objectToCreate, false, false, true);

            assertThat(cosmosDiagnostics).isNotNull();
            assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();

            // Verify status code is 404 (Not Found)
            assertThat(diagnosticsContext.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            // Verify sub-status code is either 0 or 1003
            assertThat(diagnosticsContext.getSubStatusCode())
                .as("Sub-status code should be 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                );

        } finally {
            safeClose(asyncClientToUse);
        }
    }

    @Test(groups = {"thin-client-multi-region"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void performDocumentOperationOnNonExistentContainerGatewayModeV2(OperationType operationType) {
        logger.info("Running test: Read item from non-existent container in Gateway Connection Mode");

        // Try to read the item from a non-existent container using Gateway mode client
        String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();

        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        CosmosAsyncClient v2GatewayAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode(new GatewayConnectionConfig().setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(true)))
            .buildAsyncClient();

        CosmosAsyncContainer nonExistentContainer = v2GatewayAsyncClient
            .getDatabase(existingAsyncContainer.getDatabase().getId())
            .getContainer(nonExistentContainerId);

        try {
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(nonExistentContainer, operationType, this.objectToCreate, false, false, true);

            assertThat(cosmosDiagnostics).isNotNull();
            assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();

            // Verify status code is 404 (Not Found)
            assertThat(diagnosticsContext.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            // Verify sub-status code is 1003
            assertThat(diagnosticsContext.getSubStatusCode())
                .as("Sub-status code should be 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                );
        } finally {
            safeClose(v2GatewayAsyncClient);
            System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        }
    }

    @Test(groups = {"fast"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void performDocumentOperationOnDeletedContainer(OperationType operationType) throws InterruptedException {

        // Create a dedicated container for this test
        String testContainerId = "CosmosNotFoundTestsContainer_" + UUID.randomUUID();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(testContainerId, "/mypk");
        testAsyncDatabase.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();

        CosmosAsyncClient clientToUse = getClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        CosmosAsyncContainer testContainer = clientToUse.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);

        Thread.sleep(5000);

        // Create an item in the container
        TestObject testObject = TestObject.create(this.createdItemPk);
        testContainer.createItem(testObject).block();

        // Create a different client instance to delete the container
        CosmosAsyncClient deletingAsyncClient = getClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        try {
            // Delete the container using the different client instance
            CosmosAsyncContainer containerToDelete = deletingAsyncClient.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);
            containerToDelete.delete().block();

            Thread.sleep(5000);

            // Try to read the item from the deleted container using the original client

            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(testContainer, operationType, this.objectToCreate, false, false, true);

            assertThat(cosmosDiagnostics).isNotNull();
            assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();

            // Verify status code is 404 (Not Found)
            assertThat(diagnosticsContext.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            // Verify sub-status code is either 0 or 1003

            if (ConnectionMode.DIRECT.name().equals(accessor.getConnectionMode(clientToUse))) {
                assertThat(diagnosticsContext.getSubStatusCode())
                    .as("Sub-status code should be 1003")
                    .isIn(
                        HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                    );
            }

            if (ConnectionMode.GATEWAY.name().equals(accessor.getConnectionMode(clientToUse))) {
                assertThat(diagnosticsContext.getSubStatusCode())
                    .as("Sub-status code should be 0")
                    .isIn(
                        HttpConstants.SubStatusCodes.UNKNOWN
                    );
            }
        } finally {
            safeClose(clientToUse);
            safeClose(deletingAsyncClient);
        }
    }

    @Test(groups = {"thin-client-multi-region"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void performDocumentOperationOnDeletedContainerWithGatewayV2(OperationType operationType) throws InterruptedException {
        logger.info("Running test: Read item from deleted container - Gateway V2 Connection Mode");

        // Create a dedicated container for this test
        String testContainerId = "CosmosNotFoundTestsContainer_" + UUID.randomUUID();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(testContainerId, "/mypk");
        testAsyncDatabase.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();

        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        Http2ConnectionConfig http2ConnectionConfig = new Http2ConnectionConfig().setEnabled(true);
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        gatewayConnectionConfig.setHttp2ConnectionConfig(http2ConnectionConfig);

        CosmosAsyncClient gatewayV2AsyncClientToUse = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode(gatewayConnectionConfig)
            .buildAsyncClient();
        CosmosAsyncClient containerDeletingAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .buildAsyncClient();

        CosmosAsyncContainer testContainer = gatewayV2AsyncClientToUse.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);

        Thread.sleep(5000);

        // Create an item in the container
        TestObject testObject = TestObject.create(this.createdItemPk);
        testContainer.createItem(testObject).block();

        try {
            // Delete the container using the different client instance
            CosmosAsyncContainer asyncContainerToDelete = containerDeletingAsyncClient.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);
            asyncContainerToDelete.delete().block();

            Thread.sleep(5000);

            // Try to read the item from the deleted container using the original client
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(testContainer, operationType, this.objectToCreate, false, false, true);

            assertThat(cosmosDiagnostics).isNotNull();
            assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = cosmosDiagnostics.getDiagnosticsContext();

            // Verify status code is 404 (Not Found)
            assertThat(diagnosticsContext.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            // Verify sub-status code is either 0 or 1003
            assertThat(diagnosticsContext.getSubStatusCode())
                .as("Sub-status code should be 0 or 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                );

        } finally {
            safeClose(gatewayV2AsyncClientToUse);
            safeClose(containerDeletingAsyncClient);

            System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        }
    }
}
