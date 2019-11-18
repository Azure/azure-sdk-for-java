// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosResponseDiagnosticsTest extends TestSuiteBase {
    private CosmosClient gatewayClient;
    private CosmosDatabase createdDatabase;
    private CosmosContainer cosmosContainer;
    private CosmosClient directClient;

    private final static String DATABASE_NAME = "TestDiagnosticDB";
    private final static String CONTAINER_NAME = "TestDiagnosticContainer";

    @Factory(dataProvider = "clientBuilders")
    public CosmosResponseDiagnosticsTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        assertThat(this.gatewayClient).isNull();
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.GATEWAY);
        gatewayClient = clientBuilder().setConnectionPolicy(connectionPolicy).buildClient();
        createdDatabase = TestSuiteBase.createSyncDatabase(gatewayClient, DATABASE_NAME);
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(CONTAINER_NAME, "/id");
        cosmosContainer = createdDatabase.createContainer(cosmosContainerProperties).getContainer();
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() throws CosmosClientException {
        assertThat(this.gatewayClient).isNotNull();
        createdDatabase.delete();
        this.gatewayClient.close();
        if (this.directClient != null) {
            this.directClient.close();
        }
    }

    @Test(groups = {"simple"})
    public void testGatewayDiagnostics() {
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(UUID.randomUUID().toString());
        try {
            CosmosItemResponse createResponse = cosmosContainer.createItem(cosmosItemProperties);
            String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
            assertThat(diagnostics).contains("Connection Mode : " + ConnectionMode.GATEWAY);
            assertThat(diagnostics).contains("Gateway request URI :");
            assertThat(diagnostics).contains("Operation Type :" + OperationType.Create);
            assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        } catch (CosmosClientException e) {
            fail(e.getMessage());
        }
    }

    @Test(groups = {"simple"})
    public void testGatewayDiagnosticsOnException() {
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(UUID.randomUUID().toString());
        try {
            CosmosItemResponse createResponse = this.cosmosContainer.createItem(cosmosItemProperties);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            cosmosItemRequestOptions.setPartitionKey(new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse readResponse = this.cosmosContainer.getItem(createResponse.getItem().getId(), null).read(cosmosItemRequestOptions);
            fail("request should fail as partition key is wrong");
        } catch (CosmosClientException exception) {
            String diagnostics = exception.getCosmosResponseDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("Connection Mode : " + ConnectionMode.GATEWAY);
            assertThat(diagnostics).contains("Gateway request URI :");
            assertThat(diagnostics).contains("Operation Type :" + OperationType.Read);
            assertThat(exception.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
            System.out.println(diagnostics);
        }
    }

    @Test(groups = {"simple"})
    public void testSystemDiagnosticsForSystemStateInformation() throws CosmosClientException {
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(UUID.randomUUID().toString());
        CosmosItemResponse createResponse = this.cosmosContainer.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("System State Information ------");
        assertThat(diagnostics).contains("Used Memory :");
        assertThat(diagnostics).contains("Available Memory :");
        assertThat(diagnostics).contains("CPU Process Load :");
        assertThat(diagnostics).contains("CPU System Load :");

        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
    }

    @Test(groups = {"simple"})
    public void testDirectDiagnostics() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DIRECT);
        CosmosClient directClient = clientBuilder().setConnectionPolicy(connectionPolicy).buildClient();
        CosmosContainer cosmosContainer = directClient.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(UUID.randomUUID().toString());
        try {
            CosmosItemResponse createResponse = cosmosContainer.createItem(cosmosItemProperties);
            String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
            assertThat(diagnostics).contains("Connection Mode : " + ConnectionMode.DIRECT);
            assertThat(diagnostics).contains("StoreResponseStatistics");
            assertThat(diagnostics).doesNotContain("Gateway request URI :");
            assertThat(diagnostics).contains("AddressResolutionStatistics");
            assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        } catch (CosmosClientException e) {
            fail(e.getMessage());
        }
    }

    @Test(groups = {"simple"})
    public void testDirectDiagnosticsOnException() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DIRECT);
        CosmosClient directClient = clientBuilder().setConnectionPolicy(connectionPolicy).buildClient();
        CosmosContainer cosmosContainer = directClient.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(UUID.randomUUID().toString());
        try {
            CosmosItemResponse createResponse = this.cosmosContainer.createItem(cosmosItemProperties);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            cosmosItemRequestOptions.setPartitionKey(new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse readResponse = this.cosmosContainer.getItem(createResponse.getItem().getId(), null).read(cosmosItemRequestOptions);
            fail("request should fail as partition key is wrong");
        } catch (CosmosClientException exception) {
            String diagnostics = exception.getCosmosResponseDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("Connection Mode : " + ConnectionMode.GATEWAY);
            assertThat(diagnostics).contains("Gateway request URI :");
            assertThat(diagnostics).contains("Operation Type :" + OperationType.Read);
            assertThat(exception.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        }
    }
}
