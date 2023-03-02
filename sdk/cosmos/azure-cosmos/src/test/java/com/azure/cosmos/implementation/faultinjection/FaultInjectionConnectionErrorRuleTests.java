// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.faultinjection.FaultInjectionConnectionErrorType;
import com.azure.cosmos.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.faultinjection.FaultInjectionRule;
import com.azure.cosmos.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

public class FaultInjectionConnectionErrorRuleTests extends TestSuiteBase {
    private static final int TIMEOUT = 60000;
    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private DatabaseAccount databaseAccount;

    @DataProvider(name = "connectionErrorTypeProvider")
    public static Object[][] connectionErrorTypeProvider() {
        return new Object[][]{
            { FaultInjectionConnectionErrorType.CONNECTION_CLOSE},
            { FaultInjectionConnectionErrorType.CONNECTION_RESET}
        };
    }

    @BeforeClass(groups = {"simple"}, timeOut = TIMEOUT)
    public void beforeClass() {
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager =
            ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        this.databaseAccount = databaseAccount;
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(client);
    }

    @Test(groups = {"simple"}, dataProvider = "connectionErrorTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionConnectionErrorRuleTests(FaultInjectionConnectionErrorType errorType) throws InterruptedException {
        // using single partition here so that all write operations will be on the same physical partitions
        CosmosAsyncContainer singlePartitionContainer = getSharedSinglePartitionCosmosContainer(client);

        // validate one channel exists
        TestItem createdItem = TestItem.createNewItem();
        singlePartitionContainer.createItem(createdItem).block();

        RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(this.client);
        RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
        Assertions.assertThat(provider.count()).isEqualTo(1);
        provider.list().forEach(rntbdEndpoint -> Assertions.assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(1));

        // now enable the connection error rule which expected to close the connections
        String ruleId = "connectionErrorRule-close-" + UUID.randomUUID();
        FaultInjectionRule connectionErrorRule =
            new FaultInjectionRuleBuilder(ruleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE)
                        .endpoints(
                            new FaultInjectionEndpointBuilder(
                                FeedRange.forLogicalPartition(new PartitionKey(createdItem.getMypk())))
                            .build())
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(errorType)
                        .interval(Duration.ofSeconds(1))
                        .threshold(1.0)
                        .build()
                )
                .duration(Duration.ofSeconds(2))
                .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(singlePartitionContainer, Arrays.asList(connectionErrorRule)).block();
        Thread.sleep(Duration.ofSeconds(2).toMillis());
        // validate the connection is closed
        provider.list().forEach(rntbdEndpoint -> Assertions.assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(0));

        // do another request to open a new connection
        singlePartitionContainer.createItem(TestItem.createNewItem()).block();

        Thread.sleep(Duration.ofSeconds(2).toMillis());
        // the configured connection rule should have disabled after 2s, so the connection will remain open
        provider.list().forEach(rntbdEndpoint -> Assertions.assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(1));

        connectionErrorRule.disable();
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
