// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;

public class PkRangeIdVersionLeasesBootstrapperImplTests {

    private static final String baseContinuationStringForFullRange = "{\"V\":1," +
        "\"Rid\":\"%s\"," +
        "\"Continuation\":[" +
        "{\"token\":\"%s\",\"range\":{\"min\":\"\",\"max\":\"FF\"}}" +
        "]," +
        "\"PKRangeId\":\"%s\"}";

    @DataProvider(name = "leaseProvider")
    public Object[][] leaseProvider() {
        return new Object[][] {
            {
                createLeaseWithContinuation(
                    true,
                    ChangeFeedMode.INCREMENTAL,
                    ChangeFeedStartFromInternal.createFromBeginning(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                false
            },
            {
                createLeaseWithContinuation(
                    true,
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                true
            }
        };
    }

    @Test(groups = "unit")
    public void initializeStoreFromPkRangeIdVersionLeaseStore() {
        Duration lockTime = Duration.ofSeconds(5);
        Duration expireTIme = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases(Mockito.any())).thenReturn(Mono.empty());

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.empty());

        LeaseStoreManager pkRangeIdVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        LeaseStoreManager epkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.isInitialized()).thenReturn(Mono.just(true));
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.getAllLeases()).thenReturn(Flux.empty());
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.deleteAll(anyList())).thenReturn(Mono.empty());
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.releaseInitializationLock()).thenReturn(Mono.empty());
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.empty());
        Bootstrapper bootstrapper = new PkRangeIdVersionLeaseStoreBootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            pkRangeIdVersionLeaseStoreManagerMock,
            epkRangeVersionLeaseStoreManagerMock,
            ChangeFeedMode.INCREMENTAL);

        bootstrapper.initialize().block();

        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(2)).isInitialized();
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(1)).getAllLeases();
        Mockito.verify(epkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases(anyList());
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(1)).deleteAll(anyList());
        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
        // when the pkRangeId version lease store is already initialized, there is no need to acquire the lock then release it
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(0)).releaseInitializationLock();
    }

    @Test(groups = "unit")
    public void initializeStoreFromScratch() {
        Duration lockTime = Duration.ofSeconds(5);
        Duration expireTIme = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases()).thenReturn(Mono.empty());

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.just(true));

        LeaseStoreManager pkRangeIdVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        LeaseStoreManager epkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.isInitialized()).thenReturn(Mono.just(false));
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.markInitialized()).thenReturn(Mono.empty());
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.releaseInitializationLock()).thenReturn(Mono.just(true));
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.empty());
        Bootstrapper bootstrapper = new PkRangeIdVersionLeaseStoreBootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            pkRangeIdVersionLeaseStoreManagerMock,
            epkRangeVersionLeaseStoreManagerMock,
            ChangeFeedMode.INCREMENTAL);

        bootstrapper.initialize().block();
        Mockito.verify(partitionSynchronizerMock, times(0)).createMissingLeases(anyList());
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases();
        Mockito.verify(epkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));
        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
        // when the pkRangeId version lease store is not initialized, then we need to acquire the lock of it as well
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(1)).releaseInitializationLock();
    }

    @Test(groups = {"unit"}, dataProvider = "leaseProvider")
    public void tryInitializeStoreFromEpkVersionLeaseStoreWithExistingLeases(ServiceItemLeaseV1 lease, boolean expectIllegalStateException) {
        Duration lockTime = Duration.ofSeconds(5);
        Duration expireTIme = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases(Mockito.any())).thenReturn(Mono.empty());

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.empty());

        LeaseStoreManager pkRangeIdVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        LeaseStoreManager epkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.isInitialized()).thenReturn(Mono.just(true));
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.getAllLeases()).thenReturn(Flux.empty());
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.deleteAll(anyList())).thenReturn(Mono.empty());
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.releaseInitializationLock()).thenReturn(Mono.empty());
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.just(lease));
        Bootstrapper bootstrapper = new PkRangeIdVersionLeaseStoreBootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            pkRangeIdVersionLeaseStoreManagerMock,
            epkRangeVersionLeaseStoreManagerMock,
            ChangeFeedMode.INCREMENTAL);

        if (expectIllegalStateException) {
            Assert.expectThrows(IllegalStateException.class, () -> bootstrapper.initialize().block());
        } else {
            bootstrapper.initialize().block();
        }

        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(2)).isInitialized();
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(1)).getAllLeases();
        Mockito.verify(epkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases(anyList());
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(1)).deleteAll(anyList());
        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
        // when the pkRangeId version lease store is already initialized, there is no need to acquire the lock then release it
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(0)).releaseInitializationLock();
    }

    private static ServiceItemLeaseV1 createLeaseWithContinuation(
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
                baseContinuationStringForFullRange,
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
}
