// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.faultinjection.FaultInjectionTestBase;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import org.assertj.core.api.AssertionsForClassTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class CosmosNotFoundTests extends FaultInjectionTestBase {

    private static final Logger logger = LoggerFactory.getLogger(CosmosNotFoundTests.class);

    private static final String thinClientEndpointIndicator = ":10250/";
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

    @BeforeClass(groups = {"fast", "thinclient"}, timeOut = SETUP_TIMEOUT)
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

    @AfterClass(groups = {"fast", "thinclient"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.commonAsyncClient);
    }

    @Test(groups = {"fast"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void performDocumentOperationOnNonExistentContainer(OperationType operationType) {

        CosmosAsyncClient asyncClientToUse = null;

        try {
            asyncClientToUse = getClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            // Try to read the item from a non-existent container
            String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();
            CosmosAsyncContainer nonExistentContainer = asyncClientToUse
                .getDatabase(existingAsyncContainer.getDatabase().getId())
                .getContainer(nonExistentContainerId);

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

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void performBulkOnNonExistentContainer() {

        CosmosAsyncClient asyncClientToUse = null;
        try {
            asyncClientToUse = getClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            // Try to read the item from a non-existent container
            String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();
            CosmosAsyncContainer nonExistentContainer = asyncClientToUse
                .getDatabase(existingAsyncContainer.getDatabase().getId())
                .getContainer(nonExistentContainerId);

            List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

            CosmosItemOperation cosmosItemOperation = CosmosBulkOperations.getReadItemOperation(
                this.objectToCreate.getId(),
                new PartitionKey(this.createdItemPk),
                TestObject.class);

            cosmosItemOperations.add(cosmosItemOperation);

            Flux<CosmosItemOperation> operationsFlux = Flux.fromIterable(cosmosItemOperations);

            nonExistentContainer.executeBulkOperations(operationsFlux).blockLast();

            fail("Bulk operation on non-existent container should have failed.");
        } catch (CosmosException ce) {

            // Verify status code is 404 (Not Found)
            assertThat(ce.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            // Verify sub-status code is either 0 or 1003
            assertThat(ce.getSubStatusCode())
                .as("Sub-status code should be 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                );
        } finally {
            safeClose(asyncClientToUse);
        }
    }

    @Test(groups = {"thinclient"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void performDocumentOperationOnNonExistentContainerGatewayModeV2(OperationType operationType) {
        logger.info("Running test: Read item from non-existent container in Gateway Connection Mode");

        CosmosAsyncClient v2GatewayAsyncClient = null;

        try {

            // Try to read the item from a non-existent container using Gateway mode client
            String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();

            // Uncomment if running locally
            // System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

            v2GatewayAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .gatewayMode(new GatewayConnectionConfig().setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(true)))
                .buildAsyncClient();

            CosmosAsyncContainer nonExistentContainer = v2GatewayAsyncClient
                .getDatabase(existingAsyncContainer.getDatabase().getId())
                .getContainer(nonExistentContainerId);

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

            // Uncomment if running locally
            // System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void performBulkOnNonExistentContainerGatewayModeV2() {
        logger.info("Running test: Read item from non-existent container in Gateway Connection Mode");

        CosmosAsyncClient v2GatewayAsyncClient = null;

        try {

            // Try to read the item from a non-existent container using Gateway mode client
            String nonExistentContainerId = "NonExistentContainer_" + UUID.randomUUID();

            // Uncomment if running locally
            // System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

            v2GatewayAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .gatewayMode(new GatewayConnectionConfig().setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(true)))
                .buildAsyncClient();

            CosmosAsyncContainer nonExistentContainer = v2GatewayAsyncClient
                .getDatabase(existingAsyncContainer.getDatabase().getId())
                .getContainer(nonExistentContainerId);

            List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

            CosmosItemOperation cosmosItemOperation = CosmosBulkOperations.getReadItemOperation(
                this.objectToCreate.getId(),
                new PartitionKey(this.createdItemPk),
                TestObject.class);

            cosmosItemOperations.add(cosmosItemOperation);

            Flux<CosmosItemOperation> operationsFlux = Flux.fromIterable(cosmosItemOperations);

            nonExistentContainer.executeBulkOperations(operationsFlux).blockLast();

            fail("Bulk operation on non-existent container should have failed.");
        } catch (CosmosException ce) {

            // Verify status code is 404 (Not Found)
            assertThat(ce.getStatusCode())
                .as("Status code should be 404 (Not Found)")
                .isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            // Verify sub-status code is either 0 or 1003
            assertThat(ce.getSubStatusCode())
                .as("Sub-status code should be 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                );
        } finally {
            safeClose(v2GatewayAsyncClient);

            // Uncomment if running locally
            // System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        }
    }

    @Test(groups = {"fast"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void performDocumentOperationOnDeletedContainer(OperationType operationType) throws InterruptedException {

        CosmosAsyncClient clientToUse = null, deletingAsyncClient = null;

        try {
            // Create a dedicated container for this test
            String testContainerId = "CosmosNotFoundTestsContainer_" + UUID.randomUUID();
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(testContainerId, "/mypk");
            testAsyncDatabase.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();

            clientToUse = getClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            CosmosAsyncContainer testContainer = clientToUse.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);

            Thread.sleep(5000);

            // Create an item in the container
            TestObject testObject = TestObject.create(this.createdItemPk);
            testContainer.createItem(testObject).block();

            // Create a different client instance to delete the container
            deletingAsyncClient = getClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            // Delete the container using the different client instance
            CosmosAsyncContainer containerToDelete = deletingAsyncClient.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);
            containerToDelete.delete().block();

            Thread.sleep(15_000);

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
                if (diagnosticsContext.getSubStatusCode() != HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS) {
                    logger.error("CosmosNotFoundTests-performDocumentOperationOnDeletedContainer {}", diagnosticsContext.toJson());
                }
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

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void performBulkOnDeletedContainer() throws InterruptedException {

        CosmosAsyncClient clientToUse = null, deletingAsyncClient = null;

        try {
            // Create a dedicated container for this test
            String testContainerId = "CosmosNotFoundTestsContainer_" + UUID.randomUUID();
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(testContainerId, "/mypk");
            testAsyncDatabase.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();

            clientToUse = getClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            CosmosAsyncContainer containerToUse = clientToUse.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);

            Thread.sleep(5000);

            // Create an item in the container
            TestObject testObject = TestObject.create(this.createdItemPk);
            containerToUse.createItem(testObject).block();

            // Create a different client instance to delete the container
            deletingAsyncClient = getClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            // Delete the container using the different client instance
            CosmosAsyncContainer containerToDelete = deletingAsyncClient.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);
            containerToDelete.delete().block();

            Thread.sleep(5000);

            // Try to read the item from the deleted container using the original client

            List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

            CosmosItemOperation cosmosItemOperation = CosmosBulkOperations.getReadItemOperation(
                this.objectToCreate.getId(),
                new PartitionKey(this.createdItemPk),
                TestObject.class);

            cosmosItemOperations.add(cosmosItemOperation);

            Flux<CosmosItemOperation> operationsFlux = Flux.fromIterable(cosmosItemOperations);

            CosmosBulkOperationResponse<Object> response = containerToUse.executeBulkOperations(operationsFlux).blockLast();

            assertThat(response).isNotNull();
            assertThat(response.getException()).isNotNull();

            Exception e = response.getException();

            assertThat(e).isInstanceOf(CosmosException.class);

            CosmosException ce = Utils.as(e, CosmosException.class);

            if (ConnectionMode.DIRECT.name().equals(accessor.getConnectionMode(clientToUse))) {
                assertThat(ce.getSubStatusCode())
                    .as("Sub-status code should be 1003")
                    .isIn(HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS);
            }

            if (ConnectionMode.GATEWAY.name().equals(accessor.getConnectionMode(clientToUse))) {
                assertThat(ce.getSubStatusCode())
                    .as("Sub-status code should be 0")
                    .isIn(HttpConstants.SubStatusCodes.UNKNOWN);
            }
        } catch (CosmosException ce) {

            assertThat(ce.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);

            if (ConnectionMode.DIRECT.name().equals(accessor.getConnectionMode(clientToUse))) {
                assertThat(ce.getSubStatusCode())
                .as("Sub-status code should be 1003")
                .isIn(HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS);
            }

            if (ConnectionMode.GATEWAY.name().equals(accessor.getConnectionMode(clientToUse))) {
                assertThat(ce.getSubStatusCode())
                .as("Sub-status code should be 0")
                .isIn(HttpConstants.SubStatusCodes.UNKNOWN);
            }
        } finally {
            safeClose(clientToUse);
            safeClose(deletingAsyncClient);
        }
    }

    @Test(groups = {"thinclient"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void performDocumentOperationOnDeletedContainerWithGatewayV2(OperationType operationType) throws InterruptedException {
        logger.info("Running test: Read item from deleted container - Gateway V2 Connection Mode");

        CosmosAsyncClient gatewayV2AsyncClientToUse = null, containerDeletingAsyncClient = null;

        try {
            // Create a dedicated container for this test
            String testContainerId = "CosmosNotFoundTestsContainer_" + UUID.randomUUID();
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(testContainerId, "/mypk");
            testAsyncDatabase.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();

            // Uncomment if running locally
            // System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            Http2ConnectionConfig http2ConnectionConfig = new Http2ConnectionConfig().setEnabled(true);
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setHttp2ConnectionConfig(http2ConnectionConfig);

            gatewayV2AsyncClientToUse = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .gatewayMode(gatewayConnectionConfig)
                .buildAsyncClient();

            containerDeletingAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .gatewayMode()
                .buildAsyncClient();

            CosmosAsyncContainer testContainer = gatewayV2AsyncClientToUse.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);

            Thread.sleep(5000);

            // Create an item in the container
            TestObject testObject = TestObject.create(this.createdItemPk);
            testContainer.createItem(testObject).block();

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

            if (!OperationType.Query.equals(operationType)) {
                assertThinClientEndpointUsed(cosmosDiagnostics);
            }
        } finally {
            safeClose(gatewayV2AsyncClientToUse);
            safeClose(containerDeletingAsyncClient);

            // Uncomment if running locally
            // System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void performBulkOnDeletedContainerWithGatewayV2() throws InterruptedException {
        logger.info("Running test: Read item from deleted container - Gateway V2 Connection Mode");

        CosmosAsyncClient gatewayV2AsyncClientToUse = null, containerDeletingAsyncClient = null;

        try {

            // Create a dedicated container for this test
            String testContainerId = "CosmosNotFoundTestsContainer_" + UUID.randomUUID();
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(testContainerId, "/mypk");
            testAsyncDatabase.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();

            // Uncomment if running locally
            // System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            Http2ConnectionConfig http2ConnectionConfig = new Http2ConnectionConfig().setEnabled(true);
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setHttp2ConnectionConfig(http2ConnectionConfig);

            gatewayV2AsyncClientToUse = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .gatewayMode(gatewayConnectionConfig)
                .buildAsyncClient();

            containerDeletingAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .gatewayMode()
                .buildAsyncClient();

            CosmosAsyncContainer containerToUse = gatewayV2AsyncClientToUse.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);

            Thread.sleep(5000);

            // Create an item in the container
            TestObject testObject = TestObject.create(this.createdItemPk);
            containerToUse.createItem(testObject).block();

            // Delete the container using the different client instance
            CosmosAsyncContainer asyncContainerToDelete = containerDeletingAsyncClient.getDatabase(testAsyncDatabase.getId()).getContainer(testContainerId);
            asyncContainerToDelete.delete().block();

            Thread.sleep(5000);

            // Try to read the item from the deleted container using the original client

            List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

            CosmosItemOperation cosmosItemOperation = CosmosBulkOperations.getReadItemOperation(
                this.objectToCreate.getId(),
                new PartitionKey(this.createdItemPk),
                TestObject.class);

            cosmosItemOperations.add(cosmosItemOperation);

            Flux<CosmosItemOperation> operationsFlux = Flux.fromIterable(cosmosItemOperations);

            CosmosBulkOperationResponse<Object> response = containerToUse.executeBulkOperations(operationsFlux).blockLast();

            assertThat(response).isNotNull();
            assertThat(response.getException()).isNotNull();

            Exception ce = response.getException();

            assertThat(ce).isInstanceOf(CosmosException.class);

            CosmosException cosmosException = Utils.as(ce, CosmosException.class);

            assertThat(cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS);
        } catch (CosmosException ce) {
            assertThat(ce.getSubStatusCode())
                .as("Sub-status code should be 1003")
                .isIn(
                    HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
                );

            assertThinClientEndpointUsed(ce.getDiagnostics());
        } finally {
            safeClose(gatewayV2AsyncClientToUse);
            safeClose(containerDeletingAsyncClient);

            // Uncomment if running locally
            // System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        }
    }

    private static void assertThinClientEndpointUsed(CosmosDiagnostics diagnostics) {
        AssertionsForClassTypes.assertThat(diagnostics).isNotNull();

        CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
        AssertionsForClassTypes.assertThat(ctx).isNotNull();

        Collection<CosmosDiagnosticsRequestInfo> requests = ctx.getRequestInfo();
        AssertionsForClassTypes.assertThat(requests).isNotNull();
        AssertionsForClassTypes.assertThat(requests.size()).isPositive();

        for (CosmosDiagnosticsRequestInfo requestInfo : requests) {
            logger.info(
                "Endpoint: {}, RequestType: {}, Partition: {}/{}, ActivityId: {}",
                requestInfo.getEndpoint(),
                requestInfo.getRequestType(),
                requestInfo.getPartitionId(),
                requestInfo.getPartitionKeyRangeId(),
                requestInfo.getActivityId());
            if (requestInfo.getEndpoint().contains(thinClientEndpointIndicator)) {
                return;
            }
        }

        fail("No request targeting thin client proxy endpoint.");
    }
}
