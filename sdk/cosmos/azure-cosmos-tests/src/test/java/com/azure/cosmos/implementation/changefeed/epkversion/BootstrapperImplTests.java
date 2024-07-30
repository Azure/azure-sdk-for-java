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

import static org.mockito.Mockito.times;

public class BootstrapperImplTests {

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
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
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
                createLeaseWithContinuation(
                    false,
                    ChangeFeedMode.INCREMENTAL,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                false
            },
            {
                createLeaseWithContinuation(
                    false,
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                false
            }
        };
    }

    @Test(groups = {"unit"}, dataProvider = "leaseProvider")
    public void tryInitializeStoreFromEpkVersionLeaseStoreWithExistingLeases(ServiceItemLeaseV1 lease, boolean expectIllegalStateException) {
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
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.empty());

        LeaseStoreManager epkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.just(lease));
        Bootstrapper bootstrapper = new BootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            epkRangeVersionLeaseStoreManagerMock,
            ChangeFeedMode.FULL_FIDELITY);

        if (expectIllegalStateException) {
            Assert.assertThrows(IllegalStateException.class, () -> bootstrapper.initialize().block());
        } else {
            bootstrapper.initialize().block();
        }

        Mockito.verify(epkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases();
        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
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
