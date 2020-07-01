// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
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

public class CosmosDiagnosticsTest extends TestSuiteBase {
    private CosmosClient gatewayClient;
    private CosmosClient directClient;
    private CosmosContainer container;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private CosmosClientBuilder cosmosClientBuilder;

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        assertThat(this.gatewayClient).isNull();
        gatewayClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .gatewayMode()
            .buildClient();
        directClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildClient();
        cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(this.gatewayClient.asyncClient());
        container = gatewayClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.gatewayClient).isNotNull();
        this.gatewayClient.close();
        if (this.directClient != null) {
            this.directClient.close();
        }
    }

    @Test(groups = {"simple"})
    public void gatewayDiagnostics() {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = this.container.createItem(internalObjectNode);
        String diagnostics = createResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
        assertThat(diagnostics).doesNotContain(("\"gatewayStatistics\":null"));
        assertThat(diagnostics).contains("\"operationType\":\"Create\"");
        assertThat(diagnostics).contains("\"metaDataName\":\"CONTAINER_LOOK_UP\"");
        assertThat(diagnostics).contains("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
        assertThat(diagnostics).contains("userAgent=" + Utils.getUserAgent());
        assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
        validateTransportRequestTimelineGateway(diagnostics);
    }

    @Test(groups = {"simple"})
    public void gatewayDiagnosticsOnException() {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = null;
        CosmosClient client = null;
        try {
            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .gatewayMode()
                .buildClient();
            CosmosContainer container = client.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            createResponse = container.createItem(internalObjectNode);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            ModelBridgeInternal.setPartitionKey(cosmosItemRequestOptions, new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse<InternalObjectNode> readResponse =
                container.readItem(BridgeInternal.getProperties(createResponse).getId(),
                    new PartitionKey("wrongPartitionKey"),
                    InternalObjectNode.class);
            fail("request should fail as partition key is wrong");
        } catch (CosmosException exception) {
            String diagnostics = exception.getDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
            assertThat(diagnostics).doesNotContain(("\"gatewayStatistics\":null"));
            assertThat(diagnostics).contains("\"statusCode\":404");
            assertThat(diagnostics).contains("\"operationType\":\"Read\"");
            assertThat(diagnostics).contains("userAgent=" + Utils.getUserAgent());
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            validateTransportRequestTimelineGateway(diagnostics);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(groups = {"simple"})
    public void systemDiagnosticsForSystemStateInformation() {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = this.container.createItem(internalObjectNode);
        String diagnostics = createResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("systemInformation");
        assertThat(diagnostics).contains("usedMemory");
        assertThat(diagnostics).contains("availableMemory");
        assertThat(diagnostics).contains("processCpuLoad");
        assertThat(diagnostics).contains("systemCpuLoad");
        assertThat(diagnostics).contains("userAgent=" + Utils.getUserAgent());
        assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
    }

    @Test(groups = {"simple"})
    public void directDiagnostics() {
        CosmosContainer cosmosContainer = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = cosmosContainer.createItem(internalObjectNode);
        String diagnostics = createResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
        assertThat(diagnostics).contains("supplementalResponseStatisticsList");
        assertThat(diagnostics).contains("\"gatewayStatistics\":null");
        assertThat(diagnostics).contains("addressResolutionStatistics");
        assertThat(diagnostics).contains("\"metaDataName\":\"CONTAINER_LOOK_UP\"");
        assertThat(diagnostics).contains("\"metaDataName\":\"PARTITION_KEY_RANGE_LOOK_UP\"");
        assertThat(diagnostics).contains("\"metaDataName\":\"SERVER_ADDRESS_LOOKUP\"");
        assertThat(diagnostics).contains("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
        assertThat(diagnostics).contains("userAgent=" + Utils.getUserAgent());
        assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
        validateTransportRequestTimelineDirect(diagnostics);
    }

    //  TODO: (naveen) - Check the priority
    @Test(groups = {"simple"}, priority = 1, enabled = false)
    public void directDiagnosticsOnException() {
        CosmosContainer cosmosContainer = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = null;
        CosmosClient client = null;
        try {
            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();
            CosmosContainer container = client.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            createResponse = container.createItem(internalObjectNode);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            ModelBridgeInternal.setPartitionKey(cosmosItemRequestOptions, new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse<InternalObjectNode> readResponse =
                cosmosContainer.readItem(BridgeInternal.getProperties(createResponse).getId(),
                    new PartitionKey("wrongPartitionKey"),
                    InternalObjectNode.class);
            fail("request should fail as partition key is wrong");
        } catch (CosmosException exception) {
            String diagnostics = exception.getDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            // TODO https://github.com/Azure/azure-sdk-for-java/issues/8035
            // uncomment below if above issue is fixed
            //validateTransportRequestTimelineDirect(diagnostics);
        } finally {
            if (client != null) {
                client.close();
            }
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

    @Test(groups = {"simple"})
    public void serializationOnVariousScenarios() {
        //checking database serialization
        CosmosDatabaseResponse cosmosDatabase = gatewayClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).read();
        String diagnostics = cosmosDatabase.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"DATABASE_DESERIALIZATION\"");

        //checking container serialization
        CosmosContainerResponse containerResponse = this.container.read();
        diagnostics = containerResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"CONTAINER_DESERIALIZATION\"");
        TestItem testItem = new TestItem();
        testItem.id = "TestId";
        testItem.mypk = "TestPk";

        //checking partitionKeyFetch serialization
        CosmosItemResponse<TestItem> itemResponse = this.container.createItem(testItem);
        diagnostics = itemResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
        testItem.id = "TestId2";
        testItem.mypk = "TestPk";
        itemResponse = this.container.createItem(testItem, new PartitionKey("TestPk"), null);
        diagnostics = itemResponse.getDiagnostics().toString();
        assertThat(diagnostics).doesNotContain("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
        assertThat(diagnostics).doesNotContain("\"serializationType\":\"ITEM_DESERIALIZATION\"");

        //checking item serialization
        TestItem readTestItem = itemResponse.getItem();
        diagnostics = itemResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"ITEM_DESERIALIZATION\"");

        CosmosItemResponse<InternalObjectNode> readItemResponse = this.container.readItem(testItem.id, new PartitionKey(testItem.mypk), null, InternalObjectNode.class);
        InternalObjectNode properties = readItemResponse.getItem();
        diagnostics = readItemResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"ITEM_DESERIALIZATION\"");
        assertThat(diagnostics).contains("userAgent=" + Utils.getUserAgent());
    }

    private InternalObjectNode getInternalObjectNode() {
        InternalObjectNode internalObjectNode = new InternalObjectNode();
        internalObjectNode.setId(UUID.randomUUID().toString());
        BridgeInternal.setProperty(internalObjectNode, "mypk", "test");
        return internalObjectNode;
    }

    private List<ClientSideRequestStatistics.StoreResponseStatistics> getStoreResponseStatistics(ClientSideRequestStatistics requestStatistics) throws Exception {
        Field storeResponseStatisticsField = ClientSideRequestStatistics.class.getDeclaredField("supplementalResponseStatisticsList");
        storeResponseStatisticsField.setAccessible(true);
        @SuppressWarnings({"unchecked"})
        List<ClientSideRequestStatistics.StoreResponseStatistics> list
            = (List<ClientSideRequestStatistics.StoreResponseStatistics>) storeResponseStatisticsField.get(requestStatistics);
        return list;
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

    public static class TestItem {
        public String id;
        public String mypk;

        public TestItem() {
        }
    }
}
