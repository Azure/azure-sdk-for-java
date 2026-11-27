// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedQueryFilter;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.times;

public class BootstrapperImplTests {

    @Test(groups = "unit")
    public void auxiliaryEpkLeasesCreatedBeforeLockGrantAreRevalidatedBeforeLegacyWrites() {
        String queryA = "SELECT * FROM c WHERE c.a = 1";
        String queryB = "SELECT * FROM c WHERE c.a = 2";
        for (String[] change : new String[][] { { null, queryB }, { queryA, null }, { queryA, queryB } }) {
            java.util.concurrent.atomic.AtomicReference<ServiceItemLeaseV1> persisted =
                new java.util.concurrent.atomic.AtomicReference<>();
            ServiceItemLeaseV1 materialized = new ServiceItemLeaseV1().withId("prefix..0").withLeaseToken("0")
                .withVersion(LeaseVersion.EPK_RANGE_BASED_LEASE)
                .withFeedRange(com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl.forFullRange())
                .withProperties(ChangeFeedQueryFilter.withFingerprint(null, ChangeFeedQueryFilter.fingerprint(change[0])));
            com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient client =
                Mockito.mock(com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient.class);
            Mockito.when(client.queryItems(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.eq(com.azure.cosmos.implementation.InternalObjectNode.class))).thenReturn(Flux.defer(() -> {
                    @SuppressWarnings("unchecked")
                    com.azure.cosmos.models.FeedResponse<com.azure.cosmos.implementation.InternalObjectNode> page =
                        Mockito.mock(com.azure.cosmos.models.FeedResponse.class);
                    java.util.List<com.azure.cosmos.implementation.InternalObjectNode> items = new java.util.ArrayList<>();
                    if (persisted.get() != null) {
                        com.fasterxml.jackson.databind.node.ObjectNode serialized =
                            com.azure.cosmos.implementation.Utils.getSimpleObjectMapper().valueToTree(persisted.get());
                        items.add(new com.azure.cosmos.implementation.InternalObjectNode(serialized));
                    }
                    Mockito.when(page.getResults()).thenReturn(items);
                    return Flux.just(page);
                }));
            LeaseStoreManager auxiliary = Mockito.spy(
                com.azure.cosmos.implementation.changefeed.epkversion.LeaseStoreManagerImpl.builder()
                    .leaseContextClient(client).leasePrefix("prefix").hostName("host").query(change[1])
                    .leaseCollectionLink(Mockito.mock(com.azure.cosmos.CosmosAsyncContainer.class))
                    .requestOptionsFactory(Mockito.mock(com.azure.cosmos.implementation.changefeed.RequestOptionsFactory.class))
                    .build());
            Mockito.doReturn(Mono.just(false)).when(auxiliary).isInitialized();
            Mockito.doReturn(Mono.defer(() -> {
                persisted.set(materialized);
                return Mono.just(true);
            })).when(auxiliary).acquireInitializationLock(Mockito.any());
            Mockito.doReturn(Mono.just(true)).when(auxiliary).releaseInitializationLock();
            LeaseStore store = Mockito.mock(LeaseStore.class);
            Mockito.when(store.isInitialized()).thenReturn(Mono.just(false));
            Mockito.when(store.acquireInitializationLock(Mockito.any())).thenReturn(Mono.just(true));
            Mockito.when(store.releaseInitializationLock()).thenReturn(Mono.just(true));
            PartitionSynchronizerImpl synchronizer = Mockito.spy(new PartitionSynchronizerImpl(client,
                Mockito.mock(com.azure.cosmos.CosmosAsyncContainer.class), auxiliary, auxiliary, 1, 1, "rid"));
            Bootstrapper bootstrapper = new BootstrapperImpl(synchronizer, store, auxiliary,
                new ChangeFeedProcessorOptions().setQuery(change[1]), Duration.ofSeconds(5), Duration.ofMillis(1));
            StepVerifier.create(bootstrapper.initialize())
                .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify(Duration.ofSeconds(5));
            Mockito.verify(auxiliary, times(2)).getAllLeases();
            Mockito.verify(auxiliary).releaseInitializationLock();
            Mockito.verify(store).releaseInitializationLock();
            Mockito.verify(store, Mockito.never()).markInitialized();
            Mockito.verify(synchronizer, Mockito.never()).createMissingLeases();
        }
    }

    @Test(groups = "unit")
    public void queryMismatchTerminatesInitializationAndReleasesHeldLock() {
        PartitionSynchronizer synchronizer = Mockito.mock(PartitionSynchronizer.class);
        LeaseStore store = Mockito.mock(LeaseStore.class);
        LeaseStoreManager auxiliary = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(store.isInitialized()).thenReturn(Mono.just(false));
        Mockito.when(auxiliary.isInitialized()).thenReturn(Mono.just(false));
        Mockito.when(auxiliary.getAllLeases()).thenReturn(Flux.empty());
        Mockito.when(auxiliary.acquireInitializationLock(Mockito.any())).thenReturn(Mono.just(true));
        Mockito.when(auxiliary.releaseInitializationLock()).thenReturn(Mono.just(true));
        Mockito.when(store.acquireInitializationLock(Mockito.any())).thenReturn(Mono.just(true));
        Mockito.when(store.releaseInitializationLock()).thenReturn(Mono.empty());
        Mockito.when(store.markInitialized()).thenReturn(Mono.error(new AssertionError("Marker must not be written")));
        Mockito.when(synchronizer.createMissingLeases())
            .thenReturn(Mono.error(new ChangeFeedQueryFilter.QueryFilterMismatchException()));

        Bootstrapper bootstrapper = new BootstrapperImpl(synchronizer, store, auxiliary,
            new ChangeFeedProcessorOptions(), Duration.ofSeconds(5), Duration.ofMillis(1));
        StepVerifier.create(bootstrapper.initialize())
            .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify(Duration.ofSeconds(5));
        Mockito.verify(store).releaseInitializationLock();
        Mockito.verify(synchronizer).createMissingLeases();

        Mockito.when(auxiliary.isInitialized())
            .thenReturn(Mono.error(new ChangeFeedQueryFilter.QueryFilterMismatchException()));
        Mockito.clearInvocations(store, synchronizer);
        StepVerifier.create(bootstrapper.initialize())
            .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify(Duration.ofSeconds(5));
        Mockito.verify(store, Mockito.never()).acquireInitializationLock(Mockito.any());
        Mockito.verify(synchronizer, Mockito.never()).createMissingLeases();
    }

    private static final String BASE_CONTINUATION_STRING_FOR_EPK_FULL_RANGE = "{\"V\":1," +
        "\"Rid\":\"%s\"," +
        "\"Continuation\":[" +
        "{\"token\":\"%s\",\"range\":{\"min\":\"\",\"max\":\"FF\"}}" +
        "]," +
        "\"PKRangeId\":\"%s\"}";

    @DataProvider(name = "leaseProvider")
    public Object[][] leaseProvider() {
        return new Object[][] {
            {
                createEpkRangeBasedLeaseWithContinuation(
                    true,
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                true
            },
            {
                createEpkRangeBasedLeaseWithContinuation(
                    false,
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                true
            },
            {
                createEpkRangeBasedLeaseWithContinuation(
                    true,
                    ChangeFeedMode.INCREMENTAL,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                true
            },
            {
                createEpkRangeBasedLeaseWithContinuation(
                    false,
                    ChangeFeedMode.INCREMENTAL,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                true,
            },
            {
                createPkRangeBasedLeaseWithContinuation(false, "XyJKUI7=","NO67Hq=", "0", "0"),
                false
            },
            {
                createPkRangeBasedLeaseWithContinuation(true, "XyJKUI7=","NO67Hq=", "0", "0"),
                false
            },
            {
                null,
                false
            }
        };
    }

    @Test(groups = {"unit"}, dataProvider = "leaseProvider")
    public void tryInitializeStoreFromPkVersionLeaseStoreWithExistingLeases(Lease lease, boolean expectIllegalStateException) {
        Duration lockTime = Duration.ofSeconds(5);
        Duration sleepTime = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases()).thenReturn(Mono.empty());

        ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.empty());

        LeaseStoreManager epkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.isInitialized()).thenReturn(Mono.just(false));
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.getAllLeases()).thenReturn(Flux.empty());
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.acquireInitializationLock(Mockito.any())).thenReturn(Mono.just(true));
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.releaseInitializationLock()).thenReturn(Mono.just(true));

        if (lease != null) {
            Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.just(lease));
        } else {
            Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.empty());
        }

        Bootstrapper bootstrapper = new BootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            epkRangeVersionLeaseStoreManagerMock,
            changeFeedProcessorOptions,
            lockTime,
            sleepTime);

        if (expectIllegalStateException) {
            Assert.assertThrows(IllegalStateException.class, () -> bootstrapper.initialize().block());
        } else {
            bootstrapper.initialize().block();
        }

        Mockito.verify(epkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases();
        Mockito.verify(leaseStoreMock, times(3)).isInitialized();
    }

    @Test(groups = {"unit"}, dataProvider = "leaseProvider")
    public void tryInitializeStoreFromPkVersionLeaseStoreWithExistingButMissingLeases(Lease lease, boolean expectIllegalStateException) {
        Duration lockTime = Duration.ofSeconds(5);
        Duration sleepTime = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases()).thenReturn(Mono.empty());

        ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions()
            .setLeaseVerificationEnabledOnRestart(true);

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.empty());

        LeaseStoreManager epkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.isInitialized()).thenReturn(Mono.just(false));
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.getAllLeases()).thenReturn(Flux.empty());
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.acquireInitializationLock(Mockito.any())).thenReturn(Mono.just(true));
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.releaseInitializationLock()).thenReturn(Mono.just(true));

        // even return empty Flux when lease != null to simulate the missing leases
        if (lease != null) {
            Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1)))
                   .thenReturn(
                        // even return empty Flux when PK-range based lease != null to simulate the missing leases
                        lease.getVersion() == LeaseVersion.EPK_RANGE_BASED_LEASE ? Flux.just(lease) : Flux.empty()
                   );
        } else {
            Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.empty());
        }

        Bootstrapper bootstrapper = new BootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            epkRangeVersionLeaseStoreManagerMock,
            changeFeedProcessorOptions,
            lockTime,
            sleepTime);

        if (expectIllegalStateException) {
            Assert.assertThrows(IllegalStateException.class, () -> bootstrapper.initialize().block());
        } else {
            bootstrapper.initialize().block();
        }

        Mockito.verify(epkRangeVersionLeaseStoreManagerMock, times(expectIllegalStateException ? 1 : 2))
            .getTopLeases(Mockito.eq(1));
        if (lease != null && lease.getVersion() == LeaseVersion.EPK_RANGE_BASED_LEASE) {
            Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases();
        } else {
            Mockito.verify(partitionSynchronizerMock, times(2)).createMissingLeases();
        }
        Mockito.verify(leaseStoreMock, times(expectIllegalStateException ? 3 : 4)).isInitialized();
        Mockito.verify(leaseStoreMock, times(expectIllegalStateException ? 1 : 2)).acquireInitializationLock(Mockito.any());
    }

    private static ServiceItemLeaseV1 createEpkRangeBasedLeaseWithContinuation(
        boolean withContinuation,
        ChangeFeedMode changeFeedMode,
        ChangeFeedStartFromInternal startFromSettings,
        String databaseRid,
        String collectionRid,
        String pkRangeId,
        String leaseToken,
        String continuationToken) {

        ServiceItemLeaseV1 lease = new ServiceItemLeaseV1();

        lease.setId(String.format("%s_%s..%s", databaseRid, collectionRid, leaseToken));
        lease = lease.withLeaseToken(leaseToken);

        if (withContinuation) {
            FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRangeImpl = new FeedRangePartitionKeyRangeImpl(pkRangeId);
            String continuationAsJsonString = String.format(
                BASE_CONTINUATION_STRING_FOR_EPK_FULL_RANGE,
                collectionRid,
                continuationToken,
                pkRangeId);

            FeedRangeContinuation feedRangeContinuation = FeedRangeContinuation.convert(continuationAsJsonString);

            ChangeFeedState changeFeedState = new ChangeFeedStateV1(
                collectionRid,
                feedRangePartitionKeyRangeImpl,
                changeFeedMode,
                startFromSettings,
                feedRangeContinuation);

            lease.setContinuationToken(changeFeedState.toString());
        }

        return lease;
    }

    private static ServiceItemLease createPkRangeBasedLeaseWithContinuation(
        boolean withContinuation,
        String databaseRid,
        String collectionRid,
        String leaseToken,
        String continuationToken) {

        ServiceItemLease lease = new ServiceItemLease();

        lease.setId(String.format("%s_%s..%s", databaseRid, collectionRid, leaseToken));

        lease = lease.withLeaseToken(leaseToken);

        if (withContinuation) {
            lease = lease.withContinuationToken(continuationToken);
        }

        return lease;
    }
}
