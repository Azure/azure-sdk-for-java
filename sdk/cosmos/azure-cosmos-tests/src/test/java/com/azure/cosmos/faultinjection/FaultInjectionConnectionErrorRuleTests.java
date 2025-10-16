// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainerProactiveInitConfigBuilder;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionErrorType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class FaultInjectionConnectionErrorRuleTests extends FaultInjectionTestBase {
    private static final int TIMEOUT = 60000;
    private CosmosAsyncClient client;
    private DatabaseAccount databaseAccount;
    private Map<String, String> writeRegionMap;

    @DataProvider(name = "connectionErrorTypeProvider")
    public static Object[][] connectionErrorTypeProvider() {
        return new Object[][]{
                { FaultInjectionConnectionErrorType.CONNECTION_CLOSE},
                {FaultInjectionConnectionErrorType.CONNECTION_RESET}
        };
    }

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public FaultInjectionConnectionErrorRuleTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region", "long"}, timeOut = TIMEOUT)
    public void beforeClass() {
        try {
            client = getClientBuilder().buildAsyncClient();
            AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(client);
            GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

            this.databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
            this.writeRegionMap = getRegionMap(this.databaseAccount, true);

            // This test runs against a real account
            // Creating collections can take some time in the remote region
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"long"}, dataProvider = "connectionErrorTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionConnectionErrorRuleTestWithNoConnectionWarmup(FaultInjectionConnectionErrorType errorType) {

        client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildAsyncClient();

        try {
            // using single partition here so that all write operations will be on the same physical partitions
            CosmosAsyncContainer singlePartitionContainer = getSharedSinglePartitionCosmosContainer(client);

            // validate one channel exists
            TestObject createdItem = TestObject.create();
            singlePartitionContainer.createItem(createdItem).block();

            RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(this.client);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
            assertThat(provider.count()).isEqualTo(1);
            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(1));

            // now enable the connection error rule which expected to close the connections
            String ruleId = "connectionErrorRule-close-" + UUID.randomUUID();
            FaultInjectionRule connectionErrorRule =
                    new FaultInjectionRuleBuilder(ruleId)
                            .condition(
                                    new FaultInjectionConditionBuilder()
                                            .operationType(FaultInjectionOperationType.CREATE_ITEM)
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
            // validate that a connection is closed by fault injection
            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.durableEndpointMetrics().totalChannelsClosedMetric()).isEqualTo(1));
            // validate that proactive connection management does not reopen connections
            // this is because the openConnectionsAndInitCaches flow was not invoked for the client
            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.durableEndpointMetrics().getEndpoint().channelsMetrics()).isEqualTo(0));
            long ruleHitCount = connectionErrorRule.getHitCount();
            assertThat(ruleHitCount).isGreaterThanOrEqualTo(1);
            assertThat(connectionErrorRule.getHitCountDetails()).isNull();

            // do another request to open a new connection
            singlePartitionContainer.createItem(TestObject.create()).block();

            Thread.sleep(Duration.ofSeconds(2).toMillis());
            // the configured connection rule should have disabled after 2s, so the connection will remain open
            // Due to the open connection flow,eventually we might get 1 or 2 channels.
            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.channelsMetrics()).isLessThanOrEqualTo(2));
            assertThat(ruleHitCount).isEqualTo(ruleHitCount);

            connectionErrorRule.disable();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "connectionErrorTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionConnectionErrorRuleTestWithConnectionWarmup(FaultInjectionConnectionErrorType errorType) {

        client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .buildAsyncClient();
        CosmosAsyncClient connectionWarmupClient = null;

        try {
            // using single partition here so that all write operations will be on the same physical partitions
            CosmosAsyncContainer singlePartitionContainer = getSharedSinglePartitionCosmosContainer(client);

            String databaseId = singlePartitionContainer.getDatabase().getId();
            String containerId = singlePartitionContainer.getId();

            List<String> preferredRegions = this.writeRegionMap.keySet().stream().collect(Collectors.toList());
            List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();

            cosmosContainerIdentities.add(new CosmosContainerIdentity(databaseId, containerId));

            connectionWarmupClient = new CosmosClientBuilder()
                    .endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .preferredRegions(preferredRegions)
                    .directMode()
                    .openConnectionsAndInitCaches(new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
                            .setProactiveConnectionRegionsCount(1)
                            .build()
                    )
                    .endpointDiscoveryEnabled(true)
                    .buildAsyncClient();

            singlePartitionContainer = connectionWarmupClient.getDatabase(databaseId).getContainer(containerId);

            // validate one channel exists
            TestObject createdItem = TestObject.create();
            singlePartitionContainer.createItem(createdItem).block();

            RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(connectionWarmupClient);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);

            // provider has 4 endpoints because the connection warm up flow
            // opens a connection to each endpoint in the container
            // which also happens to be a single partition container
            // each time a connection is opened on an address, the endpoint
            // associated with the address is instantiated and added to the provider
            assertThat(provider.count()).isEqualTo(4);

            // track the primary endpoint / address since this is a create item request
            // write / create requests always go to the primary address
            Optional<RntbdEndpoint> primaryEndpointOptional = provider
                    .list()
                    .filter(rntbdEndpoint -> rntbdEndpoint.getAddressUri().isPrimary())
                    .findFirst();

            RntbdEndpoint primaryEndpoint = null;

            if (primaryEndpointOptional.isPresent()) {
                primaryEndpoint = primaryEndpointOptional.get();
            } else {
                fail("primary address could not be obtained for a write operation");
            }

            // primary endpoint has a connection opened on it
            // through the connection warm up flow
            // create item request will reuse the connection created
            assertThat(primaryEndpoint).isNotNull();
            assertThat(primaryEndpoint.channelsMetrics()).isEqualTo(1);

            // now enable the connection error rule which expected to close the connections
            String ruleId = "connectionErrorRule-close-" + UUID.randomUUID();
            FaultInjectionRule connectionErrorRule =
                    new FaultInjectionRuleBuilder(ruleId)
                            .condition(
                                    new FaultInjectionConditionBuilder()
                                            .operationType(FaultInjectionOperationType.CREATE_ITEM)
                                            .endpoints(
                                                    new FaultInjectionEndpointBuilder(
                                                            FeedRange.forFullRange())
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
            // validate that a connection is closed by fault injection
            assertThat(primaryEndpoint.durableEndpointMetrics().totalChannelsClosedMetric()).isEqualTo(1);
            // validate that proactive connection management reopens the connection
            assertThat(primaryEndpoint.durableEndpointMetrics().channelsAvailableMetric()).isEqualTo(1);
            long ruleHitCount = connectionErrorRule.getHitCount();
            assertThat(ruleHitCount).isGreaterThanOrEqualTo(1);
            assertThat(connectionErrorRule.getHitCountDetails()).isNull();

            // do another request to open a new connection
            singlePartitionContainer.createItem(TestObject.create()).block();

            Thread.sleep(Duration.ofSeconds(2).toMillis());
            // the configured connection rule should have disabled after 2s, so the connection will remain open
            // Due to the open connection flow,eventually we might get 1 or 2 channels.
            assertThat(primaryEndpoint.channelsMetrics()).isLessThanOrEqualTo(2);

            assertThat(ruleHitCount).isEqualTo(ruleHitCount);

            connectionErrorRule.disable();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            safeClose(client);
            safeClose(connectionWarmupClient);
        }
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void connectionCloseError_NoEndpoint_NoWarmup() {
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

        try {
            // using single partition here so that all write operations will be on the same physical partitions
            CosmosAsyncContainer singlePartitionContainer = getSharedSinglePartitionCosmosContainer(client);

            // validate one channel exists
            TestObject createdItem = TestObject.create();
            singlePartitionContainer.createItem(createdItem).block();

            RntbdTransportClient rntbdTransportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(this.client);
            RntbdEndpoint.Provider provider = ReflectionUtils.getRntbdEndpointProvider(rntbdTransportClient);
            assertThat(provider.count()).isEqualTo(1);
            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.channelsMetrics()).isEqualTo(1));

            // now enable the connection error rule which expected to close the connections
            String ruleId = "connectionErrorRule-close-" + UUID.randomUUID();
            FaultInjectionRule connectionErrorRule =
                new FaultInjectionRuleBuilder(ruleId)
                    .condition(
                        new FaultInjectionConditionBuilder()
                            .operationType(FaultInjectionOperationType.CREATE_ITEM)
                            .build()
                    )
                    .result(
                        FaultInjectionResultBuilders
                            .getResultBuilder(FaultInjectionConnectionErrorType.CONNECTION_CLOSE)
                            .interval(Duration.ofSeconds(1))
                            .threshold(1.0)
                            .build()
                    )
                    .duration(Duration.ofSeconds(2))
                    .build();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(singlePartitionContainer, Arrays.asList(connectionErrorRule)).block();
            Thread.sleep(Duration.ofSeconds(2).toMillis());
            // validate that a connection is closed by fault injection
            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.durableEndpointMetrics().totalChannelsClosedMetric()).isEqualTo(1));
            // validate that proactive connection management does not reopen connections
            // this is because the openConnectionsAndInitCaches flow was not invoked for the client
            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.durableEndpointMetrics().getEndpoint().channelsMetrics()).isEqualTo(0));
            long ruleHitCount = connectionErrorRule.getHitCount();
            assertThat(ruleHitCount).isGreaterThanOrEqualTo(1);
            assertThat(connectionErrorRule.getHitCountDetails()).isNull();

            // do another request to open a new connection
            singlePartitionContainer.createItem(TestObject.create()).block();

            Thread.sleep(Duration.ofSeconds(2).toMillis());
            // the configured connection rule should have disabled after 2s, so the connection will remain open
            // Due to the open connection flow,eventually we might get 1 or 2 channels.
            provider.list().forEach(rntbdEndpoint -> assertThat(rntbdEndpoint.channelsMetrics()).isLessThanOrEqualTo(2));
            assertThat(ruleHitCount).isEqualTo(ruleHitCount);

            connectionErrorRule.disable();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            safeClose(client);
        }
    }

    private Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
                writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }
}
