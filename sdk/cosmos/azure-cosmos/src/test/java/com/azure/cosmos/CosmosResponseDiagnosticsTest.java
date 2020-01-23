// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosResponseDiagnosticsTest extends TestSuiteBase {
    private CosmosClient gatewayClient;
    private CosmosClient directClient;
    private CosmosContainer container;
    private CosmosAsyncContainer cosmosAsyncContainer;

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        assertThat(this.gatewayClient).isNull();
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .setEndpoint(TestConfigurations.HOST)
            .setKey(TestConfigurations.MASTER_KEY);
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.GATEWAY);
        gatewayClient = cosmosClientBuilder.setConnectionPolicy(connectionPolicy).buildClient();
        connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DIRECT);
        directClient = cosmosClientBuilder.setConnectionPolicy(connectionPolicy).buildClient();
        cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(this.gatewayClient.asyncClient());
        container = gatewayClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() throws CosmosClientException {
        assertThat(this.gatewayClient).isNotNull();
        this.gatewayClient.close();
        if (this.directClient != null) {
            this.directClient.close();
        }
    }

    @Test(groups = {"simple"})
    public void gatewayDiagnostics() throws CosmosClientException {
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse<CosmosItemProperties> createResponse = container.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("Connection Mode : " + ConnectionMode.GATEWAY);
        assertThat(diagnostics).contains("Gateway statistics");
        assertThat(diagnostics).contains("Operation Type : " + OperationType.Create);
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
    }

    @Test(groups = {"simple"})
    public void gatewayDiagnosticsOnException() throws CosmosClientException {
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse<CosmosItemProperties> createResponse = null;
        try {
            createResponse = this.container.createItem(cosmosItemProperties);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            cosmosItemRequestOptions.setPartitionKey(new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse<CosmosItemProperties> readResponse =
                this.container.readItem(createResponse.getProperties().getId(), 
                                        new PartitionKey("wrongPartitionKey"), 
                                        CosmosItemProperties.class);
            fail("request should fail as partition key is wrong");
        } catch (CosmosClientException exception) {
            String diagnostics = exception.getCosmosResponseDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("Connection Mode : " + ConnectionMode.GATEWAY);
            assertThat(diagnostics).contains("Gateway statistics");
            assertThat(diagnostics).contains("Status Code : 404");
            assertThat(diagnostics).contains("Operation Type : " + OperationType.Read);
            assertThat(exception.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        }
    }

    @Test(groups = {"simple"})
    public void systemDiagnosticsForSystemStateInformation() throws CosmosClientException {
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse<CosmosItemProperties> createResponse = this.container.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("System State Information ------");
        assertThat(diagnostics).contains("Used Memory :");
        assertThat(diagnostics).contains("Available Memory :");
        assertThat(diagnostics).contains("CPU Process Load :");
        assertThat(diagnostics).contains("CPU System Load :");
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
    }

    @Test(groups = {"simple"})
    public void directDiagnostics() throws CosmosClientException {
        CosmosContainer cosmosContainer = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse<CosmosItemProperties> createResponse = cosmosContainer.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("Connection Mode : " + ConnectionMode.DIRECT);
        assertThat(diagnostics).contains("StoreResponseStatistics");
        assertThat(diagnostics).doesNotContain("Gateway request URI :");
        assertThat(diagnostics).contains("AddressResolutionStatistics");
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
    }

    @Test(groups = {"simple"})
    public void directDiagnosticsOnException() throws CosmosClientException {
        CosmosContainer cosmosContainer = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse<CosmosItemProperties> createResponse = null;
        try {
            createResponse = this.container.createItem(cosmosItemProperties);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            cosmosItemRequestOptions.setPartitionKey(new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse<CosmosItemProperties> readResponse =
                cosmosContainer.readItem(createResponse.getProperties().getId(), 
                                        new PartitionKey("wrongPartitionKey"),
                                        CosmosItemProperties.class);
            fail("request should fail as partition key is wrong");
        } catch (CosmosClientException exception) {
            String diagnostics = exception.getCosmosResponseDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("Connection Mode : " + ConnectionMode.DIRECT);
            assertThat(exception.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        }
    }

    private CosmosItemProperties getCosmosItemProperties() {
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(UUID.randomUUID().toString());
        cosmosItemProperties.set("mypk", "test");
        return cosmosItemProperties;
    }
}
