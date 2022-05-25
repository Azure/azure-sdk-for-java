// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.ReportPayload;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.rx.proxy.HttpProxyServer;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTelemetryTest extends TestSuiteBase {
    private CosmosClient gatewayClient = null;
    private CosmosClient directClient = null;
    private CosmosClient telemetryDisabledClient = null;
    private HttpProxyServer httpProxyServer;

    @BeforeClass(groups = {"emulator"})
    public void beforeClass() {
        List<String> preferredRegion = new ArrayList<>();
        preferredRegion.add("East US 2");
        preferredRegion.add("West US");
        gatewayClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .gatewayMode()
            .clientTelemetryEnabled(true)
            .preferredRegions(preferredRegion)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildClient();

        directClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .clientTelemetryEnabled(true)
            .buildClient();

        telemetryDisabledClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildClient();

        httpProxyServer = new HttpProxyServer();
        httpProxyServer.start();
    }

    @AfterClass(groups = {"emulator"})
    public void afterClass() {
        if (this.gatewayClient != null) {
            this.gatewayClient.close();
        }

        if (this.directClient != null) {
            this.directClient.close();
        }

        if (this.telemetryDisabledClient != null) {
            this.telemetryDisabledClient.close();
        }

        if (this.httpProxyServer != null) {
            this.httpProxyServer.shutDown();
        }
    }

    @DataProvider(name = "clients")
    public Object[][] clients() {
        return new Object[][]{
            {gatewayClient},
            {directClient}
        };
    }


    @DataProvider(name = "useProxy", parallel = false)
    public static Object[][] useProxy() {
        return new Object[][]{
                { true },
                { false }
        };
    }

    @Test(groups = {"emulator"}, dataProvider = "clients", timeOut = TIMEOUT)
    public void operationsList(CosmosClient cosmosClient) throws Exception {
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
        CosmosContainer cosmosContainer =
            cosmosClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());

        ClientTelemetry clientTelemetry = cosmosClient.asyncClient().getContextClient().getClientTelemetry();
        setClientTelemetrySchedulingInSec(clientTelemetry, 5);
        clientTelemetry.init();

        InternalObjectNode internalObjectNode = getInternalObjectNode();
        cosmosContainer.createItem(internalObjectNode); //create operation
        try {
            cosmosContainer.readItem("wrong", PartitionKey.NONE, InternalObjectNode.class); //read operation with 404
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
        }

        for (int i = 0; i < 10; i++) {
            cosmosContainer.readItem(internalObjectNode.getId(),
                new PartitionKey(internalObjectNode.getId()),
                InternalObjectNode.class);
        }//read operation 10 times will have one entry in telemetry

        cosmosContainer.replaceItem(internalObjectNode, internalObjectNode.getId(),
            new PartitionKey(internalObjectNode.getId()), new CosmosItemRequestOptions()); // replace operation
        cosmosContainer.deleteItem(internalObjectNode.getId(), new PartitionKey(internalObjectNode.getId()),
            new CosmosItemRequestOptions()); // delete operation

        readClientTelemetry(clientTelemetry);
        //Verifying above 5 operation, we should have 10 operation (5 latency, 5 request charge)
        String json = Utils.getSimpleObjectMapper().writeValueAsString(clientTelemetry);
        if(cosmosClient.asyncClient().getConnectionPolicy().getConnectionMode().equals(ConnectionMode.GATEWAY)) {
           validateCTJsonFields(json, true, true);
        } else {
            validateCTJsonFields(json, false, false);
            assertThat(clientTelemetry.getClientTelemetryInfo().getSystemInfoMap().size()).isEqualTo(3);
            for(ReportPayload reportPayload : clientTelemetry.getClientTelemetryInfo().getSystemInfoMap().keySet()) {
                if(reportPayload.getMetricInfo().getMetricsName().equals(ClientTelemetry.TCP_NEW_CHANNEL_LATENCY_NAME)) {
                    //Validate that we have open at least 1 channel
                    assertThat(reportPayload.getMetricInfo().getCount()).isGreaterThanOrEqualTo(1);
                }
            }
        }

        assertThat(clientTelemetry.getClientTelemetryInfo().getOperationInfoMap().size()).isEqualTo(10);
        assertThat(clientTelemetry.getClientTelemetryInfo().getOperationInfoMap().size()).isEqualTo(10);

        Thread.sleep(5000); // Making sure we clear previous operations info from client telemetry
        Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer.queryItems("select * from c",
            new CosmosQueryRequestOptions(), InternalObjectNode.class).iterableByPage().iterator();
        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> response = iterator.next();
        }

        //Verifying above query operation, we should have 2 operation (1 latency, 1 request charge)
        assertThat(clientTelemetry.getClientTelemetryInfo().getOperationInfoMap().size()).isEqualTo(2);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void operationsListWithNoTelemetry() throws Exception {
        CosmosAsyncContainer asyncContainer =
            getSharedMultiPartitionCosmosContainer(telemetryDisabledClient.asyncClient());
        CosmosContainer cosmosContainer =
            telemetryDisabledClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());

        ClientTelemetry clientTelemetry = telemetryDisabledClient.asyncClient().getContextClient().getClientTelemetry();
        Field backgroundRefreshLocationTimeIntervalInMSField = ClientTelemetry.class.getDeclaredField(
            "clientTelemetrySchedulingSec");
        backgroundRefreshLocationTimeIntervalInMSField.setAccessible(true);
        backgroundRefreshLocationTimeIntervalInMSField.setInt(clientTelemetry, 5);
        clientTelemetry.init();

        InternalObjectNode internalObjectNode = getInternalObjectNode();
        cosmosContainer.createItem(internalObjectNode); // create operation
        try {
            cosmosContainer.readItem("wrong", PartitionKey.NONE, InternalObjectNode.class); // read operation with 404
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
        }

        cosmosContainer.readItem(internalObjectNode.getId(),
            new PartitionKey(internalObjectNode.getId()),
            InternalObjectNode.class); // read operation

        cosmosContainer.replaceItem(internalObjectNode, internalObjectNode.getId(),
            new PartitionKey(internalObjectNode.getId()), new CosmosItemRequestOptions()); // replace operation
        cosmosContainer.deleteItem(internalObjectNode.getId(), new PartitionKey(internalObjectNode.getId()),
            new CosmosItemRequestOptions()); // delete operation

        Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer.queryItems("select * from c",
            new CosmosQueryRequestOptions(), InternalObjectNode.class).iterableByPage().iterator();
        while (iterator.hasNext()) {
            FeedResponse<InternalObjectNode> response = iterator.next(); // query operation
        }

        //Verifying we should not capture telemetry for any above operations
        assertThat(clientTelemetry.getClientTelemetryInfo().getOperationInfoMap().size()).isEqualTo(0);
    }

    @Test(groups = {"emulator"}, dataProvider = "clients", timeOut = TIMEOUT)
    public void systemInfo(CosmosClient cosmosClient) throws Exception {
        ClientTelemetry clientTelemetry = cosmosClient.asyncClient().getContextClient().getClientTelemetry();
        readClientTelemetry(clientTelemetry);
        assertThat(clientTelemetry.getClientTelemetryInfo().getSystemInfoMap().size()).isEqualTo(2);
        for (ReportPayload reportPayload : clientTelemetry.getClientTelemetryInfo().getSystemInfoMap().keySet()) {
            if (reportPayload.getMetricInfo().getMetricsName().equals("CPU")) {
                assertThat(reportPayload.getMetricInfo().getUnitName()).isEqualTo("Percentage");
            } else {
                assertThat(reportPayload.getMetricInfo().getMetricsName()).isEqualTo("MemoryRemaining");
                assertThat(reportPayload.getMetricInfo().getUnitName()).isEqualTo("MB");
            }
        }
    }

    @Test(groups = {"unit"})
    public void clientTelemetryScheduling() {
        assertThat(Configs.getClientTelemetrySchedulingInSec()).isEqualTo(600);

        System.setProperty("COSMOS.CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS", "10");
        assertThat(Configs.getClientTelemetrySchedulingInSec()).isEqualTo(10);

        System.setProperty("COSMOS.CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS", "600");// setting it back for other tests
    }

    @SuppressWarnings("unchecked")
    @Test(groups = {"non-emulator"}, dataProvider = "useProxy", timeOut = TIMEOUT)
    public void clientTelemetryWithStageJunoEndpoint(boolean useProxy) throws InterruptedException, NoSuchFieldException,
        IllegalAccessException {
        CosmosClient cosmosClient = null;
        String databaseId = UUID.randomUUID().toString();
        try {
            String whiteListedAccountForTelemetry = System.getProperty("COSMOS.CLIENT_TELEMETRY_COSMOS_ACCOUNT");
            assertThat(whiteListedAccountForTelemetry).isNotNull();
            String[] credentialList = whiteListedAccountForTelemetry.split(";");
            String host = credentialList[0].substring("AccountEndpoint=".length());
            String key = credentialList[1].substring("AccountKey=".length());

            if (useProxy) {
                System.setProperty(
                        "COSMOS.CLIENT_TELEMETRY_PROXY_OPTIONS_CONFIG",
                        String.format("{\"type\":\"HTTP\", \"host\": \"%s\", \"port\": %d}", this.httpProxyServer.getHost(), this.httpProxyServer.getPort()));
            } else {
                System.clearProperty("COSMOS.CLIENT_TELEMETRY_PROXY_OPTIONS_CONFIG");
            }

            CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
                .endpoint(host)
                .key(key)
                .clientTelemetryEnabled(true);

            cosmosClient = clientBuilder.buildClient();

            String containerId = UUID.randomUUID().toString();
            cosmosClient.createDatabase(databaseId);

            CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerId, "/id");
            cosmosClient.getDatabase(databaseId).createContainer(containerProperties);
            CosmosContainer cosmosContainer =
                cosmosClient.getDatabase(databaseId).getContainer(containerId);
            ClientTelemetry clientTelemetry = cosmosClient.asyncClient().getContextClient().getClientTelemetry();
            setClientTelemetrySchedulingInSec(clientTelemetry, 5);
            clientTelemetry.init();

            // If this test need to run on local machine please add below env property,
            // in test env we add the env property with cosmos-client-telemetry-endpoint variable in tests.yml,
            // which gets its value from key vault TestSecrets-Cosmos

            logger.info(
                    "clientTelemetryWithStageJunoEndpoint client telemetry endpoint {}",
                    System.getProperty("COSMOS.CLIENT_TELEMETRY_ENDPOINT").split("/")[2]);
            logger.info(
                    "clientTelemetryWithStageJunoEndpoint cosmos account name  {}",
                    ReflectionUtils.getGlobalEndpointManager(
                            (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient)).getLatestDatabaseAccount().getResourceId());


            HttpClient httpClient = ReflectionUtils.getHttpClient(clientTelemetry);
            HttpClient spyHttpClient = Mockito.spy(httpClient);
            ReflectionUtils.setHttpClient(clientTelemetry, spyHttpClient);

            AtomicReference<Mono<HttpResponse>> httpResponseMono = new AtomicReference<>();
            Mockito.doAnswer(invocationOnMock -> {
                httpResponseMono.set((Mono<HttpResponse>) invocationOnMock.callRealMethod());
                return httpResponseMono.get();
            }).when(spyHttpClient).send(ArgumentMatchers.any(),
                ArgumentMatchers.any());
            InternalObjectNode internalObjectNode = getInternalObjectNode();
            cosmosContainer.createItem(internalObjectNode);
            cosmosContainer.readItem(internalObjectNode.getId(),
                new PartitionKey(internalObjectNode.getId()),
                InternalObjectNode.class);
            Thread.sleep(5000);
            StepVerifier.create(httpResponseMono.get()).expectNextMatches(httpResponse -> {
                logger.info("clientTelemetryWithStageJunoEndpoint statusCode from juno {}",  httpResponse.statusCode());
                return httpResponse.statusCode() == HttpConstants.StatusCodes.OK;
            }).verifyComplete();
        } finally {
            if (cosmosClient != null) {
                cosmosClient.getDatabase(databaseId).delete();
            }
            safeCloseSyncClient(cosmosClient);

            // clear system property
            System.clearProperty("COSMOS.CLIENT_TELEMETRY_PROXY_OPTIONS_CONFIG");
        }
    }

    private InternalObjectNode getInternalObjectNode() {
        InternalObjectNode internalObjectNode = new InternalObjectNode();
        String uuid = UUID.randomUUID().toString();
        internalObjectNode.setId(uuid);
        BridgeInternal.setProperty(internalObjectNode, "mypk", uuid);
        return internalObjectNode;
    }

    private void readClientTelemetry(ClientTelemetry telemetry) throws Exception {
        Method readHistogram = ClientTelemetry.class.getDeclaredMethod("readHistogram");
        readHistogram.setAccessible(true);
        readHistogram.invoke(telemetry);
    }

    private void setClientTelemetrySchedulingInSec(ClientTelemetry clientTelemetry, int backgroundScheduling) throws IllegalAccessException, NoSuchFieldException {
        Field backgroundRefreshLocationTimeIntervalInSecField = ClientTelemetry.class.getDeclaredField(
            "clientTelemetrySchedulingSec");
        backgroundRefreshLocationTimeIntervalInSecField.setAccessible(true);
        backgroundRefreshLocationTimeIntervalInSecField.setInt(clientTelemetry, backgroundScheduling);
    }

    private void validateCTJsonFields(String ctJson, boolean shouldContainsSession,
                                      boolean shouldContainsPreferredRegion) {
        if (shouldContainsSession) {
            assertThat(ctJson).contains("\"consistency\":\"Session\"");
        } else {
            assertThat(ctJson).doesNotContain("consistency");
        }

        if (shouldContainsPreferredRegion) {
            assertThat(ctJson).contains("\"preferredRegions\":[\"East US 2\",\"West US\"]");
        } else {
            assertThat(ctJson).doesNotContain("preferredRegions");
        }

        assertThat(ctJson).contains("\"aggregationIntervalInSec\":600");
    }
}
