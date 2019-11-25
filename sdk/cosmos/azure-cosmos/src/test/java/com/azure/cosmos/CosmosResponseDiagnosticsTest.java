// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResult;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosResponseDiagnosticsTest extends TestSuiteBase {
    private CosmosClient gatewayClient;
    private CosmosClient directClient;
    private CosmosContainer container;
    private CosmosAsyncContainer cosmosAsyncContainer;

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
        connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DIRECT);
        directClient = clientBuilder().setConnectionPolicy(connectionPolicy).buildClient();
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
        CosmosItemResponse createResponse = container.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
        assertThat(diagnostics).contains("gatewayStatistics");
        assertThat(diagnostics).contains("\"operationType\":\"Create\"");
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        deleteItem(createResponse);
    }

    @Test(groups = {"simple"})
    public void gatewayDiagnosticsOnException() throws CosmosClientException {
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse createResponse = null;
        try {
            createResponse = this.container.createItem(cosmosItemProperties);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            cosmosItemRequestOptions.setPartitionKey(new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse readResponse = this.container.getItem(createResponse.getItem().getId(), null).read(cosmosItemRequestOptions);
            fail("request should fail as partition key is wrong");
        } catch (CosmosClientException exception) {
            String diagnostics = exception.getCosmosResponseDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
            assertThat(diagnostics).contains("gatewayStatistics");
            assertThat(diagnostics).contains("\"statusCode\":404");
            assertThat(diagnostics).contains("\"operationType\":\"Read\"");
            assertThat(exception.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
            System.out.println(diagnostics);
        } finally {
            deleteItem(createResponse);
        }
    }

    @Test(groups = {"simple"})
    public void systemDiagnosticsForSystemStateInformation() throws CosmosClientException {
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse createResponse = this.container.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("systemInformation");
        assertThat(diagnostics).contains("usedMemory");
        assertThat(diagnostics).contains("availableMemory");
        assertThat(diagnostics).contains("processCpuLoad");
        assertThat(diagnostics).contains("systemCpuLoad");
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        deleteItem(createResponse);
    }

    @Test(groups = {"simple"})
    public void directDiagnostics() throws CosmosClientException {
        CosmosContainer cosmosContainer = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse createResponse = cosmosContainer.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
        assertThat(diagnostics).contains("responseStatisticsList");
        assertThat(diagnostics).contains("\"gatewayStatistics\":null");
        assertThat(diagnostics).contains("addressResolutionStatistics");
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        deleteItem(createResponse);
    }

    @Test(groups = {"simple"})
    public void directDiagnosticsOnException() throws CosmosClientException {
        CosmosContainer cosmosContainer = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse createResponse = null;
        try {
            createResponse = this.container.createItem(cosmosItemProperties);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            cosmosItemRequestOptions.setPartitionKey(new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse readResponse = cosmosContainer.getItem(createResponse.getItem().getId(), null).read(cosmosItemRequestOptions);
            fail("request should fail as partition key is wrong");
        } catch (CosmosClientException exception) {
            String diagnostics = exception.getCosmosResponseDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
            assertThat(exception.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        } finally {
            deleteItem(createResponse);
        }
    }

    @Test(groups = {"simple"})
    public void supplementalResponseStatisticsList(){
        ClientSideRequestStatistics clientSideRequestStatistics = new ClientSideRequestStatistics();
        for(int i = 0;i < 15 ;i++) {
            RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(OperationType.Head, ResourceType.Document);
            clientSideRequestStatistics.recordResponse(rxDocumentServiceRequest, null);
        }
        assertThat(clientSideRequestStatistics.supplementalResponseStatisticsList.size()).isEqualTo(15);
        clientSideRequestStatistics.toString();
        assertThat(clientSideRequestStatistics.supplementalResponseStatisticsList.size()).isEqualTo(10);
        clientSideRequestStatistics.toString();
        assertThat(clientSideRequestStatistics.supplementalResponseStatisticsList.size()).isEqualTo(10);

        clientSideRequestStatistics.supplementalResponseStatisticsList.clear();
        assertThat(clientSideRequestStatistics.supplementalResponseStatisticsList.size()).isEqualTo(0);
        for(int i = 0;i < 7 ;i++) {
            RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(OperationType.Head, ResourceType.Document);
            clientSideRequestStatistics.recordResponse(rxDocumentServiceRequest, null);
        }
        clientSideRequestStatistics.toString();
        assertThat(clientSideRequestStatistics.supplementalResponseStatisticsList.size()).isEqualTo(7);
    }

    private CosmosItemProperties getCosmosItemProperties() {
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(UUID.randomUUID().toString());
        cosmosItemProperties.set("mypk", "test");
        return cosmosItemProperties;
    }

    private void deleteItem(CosmosItemResponse cosmosItemResponse) throws CosmosClientException {
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        cosmosItemRequestOptions.setPartitionKey(new PartitionKey("test"));
        cosmosItemResponse.getItem().delete(cosmosItemRequestOptions);
    }
}
