// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.GatewayAddressCache;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosDiagnosticsTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter RESPONSE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private CosmosClient gatewayClient;
    private CosmosClient directClient;
    private CosmosContainer container;
    private CosmosAsyncContainer cosmosAsyncContainer;

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
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
        if (this.gatewayClient != null) {
            this.gatewayClient.close();
        }
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
            assertThat(createResponse.getDiagnostics().getRegionsContacted()).isNotNull();
            // TODO: (nakumars) - Uncomment the following line after your client telemetry fix
            // assertThat(createResponse.getDiagnostics().getRegionsContacted()).isNotEmpty();
            validateTransportRequestTimelineGateway(diagnostics);
            isValidJSON(diagnostics);
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
            isValidJSON(exception.toString());
            isValidJSON(exception.getMessage());
            String diagnostics = exception.getDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
            assertThat(diagnostics).doesNotContain(("\"gatewayStatistics\":null"));
            assertThat(diagnostics).contains("\"statusCode\":404");
            assertThat(diagnostics).contains("\"operationType\":\"Read\"");
            assertThat(diagnostics).contains("\"userAgent\":\"" + Utils.getUserAgent() + "\"");
            assertThat(diagnostics).doesNotContain(("\"resourceAddress\":null"));
            assertThat(createResponse.getDiagnostics().getRegionsContacted()).isNotNull();
            // TODO: (nakumars) - Uncomment the following line after your client telemetry fix
            // assertThat(createResponse.getDiagnostics().getRegionsContacted()).isNotEmpty();
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            validateTransportRequestTimelineGateway(diagnostics);
            isValidJSON(diagnostics);
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
            assertThat(diagnostics).contains("\"backendLatencyInMs\"");
            // TODO: Add this check back when enable the channelAcquisitionContext again
            // assertThat(diagnostics).contains("\"transportRequestChannelAcquisitionContext\"");
            assertThat(createResponse.getDiagnostics().getRegionsContacted()).isNotEmpty();
            assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
            validateTransportRequestTimelineDirect(diagnostics);
            isValidJSON(diagnostics);

            // validate that on failed operation request timeline is populated
            try {
                cosmosContainer.createItem(internalObjectNode);
                fail("expected 409");
            } catch (CosmosException e) {
                diagnostics = e.getDiagnostics().toString();
                assertThat(diagnostics).contains("\"backendLatencyInMs\"");
                validateTransportRequestTimelineDirect(e.getDiagnostics().toString());
            }
        } finally {
            if (testDirectClient != null) {
                testDirectClient.close();
            }
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryPlanDiagnostics() throws JsonProcessingException {
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
                    String requestTimeLine = OBJECT_MAPPER.writeValueAsString(feedResponse.getCosmosDiagnostics().getFeedResponseDiagnostics().getQueryPlanDiagnosticsContext().getRequestTimeline());
                    assertThat(requestTimeLine).contains("connectionConfigured");
                    assertThat(requestTimeLine).contains("requestSent");
                    assertThat(requestTimeLine).contains("transitTime");
                    assertThat(requestTimeLine).contains("received");
                } else {
                    assertThat(queryDiagnostics).doesNotContain("QueryPlan Start Time (UTC)=");
                    assertThat(queryDiagnostics).doesNotContain("QueryPlan End Time (UTC)=");
                    assertThat(queryDiagnostics).doesNotContain("QueryPlan Duration (ms)=");
                    assertThat(queryDiagnostics).doesNotContain("QueryPlan RequestTimeline =");
                }
                feedResponseCounter++;
            }
        }
    }

    @Test(groups = {"simple"}, dataProvider = "query", timeOut = TIMEOUT)
    public void queryMetrics(String query, Boolean qmEnabled) {
        CosmosContainer directContainer =
            this.directClient.getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }
        boolean qroupByFirstResponse = true; // TODO https://github.com/Azure/azure-sdk-for-java/issues/14142
        Iterator<FeedResponse<InternalObjectNode>> iterator = directContainer.queryItems(query, options,
            InternalObjectNode.class).iterableByPage().iterator();
        assertThat(iterator.hasNext()).isTrue();
        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            String queryDiagnostics = feedResponse.getCosmosDiagnostics().toString();
            assertThat(feedResponse.getResults().size()).isEqualTo(0);
            if (!query.contains("group by") || qroupByFirstResponse) { // TODO https://github
                validateQueryDiagnostics(queryDiagnostics, qmEnabled, true);
                validateDirectModeQueryDiagnostics(queryDiagnostics);
                if (query.contains("group by")) {
                    qroupByFirstResponse = false;
                }
            }
        }
    }

    private void validateDirectModeQueryDiagnostics(String diagnostics) {
        assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
        assertThat(diagnostics).contains("supplementalResponseStatisticsList");
        assertThat(diagnostics).contains("responseStatisticsList");
        assertThat(diagnostics).contains("\"gatewayStatistics\":null");
        assertThat(diagnostics).contains("addressResolutionStatistics");
        assertThat(diagnostics).contains("\"userAgent\":\"" + Utils.getUserAgent() + "\"");
    }

    private void validateGatewayModeQueryDiagnostics(String diagnostics) {
        assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
        assertThat(diagnostics).doesNotContain(("\"gatewayStatistics\":null"));
        assertThat(diagnostics).contains("\"operationType\":\"Query\"");
        assertThat(diagnostics).contains("\"userAgent\":\"" + Utils.getUserAgent() + "\"");
        assertThat(diagnostics).contains("\"regionsContacted\"");
    }

    @Test(groups = {"simple"}, dataProvider = "query", timeOut = TIMEOUT*2)
    public void queryDiagnosticsGatewayMode(String query, Boolean qmEnabled) {
        CosmosClient testDirectClient = new CosmosClientBuilder()
                                            .endpoint(TestConfigurations.HOST)
                                            .key(TestConfigurations.MASTER_KEY)
                                            .contentResponseOnWriteEnabled(true)
                                            .gatewayMode()
                                            .buildClient();
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosContainer cosmosContainer = testDirectClient.getDatabase(cosmosAsyncContainer.getDatabase().getId())
                                              .getContainer(cosmosAsyncContainer.getId());
        List<String> itemIdList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            CosmosItemResponse<InternalObjectNode> createResponse = cosmosContainer.createItem(internalObjectNode);
            if (i % 20 == 0) {
                itemIdList.add(internalObjectNode.getId());
            }
        }
        boolean qroupByFirstResponse = true;
        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }
        Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer
                                                                  .queryItems(query, options, InternalObjectNode.class)
                                                                  .iterableByPage()
                                                                  .iterator();
        assertThat(iterator.hasNext()).isTrue();

        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            String queryDiagnostics = feedResponse.getCosmosDiagnostics().toString();
            assertThat(feedResponse.getResults().size()).isEqualTo(0);
            if (!query.contains("group by") || qroupByFirstResponse) { // TODO https://github
                validateQueryDiagnostics(queryDiagnostics, qmEnabled, true);
                validateGatewayModeQueryDiagnostics(queryDiagnostics);
                if (query.contains("group by")) {
                    qroupByFirstResponse = false;
                }
            }
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryMetricsWithADifferentLocale() {

        Locale.setDefault(Locale.GERMAN);
        String query = "select * from root where root.id= \"someid\"";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        Iterator<FeedResponse<InternalObjectNode>> iterator = this.container.queryItems(query, options,
                                                                                        InternalObjectNode.class)
                                                                  .iterableByPage().iterator();
        double requestCharge = 0;
        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            requestCharge += feedResponse.getRequestCharge();
        }
        assertThat(requestCharge).isGreaterThan(0);
        // resetting locale
        Locale.setDefault(Locale.ROOT);
    }

    private static void validateQueryDiagnostics(
        String queryDiagnostics,
        Boolean qmEnabled,
        boolean expectQueryPlanDiagnostics) {
        if (qmEnabled == null || qmEnabled) {
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
            isValidJSON(exception.toString());
            isValidJSON(exception.getMessage());
            String diagnostics = exception.getDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
            assertThat(diagnostics).doesNotContain(("\"resourceAddress\":null"));
            assertThat(exception.getDiagnostics().getRegionsContacted()).isNotEmpty();
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            assertThat(diagnostics).contains("\"backendLatencyInMs\"");
            isValidJSON(diagnostics);
            validateTransportRequestTimelineDirect(diagnostics);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void directDiagnosticsOnMetadataException() {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosClient client = null;
        try {
            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();
            CosmosContainer container = client.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
            Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.any(Duration.class)))
                .thenReturn(Mono.error(new CosmosException(400, "TestBadRequest")));
            RxStoreModel rxGatewayStoreModel = rxGatewayStoreModel = ReflectionUtils.getGatewayProxy((RxDocumentClientImpl) client.asyncClient().getDocClientWrapper());
            ReflectionUtils.setGatewayHttpClient(rxGatewayStoreModel, mockHttpClient);
            container.createItem(internalObjectNode);
            fail("request should fail as bad request");
        } catch (CosmosException exception) {
            isValidJSON(exception.toString());
            isValidJSON(exception.getMessage());
            String diagnostics = exception.getDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
            assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
            assertThat(diagnostics).doesNotContain(("\"resourceAddress\":null"));
            assertThat(diagnostics).contains("\"resourceType\":\"DocumentCollection\"");
            assertThat(exception.getDiagnostics().getRegionsContacted()).isNotEmpty();
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            isValidJSON(diagnostics);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void supplementalResponseStatisticsList() throws Exception {
        ClientSideRequestStatistics clientSideRequestStatistics = new ClientSideRequestStatistics(mockDiagnosticsClientContext());
        for (int i = 0; i < 15; i++) {
            RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Head, ResourceType.Document);
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
            RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Head, ResourceType.Document);
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
            Instant instant = Instant.from(RESPONSE_TIME_FORMATTER.parse(requestResponseTimeUTC));
            assertThat(Instant.now().toEpochMilli() - instant.toEpochMilli()).isLessThan(5000);
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

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void rntbdRequestResponseLengthStatistics() throws Exception {
        TestItem testItem = new TestItem();
        testItem.id = UUID.randomUUID().toString();
        testItem.mypk = UUID.randomUUID().toString();

        int testItemLength = OBJECT_MAPPER.writeValueAsBytes(testItem).length;
        CosmosContainer container = directClient.getDatabase(this.cosmosAsyncContainer.getDatabase().getId()).getContainer(this.cosmosAsyncContainer.getId());

        // create
        CosmosItemResponse<TestItem> createItemResponse = container.createItem(testItem);
        validate(createItemResponse.getDiagnostics(), testItemLength,  ModelBridgeInternal.getPayloadLength(createItemResponse));

        // reading a deleted item.
        try {
            container.createItem(testItem);
            fail("expected to fail due to 409");
        } catch (CosmosException e) {
            // no request payload and no response payload
            validate(e.getDiagnostics(), testItemLength, 0);
        }

        // read
        CosmosItemResponse<TestItem> readItemResponse = container.readItem(testItem.id, new PartitionKey(testItem.mypk), TestItem.class);
        // no request payload and no response payload
        validate(readItemResponse.getDiagnostics(), 0, ModelBridgeInternal.getPayloadLength(readItemResponse));

        // delete
        CosmosItemResponse<Object> deleteItemResponse = container.deleteItem(testItem, null);
        // no request payload and no response payload
        validate(deleteItemResponse.getDiagnostics(), 0, 0);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void rntbdStatistics() throws Exception {
        Instant beforeClientInitialization = Instant.now();

        CosmosClient client1 = null;
        try {

            client1 = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .directMode()
                .buildClient();

            TestItem testItem = new TestItem();
            testItem.id = UUID.randomUUID().toString();
            testItem.mypk = UUID.randomUUID().toString();

            int testItemLength = OBJECT_MAPPER.writeValueAsBytes(testItem).length;
            CosmosContainer container = client1.getDatabase(this.cosmosAsyncContainer.getDatabase().getId()).getContainer(this.cosmosAsyncContainer.getId());

            Thread.sleep(1000);

            // create
            // initializes the rntbd service endpoint
            Instant beforeInitializingRntbdServiceEndpoint = Instant.now();
            CosmosItemResponse<TestItem> operation1Response = container.upsertItem(testItem);
            Instant afterInitializingRntbdServiceEndpoint = Instant.now();

            Thread.sleep(1000);
            Instant beforeOperation2 = Instant.now();
            CosmosItemResponse<TestItem> operation2Response = container.upsertItem(testItem);
            Instant afterOperation2 = Instant.now();

            Thread.sleep(1000);
            Instant beforeOperation3 = Instant.now();
            CosmosItemResponse<TestItem> operation3Response = container.upsertItem(testItem);
            Instant afterOperation3 = Instant.now();

            validateRntbdStatistics(operation3Response.getDiagnostics(),
                beforeClientInitialization,
                beforeInitializingRntbdServiceEndpoint,
                afterInitializingRntbdServiceEndpoint,
                beforeOperation2,
                afterOperation2,
                beforeOperation3,
                afterOperation3);

            // read
            CosmosItemResponse<TestItem> readItemResponse = container.readItem(testItem.id, new PartitionKey(testItem.mypk), TestItem.class);
            // no request payload and no response payload
            validate(readItemResponse.getDiagnostics(), 0, ModelBridgeInternal.getPayloadLength(readItemResponse));

            // delete
            CosmosItemResponse<Object> deleteItemResponse = container.deleteItem(testItem, null);
            // no request payload and no response payload
            validate(deleteItemResponse.getDiagnostics(), 0, 0);
        } finally {
            LifeCycleUtils.closeQuietly(client1);
        }
    }

    private void validateRntbdStatistics(CosmosDiagnostics cosmosDiagnostics,
                                         Instant clientInitializationTime,
                                         Instant beforeInitializingRntbdServiceEndpoint,
                                         Instant afterInitializingRntbdServiceEndpoint,
                                         Instant beforeOperation2,
                                         Instant afterOperation2,
                                         Instant beforeOperation3,
                                         Instant afterOperation3) throws Exception {
        ObjectNode diagnostics = (ObjectNode) OBJECT_MAPPER.readTree(cosmosDiagnostics.toString());
        JsonNode responseStatisticsList = diagnostics.get("responseStatisticsList");
        assertThat(responseStatisticsList.isArray()).isTrue();
        assertThat(responseStatisticsList.size()).isGreaterThan(0);
        JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");
        assertThat(storeResult).isNotNull();

        boolean hasPayload = storeResult.get("exception").isNull();
        assertThat(storeResult.get("channelTaskQueueSize").asInt(-1)).isGreaterThan(0);
        assertThat(storeResult.get("pendingRequestsCount").asInt(-1)).isGreaterThanOrEqualTo(0);

        JsonNode serviceEndpointStatistics = storeResult.get("serviceEndpointStatistics");
        assertThat(serviceEndpointStatistics).isNotNull();

        assertThat(serviceEndpointStatistics.get("availableChannels").asInt(-1)).isGreaterThan(0);
        // no concurrent work
        assertThat(serviceEndpointStatistics.get("acquiredChannels").asInt(-1)).isEqualTo(0);
        // current request
        assertThat(serviceEndpointStatistics.get("inflightRequests").asInt(-1)).isEqualTo(1);

        assertThat(serviceEndpointStatistics.get("isClosed").asBoolean()).isEqualTo(false);

        // first request initialized the rntbd service endpoint
        Instant beforeInitializationThreshold = beforeInitializingRntbdServiceEndpoint.minusMillis(1);
        assertThat(Instant.parse(serviceEndpointStatistics.get("createdTime").asText()))
            .isAfterOrEqualTo(beforeInitializationThreshold);

        // Adding 1 ms to cover for rounding errors (only 3 fractional digits)
        Instant afterInitializationThreshold = afterInitializingRntbdServiceEndpoint.plusMillis(1);
        assertThat(Instant.parse(serviceEndpointStatistics.get("createdTime").asText()))
            .isBeforeOrEqualTo(afterInitializationThreshold);

        // Adding 1 ms to cover for rounding errors (only 3 fractional digits)
        Instant afterOperation2Threshold = afterOperation2.plusMillis(1);
        Instant beforeOperation2Threshold = beforeOperation2.minusMillis(1);
        assertThat(Instant.parse(serviceEndpointStatistics.get("lastRequestTime").asText()))
            .isAfterOrEqualTo(beforeOperation2Threshold)
            .isBeforeOrEqualTo(afterOperation2Threshold);
        assertThat(Instant.parse(serviceEndpointStatistics.get("lastSuccessfulRequestTime").asText()))
            .isAfterOrEqualTo(beforeOperation2Threshold)
            .isBeforeOrEqualTo(afterOperation2Threshold);
    }

    private void validate(CosmosDiagnostics cosmosDiagnostics, int expectedRequestPayloadSize, int expectedResponsePayloadSize) throws Exception {
        ObjectNode diagnostics = (ObjectNode) OBJECT_MAPPER.readTree(cosmosDiagnostics.toString());
        JsonNode responseStatisticsList = diagnostics.get("responseStatisticsList");
        assertThat(responseStatisticsList.isArray()).isTrue();
        assertThat(responseStatisticsList.size()).isGreaterThan(0);
        JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");

        boolean hasPayload = storeResult.get("exception").isNull();
        assertThat(storeResult).isNotNull();
        assertThat(storeResult.get("rntbdRequestLengthInBytes").asInt(-1)).isGreaterThan(expectedRequestPayloadSize);
        assertThat(storeResult.get("rntbdRequestLengthInBytes").asInt(-1)).isGreaterThan(expectedRequestPayloadSize);
        assertThat(storeResult.get("requestPayloadLengthInBytes").asInt(-1)).isEqualTo(expectedRequestPayloadSize);
        if (hasPayload) {
            assertThat(storeResult.get("responsePayloadLengthInBytes").asInt(-1)).isEqualTo(expectedResponsePayloadSize);
        }
        assertThat(storeResult.get("rntbdResponseLengthInBytes").asInt(-1)).isGreaterThan(expectedResponsePayloadSize);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void addressResolutionStatistics() {
        CosmosClient client1 = null;
        CosmosClient client2 = null;
        String databaseId = DatabaseForTest.generateId();
        String containerId = UUID.randomUUID().toString();
        CosmosDatabase cosmosDatabase = null;
        CosmosContainer cosmosContainer = null;
        try {
            client1 = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();
            client1.createDatabase(databaseId);
            cosmosDatabase = client1.getDatabase(databaseId);
            cosmosDatabase.createContainer(containerId, "/mypk");

            InternalObjectNode internalObjectNode = getInternalObjectNode();
            cosmosContainer = cosmosDatabase.getContainer(containerId);
            CosmosItemResponse<InternalObjectNode> writeResourceResponse = cosmosContainer.createItem(internalObjectNode);
            //Success address resolution client side statistics
            assertThat(writeResourceResponse.getDiagnostics().toString()).contains("addressResolutionStatistics");
            assertThat(writeResourceResponse.getDiagnostics().toString()).contains("\"inflightRequest\":false");
            assertThat(writeResourceResponse.getDiagnostics().toString()).doesNotContain("endTime=\"null\"");
            assertThat(writeResourceResponse.getDiagnostics().toString()).contains("\"errorMessage\":null");
            assertThat(writeResourceResponse.getDiagnostics().toString()).doesNotContain("\"errorMessage\":\"io.netty" +
                ".channel.AbstractChannel$AnnotatedConnectException: Connection refused: no further information");

            client2 = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();
            cosmosDatabase = client2.getDatabase(databaseId);
            cosmosContainer = cosmosDatabase.getContainer(containerId);
            AsyncDocumentClient asyncDocumentClient = client2.asyncClient().getContextClient();
            GlobalAddressResolver addressResolver = (GlobalAddressResolver) FieldUtils.readField(asyncDocumentClient,
                "addressResolver", true);

            @SuppressWarnings("rawtypes")
            Map addressCacheByEndpoint = (Map) FieldUtils.readField(addressResolver,
                "addressCacheByEndpoint",
                true);
            Object endpointCache = addressCacheByEndpoint.values().toArray()[0];
            GatewayAddressCache addressCache = (GatewayAddressCache) FieldUtils.readField(endpointCache, "addressCache", true);

            HttpClient httpClient = httpClient(true);
            FieldUtils.writeField(addressCache, "httpClient", httpClient, true);
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    HttpClient httpClient1 = httpClient(false);
                    FieldUtils.writeField(addressCache, "httpClient", httpClient1, true);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }).start();
            PartitionKey partitionKey = new PartitionKey(internalObjectNode.get("mypk"));
            CosmosItemResponse<InternalObjectNode> readResourceResponse =
                cosmosContainer.readItem(internalObjectNode.getId(), partitionKey, new CosmosItemRequestOptions(),
                    InternalObjectNode.class);

            //Partial success address resolution client side statistics
            assertThat(readResourceResponse.getDiagnostics().toString()).contains("addressResolutionStatistics");
            assertThat(readResourceResponse.getDiagnostics().toString()).contains("\"inflightRequest\":false");
            assertThat(readResourceResponse.getDiagnostics().toString()).doesNotContain("endTime=\"null\"");
            assertThat(readResourceResponse.getDiagnostics().toString()).contains("\"errorMessage\":null");
            assertThat(readResourceResponse.getDiagnostics().toString()).contains("\"errorMessage\":\"io.netty" +
                ".channel.AbstractChannel$AnnotatedConnectException: Connection refused");
        } catch (Exception ex) {
            logger.error("Error in test addressResolutionStatistics", ex);
            fail("This test should not throw exception " + ex);
        } finally {
            safeDeleteSyncDatabase(cosmosDatabase);
            if (client1 != null) {
                client1.close();
            }

            if (client2 != null) {
                client2.close();
            }
        }
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
        assertThat(diagnostics).contains("\"eventName\":\"channelAcquisitionStarted\"");
        assertThat(diagnostics).contains("\"eventName\":\"pipelined\"");
        assertThat(diagnostics).contains("\"eventName\":\"transitTime\"");
        assertThat(diagnostics).contains("\"eventName\":\"received\"");
        assertThat(diagnostics).contains("\"eventName\":\"completed\"");
        assertThat(diagnostics).contains("\"startTimeUTC\"");
        assertThat(diagnostics).contains("\"durationInMicroSec\"");
    }

    public void isValidJSON(final String json) {
        try {
            final JsonParser parser = new ObjectMapper().createParser(json);
            while (parser.nextToken() != null) {
            }
        } catch (IOException ex) {
            fail("Diagnostic string is not in json format ", ex);
        }
    }

    private HttpClient httpClient(boolean fakeProxy) {
        HttpClientConfig httpClientConfig;
        if(fakeProxy) {
            httpClientConfig = new HttpClientConfig(new Configs())
                .withProxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)));
        } else {
            httpClientConfig = new HttpClientConfig(new Configs());
        }

        return HttpClient.createFixed(httpClientConfig);
    }

    public static class TestItem {
        public String id;
        public String mypk;

        public TestItem() {
        }
    }
}
