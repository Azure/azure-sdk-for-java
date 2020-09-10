// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosDiagnosticsTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter RESPONSE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss" + ".SSS").withLocale(Locale.US).withZone(ZoneOffset.UTC);
    private CosmosClient gatewayClient;
    private CosmosClient directClient;
    private CosmosContainer container;
    private CosmosAsyncContainer cosmosAsyncContainer;

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

    @DataProvider(name = "query")
    private Object[][] query() {
        return new Object[][]{
            new Object[] { "Select * from c where c.id = 'wrongId'", true },
            new Object[] { "Select top 1 * from c where c.id = 'wrongId'", true },
            new Object[] { "Select * from c where c.id = 'wrongId' order by c.id", true },
            new Object[] { "Select count(1) from c where c.id = 'wrongId' group by c.pk", true },
            new Object[] { "Select distinct c.pk from c where c.id = 'wrongId'", true },
            new Object[] { "Select * from c where c.id = 'wrongId'", false },
            new Object[] { "Select top 1 * from c where c.id = 'wrongId'", false },
            new Object[] { "Select * from c where c.id = 'wrongId' order by c.id", false },
            new Object[] { "Select count(1) from c where c.id = 'wrongId' group by c.pk", false },
            new Object[] { "Select distinct c.pk from c where c.id = 'wrongId'", false },
            new Object[] { "Select * from c where c.id = 'wrongId'", false },
            new Object[] { "Select top 1 * from c where c.id = 'wrongId'", false },
            new Object[] { "Select * from c where c.id = 'wrongId' order by c.id", false },
            new Object[] { "Select count(1) from c where c.id = 'wrongId' group by c.pk", false },
            new Object[] { "Select distinct c.pk from c where c.id = 'wrongId'", false },
        };
    }

    @DataProvider(name = "readAllItemsOfLogicalPartition")
    private Object[][] readAllItemsOfLogicalPartition() {
        return new Object[][]{
            new Object[] { 1, true },
            new Object[] { 5, null },
            new Object[] { 20, null },
            new Object[] { 1, false },
            new Object[] { 5, false },
            new Object[] { 20, false },
        };
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void gatewayDiagnostics() {
        CosmosClient testGatewayClient = null;
        try {
            testGatewayClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .gatewayMode()
                .buildClient();
            CosmosContainer container =
                testGatewayClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            CosmosItemResponse<InternalObjectNode> createResponse = container.createItem(internalObjectNode);
            String diagnostics = createResponse.getDiagnostics().toString();
            assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
            assertThat(diagnostics).doesNotContain(("\"gatewayStatistics\":null"));
            assertThat(diagnostics).contains("\"operationType\":\"Create\"");
            assertThat(diagnostics).contains("\"metaDataName\":\"CONTAINER_LOOK_UP\"");
            assertThat(diagnostics).contains("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
            assertThat(diagnostics).contains("\"userAgent\":\"" + Utils.getUserAgent() + "\"");
            assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
            validateTransportRequestTimelineGateway(diagnostics);
            validateJson(diagnostics);
        } finally {
            if (testGatewayClient != null) {
                testGatewayClient.close();
            }
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void gatewayDiagnosticsOnException() {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = null;
        try {
            createResponse = this.container.createItem(internalObjectNode);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            ModelBridgeInternal.setPartitionKey(cosmosItemRequestOptions, new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse<InternalObjectNode> readResponse =
                this.container.readItem(BridgeInternal.getProperties(createResponse).getId(),
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
            assertThat(diagnostics).contains("\"userAgent\":\"" + Utils.getUserAgent() + "\"");
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            validateTransportRequestTimelineGateway(diagnostics);
            validateJson(diagnostics);
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void systemDiagnosticsForSystemStateInformation() {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = this.container.createItem(internalObjectNode);
        String diagnostics = createResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("systemInformation");
        assertThat(diagnostics).contains("usedMemory");
        assertThat(diagnostics).contains("availableMemory");
        assertThat(diagnostics).contains("processCpuLoad");
        assertThat(diagnostics).contains("systemCpuLoad");
        assertThat(diagnostics).contains("\"userAgent\":\"" + Utils.getUserAgent() + "\"");
        assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void directDiagnostics() {
        CosmosClient testDirectClient = null;
        try {
            testDirectClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();
            CosmosContainer cosmosContainer =
                testDirectClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
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
            assertThat(diagnostics).contains("\"userAgent\":\"" + Utils.getUserAgent() + "\"");
            assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
            validateTransportRequestTimelineDirect(diagnostics);
            validateJson(diagnostics);
        } finally {
            if (testDirectClient != null) {
                testDirectClient.close();
            }
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryPlanDiagnostics() {
        CosmosContainer cosmosContainer = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        List<String> itemIdList = new ArrayList<>();
        for(int i = 0; i< 100; i++) {
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            CosmosItemResponse<InternalObjectNode> createResponse = cosmosContainer.createItem(internalObjectNode);
            if(i%20 == 0) {
                itemIdList.add(internalObjectNode.getId());
            }
        }

        String queryDiagnostics = null;
        List<String> queryList = new ArrayList<>();
        queryList.add("Select * from c"); //query with full range of pkrange from queryPlan
        StringBuilder queryBuilder = new StringBuilder("SELECT * from c where c.mypk in (");//query with partial range of pkrange from queryPlan
        for(int i = 0 ; i < itemIdList.size(); i++){
            queryBuilder.append("'").append(itemIdList.get(i)).append("'");
            if(i < (itemIdList.size()-1)) {
                queryBuilder.append(",");
            } else {
                queryBuilder.append(")");
            }
        }

        queryList.add(queryBuilder.toString());
        queryList.add("Select * from c where c.id = 'wrongId'");//query with no result
        for(String query : queryList) {
            int feedResponseCounter = 0;
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setQueryMetricsEnabled(true);
            Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer.queryItems(query, options, InternalObjectNode.class).iterableByPage().iterator();
            while (iterator.hasNext()) {
                FeedResponse<InternalObjectNode> feedResponse = iterator.next();
                queryDiagnostics = feedResponse.getCosmosDiagnostics().toString();
                if (feedResponseCounter == 0) {
                    assertThat(queryDiagnostics).contains("QueryPlan Start Time (UTC)=");
                    assertThat(queryDiagnostics).contains("QueryPlan End Time (UTC)=");
                    assertThat(queryDiagnostics).contains("QueryPlan Duration (ms)=");
                } else {
                    assertThat(queryDiagnostics).doesNotContain("QueryPlan Start Time (UTC)=");
                    assertThat(queryDiagnostics).doesNotContain("QueryPlan End Time (UTC)=");
                    assertThat(queryDiagnostics).doesNotContain("QueryPlan Duration (ms)=");
                }
                feedResponseCounter++;
            }
        }
    }

    @Test(groups = {"simple"}, dataProvider = "query", timeOut = TIMEOUT)
    public void queryMetrics(String query, Boolean qmEnabled) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }
        boolean qroupByFirstResponse = true; // TODO https://github.com/Azure/azure-sdk-for-java/issues/14142
        Iterator<FeedResponse<InternalObjectNode>> iterator = this.container.queryItems(query, options,
            InternalObjectNode.class).iterableByPage().iterator();
        assertThat(iterator.hasNext()).isTrue();
        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            String queryDiagnostics = feedResponse.getCosmosDiagnostics().toString();
            assertThat(feedResponse.getResults().size()).isEqualTo(0);
            if (!query.contains("group by") || qroupByFirstResponse) { // TODO https://github
                validateQueryDiagnostics(queryDiagnostics, qmEnabled, true);

                if (query.contains("group by")) {
                    qroupByFirstResponse = false;
                }
            }
        }
    }

    private static void validateQueryDiagnostics(
        String queryDiagnostics,
        Boolean qmEnabled,
        boolean expectQueryPlanDiagnostics) {
        if (qmEnabled == null || qmEnabled == true) {
            assertThat(queryDiagnostics).contains("Retrieved Document Count");
            assertThat(queryDiagnostics).contains("Query Preparation Times");
            assertThat(queryDiagnostics).contains("Runtime Execution Times");
            assertThat(queryDiagnostics).contains("Partition Execution Timeline");
        } else {
            assertThat(queryDiagnostics).doesNotContain("Retrieved Document Count");
            assertThat(queryDiagnostics).doesNotContain("Query Preparation Times");
            assertThat(queryDiagnostics).doesNotContain("Runtime Execution Times");
            assertThat(queryDiagnostics).doesNotContain("Partition Execution Timeline");
        }

        if (expectQueryPlanDiagnostics) {
            assertThat(queryDiagnostics).contains("QueryPlan Start Time (UTC)=");
            assertThat(queryDiagnostics).contains("QueryPlan End Time (UTC)=");
            assertThat(queryDiagnostics).contains("QueryPlan Duration (ms)=");
        } else {
            assertThat(queryDiagnostics).doesNotContain("QueryPlan Start Time (UTC)=");
            assertThat(queryDiagnostics).doesNotContain("QueryPlan End Time (UTC)=");
            assertThat(queryDiagnostics).doesNotContain("QueryPlan Duration (ms)=");
        }
    }

    @Test(groups = {"simple"}, dataProvider = "readAllItemsOfLogicalPartition", timeOut = TIMEOUT)
    public void queryMetricsForReadAllItemsOfLogicalPartition(Integer expectedItemCount, Boolean qmEnabled) {
        String pkValue = UUID.randomUUID().toString();

        for (int i = 0; i < expectedItemCount; i++) {
            InternalObjectNode internalObjectNode = getInternalObjectNode(pkValue);
            CosmosItemResponse<InternalObjectNode> createResponse = container.createItem(internalObjectNode);
        }

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        if (qmEnabled != null) {
            options = options.setQueryMetricsEnabled(qmEnabled);
        }
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, 5);

        Iterator<FeedResponse<InternalObjectNode>> iterator =
            this.container
                .readAllItems(
                    new PartitionKey(pkValue),
                    options,
                    InternalObjectNode.class)
                .iterableByPage().iterator();
        assertThat(iterator.hasNext()).isTrue();

        int actualItemCount = 0;
        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            String queryDiagnostics = feedResponse.getCosmosDiagnostics().toString();
            actualItemCount += feedResponse.getResults().size();

            validateQueryDiagnostics(queryDiagnostics, qmEnabled, false);
        }
        assertThat(actualItemCount).isEqualTo(expectedItemCount);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
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
            validateJson(diagnostics);
            // TODO https://github.com/Azure/azure-sdk-for-java/issues/8035
            // uncomment below if above issue is fixed
            //validateTransportRequestTimelineDirect(diagnostics);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
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

        //verifying all components from StoreResponseStatistics
        for(JsonNode node : supplementalResponseStatisticsListNode) {
            assertThat(node.get("storeResult").asText()).isNotNull();

            String requestResponseTimeUTC  = node.get("requestResponseTimeUTC").asText();
            String formattedInstant = RESPONSE_TIME_FORMATTER.format(Instant.now());
            String[] requestResponseTimeUTCList = requestResponseTimeUTC.split(" ");
            String[] formattedInstantList = formattedInstant.split(" ");
            assertThat(requestResponseTimeUTC.length()).isEqualTo(formattedInstant.length());
            assertThat(requestResponseTimeUTCList.length).isEqualTo(formattedInstantList.length);
            assertThat(requestResponseTimeUTCList[0]).isEqualTo(formattedInstantList[0]);
            assertThat(requestResponseTimeUTCList[1]).isEqualTo(formattedInstantList[1]);
            assertThat(requestResponseTimeUTCList[2]).isEqualTo(formattedInstantList[2]);

            assertThat(node.get("requestResponseTimeUTC")).isNotNull();
            assertThat(node.get("requestOperationType")).isNotNull();
            assertThat(node.get("requestOperationType")).isNotNull();
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
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
        assertThat(diagnostics).contains("\"userAgent\":\"" + Utils.getUserAgent() + "\"");
    }

    private InternalObjectNode getInternalObjectNode() {
        InternalObjectNode internalObjectNode = new InternalObjectNode();
        String uuid = UUID.randomUUID().toString();
        internalObjectNode.setId(uuid);
        BridgeInternal.setProperty(internalObjectNode, "mypk", uuid);
        return internalObjectNode;
    }

    private InternalObjectNode getInternalObjectNode(String pkValue) {
        InternalObjectNode internalObjectNode = new InternalObjectNode();
        String uuid = UUID.randomUUID().toString();
        internalObjectNode.setId(uuid);
        BridgeInternal.setProperty(internalObjectNode, "mypk", pkValue);
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

    private void validateJson(String jsonInString) {
        try {
            OBJECT_MAPPER.readTree(jsonInString);
        } catch(JsonProcessingException ex) {
            fail("Diagnostic string is not in json format");
        }
    }

    public static class TestItem {
        public String id;
        public String mypk;

        public TestItem() {
        }
    }
}
