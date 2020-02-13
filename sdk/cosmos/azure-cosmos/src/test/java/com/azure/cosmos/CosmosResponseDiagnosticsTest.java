// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
        assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
        assertThat(diagnostics).doesNotContain(("\"gatewayStatistics\":null"));
        assertThat(diagnostics).contains("\"operationType\":\"Create\"");
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        validateTransportRequestTimelineGateway(diagnostics);
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
            assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
            assertThat(diagnostics).doesNotContain(("\"gatewayStatistics\":null"));
            assertThat(diagnostics).contains("\"statusCode\":404");
            assertThat(diagnostics).contains("\"operationType\":\"Read\"");
            assertThat(exception.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
            validateTransportRequestTimelineGateway(diagnostics);
        }
    }

    @Test(groups = {"simple"})
    public void systemDiagnosticsForSystemStateInformation() throws CosmosClientException {
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse<CosmosItemProperties> createResponse = this.container.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("systemInformation");
        assertThat(diagnostics).contains("usedMemory");
        assertThat(diagnostics).contains("availableMemory");
        assertThat(diagnostics).contains("processCpuLoad");
        assertThat(diagnostics).contains("systemCpuLoad");
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
    }

    @Test(groups = {"simple"})
    public void directDiagnostics() throws CosmosClientException {
        CosmosContainer cosmosContainer = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        CosmosItemProperties cosmosItemProperties = getCosmosItemProperties();
        CosmosItemResponse<CosmosItemProperties> createResponse = cosmosContainer.createItem(cosmosItemProperties);
        String diagnostics = createResponse.getCosmosResponseDiagnostics().toString();
        assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
        assertThat(diagnostics).contains("supplementalResponseStatisticsList");
        assertThat(diagnostics).contains("\"gatewayStatistics\":null");
        assertThat(diagnostics).contains("addressResolutionStatistics");
        assertThat(createResponse.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
        validateTransportRequestTimelineDirect(diagnostics);
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
            assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
            assertThat(exception.getCosmosResponseDiagnostics().getRequestLatency()).isNotNull();
            // TODO https://github.com/Azure/azure-sdk-for-java/issues/8035
            // uncomment below if above issue is fixed
            //validateTransportRequestTimelineDirect(diagnostics);
        }
    }

    @Test(groups = {"simple"})
    public void supplementalResponseStatisticsList() throws Exception {
        ClientSideRequestStatistics clientSideRequestStatistics = new ClientSideRequestStatistics();
        for (int i = 0; i < 15; i++) {
            RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(OperationType.Head, ResourceType.Document);
            clientSideRequestStatistics.recordResponse(rxDocumentServiceRequest, null);
        }
        List<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseStatistics = getStoreResponseStatistics(clientSideRequestStatistics);
        ObjectMapper objectMapper = new ObjectMapper();
        String diagnostics = objectMapper.writeValueAsString(clientSideRequestStatistics);
        JsonNode jsonNode = objectMapper.readTree(diagnostics);
        ArrayNode supplementalResponseStatisticsListNode = (ArrayNode) jsonNode.get("supplementalResponseStatisticsList");
        assertThat(storeResponseStatistics.size()).isEqualTo(15);
        assertThat(supplementalResponseStatisticsListNode.size()).isEqualTo(10);

        clearStoreResponseStatistics(clientSideRequestStatistics);
        storeResponseStatistics = getStoreResponseStatistics(clientSideRequestStatistics);
        assertThat(storeResponseStatistics.size()).isEqualTo(0);
        for (int i = 0; i < 7; i++) {
            RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(OperationType.Head, ResourceType.Document);
            clientSideRequestStatistics.recordResponse(rxDocumentServiceRequest, null);
        }
        storeResponseStatistics = getStoreResponseStatistics(clientSideRequestStatistics);
        objectMapper = new ObjectMapper();
        diagnostics = objectMapper.writeValueAsString(clientSideRequestStatistics);
        jsonNode = objectMapper.readTree(diagnostics);
        supplementalResponseStatisticsListNode = (ArrayNode) jsonNode.get("supplementalResponseStatisticsList");
        assertThat(storeResponseStatistics.size()).isEqualTo(7);
        assertThat(supplementalResponseStatisticsListNode.size()).isEqualTo(7);
    }

    private CosmosItemProperties getCosmosItemProperties() {
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(UUID.randomUUID().toString());
        BridgeInternal.setProperty(cosmosItemProperties, "mypk", "test");
        return cosmosItemProperties;
    }

    private List<ClientSideRequestStatistics.StoreResponseStatistics> getStoreResponseStatistics(ClientSideRequestStatistics requestStatistics) throws Exception {
        Field storeResponseStatisticsField = ClientSideRequestStatistics.class.getDeclaredField("supplementalResponseStatisticsList");
        storeResponseStatisticsField.setAccessible(true);
        return (List<ClientSideRequestStatistics.StoreResponseStatistics>) storeResponseStatisticsField.get(requestStatistics);
    }

    private void clearStoreResponseStatistics(ClientSideRequestStatistics requestStatistics) throws Exception {
        Field storeResponseStatisticsField = ClientSideRequestStatistics.class.getDeclaredField("supplementalResponseStatisticsList");
        storeResponseStatisticsField.setAccessible(true);
        storeResponseStatisticsField.set(requestStatistics, new ArrayList<ClientSideRequestStatistics.StoreResponseStatistics>());
    }

    private void validateTransportRequestTimelineGateway(String diagnostics) {
        assertThat(diagnostics).contains("\"eventName\":\"connectionConfigured\"");
        assertThat(diagnostics).contains("\"eventName\":\"connectionConfigured\"");
        assertThat(diagnostics).contains("\"eventName\":\"requestSent\"");
        assertThat(diagnostics).contains("\"eventName\":\"transitTime\"");
        assertThat(diagnostics).contains("\"eventName\":\"received\"");
    }

    private void validateTransportRequestTimelineDirect(String diagnostics) {
        assertThat(diagnostics).contains("\"eventName\":\"created\"");
        assertThat(diagnostics).contains("\"eventName\":\"queued\"");
        assertThat(diagnostics).contains("\"eventName\":\"pipelined\"");
        assertThat(diagnostics).contains("\"eventName\":\"transitTime\"");
        assertThat(diagnostics).contains("\"eventName\":\"received\"");
        assertThat(diagnostics).contains("\"eventName\":\"completed\"");
    }
}
