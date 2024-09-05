// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.OperationCancelledException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.directconnectivity.GatewayAddressCache;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.AsyncRntbdRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelStatistics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestTimer;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.routing.LocationCache;
import com.azure.cosmos.models.*;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionErrorType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.core.JsonFactory;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosDiagnosticsTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String USER_AGENT_SUFFIX_GATEWAY_CLIENT = "gatewayClientSuffix";
    private static final String USER_AGENT_SUFFIX_DIRECT_CLIENT = "directClientSuffix";
    private static final DateTimeFormatter RESPONSE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final String tempMachineId = getTempMachineId();
    private CosmosClient gatewayClient;
    private CosmosClient directClient;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private CosmosContainer containerGateway;
    private CosmosContainer containerDirect;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private String gatewayClientUserAgent;
    private String directClientUserAgent;

    private static String getTempMachineId() {
        Field field = null;
        try {
            field = RxDocumentClientImpl.class.getDeclaredField("tempMachineId");
        } catch (NoSuchFieldException e) {
            fail(e.toString());
        }
        field.setAccessible(true);

        try {
            return (String)field.get(null);
        } catch (IllegalAccessException e) {
            fail(e.toString());

            return null;
        }
    }


    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.gatewayClient).isNull();
        gatewayClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .userAgentSuffix(USER_AGENT_SUFFIX_GATEWAY_CLIENT)
            .gatewayMode()
            .buildClient();

        UserAgentContainer userAgentContainer = new UserAgentContainer();
        userAgentContainer.setSuffix(USER_AGENT_SUFFIX_GATEWAY_CLIENT);
        this.gatewayClientUserAgent = userAgentContainer.getUserAgent();

        directClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .userAgentSuffix(USER_AGENT_SUFFIX_DIRECT_CLIENT)
            .directMode()
            .buildClient();
        userAgentContainer.setSuffix(USER_AGENT_SUFFIX_DIRECT_CLIENT);
        this.directClientUserAgent = userAgentContainer.getUserAgent();

        cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(this.gatewayClient.asyncClient());
        cosmosAsyncDatabase = directClient.asyncClient().getDatabase(cosmosAsyncContainer.getDatabase().getId());
        containerGateway = gatewayClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
        containerDirect = directClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
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

    @DataProvider(name = "connectionStateListenerArgProvider")
    public Object[][] connectionStateListenerArgProvider() {
        return new Object[][]{
            {true},
            {false}
        };
    }

    @DataProvider(name = "operationTypeProvider")
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            { OperationType.Read },
            { OperationType.Replace },
            { OperationType.Create },
            { OperationType.Query },
        };
    }

    @DataProvider(name = "gatewayAndDirect")
    private Object[][] gatewayAndDirect() {
        return new Object[][]{
            { containerDirect },
            { containerGateway }
        };
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void queryChangeFeed() throws Exception {
        CosmosAsyncContainer cosmosContainer = cosmosAsyncContainer;

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());
        options.allVersionsAndDeletes();

        Iterator<FeedResponse<JsonNode>> results = cosmosContainer
            .queryChangeFeed(options, JsonNode.class)
            .byPage()
            .toIterable()
            .iterator();

        if (results.hasNext()) {
            FeedResponse<JsonNode> response = results.next();
            String diagnostics = response.getCosmosDiagnostics().toString();
            assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
            assertThat(diagnostics).contains("\"userAgent\":\"" + this.gatewayClientUserAgent + "\"");
            assertThat(diagnostics).contains("gatewayStatisticsList");
            assertThat(diagnostics).contains("\"operationType\":\"ReadFeed\"");
            assertThat(diagnostics).contains("\"userAgent\":\"" + this.gatewayClientUserAgent + "\"");
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gatewayDiagnostics() throws Exception {
        CosmosClient testClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .userAgentSuffix(USER_AGENT_SUFFIX_GATEWAY_CLIENT)
            .gatewayMode()
            .buildClient();

        CosmosContainer testContainer =
            testClient
                .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                .getContainer(cosmosAsyncContainer.getId());
        // Adding a delay to allow async VM instance metadata initialization to complete
        Thread.sleep(2000);
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = testContainer.createItem(internalObjectNode);
        String diagnostics = createResponse.getDiagnostics().toString();
        logger.info("DIAGNOSTICS: {}", diagnostics);
        assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
        assertThat(diagnostics).contains("\"userAgent\":\"" + this.gatewayClientUserAgent + "\"");
        assertThat(diagnostics).contains("gatewayStatisticsList");
        assertThat(diagnostics).contains("\"operationType\":\"Create\"");
        assertThat(diagnostics).contains("\"metaDataName\":\"CONTAINER_LOOK_UP\"");
        assertThat(diagnostics).contains("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
        assertThat(diagnostics).contains("\"userAgent\":\"" + this.gatewayClientUserAgent + "\"");
        assertThat(diagnostics).containsAnyOf(
            "\"machineId\":\"" + tempMachineId + "\"", // logged machineId should be static uuid or
            "\"machineId\":\"" + ClientTelemetry.getMachineId(null) + "\"" // the vmId from Azure
        );
        assertThat(diagnostics).containsPattern("(?s).*?\"activityId\":\"[^\\s\"]+\".*");
        assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
        assertThat(createResponse.getDiagnostics().getContactedRegionNames()).isNotNull();
        assertThat(createResponse.getDiagnostics().getRegionsContacted()).isNotEmpty();
        validateTransportRequestTimelineGateway(diagnostics);
        validateRegionContacted(createResponse.getDiagnostics(), gatewayClient.asyncClient());
        isValidJSON(diagnostics);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gatewayDiagnosticsOnException() throws Exception {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = null;
        try {
            createResponse = this.containerGateway.createItem(internalObjectNode);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            ModelBridgeInternal.setPartitionKey(cosmosItemRequestOptions, new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse<InternalObjectNode> readResponse =
                this.containerGateway.readItem(BridgeInternal.getProperties(createResponse).getId(),
                    new PartitionKey("wrongPartitionKey"),
                    InternalObjectNode.class);
            fail("request should fail as partition key is wrong");
        } catch (CosmosException exception) {
            System.out.println(exception.getDiagnostics());
            isValidJSON(exception.toString());
            isValidJSON(exception.getMessage());
            String diagnostics = exception.getDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
            assertThat(diagnostics).contains("\"userAgent\":\"" + this.gatewayClientUserAgent + "\"");
            assertThat(diagnostics).contains("gatewayStatisticsList");
            assertThat(diagnostics).contains("\"statusCode\":404");
            assertThat(diagnostics).contains("\"operationType\":\"Read\"");
            assertThat(diagnostics).contains("\"userAgent\":\"" + this.gatewayClientUserAgent + "\"");
            assertThat(diagnostics).contains("\"exceptionMessage\":\"Entity with the specified id does not exist in the system.");
            assertThat(diagnostics).contains("\"exceptionResponseHeaders\"");
            assertThat(diagnostics).doesNotContain("\"exceptionResponseHeaders\": \"{}\"");
            assertThat(diagnostics).containsAnyOf(
                "\"machineId\":\"" + tempMachineId + "\"", // logged machineId should be static uuid or
                "\"machineId\":\"" + ClientTelemetry.getMachineId(null) + "\"" // the vmId from Azure
            );
            assertThat(diagnostics).containsPattern("(?s).*?\"activityId\":\"[^\\s\"]+\".*");
            assertThat(diagnostics).doesNotContain(("\"resourceAddress\":null"));
            assertThat(createResponse.getDiagnostics().getContactedRegionNames()).isNotNull();
            validateRegionContacted(createResponse.getDiagnostics(), this.containerGateway.asyncContainer.getDatabase().getClient());
            assertThat(createResponse.getDiagnostics().getRegionsContacted()).isNotEmpty();
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            validateTransportRequestTimelineGateway(diagnostics);
            isValidJSON(diagnostics);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gatewayDiagnostgiticsOnNonCosmosException() {
        CosmosAsyncClient testClient = null;
        try {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(1); // using a small value to force pendingAcquisitionTimeout happen

            testClient =
                new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .gatewayMode(gatewayConnectionConfig)
                    .buildAsyncClient();

            CosmosAsyncContainer testContainer =
                testClient
                    .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(cosmosAsyncContainer.getId());

            AtomicBoolean pendingAcquisitionTimeoutHappened = new AtomicBoolean(false);
            Flux.range(1, 10)
                .flatMap(t -> testContainer.createItem(TestItem.createNewItem()))
                .onErrorResume(throwable -> {
                    assertThat(throwable).isInstanceOf(CosmosException.class);
                    String cosmosDiagnostics = ((CosmosException)throwable).getDiagnostics().toString();
                    assertThat(cosmosDiagnostics).contains("exceptionMessage");
                    if (cosmosDiagnostics.contains("Pending acquire queue has reached its maximum size")) {
                        pendingAcquisitionTimeoutHappened.compareAndSet(false, true);
                    }

                    return Mono.empty();
                })
                .blockLast();

            assertThat(pendingAcquisitionTimeoutHappened.get()).isTrue();
        } finally {
            safeClose(testClient);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void systemDiagnosticsForSystemStateInformation() {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = this.containerGateway.createItem(internalObjectNode);
        String diagnostics = createResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("systemInformation");
        assertThat(diagnostics).contains("usedMemory");
        assertThat(diagnostics).contains("availableMemory");
        assertThat(diagnostics).contains("systemCpuLoad");
        assertThat(diagnostics).contains("\"userAgent\":\"" + this.gatewayClientUserAgent + "\"");
        assertThat(diagnostics).containsPattern("(?s).*?\"activityId\":\"[^\\s\"]+\".*");
        assertThat(createResponse.getDiagnostics().getDuration()).isNotNull();
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void directDiagnostics() throws Exception {
        CosmosClient testClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .userAgentSuffix(USER_AGENT_SUFFIX_DIRECT_CLIENT)
            .directMode()
            .buildClient();

        CosmosContainer testContainer =
            testClient
                .getDatabase(cosmosAsyncContainer.getDatabase().getId())
                .getContainer(cosmosAsyncContainer.getId());

        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = testContainer.createItem(internalObjectNode);
        validateDirectModeDiagnosticsOnSuccess(createResponse.getDiagnostics(), directClient, this.directClientUserAgent);
        validateChannelAcquisitionContext(createResponse.getDiagnostics(), false);

        // validate that on failed operation request timeline is populated
        try {
            testContainer.createItem(internalObjectNode);
            fail("expected 409");
        } catch (CosmosException e) {
            validateDirectModeDiagnosticsOnException(e, this.directClientUserAgent);
            validateChannelAcquisitionContext(e.getDiagnostics(), false);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void requestSessionTokenDiagnostics() {
        CosmosClient testSessionTokenClient = null;
        try {
            testSessionTokenClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();
            CosmosContainer cosmosContainer =
                testSessionTokenClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            CosmosItemResponse<InternalObjectNode> createResponse = cosmosContainer.createItem(internalObjectNode);
            String diagnostics = createResponse.getDiagnostics().toString();

            // assert that request session token was not sent to backend (null)
            assertThat(diagnostics).contains("\"requestSessionToken\":null");

            // read item to validate response and request session tokens are equal
            String sessionToken = createResponse.getSessionToken();
            CosmosItemResponse<InternalObjectNode> readResponse =
                cosmosContainer.readItem(BridgeInternal.getProperties(createResponse).getId(),
                    new PartitionKey(BridgeInternal.getProperties(createResponse).getId()),
                    InternalObjectNode.class);
            diagnostics = readResponse.getDiagnostics().toString();
            assertThat(diagnostics).contains(String.format("\"requestSessionToken\":\"%s\"", sessionToken));

            // use batch operation to check that user-set session token is being passed in
            // need to use batch since we only pass session token on multiple region write or batch operation
            CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(
                BridgeInternal.getProperties(createResponse).getId()));
            internalObjectNode = getInternalObjectNode();
            batch.createItemOperation(internalObjectNode);
            CosmosBatchResponse batchResponse = cosmosContainer.executeCosmosBatch(batch,
                new CosmosBatchRequestOptions().setSessionToken(readResponse.getSessionToken()));
            diagnostics = batchResponse.getDiagnostics().toString();
            assertThat(diagnostics).contains(String.format("\"requestSessionToken\":\"%s\"",
                readResponse.getSessionToken()));

        } finally {
            if (testSessionTokenClient != null) {
                testSessionTokenClient.close();
            }
        }
    }

    @Test(groups = {"fast"})
    public void databaseAccountToClients() {
        CosmosClient testClient = null;
        try {
            testClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();
            CosmosContainer cosmosContainer =
                testClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            CosmosItemResponse<InternalObjectNode> createResponse = cosmosContainer.createItem(internalObjectNode);
            String diagnostics = createResponse.getDiagnostics().toString();

            // assert diagnostics shows the correct format for tracking client instances
            assertThat(diagnostics).contains(String.format("\"clientEndpoints\"" +
                    ":{\"%s\"", TestConfigurations.HOST));
            // track number of clients currently mapped to account
            int clientsIndex = diagnostics.indexOf("\"clientEndpoints\":");
            // we do end at +120 to ensure we grab the bracket even if the account is very long or if
            // we have hundreds of clients (triple digit ints) running at once in the pipelines
            String[] substrings = diagnostics.substring(clientsIndex, clientsIndex + 120)
                .split("}")[0].split(":");
            String intString = substrings[substrings.length-1];
            int intValue = Integer.parseInt(intString);


            CosmosClient testClient2 = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildClient();

            internalObjectNode = getInternalObjectNode();
            createResponse = cosmosContainer.createItem(internalObjectNode);
            diagnostics = createResponse.getDiagnostics().toString();
            // assert diagnostics shows the correct format for tracking client instances
            assertThat(diagnostics).contains(String.format("\"clientEndpoints\"" +
                ":{\"%s\"", TestConfigurations.HOST));
            // grab new value and assert one additional client is mapped to the same account used previously
            clientsIndex = diagnostics.indexOf("\"clientEndpoints\":");
            substrings = diagnostics.substring(clientsIndex, clientsIndex + 120)
                .split("}")[0].split(":");
            intString = substrings[substrings.length-1];
            assertThat(Integer.parseInt(intString)).isEqualTo(intValue+1);

            //close second client
            testClient2.close();

        } finally {
            if (testClient != null) {
                testClient.close();
            }
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void queryPlanDiagnostics() throws JsonProcessingException {
        List<String> itemIdList = new ArrayList<>();
        for(int i = 0; i< 100; i++) {
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            CosmosItemResponse<InternalObjectNode> createResponse = containerDirect.createItem(internalObjectNode);
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
            Iterator<FeedResponse<InternalObjectNode>> iterator = containerDirect.queryItems(query, options, InternalObjectNode.class).iterableByPage().iterator();
            while (iterator.hasNext()) {
                FeedResponse<InternalObjectNode> feedResponse = iterator.next();
                queryDiagnostics = feedResponse.getCosmosDiagnostics().toString();
                if (feedResponseCounter == 0) {
                    assertThat(queryDiagnostics).contains("queryPlanDiagnosticsContext");
                    assertThat(queryDiagnostics).contains("startTimeUTC");
                    assertThat(queryDiagnostics).contains("endTimeUTC");
                    assertThat(queryDiagnostics).contains("durationInMilliSecs");
                    String requestTimeLine = OBJECT_MAPPER.writeValueAsString(feedResponse.getCosmosDiagnostics().getFeedResponseDiagnostics().getQueryPlanDiagnosticsContext().getRequestTimeline());
                    assertThat(requestTimeLine).contains("connectionConfigured");
                    assertThat(requestTimeLine).contains("requestSent");
                    assertThat(requestTimeLine).contains("transitTime");
                    assertThat(requestTimeLine).contains("received");
                } else {
                    assertThat(queryDiagnostics).contains("\"queryPlanDiagnosticsContext\":null");
                }
                feedResponseCounter++;
            }
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readManyDiagnostics() {
        String pkValue = UUID.randomUUID().toString();
        PartitionKey partitionKey = new PartitionKey(pkValue);
        List<CosmosItemIdentity> itemIdList = new ArrayList<>();
        for(int i = 0; i< 100; i++) {
            InternalObjectNode internalObjectNode = getInternalObjectNode(pkValue);
            CosmosItemResponse<InternalObjectNode> createResponse = containerDirect.createItem(internalObjectNode);
            if(i%20 == 0) {
                itemIdList.add(new CosmosItemIdentity(partitionKey, internalObjectNode.getId()));
            }
        }

        FeedResponse<InternalObjectNode> response = containerDirect.readMany(itemIdList, InternalObjectNode.class);
        FeedResponseDiagnostics diagnostics = response.getCosmosDiagnostics().getFeedResponseDiagnostics();

        assertThat(diagnostics.getClientSideRequestStatistics().size()).isEqualTo(1);
        assertThat(diagnostics.getQueryMetricsMap().values().iterator().next().getRetrievedDocumentCount()).isEqualTo(itemIdList.size());

        String cosmosDiagnosticsString = response.getCosmosDiagnostics().toString();
        assertThat(cosmosDiagnosticsString).contains("\"userAgent\":\"" + this.directClientUserAgent + "\"");
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void queryMetricsWithIndexMetrics() {
        List<String> itemIdList = new ArrayList<>();
        for(int i = 0; i< 100; i++) {
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            CosmosItemResponse<InternalObjectNode> createResponse = containerDirect.createItem(internalObjectNode);
            if(i%20 == 0) {
                itemIdList.add(internalObjectNode.getId());
            }
        }

        String queryDiagnostics = null;
        List<String> queryList = new ArrayList<>();
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
        for (String query : queryList) {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setQueryMetricsEnabled(true);
            options.setIndexMetricsEnabled(true);
            Iterator<FeedResponse<InternalObjectNode>> iterator = containerDirect.queryItems(query, options, InternalObjectNode.class).iterableByPage().iterator();
            while (iterator.hasNext()) {
                FeedResponse<InternalObjectNode> feedResponse = iterator.next();
                queryDiagnostics = feedResponse.getCosmosDiagnostics().toString();
                assertThat(queryDiagnostics).contains("\"indexUtilizationInfo\"");
                assertThat(queryDiagnostics).contains("\"UtilizedSingleIndexes\"");
                assertThat(queryDiagnostics).contains("\"PotentialSingleIndexes\"");
                assertThat(queryDiagnostics).contains("\"UtilizedCompositeIndexes\"");
                assertThat(queryDiagnostics).contains("\"PotentialCompositeIndexes\"");
            }
        }
    }

    @Test(groups = {"fast"}, dataProvider = "query", timeOut = TIMEOUT)
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
                validateDirectModeQueryDiagnostics(queryDiagnostics, this.directClientUserAgent);
                if (query.contains("group by")) {
                    qroupByFirstResponse = false;
                }
            }
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void queryDiagnosticsOnOrderBy() {
        //  create container with more than 4 physical partitions
        String containerId = "testcontainer";
        cosmosAsyncDatabase.createContainer(containerId, "/mypk",
            ThroughputProperties.createManualThroughput(40000)).block();
        CosmosAsyncContainer testcontainer = cosmosAsyncDatabase.getContainer(containerId);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
        testcontainer.createItem(getInternalObjectNode()).block();
        options.setMaxDegreeOfParallelism(-1);
        String query = "SELECT * from c ORDER BY c._ts DESC";
        CosmosPagedFlux<InternalObjectNode> cosmosPagedFlux = testcontainer.queryItems(query, options,
            InternalObjectNode.class);
        Set<String> partitionKeyRangeIds = new HashSet<>();
        Set<String> pkRids = new HashSet<>();
        cosmosPagedFlux.byPage().flatMap(feedResponse -> {
            String cosmosDiagnosticsString = feedResponse.getCosmosDiagnostics().toString();
            //  find all partition key range ids in cosmos diagnostics
            Pattern pattern = Pattern.compile("(\"partitionKeyRangeId\":\")(\\d)");
            Matcher matcher = pattern.matcher(cosmosDiagnosticsString);
            while (matcher.find()) {
                //  get the partition key range id from cosmos diagnostics
                String group = matcher.group(2);
                partitionKeyRangeIds.add(group);
            }
            //  find all partition key range ids in query metrics
            pattern = Pattern.compile("(pkrId:)(\\d)");
            matcher = pattern.matcher(cosmosDiagnosticsString);
            while (matcher.find()) {
                //  get the partition key range id from query metrics
                String group = matcher.group(2);
                pkRids.add(group);
            }
            return Flux.just(feedResponse);
        }).blockLast();

        // assert that cosmos diagnostics has diagnostics information for all partitions ids same as query metrics
        assertThat(pkRids).isNotEmpty();
        assertThat(pkRids).isEqualTo(partitionKeyRangeIds);

        deleteCollection(testcontainer);
    }

    @Test(groups = {"fast"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void directDiagnosticsOnCancelledOperation(OperationType operationType) {

        CosmosAsyncClient client = null;
        FaultInjectionRule faultInjectionRule = null;

        try {
            client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(TestConfigurations.HOST)
                .endToEndOperationLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(1)).build()
                ).buildAsyncClient();

            CosmosAsyncContainer container =
                client.getDatabase(containerDirect.asyncContainer.getDatabase().getId()).getContainer(containerDirect.getId());

            TestItem testItem = TestItem.createNewItem();
            container.createItem(testItem).block();

            faultInjectionRule =
                new FaultInjectionRuleBuilder("responseDelay")
                    .condition(new FaultInjectionConditionBuilder().build())
                    .result(
                        FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                            .delay(Duration.ofSeconds(2))
                            .build()
                    )
                    .build();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(faultInjectionRule)).block();
            this.performDocumentOperation(container, operationType, testItem);
            fail("expected OperationCancelledException");
        } catch (CosmosException e) {
                assertThat(e).isInstanceOf(OperationCancelledException.class);
                String cosmosDiagnosticsString = e.getDiagnostics().toString();
                assertThat(cosmosDiagnosticsString).contains("\"statusCode\":408");
                assertThat(cosmosDiagnosticsString).contains("\"subStatusCode\":20008");
        } finally {
            if (faultInjectionRule != null) {
                faultInjectionRule.disable();
            }
            safeClose(client);
        }
    }

    @Test(groups = {"fast"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void directDiagnostics_WithFaultInjection(OperationType operationType) {

        CosmosAsyncClient client = null;
        FaultInjectionRule faultInjectionRule = null;

        try {
            client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(TestConfigurations.HOST)
                .buildAsyncClient();

            CosmosAsyncContainer container =
                client.getDatabase(containerDirect.asyncContainer.getDatabase().getId()).getContainer(containerDirect.getId());

            TestItem testItem = TestItem.createNewItem();
            container.createItem(testItem).block();

            faultInjectionRule =
                new FaultInjectionRuleBuilder("serverResponseError")
                    .condition(new FaultInjectionConditionBuilder().build())
                    .result(
                        FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.GONE)
                            .times(1)
                            .build()
                    )
                    .build();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(faultInjectionRule)).block();
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(container, operationType, testItem);

            assertThat(cosmosDiagnostics).isNotNull();
            String diagnosticsString = cosmosDiagnostics.toString();
            assertThat(diagnosticsString).contains("\"statusCode\":410");
            assertThat(diagnosticsString).contains("\"subStatusCode\":21005");
            assertThat(diagnosticsString).doesNotContain("\"statusCode\":408");
            assertThat(diagnosticsString).doesNotContain("\"subStatusCode\":20008");

        } catch (CosmosException e) {
            fail("Request should succeeded but failed with " + e.getMessage());

        } finally {
            if (faultInjectionRule != null) {
                faultInjectionRule.disable();
            }
            safeClose(client);
        }
    }

    private void validateDirectModeDiagnosticsOnSuccess(
        CosmosDiagnostics cosmosDiagnostics,
        CosmosClient testDirectClient,
        String userAgent) throws Exception {

        String diagnostics = cosmosDiagnostics.toString();
        logger.info("DIAGNOSTICS: {}", diagnostics);
        assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
        assertThat(diagnostics).contains("supplementalResponseStatisticsList");
        assertThat(diagnostics).contains("gatewayStatisticsList");
        assertThat(diagnostics).contains("addressResolutionStatistics");
        assertThat(diagnostics).contains("\"metaDataName\":\"CONTAINER_LOOK_UP\"");
        assertThat(diagnostics).contains("\"metaDataName\":\"PARTITION_KEY_RANGE_LOOK_UP\"");
        assertThat(diagnostics).contains("\"metaDataName\":\"SERVER_ADDRESS_LOOKUP\"");
        assertThat(diagnostics).contains("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
        assertThat(diagnostics).contains("\"userAgent\":\"" + userAgent + "\"");
        assertThat(diagnostics).containsAnyOf(
            "\"machineId\":\"" + tempMachineId + "\"", // logged machineId should be static uuid or
            "\"machineId\":\"" + ClientTelemetry.getMachineId(null) + "\"" // the vmId from Azure
        );
        assertThat(diagnostics).containsPattern("(?s).*?\"activityId\":\"[^\\s\"]+\".*");
        assertThat(diagnostics).contains("\"backendLatencyInMs\"");
        assertThat(diagnostics).contains("\"retryAfterInMs\"");
        assertThat(diagnostics).contains("\"channelStatistics\"");

        assertThat(cosmosDiagnostics.getContactedRegionNames()).isNotEmpty();
        assertThat(cosmosDiagnostics.getDuration()).isNotNull();
        validateTransportRequestTimelineDirect(diagnostics);
        validateRegionContacted(cosmosDiagnostics, testDirectClient.asyncClient());
        validateChannelStatistics(cosmosDiagnostics);
        isValidJSON(diagnostics);
    }

    private void validateDirectModeDiagnosticsOnException(CosmosException cosmosException, String userAgent) {
        CosmosDiagnostics cosmosDiagnostics = cosmosException.getDiagnostics();
        String diagnosticsString = cosmosDiagnostics.toString();
        assertThat(diagnosticsString).contains("\"backendLatencyInMs\"");
        assertThat(diagnosticsString).contains("\"userAgent\":\"" + userAgent + "\"");
        assertThat(diagnosticsString).contains("\"retryAfterInMs\"");
        assertThat(diagnosticsString).contains("\"exceptionMessage\":\"[\\\"Resource with specified id or name already exists.\\\"]\"");
        assertThat(diagnosticsString).contains("\"exceptionResponseHeaders\"");
        assertThat(diagnosticsString).doesNotContain("\"exceptionResponseHeaders\": \"{}\"");
        validateTransportRequestTimelineDirect(diagnosticsString);
        validateChannelStatistics(cosmosDiagnostics);

        if (!(cosmosException instanceof OperationCancelledException)) {
            assertThat(diagnosticsString).doesNotContain("\"statusCode\":408");
            assertThat(diagnosticsString).doesNotContain("\"subStatusCode\":20008");
        }
    }

    private void validateDirectModeQueryDiagnostics(String diagnostics, String userAgent) {
        assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
        assertThat(diagnostics).contains("supplementalResponseStatisticsList");
        assertThat(diagnostics).contains("responseStatisticsList");
        assertThat(diagnostics).contains("gatewayStatisticsList");
        assertThat(diagnostics).contains("addressResolutionStatistics");
        assertThat(diagnostics).contains("\"userAgent\":\"" + userAgent + "\"");
        assertThat(diagnostics).containsPattern("(?s).*?\"activityId\":\"[^\\s\"]+\".*");
    }

    private void validateGatewayModeQueryDiagnostics(String diagnostics, String userAgent) {
        assertThat(diagnostics).contains("\"connectionMode\":\"GATEWAY\"");
        assertThat(diagnostics).contains(("gatewayStatisticsList"));
        assertThat(diagnostics).contains("\"operationType\":\"Query\"");
        assertThat(diagnostics).contains("\"userAgent\":\"" + userAgent + "\"");
        assertThat(diagnostics).containsPattern("(?s).*?\"activityId\":\"[^\\s\"]+\".*");
        assertThat(diagnostics).contains("\"regionsContacted\"");
    }

    @Test(groups = {"fast"}, dataProvider = "query", timeOut = TIMEOUT*2)
    public void queryDiagnosticsGatewayMode(String query, Boolean qmEnabled) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        List<String> itemIdList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            containerGateway.createItem(internalObjectNode);
            if (i % 20 == 0) {
                itemIdList.add(internalObjectNode.getId());
            }
        }
        boolean qroupByFirstResponse = true;
        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }
        Iterator<FeedResponse<InternalObjectNode>> iterator = containerGateway
                                                                  .queryItems(query, options, InternalObjectNode.class)
                                                                  .iterableByPage()
                                                                  .iterator();
        assertThat(iterator.hasNext()).isTrue();

        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            String queryDiagnostics = feedResponse.getCosmosDiagnostics().toString();
            assertThat(feedResponse.getResults().size()).isEqualTo(0);
            if (!query.contains("group by") || qroupByFirstResponse) {
                validateQueryDiagnostics(queryDiagnostics, qmEnabled, true);
                validateGatewayModeQueryDiagnostics(queryDiagnostics, this.gatewayClientUserAgent);
                if (query.contains("group by")) {
                    qroupByFirstResponse = false;
                }
            }
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void queryMetricsWithADifferentLocale() {

        Locale.setDefault(Locale.GERMAN);
        String query = "select * from root where root.id= \"someid\"";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        Iterator<FeedResponse<InternalObjectNode>> iterator = this.containerGateway.queryItems(query, options,
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
            assertThat(queryDiagnostics).contains("retrievedDocumentCount");
            assertThat(queryDiagnostics).contains("queryPreparationTimes");
            assertThat(queryDiagnostics).contains("runtimeExecutionTimes");
            assertThat(queryDiagnostics).contains("fetchExecutionRanges");
        } else {
            assertThat(queryDiagnostics).doesNotContain("retrievedDocumentCount");
            assertThat(queryDiagnostics).doesNotContain("queryPreparationTimes");
            assertThat(queryDiagnostics).doesNotContain("runtimeExecutionTimes");
            assertThat(queryDiagnostics).doesNotContain("fetchExecutionRanges");
        }

        if (expectQueryPlanDiagnostics) {
            assertThat(queryDiagnostics).contains("queryPlanDiagnosticsContext");
            assertThat(queryDiagnostics).contains("startTimeUTC");
            assertThat(queryDiagnostics).contains("endTimeUTC");
        } else {
            assertThat(queryDiagnostics).contains("\"queryPlanDiagnosticsContext\":null");
        }
    }

    @Test(groups = {"fast"}, dataProvider = "readAllItemsOfLogicalPartition", timeOut = TIMEOUT)
    public void queryMetricsForReadAllItemsOfLogicalPartition(Integer expectedItemCount, Boolean qmEnabled) {
        String pkValue = UUID.randomUUID().toString();

        for (int i = 0; i < expectedItemCount; i++) {
            InternalObjectNode internalObjectNode = getInternalObjectNode(pkValue);
            CosmosItemResponse<InternalObjectNode> createResponse = containerGateway.createItem(internalObjectNode);
        }

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        if (qmEnabled != null) {
            options = options.setQueryMetricsEnabled(qmEnabled);
        }
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, 5);

        Iterator<FeedResponse<InternalObjectNode>> iterator =
            this.containerGateway
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

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void directDiagnosticsOnException() throws Exception {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        CosmosItemResponse<InternalObjectNode> createResponse = null;
        try {
            createResponse = containerDirect.createItem(internalObjectNode);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            ModelBridgeInternal.setPartitionKey(cosmosItemRequestOptions, new PartitionKey("wrongPartitionKey"));
            CosmosItemResponse<InternalObjectNode> readResponse =
                containerDirect.readItem(BridgeInternal.getProperties(createResponse).getId(),
                    new PartitionKey("wrongPartitionKey"),
                    InternalObjectNode.class);
            fail("request should fail as partition key is wrong");
        } catch (CosmosException exception) {
            isValidJSON(exception.toString());
            isValidJSON(exception.getMessage());
            String diagnostics = exception.getDiagnostics().toString();
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(diagnostics).contains("\"connectionMode\":\"DIRECT\"");
            assertThat(diagnostics).contains("\"userAgent\":\"" + this.directClientUserAgent + "\"");
            assertThat(diagnostics).doesNotContain(("\"resourceAddress\":null"));
            assertThat(exception.getDiagnostics().getContactedRegionNames()).isNotEmpty();
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            assertThat(diagnostics).contains("\"backendLatencyInMs\"");
            assertThat(diagnostics).contains("\"retryAfterInMs\"");
            assertThat(diagnostics).containsPattern("(?s).*?\"activityId\":\"[^\\s\"]+\".*");
            assertThat(diagnostics).contains("\"exceptionMessage\":\"[\\\"Resource Not Found.");
            assertThat(diagnostics).contains("\"exceptionResponseHeaders\"");
            assertThat(diagnostics).doesNotContain("\"exceptionResponseHeaders\":null");
            isValidJSON(diagnostics);
            validateTransportRequestTimelineDirect(diagnostics);
            validateRegionContacted(createResponse.getDiagnostics(), directClient.asyncClient());

            // TODO: add better store result diagnostic validation on exception
            ObjectNode diagnosticsNode = (ObjectNode) OBJECT_MAPPER.readTree(diagnostics);
            JsonNode responseStatisticsList = diagnosticsNode.get("responseStatisticsList");
            assertThat(responseStatisticsList.isArray()).isTrue();
            assertThat(responseStatisticsList.size()).isGreaterThan(0);
            JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");
            assertThat(storeResult).isNotNull();
            int currentReplicaSetSize = storeResult.get("currentReplicaSetSize").asInt(-1);
            assertThat(currentReplicaSetSize).isEqualTo(-1);
            JsonNode replicaStatusList = storeResult.get("replicaStatusList");
            assertThat(replicaStatusList.isObject()).isTrue();
            int quorumAcked = storeResult.get("quorumAckedLSN").asInt(-1);
            assertThat(quorumAcked).isEqualTo(-1);
        }
    }

    @Test(groups = {"fast"}, dataProvider = "gatewayAndDirect", timeOut = TIMEOUT)
    public void diagnosticsKeywordIdentifiers(CosmosContainer container) {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        HashSet<String> keywordIdentifiers = new HashSet<>();
        keywordIdentifiers.add("orderId");
        keywordIdentifiers.add("customerId");
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions().setKeywordIdentifiers(keywordIdentifiers);
        CosmosItemResponse<InternalObjectNode> createResponse = container.createItem(internalObjectNode, cosmosItemRequestOptions);
        ArrayList<String> diagnosticsList = new ArrayList<>();
        String diagnostics = createResponse.getDiagnostics().toString();
        diagnosticsList.add(diagnostics);
        diagnostics = container.readItem(internalObjectNode.getId(), new PartitionKey(internalObjectNode.get("mypk")), cosmosItemRequestOptions,
            InternalObjectNode.class).getDiagnostics().toString();
        diagnosticsList.add(diagnostics);
        diagnostics = container.upsertItem(internalObjectNode, cosmosItemRequestOptions).getDiagnostics().toString();
        diagnosticsList.add(diagnostics);

        InternalObjectNode updatedInternalObjectNode = getInternalObjectNode(internalObjectNode.get("mypk").toString());
        updatedInternalObjectNode.setId(internalObjectNode.getId());;
        diagnostics = container.replaceItem(updatedInternalObjectNode, updatedInternalObjectNode.get("mypk").toString(), new PartitionKey(internalObjectNode.getId()), cosmosItemRequestOptions).getDiagnostics().toString();
        diagnosticsList.add(diagnostics);

        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions().setKeywordIdentifiers(keywordIdentifiers);

        for (FeedResponse<InternalObjectNode> feedResponse : container.queryItems("SELECT * from c", queryRequestOptions, InternalObjectNode.class).iterableByPage()) {
            diagnostics = feedResponse.getCosmosDiagnostics().toString();
            diagnosticsList.add(diagnostics);
        }

        for (String diagnostic : diagnosticsList) {
            isValidJSON(diagnostic);
            assertThat(diagnostic).contains("\"keywordIdentifiers\":[\"orderId\",\"customerId\"]");
        }
    }

    @Test(groups = {"fast"}, dataProvider = "gatewayAndDirect", timeOut = TIMEOUT)
    public void diagnosticsKeywordIdentifiersOnException(CosmosContainer container) {
        InternalObjectNode internalObjectNode = getInternalObjectNode();
        HashSet<String> keywordIdentifiers = new HashSet<>();
        keywordIdentifiers.add("orderId");
        keywordIdentifiers.add("customerId");
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions().setKeywordIdentifiers(keywordIdentifiers);
        CosmosItemResponse<InternalObjectNode> createResponse = container.createItem(internalObjectNode, cosmosItemRequestOptions);
        try {
            container.readItem(internalObjectNode.getId(), new PartitionKey("Wrong Partition Key"), cosmosItemRequestOptions,
                InternalObjectNode.class);
            fail("request should fail as partition key is wrong");
        } catch (CosmosException e) {
            String diagnostics = e.getDiagnostics().toString();
            isValidJSON(diagnostics);
            assertThat(diagnostics).contains("\"keywordIdentifiers\":[\"orderId\",\"customerId\"]");
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
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
            assertThat(diagnostics).contains("\"exceptionMessage\":\"TestBadRequest\"");
            assertThat(diagnostics).doesNotContain(("\"resourceAddress\":null"));
            assertThat(diagnostics).contains("\"resourceType\":\"DocumentCollection\"");
            assertThat(exception.getDiagnostics().getContactedRegionNames()).isNotEmpty();
            assertThat(exception.getDiagnostics().getDuration()).isNotNull();
            isValidJSON(diagnostics);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void supplementalResponseStatisticsList() throws Exception {
        ClientSideRequestStatistics clientSideRequestStatistics = new ClientSideRequestStatistics(mockDiagnosticsClientContext());
        for (int i = 0; i < 15; i++) {
            RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Head, ResourceType.Document);
            clientSideRequestStatistics.recordResponse(rxDocumentServiceRequest, null, null);
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
            clientSideRequestStatistics.recordResponse(rxDocumentServiceRequest, null, null);
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
            assertThat(node.get("requestSessionToken")).isNotNull();
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void serializationOnVariousScenarios() {
        //checking database serialization
        CosmosDatabaseResponse cosmosDatabase = gatewayClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).read();
        String diagnostics = cosmosDatabase.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"DATABASE_DESERIALIZATION\"");

        //checking container serialization
        CosmosContainerResponse containerResponse = this.containerGateway.read();
        diagnostics = containerResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"CONTAINER_DESERIALIZATION\"");
        TestItem testItem = new TestItem();
        testItem.id = "TestId";
        testItem.mypk = "TestPk";

        //checking partitionKeyFetch serialization
        CosmosItemResponse<TestItem> itemResponse = this.containerGateway.createItem(testItem);
        diagnostics = itemResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
        testItem.id = "TestId2";
        testItem.mypk = "TestPk";
        itemResponse = this.containerGateway.createItem(testItem, new PartitionKey("TestPk"), null);
        diagnostics = itemResponse.getDiagnostics().toString();
        assertThat(diagnostics).doesNotContain("\"serializationType\":\"PARTITION_KEY_FETCH_SERIALIZATION\"");
        assertThat(diagnostics).doesNotContain("\"serializationType\":\"ITEM_DESERIALIZATION\"");

        //checking item serialization
        TestItem readTestItem = itemResponse.getItem();
        diagnostics = itemResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"ITEM_DESERIALIZATION\"");

        CosmosItemResponse<InternalObjectNode> readItemResponse = this.containerGateway.readItem(testItem.id, new PartitionKey(testItem.mypk), null, InternalObjectNode.class);
        InternalObjectNode properties = readItemResponse.getItem();
        diagnostics = readItemResponse.getDiagnostics().toString();
        assertThat(diagnostics).contains("\"serializationType\":\"ITEM_DESERIALIZATION\"");
        assertThat(diagnostics).contains("\"userAgent\":\"" + this.gatewayClientUserAgent + "\"");
        assertThat(diagnostics).containsPattern("(?s).*?\"activityId\":\"[^\\s\"]+\".*");
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
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
            logger.info("Diagnostics are : {}", e.getDiagnostics());
            String diagnostics = e.getDiagnostics().toString();
            assertThat(diagnostics).contains("\"exceptionMessage\":\"[\\\"Resource with specified id or name already exists.\\\"]\"");
            assertThat(diagnostics).contains("\"exceptionResponseHeaders\"");
            assertThat(diagnostics).doesNotContain("\"exceptionResponseHeaders\": \"{}\"");

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

    @Test(groups = {"fast"}, dataProvider = "connectionStateListenerArgProvider", timeOut = TIMEOUT)
    public void rntbdStatistics(boolean connectionStateListenerEnabled) throws Exception {
        Instant beforeClientInitialization = Instant.now();

        CosmosClient client1 = null;
        try {

            DirectConnectionConfig connectionConfig = DirectConnectionConfig.getDefaultConfig();
            connectionConfig.setConnectionEndpointRediscoveryEnabled(connectionStateListenerEnabled);

            client1 = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .directMode(connectionConfig)
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
                afterOperation3,
                connectionStateListenerEnabled);

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
                                         Instant afterOperation3,
                                         boolean connectionStateListenerEnabled) throws Exception {
        ObjectNode diagnostics = (ObjectNode) OBJECT_MAPPER.readTree(cosmosDiagnostics.toString());
        JsonNode responseStatisticsList = diagnostics.get("responseStatisticsList");
        assertThat(responseStatisticsList.isArray()).isTrue();
        assertThat(responseStatisticsList.size()).isGreaterThan(0);
        JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");
        assertThat(storeResult).isNotNull();
        int replicaSetSize = storeResult.get("currentReplicaSetSize").asInt(-1);
        assertThat(replicaSetSize).isGreaterThan(0);
        JsonNode replicaStatusList = storeResult.get("replicaStatusList");
        assertThat(replicaStatusList.isObject()).isTrue();
        int replicasNum = replicaStatusList.get(Uri.ATTEMPTING).size() + replicaStatusList.get(Uri.IGNORING).size();
        assertThat(replicasNum).isEqualTo(replicaSetSize);
        String replicaStatusTxt = replicaStatusList.get(Uri.ATTEMPTING).get(0).asText();
        assertThat(replicaStatusTxt.contains("P")).isTrue();
        assertThat(replicaStatusTxt.contains("Connected")).isTrue();
        // validate serviceEndpointStatistics
        JsonNode serviceEndpointStatistics = storeResult.get("serviceEndpointStatistics");
        assertThat(serviceEndpointStatistics).isNotNull();

        assertThat(serviceEndpointStatistics.get("availableChannels").asInt(-1)).isGreaterThan(0);
        // no concurrent work
        assertThat(serviceEndpointStatistics.get("acquiredChannels").asInt(-1)).isEqualTo(0);
        // current request
        assertThat(serviceEndpointStatistics.get("inflightRequests").asInt(-1)).isEqualTo(1);

        assertThat(serviceEndpointStatistics.get("isClosed").asBoolean()).isEqualTo(false);

        // validate connectionStats
        JsonNode channelStatistics = storeResult.get("channelStatistics");
        assertThat(channelStatistics).isNotNull();
        assertThat(channelStatistics.get("channelId").asText()).isNotEmpty();
        assertThat(channelStatistics.get("channelTaskQueueSize").asInt(-1)).isGreaterThanOrEqualTo(0);
        assertThat(channelStatistics.get("pendingRequestsCount").asInt(-1)).isGreaterThanOrEqualTo(0);
        assertThat(channelStatistics.get("lastReadTime").asText()).isNotEmpty();
        assertThat(channelStatistics.get("waitForConnectionInit").asText()).isNotEmpty();

        JsonNode connectionStateListenerMetrics = serviceEndpointStatistics.get("cerMetrics");
        if (connectionStateListenerEnabled) {

            assertThat(connectionStateListenerMetrics).isNotNull();
            assertThat(connectionStateListenerMetrics.get("lastCallTimestamp")).isNull();
            assertThat(connectionStateListenerMetrics.get("lastActionableContext")).isNull();
        } else {
            assertThat(connectionStateListenerMetrics).isNull();
        }

        // first request initialized the rntbd service endpoint
        Instant beforeInitializationThreshold = beforeInitializingRntbdServiceEndpoint.minusMillis(5);
        assertThat(Instant.parse(serviceEndpointStatistics.get("createdTime").asText()))
            .isAfterOrEqualTo(beforeInitializationThreshold);

        // Adding 5 ms to cover for rounding errors (only 3 fractional digits)
        Instant afterInitializationThreshold = afterInitializingRntbdServiceEndpoint.plusMillis(5);
        assertThat(Instant.parse(serviceEndpointStatistics.get("createdTime").asText()))
            .isBeforeOrEqualTo(afterInitializationThreshold);

        // Adding 5 ms to cover for rounding errors (only 3 fractional digits)
        Instant afterOperation2Threshold = afterOperation2.plusMillis(5);
        Instant beforeOperation2Threshold = beforeOperation2.minusMillis(5);
        assertThat(Instant.parse(serviceEndpointStatistics.get("lastRequestTime").asText()))
            .isAfterOrEqualTo(beforeOperation2Threshold.toString())
            .isBeforeOrEqualTo(afterOperation2Threshold.toString());
        assertThat(Instant.parse(serviceEndpointStatistics.get("lastSuccessfulRequestTime").asText()))
            .isAfterOrEqualTo(beforeOperation2Threshold.toString())
            .isBeforeOrEqualTo(afterOperation2Threshold.toString());
    }

    private void validate(CosmosDiagnostics cosmosDiagnostics, int expectedRequestPayloadSize, int expectedResponsePayloadSize) throws Exception {
        ObjectNode diagnostics = (ObjectNode) OBJECT_MAPPER.readTree(cosmosDiagnostics.toString());
        JsonNode responseStatisticsList = diagnostics.get("responseStatisticsList");
        assertThat(responseStatisticsList.isArray()).isTrue();
        assertThat(responseStatisticsList.size()).isGreaterThan(0);
        JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");

        boolean hasPayload = storeResult.get("exceptionMessage") == null;
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
            assertThat(writeResourceResponse.getDiagnostics().toString()).contains("\"exceptionMessage\":null");

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
            assertThat(readResourceResponse.getDiagnostics().toString()).contains("\"exceptionMessage\":\"io.netty" +
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

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void responseStatisticRequestStartTimeUTCForDirectCall() {
        CosmosAsyncClient client = null;
        String databaseId = DatabaseForTest.generateId();
        FaultInjectionRule faultInjectionRule = null;

        try {
            client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(TestConfigurations.HOST)
                .endToEndOperationLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2)).build()
                ).buildAsyncClient();

            createDatabase(client, databaseId);
            CosmosAsyncContainer container = createCollection(client, databaseId, getCollectionDefinition());

            TestItem testItem = TestItem.createNewItem();
            container.createItem(testItem).block();

            faultInjectionRule =
                new FaultInjectionRuleBuilder("PARTITION_IS_MIGRATING")
                    .condition(
                        new FaultInjectionConditionBuilder()
                            .operationType(FaultInjectionOperationType.CREATE_ITEM)
                            .build()
                    )
                    .result(
                        FaultInjectionResultBuilders
                            .getResultBuilder(FaultInjectionServerErrorType.PARTITION_IS_MIGRATING)
                            .times(1)
                            .build()
                    )
                    .duration(Duration.ofMinutes(5))
                    .build();
            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(faultInjectionRule)).block();

            CosmosDiagnostics cosmosDiagnostics = null;
            try {
                cosmosDiagnostics = container.createItem(TestItem.createNewItem())
                    .block()
                    .getDiagnostics();

            } catch (Exception exception) {
                fail("Request should succeeded, but failed with " + exception);
            }

            Collection<ClientSideRequestStatistics> clientSideRequestStatistics = cosmosDiagnostics.getClientSideRequestStatistics();
            ClientSideRequestStatistics.StoreResponseStatistics[] responseStatistic =
                clientSideRequestStatistics.iterator()
                                           .next()
                                           .getResponseStatisticsList()
                                           .toArray(new ClientSideRequestStatistics.StoreResponseStatistics[0]);

            assert responseStatistic.length == 2;

            Instant firstRequestStartTime = responseStatistic[0].getRequestStartTimeUTC();
            Instant secondRequestStartTime = responseStatistic[1].getRequestStartTimeUTC();

            assert firstRequestStartTime != null && secondRequestStartTime != null;
            assert firstRequestStartTime != secondRequestStartTime;
            assert firstRequestStartTime.compareTo(secondRequestStartTime) < 0;

        } finally {
            if (faultInjectionRule != null) {
                faultInjectionRule.disable();
            }
            safeClose(client);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void negativeE2ETimeoutWithPointOperation() {
        CosmosAsyncClient client = null;
        String databaseId = DatabaseForTest.generateId();

        try {
            client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(TestConfigurations.HOST)
                .buildAsyncClient();

            createDatabase(client, databaseId);
            CosmosAsyncContainer container = createCollection(client, databaseId, getCollectionDefinition());

            TestItem testItem = TestItem.createNewItem();
            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(-1)).build()
            );
            container.createItem(testItem, requestOptions).block();
            fail("This should have failed with an exception");
        } catch(OperationCancelledException cancelledException) {
            assertThat(cancelledException).isNotNull();
            assertThat(cancelledException.getStatusCode()).isEqualTo(408);
            assertThat(cancelledException.getSubStatusCode())
                .isEqualTo(HttpConstants.SubStatusCodes.NEGATIVE_TIMEOUT_PROVIDED);
            assertThat(cancelledException.getDiagnostics()).isNotNull();
            logger.info("Expected request timeout: ", cancelledException);
        }
        finally {
            safeClose(client);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void negativeE2ETimeoutWithQueryOperation() {
        CosmosAsyncClient client = null;
        String databaseId = DatabaseForTest.generateId();

        try {
            client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(TestConfigurations.HOST)
                .buildAsyncClient();

            createDatabase(client, databaseId);
            CosmosAsyncContainer container = createCollection(client, databaseId, getCollectionDefinition());

            TestItem testItem = TestItem.createNewItem();
            container.createItem(testItem).block();

            CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
            requestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(-1)).build()
            );
            CosmosPagedFlux<ObjectNode> flux = container.readAllItems(requestOptions, ObjectNode.class);
            List<ObjectNode> results = flux.collectList().block();

            fail("This should have failed with an exception");
        } catch(OperationCancelledException cancelledException) {
            assertThat(cancelledException).isNotNull();
            assertThat(cancelledException.getStatusCode()).isEqualTo(408);
            assertThat(cancelledException.getSubStatusCode())
                .isEqualTo(HttpConstants.SubStatusCodes.NEGATIVE_TIMEOUT_PROVIDED);
            // No pending requests - so, diagnostics are not guaranteed to be there
            // assertThat(cancelledException.getDiagnostics()).isNotNull();
            logger.info("Expected request timeout: ", cancelledException);
        }
        finally {
            safeClose(client);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void directDiagnosticsWithChannelAcquisitionContext() throws Exception {
        InternalObjectNode internalObjectNode = getInternalObjectNode();

        CosmosAsyncClient testClient = null;
        FaultInjectionRule connectionDelayRule =
                new FaultInjectionRuleBuilder("connectionDelay")
                        .condition(new FaultInjectionConditionBuilder().build())
                        .result(
                                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                                        .delay(Duration.ofSeconds(2))
                                        .build()
                        )
                        .build();

        FaultInjectionRule closeConnectionsRule =
                new FaultInjectionRuleBuilder("connectionClose")
                        .condition(new FaultInjectionConditionBuilder().build())
                        .result(
                                FaultInjectionResultBuilders
                                        .getResultBuilder(FaultInjectionConnectionErrorType.CONNECTION_CLOSE)
                                        .interval(Duration.ofMillis(10))
                                        .threshold(1.0)
                                        .build())
                        .duration(Duration.ofMillis(50))
                        .build();

        try {
            String userAgentSuffix = "testForChannelAcquisitionContext";
            testClient = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .contentResponseOnWriteEnabled(true)
                    .userAgentSuffix(userAgentSuffix)
                    .buildAsyncClient();

            CosmosAsyncContainer container = testClient.getDatabase(cosmosAsyncDatabase.getId()).getContainer(cosmosAsyncContainer.getId());

            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(connectionDelayRule)).block();
            CosmosItemResponse<InternalObjectNode> createResponse = container.createItem(internalObjectNode).block();
            validateChannelAcquisitionContext(createResponse.getDiagnostics(), true);

            try {
                CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(closeConnectionsRule)).block();
                // wait for some time to let the connection close rule kick in
                Thread.sleep(100);
                container.createItem(internalObjectNode).block();
                fail("expected 409");
            } catch (CosmosException e) {
                validateChannelAcquisitionContext(e.getDiagnostics(), true);
            }

        } finally {
            safeClose(testClient);
        }
    }

    @Test(groups = { "fast" })
    public void expireRecordWhenRecordAlreadyCompleteExceptionally() throws URISyntaxException, JsonProcessingException {
        CosmosAsyncClient client = null;
        try {
            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .userAgentSuffix("expireRecordWhenRecordAlreadyCompleteExceptionally")
                .buildAsyncClient();

            CosmosAsyncContainer container = getSharedSinglePartitionCosmosContainer(client);
            CosmosException exception = null;
            try {
                container.readItem("randomId", new PartitionKey("randomId"), JsonNode.class).block();
            } catch (CosmosException e) {
                exception = e;
                assertThat(e.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                CosmosDiagnostics cosmosDiagnostics = e.getDiagnostics();
                assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();
                assertThat(cosmosDiagnostics.getDiagnosticsContext().getDiagnostics()).isNotEmpty();

                // validate serialize the cosmos diagnostics will succeeded
                Utils.getSimpleObjectMapper().writeValueAsString(cosmosDiagnostics);

                // validate serialize the cosmos diagnostics will succeeded
                Utils.getSimpleObjectMapper().writeValueAsString(cosmosDiagnostics.getDiagnosticsContext());
            }

            // complete a Rntbd request record
            RntbdRequestArgs requestArgs = new RntbdRequestArgs(
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document),
                new Uri(new URI("http://localhost/replica-path").toString())
            );
            RntbdRequestTimer requestTimer = new RntbdRequestTimer(5000, 5000);
            RntbdRequestRecord record = new AsyncRntbdRequestRecord(requestArgs, requestTimer);
            record.completeExceptionally(exception);
            // validate record.toString() will work correctly
            String recordString = record.toString();
            assertThat(recordString.contains("NotFoundException")).isTrue();
        } finally {
            safeClose(client);
        }
    }

    private InternalObjectNode getInternalObjectNode() {
        InternalObjectNode internalObjectNode = new InternalObjectNode();
        String uuid = UUID.randomUUID().toString();
        internalObjectNode.setId(uuid);
        internalObjectNode.set("mypk", uuid, CosmosItemSerializer.DEFAULT_SERIALIZER);
        return internalObjectNode;
    }

    private InternalObjectNode getInternalObjectNode(String pkValue) {
        InternalObjectNode internalObjectNode = new InternalObjectNode();
        String uuid = UUID.randomUUID().toString();
        internalObjectNode.setId(uuid);
        internalObjectNode.set( "mypk", pkValue, CosmosItemSerializer.DEFAULT_SERIALIZER);
        return internalObjectNode;
    }

    private List<ClientSideRequestStatistics.StoreResponseStatistics> getStoreResponseStatistics(ClientSideRequestStatistics requestStatistics) throws Exception {
        Field storeResponseStatisticsField = ClientSideRequestStatistics.class.getDeclaredField("supplementalResponseStatisticsList");
        storeResponseStatisticsField.setAccessible(true);
        @SuppressWarnings({"unchecked"})
        Collection<ClientSideRequestStatistics.StoreResponseStatistics> list
            = (Collection<ClientSideRequestStatistics.StoreResponseStatistics>) storeResponseStatisticsField.get(requestStatistics);
        return new ArrayList<>(list);
    }

    private void clearStoreResponseStatistics(ClientSideRequestStatistics requestStatistics) throws Exception {
        Field storeResponseStatisticsField = ClientSideRequestStatistics.class.getDeclaredField("supplementalResponseStatisticsList");
        storeResponseStatisticsField.setAccessible(true);
        storeResponseStatisticsField.set(requestStatistics, new ArrayList<ClientSideRequestStatistics.StoreResponseStatistics>());
    }

    private void validateTransportRequestTimelineGateway(String diagnostics) {
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
        assertThat(diagnostics).contains("\"eventName\":\"decodeTime");
        assertThat(diagnostics).contains("\"eventName\":\"received\"");
        assertThat(diagnostics).contains("\"eventName\":\"completed\"");
        assertThat(diagnostics).contains("\"startTimeUTC\"");
        assertThat(diagnostics).contains("\"durationInMilliSecs\"");
    }

    public void isValidJSON(final String json) {
        try {
            final JsonParser parser = new JsonFactory().createParser(json);
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

    private void validateRegionContacted(CosmosDiagnostics cosmosDiagnostics, CosmosAsyncClient cosmosAsyncClient) throws Exception {
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(cosmosAsyncClient);
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        LocationCache locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);

        Field locationInfoField = LocationCache.class.getDeclaredField("locationInfo");
        locationInfoField.setAccessible(true);
        Object locationInfo = locationInfoField.get(locationCache);

        Class<?> DatabaseAccountLocationsInfoClass = Class.forName("com.azure.cosmos.implementation.routing" +
            ".LocationCache$DatabaseAccountLocationsInfo");
        Field availableWriteLocations = DatabaseAccountLocationsInfoClass.getDeclaredField(
            "availableWriteLocations");
        availableWriteLocations.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) availableWriteLocations.get(locationInfo);
        String regionName = list.get(0);
        assertThat(cosmosDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
        assertThat(cosmosDiagnostics.getContactedRegionNames().iterator().next()).isEqualTo(regionName.toLowerCase());
    }

    private void validateChannelStatistics(CosmosDiagnostics cosmosDiagnostics) {
        for (ClientSideRequestStatistics clientSideRequestStatistics : cosmosDiagnostics.getClientSideRequestStatistics()) {
            for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics : clientSideRequestStatistics.getResponseStatisticsList()) {
                assertThat(storeResponseStatistics).isNotNull();
                RntbdChannelStatistics rntbdChannelStatistics =
                    storeResponseStatistics
                        .getStoreResult()
                        .getStoreResponseDiagnostics()
                        .getRntbdChannelStatistics();
                assertThat(rntbdChannelStatistics).isNotNull();

                try {
                    String rntbdChannelStatisticsString =
                        Utils.getSimpleObjectMapper().writeValueAsString(rntbdChannelStatistics);
                    assertThat(rntbdChannelStatisticsString).contains("\"channelId\":\"" + rntbdChannelStatistics.getChannelId() + "\"");
                    assertThat(rntbdChannelStatisticsString)
                        .contains("\"channelTaskQueueSize\":" + rntbdChannelStatistics.getChannelTaskQueueSize());
                    assertThat(rntbdChannelStatisticsString)
                        .contains("\"pendingRequestsCount\":" + rntbdChannelStatistics.getPendingRequestsCount());
                    assertThat(rntbdChannelStatisticsString)
                        .contains("\"lastReadTime\":\"" + rntbdChannelStatistics.getLastReadTime() + "\"");

                    if (rntbdChannelStatistics.getTransitTimeoutCount() > 0) {
                        assertThat(rntbdChannelStatisticsString)
                            .contains("\"transitTimeoutCount\":" + rntbdChannelStatistics.getTransitTimeoutCount());
                        assertThat(rntbdChannelStatisticsString)
                            .contains("\"transitTimeoutStartingTime\":\"" + rntbdChannelStatistics.getTransitTimeoutStartingTime() + "\"");
                    } else {
                        assertThat(rntbdChannelStatisticsString)
                            .doesNotContain("\"transitTimeoutCount\":" + rntbdChannelStatistics.getTransitTimeoutCount());
                        assertThat(rntbdChannelStatisticsString)
                            .doesNotContain("\"transitTimeoutStartingTime\":\"" + rntbdChannelStatistics.getTransitTimeoutStartingTime() + "\"");
                    }

                    assertThat(rntbdChannelStatisticsString).contains("\"waitForConnectionInit\":" + rntbdChannelStatistics.isWaitForConnectionInit());
                } catch (JsonProcessingException e) {
                    fail("Failed to parse RntbdChannelStatistics");
                }
            }
        }
    }

    private void validateChannelAcquisitionContext(CosmosDiagnostics diagnostics, boolean channelAcquisitionContextExists) {
        String diagnosticsString = diagnostics.toString();

        if (channelAcquisitionContextExists) {
            assertThat(diagnosticsString).contains("\"transportRequestChannelAcquisitionContext\"");
        } else {
            assertThat(diagnosticsString).doesNotContain("\"transportRequestChannelAcquisitionContext\"");
        }
    }

    private CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestItem createdItem) {
        if (operationType == OperationType.Query) {
            String query = "SELECT * from c";
            FeedResponse<TestItem> itemFeedResponse =
                cosmosAsyncContainer.queryItems(query, TestItem.class).byPage().blockFirst();

            return itemFeedResponse.getCosmosDiagnostics();
        }


        if (operationType == OperationType.Read) {
            return cosmosAsyncContainer
                .readItem(createdItem.id, new PartitionKey(createdItem.mypk), TestItem.class)
                .block()
                .getDiagnostics();
        }

        if (operationType == OperationType.Replace) {
            return cosmosAsyncContainer
                .replaceItem(createdItem, createdItem.id, new PartitionKey(createdItem.mypk))
                .block()
                .getDiagnostics();
        }

        if (operationType == OperationType.Create) {
            return cosmosAsyncContainer.createItem(TestItem.createNewItem()).block().getDiagnostics();
        }

        throw new IllegalArgumentException("The operation type is not supported");
    }

    public static class TestItem {
        public String id;
        public String mypk;

        public TestItem() {
        }

        public TestItem(String id, String mypk) {
            this.id = id;
            this.mypk = mypk;
        }

        public static TestItem createNewItem() {
            return new TestItem(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
    }
}
