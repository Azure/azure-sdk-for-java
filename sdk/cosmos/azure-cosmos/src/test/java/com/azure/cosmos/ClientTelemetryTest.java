// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.clientTelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clientTelemetry.ReportPayload;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTelemetryTest extends TestSuiteBase {
    CosmosClient gatewayClient = null;
    CosmosClient directClient = null;
    CosmosClient telemetryDisabledClient = null;

    @BeforeClass(groups = {"emulator"})
    public void beforeClass() {
        gatewayClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .gatewayMode()
            .clientTelemetryEnabled(true)
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
    }

    @DataProvider(name = "clients")
    public Object[][] clients() {
        return new Object[][]{
            {gatewayClient},
            {directClient}
        };
    }

    @Test(groups = {"emulator"}, dataProvider = "clients", timeOut = TIMEOUT)
    public void operationsList(CosmosClient cosmosClient) throws Exception {
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
        CosmosContainer cosmosContainer =
            cosmosClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());

        ClientTelemetry clientTelemetry = cosmosClient.asyncClient().getContextClient().getClientTelemetry();
        Field backgroundRefreshLocationTimeIntervalInSecField = ClientTelemetry.class.getDeclaredField(
            "clientTelemetrySchedulingSec");
        backgroundRefreshLocationTimeIntervalInSecField.setAccessible(true);
        backgroundRefreshLocationTimeIntervalInSecField.setInt(clientTelemetry, 5);
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

        //Verifying above 5 operation, we should have 10 operation (5 latency, 5 request charge)
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

    //Setting priority = 1 as system properties below interfering in other tests in this file
    @Test(groups = {"unit"}, priority = 1)
    public void clientTelemetryEnabledFlag() {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder();
        assertThat(cosmosClientBuilder.isClientTelemetryEnabled()).isFalse();
        assertThat(Configs.isClientTelemetryEnabled(cosmosClientBuilder.isClientTelemetryEnabled())).isFalse();

        System.setProperty("COSMOS.CLIENT_TELEMETRY_ENABLED", "true");
        assertThat(Configs.isClientTelemetryEnabled(cosmosClientBuilder.isClientTelemetryEnabled())).isTrue();

        System.setProperty("COSMOS.CLIENT_TELEMETRY_ENABLED", "false");// setting it back for other tests
        assertThat(Configs.isClientTelemetryEnabled(cosmosClientBuilder.isClientTelemetryEnabled())).isFalse();
    }

    @Test(groups = {"unit"})
    public void clientTelemetryScheduling() {
        assertThat(Configs.getClientTelemetrySchedulingInSec()).isEqualTo(600);

        System.setProperty("COSMOS.CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS", "10");
        assertThat(Configs.getClientTelemetrySchedulingInSec()).isEqualTo(10);

        System.setProperty("COSMOS.CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS", "600");// setting it back for other tests
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
}
