// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedContextClientImpl;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.PartitionedByIdCollectionRequestOptionsFactory;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class BootstrapperLeaseStoreTest extends TestSuiteBase {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer feedContainer;
    private CosmosAsyncContainer leaseContainer;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public BootstrapperLeaseStoreTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = "query", timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void setUp() {
        client = getClientBuilder().buildAsyncClient();
        database = createTestDatabase(client, "cfp-bootstrap");
        database.createContainer("feed", "/id").block(OPERATION_TIMEOUT);
        CosmosContainerProperties leaseProperties = new CosmosContainerProperties("leases", "/id");
        leaseProperties.setDefaultTimeToLiveInSeconds(-1);
        database.createContainer(leaseProperties).block(OPERATION_TIMEOUT);
        feedContainer = database.getContainer("feed");
        leaseContainer = database.getContainer("leases");
    }

    @AfterClass(groups = "query", timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void tearDown() {
        try {
            if (database != null) {
                safeDeleteDatabase(database);
            }
        } finally {
            safeClose(client);
        }
    }

    @DataProvider
    public Object[][] changeFeedModes() {
        return new Object[][] {{ChangeFeedMode.INCREMENTAL}, {ChangeFeedMode.FULL_FIDELITY}};
    }

    @Test(groups = "query", dataProvider = "changeFeedModes", timeOut = 2 * TIMEOUT)
    public void failedLeaseWriteLeavesStoreUninitializedAndRetryPersistsAllLeases(ChangeFeedMode mode) {
        IllegalStateException failure = new IllegalStateException("Injected lease write failure");
        AtomicBoolean failNextLeaseWrite = new AtomicBoolean(true);
        ChangeFeedContextClientImpl leaseClient = new ChangeFeedContextClientImpl(leaseContainer) {
            @Override
            public <T> Mono<CosmosItemResponse<T>> createItem(
                CosmosAsyncContainer container, T document, CosmosItemRequestOptions options,
                boolean disableAutomaticIdGeneration) {

                if (document instanceof ServiceItemLeaseV1 && failNextLeaseWrite.compareAndSet(true, false)) {
                    return Mono.error(failure);
                }
                return super.createItem(container, document, options, disableAutomaticIdGeneration);
            }
        };

        PartitionedByIdCollectionRequestOptionsFactory requestOptionsFactory =
            new PartitionedByIdCollectionRequestOptionsFactory();
        LeaseStoreManager epkStore = LeaseStoreManagerImpl.builder()
            .leasePrefix("epk-" + mode)
            .leaseCollectionLink(leaseContainer)
            .leaseContextClient(leaseClient)
            .requestOptionsFactory(requestOptionsFactory)
            .hostName("bootstrap-test")
            .build();
        LeaseStoreManager pkStore = com.azure.cosmos.implementation.changefeed.pkversion.LeaseStoreManagerImpl.builder()
            .leasePrefix("pk-" + mode)
            .leaseCollectionLink(leaseContainer)
            .leaseContextClient(leaseClient)
            .requestOptionsFactory(requestOptionsFactory)
            .hostName("bootstrap-test")
            .build();

        ChangeFeedContextClientImpl feedClient = new ChangeFeedContextClientImpl(feedContainer);
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
        PartitionSynchronizer synchronizer = new PartitionSynchronizerImpl(
            feedClient, BridgeInternal.extractContainerSelfLink(feedContainer), epkStore, epkStore, 1, 1, options, mode);
        Duration lockTime = Duration.ofMinutes(2);
        Duration sleepTime = Duration.ofMillis(100);
        Bootstrapper bootstrapper = mode == ChangeFeedMode.INCREMENTAL
            ? new PkRangeIdVersionLeaseStoreBootstrapperImpl(
                synchronizer, epkStore, lockTime, sleepTime, pkStore, epkStore, options, mode)
            : new BootstrapperImpl(synchronizer, epkStore, lockTime, sleepTime, epkStore, pkStore, options, mode);

        StepVerifier.create(bootstrapper.initialize())
            .expectErrorMatches(error -> error == failure)
            .verify(OPERATION_TIMEOUT);
        assertThat(epkStore.isInitialized().block(OPERATION_TIMEOUT)).isFalse();
        assertThat(pkStore.isInitialized().block(OPERATION_TIMEOUT)).isFalse();

        // Retry before either lock could expire. A successful retry also verifies that cleanup released the locks.
        StepVerifier.create(bootstrapper.initialize()).expectComplete().verify(OPERATION_TIMEOUT);
        assertThat(epkStore.isInitialized().block(OPERATION_TIMEOUT)).isTrue();
        if (mode == ChangeFeedMode.INCREMENTAL) {
            assertThat(pkStore.isInitialized().block(OPERATION_TIMEOUT)).isTrue();
        }

        List<FeedRangeEpkImpl> expectedRanges = feedClient
            .getOverlappingRanges(PartitionKeyInternalHelper.FullRange, true)
            .flatMapIterable(ranges -> ranges)
            .map(range -> new FeedRangeEpkImpl(range.toRange()))
            .collectList()
            .block(OPERATION_TIMEOUT);
        assertThat(expectedRanges).isNotEmpty();
        assertThat(epkStore.getAllLeases().map(lease -> (FeedRangeEpkImpl) lease.getFeedRange())
            .collectList().block(OPERATION_TIMEOUT)).containsExactlyInAnyOrderElementsOf(expectedRanges);
    }
}
